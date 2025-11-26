# 📡 Documentación: Datos que se Intercambian entre la App Android y la API .NET

## 🔄 Resumen General

La aplicación Android consume una API REST .NET que maneja **Hábitos (Habits)** y **Puntajes (Scores)**. Los datos se intercambian en formato **JSON** usando **Retrofit** en Android y **ASP.NET Core** en el backend.

---

## 📋 ENDPOINTS DE LA API

### Base URL
- **Desarrollo (Emulador)**: `http://10.0.2.2:5098/api/v1/`
- **Desarrollo (Dispositivo Físico)**: `http://192.168.x.x:5098/api/v1/`
- **Producción**: `https://demopagina.somee.com/api/v1/`

---

## 🎯 ENDPOINTS DE HÁBITOS (`/api/v1/habits`)

### 1. **GET** `/habits` - Obtener todos los hábitos
**Query Parameters (opcionales):**
- `type` (string): Filtrar por tipo de hábito
- `completed` (boolean): Filtrar por estado completado
- `category` (string): Filtrar por categoría
- `page` (int, default: 1): Número de página
- `pageSize` (int, default: 100): Tamaño de página

**Respuesta (200 OK):**
```json
{
  "success": true,
  "message": "Hábitos obtenidos correctamente",
  "habits": [
    {
      "id": 1,
      "title": "Ejercicio",
      "goal": "Goal: movimiento detectado",
      "category": "salud",
      "type": "EXERCISE",
      "completed": false,
      "points": 10,
      "targetValue": 0.0,
      "targetUnit": null,
      "pagesPerDay": null,
      "reminderTimes": null,
      "durationMinutes": null,
      "dndMode": null,
      "musicId": null,
      "journalEnabled": null,
      "gymDays": null,
      "waterGoalGlasses": null,
      "oneClickComplete": null,
      "englishMode": null,
      "codingMode": null,
      "habitIcon": null,
      "createdAt": "2025-01-11T10:30:00Z"
    }
  ]
}
```

**Headers de respuesta:**
- `X-Total-Count`: Total de registros
- `X-Page`: Página actual
- `X-Page-Size`: Tamaño de página
- `X-Total-Pages`: Total de páginas

---

### 2. **GET** `/habits/{id}` - Obtener un hábito por ID

**Respuesta (200 OK):**
```json
{
  "id": 1,
  "title": "Ejercicio",
  "goal": "Goal: movimiento detectado",
  "category": "salud",
  "type": "EXERCISE",
  "completed": false,
  "points": 10,
  "targetValue": 0.0,
  "targetUnit": null,
  "pagesPerDay": null,
  "reminderTimes": null,
  "durationMinutes": null,
  "dndMode": null,
  "musicId": null,
  "journalEnabled": null,
  "gymDays": null,
  "waterGoalGlasses": null,
  "oneClickComplete": null,
  "englishMode": null,
  "codingMode": null,
  "habitIcon": null,
  "createdAt": "2025-01-11T10:30:00Z"
}
```

**Error (404 Not Found):**
```json
{
  "error": "Hábito no encontrado",
  "message": "No se encontró un hábito con ID 999"
}
```

---

### 3. **POST** `/habits` - Crear un nuevo hábito

**Request Body:**
```json
{
  "title": "Leer libro",
  "goal": "Leer 20 páginas al día",
  "category": "educación",
  "type": "READ_BOOK",
  "completed": false,
  "points": 15,
  "targetValue": 20.0,
  "targetUnit": "páginas",
  "pagesPerDay": 20,
  "reminderTimes": "[\"08:00\", \"20:00\"]",
  "durationMinutes": null,
  "dndMode": null,
  "musicId": null,
  "journalEnabled": null,
  "gymDays": null,
  "waterGoalGlasses": null,
  "oneClickComplete": null,
  "englishMode": null,
  "codingMode": null,
  "habitIcon": "ic_habit_book"
}
```

