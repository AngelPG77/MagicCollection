# MagicCollection - Proyecto Completo

Sistema completo de gestión de colecciones de cartas Magic: The Gathering.

## 📁 Estructura del Proyecto

```
MagicCollection/
├── MagicCollectionSpring/    # Backend - API REST con Spring Boot
│   ├── src/
│   ├── pom.xml
│   └── README.md
│
└── MagicCollectionAndroid/   # Frontend - App Android (Kotlin)
    └── (Proyecto Android Studio a crear)
```

## 🛠️ Tecnologías

### Backend (Spring)
- **Framework:** Spring Boot 3.4.1
- **Java:** 17
- **Base de Datos:** MySQL 8.x
- **Autenticación:** JWT + Spring Security
- **Arquitectura:** Hexagonal + CQRS

### Frontend (Android)
- **Framework:** Android Studio
- **Lenguaje:** Kotlin (a definir)
- **Min SDK:** TBD
- **Arquitectura:** TBD

## 🚀 Inicio Rápido

### Backend (Spring Boot)
```bash
cd MagicCollectionSpring
.\mvnw.cmd spring-boot:run
```

### Frontend (Android)
```bash
cd MagicCollectionAndroid
# Abrir proyecto en Android Studio
```

## 📚 Documentación

- **Backend API:** [Ver análisis completo](./documentacion/ANALISIS_COMPLETO_PROYECTO.md)
- **Endpoints:** http://localhost:8080/swagger-ui.html
- **Android:** TBD

## 🔗 Links Útiles

- **API Base URL:** http://localhost:8080
- **MySQL Database:** mtg_db
- **Scryfall API:** https://api.scryfall.com

## 📝 Notas

Este proyecto está en desarrollo activo.

**Última actualización:** 2026-03-23
