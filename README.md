# 🌟 NutriCoach - Memoria Técnica y Reporte del Proyecto (Android Client)

Este documento expone en profundidad el estado actual de la aplicación Android **NutriCoach**, proporcionando una memoria detallada y completa de su arquitectura de software, decisiones de diseño, módulos clave, flujos de trabajo e implementaciones tecnológicas de nivel premium.

---

## 🔑 Credenciales de Prueba

Para propósitos de prueba y evaluación en el entorno local o de producción:
*   **Email:** `ana@email.com`
*   **Contraseña:** `1234`

---

## 🏗️ 1. Arquitectura de Software y Patrones de Diseño

NutriCoach se ha construido siguiendo principios modernos de la ingeniería de software para Android, garantizando que el sistema sea modular, testable y escalable a largo plazo.

```
       ┌──────────────────────────────────────────────────────────┐
       │                   VISTA (UI - Jetpack Compose)           │
       └───────────────────────────┬──────────────────────────────┘
                                   │ (Suscribe a UI States)
                                   ▼
       ┌──────────────────────────────────────────────────────────┐
       │                VIEWMODEL (State & Logic)                 │
       └───────────────────────────┬──────────────────────────────┘
                                   │ (Llamadas asíncronas / Flows)
                                   ▼
       ┌──────────────────────────────────────────────────────────┐
       │                    REPOSITORY LAYER                      │
       └───────────────┬───────────────────────────┬──────────────┘
                       │                           │
                       ▼                           ▼
       ┌────────────────────────┐      ┌────────────────────────┐
       │  LOCAL DATA SOURCE     │      │  REMOTE DATA SOURCE    │
       │ (Room, DataStore, SEC) │      │ (Retrofit API, Cloud)  │
       └────────────────────────┘      └────────────────────────┘
```

### A. Model-View-ViewModel (MVVM)
El núcleo de la aplicación implementa de manera rigurosa el patrón **MVVM**, recomendado por el equipo oficial de Android de Google:
*   **Model (Capa de Datos):** Mapea directamente la estructura de la base de datos remota (BSON de MongoDB) y las entidades locales.
*   **View (Capa de UI - Jetpack Compose):** 100% declarativa. Las pantallas (`Views` / `Composables`) se comportan como funciones puras que reaccionan a los estados expuestos por el ViewModel, asegurando un desacoplamiento total de la lógica de negocio.
*   **ViewModel (Capa de Negocio):** Actúa como el centro de comandos de cada vista. Captura los eventos del usuario, interactúa con los repositorios en hilos secundarios mediante Corrutinas (`viewModelScope`) y expone los resultados a través de flujos reactivos de datos (`StateFlow` y `mutableStateOf`).

### B. Inyección de Dependencias (DI) con Hilt
Para evitar el acoplamiento rígido, la creación manual de dependencias y simplificar las pruebas unitarias, se ha implementado **Dagger Hilt** como motor de inyección de dependencias a nivel de aplicación:
*   `DatabaseModule.kt`: Provee de forma centralizada la base de datos Room (`AppDatabase`) y los DAOs necesarios para el bloc de notas y el historial de progreso.
*   `NetworkModule.kt`: Configura de manera centralizada la instancia de `Retrofit` con un cliente `OkHttpClient` común, serializadores de JSON nativos (`kotlinx.serialization`) y maneja la inyección de interceptores de cabeceras de autorización.

### C. Navegación Segura (Type-Safe Navigation)
Se utiliza el moderno sistema de navegación declarativo **Compose Navigation 3 (Type-Safe)**. En lugar de navegar utilizando strings planos propensos a errores tipográficos (`"detalle_rutina/{id}"`), la app utiliza objetos y clases serializables (`PantallaDetalleRutina(val rutinaId: String)`). Esto garantiza validación en tiempo de compilación y elimina los riesgos de caídas de la aplicación (crashes) en tiempo de ejecución.

---

## 🎨 2. Diseño Visual e Identidad de Marca (Aesthetics)

NutriCoach cuenta con un diseño de interfaz de usuario de calidad premium que impresiona a primera vista. Utiliza una estética oscura y futurista enriquecida con acentos neón y efectos dinámicos interactivos.

