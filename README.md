# 🤖 JAVIS Launcher OS V06 Ultimate Edition

> **Just A Very Intelligent System** — The most advanced Android AI Launcher

[![Build APK](https://github.com/manl244345-rgb/JAVIS-Launcher-OS/actions/workflows/build-apk.yml/badge.svg)](https://github.com/manl244345-rgb/JAVIS-Launcher-OS/actions/workflows/build-apk.yml)

---

## What is JAVIS?

JAVIS transforms your Android phone into a Jarvis-like AI companion. It replaces your home screen with:

- 🧠 **AI Assistant** — Multi-provider (OpenRouter, Groq, DeepSeek, Together, Fireworks)
- 🎙️ **Voice Control** — No popups, direct SpeechRecognizer API
- 💀 **Animated Skull Orb** — 7 states: Idle, Listening, Thinking, Executing, Speaking, Completed, Error
- 🧠 **Memory System** — Learns your name, habits, preferences, goals
- ⏰ **Alarm System** — Create, manage, wake-up briefings
- 📱 **Contact Assistant** — Voice-call contacts by name
- 🔔 **Notification Reader** — Summarize and read notifications
- 🖼️ **Image Studio** — Edit, filter, save images
- 🎬 **Video Studio** — Play, trim, caption videos
- 📋 **Mission Control** — System status dashboard
- 📝 **Command Log** — Full history of all commands

---

## Navigation

| Gesture | Action |
|---------|--------|
| Swipe Up | All Apps |
| Swipe Left | AI Chat |
| Swipe Right | Memory Bank |
| Swipe Down | Mission Control |
| Tap Orb | Voice Input |

---

## Setup

1. Install APK → Set JAVIS as default launcher
2. Settings → Enter API key (get free key at openrouter.ai)
3. Grant permissions: Microphone, Contacts, Notifications, Accessibility
4. Say your name → JAVIS will remember it

---

## AI Providers

| Provider | Free Tier | Model |
|----------|-----------|-------|
| **OpenRouter** (default) | ✅ Yes | qwen/qwen3-mini:free |
| Groq | ✅ Yes | llama3-8b-8192 |
| DeepSeek | ✅ Limited | deepseek-chat |
| Together AI | ✅ Credits | llama3-8b |
| Fireworks | ✅ Credits | llama3-8b |

---

## Target Device

**Redmi A1** (Android 12 Go) — optimized for low RAM & battery

---

## Build

```bash
./gradlew assembleDebug
```

APKs in: `app/build/outputs/apk/debug/`
