# KAI AI

An early Android voice-assistant starter project.

## Current features
- Dark KAI home screen
- Tap-to-talk speech recognition (uses the Android speech service)
- Spoken replies using Android Text-to-Speech
- Basic sample replies for greetings, date, and time
- GitHub Actions workflow to build a debug APK

## Important
This first build does **not** include an always-listening “Hey Kai” wake word, screen-off listening, cloud AI conversations, or full app automation yet. Those need additional implementation and Android permissions. Voice recognition may require an internet connection and a speech service on the phone.

## Build
Push this project to the `main` branch. In GitHub, open **Actions** and run **Build KAI AI APK** (or let it run after a push). Download the `kai-ai-debug-apk` artifact from the completed workflow.
