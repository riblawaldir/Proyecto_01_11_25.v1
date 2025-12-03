# 📝 EJEMPLOS DE JSON PARA CREAR HÁBITOS

Este documento contiene ejemplos de JSON para crear diferentes tipos de hábitos usando el endpoint `POST /api/v1/habits`.

---

## 📋 FORMATO BASE

Todos los hábitos requieren estos campos mínimos:
- `userId` (long) - **NOTA**: Este campo es ignorado por la API, el userId se obtiene del token JWT
- `title` (string, requerido) - Título del hábito
- `type` (string, requerido) - Tipo de hábito

**Campos opcionales comunes:**
- `goal` (string) - Meta del hábito
- `category` (string) - Categoría
- `points` (int) - Puntos por completar (default: 10)
- `targetValue` (double) - Valor objetivo
- `targetUnit` (string) - Unidad del objetivo
- `completed` (bool) - Estado completado (default: false)

---

## 🏃 EJEMPLO 1: HÁBITO DE EJERCICIO (EXERCISE)

```json
{
  "id": 0,
  "userId": 0,
  "title": "Ejercicio Matutino",
  "goal": "Hacer ejercicio todos los días para mantenerme en forma",
  "category": "Salud",
  "type": "EXERCISE",
  "measurementType": "time",
  "trackingMethod": "sensor",
  "completed": false,
  "points": 15,
  "targetValue": 30,
  "targetUnit": "minutos",
  "currentValue": 0,
  "lastCompletionDate": null,
  "pagesPerDay": null,
  "reminderTimes": "08:00",
  "durationMinutes": 30,
  "dndMode": false,
  "musicId": null,
  "journalEnabled": false,
  "gymDays": null,
  "waterGoalGlasses": null,
  "oneClickComplete": false,
  "englishMode": null,
  "codingMode": null,
  "habitIcon": "ic_habit_exercise"
}
```

---

## 🚶 EJEMPLO 2: CAMINAR (WALK)

```json
{
  "id": 0,
  "userId": 0,
  "title": "Caminar 10,000 pasos",
  "goal": "Caminar al menos 10,000 pasos diarios",
  "category": "Salud",
  "type": "WALK",
  "measurementType": "distance",
  "trackingMethod": "sensor",
  "completed": false,
  "points": 20,
  "targetValue": 10000,
  "targetUnit": "pasos",
  "currentValue": 0,
  "lastCompletionDate": null,
  "pagesPerDay": null,
  "reminderTimes": "07:00,18:00",
  "durationMinutes": null,
  "dndMode": false,
  "musicId": null,
  "journalEnabled": false,
  "gymDays": null,
  "waterGoalGlasses": null,
  "oneClickComplete": false,
  "englishMode": null,
  "codingMode": null,
  "habitIcon": "ic_habit_walk"
}
```

---

## 📚 EJEMPLO 3: LEER LIBRO (READ_BOOK)

```json
{
  "id": 0,
  "userId": 0,
  "title": "Leer 20 páginas diarias",
  "goal": "Leer al menos 20 páginas de un libro cada día",
  "category": "Educación",
  "type": "READ_BOOK",
  "measurementType": "quantity",
  "trackingMethod": "manual",
  "completed": false,
  "points": 10,
  "targetValue": 20,
  "targetUnit": "páginas",
  "currentValue": 0,
  "lastCompletionDate": null,
  "pagesPerDay": 20,
  "reminderTimes": "21:00",
  "durationMinutes": null,
  "dndMode": false,
  "musicId": null,
  "journalEnabled": true,
  "gymDays": null,
  "waterGoalGlasses": null,
  "oneClickComplete": false,
  "englishMode": null,
  "codingMode": null,
  "habitIcon": "ic_habit_book"
}
```

---

## 💊 EJEMPLO 4: TOMAR VITAMINAS (VITAMINS)

```json
{
  "id": 0,
  "userId": 0,
  "title": "Tomar vitaminas",
  "goal": "Tomar mis vitaminas todas las mañanas",
  "category": "Salud",
  "type": "VITAMINS",
  "measurementType": "custom",
  "trackingMethod": "manual",
  "completed": false,
  "points": 5,
  "targetValue": 1,
  "targetUnit": "vez",
  "currentValue": 0,
  "lastCompletionDate": null,
  "pagesPerDay": null,
  "reminderTimes": "08:00",
  "durationMinutes": null,
  "dndMode": false,
  "musicId": null,
  "journalEnabled": false,
  "gymDays": null,
  "waterGoalGlasses": null,
  "oneClickComplete": true,
  "englishMode": null,
  "codingMode": null,
  "habitIcon": "ic_habit_vitamins"
}
```

---

## 🧘 EJEMPLO 5: MEDITAR (MEDITATE)

