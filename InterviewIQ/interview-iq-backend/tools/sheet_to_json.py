#!/usr/bin/env python3
"""
Google Sheet / Excel (CSV export) -> problems.json  (coding-service ke bulk import ke liye)

Sheet format: har TEST CASE ki ek row. Same slug ki rows ek problem ban jati hain.
Problem ki details (title, topic, ...) us slug ki PEHLI row se li jati hain.

Columns (header row zaroori):
  slug, title, topic, difficulty, description, time_limit_seconds, published, input, expected_output, sample
  - difficulty: EASY / MEDIUM / HARD
  - sample: yes/no   (yes = student ko dikhega aur "Run" mein use hoga; hidden = no)
  - time_limit_seconds, published: optional (khali chhodo toh default 2 sec / published)

Use:   python tools/sheet_to_json.py problems.csv problems.json
Sheet mein se:  File -> Download -> Comma Separated Values (.csv)
"""
import csv
import json
import sys
from collections import OrderedDict

REQUIRED = ["slug", "title", "topic", "difficulty", "description", "input", "expected_output", "sample"]


def truthy(value):
    return str(value).strip().lower() in ("yes", "y", "true", "1")


def with_newline(text):
    text = text.replace("\r\n", "\n").replace("\r", "\n")
    return text if text.endswith("\n") else text + "\n"


def main():
    if len(sys.argv) != 3:
        print(__doc__)
        sys.exit(1)

    src, dst = sys.argv[1], sys.argv[2]
    problems = OrderedDict()

    with open(src, newline="", encoding="utf-8-sig") as f:
        reader = csv.DictReader(f)
        headers = [h.strip() for h in (reader.fieldnames or [])]
        missing = [c for c in REQUIRED if c not in headers]
        if missing:
            sys.exit("Missing columns in sheet: " + ", ".join(missing))

        for line_no, raw in enumerate(reader, start=2):
            row = {k.strip(): (v or "") for k, v in raw.items() if k}
            slug = row["slug"].strip()
            if not slug:
                continue                      # khali row

            if slug not in problems:
                for col in ("title", "topic", "difficulty", "description"):
                    if not row[col].strip():
                        sys.exit(f"Row {line_no}: '{col}' is empty for the first row of '{slug}'")
                tl = row.get("time_limit_seconds", "").strip()
                pub = row.get("published", "").strip()
                problems[slug] = {
                    "slug": slug,
                    "title": row["title"].strip(),
                    "description": row["description"].replace("\r\n", "\n").strip(),
                    "difficulty": row["difficulty"].strip().upper(),
                    "topic": row["topic"].strip(),
                    "timeLimitSeconds": float(tl) if tl else None,
                    "memoryLimitKb": None,
                    "published": truthy(pub) if pub else True,
                    "testCases": [],
                }

            if row["input"] == "" and row["expected_output"] == "":
                sys.exit(f"Row {line_no}: both input and expected_output are empty for '{slug}'")

            problems[slug]["testCases"].append({
                "input": with_newline(row["input"]),
                "expectedOutput": with_newline(row["expected_output"]),
                "sample": truthy(row["sample"]),
            })

    # checks jo backend bhi karega, yahin pehle pakad lo
    for slug, p in problems.items():
        if p["difficulty"] not in ("EASY", "MEDIUM", "HARD"):
            sys.exit(f"'{slug}': difficulty must be EASY/MEDIUM/HARD, got '{p['difficulty']}'")
        if not any(tc["sample"] for tc in p["testCases"]):
            sys.exit(f"'{slug}': at least one test case must have sample = yes")
        if len(p["testCases"]) > 20:
            sys.exit(f"'{slug}': at most 20 test cases allowed")
        # null fields hata do (backend defaults use karega)
        for k in ("timeLimitSeconds", "memoryLimitKb"):
            if p[k] is None:
                del p[k]

    with open(dst, "w", encoding="utf-8") as f:
        json.dump({"problems": list(problems.values())}, f, indent=2, ensure_ascii=False)

    total_cases = sum(len(p["testCases"]) for p in problems.values())
    print(f"OK: {len(problems)} problems, {total_cases} test cases -> {dst}")


if __name__ == "__main__":
    main()
