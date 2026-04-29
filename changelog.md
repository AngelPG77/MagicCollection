# Changelog

## [Unreleased] - 2026-04-29

### Alcance de este corte
- Esta entrada documenta de forma consolidada los cambios acumulados desde el ultimo commit del repositorio (`6f7c997`, 2026-04-01) hasta el estado actual del arbol de trabajo.
- Incluye backend Spring, app Android, sincronización de catalogo, indices multiidioma, seguridad de sesion y capa de datos local/offline.

### Added

#### Backend (Spring Boot)
- **Refresh token persistente**:
  - Nueva entidad `refresh_tokens` y servicio dedicado (`RefreshTokenService`).
  - Nuevo endpoint `POST /auth/refresh-token`.
  - Login compatible con `rememberMe` para emitir token de refresco.
- **Modulo completo de WantList**:
  - Nuevas entidades `want_lists` y `want_list_cards`.
  - Controlador `WantListController` con CRUD de listas e items.
  - Integración con ownership por usuario autenticado.
- **Infraestructura de indices por idioma**:
  - Endpoints: `index/version`, `index/languages`, `index/{lang}/manifest`, `index/{lang}/page`, `index/{lang}/delta`, `index/{lang}/builds`, `index/rebuild/{lang}`, `index/{lang}/snapshot`.
  - Nuevas tablas de soporte: `index_language_state`, `index_language_stage_row`, `index_language_row_state`, `index_language_delta_entry`, `index_build_log`.
  - Flujo staging -> diff -> delta para distribución incremental por version.
- **Sincronización avanzada de catalogo**:
  - Tabla `card_catalog_sync_state` para control de token/version remota por bulk de Scryfall.
  - Sincronización de expansiones `mtg_sets`.
  - Soporte multiidioma con `card_localizations`.
  - Tarea programada diaria (`CardSyncScheduledService`) y health indicator (`LanguageIndexHealthIndicator`).
- **Nuevos endpoints de cartas**:
  - `GET /cards/autocomplete`
  - `GET /cards/random`
  - `GET /cards/scryfall/{id}`
  - `GET /cards/sets`
  - `POST /cards/sync-full`

#### Android
- **Refactor de arquitectura a Hilt + WorkManager**:
  - `@HiltAndroidApp` en `MagicCollectionApp`.
  - Modulos DI para red/repositorios/base de datos.
  - Worker de descarga de idioma con foreground notification (`DownloadLanguageWorker`).
- **Motor de busqueda local indexado**:
  - Repositorio dedicado `CardSearchIndexRepository`.
  - Tabla FTS4 `card_names_fts` + metadata en `master_cards`.
  - Descarga por snapshot/delta con checksum.
  - Estado por idioma en `language_index_state` y registro de idiomas en `downloaded_languages`.
- **WantList offline-first en Room**:
  - Tablas locales `want_lists` y `want_list_cards`.
  - APIs remotas para sincronización bidireccional (`WantListApi`).
- **Nuevas superficies de UI/flujo**:
  - Pantallas y ViewModels para busqueda, detalle de carta y wantlists.
  - Navegacion ampliada en `MagicCollectionActivity`.
- **Sesion robusta en cliente**:
  - Interceptor `AuthInterceptor` con refresh automatico en `401`.
  - Persistencia local de `jwt_token` + `refresh_token` + usuario.

### Changed

#### Modelo de datos y sincronización
- **`master_cards` (backend y Android)** enriquecida para filtros avanzados:
  - campos de rareza, CMC, mascaras de color/identidad e imagen.
- **`cards_owned`**:
  - consolidación por variante (idioma/condicion/foil) y merge de cantidades.
  - control de sincronización local con `synced` y `pendingDelete` en Android.
- **Entidades offline-first (Android)**:
  - `collections`, `cards_owned`, `want_lists`, `want_list_cards` incluyen `remoteId`, `synced`, `pendingDelete`.
  - migración Room 12 -> 13 para columna `pendingDelete`.

#### Seguridad y red
- `AuthApi` y DTOs de login adaptados a `token + refreshToken + userId`.
- `SecurityConfig` actualizado para permitir `POST /auth/refresh-token`.
- Cliente HTTP Android endurecido (timeouts amplios, logs sanitizados, interceptor autenticado separado).

#### Capa de cartas/indexado
- Pipeline de sincronización de catalogo dividido entre:
  - `default_cards` (catalogo base),
  - `all_cards` (localizaciones),
  - rebuild asincrono de indices por idioma solo cuando hay cambios efectivos.
- Endpoints de indice y descarga preparados para payloads grandes y consumo incremental.

### Fixed

- Prevención de builds/indexaciones duplicadas por idioma mediante locks y estado.
- Limpieza de staging rows (`index_language_stage_row`) al finalizar o fallar un build.
- Manejo de errores y reintentos en descarga de snapshots de idioma (incluyendo checksum mismatch).
- Reducción del riesgo de consumo excesivo de memoria en cliente al limitar logging HTTP en debug.
- Mayor consistencia entre estado de sesion local y estado remoto tras cambios de usuario/token.

### Documentation
- **README actualizado en profundidad**:
  - Estado real de arquitectura backend y Android.
  - Endpoints ampliados y corregidos.
  - Sección de **Estructura de Base de Datos** reescrita y desglosada tabla por tabla para:
    - MySQL (Spring Boot/Hibernate),
    - Room/SQLite (Android).
- Corrección de descripciones obsoletas (DI manual, alcance funcional antiguo, endpoints incompletos).