### A. Paleta de Colores Curada
El esquema de color define una identidad visual coherente y muy deportiva:
*   **Fondo Principal (`DarkBackground` = `#060709`):** Un negro profundo y elegante que reduce la fatiga visual y realza los componentes.
*   **Superficies (`DarkSurface` = `#0A0B0D`):** Gris oscuro para tarjetas, campos de texto y barras de navegación, generando una clara jerarquía visual y contraste tridimensional.
*   **Verde Neón (`PrimaryNeon` = `#00E676`):** El color primario de la marca. Transmite energía, salud y dinamismo.
*   **Turquesa (`SecondaryTeal` = `#1DE9B6`):** Utilizado para estados secundarios y transiciones.
*   **Amarillo Lima (`AccentYellowGreen` = `#D1FF1A`):** Un toque de color complementario brillante para llamadas a la acción, títulos importantes y elementos destacados.

### B. Gradientes y Efecto "Glow" Dinámico
*   **Gradients Premium:** Se han configurado gradientes lineales centralizados (`AppBrushes.MainGradient` y `AppBrushes.AccentGradient`) aplicados a textos importantes, botones principales y tarjetas.
*   **NutriGridBackground:** Diseñado para pintar un sutil resplandor verde neón radial en el centro de las pantallas, simulando un efecto de luz ambiental difusa (Ambient Glow) que añade sofisticación y profundidad tridimensional al diseño.
*   **Micro-animaciones e Indicadores:** Botones con elevaciones interactivas, indicadores de carga fluidos y un Scaffold que oculta y muestra la barra de navegación de forma animada mediante `AnimatedVisibility` (con deslizamientos verticales suaves) según el contexto de la pantalla activa.

---

## 🔒 3. Seguridad de Datos y Gestión de Sesión

La app protege celosamente la sesión del usuario mediante un sistema de persistencia híbrido:
1.  **Cifrado Criptográfico de Credenciales:** El token de autenticación (`auth_token`) se guarda en **EncryptedSharedPreferences** mediante algoritmos avanzados de cifrado de nivel militar (AES-256 GCM/SIV), gestionados por la biblioteca `androidx.security`. Esto impide que atacantes o root-kits extraigan el token de acceso del almacenamiento local.
2.  **Persistencia Reactiva Ligera:** Los metadatos de usuario no sensibles (email, ID de cliente, rol) se almacenan en **Jetpack DataStore Preferences**, un reemplazo asíncrono y robusto de SharedPreferences construido sobre Kotlin Coroutines y Kotlin Flows.
3.  **Interceptor Global de Autenticación:** Se ha implementado un `AuthInterceptor` en OkHttp que extrae la sesión y añade automáticamente la cabecera `Authorization: Bearer <token>` en todas las peticiones salientes. Esto elimina la gestión manual de credenciales en las capas de UI/ViewModel y asegura la red por diseño.
4.  **Ocultación de Entornos (BuildConfig):** La URL del servidor de producción ya no reside como texto estático en el código base. Se almacena localmente en `local.properties` (excluido de git) y se inyecta en tiempo de compilación con `BuildConfig.BASE_URL`, blindando el repositorio de filtraciones de red.
5.  **Desacoplamiento de Criptografía (SRP):** La decodificación en Base64 de las tramas de los Tokens JWT ha sido totalmente desacoplada de la interfaz gráfica y enviada a un componente utilitario nativo (`JwtUtils`), facilitando pruebas unitarias.

---

## 📊 4. Módulos y Funcionalidades del Sistema

### 🍏 Módulo de Nutrición y Dietas
*   **Visualización de Menús:** Permite visualizar los platos, ingredientes y cantidades necesarias para el día.
*   **DietTracker (Seguimiento Nutricional):** A través del `DietTrackerManager` local, los usuarios pueden marcar sus comidas completadas en tiempo real mediante casillas de verificación interactivas. Esta información se lee de forma reactiva y persiste localmente sin tiempos de espera.
*   **Detalles del Alimento:** Información de calorías, macronutrientes y notas sobre la preparación culinaria.

### 🏋️‍♂️ Módulo de Rutinas y Gimnasio
*   **Creador de Planes de Entrenamiento:** El usuario puede estructurar rutinas compuestas por múltiples días (ej. Tirón, Empuje, Pierna).
*   **Editor de Ejercicios:** Permite añadir ejercicios dinámicamente indicando número de series, repeticiones programadas, RIR (Reps in Reserve) y tiempos de descanso deseados.

