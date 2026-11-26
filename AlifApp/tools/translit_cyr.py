#!/usr/bin/env python3
"""
Транслитерация текста Корана (Tanzil Uthmani) кириллицей — в стиле таджикских изданий:
  Бисмиллāҳи-р-раҳмāни-р-раҳӣм.  Ал-ҳамду лиллāҳи рабби-л-ъāламӣн.
Долгие гласные: ā ӣ ӯ (после твёрдых согласных долгое «а» → ō), ъ — айн и хамза,
ҳ — ح и ه, қ — ق, с̇ — ص, з̆ — ض, т̣ — ط, з̣ — ظ, з́ — ذ, с̱ — ث.
Учитывает: шадду, сукун, танвин, алиф-васлу, солнечные буквы, слово «Аллах»,
остановку в конце аята, та марбуту, малые знаки Усмани (ٰ ۥ ۦ ۟).
"""

FATHA, DAMMA, KASRA = "َ", "ُ", "ِ"
FATHATAN, DAMMATAN, KASRATAN = "ً", "ٌ", "ٍ"
SUKUN, SHADDA = "ْ", "ّ"
DAGGER, MADDAH, HAMZA_ABOVE = "ٰ", "ٓ", "ٔ"
SMALL_WAW, SMALL_YA = "ۥ", "ۦ"
SILENT = {"۟", "۠"}
TATWEEL = "ـ"
WASLA = "ٱ"

CONS = {
    "ب": "б", "ت": "т", "ث": "с̱", "ج": "дж", "ح": "ҳ", "خ": "х", "د": "д", "ذ": "з́",
    "ر": "р", "ز": "з", "س": "с", "ش": "ш", "ص": "с̇", "ض": "з̆", "ط": "т̣", "ظ": "з̣",
    "ع": "ъ", "غ": "г", "ف": "ф", "ق": "қ", "ك": "к", "ل": "л", "م": "м", "ن": "н",
    "ه": "ҳ", "و": "в", "ي": "й", "ى": "й",
}
HAMZAS = {"ء", "أ", "إ", "ؤ", "ئ"}
EMPHATIC = {"ص", "ض", "ط", "ظ", "ق", "خ", "غ", "ر"}
SUN = {"ت", "ث", "د", "ذ", "ر", "ز", "س", "ش", "ص", "ض", "ط", "ظ", "ل", "ن"}
MARK_SET = {FATHA, DAMMA, KASRA, FATHATAN, DAMMATAN, KASRATAN, SUKUN, SHADDA, DAGGER, MADDAH,
            HAMZA_ABOVE, SMALL_WAW, SMALL_YA} | SILENT
PREFIXES = {"ва", "фа", "би", "ли", "ка", "ла", "са"}
LONG = {"а": "ā", "и": "ӣ", "у": "ӯ"}
SHORT = {"ā": "а", "ō": "а", "ӣ": "и", "ӯ": "у"}


def units(word):
    """Разбить слово на (буква, набор знаков)."""
    out = []
    for ch in word:
        if "ً" <= ch <= "ٟ" or ch == DAGGER or "ۖ" <= ch <= "ۭ" or "࣓" <= ch <= "ࣿ":
            if out:
                out[-1][1].add(ch)
        else:
            out.append([ch, set()])
    # Знаки на татвиле (ـٰ) относятся к предыдущей букве
    merged = []
    for b, m in out:
        if b == TATWEEL and HAMZA_ABOVE in m:
            merged.append(["ء", m - {HAMZA_ABOVE}])
        elif b == TATWEEL and merged:
            merged[-1][1] |= m
        elif b != TATWEEL:
            merged.append([b, m])
    return merged


def vowel_of(m):
    if FATHA in m: return "а"
    if KASRA in m: return "и"
    if DAMMA in m: return "у"
    if FATHATAN in m: return "ан"
    if KASRATAN in m: return "ин"
    if DAMMATAN in m: return "ун"
    return ""


def is_allah(word):
    base = "".join(c for c in word if c not in MARK_SET and c != TATWEEL)
    return base.startswith("ٱلله") or base == "ٱللهم"


