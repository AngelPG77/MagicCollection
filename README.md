# MagicCollection

Sistema completo para gestionar colecciones de cartas de *Magic: The Gathering* con:

- **Backend** Java/Spring (`MagicCollectionSpring`)
- **App Android** Kotlin/Compose (`MagicCollectionAndroid`)

## Estructura del repositorio

```text
MagicCollection/
├── MagicCollectionSpring/     # API REST + MySQL + JWT + sincronización Scryfall
└── MagicCollectionAndroid/    # App Android (Compose + Room + Retrofit + Hilt + WorkManager)
```

## Estado actual (implementado)

### Backend (`MagicCollectionSpring`)

- Autenticación con JWT (`register`, `login`, actualización de usuario/contraseña, borrado de cuenta).
- Soporte de **refresh token** (`/auth/refresh-token`) para sesiones persistentes.
- CRUD de colecciones por usuario autenticado, con cartas embebidas (`POST/PUT/DELETE /collections/{id}/cards`).
- Modulo de **WantList** completo (`want_lists` + `want_list_cards`).
- Catalogo maestro de cartas con sincronización parcial/completa desde Scryfall.
- Sincronización de expansiones (`mtg_sets`).
- Indices multiidioma con manifest/snapshot/delta/build logs y endpoint de drift (`/cards/index/sync-status`).
- Programación de sincronización periodica y health indicator del indice.
- Swagger/OpenAPI habilitado.

### Android (`MagicCollectionAndroid`)

- UI Compose con navegacion por pantallas (home, busqueda, detalle, ajustes, auth, colecciones, wantlists, etc.).
- Arquitectura **MVVM + Use Cases + Repositories**.
- Inyeccion de dependencias con **Hilt**.
- Persistencia local con **Room** (version 17) y sincronización offline-first.
- Busqueda local con **FTS4** (`card_search_fts`) multiidioma + filtros por metadatos (`master_cards`).
- Descarga de idiomas y catalogo en segundo plano con **WorkManager** (`DownloadLanguageWorker`, `SyncCatalogWorker`).
- **Sincronización resiliente**: `SyncDataWorker` + `NetworkConnectivityObserver` reanuda automaticamente la subida de cambios al recuperar conexion; el pull respeta cambios locales con `synced=false` (client-wins).
- **Tema dinamico por gremio MTG**: 10 paletas (Azorius, Dimir, Rakdos, Gruul, Selesnya, Orzhov, Izzet, Golgari, Boros, Simic) con Material You hibrido (gremio aporta brand colors, sistema aporta surface tokens).
- Internacionalizacion completa con idioma seguido del sistema por defecto (override manual en Ajustes).
- Sesion local cifrada (`EncryptedSharedPreferences`) con JWT + refresh token.
- Interceptor de red con refresco automatico de token ante `401`.

## Arquitectura y patrones

### Backend

- Monolito modular por dominio: `auth`, `user`, `card`, `collection`, `wantlist`, `shared`.
- Patrón por capas: **Controller -> Application Service -> Repository/Domain**.
- CQRS ligero para comandos/consultas.
- Contratos de servicio/repositorio en `shared/abstractions` y dominio.
- Seguridad desacoplada con `CurrentUserProvider` y filtros JWT.

### Android

- `Compose UI -> ViewModel -> UseCase -> Repository -> Room/Retrofit`.
- Estado de UI centralizado por pantalla (`UiState` + `StateFlow`).
- Persistencia local desacoplada por DAOs especializados.
- Sincronización explicita de entidades locales con backend (`synced`, `pendingDelete`, `remoteId`) con política **client-wins** para cambios pendientes.
- Capa de tema con `CompositionLocalProvider` para gremio activo + colores semanticos (`LocalGuild`, `LocalMtgSemanticColors`, `LocalAppSpacing`).
- Resiliencia de sync vía `NetworkConnectivityObserver` (callback flow sobre `ConnectivityManager`) + `SyncDataWorker` (`HiltWorker` con `NetworkType.CONNECTED` y backoff exponencial).

## Estructura de Base de Datos (detalle completo)

El proyecto usa **dos almacenes distintos**:

1. **MySQL (Spring Boot/Hibernate)** para la persistencia de servidor.
2. **Room/SQLite (Android)** para cache local, busqueda offline y cola de sincronización.

---

## Java Spring Boot (MySQL + JPA/Hibernate)

### 1) `users`
- **Objetivo:** identidad base de usuario para autenticación/autorización.
- **Clave primaria:** `id` (auto incremental).
- **Campos clave:** `username` (unico), `password` (hash).
- **Utilidad:** ancla de ownership para colecciones y wantlists; origen de identidad para JWT.