### 📈 Módulo de Seguimiento, Historial y Gráficos (Premium)
Este es uno de los apartados más pulidos visualmente y potentes de la aplicación:
1.  **Gráfico de Evolución Personalizado (Canvas Engine):** La sección "Seguimiento" cuenta con `EvolutionChart`, un gráfico interactivo dibujado programáticamente píxel a píxel sobre un `Canvas` de Compose. Muestra la progresión del peso máximo levantado a lo largo del tiempo para cada ejercicio individual. Cuenta con un buscador en tiempo real para filtrar entre decenas de ejercicios interactivos de forma fluida.
2.  **Calendario de Entrenamientos Mensual Interactivo:** Un calendario personalizado que resalta con el gradiente neón (`AppBrushes.MainGradient`) los días exactos en los que el usuario ha completado entrenamientos. Al pulsar sobre cualquier día destacado, el sistema inyecta y visualiza de forma instantánea el listado detallado de series, repeticiones y kilajes levantados ese día específico.

---

### ⚡ 5. Modo de Entrenamiento Activo (Foreground Service)

El entrenamiento activo de NutriCoach ofrece una experiencia inmersiva e ininterrumpida utilizando servicios avanzados del sistema Android:

```
┌─────────────────────────────────────────────────────────────────┐
│                    EntrenamientoDiaView (UI)                     │
│  - Captura kilajes levantados en tiempo real                    │
│  - Muestra cuenta atrás interactiva del descanso                │
└────────────────────────────────┬────────────────────────────────┘
                                 │ Comunica estados bidireccionales
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                    WorkoutManager (Singleton)                   │
│  - Controla el temporizador activo                              │
│  - Rastrea serie actual, ejercicio actual e historial de pesos  │
└────────────────────────────────┬────────────────────────────────┘
                                 │ Inicia / Actualiza estado
                                 ▼
┌─────────────────────────────────────────────────────────────────┐
│                 WorkoutService (Foreground Service)             │
│  - Garantiza supervivencia del temporizador en segundo plano    │
│  - Notificación activa permanente con botones de acción         │
│  - Botones interactivos directos en la barra de notificaciones  │
└─────────────────────────────────────────────────────────────────┘
```

*   **Android Foreground Service (`WorkoutService`):** Cuando el usuario pulsa "Comenzar Entrenamiento", la app inicia un servicio en primer plano. Esto garantiza que el sistema operativo Android no mate el entrenamiento ni el cronómetro, incluso si el usuario apaga la pantalla o abre otra aplicación como Spotify.
*   **Notificación Interactiva Inteligente:** Se muestra una notificación persistente que se actualiza segundo a segundo mostrando:
    *   El ejercicio actual, la serie actual y el total de series.
    *   Una barra de progreso visual interactiva del ejercicio.
    *   **Botones de Acción Rápidos (PendingIntents):** Sin necesidad de desbloquear el teléfono ni abrir la app, el usuario puede marcar la serie actual como terminada ("Terminar Serie"), saltar el tiempo de descanso activo ("Saltar Descanso") o abortar la sesión ("Detener").
*   **Temporizador de Descanso Reactivo:** Al pulsar "Terminar Serie", el sistema inicia automáticamente una cuenta atrás visualizada con un gran cronómetro en la aplicación y reflejada en la barra de notificaciones del móvil.
*   **Sugerencia de Peso Inteligente (Room DB Sync):** Al iniciar una serie, el campo de peso del ejercicio se pre-rellena automáticamente leyendo el último kilaje registrado en la base de datos Room, motivando una sobrecarga progresiva eficiente y libre de fricciones.
*   **Sincronización en la Nube:** Al finalizar el entrenamiento, el ViewModel procesa el registro y lo envía inmediatamente a la API del servidor remoto para sincronizar el historial, manteniendo un respaldo seguro en todo momento.

---

### 👥 6. Comunidad y Red Social Integrada

NutriCoach va más allá de una agenda deportiva y ofrece una vibrante red social integrada dentro de la pestaña `FeedView`:

