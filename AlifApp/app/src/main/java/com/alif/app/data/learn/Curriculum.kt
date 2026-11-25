package com.alif.app.data.learn

import com.alif.app.R
import com.alif.app.data.learn.ExerciseKind.BUILD_WORD
import com.alif.app.data.learn.ExerciseKind.CHOOSE_ARABIC
import com.alif.app.data.learn.ExerciseKind.FIND_FORM
import com.alif.app.data.learn.ExerciseKind.HEAR_CHOOSE
import com.alif.app.data.learn.ExerciseKind.READ_CHECK
import com.alif.app.data.learn.ExerciseKind.SEE_CHOOSE

/**
 * Весь курс: 8 модулей, уроки, объяснения и материал для упражнений.
 * Тексты на русском; чтобы перевести курс, достаточно перевести строки в этом файле.
 */
object Curriculum {

    // ---------- Огласовки ----------
    const val FATHA = "َ"
    const val KASRA = "ِ"
    const val DAMMA = "ُ"
    const val SUKUN = "ْ"
    const val SHADDA = "ّ"
    const val FATHATAN = "ً"
    const val KASRATAN = "ٍ"
    const val DAMMATAN = "ٌ"

    /** Транскрипция согласного в слогах (точка снизу — твёрдый звук, черта — межзубный). */
    private val cons = mapOf(
        "alif" to "", "ba" to "б", "ta" to "т", "tha" to "с̱", "jim" to "дж", "hha" to "х̣",
        "kha" to "х", "dal" to "д", "dhal" to "з̱", "ra" to "р", "zay" to "з", "sin" to "с",
        "shin" to "ш", "sad" to "с̣", "dad" to "д̣", "tta" to "т̣", "zza" to "з̣", "ayn" to "ʿ",
        "ghayn" to "г", "fa" to "ф", "qaf" to "к̣", "kaf" to "к", "lam" to "л", "mim" to "м",
        "nun" to "н", "ha" to "h", "waw" to "в", "ya" to "й",
    )

    private fun carrier(l: Letter, mark: String): String = when {
        l.id != "alif" -> l.char
        mark == KASRA || mark == KASRATAN -> "إ"
        else -> "أ"
    }

    enum class Haraka(val mark: String, val suffix: String, val vowel: String) {
        FATHA_(FATHA, "fatha", "а"), KASRA_(KASRA, "kasra", "и"), DAMMA_(DAMMA, "damma", "у"),
        FATHATAN_(FATHATAN, "fathatan", "ан"), KASRATAN_(KASRATAN, "kasratan", "ин"), DAMMATAN_(DAMMATAN, "dammatan", "ун"),
    }

    fun syllable(l: Letter, h: Haraka): QuizItem {
        val tail = if (h == Haraka.FATHATAN_ && l.id != "alif") "ا" else ""
        return QuizItem(
            key = "syl:${l.id}_${h.suffix}",
            arabic = carrier(l, h.mark) + h.mark + tail,
            answer = cons.getValue(l.id) + h.vowel,
            audioKey = "syllables/${l.id}_${h.suffix}",
        )
    }

    fun sukun(l: Letter): QuizItem = QuizItem(
        key = "syl:${l.id}_sukun",
        arabic = "أَ" + l.char + SUKUN,
        answer = "а" + cons.getValue(l.id),
        audioKey = "syllables/${l.id}_sukun",
    )

    enum class Madd(val suffix: String, val vowel: String) { ALIF("madd_alif", "а̄"), WAW("madd_waw", "ӯ"), YA("madd_ya", "ӣ") }

    fun madd(l: Letter, m: Madd): QuizItem {
        val arabic = if (l.id == "alif") when (m) {
            Madd.ALIF -> "آ"; Madd.WAW -> "أُو"; Madd.YA -> "إِي"
        } else when (m) {
            Madd.ALIF -> l.char + FATHA + "ا"; Madd.WAW -> l.char + DAMMA + "و"; Madd.YA -> l.char + KASRA + "ي"
        }
        return QuizItem("madd:${l.id}_${m.suffix}", arabic, cons.getValue(l.id) + m.vowel, "madd/${l.id}_${m.suffix}")
    }

    fun letterItem(l: Letter) = QuizItem("letter:${l.id}", l.char, l.name, "letters/${l.id}", meaning = l.sound)

    // ---------- Слова ----------
    data class Word(val id: String, val arabic: String, val build: String, val translit: String, val meaning: String)

    private fun w(id: String, arabic: String, build: String, translit: String, meaning: String) = Word(id, arabic, build, translit, meaning)

    fun Word.item() = QuizItem("word:$id", arabic, translit, "words/$id", meaning, build)

    val shortWords = listOf(
        w("bab", "بَاب", "باب", "баб", "дверь"),
        w("kitab", "كِتَاب", "كتاب", "китаб", "книга"),
        w("qalam", "قَلَم", "قلم", "калам", "ручка"),
        w("walad", "وَلَد", "ولد", "валад", "мальчик"),
        w("bint", "بِنْت", "بنت", "бинт", "девочка"),
        w("bayt", "بَيْت", "بيت", "байт", "дом"),
        w("nur", "نُور", "نور", "нур", "свет"),
        w("shams", "شَمْس", "شمس", "шамс", "солнце"),
        w("qamar", "قَمَر", "قمر", "камар", "луна"),
        w("ab", "أَب", "اب", "аб", "отец"),
        w("yawm", "يَوْم", "يوم", "яум", "день"),
        w("samak", "سَمَك", "سمك", "самак", "рыба"),
        w("dars", "دَرْس", "درس", "дарс", "урок"),
        w("kataba", "كَتَبَ", "كتب", "катаба", "он написал"),
    )

