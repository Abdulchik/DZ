package com.alif.app.data.learn

/**
 * 28 букв арабского алфавита.
 * id совпадает с именем аудиофайла: assets/audio/letters/<id>.mp3
 */
data class Letter(
    val id: String,
    val char: String,
    /** Название буквы по-русски. */
    val name: String,
    /** Какой звук передаёт (коротко). */
    val sound: String,
    /** Как произносить. */
    val tip: String,
    /** Соединяется ли со следующей буквой (слева). 6 букв — нет. */
    val joinsLeft: Boolean = true,
) {
    // Формы получаем через невидимый соединитель ZWJ — шрифт сам рисует нужную форму
    val isolated: String get() = char
    val initial: String get() = if (joinsLeft) "$char$ZWJ" else char
    val medial: String get() = if (joinsLeft) "$ZWJ$char$ZWJ" else "$ZWJ$char"
    val final: String get() = "$ZWJ$char"

    fun form(f: LetterForm): String = when (f) {
        LetterForm.ISOLATED -> isolated
        LetterForm.INITIAL -> initial
        LetterForm.MEDIAL -> medial
        LetterForm.FINAL -> final
    }

    companion object {
        const val ZWJ = "‍"
    }
}

enum class LetterForm(val title: String) {
    ISOLATED("Отдельная"),
    INITIAL("Начальная"),
    MEDIAL("Срединная"),
    FINAL("Конечная"),
}

/** Группа похожих букв с подсказкой, как их различать. */
data class SimilarGroup(val letterIds: List<String>, val tip: String)

object Alphabet {
    val letters: List<Letter> = listOf(
        Letter("alif", "ا", "алиф", "а / опора для хамзы",
            "Своего звука не имеет. Служит опорой для хамзы (короткий толчок горлом + а, и, у) или удлиняет звук «а».", joinsLeft = false),
        Letter("ba", "ب", "ба", "б", "Как русское «б»."),
        Letter("ta", "ت", "та", "т", "Как русское «т», кончик языка у верхних зубов."),
        Letter("tha", "ث", "са", "с межзубный", "Кончик языка между зубами, выдыхаем «с» — как английское th в слове think."),
        Letter("jim", "ج", "джим", "дж", "Слитное «дж», как в слове «джинсы»."),
        Letter("hha", "ح", "ха", "х гортанный", "Глухое «х» из середины горла: как будто сильно дышите на стекло, сжав горло."),
        Letter("kha", "خ", "хо", "х хриплый", "Хриплое «х» из верхней части горла, как в слове «хор», но грубее."),
        Letter("dal", "د", "даль", "д", "Как русское «д».", joinsLeft = false),
        Letter("dhal", "ذ", "заль", "з межзубный", "Кончик языка между зубами, звонкое «з» — как английское th в слове this.", joinsLeft = false),
        Letter("ra", "ر", "ро", "р", "Раскатистое «р», чаще твёрдое.", joinsLeft = false),
        Letter("zay", "ز", "зай", "з", "Как русское «з».", joinsLeft = false),
        Letter("sin", "س", "син", "с", "Как русское «с»."),
        Letter("shin", "ش", "шин", "ш", "Мягкое «ш»."),
        Letter("sad", "ص", "сод", "с твёрдый", "Твёрдое «толстое» «с»: язык прижат, рот слегка округлён. Гласные рядом звучат глуше."),
        Letter("dad", "ض", "дод", "д твёрдый", "Твёрдое «д»: боковой край языка прижат к коренным зубам."),
        Letter("tta", "ط", "то", "т твёрдый", "Твёрдое «т»: язык плотно прижат к нёбу, звук глубокий."),
        Letter("zza", "ظ", "зо", "з твёрдый межзубный", "Как ذ, но твёрже и глубже: язык между зубами, задняя часть языка поднята."),
        Letter("ayn", "ع", "айн", "ʿ горловой", "Особый звук из середины горла: горло сжимается, как при сдавленном «а»."),
        Letter("ghayn", "غ", "гойн", "г картавое", "Картавое «г», как французское r."),
        Letter("fa", "ف", "фа", "ф", "Как русское «ф»."),
        Letter("qaf", "ق", "коф", "к глубокий", "Глубокое «к» у самого язычка — звук идёт из горла."),
        Letter("kaf", "ك", "каф", "к", "Мягкое «к», как в слове «кит»."),
        Letter("lam", "ل", "лям", "л", "Мягкое «л», как в слове «лето»."),
        Letter("mim", "م", "мим", "м", "Как русское «м»."),
        Letter("nun", "ن", "нун", "н", "Как русское «н»."),
        Letter("ha", "ه", "ха", "h лёгкий", "Лёгкий выдох, как английское h в слове hello."),
        Letter("waw", "و", "вав", "в (w) / у", "Губы трубочкой, как английское w. Как долгая гласная — «у».", joinsLeft = false),
        Letter("ya", "ي", "йа", "й / и", "Как «й». Как долгая гласная — «и»."),
    )

    private val byId = letters.associateBy { it.id }
    private val byChar = letters.associateBy { it.char }

    fun get(id: String): Letter = byId.getValue(id)
    fun byChar(c: String): Letter? = byChar[c] ?: when (c) {
        "أ", "إ", "آ", "ٱ" -> byChar["ا"]
        "ة" -> byChar["ه"]
        "ى", "ئ" -> byChar["ي"]
        "ؤ" -> byChar["و"]
        else -> null
    }

    val nonJoining: List<Letter> get() = letters.filter { !it.joinsLeft }

    val similarGroups = listOf(
        SimilarGroup(listOf("ba", "ta", "tha"), "Одинаковая «лодочка», разные точки: ب — одна точка снизу, ت — две сверху, ث — три сверху."),
        SimilarGroup(listOf("jim", "hha", "kha"), "Одинаковая форма: ج — точка внутри снизу, ح — без точки, خ — точка сверху."),
        SimilarGroup(listOf("dal", "dhal"), "ذ — это د с точкой сверху."),
        SimilarGroup(listOf("ra", "zay"), "ز — это ر с точкой сверху."),
        SimilarGroup(listOf("sin", "shin"), "ش — это س с тремя точками сверху."),
        SimilarGroup(listOf("sad", "dad"), "ض — это ص с точкой сверху."),
        SimilarGroup(listOf("tta", "zza"), "ظ — это ط с точкой сверху."),
        SimilarGroup(listOf("ayn", "ghayn"), "غ — это ع с точкой сверху."),
        SimilarGroup(listOf("fa", "qaf"), "ف — одна точка, хвост плоский; ق — две точки, хвост глубокий и круглый."),
        SimilarGroup(listOf("alif", "lam"), "ا — прямая палочка; ل — палочка с крючком внизу."),
        SimilarGroup(listOf("nun", "ba"), "ن — точка сверху и глубокая чаша; ب — точка снизу, форма плоская."),
        SimilarGroup(listOf("ha", "waw", "mim"), "ه — петелька; و — петля с хвостом вниз; م — кружок с хвостом."),
    )

    fun similarFor(id: String): SimilarGroup? = similarGroups.firstOrNull { id in it.letterIds }

    /** Разбивает слово без огласовок на буквы (лям-алиф — две буквы). */
    fun lettersOf(word: String): List<Letter> = word.mapNotNull { byChar(it.toString()) }
}