*   **Publicaciones Dinámicas:** Los usuarios pueden compartir reflexiones, fotografías y sus propios logros en un feed comunitario interactivo.
*   **Optimización de Imágenes e Integración con Cloudinary:** 
    *   El motor `CloudinaryUploader` optimiza las imágenes capturadas con la cámara o galería del teléfono antes de subirlas al servidor de Cloudinary.
    *   Para evitar el error crítico clásico de falta de memoria (`OutOfMemoryError`) en dispositivos móviles, se utiliza carga inteligente en modo `inJustDecodeBounds` para pre-calcular las dimensiones de la imagen.
    *   Comprime las fotografías a una resolución máxima idónea de 1280px con un 80% de calidad en formato JPEG.
    *   **Corrección de Rotación EXIF:** El uploader lee los metadatos EXIF del sensor físico de la cámara del móvil y corrige cualquier rotación incorrecta para que las fotos nunca se suban giradas o de lado.
*   **Clonación de Planes Interactiva (Social Sharing) y Modo Lectura:** Los usuarios pueden adjuntar una de sus **Dietas o Rutinas** directamente a sus posts. Debido al diseño de borrado lógico en el backend, **incluso si un usuario elimina el plan de su sección personal, este sigue estando disponible en el feed para ser visualizado en modo de solo lectura y replicado por cualquier otro miembro de la comunidad**, garantizando que el conocimiento compartido nunca se pierda. Al abrir el adjunto desde cualquier post en la comunidad, la app aplica una protección de **solo lectura**, impidiendo la edición, borrado, cambios de visibilidad o la alteración accidental del plan original. Desde allí, el usuario puede revisar el plan y **clonar / replicar** esa rutina o dieta completa directamente en su perfil personal como una copia propia e independiente.
*   **Pestaña de Publicaciones Propias ("Tus Posts"):** Integrado en el panel de control **Personal -> Tus Posts**, permite a cada usuario visualizar sus publicaciones en un solo lugar y eliminarlas de forma permanente del feed de la comunidad mediante llamadas asíncronas y seguras con diálogos de confirmación interactivos.
*   **Paginación Infinita Fluida (Infinite Scroll):** La pestaña de publicaciones comunitarias recupera los posts en lotes eficientes de 10 elementos mediante una paginación basada en cursores (`fecha_creacion`). Utiliza Compose `derivedStateOf` y `LazyListState` para detectar cuándo el usuario está a 2 ítems del final y precargar la siguiente página de forma fluida. Se incluye un spinner de carga no intrusivo en el pie y soporte completo para refrescos completos (Pull-to-Refresh).
*   **Actualizaciones de Estado Unitarias (Single-Item Refresh):** Para lograr la fluidez de interacción de redes como X e Instagram, las acciones de dar/quitar like o agregar comentarios obtienen únicamente el post modificado del servidor (`GET /posts/{id}`) y actualizan su estado en memoria local de forma atómica. Esto previene el flickering de recarga completa, ahorra ancho de banda y preserva la posición exacta de scroll del usuario de forma ininterrumpida.
*   **Seguimiento de Usuarios:** Pestaña interactiva de "Usuarios" con un potente motor de búsqueda por texto. Permite seguir y dejar de seguir a otros entusiastas del fitness en tiempo real.

---

### 🔍 7. Scanner y Generador QR (Google ML Kit + CameraX)
*   **Escaner QR Moderno (`QrScannerView`):** NutriCoach integra la biblioteca de cámara reactiva **Jetpack CameraX** combinada con el motor inteligente **Google ML Kit Barcode Scanning**. 
*   **Clonación Instantánea:** Un usuario puede abrir su código QR generado (`nutricoach://rutina/{id}` o `nutricoach://dieta/{id}`) en la pantalla de su teléfono y otro usuario puede apuntar su cámara para escanearlo. Al leerlo, el escáner identifica el plan al instante y realiza una clonación en la nube para importar el plan nutricional o de entrenamiento de inmediato.

---

### 📝 8. Bloc de Notas Local Persistente (Room)
*   **Notas del Atleta:** Un diario de notas personales integrado a nivel local.
*   **Persistencia Segura con Room:** Utiliza una base de datos SQLite relacional persistente administrada mediante Room.
*   **Evolución Estructural Controlada (Migrations):** El esquema de la base de datos local cuenta con control de versiones y migraciones automatizadas:
    *   *Migración 1 a 2:* Añade la columna `clienteId` para segmentar notas locales de forma segura por usuario.
    *   *Migración 2 a 3:* Añade la tabla `peso_historial` que permite guardar el registro cronológico del rendimiento de los ejercicios.

---

## 📂 5. Organización Esquemática del Código (`com.spc.nutricoach`)

El código fuente sigue las directrices del diseño por capas (Clean Architecture):

