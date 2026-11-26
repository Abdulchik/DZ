#!/usr/bin/env python3
"""
Собирает данные Корана для приложения в app/src/main/assets/quran/.

Источники (скачать заранее и передать путями):
  1) Tanzil Quran Text (Uthmani, v1.1), формат "сура|аят|текст" — quran-uthmani.txt
     https://tanzil.net/download/  (CC BY 3.0, текст не изменяется)
  2) Перевод Э. Кулиева (Tanzil ru.kuliev) в JSON fawazahmed0/quran-api: rus-elmirkuliev.json
  3) Транслитерация Tanzil (en.transliteration) в JSON fawazahmed0/quran-api: ara-quran-la.json
  4) info.json из fawazahmed0/quran-api — арабские названия сур и место ниспослания

Запуск:
  python3 tools/build_quran_data.py quran-uthmani.txt rus-elmirkuliev.json ara-quran-la.json info.json \
      tgk-wwwislamhouseco.json eng-ummmuhammad.json

  5) Таджикский перевод (islamhouse.com / quranenc.com, tajik_arifi) — tgk-wwwislamhouseco.json
  6) Английский перевод Saheeh International (Tanzil en.sahih) — eng-ummmuhammad.json
Транслитерация кириллицей строится из арабского текста скриптом translit_cyr.py.
"""
import json
import re
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from translit_cyr import ayah_to_cyr  # noqa: E402

ROOT = Path(__file__).resolve().parent.parent
OUT = ROOT / "app" / "src" / "main" / "assets" / "quran"

# Русские названия сур (транслитерация, смысл) — как у Э. Кулиева
SURAH_RU = [
    ("Аль-Фатиха", "Открывающая Коран"), ("Аль-Бакара", "Корова"), ("Аль Имран", "Семейство Имрана"),
    ("Ан-Ниса", "Женщины"), ("Аль-Маида", "Трапеза"), ("Аль-Анам", "Скот"), ("Аль-Араф", "Ограды"),
    ("Аль-Анфаль", "Трофеи"), ("Ат-Тауба", "Покаяние"), ("Юнус", "Иона"), ("Худ", "Худ"),
    ("Юсуф", "Иосиф"), ("Ар-Раад", "Гром"), ("Ибрахим", "Авраам"), ("Аль-Хиджр", "Аль-Хиджр"),
    ("Ан-Нахль", "Пчёлы"), ("Аль-Исра", "Ночной перенос"), ("Аль-Кахф", "Пещера"), ("Марьям", "Мария"),
    ("Та Ха", "Та Ха"), ("Аль-Анбия", "Пророки"), ("Аль-Хадж", "Паломничество"),
    ("Аль-Муминун", "Верующие"), ("Ан-Нур", "Свет"), ("Аль-Фуркан", "Различение"),
    ("Аш-Шуара", "Поэты"), ("Ан-Намль", "Муравьи"), ("Аль-Касас", "Рассказ"), ("Аль-Анкабут", "Паук"),
    ("Ар-Рум", "Римляне"), ("Лукман", "Лукман"), ("Ас-Сажда", "Земной поклон"),
    ("Аль-Ахзаб", "Союзники"), ("Саба", "Саба"), ("Фатыр", "Творец"), ("Йа Син", "Йа Син"),
    ("Ас-Саффат", "Выстроившиеся в ряды"), ("Сад", "Сад"), ("Аз-Зумар", "Толпы"),
    ("Гафир", "Прощающий"), ("Фуссылят", "Разъяснены"), ("Аш-Шура", "Совет"),
    ("Аз-Зухруф", "Украшения"), ("Ад-Духан", "Дым"), ("Аль-Джасия", "Коленопреклонённые"),
    ("Аль-Ахкаф", "Барханы"), ("Мухаммад", "Мухаммад"), ("Аль-Фатх", "Победа"),
    ("Аль-Худжурат", "Комнаты"), ("Каф", "Каф"), ("Аз-Зарият", "Рассеивающие прах"),
    ("Ат-Тур", "Гора"), ("Ан-Наджм", "Звезда"), ("Аль-Камар", "Месяц"), ("Ар-Рахман", "Милостивый"),
    ("Аль-Вакиа", "Событие"), ("Аль-Хадид", "Железо"), ("Аль-Муджадила", "Препирающаяся"),
    ("Аль-Хашр", "Сбор"), ("Аль-Мумтахана", "Испытуемая"), ("Ас-Сафф", "Ряд"),
    ("Аль-Джумуа", "Собрание"), ("Аль-Мунафикун", "Лицемеры"), ("Ат-Тагабун", "Взаимное обделение"),
    ("Ат-Талак", "Развод"), ("Ат-Тахрим", "Запрещение"), ("Аль-Мульк", "Власть"),
    ("Аль-Калям", "Письменная трость"), ("Аль-Хакка", "Неизбежное"), ("Аль-Мааридж", "Ступени"),
    ("Нух", "Ной"), ("Аль-Джинн", "Джинны"), ("Аль-Муззаммиль", "Закутавшийся"),
    ("Аль-Муддассир", "Завернувшийся"), ("Аль-Кияма", "Воскресение"), ("Аль-Инсан", "Человек"),
    ("Аль-Мурсалят", "Посылаемые"), ("Ан-Наба", "Весть"), ("Ан-Назиат", "Вырывающие"),
    ("Абаса", "Нахмурился"), ("Ат-Таквир", "Скручивание"), ("Аль-Инфитар", "Раскалывание"),
    ("Аль-Мутаффифин", "Обвешивающие"), ("Аль-Иншикак", "Разверзнется"), ("Аль-Бурудж", "Созвездия"),
    ("Ат-Тарик", "Ночной путник"), ("Аль-Аля", "Всевышний"), ("Аль-Гашия", "Покрывающее"),
    ("Аль-Фаджр", "Заря"), ("Аль-Балад", "Город"), ("Аш-Шамс", "Солнце"), ("Аль-Лейль", "Ночь"),
    ("Ад-Духа", "Утро"), ("Аш-Шарх", "Раскрытие"), ("Ат-Тин", "Смоковница"), ("Аль-Алак", "Сгусток"),
    ("Аль-Кадр", "Предопределение"), ("Аль-Баййина", "Ясное знамение"),
    ("Аз-Зальзаля", "Землетрясение"), ("Аль-Адият", "Скачущие"), ("Аль-Кариа", "Великое бедствие"),
    ("Ат-Такасур", "Страсть к приумножению"), ("Аль-Аср", "Предвечернее время"),
    ("Аль-Хумаза", "Хулитель"), ("Аль-Филь", "Слон"), ("Курайш", "Курайшиты"),
    ("Аль-Маун", "Мелочь"), ("Аль-Каусар", "Изобилие"), ("Аль-Кафирун", "Неверующие"),
    ("Ан-Наср", "Помощь"), ("Аль-Масад", "Пальмовые волокна"), ("Аль-Ихлас", "Очищение веры"),
    ("Аль-Фаляк", "Рассвет"), ("Ан-Нас", "Люди"),
]
assert len(SURAH_RU) == 114, len(SURAH_RU)