    val quranWords = listOf(
        w("allah", "ٱللَّه", "الله", "Аллах", "Аллах"),
        w("bismi", "بِسْمِ", "بسم", "бисми", "во имя"),
        w("rahim", "رَحِيم", "رحيم", "рахим", "Милосердный"),
        w("hamd", "حَمْد", "حمد", "хамд", "хвала"),
        w("alamin", "عَالَمِين", "عالمين", "ʿалямин", "миры"),
        w("malik", "مَالِك", "مالك", "малик", "Владыка"),
        w("din", "دِين", "دين", "дин", "религия, воздаяние"),
        w("iyyaka", "إِيَّاكَ", "اياك", "иййака", "Тебе (одному)"),
        w("sirat", "صِرَاط", "صراط", "сират", "путь"),
        w("mustaqim", "مُسْتَقِيم", "مستقيم", "мустаким", "прямой"),
        w("qul", "قُلْ", "قل", "куль", "скажи"),
        w("ahad", "أَحَد", "احد", "ахад", "Единый"),
        w("samad", "صَمَد", "صمد", "самад", "Самодостаточный"),
        w("nas", "نَاس", "ناس", "нас", "люди"),
        w("falaq", "فَلَق", "فلق", "фалак", "рассвет"),
        w("kawthar", "كَوْثَر", "كوثر", "каусар", "изобилие"),
    )

    val specialWords = mapOf(
        "ta_marbuta" to listOf(
            w("madrasa", "مَدْرَسَة", "مدرسة", "мадраса", "школа"),
            w("salat", "صَلَاة", "صلاة", "салят", "молитва"),
            w("jannah", "جَنَّة", "جنة", "джанна", "рай"),
        ),
        "hamza" to listOf(
            w("sama", "سَمَاء", "سماء", "самаъ", "небо"),
            w("mumin", "مُؤْمِن", "مؤمن", "муъмин", "верующий"),
            w("suaal", "سُؤَال", "سؤال", "суъаль", "вопрос"),
            w("maa", "مَاء", "ماء", "маъ", "вода"),
        ),
        "alif_maqsura" to listOf(
            w("musa", "مُوسَى", "موسى", "Муса", "пророк Муса"),
            w("huda", "هُدًى", "هدى", "худан", "руководство"),
        ),
        "lam_alif" to listOf(
            w("la", "لَا", "لا", "ля", "нет"),
            w("illa", "إِلَّا", "الا", "илля", "кроме"),
            w("kalla", "كَلَّا", "كلا", "калля", "так нет же!"),
        ),
        "sun_moon" to listOf(
            w("ash_shams", "الشَّمْس", "الشمس", "аш-шамс", "солнце · солнечная буква"),
            w("ar_rahman", "الرَّحْمَٰن", "الرحمن", "ар-Рахман", "Милостивый · солнечная буква"),
            w("an_nur", "النُّور", "النور", "ан-нур", "свет · солнечная буква"),
            w("al_qamar", "الْقَمَر", "القمر", "аль-камар", "луна · лунная буква"),
            w("al_kitab", "الْكِتَاب", "الكتاب", "аль-китаб", "книга · лунная буква"),
            w("al_bayt", "الْبَيْت", "البيت", "аль-байт", "дом · лунная буква"),
        ),
    )

    /** Слова без огласовок для упражнений на формы и соединение букв. */
    val formWords = listOf(
        "كتب", "باب", "قلم", "ولد", "بنت", "بيت", "نور", "شمس", "قمر", "سمك", "درس", "جبل", "حبل",
        "خبز", "ذهب", "رجل", "زيت", "صبر", "ضحك", "طفل", "ظهر", "عين", "غرب", "فجر", "قلب", "كلب",
        "لحم", "ملك", "نجم", "وجه", "يد", "علم", "شجر", "مسجد", "سلام", "ثلج", "ثوب",
    )

    // ---------- Помощники ----------
    private val L = Alphabet.letters
    private fun ids(vararg id: String) = id.toList()
    private fun letters(ids: List<String>) = ids.map(Alphabet::get)

    private fun alphabetLesson(n: Int, title: String, letterIds: List<String>): Lesson {
        val ls = letters(letterIds)
        return Lesson(
            id = "alphabet_$n",
            moduleId = "alphabet",
            title = title,
            pages = buildList {
                if (n == 1) add(
                    LessonPage.Info(
                        "Добро пожаловать!",
                        "В арабском алфавите 28 букв. Пишут и читают справа налево. " +
                            "Почти все буквы — согласные звуки, а гласные обозначаются маленькими значками над и под буквами (их изучим позже).\n\n" +
                            "В каждом уроке: знакомство с буквами → прослушивание → упражнения → мини-тест.",
                        arabic = "ا ب ت ث",
                    )
                )
                ls.forEach { add(LessonPage.LetterIntro(it.id)) }
                val groups = ls.mapNotNull { Alphabet.similarFor(it.id) }.distinct()
                if (groups.isNotEmpty()) add(LessonPage.SimilarLetters(groups))
            },
            items = ls.map(::letterItem),
            kinds = listOf(SEE_CHOOSE, CHOOSE_ARABIC, HEAR_CHOOSE),
        )
    }