**Respuesta (201 Created):**
```json
{
  "id": 5,
  "title": "Leer libro",
  "goal": "Leer 20 páginas al día",
  "category": "educación",
  "type": "READ_BOOK",
  "completed": false,
  "points": 15,
  "targetValue": 20.0,
  "targetUnit": "páginas",
  "pagesPerDay": 20,
  "reminderTimes": "[\"08:00\", \"20:00\"]",
  "durationMinutes": null,
  "dndMode": null,
  "musicId": null,
  "journalEnabled": null,
  "gymDays": null,
  "waterGoalGlasses": null,
  "oneClickComplete": null,
  "englishMode": null,
  "codingMode": null,
  "habitIcon": "ic_habit_book",
  "createdAt": "2025-01-11T12:00:00Z"
}
```

**Error (400 Bad Request):**
```json
{
  "title": ["El título es requerido"],
  "type": ["El tipo es requerido"]
}
```

---

### 4. **PUT** `/habits/{id}` - Actualizar un hábito existente

**Request Body:** (Igual que POST, pero con `id` en la URL)

**Respuesta (200 OK):**
```json
{
  "id": 1,
  "title": "Ejercicio Actualizado",
  "goal": "Nueva meta",
  "category": "salud",
  "type": "EXERCISE",
  "completed": true,
  "points": 20,
  ...
}
```

---

### 5. **DELETE** `/habits/{id}` - Eliminar un hábito

**Respuesta (200 OK):**
```json
{
  "success": true,
  "message": "Hábito eliminado correctamente",
  "habits": []
}
```

---

### 6. **POST** `/habits/sync` - Sincronizar múltiples hábitos (batch)

**Request Body:**
```json
[
  {
    "id": 0,
    "title": "Nuevo hábito",
    "type": "WALK",
    ...
  },
  {
    "id": 1,
    "title": "Hábito actualizado",
    "type": "EXERCISE",
    ...
  }
]
```

**Respuesta (200 OK):**
```json
{
  "success": true,
  "message": "Se sincronizaron 2 hábitos correctamente",
  "habits": [
    {
      "id": 5,
      "title": "Nuevo hábito",
      ...
    },
    {
      "id": 1,
      "title": "Hábito actualizado",
      ...
    }
  ]
}
```

---

## 🏆 ENDPOINTS DE PUNTAJES (`/api/v1/scores`)

### 1. **GET** `/scores` - Obtener todos los puntajes

**Query Parameters (opcionales):**
- `habitId` (int): Filtrar por ID de hábito
- `startDate` (DateTime): Fecha inicial
- `endDate` (DateTime): Fecha final
- `page` (int, default: 1)
- `pageSize` (int, default: 50)

**Respuesta (200 OK):**
```json
{
  "success": true,
  "message": "Scores obtenidos correctamente",
  "data": [
    {
      "id": 1,
      "habitId": 1,
      "habitTitle": "Ejercicio",
      "points": 10,
      "date": "2025-01-11T10:30:00Z"
    }
  ],
  "totalCount": 1,
  "page": 1,
  "pageSize": 50,
  "totalPages": 1
}
```

---

### 2. **GET** `/scores/{id}` - Obtener un puntaje por ID

**Respuesta (200 OK):**
```json
{
  "id": 1,
  "habitId": 1,
  "habitTitle": "Ejercicio",
  "points": 10,
  "date": "2025-01-11T10:30:00Z"
}
```

---

### 3. **POST** `/scores` - Crear un nuevo puntaje

**Request Body:**
```json
{
  "habitId": 1,
  "habitTitle": "Ejercicio",
  "points": 10,
  "date": "2025-01-11T10:30:00Z"
}
```

**Respuesta (201 Created):**
```json
{
  "id": 5,
  "habitId": 1,
  "habitTitle": "Ejercicio",
  "points": 10,
  "date": "2025-01-11T10:30:00Z"
}
```

---

### 4. **PUT** `/scores/{id}` - Actualizar un puntaje

**Request Body:** (Igual que POST)

---

### 5. **DELETE** `/scores/{id}` - Eliminar un puntaje

**Respuesta (200 OK):**
```json
{
  "success": true,
  "message": "Score eliminado correctamente"
}
```

---

### 6. **GET** `/scores/habit/{habitId}/total` - Obtener total de puntos de un hábito

**Respuesta (200 OK):**
```json
{
  "habitId": 1,
  "totalPoints": 150
}
```

---