# ---------- Латинская транслитерация Tanzil -> упрощённая кириллица ----------
DIGRAPHS = [
    ("AA", "‘"), ("sh", "ш"), ("kh", "х"), ("gh", "г"), ("th", "з"), ("dh", "з"),
    ("ee", "и"), ("oo", "у"), ("aa", "а"),
]
SINGLE = {
    "a": "а", "b": "б", "t": "т", "j": "дж", "h": "х", "d": "д", "r": "р", "z": "з", "s": "с",
    "f": "ф", "q": "к", "k": "к", "l": "л", "m": "м", "n": "н", "w": "в", "y": "й", "g": "г",
    "i": "и", "u": "у", "e": "е", "o": "у", "c": "к", "p": "п", "v": "в", "x": "кс",
}
VOWELS = set("аиуеоя ю")


def to_cyrillic_word(word: str) -> str:
    cap = word[:1].isupper()
    w = word
    # Солнечные буквы: alrrahmani -> ar-rahmani
    w = re.sub(r"^([Aa])l(t|d|r|z|s|n|l)(?=\2)", r"\1\2-", w)
    w = re.sub(r"^([Aa])l(sh|th|dh)(?=\2)", r"\1\2-", w)
    tokens = []
    i = 0
    while i < len(w):
        for lat, cyr in DIGRAPHS:
            if w.startswith(lat, i):
                tokens.append(cyr)
                i += len(lat)
                break
        else:
            ch = w[i]
            low = ch.lower()
            tokens.append(SINGLE.get(low, ch))
            i += 1
    out = []
    for k, tok in enumerate(tokens):
        nxt = tokens[k + 1] if k + 1 < len(tokens) else ""
        prev = out[-1] if out else ""
        if tok == "й" and nxt in ("а", "у"):  # йа -> я, йу -> ю
            out.append("я" if nxt == "а" else "ю")
            tokens[k + 1] = ""
            continue
        if tok == "в" and prev and prev[-1] in "аиу" and (not nxt or nxt not in "аиуе"):
            out.append("у")  # yawm -> яум
            continue
        if tok == "л" and nxt not in ("", "-") and nxt[0] not in "аиуеяюл‘" :
            out.append("ль")  # alhamdu -> альхамду
            continue
        if tok == "л" and nxt == "" and k > 0:
            out.append("ль")  # qul -> куль
            continue
        out.append(tok)
    res = "".join(out)
    if cap and res:
        res = res[0].upper() + res[1:]
    return res


