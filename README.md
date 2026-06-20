<div align="center">

# 💪 TRAINEX

### Entrena inteligente, Compite con el mundo

Aplicación móvil nativa Android para la gestión integral del entrenamiento físico y la nutrición, potenciada con Inteligencia Artificial Generativa.

![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue)
![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)
![License](https://img.shields.io/badge/Status-Proyecto%20Académico-lightgrey)

</div>

---

## 📱 ¿Qué es Trainex?

**Trainex** es una aplicación "all-in-one" para Android que centraliza tres pilares de la salud física: **entrenamiento**, **nutrición** y **seguimiento de progreso**, eliminando la necesidad de combinar varias apps distintas.

Nace para resolver tres problemas reales del mundo del fitness:

- 🧭 **Desorientación en el gimnasio** — gracias a un entrenador virtual con IA.
- 🍎 **Falta de seguimiento nutricional** — con un contador de calorías y macros conectado a una base de datos global de alimentos.
- 📉 **Abandono deportivo por falta de motivación** — mediante estadísticas visuales y un componente social (seguir usuarios, comparar progreso).

---

## ✨ Funcionalidades principales

### 🏋️ Entrenamiento
- Generación de rutinas personalizadas mediante **IA (DeepSeek)** según objetivo, edad y experiencia.
- Modo *Live Workout*: cronómetro, registro de series/repeticiones/peso en tiempo real y sobrecarga progresiva (comparación con la sesión anterior).
- Biblioteca de ejercicios con grupo muscular, dificultad y vídeo demostrativo.

### 🥗 Nutrición
- Dashboard de calorías y macronutrientes (proteínas, carbohidratos, grasas).
- Diario de comidas (desayuno, comida, cena, snack).
- Base de datos de alimentos colaborativa, alimentada por **Open Food Facts**, con traducción automática al español.

### 📊 Perfil y progreso
- Gráficos de evolución de fuerza, peso e IMC.
- Galería de fotos de progreso semanal con zoom (pinch-to-zoom).
- Buscador de perfiles, sistema de seguidores y comparación de estadísticas entre usuarios.

### ⚙️ Personalización
- Modo claro / oscuro.
- Unidades métricas o imperiales (kg/lbs, km/millas).
- Gestión completa de cuenta (email, contraseña, eliminación de cuenta).

---

## 🛠️ Stack tecnológico

| Categoría | Tecnología |
|---|---|
| **Lenguaje** | Kotlin (JVM 11) |
| **Arquitectura** | MVVM (Model-View-ViewModel) |
| **IDE / Build** | Android Studio, Gradle (Kotlin DSL) |
| **Backend** | Firebase (Authentication, Firestore, Analytics, Cloud Messaging) |
| **Persistencia local** | Room (caché de ejercicios y progreso diario) |
| **IA Generativa** | DeepSeek API (generación de rutinas) |
| **Datos nutricionales** | Open Food Facts API |
| **Traducción** | Google ML Kit (Translate & Language ID) |
| **Red** | Retrofit 2 + OkHttp + Gson |
| **UI / Multimedia** | Glide, PhotoView, ExoPlayer, MPAndroidChart |
| **Monetización** | Google Mobile Ads |
| **Tareas en background** | WorkManager |

> 💡 **¿Por qué Kotlin nativo?** Permite un acceso más directo al hardware del dispositivo frente a soluciones híbridas (Flutter/React Native), con mejor rendimiento, *null safety* y corrutinas para operaciones asíncronas fluidas.

---

## 🏗️ Arquitectura

El proyecto sigue el patrón **MVVM** recomendado por Google, con una estrategia de persistencia híbrida (*Single Source of Truth*):

```
View (Activities/Fragments/XML)
        ↓ observa
ViewModel (StateFlow / uiState)
        ↓ solicita datos
Repository
   ↙          ↘
Room (local)   Firebase (nube)
```

- La **Vista** solo representa datos y captura interacción.
- El **ViewModel** transforma los datos del modelo en estado consumible por la UI y sobrevive a cambios de configuración (ej. rotación de pantalla).
- El **Repositorio** decide si los datos provienen de caché local (Room) o de la nube (Firestore), priorizando inmediatez y actualizando ambas capas cuando es necesario.

### Modelo de datos (Firestore)

Colecciones principales: `usuarios` (con subcolecciones `historial_fotos`, `historial_medidas`, `historial_pesos`), `seguimientos`, `alimentos_globales`, `rutinas`, `sesiones_completadas`, `historial_ejercicios`.

---

## 🎨 Diseño

- **Estilo visual:** Neumorfismo + minimalismo, buscando una sensación de calma y orden.
- **Tipografía:** Inter.
- **Sistema de espaciado:** múltiplos de 8px, márgenes de 16px, áreas táctiles mínimas de 44px.
- **Paleta semántica:** verde (éxito/progreso), rojo (alertas/acciones destructivas), azul (navegación), naranja (errores/estancamiento).
- Soporte completo de modo claro/oscuro.

---

## 🧩 Decisiones técnicas destacadas

Durante el desarrollo surgieron varios retos de ingeniería que obligaron a pivotar respecto al diseño inicial:

- **Alimentos vía API en vez de JSON local:** se descartó un dataset local de alimentos por motivos de rendimiento y peso de la app, optando por integrar Open Food Facts.
- **Barrera del idioma:** al ser Open Food Facts una base internacional, se integró Google ML Kit para traducir automáticamente los nombres de alimentos al español.
- **Almacenamiento de imágenes sin coste:** para evitar el plan de pago de Firebase Storage, las imágenes se codifican en Base64 y se almacenan como campos de texto en Firestore.
- **Notificaciones modernas:** se combinó Firebase Cloud Messaging, `NotificationCompat` y WorkManager para garantizar entregas tanto push como locales.
- **Refactor a MVVM:** migración desde una estructura por paquetes hacia MVVM para aislar errores y mejorar la mantenibilidad.

---

## 🚀 Instalación y ejecución

**Requisitos:**
- Dispositivo físico o emulador con Android 8.0 (Oreo) o superior.

**Pasos:**
1. Descarga el archivo `.apk` desde este repositorio.
2. Transfiérelo al dispositivo Android.
3. Instálalo manualmente (no requiere configuración adicional; la conexión con Firebase y las APIs ya está integrada en el código).

> Para compilar desde el código fuente, clona el repositorio y ábrelo en Android Studio. El proyecto usa Gradle (Kotlin DSL) y resolverá las dependencias automáticamente.

```bash
git clone https://github.com/raulito136/Trainex.git
```

---

## 🔮 Roadmap / Mejoras futuras

- [ ] Generación de **dietas con IA** personalizadas.
- [ ] Análisis de técnica de ejercicio mediante vídeo e IA (corrección postural).
- [ ] Comparativa de estadísticas entre amigos de forma más visual.

---

## 👥 Equipo

Proyecto desarrollado por **Gymbros** como Proyecto Intermodular de 2º DAM.

| Integrante | Rol principal |
|---|---|
| **Raúl López Palomo** | Configuración y despliegue de backend (Firebase), lógica e interfaces |
| **Carlos García Sánchez** | Lógica, interfaces y documentación |

Ambos miembros participaron en todas las áreas del proyecto (frontend, backend y QA) bajo un modelo *fullstack* compartido.

---

## 📄 Licencia

Proyecto académico desarrollado en el marco del módulo *Proyecto Intermodular* (2º DAM). Uso educativo.
