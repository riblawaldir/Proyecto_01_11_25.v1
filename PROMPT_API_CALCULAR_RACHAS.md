# PROMPT PARA IMPLEMENTAR CÁLCULO DE RACHAS EN LA API

## PROBLEMA ACTUAL
El endpoint `GET /api/v1/users/{id}/stats` devuelve:
- `totalHabits`: ✅ Correcto (se calcula)
- `totalPoints`: ✅ Correcto (se calcula sumando scores)
- `currentStreak`: ❌ Siempre devuelve 0 (NO se calcula)
- `longestStreak`: ❌ Siempre devuelve 0 (NO se calcula)

## REQUERIMIENTO
Implementar la lógica de cálculo de rachas en el endpoint `GET /api/v1/users/{id}/stats` (y también en `GET /api/v1/users/by-email/{email}`).

## LÓGICA DE RACHAS

### Reglas:
1. **Una racha se activa** cuando un usuario completa **3 o más hábitos en un día**
2. **La racha se mantiene** si el usuario completa 3+ hábitos al día siguiente
3. **La racha se resetea** si el usuario NO completa 3+ hábitos en un día
4. **La racha se resetea** si pasan más de 1 día sin completar 3+ hábitos

### Cálculo de `currentStreak`:
1. Obtener todos los scores del usuario donde `countsForStreak = true`
2. Agrupar los scores por día (usando el campo `date`)
3. Contar cuántos hábitos únicos se completaron cada día (contar `habitId` únicos por día)
4. Identificar días consecutivos donde se completaron 3+ hábitos
5. Contar cuántos días consecutivos hay desde hoy hacia atrás

### Cálculo de `longestStreak`:
1. Mismo proceso que `currentStreak`
2. Pero buscar la secuencia más larga de días consecutivos con 3+ hábitos en toda la historia del usuario
3. Guardar este valor en la tabla `Users` (campo `LongestStreak`) para no recalcularlo cada vez

## IMPLEMENTACIÓN SUGERIDA

### 1. Modificar el endpoint `GET /api/v1/users/{id}/stats`