    private fun formsLesson(n: Int, letterIds: List<String>): Lesson {
        val ls = letters(letterIds)
        return Lesson(
            id = "forms_$n",
            moduleId = "forms",
            title = "Формы: " + ls.joinToString(" ") { it.char },
            pages = buildList {
                if (n == 1) add(
                    LessonPage.Info(
                        "У букв есть 4 формы",
                        "Внутри слова буквы соединяются, поэтому одна и та же буква выглядит по-разному:\n\n" +
                            "• отдельная — когда буква стоит одна;\n" +
                            "• начальная — в начале слова (соединяется только с буквой слева);\n" +
                            "• срединная — в середине (соединяется с обеих сторон);\n" +
                            "• конечная — в конце (соединяется только справа).\n\n" +
                            "Основа буквы (её «лицо» и точки) сохраняется, меняется только хвостик.",
                        arabic = "ب  بـ  ـبـ  ـب",
                        examples = listOf(Example("كتب", "три буквы ك ت ب в одном слове: «китаба» — он написал")),
                    )
                )
                add(LessonPage.FormsTable(letterIds))
            },
            items = ls.map(::letterItem),
            kinds = listOf(FIND_FORM, SEE_CHOOSE),
            formWords = formWords,
            formLetters = letterIds,
        )
    }

    private fun harakaGrid(h: Haraka, title: String, body: String): LessonPage =
        LessonPage.Grid(title, body, L.map { syllable(it, h) })

    // ---------- Модули ----------
    val modules: List<LearningModule> by lazy { buildModules() }

