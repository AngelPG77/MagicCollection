# MagicCollection

Sistema completo para gestionar colecciones de cartas de *Magic: The Gathering* con:

- Backend Java/Spring (`MagicCollectionSpring`)
- App Android Kotlin/Compose (`MagicCollectionAndroid`)

## Estructura del repositorio

```text
MagicCollection/
├── MagicCollectionSpring/     # API REST + MySQL + JWT + Scryfall
└── MagicCollectionAndroid/    # App Android (Compose + Room + Retrofit)
```

## Estado actual (implementado)

### Backend (`MagicCollectionSpring`)

- Autenticación con JWT (`register`, `login`, actualización de usuario y password, borrado de usuario).
- Gestión de colecciones por usuario autenticado (CRUD).
- Inventario de cartas poseídas (`cards_owned`) con cantidad, condición, idioma y foil.
- Catálogo maestro de cartas (`master_cards`) con búsqueda local y descubrimiento vía Scryfall.
- Integración con Scryfall para búsqueda y carga de cartas no existentes en catálogo local.
- Swagger/OpenAPI habilitado.

### Android (`MagicCollectionAndroid`)

- Pantalla Compose de demo funcional con flujo completo:
  - Registro y login
  - Búsqueda de cartas contra backend (que consulta Scryfall)
  - Creación de colección local (offline-first)
  - Sincronización manual de colecciones locales pendientes al backend
- Persistencia local con Room.
- Sesión local cifrada (`EncryptedSharedPreferences`).
- Arquitectura MVVM + Use Cases + Repositories (la UI no accede directamente a repositorios).
- Inyección de dependencias por constructor/factory (DI manual en `MainActivity`).

## Arquitectura y patrones

### Backend

- Estructura modular por dominios (`auth`, `card`, `collection`, `inventory`, `user`, `shared`).
- Patrón por capas: **Controller → Application Service → Repository/Domain**.
- CQRS ligero en capa de aplicación (`Command/Query/Response` + `ICommandService` / `IQueryService`).
- Repositorios de dominio por interfaz + implementación con Spring Data JPA.
- Mappers dedicados para separar entidades de DTOs.
- Seguridad desacoplada con `CurrentUserProvider` inyectable.

### Android

- **MVVM** con `MainViewModel` y `StateFlow<MainUiState>`.
- **Use Cases** para encapsular acciones de negocio (`LoginUseCase`, `SearchCardsUseCase`, etc.).
- **Repository pattern** para acceso a datos remotos/locales.
- **Offline-first** en colecciones: persistencia local + sincronización explícita.
- Separación de responsabilidades en DAO:
  - `CardOwnedDao`: operaciones CRUD/base
  - `CardOwnedQueryDao`: búsquedas/filtros complejos

## Refactorización

Este apartado resume la refactorización aplicada y el estado resultante de la arquitectura en ambos proyectos.

### Backend (`MagicCollectionSpring`)

- Se consolidó una estructura **monolítica modular** por dominio: `auth`, `user`, `card`, `collection`, `inventory` y `shared`.
- Se reforzó el estilo **Ports & Adapters (Hexagonal)**:
  - `domain`: entidades + contratos (`IUserRepository`, `ICardRepository`, etc.).
  - `application`: casos de uso command/query.
  - `infrastructure`: implementaciones JPA y adaptadores técnicos (`ScryfallAdapter`).
  - `api`: controladores REST + DTOs + mappers.
- Se aplica **CQRS ligero**:
  - Escritura: servicios command (`CreateCollectionService`, `AddCardService`, etc.).
  - Lectura: servicios query (`GetCollectionsByUserService`, `SearchGlobalService`, etc.).
  - Lecturas asíncronas para I/O externo (`IQueryServiceAsync`) en búsquedas Scryfall.
- Se homogenizó la capa de servicios con contratos compartidos:
  - `ICommandService<TCommand, TResponse>`
  - `IQueryService<TQuery, TResponse>`
  - `IQueryServiceAsync<TQuery, TResponse>`
  - `IRepository<T, ID>`
- Se mejoró la DI y testabilidad:
  - Inyección por constructor en controladores/servicios/componentes.
  - Sin uso de `@Autowired` en campos.
  - Introducción de `CurrentUserProvider` para desacoplar lógica de negocio del acceso directo al `SecurityContextHolder`.
- Se centralizó el manejo de errores en `GlobalExceptionHandler` para eliminar manejo repetitivo en controladores.

