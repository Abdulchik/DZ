package com.alif.app.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.alif.app.AlifApplication
import com.alif.app.MainActivity
import com.alif.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

/** Ежедневное напоминание о занятиях в выбранное время. */
object ReminderScheduler {
    private const val CHANNEL_ID = "daily_reminder"
    private const val REQUEST = 1001

    private fun pendingIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context, REQUEST,
        Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    fun schedule(context: Context, hour: Int, minute: Int) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        // Неточный будильник: не требует особых разрешений, срабатывает в пределах нескольких минут
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, next.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent(context))
    }

    fun cancel(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context))
    }

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Напоминание о занятиях", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
    }

    fun show(context: Context, text: String) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        ensureChannel(context)
        val open = PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val n = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Время для урока")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(REQUEST, n)
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        val container = (context.applicationContext as AlifApplication).container
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = container.settingsRepository.current()
                if (!settings.reminderEnabled) return@launch
                val today = container.database.progress().activityOn(LocalDate.now().toString())?.lessonsDone ?: 0
                if (today >= settings.dailyGoal) return@launch // цель на сегодня уже выполнена
                val left = settings.dailyGoal - today
                ReminderScheduler.show(
                    context,
                    "Осталось уроков до цели: $left. Пара минут в день — и вы скоро будете читать Коран сами.",
                )
            } finally {
                pending.finish()
            }
        }
    }
}

/** После перезагрузки телефона будильники сбрасываются — ставим заново. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        val container = (context.applicationContext as AlifApplication).container
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val s = container.settingsRepository.settings.first()
                if (s.reminderEnabled) ReminderScheduler.schedule(context, s.reminderHour, s.reminderMinute)
            } finally {
                pending.finish()
            }
        }
    }
}