## 📦 ESTRUCTURA DE DATOS

### HabitDto (Modelo de Datos de Hábito)

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `id` | long | No | ID del hábito (0 para nuevos) |
| `title` | string | **Sí** | Título del hábito (max 200 chars) |
| `goal` | string? | No | Meta del hábito (max 500 chars) |
| `category` | string? | No | Categoría (max 100 chars) |
| `type` | string | **Sí** | Tipo de hábito (max 50 chars) |
| `completed` | bool | No | Estado completado (default: false) |
| `points` | int | No | Puntos del hábito (default: 10, min: 0) |
| `targetValue` | double | No | Valor objetivo (default: 0.0) |
| `targetUnit` | string? | No | Unidad del objetivo (max 50 chars) |
| `pagesPerDay` | int? | No | Páginas por día (min: 1) |
| `reminderTimes` | string? | No | Horarios de recordatorio JSON (max 500 chars) |
| `durationMinutes` | int? | No | Duración en minutos (min: 1) |
| `dndMode` | bool? | No | Modo No Molestar |
| `musicId` | int? | No | ID de música |
| `journalEnabled` | bool? | No | Journal habilitado |
| `gymDays` | string? | No | Días de gym JSON (max 200 chars) |
| `waterGoalGlasses` | int? | No | Vasos de agua objetivo (min: 1) |
| `oneClickComplete` | bool? | No | Completar con un toque |
| `englishMode` | bool? | No | Modo inglés |
| `codingMode` | bool? | No | Modo coding |
| `habitIcon` | string? | No | Nombre del ícono (max 100 chars) |
| `createdAt` | DateTime | No | Fecha de creación (UTC) |

**Tipos de Hábito (`type`):**
- `EXERCISE` - Ejercicio (acelerómetro)
- `WALK` - Caminar (GPS)
- `DEMO` - Manual (tocar para completar)
- `READ` - Leer (cámara + ML)
- `READ_BOOK` - Leer libro
- `VITAMINS` - Tomar vitaminas
- `MEDITATE` - Meditar
- `JOURNALING` - Journaling
- `GYM` - Ir al gym
- `WATER` - Beber agua
- `COLD_SHOWER` - Ducha fría
- `ENGLISH` - Practicar inglés
- `CODING` - Practicar coding

---

### ScoreDto (Modelo de Datos de Puntaje)

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `id` | long | No | ID del puntaje (0 para nuevos) |
| `habitId` | long | **Sí** | ID del hábito relacionado |
| `habitTitle` | string? | No | Título del hábito (max 200 chars) |
| `points` | int | **Sí** | Puntos obtenidos (min: 0) |
| `date` | DateTime | No | Fecha del puntaje (UTC, default: ahora) |

---

## 🔄 FLUJO DE DATOS EN LA APP ANDROID

### 1. **Crear Hábito (POST)**
```
Usuario crea hábito → HabitRepository.createHabit()
  ↓
1. Guarda en SQLite local (HabitDatabaseHelperSync)
2. Si hay conexión → Envía POST /habits a la API
3. Si éxito → Actualiza server_id en SQLite
4. Si falla → Guarda en cola de operaciones pendientes
```

### 2. **Obtener Hábitos (GET)**
```
Usuario abre dashboard → HabitRepository.getAllHabits()
  ↓
1. Obtiene de SQLite local (inmediato)
2. Si hay conexión → Sincroniza con API en segundo plano
3. Actualiza datos locales con datos del servidor
4. Notifica cambios a la UI
```

### 3. **Actualizar Hábito (PUT)**
```
Usuario edita hábito → HabitRepository.updateHabit()
  ↓
1. Actualiza en SQLite local
2. Si hay conexión → Envía PUT /habits/{id} a la API
3. Si éxito → Marca como sincronizado
4. Si falla → Guarda en cola de operaciones pendientes
```

### 4. **Eliminar Hábito (DELETE)**
```
Usuario elimina hábito → HabitRepository.deleteHabit()
  ↓
1. Elimina de SQLite local
2. Si hay conexión → Envía DELETE /habits/{id} a la API
3. Si falla → Guarda en cola de operaciones pendientes
```

