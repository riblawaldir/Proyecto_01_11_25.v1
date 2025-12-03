# ENDPOINTS A PROBAR EN SWAGGER

## 🎯 OBJETIVO
Probar endpoints existentes para entender la estructura de datos y así poder crear correctamente los endpoints de estadísticas y rachas.

---

## 1. **GET /api/v1/users/{id}** ⭐ PRIORIDAD ALTA

**¿Por qué?**
- Necesitamos ver qué estructura tiene un usuario
- Ver si ya incluye algún campo de estadísticas o rachas
- Entender el formato de respuesta

**Pasos:**
1. Expande el endpoint `GET /api/v1/users/{id}`
2. Haz clic en "Try it out"
3. Ingresa un ID de usuario válido (puedes usar el ID de tu usuario actual)
4. Haz clic en "Execute"
5. **Copia y pega la respuesta JSON completa aquí**

**¿Qué buscar?**
- Estructura completa del objeto User
- Campos que devuelve
- Si hay algún campo relacionado con estadísticas, rachas, o puntos

---

## 2. **GET /api/v1/habits/{id}** ⭐ PRIORIDAD ALTA

**¿Por qué?**
- Necesitamos ver cómo se estructura un hábito
- Ver si tiene campo `completed` o `isCompleted`
- Ver si tiene fecha de completado
- Entender la relación con usuarios

**Pasos:**
1. Expande el endpoint `GET /api/v1/habits/{id}`
2. Haz clic en "Try it out"
3. Ingresa un ID de hábito válido
4. Haz clic en "Execute"
5. **Copia y pega la respuesta JSON completa aquí**

**¿Qué buscar?**
- Campo `userId` o `UserId` (para relacionar con usuario)
- Campo `completed`, `isCompleted`, o similar
- Campo `lastCompletedDate` o similar
- Campo `points` o `Points`

---

## 3. **GET /api/v1/scores** ⭐ PRIORIDAD ALTA

**¿Por qué?**
- Necesitamos ver cómo se estructuran los scores
- Ver si tienen fecha de creación
- Ver cómo se relacionan con usuarios y hábitos
- Entender el formato de datos

**Pasos:**
1. Expande el endpoint `GET /api/v1/scores`
2. Haz clic en "Try it out"
3. Haz clic en "Execute"
4. **Copia y pega la respuesta JSON completa aquí** (o al menos los primeros 2-3 elementos del array)

**¿Qué buscar?**
- Campo `userId` o `UserId`
- Campo `habitId` o `HabitId`
- Campo `date`, `createdAt`, `Date`, o similar
- Campo `points` o `Points`
- Estructura completa del objeto Score

---

## 4. **POST /api/v1/scores** (Ver esquema de request) ⭐ PRIORIDAD MEDIA

**¿Por qué?**
- Ver qué campos se envían al crear un score
- Entender el formato de datos que espera la API

**Pasos:**
1. Expande el endpoint `POST /api/v1/scores`
2. **NO necesitas ejecutarlo**, solo ver el esquema de request
3. **Copia el esquema de "Request body" o "Parameters"**

**¿Qué buscar?**
- Campos requeridos
- Tipos de datos
- Ejemplo de JSON

---

## 5. **PUT /api/v1/habits/{id}** (Ver esquema de request) ⭐ PRIORIDAD MEDIA

**¿Por qué?**
- Ver qué campos se pueden actualizar en un hábito
- Ver si hay campo para marcar como completado
- Entender cómo se actualiza un hábito

**Pasos:**
1. Expande el endpoint `PUT /api/v1/habits/{id}`
2. **NO necesitas ejecutarlo**, solo ver el esquema de request
3. **Copia el esquema de "Request body"**

**¿Qué buscar?**
- Campo `completed`, `isCompleted`, o similar
- Campo `lastCompletedDate` o similar
- Otros campos relacionados con completado

---

## 6. **GET /api/v1/users** (Opcional) ⭐ PRIORIDAD BAJA

**¿Por qué?**
- Ver la lista completa de usuarios
- Ver si hay algún filtro o parámetro especial

**Pasos:**
1. Expande el endpoint `GET /api/v1/users`
2. Haz clic en "Try it out"
3. Haz clic en "Execute"
4. **Copia y pega la respuesta JSON** (o al menos el primer elemento del array)

---

## 📋 FORMATO PARA COMPARTIR RESULTADOS

Por favor, comparte los resultados en este formato:

```markdown
## 1. GET /api/v1/users/{id}
**ID usado:** [tu ID de usuario]
**Respuesta:**
```json
[pegar JSON completo aquí]
```

## 2. GET /api/v1/habits/{id}
**ID usado:** [ID del hábito]
**Respuesta:**
```json
[pegar JSON completo aquí]
```

## 3. GET /api/v1/scores
**Respuesta:**
```json
[pegar JSON completo aquí]
```

## 4. POST /api/v1/scores (Esquema)
[pegar esquema o ejemplo de request body]

## 5. PUT /api/v1/habits/{id} (Esquema)
[pegar esquema o ejemplo de request body]
```

---

## 🎯 PRIORIDADES

**HAZ PRIMERO:**
1. ✅ `GET /api/v1/users/{id}` - **MÁS IMPORTANTE**
2. ✅ `GET /api/v1/habits/{id}` - **MUY IMPORTANTE**
3. ✅ `GET /api/v1/scores` - **MUY IMPORTANTE**

**DESPUÉS:**
4. `POST /api/v1/scores` (solo esquema)
5. `PUT /api/v1/habits/{id}` (solo esquema)

**OPCIONAL:**
6. `GET /api/v1/users` (si tienes tiempo)

---

## 💡 NOTAS

- **No necesitas autenticarte** si Swagger permite probar sin autenticación
- Si pide autenticación, usa el botón "Authorize" en Swagger
- Si algún endpoint falla, anota el error
- Si no puedes probar algún endpoint, solo comparte los que sí puedas

