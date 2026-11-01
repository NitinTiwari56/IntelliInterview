# Coding problems kahan se aayenge (aur kaise add honge)

Problems AI se nahi aati. Ye **backend ke database (coding_db)** mein rehti hain. Frontend mein kabhi nahi,
kyunki hidden test cases (answers) frontend tak gaye toh student DevTools mein dekh legi.

## 3 tareeke
1. **Seed (ready):** pehli baar service chalane pe 7 sample problems apne aap aa jati hain. Demo ke liye kaafi.
2. **Sheet / JSON se bulk import (recommended):** neeche ka workflow.
3. **Admin API se ek-ek:** `POST /coding/admin/problems` (Bruno se).

## Sheet workflow
1. Google Sheet / Excel banao. Format `sample-sheet.csv` jaisa (har TEST CASE ki ek row):

| slug | title | topic | difficulty | description | time_limit_seconds | published | input | expected_output | sample |
|---|---|---|---|---|---|---|---|---|---|
| count-vowels | Count Vowels | Strings | EASY | (statement) | | | hello world | 3 | yes |
| count-vowels | | | | | | | aeiou | 5 | no |

- Problem ki details (title, topic, difficulty, description) sirf us slug ki **pehli row** mein.
- `sample = yes` wale cases student ko dikhte hain aur "Run" mein chalte hain. `no` = hidden (sirf Submit mein).
- Har problem mein kam se kam 1 sample, aur zyada se zyada 20 test cases.
- `difficulty`: EASY / MEDIUM / HARD. `slug`: chhote letters, numbers, hyphen (jaise `two-sum`).
- `description` markdown ho sakta hai (Input/Output format, constraints, example).
2. Sheet ko CSV mein download karo: File -> Download -> Comma Separated Values.
3. Convert:
   ```
   python tools/sheet_to_json.py problems.csv problems.json
   ```
   Galti hogi (difficulty galat, sample nahi, etc.) toh script bata degi.
4. **ADMIN token** se import karo (Bruno): `POST http://localhost:8080/coding/admin/problems/import`
   body = `problems.json` ka content. Same slug dobara bhejoge toh wo problem update ho jayegi.
   ```sql
   UPDATE auth_db.users SET role = 'ADMIN' WHERE email = 'your@email.com';
   ```
   Isko chalane ke baad **dobara login** karo (role token ke andar hota hai).

## Expected output kaise nikalein (sabse zaroori)
Galat expected output = sahi solution ko bhi "Wrong Answer" milega. Isliye expected output kabhi haath se mat likho:
1. Problem ka ek **reference solution** (Python mein) likho.
2. Test inputs likho (chhote, edge cases, aur kuch bade).
3. Reference solution ko har input pe chalao, output ko `expected_output` mein daalo.

## Content ke rules
- Problem statements **apni language mein likho**. TakeUForward / LeetCode / GFG ka text copy mat karo (copyright).
  Classic problems (Two Sum, Palindrome...) ka idea free hai, statement apna hona chahiye.
- Style: **stdin/stdout** (input padho, answer print karo). LeetCode jaisa "function likho" style nahi, kyunki
  wo har language ke liye wrapper maangta hai.
- Test cases mein edge cases rakho: minimum size, negative numbers, duplicates, bade numbers (Java `int` overflow pakadne ke liye).

## Kitni problems chahiye?
College demo ke liye 20-30 problems kaafi hain (Arrays, Strings, Math, Stack, DP, Sorting/Searching, Linked-list-type
ko stdin ke liye array jaisa convert karke). Ye Claude se bhi banwa sakti ho: topic aur count batao, wo problems + reference solution
chalake verified test cases ke saath CSV/JSON bana dega.