### Android (`MagicCollectionAndroid`)

- Se consolidó la arquitectura **MVVM + Use Cases + Repositories** para impedir que la UI modifique repositorios directamente.
- Flujo de dependencias actual:
  - `Compose UI` -> `MainViewModel` -> `UseCase` -> `Repository` -> `Room/Retrofit`.
- Se aplicó separación explícita de responsabilidades:
  - `MainUiState` para estado único de pantalla.
  - `MainViewModel` para orquestación de casos de uso, estado y mensajes UI.
  - Repositorios dedicados por contexto (`AuthRepository`, `CardSearchRepository`, `CollectionSyncRepository`, `SessionRepository`).
- Se reforzó el patrón de persistencia offline-first en colecciones:
  - creación local con marca `synced = false`,
  - sincronización manual con backend.
- DI actual en Android:
  - Inyección por constructor/factory (composition root en `MainActivity` + `MainViewModel.Factory`).

## Diseño de Base de Datos

### Tablas de MySQL (backend Spring/JPA)

#### `users`

- Objetivo: almacenar identidad y credenciales de autenticación.
- Columnas:
  - `id` (PK, `Long`, auto-increment): identificador interno estable.
  - `username` (único, no nulo): clave funcional de login.
  - `password` (no nulo): hash de contraseña.
- Claves:
  - PK: `id` por simplicidad referencial y rendimiento en joins.
  - Restricción única: `username` para evitar colisiones de cuenta.

#### `collections`

- Objetivo: representar carpetas/lotes de cartas propiedad de un usuario.
- Columnas:
  - `id` (PK, auto-increment): identificador interno de colección.
  - `name` (no nulo): nombre visible de la colección.
  - `user_id` (FK -> `users.id`, no nulo): propietario.
- Claves:
  - PK: `id` para operaciones CRUD directas.
  - FK: `user_id` para ownership y autorización por usuario.
- Razón de diseño:
  - permite colecciones con mismo nombre entre usuarios distintos,
  - permite validar unicidad por usuario en capa de servicio/repositorio.

#### `master_cards`

- Objetivo: catálogo maestro de cartas (local cache + datos enriquecidos de Scryfall).
- Columnas:
  - `id` (PK, auto-increment): id interno para relaciones.
  - `name` (no nulo): nombre de carta.
  - `set_code` (no nulo): edición/set.
  - `scryfall_id` (único, no nulo): identificador global externo.
  - `oracle_text` (texto): reglas de carta.
  - `type_line` (texto): tipo/subtipo.
  - `mana_cost` (texto): coste de maná.
  - `converted_mana_cost` (entero): CMC para filtros.
- Claves:
  - PK: `id` para joins internos rápidos.
  - Única: `scryfall_id` para evitar duplicados globales del catálogo.

#### `cards_owned`

- Objetivo: inventario físico poseído por usuario a través de sus colecciones.
- Columnas:
  - `id` (PK, auto-increment): identificador de registro poseído.
  - `quantity` (no nulo): número de copias.
  - `is_foil` (no nulo): variante foil/no foil.
  - `card_condition` (enum texto, no nulo): estado de conservación.
  - `language` (enum texto): idioma.
  - `collection_id` (FK -> `collections.id`, no nulo): colección propietaria.
  - `card_master_id` (FK -> `master_cards.id`, no nulo): carta del catálogo.
- Claves:
  - PK: `id` para edición/eliminación de registros específicos.
  - FKs: `collection_id` y `card_master_id` para integridad referencial.
- Razón de diseño:
  - desacopla catálogo maestro de la variante física,
  - permite múltiples variantes por carta (condición/idioma/foil) y consolidación de cantidad.

### Tablas de Room (Android)

#### `users`

- Objetivo: cache local de usuarios conocidos por el dispositivo.
- Columnas:
  - `id` (PK manual): id local asociado a sesión.
  - `username`: nombre de usuario.
- Claves:
  - PK simple por consistencia con el resto de tablas locales.

#### `collections`

- Objetivo: persistir colecciones locales para flujo offline-first.
- Columnas:
  - `localId` (PK autogenerada): identificador local estable.
  - `remoteId` (nullable): id remoto cuando sincroniza con backend.
  - `name`: nombre de colección.
  - `userId`: propietario local.
  - `synced`: bandera de estado de sincronización.
- Claves:
  - PK: `localId` para operaciones locales inmediatas.
  - FK: `userId` -> `users.id` (`ON DELETE CASCADE`).
  - Índice en `userId` para consultas por usuario (`observeCollectionsByUserId`).
