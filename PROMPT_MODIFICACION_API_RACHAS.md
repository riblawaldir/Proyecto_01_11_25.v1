# PROMPT PARA MODIFICAR LA API - SISTEMA DE RACHAS

## CONTEXTO
La aplicación Android calcula las rachas localmente en SQLite cuando un usuario completa hábitos. Sin embargo, la API también necesita calcular y almacenar las rachas para que:
1. Los endpoints de estadísticas (`/users/{id}/stats` y `/users/by-email/{email}`) devuelvan las rachas correctas
2. Las estadísticas de amigos se muestren correctamente
3. Las rachas se sincronicen entre dispositivos

## PROBLEMA ACTUAL
- La aplicación Android calcula rachas localmente pero no las sincroniza con la API
- La API probablemente no está calculando las rachas basándose en los hábitos completados
- Los endpoints de estadísticas pueden estar devolviendo rachas incorrectas o en 0

## REQUERIMIENTOS

### 1. TABLA DE USUARIOS - Campos necesarios
La tabla `Users` debe tener los siguientes campos para el sistema de rachas:
- `CurrentStreak` (INTEGER): Racha actual del usuario (días consecutivos)
- `LongestStreak` (INTEGER): Racha más larga alcanzada
- `LastStreakDate` (DATETIME): Fecha de la última vez que se mantuvo la racha
- `DailyHabitsCompleted` (INTEGER): Número de hábitos completados hoy
- `LastActivityDate` (DATETIME): Fecha de la última actividad (para resetear contador diario)

### 2. LÓGICA DE RACHAS
La API debe implementar la misma lógica que la aplicación Android:

**Reglas de racha:**
- Una racha se activa cuando un usuario completa **3 o más hábitos en un día**
- La racha se mantiene si el usuario completa 3+ hábitos al día siguiente
- La racha se resetea a 0 si el usuario no completa 3+ hábitos en un día
- Si la última actividad fue hace más de un día, la racha se resetea

**Cálculo de racha:**
1. Al completar un hábito, incrementar `DailyHabitsCompleted`
2. Si `DailyHabitsCompleted >= 3`:
   - Si `CurrentStreak == 0`: Iniciar racha (`CurrentStreak = 1`)
   - Si `CurrentStreak > 0`: Mantener racha (actualizar `LastStreakDate`)
3. Al inicio de un nuevo día (después de 00:00):
   - Si `LastActivityDate < hoy`:
     - Si fue ayer y `DailyHabitsCompleted >= 3`: Incrementar `CurrentStreak`
     - Si fue ayer y `DailyHabitsCompleted < 3`: Resetear `CurrentStreak = 0`
     - Si fue hace más de un día: Resetear `CurrentStreak = 0`
   - Resetear `DailyHabitsCompleted = 0`

### 3. ENDPOINTS A MODIFICAR/CREAR

#### A. Actualizar racha al completar hábito
**Endpoint:** `POST /api/habits/{id}/complete` o modificar `PUT /api/habits/{id}`
- Cuando se marca un hábito como completado, actualizar `DailyHabitsCompleted`
- Si `DailyHabitsCompleted >= 3`, activar o mantener la racha
- Actualizar `LastActivityDate` a la fecha actual

#### B. Endpoint de estadísticas (YA EXISTE - VERIFICAR)
**Endpoint:** `GET /api/users/{id}/stats`
- Debe devolver:
  ```json
  {
    "totalHabits": 5,
    "totalPoints": 150,
    "currentStreak": 3,
    "longestStreak": 7
  }
  ```
- Debe calcular `currentStreak` y `longestStreak` desde la base de datos

#### C. Endpoint de usuario por email (YA EXISTE - VERIFICAR)
**Endpoint:** `GET /api/users/by-email/{email}`
- Debe devolver el usuario con sus estadísticas incluyendo rachas

#### D. Endpoint para sincronizar racha (NUEVO - OPCIONAL)
**Endpoint:** `PUT /api/users/{id}/streak`
- Permite sincronizar la racha desde la aplicación Android
- Body:
  ```json
  {
    "currentStreak": 3,
    "dailyHabitsCompleted": 3,
    "lastActivityDate": "2024-01-15T00:00:00Z"
  }
  ```

### 4. MIGRACIÓN DE BASE DE DATOS
Si los campos no existen, crear una migración para agregarlos:
```sql
ALTER TABLE Users ADD COLUMN CurrentStreak INT DEFAULT 0;
ALTER TABLE Users ADD COLUMN LongestStreak INT DEFAULT 0;
ALTER TABLE Users ADD COLUMN LastStreakDate DATETIME NULL;
ALTER TABLE Users ADD COLUMN DailyHabitsCompleted INT DEFAULT 0;
ALTER TABLE Users ADD COLUMN LastActivityDate DATETIME NULL;
```

### 5. SERVICIO DE CÁLCULO DE RACHAS
Crear un servicio `IStreakService` que:
- Calcule las rachas basándose en los hábitos completados
- Maneje el reset diario de contadores
- Actualice `LongestStreak` cuando `CurrentStreak` supere el valor actual
- Se ejecute automáticamente al completar hábitos

## VERIFICACIÓN EN SWAGGER

Antes de hacer cambios, verificar en Swagger:

1. **GET /api/users/{id}/stats**
   - Probar con un usuario que tenga hábitos completados
   - Verificar que `currentStreak` y `longestStreak` se devuelven
   - Verificar que los valores son correctos

2. **GET /api/users/by-email/{email}**
   - Probar con un email de usuario existente
   - Verificar que devuelve el objeto `stats` con rachas
   - Verificar la estructura de la respuesta

3. **PUT /api/habits/{id}** o **POST /api/habits/{id}/complete**
   - Verificar si existe un endpoint para completar hábitos
   - Ver si actualiza algún campo relacionado con rachas

4. **Estructura de la tabla Users**
   - Verificar en la base de datos si existen los campos de racha
   - Verificar los tipos de datos

## IMPLEMENTACIÓN SUGERIDA

1. **Crear servicio `StreakService.cs`:**
   ```csharp
   public interface IStreakService
   {
       Task UpdateStreakOnHabitCompletion(int userId, int habitId);
       Task<int> GetCurrentStreak(int userId);
       Task<int> GetLongestStreak(int userId);
       Task ResetDailyCounterIfNeeded(int userId);
   }
   ```

2. **Modificar `HabitController.cs`:**
   - Al completar un hábito, llamar a `IStreakService.UpdateStreakOnHabitCompletion()`

3. **Modificar `UserController.cs`:**
   - En `GetUserStats()`, calcular rachas desde la base de datos
   - En `GetUserByEmailWithStats()`, incluir estadísticas con rachas

4. **Crear migración de base de datos** para agregar campos si no existen

## NOTAS IMPORTANTES
- La lógica de rachas debe ser idéntica a la de la aplicación Android
- Las rachas se calculan basándose en hábitos completados, no en días consecutivos de login
- Un día se considera "completado" si el usuario completa 3+ hábitos ese día
- La racha se mantiene si se completan 3+ hábitos al día siguiente

