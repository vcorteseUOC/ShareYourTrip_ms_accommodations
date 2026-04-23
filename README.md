# ShareYourTrip_ms_accommodations

Microservicio de gestión de alojamientos para la plataforma ShareYourTrip.

## Descripción

Este microservicio es responsable de la gestión completa de alojamientos (accommodations) en la plataforma ShareYourTrip. Permite a los anfitriones crear y gestionar sus propiedades, y a los viajeros buscar alojamientos según diferentes criterios.

## Características Principales

- **Gestión de alojamientos**: Creación y consulta de alojamientos
- **Búsqueda avanzada**: Filtrado por ubicación, precio, capacidad, servicios y fechas
- **Gestión de imágenes**: Soporte para múltiples imágenes por alojamiento
- **Gestión de disponibilidad**: Fechas disponibles para reservas
- **Validación de origen**: Filtro que valida peticiones provenientes del API Gateway

## Arquitectura

```
API Gateway (8080)
   ↓ (valida JWT + añade headers X-User-Id, X-User-Roles)
Accommodations Microservice (8082)
   ↓ (valida header X-User-Id)
Procesa la petición
```

## Modelo de Datos

### Entidad Accommodation

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | Identificador único del alojamiento |
| hostId | Long | ID del anfitrión propietario |
| title | String (200) | Título del alojamiento |
| description | Text | Descripción detallada |
| addressLine | String (255) | Dirección completa |
| city | String (100) | Ciudad |
| country | String (100) | País |
| postalCode | String (20) | Código postal |
| latitude | Double | Latitud (coordenadas) |
| longitude | Double | Longitud (coordenadas) |
| pricePerNight | Double | Precio por noche |
| maxGuests | Integer | Máximo de huéspedes |
| roomType | Enum | Tipo de habitación (APARTMENT, PRIVATE_ROOM, SHARED_ROOM, ENTIRE_HOME) |
| rules | Text | Reglas del alojamiento |
| createdAt | LocalDateTime | Fecha de creación |
| updatedAt | LocalDateTime | Fecha de última actualización |

### Entidad AccommodationImage

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | Identificador único |
| accommodationId | Long | ID del alojamiento (FK) |
| imageUrl | String (255) | URL de la imagen |
| isCover | Boolean | Indica si es la imagen principal |

### Entidad AvailableDate

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | Identificador único |
| accommodationId | Long | ID del alojamiento (FK) |
| date | LocalDate | Fecha disponible |
| createdAt | LocalDateTime | Fecha de creación |

## Endpoints

### Obtener alojamiento por ID
```http
GET /accommodations/{id}
Authorization: Bearer {{token}}
```

**Respuesta (200):**
```json
{
  "id": 1,
  "hostId": 1,
  "title": "Cozy apartment in Barcelona",
  "description": "Beautiful apartment with sea views.",
  "addressLine": "Carrer de l'Example 123",
  "city": "Barcelona",
  "country": "Spain",
  "postalCode": "08001",
  "latitude": 41.3851,
  "longitude": 2.1734,
  "pricePerNight": 120.00,
  "maxGuests": 4,
  "roomType": "APARTMENT",
  "rules": "No smoking, no pets.",
  "createdAt": "2024-01-15T10:00:00",
  "updatedAt": "2024-01-15T10:00:00",
  "images": [
    {
      "id": 1,
      "imageUrl": "https://example.com/image1.jpg",
      "isCover": true
    }
  ],
  "availableDates": [
    "2024-06-01",
    "2024-06-02",
    "2024-06-03"
  ]
}
```

**Respuesta de error (404):**
```json
{
  "error": "Alojamiento no encontrado"
}
```