```json
{
  "id": 0,
  "userId": 0,
  "title": "Meditación diaria",
  "goal": "Meditar 15 minutos cada día para reducir el estrés",
  "category": "Bienestar",
  "type": "MEDITATE",
  "measurementType": "time",
  "trackingMethod": "manual",
  "completed": false,
  "points": 15,
  "targetValue": 15,
  "targetUnit": "minutos",
  "currentValue": 0,
  "lastCompletionDate": null,
  "pagesPerDay": null,
  "reminderTimes": "06:00",
  "durationMinutes": 15,
  "dndMode": true,
  "musicId": 1,
  "journalEnabled": false,
  "gymDays": null,
  "waterGoalGlasses": null,
  "oneClickComplete": false,
  "englishMode": null,
  "codingMode": null,
  "habitIcon": "ic_habit_meditate"
}
```

---

## 📔 EJEMPLO 6: JOURNALING (JOURNALING)

```json
{
  "id": 0,
  "userId": 0,
  "title": "Escribir en mi diario",
  "goal": "Escribir en mi diario todas las noches antes de dormir",
  "category": "Bienestar",
  "type": "JOURNALING",
  "measurementType": "custom",
  "trackingMethod": "manual",
  "completed": false,
  "points": 10,
  "targetValue": 1,
  "targetUnit": "entrada",
  "currentValue": 0,
  "lastCompletionDate": null,
  "pagesPerDay": null,
  "reminderTimes": "22:00",
  "durationMinutes": null,
  "dndMode": false,
  "musicId": null,
  "journalEnabled": true,
  "gymDays": null,
  "waterGoalGlasses": null,
  "oneClickComplete": false,
  "englishMode": null,
  "codingMode": null,
  "habitIcon": "ic_habit_journal"
}
```

---

## 💪 EJEMPLO 7: IR AL GYM (GYM)

```json
{
  "id": 0,
  "userId": 0,
  "title": "Ir al gimnasio",
  "goal": "Ir al gimnasio 3 veces por semana",
  "category": "Salud",
  "type": "GYM",
  "measurementType": "custom",
  "trackingMethod": "manual",
  "completed": false,
  "points": 25,
  "targetValue": 3,
  "targetUnit": "veces por semana",
  "currentValue": 0,
  "lastCompletionDate": null,
  "pagesPerDay": null,
  "reminderTimes": "18:00",
  "durationMinutes": null,
  "dndMode": false,
  "musicId": null,
  "journalEnabled": false,
  "gymDays": "[\"lunes\", \"miércoles\", \"viernes\"]",
  "waterGoalGlasses": null,
  "oneClickComplete": false,
  "englishMode": null,
  "codingMode": null,
  "habitIcon": "ic_habit_gym"
}
```

---

## 💧 EJEMPLO 8: BEBER AGUA (WATER)

```json
{
  "id": 0,
  "userId": 0,
  "title": "Beber 8 vasos de agua",
  "goal": "Beber al menos 8 vasos de agua al día",
  "category": "Salud",
  "type": "WATER",
  "measurementType": "quantity",
  "trackingMethod": "manual",
  "completed": false,
  "points": 5,
  "targetValue": 8,
  "targetUnit": "vasos",
  "currentValue": 0,
  "lastCompletionDate": null,
  "pagesPerDay": null,
  "reminderTimes": "08:00,12:00,16:00,20:00",
  "durationMinutes": null,
  "dndMode": false,
  "musicId": null,
  "journalEnabled": false,
  "gymDays": null,
  "waterGoalGlasses": 8,
  "oneClickComplete": false,
  "englishMode": null,
  "codingMode": null,
  "habitIcon": "ic_habit_water"
}
```

---

## 🚿 EJEMPLO 9: DUCHA FRÍA (COLD_SHOWER)

```json
{
  "id": 0,
  "userId": 0,
  "title": "Ducha fría matutina",
  "goal": "Tomar una ducha fría todas las mañanas para aumentar la energía",
  "category": "Salud",
  "type": "COLD_SHOWER",
  "measurementType": "custom",
  "trackingMethod": "manual",
  "completed": false,
  "points": 10,
  "targetValue": 1,
  "targetUnit": "vez",
  "currentValue": 0,
  "lastCompletionDate": null,
  "pagesPerDay": null,
  "reminderTimes": "07:00",
  "durationMinutes": null,
  "dndMode": false,
  "musicId": null,
  "journalEnabled": false,
  "gymDays": null,
  "waterGoalGlasses": null,
  "oneClickComplete": true,
  "englishMode": null,
  "codingMode": null,
  "habitIcon": "ic_habit_shower"
}
```

---

## 🌍 EJEMPLO 10: PRACTICAR INGLÉS (ENGLISH)

```json
{
  "id": 0,
  "userId": 0,
  "title": "Practicar inglés",
  "goal": "Practicar inglés 30 minutos diarios",
  "category": "Educación",
  "type": "ENGLISH",
  "measurementType": "time",
  "trackingMethod": "manual",
  "completed": false,
  "points": 15,
  "targetValue": 30,
  "targetUnit": "minutos",
  "currentValue": 0,
  "lastCompletionDate": null,
  "pagesPerDay": null,
  "reminderTimes": "19:00",
  "durationMinutes": 30,
  "dndMode": false,
  "musicId": null,
  "journalEnabled": false,
  "gymDays": null,
  "waterGoalGlasses": null,
  "oneClickComplete": false,
  "englishMode": true,
  "codingMode": null,
  "habitIcon": "ic_habit_english"
}
```