```
com.spc.nutricoach/
│
├── MainActivity.kt               # Actividad principal y punto de entrada al Scaffold de la App
├── NutriCoachApp.kt               # Clase Application encargada de la inicialización de Hilt
│
├── data/                          # CAPA DE DATOS (REPOSITORIOS Y ORÍGENES DE DATOS)
│   ├── api/                       # Interfaces de Retrofit para peticiones HTTP
│   │   ├── AuthApiService.kt
│   │   ├── ClienteApiService.kt
│   │   ├── ComunidadApiService.kt
│   │   ├── DietaApiService.kt
│   │   └── RutinaApiService.kt
│   ├── local/                     # Persistencia SQLite local mediante Room
│   │   ├── AppDatabase.kt         # Definición de la DB y sus migraciones (v1, v2, v3)
│   │   ├── dao/                   # DAOs (NotasDao, PesoHistorialDao)
│   │   └── entity/                # Entidades persistentes (NotasEntity, PesoHistorialEntity)
│   ├── repository/                # Implementación de repositorios de sincronización
│   │   ├── AuthRepository.kt
│   │   ├── ComunidadRepository.kt
│   │   ├── DietaRepository.kt
│   │   ├── NotasRepository.kt
│   │   └── RutinaRepository.kt
│   ├── ApiResponse.kt             # Modelado de respuestas estándar del servidor
│   ├── SessionManager.kt          # Gestión cifrada de tokens y almacenamiento reactivo
│   ├── DietTrackerManager.kt      # Almacenamiento local del estado diario de comidas
│   └── RoutineTrackerManager.kt   # Gestor local de pesos del historial de Room
│
├── model/                         # MODELO DE DOMINIO (ENTIDADES Y SERIALIZADORES JSON)
│   ├── Cliente.kt
│   ├── Dieta.kt
│   ├── Rutina.kt
│   ├── Post.kt
│   └── LoginApiResponse.kt
│
├── ui/                            # CAPA DE VISTA (JETPACK COMPOSE)
│   ├── components/                # Pantallas y componentes estéticos de la UI
│   │   ├── AppNavigation.kt       # Grafo centralizado de navegación segura (Compose Route)
│   │   ├── FeedView.kt            # Pantalla del feed social e interactivo de la comunidad
│   │   ├── PersonalView.kt        # Panel personal (Dietas, Rutinas, Gráfico e Historial)
│   │   ├── EntrenamientoDiaView.kt # Pantalla interactiva del entrenamiento activo
│   │   ├── DetalleRutinaView.kt   # Detalle de rutina, toggle público/privado y QR
│   │   ├── QrScannerView.kt       # Escáner inteligente con CameraX y Google ML Kit
│   │   └── ...
│   ├── theme/                     # Identidad corporativa de la marca
│   │   ├── Color.kt               # Paleta neón, lima y carbon background
│   │   ├── Theme.kt               # Configuración del MaterialTheme oscuro/claro
│   │   ├── ThemeBrushes.kt        # Gradientes corporativos de la marca
│   │   └── Type.kt                # Tipografías y estilos de fuente
│   └── viewmodel/                 # CAPA DE VIEWMODELS (LÓGICA Y ESTADOS DE PANTALLA)
│       ├── DietaViewModel.kt
│       ├── RutinaViewModel.kt
│       ├── EntrenamientoViewModel.kt
│       ├── NotasViewModel.kt
│       ├── FeedViewModel.kt
│       └── ...
│
├── util/                          # COMPONENTES AUXILIARES DE ALTO RENDIMIENTO
│   ├── CloudinaryUploader.kt      # Optimizador, rotador de fotos y uploader de imágenes
│   └── QrUtils.kt                 # Generador de códigos QR personalizados para clones
│
└── workout/                       # INFRAESTRUCTURA DEL SERVICIO DE ENTRENAMIENTO
    ├── WorkoutManager.kt          # Estado y flujo lógico de la sesión de entreno activa
    └── WorkoutService.kt          # Android Foreground Service (Notificaciones e hilos)
```

---

## 🔄 6. Resumen de Flujos de Trabajo Críticos

