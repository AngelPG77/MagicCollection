# Changelog

## [Unreleased] - 2026-05-10

### Alcance de este corte
- Continuación del [Unreleased] del 2026-04-29. Esta entrada cubre los commits locales (desde `9824454`, 2026-04-30) hasta el estado actual del arbol de trabajo, listos para push a `origin/master`.
- Incluye eliminación del módulo `inventory`/`cards_owned` end-to-end, sistema de tema dinamico MTG, sincronización resiliente, internacionalizacion completa y unificación de componentes UI.

### Added

#### Android — Tema dinamico por gremio MTG
- Sistema completo de **10 paletas de gremio**: Azorius (WU), Dimir (UB), Rakdos (BR), Gruul (RG), Selesnya (GW), Orzhov (WB), Izzet (UR), Golgari (BG), Boros (RW), Simic (GU). Definidas en `ui/theme/Guild.kt` y materializadas en `ColorScheme`s cacheados en `GuildSchemes.kt`.
- **Material You hibrido**: el gremio activo aporta `primary/secondary/tertiary/onX/Container/onContainer`, Material You solo contribuye tokens de surface/neutral cuando esta disponible (Android 12+).
- Transiciones suaves entre gremios via `animateColorAsState` (tween 320ms) en `Theme.kt`.
- `CompositionLocalProvider` para `LocalGuild`, `LocalManaColor`, `LocalMtgSemanticColors`, `LocalAppSpacing`, `LocalAppShapes`, `LocalAppElevation`.
- Componente visual `GuildBadge` con drawables `ic_hybrid_wu/ub/br/rg/gw/wb/ur/bg/rw/gu.xml`.
- Iconos de mana `ic_mana_w/u/b/r/g/c.xml` aplicados en costes (`ManaCostRow`) y filtros de color en busqueda.

#### Android — UI unificada
- `GuildSearchBar` (barra pill con accento del gremio activo, separador vertical, magnifier/clear adaptativo) aplicada en 6 pantallas: `CollectionScreen`, `CollectionDetailScreen`, `WantListScreen`, `WantListDetailScreen`, `SearchScreen`, `CollectionAddCardScreen`, `WantListAddCardScreen`.
- Componente unificado `OwnedCardItem` para colecciones **y** wantlists: imagen 80dp + nombre + edit icon (esquina superior derecha) + chips (foil / idioma / condicion) + badge `×N` (esquina inferior derecha) + mana cost con simbolos.
- `EditOwnedCardModal` unificado con acción de eliminar embebida en el header (red tint).
- Iconos del bottom navigation renovados: `Archive` (Colecciones), `ContentCopy` (Mazos), `PhotoCamera` (Escaner).
- Iconos en Collections actualizados: `Inventory2` para "Todas las colecciones", `CreateNewFolder` para FAB de creacion.
- Componentes de estado nuevos: `EmptyState`, `ErrorState`, `LoadingState`, `CardDetailSkeleton` (con shimmer), `RarityBadge` (colores semanticos por rareza), `AccessibleIconButton` (requiere `contentDescription` no nulo).

#### Android — Sincronizacion resiliente
- `NetworkConnectivityObserver`: cold `Flow<Boolean>` basado en `ConnectivityManager.NetworkCallback`, detecta transiciones offline → online filtrando captive portals (`NET_CAPABILITY_VALIDATED`).
- `SyncDataWorker` (`HiltWorker`): WorkManager con `NetworkType.CONNECTED` + `BackoffPolicy.EXPONENTIAL` (30s base, hasta 5 reintentos). Idempotente y singleton por `ExistingWorkPolicy.KEEP`.
- `MainViewModel.observeConnectivity()` encola el worker automaticamente al recuperar conexion. Tambien encolado como red de seguridad si la sync directa de `syncAll()` falla.
- **Proteccion client-wins** en `CollectionRepository.syncCollections` y `WantListRepository.syncWantLists`: el pull bidireccional ya no sobreescribe `quantity` de cartas con `synced=false`, solo enlaza `remoteId`. El cambio local se preserva hasta el siguiente push exitoso.
- Permiso `ACCESS_NETWORK_STATE` añadido al manifest.

