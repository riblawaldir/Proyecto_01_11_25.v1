# ANÁLISIS DE SWAGGER - ENDPOINTS VISIBLES

## ✅ ENDPOINTS QUE SÍ EXISTEN (según las capturas)

### Sección "Users"
- `GET /api/v1/users` - Obtener todos los usuarios
- `POST /api/v1/users` - Crear usuario
- `GET /api/v1/users/{id}` - Obtener usuario por ID
- `PUT /api/v1/users/{id}` - Actualizar usuario
- `DELETE /api/v1/users/{id}` - Eliminar usuario

### Sección "Scores"
- `GET /api/v1/scores` - Obtener todos los scores
- `POST /api/v1/scores` - Crear score
- `GET /api/v1/scores/{id}` - Obtener score por ID
- `PUT /api/v1/scores/{id}` - Actualizar score
- `DELETE /api/v1/scores/{id}` - Eliminar score

### Sección "Habit"
- `GET /api/v1/habits` - Obtener todos los hábitos
- `POST /api/v1/habits` - Crear hábito
- `GET /api/v1/habits/{id}` - Obtener hábito por ID
- `PUT /api/v1/habits/{id}` - Actualizar hábito
- `DELETE /api/v1/habits/{id}` - Eliminar hábito
- `POST /api/v1/habits/sync` - Sincronizar hábitos

## ❌ ENDPOINTS QUE NO SE VEN (pero necesitamos)

### Endpoints de Estadísticas
- `GET /api/v1/users/{id}/stats` - **NO VISIBLE** ❌
- `GET /api/v1/users/by-email/{email}` - **NO VISIBLE** ❌

## 🔍 ACCIONES INMEDIATAS

### 1. Expandir el endpoint `GET /api/v1/users/{id}`
**¿Qué hacer?**
1. Haz clic en el endpoint `GET /api/v1/users/{id}` para expandirlo
2. Verifica qué campos devuelve en la respuesta
3. Anota si incluye campos como:
   - `currentStreak`
   - `longestStreak`
   - `dailyHabitsCompleted`
   - `totalPoints`
   - `totalHabits`

**¿Qué buscar?**
- Ver el esquema de respuesta (Response Schema)
- Ver ejemplos de respuesta
- Ver si hay parámetros adicionales

### 2. Buscar endpoints personalizados
**¿Qué hacer?**
1. Desplázate hacia abajo en Swagger
2. Busca secciones como:
   - "Statistics"
   - "UserStats"
   - "Stats"
   - O cualquier sección que no hayas visto aún

### 3. Probar `GET /api/v1/users/{id}` directamente
**¿Qué hacer?**
1. Expande el endpoint `GET /api/v1/users/{id}`
2. Haz clic en "Try it out"
3. Ingresa un ID de usuario válido (puedes usar el ID de tu usuario actual)
4. Haz clic en "Execute"
5. **Copia y pega la respuesta JSON completa**

**¿Qué verificar?**
- ¿La respuesta incluye campos de racha?
- ¿Qué estructura tiene la respuesta?
- ¿Hay algún campo relacionado con estadísticas?

### 4. Verificar estructura de la tabla Users
**Si tienes acceso a la base de datos:**
1. Verifica la estructura de la tabla `Users`
2. Busca estos campos:
   - `CurrentStreak`
   - `LongestStreak`
   - `LastStreakDate`
   - `DailyHabitsCompleted`
   - `LastActivityDate`

## 📋 INFORMACIÓN ESPECÍFICA A RECOPILAR

### De `GET /api/v1/users/{id}`:
1. **Estructura de respuesta completa** (copiar JSON)
2. **Campos que devuelve** (lista completa)
3. **Si incluye rachas o estadísticas**

### De la base de datos:
1. **Campos de la tabla Users** (lista completa)
2. **Tipos de datos de cada campo**
3. **Si existen campos de racha**

## 🎯 CONCLUSIÓN TEMPORAL

Basándome en las capturas:
- ✅ Los endpoints básicos de Users existen
- ❌ Los endpoints específicos de estadísticas NO son visibles
- ⚠️ **Probablemente necesitamos CREAR estos endpoints**

## 🔧 SIGUIENTE PASO

**OPCIÓN A:** Si `GET /api/v1/users/{id}` devuelve estadísticas:
- Modificar el endpoint para incluir rachas si faltan
- Crear endpoint `/api/v1/users/{id}/stats` si no existe

**OPCIÓN B:** Si `GET /api/v1/users/{id}` NO devuelve estadísticas:
- Crear los endpoints desde cero
- Implementar la lógica de cálculo de rachas
- Agregar campos a la tabla Users si faltan