### 5. **Sincronización Offline → Online**
```
Conexión restaurada → SyncManager.syncAll()
  ↓
1. Obtiene hábitos no sincronizados de SQLite
2. Envía POST /habits/sync con lista de hábitos
3. Actualiza server_id en SQLite
4. Procesa operaciones pendientes (POST/PUT/DELETE)
```

---

## 🛠️ CONFIGURACIÓN EN ANDROID

### HabitApiClient.java
```java
BASE_URL = "http://10.0.2.2:5098/api/v1/"  // Emulador
BASE_URL = "http://192.168.x.x:5098/api/v1/"  // Dispositivo físico
```

### HabitApiService.java
- Define los endpoints usando anotaciones Retrofit
- Mapea automáticamente JSON ↔ Objetos Java usando Gson

### HabitRepository.java
- Abstrae el acceso a datos (local + remoto)
- Maneja sincronización automática
- Gestiona cola de operaciones pendientes

---

## 📝 EJEMPLOS DE REQUEST/RESPONSE

### Ejemplo 1: Crear Hábito de Ejercicio
**Request:**
```http
POST /api/v1/habits
Content-Type: application/json

{
  "title": "Ejercicio Matutino",
  "goal": "30 minutos de ejercicio",
  "category": "salud",
  "type": "EXERCISE",
  "points": 15,
  "targetValue": 30.0,
  "targetUnit": "minutos"
}
```

**Response:**
```http
HTTP/1.1 201 Created
Content-Type: application/json

{
  "id": 10,
  "title": "Ejercicio Matutino",
  "goal": "30 minutos de ejercicio",
  "category": "salud",
  "type": "EXERCISE",
  "completed": false,
  "points": 15,
  "targetValue": 30.0,
  "targetUnit": "minutos",
  "createdAt": "2025-01-11T14:30:00Z"
}
```

### Ejemplo 2: Obtener Hábitos con Filtros
**Request:**
```http
GET /api/v1/habits?type=EXERCISE&completed=false&page=1&pageSize=10
```

**Response:**
```http
HTTP/1.1 200 OK
X-Total-Count: 25
X-Page: 1
X-Page-Size: 10
X-Total-Pages: 3
Content-Type: application/json

{
  "success": true,
  "message": "Hábitos obtenidos correctamente",
  "habits": [...]
}
```

---

## ⚠️ CÓDIGOS DE RESPUESTA HTTP

| Código | Significado | Cuándo Ocurre |
|--------|-------------|---------------|
| **200 OK** | Éxito | GET, PUT, DELETE exitosos |
| **201 Created** | Creado | POST exitoso |
| **400 Bad Request** | Error de validación | Datos inválidos en request |
| **404 Not Found** | No encontrado | ID no existe |
| **500 Internal Server Error** | Error del servidor | Excepción en el servidor |

---

## 🔐 SEGURIDAD Y VALIDACIONES

### Validaciones en la API (.NET):
- `[Required]`: Campos obligatorios
- `[StringLength]`: Longitud máxima de strings
- `[Range]`: Valores mínimos/máximos
- Validación de existencia de hábito para scores

### Manejo de Errores en Android:
- Timeout de 30 segundos
- Reintentos automáticos (en desarrollo)
- Cola de operaciones pendientes para offline
- Logging de errores con Retrofit

---

## 📊 RESUMEN

**Datos que se envían (Android → API):**
- Objetos `Habit` completos (POST/PUT)
- Lista de `Habit` para sincronización (POST /sync)
- Objetos `Score` para puntajes (POST/PUT)

**Datos que se reciben (API → Android):**
- `HabitsResponse` con lista de hábitos (GET)
- `Habit` individual (GET/POST/PUT)
- `ScoreDto` para puntajes (GET/POST/PUT)
- Respuestas de éxito/error con mensajes

**Formato:** JSON (Content-Type: application/json)

**Autenticación:** Actualmente no implementada (puede agregarse JWT)

---

## 🚀 PRÓXIMOS PASOS

1. Implementar autenticación JWT
2. Agregar endpoints de Scores en Android
3. Implementar paginación en la UI
4. Agregar filtros en la UI
5. Mejorar manejo de errores con mensajes específicos