#### Android — Internacionalizacion completa
- Preferencia `appLanguage` con valor por defecto `"system"` (sigue el locale del dispositivo). `MagicCollectionActivity` solo aplica override de `LocalContext` cuando el usuario eligio explicitamente `"en"` o `"es"`.
- Selector de idioma en Ajustes con tres opciones: "Seguir el idioma del sistema" (icono `PhoneAndroid`), Ingles, Español.
- Funcion `localizedLanguageName(MtgLanguage)` con strings `lang_system/en/es/fr/de/it/pt/ja/ko/ru/zhs/zht` en ambos idiomas. Aplicada en subtitle de "Idioma de busqueda", dropdown de idiomas y mensajes del modal de descarga.
- `SyncCatalogWorker` y `DownloadLanguageWorker` usan strings localizadas en notificaciones del sistema (`notif_syncing_catalog`, `notif_downloading_language`, `notif_progress`, `notif_channel_*`).
- Version de app leida dinamicamente de `BuildConfig.VERSION_NAME` en lugar de hardcoded `"1.5.0"`.

#### Android — Mejoras visuales del icono y splash
- `installSplashScreen()` añadido en `onCreate` para transicion limpia entre tema splash y tema de la app.
- `splash_background` cambiado a `#FF0D0F14` en ambos modos (claro/oscuro) para coincidir con el fondo del icono adaptativo.
- `ic_launcher_foreground_inset.xml` envuelve el foreground con 18dp insets, encapsulando el medallon dorado dentro de la safe zone de 72dp (resuelve aura dorada en launchers circulares).
- Capa `<monochrome>` eliminada de los adaptive icons para evitar coloración ámbar de "themed icons" en Android 13+.
- `android:label=""` en la activity del manifest para suprimir título de ventana residual.

#### Backend
- Endpoint `GET /cards/index/sync-status?langs=...` para drift detection de doble capa (backend ↔ Scryfall y cliente ↔ backend).
- Endpoints CRUD de cartas en colecciones consolidados bajo `/collections/{id}/cards`: `POST`, `PUT /{cardId}`, `DELETE /{cardId}`.
- Endpoint `GET /collections/all-cards` para todas las cartas del usuario en una sola respuesta.

### Changed

#### Android — Base de datos (migración 16 → 17)
- **Drop de la tabla `cards_owned`**: el modulo Inventory fue eliminado, sustituido por `collection_cards` que ya tenía la misma cobertura funcional + metadata embebida para offline puro.
- Clase huerfana `CardNameFtsEntity` eliminada (tabla `card_names_fts` ya dropeada en migracion 13→14 pero la `@Entity` Kotlin permanecia, lo que hacía que Room la recreara).
- `CollectionCardDao` añade `observeUnsyncedCardsCount(userId)` y `getAllImageUrls(userId)` para sustituir los del eliminado `CardOwnedDao`.

#### Backend — Módulo Inventory eliminado
- Carpeta entera `pga.magiccollectionspring.inventory` borrada (34 archivos: controller, mapper, DTOs, 5 directorios commands/queries, entity, repos, enums).
- Endpoints retirados: `POST /your-cards/add`, `PUT /your-cards/update/{id}`, `DELETE /your-cards/delete/{id}`, `GET /your-cards/collection/{collectionId}`, `GET /your-cards/search/global`, `GET /your-cards/search/collection/{collectionId}`, `GET /your-cards/search/type`, `GET /your-cards/{id}`.
- La funcionalidad CRUD básica está cubierta por los nuevos endpoints `/collections/{id}/cards`. Las busquedas server-side (`search/global`, `search/type`) no se migran — la app usa el catalogo Scryfall local con FTS para busqueda offline.