def word_to_cyr(word, utter_start, stop):
    u = units(word)
    res = []
    i = 0
    n = len(u)
    while i < n:
        b, m = u[i]
        nxt = u[i + 1] if i + 1 < n else None
        if m & SILENT:
            i += 1
            continue
        # Алиф-васла: в начале речи — гласная, внутри — не читается
        if b == WASLA:
            is_article = nxt is not None and nxt[0] == "ل" and i + 2 < n
            if is_article and SHADDA in nxt[1]:
                # ٱلَّذِينَ → ал-лаз́ӣна
                start = "а" if (utter_start and not res) else ""
                res.append(start + "л-" if start else "-л-")
                nxt[1].discard(SHADDA)
                i += 1
                continue
            if is_article:
                lam_m = nxt[1]
                after = u[i + 2]
                sun = SHADDA in after[1] and SUKUN not in lam_m and after[0] in SUN
                start = "а" if (utter_start and not res) else ""
                if sun:
                    c = CONS.get(after[0], after[0])
                    res.append(start + c + "-" if start else "-" + c + "-")
                    after[1].discard(SHADDA)  # согласный уже показан
                else:
                    res.append(start + "л-" if start else "-л-")
                i += 2
                continue
            if utter_start and not res:
                res.append("и")
            elif not res:
                res.append("-")  # не читается — слово сливается с предыдущим
            i += 1
            continue
        if b == "ا":
            # Простой алиф: удлинение уже учтено, иначе не читается
            if MADDAH in m and not res:
                res.append("ā")
            i += 1
            continue
        if b == "آ":
            res.append(("" if (utter_start and not res) or not res else "ъ") + "ā")
            i += 1
            continue
        if b == "ة":
            if stop and i == n - 1:
                res.append("ҳ")
            else:
                res.append("т" + vowel_of(m))
            i += 1
            continue

        # Согласный
        if b in HAMZAS or HAMZA_ABOVE in m:
            joined = "".join(res)
            has_vowel = bool(m & {FATHA, KASRA, DAMMA, FATHATAN, KASRATAN, DAMMATAN})
            c = "" if (not res or joined.endswith("-") or (joined in PREFIXES and has_vowel)) else "ъ"
        elif b in CONS:
            c = CONS[b]
        else:
            i += 1
            continue
        if "\u06E2" in m and b == "ن":
            c = "м"
        if SHADDA in m and c and i > 0:
            c = c + c if len(c) == 1 else c + c  # дж → джидж не нужно; простое удвоение
            if CONS.get(b) == "дж":
                c = "ддж"

        v = vowel_of(m)
        # Буква без знаков перед буквой с шаддой (идгам: مِن رَّبِّ) — не читается
        if not v and SUKUN not in m and SHADDA not in m and b in ("ن", "م") and nxt is None:
            pass
        # Долгие гласные
        long_v = False
        if DAGGER in m:
            v, long_v = "ā", True
        elif v == "а" and nxt is not None and nxt[0] in ("ا", "ى") and not (nxt[1] - {MADDAH, DAGGER}):
            v, long_v = "ā", True
            i += 1
        elif v == "а" and nxt is not None and nxt[0] == "ى" and DAGGER in nxt[1]:
            v, long_v = "ā", True
            i += 1
        elif v == "и" and nxt is not None and nxt[0] in ("ي", "ى") and not (nxt[1] - {MADDAH}):
            v, long_v = "ӣ", True
            i += 1
        elif v == "у" and nxt is not None and nxt[0] == "و" and not (nxt[1] - {MADDAH}):
            v, long_v = "ӯ", True
            i += 1
        elif v == "ан" and nxt is not None and nxt[0] in ("ا", "ى"):
            i += 1
        if stop and v in ("а", "и", "у", "ан", "ин", "ун"):
            rest = u[i + 1:]
            if all((r[0] in ("ا", "ى") and not (r[1] - {MADDAH})) or (r[1] & SILENT) for r in rest):
                v = "ā" if (v == "ан" and b != "ة") else ""
        if SMALL_WAW in m:
            v = "ӯ"
        if SMALL_YA in m:
            v = "ӣ"
        if v == "ā" and b in EMPHATIC:
            v = "ō"
        res.append(c + v)
        i += 1

    out = "".join(res)
    base = "".join(c for c in word if c not in MARK_SET and c != TATWEEL)
    if "لله" in base:
        out = out.replace("-л-лаҳ", "ллаҳ").replace("ллаҳ", "ллāҳ")
    return out


LETTER_NAMES = {"ا": "алиф", "ل": "лāм", "م": "мӣм", "ص": "с̇ōд", "ر": "рō", "ك": "кāф", "ه": "ҳā",
                "ي": "йā", "ع": "ъайн", "ط": "т̣ō", "س": "сӣн", "ح": "ҳā", "ق": "қōф", "ن": "нӯн"}