### 2) `refresh_tokens`
- **Objetivo:** persistir refresh tokens de larga duracion.
- **Clave primaria:** `id`.
- **Campos clave:** `user_id`, `token` (unico), `expiry_date`.
- **Utilidad:** renovar JWT sin re-login cuando el usuario marcó `rememberMe`.

### 3) `collections`
- **Objetivo:** contenedor de cartas del usuario.
- **Clave primaria:** `id`.
- **Relaciones:** FK `user_id -> users.id`.
- **Campos clave:** `name`, `user_id`.
- **Utilidad:** base del inventario del usuario (las cartas viven como entidades hijas embebidas accesibles via `/collections/{id}/cards`).

### 4) `collection_cards`
- **Objetivo:** cartas dentro de cada colección con todas sus variantes (idioma / condición / foil).
- **Clave primaria:** `id`.
- **Relaciones:** FK `collection_id -> collections.id`, FK `card_master_id -> master_cards.scryfall_id`.
- **Restricciones:** unique compuesta (`collection_id`, `card_master_id`, `is_foil`, `card_condition`, `language`).
- **Campos clave:** `quantity`, `is_foil`, `card_condition`, `language`, snapshot de metadata (`name`, `type_line`, `mana_cost`, `image_url`).
- **Utilidad:** evita duplicar variantes idénticas, permite merge de cantidades y se sirve a clientes en una sola respuesta junto con la colección padre.

### 5) `want_lists`
- **Objetivo:** listas de cartas deseadas por usuario.
- **Clave primaria:** `id`.
- **Relaciones:** FK `user_id -> users.id`.
- **Restricciones:** unique (`name`, `user_id`).
- **Utilidad:** separar intencion de compra/interes del inventario poseido.

### 6) `want_list_cards`
- **Objetivo:** items de cada wantlist.
- **Clave primaria:** `id`.
- **Relaciones:** FK `want_list_id -> want_lists.id`.
- **Campos clave:** `scryfall_id`, `name`, `quantity`, `foil`, `condition`, `language`.
- **Utilidad:** granularidad por variante objetivo (idioma/estado/foil) para seguimiento de objetivos.

### 7) `master_cards`
- **Objetivo:** catalogo maestro de cartas sincronizado con Scryfall.
- **Clave primaria:** `scryfall_id` (String, natural key global).
- **Indices:** `idx_card_name` sobre `name`.
- **Campos clave:** `oracle_id`, `name`, `printed_name`, `set_code`, `type_line`, `mana_cost`, `cmc`, `rarity`, `rarity_rank`, `color_mask`, `identity_mask`, `image_small_url`, `last_updated`.
- **Utilidad:** fuente canonica para busqueda, inventario y metadatos de filtro.

### 8) `card_localizations`
- **Objetivo:** nombre localizado por carta semantica (oracle) e idioma.
- **Clave primaria compuesta:** (`oracle_id`, `language_code`) via `@EmbeddedId`.
- **Indices:** idioma (`idx_card_loc_lang`) y oracle (`idx_card_loc_oracle`).
- **Campos clave:** `localized_name`, `last_updated`.
- **Utilidad:** soporta busqueda y snapshots multiidioma sin duplicar toda la carta.

### 9) `mtg_sets`
- **Objetivo:** catalogo local de expansiones.
- **Clave primaria:** `id`.
- **Restricciones:** `code` unico.
- **Campos clave:** `code`, `name`, `release_date`.
- **Utilidad:** filtros por set y sincronización de metadata desde Scryfall.

### 10) `card_catalog_sync_state`
- **Objetivo:** control de version remota por tipo de bulk de Scryfall.
- **Clave primaria:** `bulk_type`.
- **Campos clave:** `remote_version_token`, `last_synced_at`.
- **Utilidad:** evita descargas completas innecesarias si no cambió `default_cards` / `all_cards`.

### 11) `index_language_state`
- **Objetivo:** estado publicado del indice por idioma.
- **Clave primaria:** `language_code`.
- **Campos clave:** `version`, `checksum`, `total_rows`, `generated_at`, `source_last_updated`, `status`, `artifact_path`.
- **Utilidad:** base para `manifest`, validacion de staleness y control de salud.

### 12) `index_language_stage_row`
- **Objetivo:** staging temporal de filas durante una reconstrucción de indice.
- **Clave primaria compuesta:** (`build_token`, `language_code`, `scryfall_id`).
- **Campos clave:** `localized_name`, `row_hash`.
- **Utilidad:** comparar build nueva contra estado previo antes de publicar delta.