### Flujo de Entrenamiento Activo
1. El usuario accede a una rutina en `DetalleRutinaView` y pulsa "Comenzar Entrenamiento" sobre un día.
2. `EntrenamientoDiaView` solicita permisos para notificaciones e invoca a `WorkoutManager.iniciarOReanudar()`.
3. Se arranca el servicio **`WorkoutService`** en modo *Foreground Service*, anclando una notificación persistente con la información y las acciones de control.
4. El ViewModel recupera del `pesoHistorialDao` el último peso levantado en este ejercicio para este cliente y lo sugiere en pantalla.
5. El usuario ejecuta la serie, corrige el peso levantado o reps en los inputs interactivos de la UI, y pulsa "Serie Terminada".
6. El `WorkoutManager` detiene la UI de ejecución, inicia la cuenta de descanso reactiva de forma simultánea en la notificación y la pantalla.
7. Al finalizar todos los ejercicios, se muestra la pantalla de éxito, se guarda el historial local de progresos en Room y se envía a la nube a través de la API remota. El servicio se detiene limpiamente.

---

### 🖼️ 7. Gestión, Optimización y Persistencia de Fotos de Perfil

NutriCoach cuenta con soporte completo para que los usuarios puedan personalizar su perfil mediante fotos seleccionadas desde la Galería o capturadas directamente con la Cámara del teléfono.

```
  ┌────────────────────────┐      ┌──────────────────────────┐      ┌─────────────────────────┐
  │ PerfilUsuarioView (UI) ├─────►│ PerfilUsuarioViewModel   ├─────►│  CloudinaryUploader     │
  │ - Picker / Cámara      │      │ - subirYActualizarFoto() │      │  - Optimiza EXIF y res  │
  └───────────▲────────────┘      └────────────┬─────────────┘      └────────────┬────────────┘
              │                                │                                 │
              │ Sincroniza caché               ▼ Actualiza Flow                  ▼ Retorna URL
  ┌───────────┴────────────┐      ┌────────────┴─────────────┐                   │
  │   TopAppBar Avatares   │◄─────┤   SessionManager         │◄──────────────────┘
  │   (Feed, Home, Dieta)  │      │   - userFotoPerfilFlow   │
  └────────────────────────┘      └──────────────────────────┘
```

#### A. Carga, Optimización y Subida en Hilo Secundario
*   **Captura Flexible:** Utiliza launchers de actividad de Jetpack Compose (`rememberLauncherForActivityResult`) para interactuar de forma nativa con los proveedores de Galería (`ActivityResultContracts.GetContent()`) y Cámara (`TakePicture()`).
*   **Procesamiento Inteligente:** Reutiliza el motor `CloudinaryUploader` para proteger la memoria del dispositivo móvil y el ancho de banda del usuario:
    *   Pre-calcula dimensiones óptimas con `inJustDecodeBounds` para evitar desbordes de memoria (`OutOfMemoryError`).
    *   Comprime la imagen a formato JPEG de alta calidad con dimensiones máximas de 1280px.
    *   **Corrección EXIF:** Corrige automáticamente la rotación según la orientación nativa del sensor físico de la cámara.
*   **Paralelismo y Resiliencia:** La subida se procesa en el hilo de fondo de Kotlin Coroutines (`Dispatchers.IO`) con un sistema de reintentos automatizado (3 intentos en caso de fallos de conexión).

#### B. Cacheo Local Reactivo (DataStore)
Para evitar llamadas repetitivas e innecesarias al servidor remoto al navegar entre pantallas, la foto de perfil se gestiona a través de una caché reactiva:
*   `SessionManager` expone un flujo reactivo asíncrono `userFotoPerfilFlow` respaldado por **Jetpack DataStore Preferences**.
*   Al iniciar sesión o cargar la pantalla de perfil (`cargarPerfil()`), la app sincroniza esta caché de forma automática.
*   Cuando el usuario cambia su foto y guarda los cambios, el flujo se actualiza y notifica instantáneamente a toda la interfaz gráfica.

#### C. Renderizado Visual Consistente
*   Todos los avatares interactivos y cabeceras de la aplicación —incluyendo **FeedView**, **PanelPrincipal**, **PersonalView**, **PublicarView**, y **PostCard**— observan este flujo y renderizan dinámicamente la imagen real del usuario mediante Coil `AsyncImage`.
*   Siguiendo las pautas de diseño visual neón de la marca, los avatares se recortan y enmarcan con elegancia utilizando **`CircleShape`** de forma consistente.
*   En modo edición, se superpone un badge interactivo con icono de cámara en el avatar principal para proporcionar una experiencia de usuario premium e intuitiva.