MUQATTAAT = {"الم", "المص", "الر", "المر", "كهيعص", "طه", "طسم", "طس", "يس", "ص", "حم", "عسق", "ق", "ن"}


def muqattaat(word):
    if any(c in word for c in (FATHA, KASRA, DAMMA, FATHATAN, KASRATAN, DAMMATAN, SHADDA)):
        return None
    base = "".join(c for c in word if c not in MARK_SET and c != TATWEEL)
    if base not in MUQATTAAT:
        return None
    return " ".join(LETTER_NAMES[c] for c in base)


def ayah_to_cyr(text):
    words = [w for w in text.split() if any(c in CONS or c in HAMZAS or c in "اٱآة" for c in w)]
    out = ""
    for idx, w in enumerate(words):
        stop = idx == len(words) - 1
        first = idx == 0
        mq = muqattaat(w) if idx < 2 else None
        if mq:
            out += mq + " "
            continue
        if is_allah(w):
            body = word_to_cyr(w, False, stop)  # «-л-лаҳ…» → склеиваем как «…ллāҳ…»
            body = body.lstrip("-").replace("л-л", "лл", 1)
            if first:
                piece = "А" + body
            else:
                out = out.rstrip()
                if out and out[-1] in SHORT:
                    out = out[:-1] + SHORT[out[-1]]
                piece = body
            out += piece + " "
            continue
        cyr = word_to_cyr(w, first, stop)
        if cyr.startswith("-") and out:
            out = out.rstrip()
            if out[-1] in SHORT:
                out = out[:-1] + SHORT[out[-1]]  # долгая гласная перед васлой сокращается
            out += cyr + " "
        else:
            out += cyr + " "
    out = " ".join(out.split())
    # йа → я, йу → ю (кроме удвоенного й)
    import re
    out = re.sub(r"(?:(?<=^)|(?<=[\s\-аиуāӣӯōъ]))йа", "я", out)
    out = re.sub(r"(?:(?<=^)|(?<=[\s\-аиуāӣӯōъ]))йу", "ю", out)
    out = re.sub(r"(?:(?<=^)|(?<=\s))аллāҳ", "Аллāҳ", out)
    return out[:1].upper() + out[1:]


if __name__ == "__main__":
    import sys
    path = sys.argv[1]
    for line in open(path, encoding="utf-8"):
        if line.startswith("#") or not line.strip():
            continue
        s, a, t = line.rstrip("\n").split("|", 2)
        if s in sys.argv[2:]:
            print(s, a, ayah_to_cyr(t))


def to_simple_russian(t):
    """Упрощённая русская транслитерация без особых знаков: Бисмилляхи-р-рахмани-р-рахим."""
    import re
    import unicodedata
    import re as _re
    s = _re.sub(r"(^|\s)Аллāҳ", r"\1Аллах", t)
    s = s.replace("ллāҳ", "ллях").replace("ллаҳ", "ллях")
    for a, b in (("ā", "а"), ("Ā", "А"), ("ō", "а"), ("Ō", "А"), ("ӣ", "и"), ("Ӣ", "И"), ("ӯ", "у"), ("Ӯ", "У"),
                 ("ҳ", "х"), ("Ҳ", "Х"), ("қ", "к"), ("Қ", "К"), ("ъ", "‘")):
        s = s.replace(a, b)
    # убираем надстрочные/подстрочные знаки (с̇ → с, з̆ → з, т̣ → т …)
    s = "".join(c for c in unicodedata.normalize("NFD", s) if not unicodedata.combining(c) or c in "̆̈")
    s = unicodedata.normalize("NFC", s).replace("й", "й")
    s = s.replace("з̆", "д").replace("З̆", "Д")  # ض в русской традиции — «д»: ад-даллин
    # артикль «аль»: Ал-хамду → Аль-хамду, рабби-л-‘алямин → рабби-ль-‘алямин
    s = re.sub(r"(^|\s)Ал-", r"\1Аль-", s)
    s = re.sub(r"(^|\s)ал-", r"\1аль-", s)
    s = re.sub(r"-л-", "-ль-", s)
    s = re.sub(r"(^|[\s\-аиуэоя])йу", r"\1ю", s).replace("Йу", "Ю")
    s = re.sub(r"(?<=[а-я])йу", "ю", s)
    s = re.sub(r"(^|\s)([Кк])ул(?=\s|$)", r"\1\2уль", s)
    return s