#### Android — Frontend Inventory eliminado
- Archivos eliminados: `InventoryRepository`, `InventoryApi`, `InventoryDtos`, `CardOwnedDao`, `CardOwnedQueryDao`, `CardOwnedEntity`, y los 4 use cases (`AddCardUseCase`, `DeleteCardUseCase`, `GetCardsInCollectionUseCase`, `UpdateCardUseCase`).
- `ObserveSyncStatusUseCase` y `PrefetchImagesWorker` consultan ahora `CollectionCardDao` en lugar de `CardOwnedDao`.
- `DatabaseModule.provideCardOwnedDao` y `NetworkModule.provideInventoryApi` eliminados.

#### Android — Otros cambios
- Auditoria de strings hardcodeadas: ninguna detectada en `ui/screen/` ni `ui/component/`. Solo restan strings tecnicas (codigos de idioma, nombres de enums).
- 3 strings sin uso eliminadas (`search_filter_artist`, `search_filter_mana_cost`, `search_filter_text`) en ambos `strings.xml`.
- 4 traducciones añadidas en español que faltaban (`wantlist_created_success`, `wantlist_updated_success`, `wantlist_deleted_success`, `wantlist_card_removed_success`).
- Color del numero en pips de mana genericos fijado a tono oscuro (`#1C1B1F`) para contraste estable sobre el fondo gris claro independientemente del tema.
- Drawables `ic_launcher_background.xml` y `ic_launcher_foreground.xml` por defecto (Android Studio templates) eliminados.
- Carpeta `drawable/mana_symbols/` con 16 SVGs huerfanos eliminada (no compilables al estar en subcarpeta de drawable).

### Fixed

- **Pérdida silenciosa de cambios offline**: el pull bidireccional sobreescribía cartas con `synced=false` con datos del servidor obsoletos cuando el push había fallado. Ahora el reconcile distingue tres ramas (insert / client-wins skip / upsert) y preserva cambios pendientes.
- **Sin reintento al recuperar red**: cambios `synced=false` esperaban hasta que el usuario reabriera Collections/WantLists para subirse. Ahora `NetworkConnectivityObserver` dispara `SyncDataWorker` automaticamente en transiciones offline → online.
- **App bar duplicada** en algunas pantallas (residuo de tema AppCompat) — resuelto con `@android:style/Theme.DeviceDefault.NoActionBar` + `installSplashScreen()` + `android:label=""`.
- **Carrusel "Recently visited" vacio** — `AddRecentCardUseCase` se inyectaba en `MainViewModel` pero nunca se llamaba; movido a `CardDetailViewModel` donde se invoca tras cada carga exitosa de carta.
- **Notificaciones del sistema en inglés** aunque la app este en español — strings de `DownloadLanguageWorker`/`SyncCatalogWorker` (titulos, canales, descripciones) movidas a `strings.xml` con traducciones.
- **Aura dorada exagerada en el icono adaptativo** al recortar a círculo — capa `monochrome` eliminada + foreground con 18dp insets para encapsular el medallón en la safe zone.
- **Selector de idiomas en inglés** en pantallas de Ajustes y modal de descarga — los `displayName` hardcodeados de `MtgLanguage` sustituidos por `stringResource(R.string.lang_xx)`.
- **Build incremental roto tras refactor** — documentado en notas: tras eliminar clases procesadas por kapt (`@HiltViewModel`, `@Module`, `@Dao`), ejecutar `./gradlew clean` para regenerar stubs.

### Documentation
- README actualizado:
  - Eliminadas referencias a `cards_owned` y `/your-cards/*` (tabla y endpoints retirados).
  - Renombrada `card_names_fts` → `card_search_fts` (correccion: la primera fue dropeada en migracion 13→14, la actual incluye `oracle_text`).
  - Añadida tabla `collection_cards` al esquema Android.
  - Version de DB Android 13 → 17.
  - Endpoints `/collections/{id}/cards`, `/collections/all-cards`, `/cards/index/sync-status` añadidos.
  - Sección de tema dinamico por gremio en arquitectura Android.
  - Lista de modulos backend actualizada (sin `inventory`).
- changelog.md: esta entrada documenta el push hacia `origin/master` del 2026-05-10.

---

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