---

## 💻 EJEMPLO 11: PRACTICAR CODING (CODING)

```json
{
  "id": 0,
  "userId": 0,
  "title": "Practicar programación",
  "goal": "Practicar programación 1 hora diaria",
  "category": "Educación",
  "type": "CODING",
  "measurementType": "time",
  "trackingMethod": "manual",
  "completed": false,
  "points": 20,
  "targetValue": 60,
  "targetUnit": "minutos",
  "currentValue": 0,
  "lastCompletionDate": null,
  "pagesPerDay": null,
  "reminderTimes": "20:00",
  "durationMinutes": 60,
  "dndMode": false,
  "musicId": null,
  "journalEnabled": false,
  "gymDays": null,
  "waterGoalGlasses": null,
  "oneClickComplete": false,
  "englishMode": null,
  "codingMode": true,
  "habitIcon": "ic_habit_coding"
}
```

---

## 📖 EJEMPLO 12: LEER CON CÁMARA (READ)

```json
{
  "id": 0,
  "userId": 0,
  "title": "Leer con reconocimiento de texto",
  "goal": "Leer usando la cámara para detectar páginas de libro",
  "category": "Educación",
  "type": "READ",
  "measurementType": "quantity",
  "trackingMethod": "camera",
  "completed": false,
  "points": 10,
  "targetValue": 1,
  "targetUnit": "página",
  "currentValue": 0,
  "lastCompletionDate": null,
  "pagesPerDay": null,
  "reminderTimes": "21:00",
  "durationMinutes": null,
  "dndMode": false,
  "musicId": null,
  "journalEnabled": true,
  "gymDays": null,
  "waterGoalGlasses": null,
  "oneClickComplete": false,
  "englishMode": null,
  "codingMode": null,
  "habitIcon": "ic_habit_read"
}
```

---

## 🎯 EJEMPLO 13: HÁBITO SIMPLE (DEMO)

```json
{
  "id": 0,
  "userId": 0,
  "title": "Hábito simple",
  "goal": "Completar este hábito tocando el botón",
  "category": "General",
  "type": "DEMO",
  "measurementType": "custom",
  "trackingMethod": "manual",
  "completed": false,
  "points": 5,
  "targetValue": 1,
  "targetUnit": "vez",
  "currentValue": 0,
  "lastCompletionDate": null,
  "pagesPerDay": null,
  "reminderTimes": null,
  "durationMinutes": null,
  "dndMode": null,
  "musicId": null,
  "journalEnabled": null,
  "gymDays": null,
  "waterGoalGlasses": null,
  "oneClickComplete": true,
  "englishMode": null,
  "codingMode": null,
  "habitIcon": null
}
```

---

## 📌 NOTAS IMPORTANTES

### Campos Requeridos
- `title` - **OBLIGATORIO**
- `type` - **OBLIGATORIO** (debe ser uno de los tipos válidos)

### Campos que la API Ignora
- `id` - Siempre se envía como `0`, la API lo genera automáticamente
- `userId` - **La API ignora este campo** y usa el userId del token JWT

### Tipos de Hábito Válidos
- `EXERCISE`
- `WALK`
- `READ`
- `READ_BOOK`
- `VITAMINS`
- `MEDITATE`
- `JOURNALING`
- `GYM`
- `WATER`
- `COLD_SHOWER`
- `ENGLISH`
- `CODING`
- `DEMO`

### Formato de ReminderTimes
- Puede ser un string simple: `"08:00"`
- O múltiples horarios separados por coma: `"08:00,12:00,18:00"`

### Formato de GymDays
- Debe ser un JSON string: `"[\"lunes\", \"miércoles\", \"viernes\"]"`

### Valores Null
- Los campos opcionales pueden ser `null` o simplemente omitirse del JSON
- La API asignará valores por defecto cuando corresponda

---

## 🔧 USO CON SWAGGER

1. Ir a `https://habitusplus.somee.com` (Swagger UI)
2. Hacer clic en `POST /api/v1/habits`
3. Hacer clic en "Try it out"
4. Pegar uno de los JSON de ejemplo en el campo "Request body"
5. Hacer clic en "Execute"
6. Ver la respuesta con el hábito creado (incluyendo el `id` generado)

---

## 🔧 USO CON cURL

```bash
curl -X POST "https://habitusplus.somee.com/api/v1/habits" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN_JWT_AQUI" \
  -d '{
    "id": 0,
    "userId": 0,
    "title": "Ejercicio Matutino",
    "goal": "Hacer ejercicio todos los días",
    "category": "Salud",
    "type": "EXERCISE",
    "points": 15,
    "targetValue": 30,
    "targetUnit": "minutos",
    "reminderTimes": "08:00"
  }'
```

---

## 🔧 USO CON POSTMAN

1. Método: `POST`
2. URL: `https://habitusplus.somee.com/api/v1/habits`
3. Headers:
   - `Content-Type: application/json`
   - `Authorization: Bearer TU_TOKEN_JWT_AQUI`
4. Body (raw JSON): Pegar uno de los ejemplos de arriba
5. Enviar request

---

**Última actualización**: 2025-12-03