- Razón de diseño:
  - diferencia explícita entre identidad local y remota,
  - permite colas de sincronización sin bloquear UX.

#### `master_cards`

- Objetivo: catálogo maestro en local para búsquedas y joins de inventario.
- Columnas:
  - `scryfallId` (PK): clave natural global.
  - `remoteId` (nullable): id interno remoto (si aplica).
  - `name`, `setCode`, `oracleText`, `typeLine`.
  - `manaCost`, `convertedManaCost`.
  - `imageUrl`.
- Claves:
  - PK natural (`scryfallId`) para alineación directa con API externa y joins locales.

#### `cards_owned`

- Objetivo: guardar instancias poseídas con granularidad por variante.
- Columnas:
  - `scryfallId`: carta base.
  - `collectionId`: colección local.
  - `language`: variante por idioma.
  - `condition`: variante por condición.
  - `isFoil`: variante foil.
  - `quantity`: cantidad acumulada.
  - `remoteId` (nullable): referencia remota al sincronizar.
  - `Synced`: estado de sincronización.
- Claves:
  - PK compuesta:
    - (`scryfallId`, `collectionId`, `language`, `condition`, `isFoil`)
- Razón de diseño de PK compuesta:
  - evita duplicados exactos de la misma variante,
  - permite merge de cantidad cuando la carta/variante ya existe.
- Nota técnica actual:
  - esta entidad no declara `foreignKeys` de Room explícitos, aunque su modelo lógico se relaciona con `collections` y `master_cards`.

## Funcionalidades actuales

### 1. Autenticación y sesión

- Backend:
  - registro (`/auth/register`), login (`/auth/login`),
  - actualización de username/password,
  - borrado de cuenta autenticada.
- Seguridad:
  - JWT stateless,
  - endpoints protegidos para colecciones e inventario,
  - resolución de usuario actual via `CurrentUserProvider`.
- Android:
  - persistencia de token + usuario en `EncryptedSharedPreferences`,
  - recuperación automática de estado de sesión al iniciar,
  - interceptor HTTP que agrega `Authorization: Bearer`.

### 2. Catálogo de cartas y búsqueda

- Descubrimiento remoto:
  - `/cards/discover?query=...` consulta Scryfall vía backend.
- Búsqueda exacta por nombre:
  - `/cards/search?name=...` consulta catálogo local y, si no existe, obtiene de Scryfall y persiste.
- Consulta de librería local:
  - `/cards/library` y `/cards/{id}`.
- Campos de coste de maná:
  - `manaCost` y `convertedManaCost` disponibles en backend y Android para búsquedas/filtros.

### 3. Gestión de colecciones

- Backend:
  - crear/listar/obtener/actualizar/eliminar colecciones.
  - validación de ownership por usuario autenticado.
  - validación de conflicto por nombre duplicado del mismo owner.
- Android:
  - creación local de colecciones aun sin sincronizar.
  - observación reactiva de colecciones locales por usuario.

### 4. Inventario de cartas poseídas

- Backend:
  - alta de carta en colección (`/your-cards/add`) con validación de ownership.
  - merge de cantidad si la misma variante ya existe (misma carta + condición + idioma + foil).
  - actualización de registros con lógica de consolidación de variantes.
  - eliminación de registro poseído.
  - búsquedas:
    - por colección,
    - global del usuario,
    - por tipo.
- Diseño funcional:
  - separación entre catálogo (`master_cards`) e instancia poseída (`cards_owned`) evita duplicidad semántica.

### 5. Offline-first y sincronización

- Flujo actual Android:
  1. usuario logueado crea colección local (`synced=false`);
  2. la colección aparece al instante en UI;
  3. al pulsar **Sincronizar**, se crean en backend las pendientes;
  4. se marcan como `synced=true` y se guarda `remoteId`.
- Beneficio:
  - UX sin bloqueo de red,
  - reintento manual controlado por usuario.

### 6. UX y capa de presentación Android

- Pantalla Compose única de demo con bloques funcionales:
  - auth, búsqueda, creación local, sincronización, listado.
- `MainViewModel` controla:
  - loading states por operación,
  - mensajes de éxito/error,
  - sesión activa,
  - resultados de búsqueda,
  - lista de colecciones locales.
- Manejo de errores diferenciado:
  - validación local (`IllegalArgumentException`),
  - errores HTTP,
  - errores de conectividad (`IOException`).

