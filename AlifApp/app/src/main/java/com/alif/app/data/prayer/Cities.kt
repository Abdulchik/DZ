package com.alif.app.data.prayer

data class City(val name: String, val lat: Double, val lng: Double, val zone: String)

object Cities {
    val all = listOf(
        City("Душанбе", 38.5598, 68.7870, "Asia/Dushanbe"),
        City("Худжанд", 40.2826, 69.6222, "Asia/Dushanbe"),
        City("Бохтар", 37.8364, 68.7809, "Asia/Dushanbe"),
        City("Куляб", 37.9146, 69.7845, "Asia/Dushanbe"),
        City("Истаравшан", 39.9108, 69.0064, "Asia/Dushanbe"),
        City("Турсунзаде", 38.5108, 68.2303, "Asia/Dushanbe"),
        City("Пенджикент", 39.4952, 67.6095, "Asia/Dushanbe"),
        City("Хорог", 37.4897, 71.5530, "Asia/Dushanbe"),
        City("Вахдат", 38.5563, 69.0135, "Asia/Dushanbe"),
        City("Исфара", 40.1265, 70.6253, "Asia/Dushanbe"),
        City("Москва", 55.7558, 37.6173, "Europe/Moscow"),
        City("Санкт-Петербург", 59.9311, 30.3609, "Europe/Moscow"),
        City("Казань", 55.7887, 49.1221, "Europe/Moscow"),
        City("Екатеринбург", 56.8389, 60.6057, "Asia/Yekaterinburg"),
        City("Новосибирск", 55.0084, 82.9357, "Asia/Novosibirsk"),
        City("Ташкент", 41.2995, 69.2401, "Asia/Tashkent"),
        City("Самарканд", 39.6542, 66.9597, "Asia/Samarkand"),
        City("Бишкек", 42.8746, 74.5698, "Asia/Bishkek"),
        City("Алматы", 43.2389, 76.8897, "Asia/Almaty"),
    )
}