### 13) `index_language_row_state`
- **Objetivo:** estado materializado por carta/idioma del ultimo indice publicado.
- **Clave primaria compuesta:** (`language_code`, `scryfall_id`).
- **Campos clave:** `localized_name`, `row_hash`, `last_version`, `deleted`, `updated_at`, `deleted_at`.
- **Utilidad:** detectar cambios reales (upsert/delete) entre versiones.

### 14) `index_language_delta_entry`
- **Objetivo:** registro de cambios incrementales entre versiones de indice.
- **Clave primaria:** `id`.
- **Campos clave:** `language_code`, `target_version`, `target_version_long`, `scryfall_id`, `change_type`, `localized_name`, `created_at`.
- **Utilidad:** alimentar endpoint delta para clientes que ya tienen una version previa.

### 15) `index_build_log`
- **Objetivo:** auditoria y observabilidad de cada build de indice.
- **Clave primaria:** `id`.
- **Campos clave:** `language_code`, `version`, `status`, `started_at`, `finished_at`, `duration_ms`, `total_rows`, `upserts_count`, `deletes_count`, `checksum`, `build_token`, `error_message`.
- **Utilidad:** diagnostico operativo, troubleshooting y metricas historicas.

---

## Android (Room/SQLite)

### 1) `users`
- **Objetivo:** cache local de usuarios conocidos por el dispositivo.
- **Clave primaria:** `id` (remoto).
- **Campos clave:** `username`.
- **Utilidad:** resolver ownership local sin depender de red en cada pantalla.

### 2) `collections`
- **Objetivo:** colecciones locales offline-first.
- **Clave primaria:** `localId` (autogenerada).
- **Relaciones:** FK `userId -> users.id` (`ON DELETE CASCADE`), indice por `userId`.
- **Campos clave:** `remoteId`, `name`, `synced`, `pendingDelete`.
- **Utilidad:** separar identidad local/remota y soportar cola de alta/borrado.

### 3) `collection_cards`
- **Objetivo:** cartas poseidas en local con todas sus variantes (idioma/condicion/foil), incluyendo snapshot de metadata para render sin red.
- **Clave primaria:** `localId` (autogenerada).
- **Relaciones:** FK `collectionLocalId -> collections.localId` (`ON DELETE CASCADE`), indice por `collectionLocalId`.
- **Campos clave:** `remoteId`, `scryfallId`, `name`, `typeLine`, `manaCost`, `imageUrl`, `quantity`, `foil`, `language`, `condition`, `synced`, `pendingDelete`.
- **Utilidad:** sustituye al modelo relacional anterior (`cards_owned`); embebe metadata para offline puro, sincronización bidireccional con `synced`/`remoteId`/`pendingDelete` y reconciliación por la 4-tupla (`scryfallId`, `foil`, `language`, `condition`).

### 4) `want_lists`
- **Objetivo:** listas de deseo locales.
- **Clave primaria:** `localId` (autogenerada).
- **Relaciones:** FK `userId -> users.id`, indice por `userId`, unique (`name`, `userId`).
- **Campos clave:** `remoteId`, `name`, `synced`, `pendingDelete`.
- **Utilidad:** CRUD offline y sincronización posterior contra `/wantlists`.

### 5) `want_list_cards`
- **Objetivo:** items de cada wantlist en local.
- **Clave primaria:** `localId` (autogenerada).
- **Relaciones:** FK `wantListLocalId -> want_lists.localId`, indice por `wantListLocalId`.
- **Campos clave:** `remoteId`, `scryfallId`, `quantity`, `foil`, `condition`, `language`, `synced`, `pendingDelete`.
- **Utilidad:** seguimiento fino de objetivos por variante y cola de sincronización.

### 6) `master_cards`
- **Objetivo:** metadata local de cartas para render y filtros de busqueda.
- **Clave primaria:** `scryfallId`.
- **Indices:** `name`, `typeLine`, `colorMask`, `identityMask`, `rarityRank`, `cmc`.
- **Campos clave:** `printedName`, `setCode`, `manaCost`, `convertedManaCost`, `cmc`, `rarityRank`, `imageUrl`, `isDigital`.
- **Utilidad:** evita depender de red para filtros/paginación local.

### 7) `card_search_fts` (FTS4 virtual table)
- **Objetivo:** busqueda textual ultrarapida multiidioma sobre nombre y texto oraculo.
- **Tecnica:** FTS4 (`unicode61`), `language` marcado como `notIndexed` para filtrado por idioma sin penalizar el indice.
- **Campos clave:** `card_id`, `name`, `oracle_text`, `language`.
- **Utilidad:** autocompletado, busqueda por prefijos y filtros oracle en local, con fallback de idioma a inglés. La tabla `card_names_fts` previa quedó obsoleta y fue eliminada en la migración 13→14.