## Cumplimiento de requisitos del Informe Técnico (11/03/2026)

### 1) Estructura de directorios (Monolito Modular)

**Cumplido.**  
El backend mantiene estructura modular por dominio:

- `shared`
- `auth`
- `user`
- `card`
- `collection`
- `inventory`

### 2) Definición de la arquitectura

#### Monolito Modular

**Cumplido.**  
Los módulos tienen responsabilidades diferenciadas y contratos de dominio explícitos.

#### Arquitectura Hexagonal (Ports & Adapters)

**Cumplido en el backend.**  
El dominio define interfaces (repositorios/puertos) y la infraestructura implementa detalles técnicos (JPA, cliente Scryfall).

#### CQRS

**Cumplido.**  
Separación efectiva entre comandos y consultas; además, consultas asíncronas para integraciones I/O.

### 3) Contratos y calidad de código

#### Abstracciones genéricas

**Cumplido.**  
Se usan contratos en `shared/abstractions`: `ICommandService`, `IQueryService`, `IQueryServiceAsync`, `IRepository` (más `IMapper` como contrato adicional de mapeo).

#### Inyección de dependencias limpia

**Cumplido.**  
No se detecta `@Autowired` en campos y la inyección se realiza por constructor.

### 4) Métricas de impacto (estado actual medido)

Medición realizada sobre el código actual:

- Archivos Java: **115**
- Carpetas de paquete (recursivo): **61**
- Módulos de dominio principales: **5** (`auth`, `user`, `card`, `collection`, `inventory`) + `shared`
- Archivos en `shared/abstractions`: **5**
- Archivos en `shared/exception`: **5** (incluye `GlobalExceptionHandler` + excepciones de dominio)
- `try/catch` en controladores: **0**
- `@Autowired` en campos: **0**

Conclusión de cumplimiento frente al informe:  
Se cumplen los requisitos estructurales, de patrones y de calidad definidos en el informe técnico; además, las métricas actuales mantienen (y en varios puntos superan) la escala de modularización indicada.

## Requisitos

### Generales

- Windows + PowerShell (comandos de este README orientados a Windows).

### Backend

- Java 21
- MySQL (configurado en `MagicCollectionSpring/src/main/resources/application.properties`)

Valores actuales:

- `spring.datasource.url=jdbc:mysql://localhost:3307/mtg_db`
- `spring.datasource.username=root`
- `spring.datasource.password=root`
- `server.port=8080`

### Android

- Android Studio reciente
- Emulador Android activo
- SDK de compilación 36
- Min SDK 26

## Ejecución rápida

## 1) Levantar backend

```powershell
cd .\MagicCollectionSpring
.\mvnw.cmd spring-boot:run
```

API base: `http://localhost:8080`

Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 2) Levantar app Android

Abre `MagicCollectionAndroid` en Android Studio y ejecuta en emulador.

La app usa:

- `http://10.0.2.2:8080/` como base URL (acceso del emulador al host local)

## 3) Flujo recomendado de prueba end-to-end

1. Registrar usuario en la app.
2. Loguear usuario.
3. Buscar cartas (backend + Scryfall).
4. Crear colección local (queda pendiente de sync).
5. Pulsar **Sincronizar** para enviarla al backend.

## Endpoints principales del backend

### Auth

- `POST /auth/register`
- `POST /auth/login`
- `PUT /auth/update-username`
- `PUT /auth/update-password`
- `DELETE /auth/delete`

### Cartas (catálogo)

- `GET /cards/search?name=...`
- `GET /cards/discover?query=...`
- `GET /cards/library`
- `GET /cards/{id}`

### Colecciones

- `POST /collections`
- `GET /collections`
- `GET /collections/{id}`
- `PUT /collections/{id}`
- `DELETE /collections/{id}`

### Inventario (cartas poseídas)

- `POST /your-cards/add`
- `PUT /your-cards/update/{id}`
- `DELETE /your-cards/delete/{id}`
- `GET /your-cards/collection/{collectionId}`
- `GET /your-cards/search/global?term=...`
- `GET /your-cards/search/collection/{collectionId}?term=...`
- `GET /your-cards/search/type?type=...`

## Build local (verificación rápida)

Backend:

```powershell
cd .\MagicCollectionSpring
.\mvnw.cmd clean compile
```

Android:

```powershell
cd .\MagicCollectionAndroid
.\gradlew.bat clean build -x test --no-daemon
```
