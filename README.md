# JAVIS Launcher OS V06

> **J**ust **A** **V**ery **I**ntelligent **S**ystem — AI-first Android launcher

[![Build APK](https://github.com/manl244345-rgb/JAVIS-Launcher-OS/actions/workflows/build-apk.yml/badge.svg)](https://github.com/manl244345-rgb/JAVIS-Launcher-OS/actions/workflows/build-apk.yml)

## What is JAVIS?

JAVIS Launcher OS replaces your Android home screen with a full AI companion. It listens, thinks, plans, and executes — all from your home screen.

## Features

| Layer | Features |
|---|---|
| 🏠 Launcher | Home, All Apps, AI Chat, Memory, Mission Control, Settings, Command Log |
| 🤖 AI | Multi-provider (OpenRouter, Groq, DeepSeek, Together, Fireworks), offline fallback |
| 🎤 Voice | Android STT (no popup), Android TTS, voice profiles, wake word |
| 🧠 Memory | Short-term, long-term, routine learning, semantic search |
| 📋 Tasks | Intent analyzer, task planner, step-by-step execution, verification |
| 📱 Android | Accessibility service, notification listener, foreground service, alarm system |
| 🔒 Security | Encrypted SharedPreferences, local-first storage, no telemetry |

## Architecture

```
JAVIS Core Engine
├── Intent Analyzer (NLU)
├── Task Planner
├── AI Provider Manager (5 online + offline fallover)
├── Voice Manager (STT + TTS)
├── Memory Manager (Room DB)
└── Android Services (Accessibility, Notification, Alarms)
```

## Swipe Gestures

| Gesture | Action |
|---|---|
| ← Swipe Left | AI Chat |
| → Swipe Right | Memory |
| ↑ Swipe Up | All Apps |
| ↓ Swipe Down | Mission Control |
| Tap Orb | Voice Input |
| Long-press Orb | AI Chat |

## Setup

1. Install APK (from [Releases](../../releases))
2. Set as **Default Launcher** when prompted
3. Grant **Accessibility** permission (Settings → Accessibility → JAVIS)
4. Grant **Notification Listener** permission
5. Open **Settings** tab → enter your API key (OpenRouter/Groq/etc.)

## Target Device

Optimized for **Redmi A1** (Android 12, low RAM). Offline-first architecture ensures functionality without internet.

## Build

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```

## CI/CD

Every push to `main`:
1. 🔍 Lint & Code Review
2. 🧪 Unit Tests  
3. 🔨 Debug APK Build
4. 📦 Release APK Build
5. 🩺 APK Diagnosis
6. 🚀 GitHub Release (with APK attached)

## License

MIT — Build something amazing.