```csharp
[HttpGet("{id}/stats")]
public async Task<ActionResult<UserStats>> GetUserStats(long id)
{
    // Obtener todos los scores del usuario donde countsForStreak = true
    var scores = await _context.Scores
        .Where(s => s.UserId == id && s.CountsForStreak == true)
        .OrderBy(s => s.Date)
        .ToListAsync();

    // Calcular currentStreak
    int currentStreak = CalculateCurrentStreak(scores);
    
    // Calcular longestStreak (o obtenerlo de la BD si ya está guardado)
    int longestStreak = await GetOrCalculateLongestStreak(id, scores);

    // Calcular totalHabits y totalPoints (ya lo tienes)
    int totalHabits = await _context.Habits.CountAsync(h => h.UserId == id);
    int totalPoints = scores.Sum(s => s.Points);

    return Ok(new UserStats
    {
        TotalHabits = totalHabits,
        TotalPoints = totalPoints,
        CurrentStreak = currentStreak,
        LongestStreak = longestStreak
    });
}

private int CalculateCurrentStreak(List<Score> scores)
{
    if (scores == null || scores.Count == 0)
        return 0;

    // Agrupar scores por día
    var scoresByDay = scores
        .GroupBy(s => s.Date.Date)
        .OrderByDescending(g => g.Key)
        .ToList();

    int streak = 0;
    DateTime? lastDate = null;

    foreach (var dayGroup in scoresByDay)
    {
        DateTime day = dayGroup.Key;
        int uniqueHabits = dayGroup.Select(s => s.HabitId).Distinct().Count();

        // Si completó 3+ hábitos este día
        if (uniqueHabits >= 3)
        {
            if (lastDate == null)
            {
                // Primer día de la racha
                streak = 1;
                lastDate = day;
            }
            else
            {
                // Verificar si es día consecutivo
                int daysDifference = (lastDate.Value.Date - day.Date).Days;
                
                if (daysDifference == 1)
                {
                    // Día consecutivo, incrementar racha
                    streak++;
                    lastDate = day;
                }
                else if (daysDifference > 1)
                {
                    // Hay un gap, la racha se rompió
                    break;
                }
                // Si daysDifference == 0, es el mismo día, no hacer nada
            }
        }
        else
        {
            // No completó 3+ hábitos, la racha se rompió
            if (lastDate != null && day.Date < lastDate.Value.Date)
                break;
        }
    }

    return streak;
}

private async Task<int> GetOrCalculateLongestStreak(long userId, List<Score> scores)
{
    // Intentar obtener de la BD primero
    var user = await _context.Users.FindAsync(userId);
    if (user != null && user.LongestStreak > 0)
    {
        // Recalcular solo si hay nuevos scores desde la última vez
        // (optimización: puedes agregar un campo LastStreakCalculationDate)
        int calculatedLongest = CalculateLongestStreak(scores);
        
        if (calculatedLongest > user.LongestStreak)
        {
            user.LongestStreak = calculatedLongest;
            await _context.SaveChangesAsync();
        }
        
        return user.LongestStreak;
    }
    
    // Si no existe, calcular y guardar
    int longest = CalculateLongestStreak(scores);
    if (user != null)
    {
        user.LongestStreak = longest;
        await _context.SaveChangesAsync();
    }
    
    return longest;
}

private int CalculateLongestStreak(List<Score> scores)
{
    if (scores == null || scores.Count == 0)
        return 0;

    // Agrupar scores por día
    var scoresByDay = scores
        .GroupBy(s => s.Date.Date)
        .OrderBy(g => g.Key)
        .ToList();

    int maxStreak = 0;
    int currentStreak = 0;
    DateTime? lastDate = null;

    foreach (var dayGroup in scoresByDay)
    {
        DateTime day = dayGroup.Key;
        int uniqueHabits = dayGroup.Select(s => s.HabitId).Distinct().Count();

        if (uniqueHabits >= 3)
        {
            if (lastDate == null)
            {
                currentStreak = 1;
                lastDate = day;
            }
            else
            {
                int daysDifference = (day.Date - lastDate.Value.Date).Days;
                
                if (daysDifference == 1)
                {
                    // Día consecutivo
                    currentStreak++;
                    lastDate = day;
                }
                else if (daysDifference > 1)
                {
                    // Gap detectado, reiniciar racha
                    maxStreak = Math.Max(maxStreak, currentStreak);
                    currentStreak = 1;
                    lastDate = day;
                }
            }
        }
        else
        {
            // No completó 3+ hábitos, reiniciar racha
            maxStreak = Math.Max(maxStreak, currentStreak);
            currentStreak = 0;
            lastDate = null;
        }
    }

    // Verificar la última racha
    maxStreak = Math.Max(maxStreak, currentStreak);

    return maxStreak;
}
```

### 2. Agregar campo `LongestStreak` a la tabla `Users` (si no existe)

```sql
ALTER TABLE Users ADD COLUMN LongestStreak INT DEFAULT 0;
```

### 3. Modificar también `GET /api/v1/users/by-email/{email}`

Aplicar la misma lógica de cálculo de rachas en este endpoint.

## NOTAS IMPORTANTES

1. **Usar `countsForStreak`**: Solo contar scores donde `CountsForStreak == true`
2. **Contar hábitos únicos por día**: Un usuario puede completar el mismo hábito múltiples veces en un día, pero solo cuenta como 1 hábito para la racha
3. **Días consecutivos**: Un día se considera consecutivo si es exactamente 1 día después del anterior
4. **Optimización**: Guardar `LongestStreak` en la BD para no recalcularlo cada vez
5. **Timezone**: Asegurarse de usar la misma zona horaria para agrupar por día

## PRUEBAS

Después de implementar, probar con:
- Usuario que completó 3 hábitos hoy → `currentStreak` debe ser 1
- Usuario que completó 3 hábitos ayer y hoy → `currentStreak` debe ser 2
- Usuario que completó 3 hábitos hace 3 días consecutivos → `currentStreak` debe ser 3
- Usuario que completó 3 hábitos ayer pero solo 2 hoy → `currentStreak` debe ser 0

