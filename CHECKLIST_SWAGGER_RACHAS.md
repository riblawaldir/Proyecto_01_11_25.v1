# CHECKLIST - QUÉ VERIFICAR EN SWAGGER PARA RACHAS

## 🔍 ENDPOINTS A PROBAR EN SWAGGER

### 1. **GET /api/users/{id}/stats**
**¿Qué verificar?**
- ✅ ¿Existe este endpoint?
- ✅ ¿Qué estructura tiene la respuesta?
- ✅ ¿Incluye `currentStreak` y `longestStreak`?
- ✅ ¿Los valores de racha son correctos o siempre devuelven 0?

**Ejemplo de respuesta esperada:**
```json
{
  "totalHabits": 5,
  "totalPoints": 150,
  "currentStreak": 3,
  "longestStreak": 7
}
```

**Pasos:**
1. Obtener el ID de un usuario de prueba (puedes usar el ID de tu usuario actual)
2. Llamar al endpoint con ese ID
3. Verificar la estructura de la respuesta
4. Anotar si `currentStreak` y `longestStreak` están presentes
5. Anotar los valores que devuelve

---

### 2. **GET /api/users/by-email/{email}**
**¿Qué verificar?**
- ✅ ¿Existe este endpoint?
- ✅ ¿Qué estructura tiene la respuesta?
- ✅ ¿Incluye un objeto `stats` con rachas?
- ✅ ¿La estructura coincide con `UserStatsResponse` de Android?

**Ejemplo de respuesta esperada:**
```json
{
  "user": {
    "id": 1,
    "email": "usuario@ejemplo.com",
    "displayName": "Usuario"
  },
  "stats": {
    "totalHabits": 5,
    "totalPoints": 150,
    "currentStreak": 3,
    "longestStreak": 7
  }
}
```

**Pasos:**
1. Usar tu email de prueba
2. Llamar al endpoint
3. Verificar si devuelve `user` y `stats`
4. Verificar si `stats` incluye `currentStreak` y `longestStreak`
5. Anotar la estructura completa

---

### 3. **PUT /api/habits/{id}** o **POST /api/habits/{id}/complete**
**¿Qué verificar?**
- ✅ ¿Existe un endpoint para completar hábitos?
- ✅ ¿Qué campos acepta en el body?
- ✅ ¿Actualiza algún campo relacionado con rachas?
- ✅ ¿Hay algún campo `completed` o `isCompleted`?

**Pasos:**
1. Buscar endpoints relacionados con hábitos
2. Verificar si hay un endpoint específico para completar hábitos
3. Ver la documentación del endpoint (qué campos acepta)
4. Anotar si menciona algo sobre rachas o estadísticas

---

### 4. **GET /api/habits**
**¿Qué verificar?**
- ✅ ¿Los hábitos tienen un campo `completed` o `isCompleted`?
- ✅ ¿Hay información sobre cuándo se completó el hábito?
- ✅ ¿Hay un campo `lastCompletedDate`?

**Pasos:**
1. Llamar al endpoint para obtener hábitos
2. Ver la estructura de un hábito
3. Verificar si hay campos relacionados con completado
4. Anotar la estructura

---

### 5. **GET /api/scores** o **POST /api/scores**
**¿Qué verificar?**
- ✅ ¿Existe un endpoint para scores/puntos?
- ✅ ¿Los scores tienen fecha?
- ✅ ¿Se puede ver cuántos hábitos se completaron en un día?

**Pasos:**
1. Buscar endpoints relacionados con scores
2. Ver la estructura de un score
3. Verificar si incluye fecha de completado
4. Anotar la estructura

---

## 📊 ESTRUCTURA DE BASE DE DATOS

### Tabla `Users`
**¿Qué verificar?**
- ✅ ¿Existe la tabla `Users`?
- ✅ ¿Tiene los siguientes campos?
  - `CurrentStreak` (INTEGER)
  - `LongestStreak` (INTEGER)
  - `LastStreakDate` (DATETIME)
  - `DailyHabitsCompleted` (INTEGER)
  - `LastActivityDate` (DATETIME)

**Pasos:**
1. Si tienes acceso a la base de datos, verificar la estructura de la tabla `Users`
2. Anotar qué campos existen y cuáles faltan
3. Anotar los tipos de datos de cada campo

---

### Tabla `Habits`
**¿Qué verificar?**
- ✅ ¿Tiene un campo `Completed` o `IsCompleted`?
- ✅ ¿Tiene un campo `LastCompletedDate`?
- ✅ ¿Tiene un campo `UserId` para relacionar con usuarios?

**Pasos:**
1. Verificar la estructura de la tabla `Habits`
2. Anotar qué campos existen
3. Verificar si hay relación con `Users`

---

### Tabla `Scores`
**¿Qué verificar?**
- ✅ ¿Existe la tabla `Scores`?
- ✅ ¿Tiene un campo `Date` o `CreatedAt`?
- ✅ ¿Tiene un campo `UserId`?
- ✅ ¿Tiene un campo `HabitId`?

**Pasos:**
1. Verificar si existe la tabla `Scores`
2. Ver la estructura completa
3. Anotar los campos relacionados con fechas y usuarios

---

## 🧪 PRUEBAS ESPECÍFICAS

### Prueba 1: Verificar cálculo de racha
1. Completar 3 hábitos para un usuario
2. Llamar a `GET /api/users/{id}/stats`
3. Verificar si `currentStreak` cambió a 1 o más
4. Anotar el resultado

### Prueba 2: Verificar reset diario
1. Completar 3 hábitos hoy
2. Esperar al día siguiente (o simular cambio de fecha)
3. Llamar a `GET /api/users/{id}/stats`
4. Verificar si la racha se mantuvo o se reseteó
5. Anotar el resultado

### Prueba 3: Verificar estadísticas de amigos
1. Agregar un amigo en la app Android
2. Llamar a `GET /api/users/by-email/{email}` con el email del amigo
3. Verificar si devuelve las estadísticas correctas
4. Anotar el resultado

---

## 📝 INFORMACIÓN A RECOPILAR

Después de verificar en Swagger, proporcionar:

1. **Estructura de respuestas:**
   - Copiar y pegar ejemplos de respuestas JSON de los endpoints
   - Especialmente de `/users/{id}/stats` y `/users/by-email/{email}`

2. **Campos de base de datos:**
   - Lista de campos que existen en `Users`
   - Lista de campos que existen en `Habits`
   - Lista de campos que existen en `Scores`

3. **Endpoints disponibles:**
   - Lista completa de endpoints relacionados con usuarios, hábitos y estadísticas
   - Especialmente endpoints para completar hábitos

4. **Problemas encontrados:**
   - ¿Faltan campos en la base de datos?
   - ¿Los endpoints no devuelven rachas?
   - ¿Los valores de racha siempre son 0?

---

## 🎯 PRIORIDADES

**ALTA PRIORIDAD:**
1. Verificar si `/users/{id}/stats` devuelve rachas
2. Verificar si la tabla `Users` tiene campos de racha
3. Verificar si existe un endpoint para completar hábitos

**MEDIA PRIORIDAD:**
4. Verificar estructura de `/users/by-email/{email}`
5. Verificar si los scores tienen fechas

**BAJA PRIORIDAD:**
6. Verificar otros endpoints relacionados
7. Verificar estructura completa de todas las tablas