### 8) `downloaded_languages`
- **Objetivo:** registrar idiomas descargados en el dispositivo.
- **Clave primaria:** `languageCode`.
- **Campos clave:** `downloadedAt`.
- **Utilidad:** evita redescargas innecesarias y habilita UX de idiomas instalados.

### 9) `language_index_state`
- **Objetivo:** estado local de instalacion del indice por idioma.
- **Clave primaria:** `languageCode`.
- **Campos clave:** `installedVersion`, `checksum`, `rowCount`, `lastSyncAt`, `status`, `lastError`.
- **Utilidad:** control de progreso, validacion de integridad y reintentos de sincronización.

### 10) `mtg_sets`
- **Objetivo:** cache local de expansiones.
- **Clave primaria:** `code`.
- **Campos clave:** `name`, `releaseDate`.
- **Utilidad:** filtros por set desde app sin consultar backend en cada interacción.

### 11) `recent_cards`
- **Objetivo:** historial corto de cartas visitadas.
- **Clave primaria:** `scryfallId`.
- **Campos clave:** `name`, `imageUrl`, `visitedAt`.
- **Utilidad:** mostrar recientes y mejorar navegacion/reengagement.

---

## Endpoints principales del backend

### Auth
- `POST /auth/register`
- `POST /auth/login` (incluye `rememberMe`)
- `POST /auth/refresh-token`
- `PUT /auth/update-username`
- `PUT /auth/update-password`
- `DELETE /auth/delete`

### Cartas y catalogo
- `GET /cards/search?name=...&lang=...`
- `GET /cards/discover?...` (filtros avanzados)
- `GET /cards/autocomplete?query=...`
- `GET /cards/random`
- `GET /cards/scryfall/{id}?lang=...`
- `GET /cards/library`
- `GET /cards/{id}`
- `POST /cards/sync-full`
- `GET /cards/sets`

### Indice multiidioma
- `GET /cards/index/version`
- `GET /cards/index/languages`
- `GET /cards/index/sync-status?langs=...` (drift backend ↔ Scryfall y cliente ↔ backend)
- `GET /cards/index/{lang}/manifest`
- `GET /cards/index/{lang}/page?offset=...&limit=...`
- `GET /cards/index/{lang}/delta?sinceVersion=...`
- `GET /cards/index/{lang}/builds?limit=...`
- `POST /cards/index/rebuild/{lang}`
- `GET /cards/index/{lang}/snapshot`
- `GET /cards/index/{lang}/names/snapshot`

### Colecciones
- `GET /collections`
- `GET /collections/{id}` (incluye `cards: [...]` embebidas)
- `GET /collections/all-cards` (todas las cartas del usuario en todas sus colecciones)
- `POST /collections`
- `PUT /collections/{id}`
- `DELETE /collections/{id}`
- `POST /collections/{id}/cards`
- `PUT /collections/{id}/cards/{cardId}`
- `DELETE /collections/{id}/cards/{cardId}`

### WantList
- `GET /wantlists`
- `GET /wantlists/{id}`
- `POST /wantlists`
- `PUT /wantlists/{id}`
- `DELETE /wantlists/{id}`
- `POST /wantlists/{id}/cards`
- `PUT /wantlists/{id}/cards/{cardId}`
- `DELETE /wantlists/{id}/cards/{cardId}`

## Requisitos

### Backend
- Java 21
- MySQL
- Config en `MagicCollectionSpring/src/main/resources/application.properties`:
  - `spring.datasource.url=jdbc:mysql://localhost:3307/mtg_db`
  - `spring.datasource.username=${DB_USERNAME:root}`
  - `spring.datasource.password=${DB_PASSWORD:root}`
  - `server.port=8080`

### Android
- Android Studio reciente
- SDK compilacion: 36
- Min SDK: 26
- Base URL debug: `http://10.0.2.2:8080/`

## Ejecución rapida

### 1) Levantar backend

```powershell
cd .\MagicCollectionSpring
.\mvnw.cmd spring-boot:run
```

- API base: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### 2) Levantar app Android

Abrir `MagicCollectionAndroid` en Android Studio y ejecutar en emulador.

## Validación local recomendada

Backend:

```powershell
cd .\MagicCollectionSpring
.\mvnw.cmd test -q
```

Android:

```powershell
cd .\MagicCollectionAndroid
.\gradlew.bat testDebugUnitTest
```
