<div align="center">

# 💪 TRAINEX

### Train smart, compete with the world

A native Android mobile app for comprehensive fitness training and nutrition management, powered by Generative AI.

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue)
![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)
![License](https://img.shields.io/badge/Status-Academic%20Project-lightgrey)

</div>

---

## 📱 What is Trainex?

**Trainex** is an "all-in-one" Android app that centralizes three pillars of physical health: **training**, **nutrition**, and **progress tracking**, removing the need to combine several separate apps.

It was built to solve three real problems in the fitness world:

- 🧭 **Gym disorientation** — solved with an AI-powered virtual trainer.
- 🍎 **Lack of nutritional tracking** — solved with a calorie/macro counter connected to a global food database.
- 📉 **Drop-off due to lack of motivation** — solved with visual statistics and a social component (follow users, compare progress).

---

## ✨ Key Features

### 🏋️ Training
- Personalized workout routine generation powered by **AI (DeepSeek)**, based on the user's goal, age, and experience.
- *Live Workout* mode: built-in timer, real-time set/rep/weight logging, and progressive overload (comparison against the previous session).
- Exercise library including muscle group, difficulty level, and demo video.

### 🥗 Nutrition
- Calorie and macronutrient dashboard (protein, carbs, fat).
- Meal diary (breakfast, lunch, dinner, snack).
- Collaborative food database powered by **Open Food Facts**, with automatic translation into Spanish.

### 📊 Profile & Progress
- Charts tracking strength, body weight, and BMI evolution over time.
- Weekly progress photo gallery with pinch-to-zoom.
- Profile search, follower system, and stats comparison between users.

### ⚙️ Customization
- Light / dark mode.
- Metric or imperial units (kg/lbs, km/miles).
- Full account management (email, password, account deletion).

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| **Language** | Kotlin (JVM 11) |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **IDE / Build** | Android Studio, Gradle (Kotlin DSL) |
| **Backend** | Firebase (Authentication, Firestore, Analytics, Cloud Messaging) |
| **Local persistence** | Room (exercise cache and daily diet progress) |
| **Generative AI** | DeepSeek API (workout routine generation) |
| **Nutrition data** | Open Food Facts API |
| **Translation** | Google ML Kit (Translate & Language ID) |
| **Networking** | Retrofit 2 + OkHttp + Gson |
| **UI / Media** | Glide, PhotoView, ExoPlayer, MPAndroidChart |
| **Monetization** | Google Mobile Ads |
| **Background tasks** | WorkManager |

> 💡 **Why native Kotlin?** It allows more direct access to device hardware compared to hybrid solutions (Flutter/React Native), with better performance, null safety, and coroutines for smooth asynchronous operations.

---

## 🏗️ Architecture

The project follows the **MVVM** pattern recommended by Google, with a hybrid persistence strategy (*Single Source of Truth*):

```
View (Activities/Fragments/XML)
        ↓ observes
ViewModel (StateFlow / uiState)
        ↓ requests data
Repository
   ↙          ↘
Room (local)   Firebase (cloud)
```

- The **View** only renders data and captures user interaction.
- The **ViewModel** transforms model data into a state the UI can consume, and survives configuration changes (e.g. screen rotation).
- The **Repository** decides whether data comes from local cache (Room) or the cloud (Firestore), prioritizing immediacy and updating both layers when needed.

### Data Model (Firestore)

Main collections: `usuarios` (with subcollections `historial_fotos`, `historial_medidas`, `historial_pesos`), `seguimientos`, `alimentos_globales`, `rutinas`, `sesiones_completadas`, `historial_ejercicios`.

---

## 🎨 Design

- **Visual style:** Neumorphism + minimalism, aiming for a sense of calm and order.
- **Typography:** Inter.
- **Spacing system:** multiples of 8px, 16px side margins, minimum 44px tap targets.
- **Semantic color palette:** green (success/progress), red (alerts/destructive actions), blue (navigation), orange (errors/stagnation).
- Full light/dark mode support.

---

## 🧩 Notable Technical Decisions

Several engineering challenges came up during development, leading to changes from the original design:

- **Food data via API instead of local JSON:** a local food dataset was discarded for performance and app-size reasons, in favor of integrating Open Food Facts.
- **Language barrier:** since Open Food Facts is an international database, Google ML Kit was integrated to automatically translate food names into Spanish.
- **Cost-free image storage:** to avoid Firebase Storage's paid plan, images are Base64-encoded and stored as text fields directly in Firestore.
- **Modern notifications:** Firebase Cloud Messaging, `NotificationCompat`, and WorkManager were combined to ensure reliable delivery for both push and local notifications.
- **Refactor to MVVM:** the codebase was migrated from a package-based structure to MVVM to isolate bugs and improve maintainability.

---

## 🚀 Installation & Setup

**Requirements:**
- A physical device or emulator running Android 8.0 (Oreo) or higher.

**Steps:**
1. Download the `.apk` file from this repository.
2. Transfer it to your Android device.
3. Install it manually (no additional setup required — the Firebase connection and API integrations are already configured in the code).

> To build from source, clone the repository and open it in Android Studio. The project uses Gradle (Kotlin DSL) and will resolve dependencies automatically.

```bash
git clone https://github.com/raulito136/Trainex.git
```

---

## 🔮 Roadmap / Future Improvements

- [ ] **AI-generated diets** tailored to each user.
- [ ] AI-based exercise form analysis via video (injury prevention).
- [ ] A more visual way for friends to compare stats with each other.

---

## 👥 Team

Project developed by **Gymbros** as the Intermodular Project for the 2nd year of DAM (Multiplatform App Development).

| Member | Main Role |
|---|---|
| **Raúl López Palomo** | Backend setup and deployment (Firebase), logic and UI |
| **Carlos García Sánchez** | Logic, UI, and documentation |

Both members contributed to every area of the project (frontend, backend, and QA) under a shared full-stack approach.

---

## 📄 License

Academic project developed as part of the *Intermodular Project* module (2nd year DAM). For educational use.