    private fun buildModules(): List<LearningModule> = listOf(
        LearningModule(
            "alphabet", 1, R.string.module_alphabet, R.string.module_alphabet_desc, "ا ب ت",
            listOf(
                alphabetLesson(1, "Алиф, ба, та, са", ids("alif", "ba", "ta", "tha")),
                alphabetLesson(2, "Джим, ха, хо, даль", ids("jim", "hha", "kha", "dal")),
                alphabetLesson(3, "Заль, ро, зай, син", ids("dhal", "ra", "zay", "sin")),
                alphabetLesson(4, "Шин, сод, дод, то", ids("shin", "sad", "dad", "tta")),
                alphabetLesson(5, "Зо, айн, гойн, фа", ids("zza", "ayn", "ghayn", "fa")),
                alphabetLesson(6, "Коф, каф, лям, мим", ids("qaf", "kaf", "lam", "mim")),
                alphabetLesson(7, "Нун, ха, вав, йа", ids("nun", "ha", "waw", "ya")),
                Lesson(
                    "alphabet_8", "alphabet", "Похожие буквы — повторение",
                    pages = listOf(
                        LessonPage.Info(
                            "Как не путать буквы",
                            "Многие буквы отличаются только точками: их количеством и местом (сверху или снизу). " +
                                "Смотрите сначала на основу буквы, потом — на точки.",
                            arabic = "ب ت ث ن ي",
                        ),
                        LessonPage.SimilarLetters(Alphabet.similarGroups),
                    ),
                    items = L.map(::letterItem),
                    kinds = listOf(SEE_CHOOSE, CHOOSE_ARABIC, HEAR_CHOOSE),
                ),
            ),
        ),
        LearningModule(
            "forms", 2, R.string.module_forms, R.string.module_forms_desc, "ـبـ",
            listOf(
                formsLesson(1, ids("alif", "ba", "ta", "tha", "jim", "hha", "kha")),
                formsLesson(2, ids("dal", "dhal", "ra", "zay", "sin", "shin", "sad")),
                formsLesson(3, ids("dad", "tta", "zza", "ayn", "ghayn", "fa", "qaf")),
                formsLesson(4, ids("kaf", "lam", "mim", "nun", "ha", "waw", "ya")),
            ),
        ),
        LearningModule(
            "joining", 3, R.string.module_joining, R.string.module_joining_desc, "كتب",
            listOf(
                Lesson(
                    "joining_1", "joining", "Как соединяются буквы",
                    pages = listOf(
                        LessonPage.Info(
                            "Слово пишется слитно",
                            "Арабские слова пишут не отдельными буквами, а одной линией — справа налево. " +
                                "Каждая буква соединяется с соседями тонкой чертой у основания строки.\n\n" +
                                "Посмотрите: отдельные буквы ك ت ب превращаются в слово.",
                            examples = listOf(
                                Example("ك + ت + ب = كتب", "катаба — он написал"),
                                Example("ق + ل + م = قلم", "калам — ручка"),
                                Example("ب + ي + ت = بيت", "байт — дом"),
                            ),
                        ),
                    ),
                    items = shortWords.take(8).map { it.item() },
                    kinds = listOf(BUILD_WORD, FIND_FORM),
                    formWords = formWords,
                ),
                Lesson(
                    "joining_2", "joining", "6 букв, которые не соединяются слева",
                    pages = listOf(
                        LessonPage.Info(
                            "Буквы-«эгоисты»",
                            "Шесть букв соединяются только с предыдущей буквой (справа), но никогда — со следующей (слева):\n\n" +
                                "ا  د  ذ  ر  ز  و\n\n" +
                                "После них слово как бы прерывается, и следующая буква пишется в начальной или отдельной форме.",
                            arabic = "ا د ذ ر ز و",
                            examples = listOf(
                                Example("درس", "дарс — урок: د не соединяется с ر, а ر — с س"),
                                Example("ولد", "валад — мальчик: после و разрыв"),
                                Example("باب", "баб — дверь: после ا разрыв"),
                            ),
                        ),
                    ),
                    items = Alphabet.nonJoining.map(::letterItem),
                    kinds = listOf(FIND_FORM, SEE_CHOOSE),
                    formWords = listOf("درس", "ولد", "باب", "ذهب", "زيت", "رجل", "نور", "دين", "ورد", "زاد", "سلام", "وجه"),
                    formLetters = Alphabet.nonJoining.map { it.id },
                ),
                Lesson(
                    "joining_3", "joining", "Конструктор слов",
                    pages = listOf(
                        LessonPage.Info(
                            "Соберите слово сами",
                            "На следующей странице — конструктор. Нажимайте буквы, и вы увидите, как они соединяются в слово. " +
                                "Попробуйте собрать: كتب, قلم, باب, درس.",
                        ),
                        LessonPage.JoinBuilder,
                    ),
                    items = shortWords.map { it.item() },
                    kinds = listOf(BUILD_WORD),
                ),
                Lesson(
                    "joining_4", "joining", "Практика: формы и соединение",
                    pages = listOf(
                        LessonPage.Info(
                            "Закрепим",
                            "Найдите, какой формой стоит буква в слове, и соберите слова из букв. " +
                                "Помните про 6 несоединяющихся букв: ا د ذ ر ز و.",
                        ),
                    ),
                    items = (shortWords + quranWords.take(6)).map { it.item() },
                    kinds = listOf(BUILD_WORD, FIND_FORM),
                    formWords = formWords,
                ),
            ),
        ),
        LearningModule(
            "harakat", 4, R.string.module_harakat, R.string.module_harakat_desc, "بَ بِ بُ",
            listOf(
                Lesson(
                    "harakat_1", "harakat", "Фатха — звук «а»",
                    pages = listOf(
                        LessonPage.Info(
                            "Что такое огласовки",
                            "Гласные звуки в Коране обозначаются значками-огласовками (харакатами).\n\n" +
                                "Фатха — косая черточка НАД буквой. Даёт короткий звук «а».\n" +
                                "ب + фатха = بَ «ба»",
                            arabic = "بَ  تَ  ثَ",
                            examples = listOf(Example("كَتَبَ", "катаба — он написал", "words/kataba")),
                        ),
                        harakaGrid(Haraka.FATHA_, "Все буквы с фатхой", "Нажмите на слог, чтобы послушать."),
                    ),
                    items = L.map { syllable(it, Haraka.FATHA_) },
                    kinds = listOf(SEE_CHOOSE, CHOOSE_ARABIC, HEAR_CHOOSE),
                ),
                Lesson(
                    "harakat_2", "harakat", "Кясра — звук «и»",
                    pages = listOf(
                        LessonPage.Info(
                            "Кясра",
                            "Кясра — косая черточка ПОД буквой. Даёт короткий звук «и».\nب + кясра = بِ «би»",
                            arabic = "بِ  تِ  ثِ",
                            examples = listOf(Example("بِسْمِ", "бисми — во имя", "words/bismi")),
                        ),
                        harakaGrid(Haraka.KASRA_, "Все буквы с кясрой", "Нажмите на слог, чтобы послушать."),
                    ),
                    items = L.map { syllable(it, Haraka.KASRA_) } + L.take(8).map { syllable(it, Haraka.FATHA_) },
                    kinds = listOf(SEE_CHOOSE, CHOOSE_ARABIC, HEAR_CHOOSE),
                ),
                Lesson(
                    "harakat_3", "harakat", "Дамма — звук «у»",
                    pages = listOf(
                        LessonPage.Info(
                            "Дамма",
                            "Дамма — маленькая «запятая» (как буква و) НАД буквой. Даёт короткий звук «у».\nب + дамма = بُ «бу»",
                            arabic = "بُ  تُ  ثُ",
                            examples = listOf(Example("قُلْ", "куль — скажи", "words/qul")),
                        ),
                        harakaGrid(Haraka.DAMMA_, "Все буквы с даммой", "Нажмите на слог, чтобы послушать."),
                        LessonPage.Grid(
                            "Сравните: а — и — у",
                            "Одна буква — три разных слога.",
                            L.take(10).flatMap { l -> listOf(syllable(l, Haraka.FATHA_), syllable(l, Haraka.KASRA_), syllable(l, Haraka.DAMMA_)) },
                        ),
                    ),
                    items = L.flatMap { l -> listOf(syllable(l, Haraka.FATHA_), syllable(l, Haraka.KASRA_), syllable(l, Haraka.DAMMA_)) },
                    kinds = listOf(SEE_CHOOSE, CHOOSE_ARABIC, HEAR_CHOOSE),
                ),
                Lesson(
                    "harakat_4", "harakat", "Сукун — без гласной",
                    pages = listOf(
                        LessonPage.Info(
                            "Сукун",
                            "Сукун — маленький кружок над буквой. Он означает, что после согласного НЕТ гласной: " +
                                "звук «закрывает» слог.\nأَبْ — «аб», مِنْ — «мин», قُلْ — «куль».",
                            arabic = "أَبْ  مِنْ  قُلْ",
                            examples = listOf(
                                Example("بِنْت", "бинт — девочка", "words/bint"),
                                Example("دَرْس", "дарс — урок", "words/dars"),
                            ),
                        ),
                        LessonPage.Grid("Слоги с сукуном", "«а» + согласный без гласной.", L.drop(1).map(::sukun)),
                    ),
                    items = L.drop(1).map(::sukun),
                    kinds = listOf(SEE_CHOOSE, CHOOSE_ARABIC, HEAR_CHOOSE),
                ),
                Lesson(
                    "harakat_5", "harakat", "Шадда — удвоение",
                    pages = listOf(
                        LessonPage.Info(
                            "Шадда",
                            "Шадда — значок, похожий на маленькую «ш» (ّ) над буквой. Буква с шаддой произносится ДВАЖДЫ: " +
                                "первый раз с сукуном, второй — с огласовкой.\nرَبّ — «рабб», أُمّ — «умм», جَنَّة — «джанна».",
                            arabic = "بَّ = بْ + بَ",
                            examples = listOf(
                                Example("رَبّ", "рабб — Господь", "words/rabb"),
                                Example("أُمّ", "умм — мать", "words/umm"),
                                Example("جَنَّة", "джанна — рай", "words/jannah"),
                                Example("إِيَّاكَ", "иййака — Тебе одному", "words/iyyaka"),
                            ),
                        ),
                    ),
                    items = listOf(
                        QuizItem("word:rabb", "رَبّ", "рабб", "words/rabb", "Господь"),
                        QuizItem("word:umm", "أُمّ", "умм", "words/umm", "мать"),
                        QuizItem("word:jannah", "جَنَّة", "джанна", "words/jannah", "рай"),
                        QuizItem("word:iyyaka", "إِيَّاكَ", "иййака", "words/iyyaka", "Тебе одному"),
                        QuizItem("shadda:abba", "أَبَّ", "абба"),
                        QuizItem("shadda:ammaa", "عَمَّ", "ʿамма"),
                        QuizItem("shadda:kalla", "كَلَّا", "калля", "words/kalla"),
                        QuizItem("shadda:inna", "إِنَّ", "инна"),
                    ),
                    kinds = listOf(SEE_CHOOSE, CHOOSE_ARABIC, READ_CHECK),
                ),
                Lesson(
                    "harakat_6", "harakat", "Танвин — «ан», «ин», «ун»",
                    pages = listOf(
                        LessonPage.Info(
                            "Танвин",
                            "Танвин — двойная огласовка в конце слова. Добавляет звук «н»:\n" +
                                "ً двойная фатха — «ан» (обычно с алифом: بًا),\n" +
                                "ٍ двойная кясра — «ин»,\n" +
                                "ٌ двойная дамма — «ун».",
                            arabic = "بًا  بٍ  بٌ",
                            examples = listOf(
                                Example("كِتَابًا", "китабан — книгу", "words/kitaban"),
                                Example("أَحَدٌ", "ахадун — Единый", "words/ahad"),
                            ),
                        ),
                        LessonPage.Grid(
                            "Слоги с танвином", "Нажмите, чтобы послушать.",
                            L.take(12).flatMap { l -> listOf(syllable(l, Haraka.FATHATAN_), syllable(l, Haraka.KASRATAN_), syllable(l, Haraka.DAMMATAN_)) },
                        ),
                    ),
                    items = L.flatMap { l -> listOf(syllable(l, Haraka.FATHATAN_), syllable(l, Haraka.KASRATAN_), syllable(l, Haraka.DAMMATAN_)) },
                    kinds = listOf(SEE_CHOOSE, CHOOSE_ARABIC, HEAR_CHOOSE),
                ),
            ),
        ),
        LearningModule(
            "madd", 5, R.string.module_madd, R.string.module_madd_desc, "بَا",
            listOf(
                Lesson(
                    "madd_1", "madd", "Долгое «а» — алиф",
                    pages = listOf(
                        LessonPage.Info(
                            "Мадд — удлинение",
                            "Если после буквы с фатхой стоит алиф ا, звук «а» тянется вдвое дольше: «аа».\n" +
                                "بَ «ба» → بَا «баа».\nДлительность — 2 счёта (как два коротких слога).",
                            arabic = "بَ → بَا",
                            examples = listOf(Example("بَاب", "баб — дверь", "words/bab"), Example("مَالِك", "малик — Владыка", "words/malik")),
                        ),
                        LessonPage.Grid("Долгое «а»", "Нажмите, чтобы послушать.", L.map { madd(it, Madd.ALIF) }),
                    ),
                    items = L.map { madd(it, Madd.ALIF) } + L.take(10).map { syllable(it, Haraka.FATHA_) },
                    kinds = listOf(SEE_CHOOSE, CHOOSE_ARABIC, HEAR_CHOOSE),
                ),
                Lesson(
                    "madd_2", "madd", "Долгое «у» — вав",
                    pages = listOf(
                        LessonPage.Info(
                            "Долгое «у»",
                            "Дамма + و без огласовки = долгое «уу».\nبُ «бу» → بُو «буу».",
                            arabic = "بُ → بُو",
                            examples = listOf(Example("نُور", "нур — свет", "words/nur")),
                        ),
                        LessonPage.Grid("Долгое «у»", "Нажмите, чтобы послушать.", L.map { madd(it, Madd.WAW) }),
                    ),
                    items = L.map { madd(it, Madd.WAW) } + L.take(10).map { syllable(it, Haraka.DAMMA_) },
                    kinds = listOf(SEE_CHOOSE, CHOOSE_ARABIC, HEAR_CHOOSE),
                ),
                Lesson(
                    "madd_3", "madd", "Долгое «и» — йа",
                    pages = listOf(
                        LessonPage.Info(
                            "Долгое «и»",
                            "Кясра + ي без огласовки = долгое «ии».\nبِ «би» → بِي «бии».",
                            arabic = "بِ → بِي",
                            examples = listOf(Example("دِين", "дин — религия", "words/din"), Example("رَحِيم", "рахим — Милосердный", "words/rahim")),
                        ),
                        LessonPage.Grid("Долгое «и»", "Нажмите, чтобы послушать.", L.map { madd(it, Madd.YA) }),
                    ),
                    items = L.flatMap { l -> listOf(madd(l, Madd.ALIF), madd(l, Madd.WAW), madd(l, Madd.YA)) },
                    kinds = listOf(SEE_CHOOSE, CHOOSE_ARABIC, HEAR_CHOOSE),
                ),
            ),
        ),
        LearningModule(
            "special", 6, R.string.module_special, R.string.module_special_desc, "ة ء لا",
            listOf(
                specialLesson(
                    1, "Та марбута ة", "ta_marbuta",
                    "Та марбута — «связанная та». Пишется только в конце слова и выглядит как ه с двумя точками.\n\n" +
                        "• При продолжении чтения звучит как «т»: صَلَاةُ ٱللَّهِ — «салятуллах».\n" +
                        "• При остановке на ней — как лёгкое «h» или не звучит: مَدْرَسَة — «мадраса».",
                    "ة  ـة",
                ),
                specialLesson(
                    2, "Хамза ء", "hamza",
                    "Хамза — короткий толчок горлом, как пауза в русском «не-а». Может стоять отдельно (ء) " +
                        "или на опоре: на алифе أ إ, на вав ؤ, на «подставке» ئ.\nأَ «а», إِ «и», أُ «у».",
                    "ء  أ  إ  ؤ  ئ",
                ),
                specialLesson(
                    3, "Алиф максура ى", "alif_maqsura",
                    "Алиф максура — «укороченный алиф». Похожа на ي, но без точек. Стоит в конце слова и читается " +
                        "как долгое «а»: مُوسَى — «Муса», عَلَى — «аля».",
                    "ى",
                ),
                specialLesson(
                    4, "Лям-алиф لا", "lam_alif",
                    "Когда за ل идёт ا, их пишут особым знаком-лигатурой: لا. Читается «ля».\n" +
                        "Это две буквы, а не одна!",
                    "ل + ا = لا",
                ),
                Lesson(
                    "special_5", "special", "Солнечные и лунные буквы",
                    pages = listOf(
                        LessonPage.Info(
                            "Артикль ال",
                            "Слово «аль» (ال) — как английское «the». Но звук «л» в нём слышен не всегда.\n\n" +
                                "Лунные буквы (14): ا ب ج ح خ ع غ ف ق ك م ه و ي — «л» читается: الْقَمَر «аль-камар».\n\n" +
                                "Солнечные буквы (14): ت ث د ذ ر ز س ش ص ض ط ظ ل ن — «л» не читается, а следующая буква удваивается " +
                                "(над ней шадда): الشَّمْس «аш-шамс».",
                            arabic = "الْقَمَر  ·  الشَّمْس",
                            examples = specialWords.getValue("sun_moon").map { Example(it.arabic, "${it.translit} — ${it.meaning}", "words/${it.id}") },
                        ),
                    ),
                    items = specialWords.getValue("sun_moon").map { it.item() } + listOf(
                        QuizItem("sunmoon:ta", "ت", "солнечная"), QuizItem("sunmoon:sin", "س", "солнечная"),
                        QuizItem("sunmoon:ra", "ر", "солнечная"), QuizItem("sunmoon:nun", "ن", "солнечная"),
                        QuizItem("sunmoon:ba", "ب", "лунная"), QuizItem("sunmoon:qaf", "ق", "лунная"),
                        QuizItem("sunmoon:mim", "م", "лунная"), QuizItem("sunmoon:kaf", "ك", "лунная"),
                    ),
                    kinds = listOf(SEE_CHOOSE, READ_CHECK),
                ),
            ),
        ),
        LearningModule(
            "reading", 7, R.string.module_reading, R.string.module_reading_desc, "كِتَاب",
            listOf(
                Lesson(
                    "reading_1", "reading", "Читаем слоги подряд",
                    pages = listOf(
                        LessonPage.Info(
                            "Как читать слово",
                            "Читайте справа налево, слог за слогом: буква + её огласовка.\n" +
                                "كَتَبَ = كَ «ка» + تَ «та» + بَ «ба» → «катаба».\nСначала медленно, потом слитно.",
                            arabic = "كَ · تَ · بَ",
                        ),
                    ),
                    items = listOf(
                        QuizItem("read:kataba", "كَتَبَ", "катаба", "words/kataba", "он написал"),
                        QuizItem("read:jalasa", "جَلَسَ", "джаляса", meaning = "он сел"),
                        QuizItem("read:dhahaba", "ذَهَبَ", "захаба", meaning = "он ушёл"),
                        QuizItem("read:fataha", "فَتَحَ", "фатаха", meaning = "он открыл"),
                        QuizItem("read:khalaqa", "خَلَقَ", "халяка", meaning = "он сотворил"),
                        QuizItem("read:ahad", "أَحَدٌ", "ахадун", "words/ahad", "Единый"),
                        QuizItem("read:samad", "صَمَدُ", "самаду", "words/samad", "Самодостаточный"),
                        QuizItem("read:falaq", "فَلَقِ", "фаляки", "words/falaq", "рассвет"),
                    ),
                    kinds = listOf(READ_CHECK, SEE_CHOOSE),
                ),
                readingLesson(2, "Короткие слова", shortWords.take(7)),
                readingLesson(3, "Слова с долгими гласными и сукуном", shortWords.drop(7)),
                readingLesson(4, "Слова из Корана I", quranWords.take(8)),
                readingLesson(5, "Слова из Корана II", quranWords.drop(8)),
                Lesson(
                    "reading_6", "reading", "Читаем аят: Аль-Фатиха 1–2",
                    pages = listOf(
                        LessonPage.Info(
                            "Ваш первый аят",
                            "Вы знаете все буквы и знаки, чтобы прочитать начало Корана. Прочитайте сами, затем послушайте чтеца.",
                            examples = listOf(
                                Example("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", "Бисмилляхи-р-рахмани-р-рахим", surah = 1, ayah = 1),
                                Example("ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ", "Аль-хамду лилляхи рабби-ль-ʿалямин", surah = 1, ayah = 2),
                            ),
                        ),
                    ),
                    items = listOf(
                        QuizItem("ayah:1_1", "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", "Бисмилляхи-р-рахмани-р-рахим", meaning = "Во имя Аллаха, Милостивого, Милосердного", surah = 1, ayah = 1),
                        QuizItem("ayah:1_2", "ٱلْحَمْدُ لِلَّهِ رَبِّ ٱلْعَٰلَمِينَ", "Аль-хамду лилляхи рабби-ль-ʿалямин", meaning = "Хвала Аллаху, Господу миров", surah = 1, ayah = 2),
                        QuizItem("ayah:1_3", "ٱلرَّحْمَٰنِ ٱلرَّحِيمِ", "Ар-рахмани-р-рахим", meaning = "Милостивому, Милосердному", surah = 1, ayah = 3),
                        QuizItem("ayah:1_4", "مَٰلِكِ يَوْمِ ٱلدِّينِ", "Малики яуми-д-дин", meaning = "Властелину Дня воздаяния", surah = 1, ayah = 4),
                        QuizItem("ayah:112_1", "قُلْ هُوَ ٱللَّهُ أَحَدٌ", "Куль хуа-Ллаху ахад", meaning = "Скажи: «Он — Аллах Единый»", surah = 112, ayah = 1),
                    ),
                    kinds = listOf(READ_CHECK, SEE_CHOOSE),
                ),
            ),
        ),
        LearningModule(
            "tajweed", 8, R.string.module_tajweed, R.string.module_tajweed_desc, "ۚ ۖ",
            listOf(
                tajweedLesson(
                    1, "Что такое таджвид. Мадд", "Таджвид — правила красивого и правильного чтения Корана: как произносить буквы, где тянуть звук, где делать гунну.\n\n" +
                        "Естественный мадд (2 счёта): долгая гласная без хамзы и сукуна после неё.\n" +
                        "Связанный мадд (4–5 счётов): после долгой гласной в том же слове стоит хамза — над буквой знак ٓ.\n" +
                        "Разделённый мадд (4–5): долгая гласная в конце слова, хамза — в начале следующего.",
                    listOf(
                        Triple("مَٰلِكِ", "Естественный мадд", 1 to 4),
                        Triple("جَآءَ", "Связанный мадд", 110 to 1),
                        Triple("إِنَّآ أَعْطَيْنَٰكَ", "Разделённый мадд", 108 to 1),
                        Triple("ٱلنَّاسِ", "Естественный мадд", 114 to 1),
                    ),
                ),
                tajweedLesson(
                    2, "Нун с сукуном и танвин", "Когда после نْ или танвина идёт определённая буква, звук «н» меняется:\n\n" +
                        "• Изхар — «н» читается чётко (перед горловыми ء ه ع ح غ خ).\n" +
                        "• Идгам — «н» сливается со следующей буквой (ي ر م ل و ن).\n" +
                        "• Икляб — «н» превращается в «м» (перед ب).\n" +
                        "• Ихфа — «н» произносится скрыто, в нос (перед остальными 15 буквами).",
                    listOf(
                        Triple("مِّنْ خَوْفٍ", "Изхар", 106 to 4),
                        Triple("كُفُوًا أَحَدٌ", "Изхар", 112 to 4),
                        Triple("مَالًا وَعَدَّدَهُۥ", "Идгам", 104 to 2),
                        Triple("لَيُنۢبَذَنَّ", "Икляб", 104 to 4),
                        Triple("مِّن جُوعٍ", "Ихфа", 106 to 4),
                    ),
                ),
                tajweedLesson(
                    3, "Мим с сукуном", "Правила для مْ:\n\n" +
                        "• Ихфа шафави — перед ب «м» произносится мягко, губы почти сомкнуты, с гунной.\n" +
                        "• Идгам мислейн — перед م две «м» сливаются в одну долгую с гунной.\n" +
                        "• Изхар шафави — перед остальными буквами «м» читается чётко.",
                    listOf(
                        Triple("تَرْمِيهِم بِحِجَارَةٍ", "Ихфа шафави", 105 to 4),
                        Triple("أَطْعَمَهُم مِّن", "Идгам мислейн", 106 to 4),
                        Triple("أَنْعَمْتَ عَلَيْهِمْ", "Изхар шафави", 1 to 7),
                    ),
                ),
                tajweedLesson(
                    4, "Калькаля — «отскок»", "Пять букв ق ط ب ج د (запомните: «قُطْبُ جَدٍّ») при сукуне или остановке произносятся с лёгким «отскоком» — " +
                        "как будто после них короткое эхо.",
                    listOf(
                        Triple("ٱلْفَلَقِ", "Калькаля", 113 to 1),
                        Triple("أَحَدٌ", "Калькаля", 112 to 1),
                        Triple("تَبَّتْ يَدَآ أَبِى لَهَبٍ وَتَبَّ", "Калькаля", 111 to 1),
                    ),
                ),
                tajweedLesson(
                    5, "Гунна и слово «Аллах»", "Гунна — носовой звук длиной 2 счёта. Обязательна у نّ и مّ (с шаддой).\n\n" +
                        "Лям в слове «Аллах»:\n• твёрдый (тафхим) — после «а» или «у»: هُوَ ٱللَّهُ;\n• мягкий (таркик) — после «и»: بِسْمِ ٱللَّهِ.",
                    listOf(
                        Triple("ٱلنَّاسِ", "Гунна", 114 to 1),
                        Triple("إِنَّ شَانِئَكَ", "Гунна", 108 to 3),
                        Triple("هُوَ ٱللَّهُ", "Твёрдый лям", 112 to 1),
                        Triple("بِسْمِ ٱللَّهِ", "Мягкий лям", 1 to 1),
                    ),
                ),
            ),
        ),
    )

