# Documentación Técnica: NutriCoach (App Android)

Este documento expone en profundidad las decisiones técnicas, estructurales y arquitectónicas tomadas para construir el lado del cliente (Android) del ecosistema integral NutriCoach. 

---

## 🏗 Arquitectura del Proyecto

NutriCoach sigue estrictamente **Model-View-ViewModel (MVVM)**, la arquitectura moderna recomendada por el equipo de Android de Google para una mayor mantenibilidad temporal, con total independencia entre la interfaz del usuario y los orígenes de los datos.

1.  **Model (Data Layer):** 
    Comprende los objetos estructurados de datos, fuentes tanto locales (`Room` y `DataStore`) como remotas (`Retrofit`), además de sus serializadores. Su responsabilidad es ir a la base de datos o API y entregar la información al siguiente eslabón.

2.  **View (UI Layer - Jetpack Compose):** 
    Toda la capa de visualización ha sido construida 100% de manera programática en **Jetpack Compose** (El nuevo framework declarativo moderno). Las vistas, o `Composables`, reaccionan a información estricta sin mutar ninguna lógica.

3.  **ViewModel (Domain Layer):** 
    Existen ViewModels para cada contexto visual. Se encargan de suscribirse a los `Models`, mapear y encapsular los modelos puramente de red hacia "estado de la UI" listo para la interfaz, para así informar a la `View` de lo que ocurre automáticamente mediante objetos mutables o flujos (`StateFlow`). 

---

## 🛠 Tecnologías Core e Implementación

Para asegurar robustez y asincronía ágil sin sobrecarga de hilos, el ecosistema utiliza las siguientes bibliotecas nucleares:

- **Interfaz Declarativa:** Jetpack Compose (Material Design 3).
- **Navegación Intento-Segura:** Type-Safe Navigation Compose (`navigation3`). Para eliminar errores típicos de Strings no encontrados al navegar (p. ej, pasando objetos o dataclases como `PantallaDetalleDieta(val id)` previendo un sistema hermético anti-crasheo).
- **Asincronía Intensa:** Kotlin Coroutines (`viewModelScope`) combinadas con `Dispatchers.IO` para la absorción total sin trabar el hilo principal de la interfaz al interactuar con internet o disco.
- **Red/API:**
  - `Retrofit2` para instanciar el mapeador rest de interfaz.
  - `OkHttp3` manejador secundario debajo de Retrofit.
  - `Kotlinx.serialization` (JSON nativo de kotlin) usado a favor de dependencias antiguas como GSON, para ofrecer mayor fiabilidad entre los mapeos JSON complejos traídos desde colecciones de MongoDB.
- **Persistencia Local:**
  - `DataStore Preferences`: Administrador moderno para almacenar sesión e identificador de usuario, así como para rastrear de manera local y en tiempo real las comidas completadas de las dietas (`diet_tracker`) y los pesos levantados por ejercicio de las rutinas (`routine_tracker`) individualmente de cada plan nutricional con asincronía y latencia cero.
  - `Room Database`: Base de Datos SQLite relacional configurada para el caché y consultas persistentes sin internet de la sección del bloc de Notas.

---

## 📂 Organización Esquemática del Código (`com.spc.nutricoach`)

El encapsulamiento se subdivide en áreas vitales correspondientes a las dependencias:

*   `.model` 📦: Datos espejo representativos de BSON y DB del servidor. Por ejemplo, `Cliente`, `Dieta`, `Rutina`, `Nota`. Se usan fuertemente las anotaciones de Serialización.
*   `.data` 📡: Interactores natales de bases de datos. Aquí yace `NutriCoachApi.kt` definiendo las peticiones `@GET`, `@POST`, `@PUT` junto al motor principal `Retrofit.Builder()`, y el `SessionManager` o base de datos local `NotaDao`.
*   `.ui.components` 🎨: Estructura del framework Jetpack Compose. Aquí conviven objetos de jerarquía como `AppNavigation.kt` (malla de ruteo) y `MainView.kt` al hombro con componentes estéticos granulares cómo `PerfilTextField.kt` hechos específicamente para la identidad empresarial.
*   `.ui.theme` 🖌: Sistema canónico centralizado de identidad. Define paletas, tipografías personalizadas (`AppBrushes.Main`, `PrimaryGreen`) que la UI consume inyectando consistencia global.
*   `.ui.viewmodel` 🧠: Centro de comandos lógicos. (e.g., `LoginViewModel.kt`, `RutinaViewModel.kt`).

---

## 🔄 Flujo de Trabajo (Ejemplo: Carga del Perfil de Usuario)

1.  **Inicio Visual**: Interfaz `PerfilUsuarioView` se invoca debido a un click de la barra inferior. En conjunto, exige la inyección del `PerfilUsuarioViewModel`.
2.  **Llamada al ViewModel**: Al ser creado, el bloque `init { cargarPerfil() }` invoca transparentemente tareas asincronas para traer información con estado "Loading" activado, mostrando una animación circular al cliente.
3.  **Obtención Local**: Interacción asíncrona mediante el `SessionManager`, rescatando con la corrutina el `clienteId` de la cache del DataStore a la velocidad de memoria.
4.  **Consulta de Red Remota**: El `viewModelScope.launch(Dispatchers.IO)` envía la función suspensiva `obtenerCliente(id)` de `Retrofit` hasta los repositorios de AWS (`http://nutricoach...`), con total asincronía.
5.  **Detección Excepcional (Try/Catch)**: 
    * El bloque verifica explícitamente y maneja `IOException` (Falla de WiFI del móvil del usuario).
    * Maneja e intercepta los `HttpException` (Errores 401, 500 originarios del backend). Notificando a la UI de forma suave de los fallos, impidiendo paralizaciones irreversibles.
6.  **Despliegue UI Re-activo**: Finalizada la carga exitosamente, la "Varita Mágica" de Compose detecta inmediatamente la modificación en las variables `var nombre by mutableStateOf("")` sustituyendo los placeholders por los datos del cliente, reactivando los input texts correspondientes para permitir interactuar de nuevo al cliente.

---

## 📝 Notas Adicionales

- La funcionalidad de notas está basada en la aplicación de notas dada en clase.
- La navegación e integración a la API también están basadas en los ejemplos vistos en clase.
- Para realizar el diseño de la aplicación me he ayudado de una IA.
