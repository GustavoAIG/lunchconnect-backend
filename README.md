# LunchConnect – Backend

Backend del proyecto LunchConnect: una plataforma web que conecta a profesionales a través de almuerzos de networking en restaurantes.

## 🏗 Tecnologías
- Java 17
- Spring Boot 3.5
- Spring Security + JWT
- PostgreSQL
- Spring Data JPA / Hibernate
- Maven

## 📐 Arquitectura
Arquitectura en capas + Domain-Driven Design:
src/main/java/com/lunchconnect/
├── presentation/ # Controladores y DTOs
├── application/ # Servicios y casos de uso
├── domain/ # Entidades del negocio
└── infrastructure/ # Seguridad, repos, correo, config


## 🚀 Configuración inicial
1. Clonar repo  
2. Crear archivo `application.properties`  
3. Añadir credenciales de PostgreSQL  
4. Ejecutar la app con Maven o IDE  

## 🔑 Funciones base del backend
- Registro y login con JWT
- Gestión de usuarios
- Gestión de restaurantes
- Gestión de eventos (almuerzos)
- Reservas y asistentes