    private fun specialLesson(n: Int, title: String, group: String, body: String, arabic: String): Lesson {
        val words = specialWords.getValue(group)
        return Lesson(
            "special_$n", "special", title,
            pages = listOf(
                LessonPage.Info(title, body, arabic, words.map { Example(it.arabic, "${it.translit} — ${it.meaning}", "words/${it.id}") }),
            ),
            items = words.map { it.item() } + shortWords.take(4).map { it.item() },
            kinds = listOf(SEE_CHOOSE, CHOOSE_ARABIC, READ_CHECK, HEAR_CHOOSE),
        )
    }

    private fun readingLesson(n: Int, title: String, words: List<Word>) = Lesson(
        "reading_$n", "reading", title,
        pages = listOf(
            LessonPage.Info(
                title, "Прочитайте слово сами, затем нажмите «Проверить» — увидите транскрипцию и услышите правильное чтение.",
                examples = words.map { Example(it.arabic, "${it.translit} — ${it.meaning}", "words/${it.id}") },
            ),
        ),
        items = words.map { it.item() },
        kinds = listOf(READ_CHECK, SEE_CHOOSE, BUILD_WORD, HEAR_CHOOSE),
    )

    private fun tajweedLesson(n: Int, title: String, body: String, examples: List<Triple<String, String, Pair<Int, Int>>>) = Lesson(
        "tajweed_$n", "tajweed", title,
        pages = listOf(
            LessonPage.Info(
                title, body,
                examples = examples.map { (ar, rule, ref) ->
                    Example(ar, "$rule · сура ${ref.first}, аят ${ref.second}", surah = ref.first, ayah = ref.second)
                },
            ),
        ),
        items = examples.mapIndexed { i, (ar, rule, ref) ->
            QuizItem("tajweed:${n}_$i", ar, rule, meaning = "сура ${ref.first}, аят ${ref.second}", surah = ref.first, ayah = ref.second)
        },
        kinds = listOf(SEE_CHOOSE, READ_CHECK),
    )

    // ---------- Поиск ----------
    val allLessons: List<Lesson> by lazy { modules.flatMap { it.lessons } }
    fun lesson(id: String): Lesson? = allLessons.firstOrNull { it.id == id }
    fun module(id: String): LearningModule? = modules.firstOrNull { it.id == id }

    /** Все элементы курса по ключу — нужно для повторения ошибок. */
    val itemsByKey: Map<String, QuizItem> by lazy {
        allLessons.flatMap { it.items }.associateBy { it.key }
    }

    /** Похожие элементы для неправильных вариантов (тот же тип). */
    fun sameKind(item: QuizItem): List<QuizItem> {
        val prefix = item.key.substringBefore(':')
        return itemsByKey.values.filter { it.key.substringBefore(':') == prefix && it.key != item.key }
    }
}