def to_cyrillic(text: str) -> str:
    return " ".join(to_cyrillic_word(w) for w in text.split())


def without_bismillah(s, a, text):
    """Басмала в начале 1-го аята (кроме сур 1 и 9) — для транслитерации убираем, как в приложении."""
    if a != 1 or s in (1, 9):
        return text
    w = text.split(" ")
    if " ".join(w[:4]).replace("\u0651", "") == "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ".replace("\u0651", ""):
        return " ".join(w[4:])
    return text


def main():
    uthmani_path, kuliev_path, translit_path, info_path, tg_path, en_path = map(Path, sys.argv[1:7])

    lines = uthmani_path.read_text(encoding="utf-8").splitlines()
    ayahs = []
    license_block = []
    for line in lines:
        if line.startswith("#"):
            license_block.append(line)
        elif line.strip():
            s, a, text = line.split("|", 2)
            ayahs.append((int(s), int(a), text))
    assert len(ayahs) == 6236, len(ayahs)

    kuliev = {(v["chapter"], v["verse"]): v["text"] for v in json.loads(kuliev_path.read_text("utf-8"))["quran"]}
    translit = {(v["chapter"], v["verse"]): v["text"] for v in json.loads(translit_path.read_text("utf-8"))["quran"]}
    info = json.loads(info_path.read_text("utf-8"))["chapters"]

    OUT.mkdir(parents=True, exist_ok=True)
    clean = lambda t: " ".join(t.replace("\t", " ").split())

    with (OUT / "quran.tsv").open("w", encoding="utf-8") as f:
        for l in license_block:
            f.write(l + "\n")
        f.write("# Колонки: сура<TAB>аят<TAB>арабский текст (Tanzil Uthmani, без изменений)"
                "<TAB>транслитерация (упрощённая, по Tanzil)<TAB>перевод Э. Кулиева\n")
        for s, a, text in ayahs:
            f.write(f"{s}\t{a}\t{text}\t{ayah_to_cyr(without_bismillah(s, a, text))}\t{clean(kuliev[(s, a)])}\n")

    for lang, path, title in (
        ("tg", tg_path, "Таджикский перевод: islamhouse.com (quranenc.com, tajik_arifi)"),
        ("en", en_path, "English: Saheeh International (Tanzil, en.sahih)"),
    ):
        data = {(v["chapter"], v["verse"]): v["text"] for v in json.loads(path.read_text("utf-8"))["quran"]}
        with (OUT / f"tr_{lang}.tsv").open("w", encoding="utf-8") as f:
            f.write(f"# {title}\n# Колонки: сура<TAB>аят<TAB>перевод\n")
            for s, a, _ in ayahs:
                f.write(f"{s}\t{a}\t{clean(data[(s, a)])}\n")

    counts = {}
    for s, a, _ in ayahs:
        counts[s] = max(counts.get(s, 0), a)
    with (OUT / "surahs.tsv").open("w", encoding="utf-8") as f:
        f.write("# номер\tарабское название\tрусское название\tзначение\tаятов\tместо (m=Мекка, d=Медина)\n")
        for ch in info:
            n = ch["chapter"]
            ar = ch["arabicname"].replace("سُوْرَةُ", "").strip()
            ru, meaning = SURAH_RU[n - 1]
            place = "m" if ch["revelation"].lower().startswith("mec") else "d"
            f.write(f"{n}\t{ar}\t{ru}\t{meaning}\t{counts[n]}\t{place}\n")

    (OUT / "TANZIL_LICENSE.txt").write_text("\n".join(license_block) + "\n", encoding="utf-8")
    print("ok", len(ayahs))


if __name__ == "__main__":
    main()