### Buscar alojamientos
```http
POST /accommodations/search
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "city": "Barcelona",
  "country": "Spain",
  "hostId": 1,
  "language": "ES",
  "minPrice": 50.00,
  "maxPrice": 200.00,
  "guests": 2,
  "roomType": "PRIVATE",
  "wifi": true,
  "washing": true,
  "air": true,
  "kitchen": true,
  "checkIn": "2024-06-01",
  "checkOut": "2024-06-10",
  "hostIds": [1, 2]
}
```

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "hostId": 1,
    "title": "Cozy apartment in Barcelona",
    "description": "Beautiful apartment with sea views.",
    "addressLine": "Carrer de l'Example 123",
    "city": "Barcelona",
    "country": "Spain",
    "postalCode": "08001",
    "latitude": 41.3851,
    "longitude": 2.1734,
    "pricePerNight": 120.00,
    "maxGuests": 4,
    "roomType": "APARTMENT",
    "rules": "No smoking, no pets.",
    "images": [
      {
        "id": 1,
        "imageUrl": "https://example.com/image1.jpg",
        "isCover": true
      }
    ]
  }
]
```

### Crear alojamiento
```http
POST /accommodations
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "hostId": 1,
  "title": "Cozy apartment in Barcelona",
  "description": "Beautiful apartment with sea views.",
  "addressLine": "Carrer de l'Example 123",
  "city": "Barcelona",
  "country": "Spain",
  "postalCode": "08001",
  "latitude": 41.3851,
  "longitude": 2.1734,
  "pricePerNight": 120.00,
  "maxGuests": 4,
  "roomType": "APARTMENT",
  "rules": "No smoking, no pets.",
  "facilities": {
    "wifi": true,
    "washing": true,
    "air": true,
    "kitchen": true
  },
  "images": [
    {
      "imageUrl": "https://example.com/image1.jpg",
      "isCover": true
    }
  ],
  "availableDates": [
    "2024-06-01",
    "2024-06-02",
    "2024-06-03"
  ]
}
```

**Respuesta (201):**
```json
{
  "id": 1,
  "hostId": 1,
  "title": "Cozy apartment in Barcelona",
  "description": "Beautiful apartment with sea views.",
  "addressLine": "Carrer de l'Example 123",
  "city": "Barcelona",
  "country": "Spain",
  "postalCode": "08001",
  "latitude": 41.3851,
  "longitude": 2.1734,
  "pricePerNight": 120.00,
  "maxGuests": 4,
  "roomType": "APARTMENT",
  "rules": "No smoking, no pets.",
  "createdAt": "2024-01-15T10:00:00",
  "updatedAt": "2024-01-15T10:00:00",
  "images": [
    {
      "id": 1,
      "imageUrl": "https://example.com/image1.jpg",
      "isCover": true
    }
  ],
  "availableDates": [
    "2024-06-01",
    "2024-06-02",
    "2024-06-03"
  ]
}
```

## Seguridad

### Validación de Origen

El microservicio implementa un filtro `GatewayAuthenticationFilter` que:
- Valida la presencia del header `X-User-Id` enviado por el API Gateway
- Rechaza peticiones directas sin este header (401)

### Configuración Spring Security

- Spring Security configurado con `permitAll()` (validación manual)
- CSRF deshabilitado
- Filtro `GatewayAuthenticationFilter` añadido antes de `UsernamePasswordAuthenticationFilter`

## Configuración

### application.yaml

```yaml
server:
  port: 8082
  servlet:
    context-path: /

spring:
  application:
    name: share-your-trip-accommodations
  datasource:
    url: jdbc:postgresql://localhost:54320/product
    username: product
    password: product
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

# Users Service URL
users:
  service:
    url: http://localhost:8081
```

## Tecnologías

- Spring Boot 3.5.13
- Spring Data JPA
- Spring Security
- Spring Cloud OpenFeign (para comunicación con servicio users)
- Spring Boot Starter Web
- PostgreSQL
- Lombok
- Jakarta Validation API

## DTOs

### AccommodationRequestDto
```java
{
  "hostId": 1,
  "title": "Cozy apartment",
  "description": "Beautiful apartment",
  "addressLine": "Carrer de l'Example 123",
  "city": "Barcelona",
  "country": "Spain",
  "postalCode": "08001",
  "latitude": 41.3851,
  "longitude": 2.1734,
  "pricePerNight": 120.00,
  "maxGuests": 4,
  "roomType": "APARTMENT",
  "rules": "No smoking",
  "facilities": {
    "wifi": true,
    "washing": true,
    "air": true,
    "kitchen": true
  },
  "images": [
    {
      "imageUrl": "https://example.com/image.jpg",
      "isCover": true
    }
  ],
  "availableDates": ["2024-06-01"]
}
```

### AccommodationResponseDto
```java
{
  "id": 1,
  "hostId": 1,
  "title": "Cozy apartment",
  "description": "Beautiful apartment",
  "addressLine": "Carrer de l'Example 123",
  "city": "Barcelona",
  "country": "Spain",
  "postalCode": "08001",
  "latitude": 41.3851,
  "longitude": 2.1734,
  "pricePerNight": 120.00,
  "maxGuests": 4,
  "roomType": "APARTMENT",
  "rules": "No smoking",
  "createdAt": "2024-01-15T10:00:00",
  "updatedAt": "2024-01-15T10:00:00",
  "images": [...],
  "availableDates": [...]
}
```

### AccommodationFilterRequestDto
```java
{
  "city": "Barcelona",
  "country": "Spain",
  "hostId": 1,
  "language": "ES",
  "minPrice": 50.00,
  "maxPrice": 200.00,
  "guests": 2,
  "roomType": "PRIVATE",
  "wifi": true,
  "washing": true,
  "air": true,
  "kitchen": true,
  "checkIn": "2024-06-01",
  "checkOut": "2024-06-10",
  "hostIds": [1, 2]
}
```

## Tipos de Habitación

| Tipo | Descripción |
|------|-------------|
| APARTMENT | Apartamento completo |
| PRIVATE_ROOM | Habitación privada |
| SHARED_ROOM | Habitación compartida |
| ENTIRE_HOME | Casa completa |

## Cómo Ejecutar

### Compilar
```bash
mvnw.cmd clean package -DskipTests
```

### Ejecutar
```bash
java -jar target/*.jar
```

El servicio estará disponible en `http://localhost:8082`

## Notas Importantes

- El microservicio debe recibir peticiones únicamente a través del API Gateway
- Las coordenadas (latitud, longitud) se utilizan para mapas y cálculos de distancia
- Las fechas disponibles deben actualizarse cuando se realizan reservas
- El campo `isCover` en imágenes indica cuál es la imagen principal para mostrar en listados
- El servicio usa OpenFeign para comunicarse con el servicio users y validar el anfitrión
