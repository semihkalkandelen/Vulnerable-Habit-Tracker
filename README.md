# Vulnerable Habit Tracker
 
A deliberately vulnerable Android habit tracker application created as a term project to demonstrate common mobile security vulnerabilities through reverse engineering.
 
> This app was intentionally built with vulnerabilities. Do not use it.
 
---

![Screenshot](/screenshots/ss3.png)

---
 
## About
 
The app simulates a habit tracking tool with a streak counter. It was designed to contain the following OWASP Mobile Top 10 (2024) vulnerabilities:
 
- **M6 – Inadequate Privacy Controls:** PII stored and transmitted without proper safeguards
- **M9 – Insecure Data Storage:** Sensitive user data written to accessible local files
- **M10 – Insufficient Cryptography:** Weak AES/ECB encryption with a hardcoded key
 
---
 
## Required Tools 
 
- [Android Studio](https://developer.android.com/studio) — Android emulator
- [APKTool](https://apktool.org/) — APK decompilation

## Hidden Flag Challenge
 
While doing this Project I also added a CTF-style flag in following format:

flag{}

Feel free to try.