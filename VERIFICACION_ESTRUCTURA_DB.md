# ✅ Verificación de Estructura: SQLite vs SQL Server

## Comparación de Tablas

### Tabla: Habits

| Campo SQL Server | Tipo SQL Server | Campo SQLite | Tipo SQLite | ✅ Coincide |
|-----------------|-----------------|--------------|-------------|-------------|
| Id | BIGINT IDENTITY | id | INTEGER PRIMARY KEY AUTOINCREMENT | ✅ |
| Title | NVARCHAR(200) NOT NULL | title | TEXT NOT NULL | ✅ |
| Goal | NVARCHAR(500) | goal | TEXT | ✅ |
| Category | NVARCHAR(100) | category | TEXT | ✅ |
| Type | NVARCHAR(50) NOT NULL | type | TEXT NOT NULL | ✅ |
| Completed | BIT DEFAULT 0 | completed | INTEGER DEFAULT 0 | ✅ |
| Points | INT DEFAULT 10 | points | INTEGER DEFAULT 10 | ✅ |
| TargetValue | FLOAT DEFAULT 0.0 | target_value | REAL DEFAULT 0 | ✅ |
| TargetUnit | NVARCHAR(50) | target_unit | TEXT | ✅ |
| PagesPerDay | INT | pages_per_day | INTEGER | ✅ |
| ReminderTimes | NVARCHAR(500) | reminder_times | TEXT | ✅ |
| DurationMinutes | INT | duration_minutes | INTEGER | ✅ |
| DndMode | BIT | dnd_mode | INTEGER | ✅ |
| MusicId | INT | music_id | INTEGER | ✅ |
| JournalEnabled | BIT | journal_enabled | INTEGER | ✅ |
| GymDays | NVARCHAR(200) | gym_days | TEXT | ✅ |
| WaterGoalGlasses | INT | water_goal_glasses | INTEGER | ✅ |
| OneClickComplete | BIT | one_click_complete | INTEGER | ✅ |
| EnglishMode | BIT | english_mode | INTEGER | ✅ |
| CodingMode | BIT | coding_mode | INTEGER | ✅ |
| HabitIcon | NVARCHAR(100) | habit_icon | TEXT | ✅ |
| CreatedAt | DATETIME2 DEFAULT GETUTCDATE() | created_at | INTEGER DEFAULT (strftime('%s', 'now')) | ✅ |

**Campos adicionales en SQLite para sincronización:**
- `synced` (INTEGER DEFAULT 0) - Indica si está sincronizado
- `server_id` (INTEGER) - ID del servidor
- `updated_at` (INTEGER) - Timestamp de última actualización

### Tabla: Scores

| Campo SQL Server | Tipo SQL Server | Campo SQLite | Tipo SQLite | ✅ Coincide |
|-----------------|-----------------|--------------|-------------|-------------|
| Id | BIGINT IDENTITY | id | INTEGER PRIMARY KEY AUTOINCREMENT | ✅ |
| HabitId | BIGINT NOT NULL | habit_id | INTEGER | ✅ |
| HabitTitle | NVARCHAR(200) NOT NULL | habit_title | TEXT NOT NULL | ✅ |
| Points | INT NOT NULL | points | INTEGER NOT NULL | ✅ |
| Date | DATETIME2 DEFAULT GETUTCDATE() | date | INTEGER DEFAULT (strftime('%s', 'now')) | ✅ |

**Campos adicionales en SQLite para sincronización:**
- `synced` (INTEGER DEFAULT 0)
- `server_id` (INTEGER)
- `local_id` (INTEGER)

### Tabla: PendingOperations (Solo SQLite)

Esta tabla solo existe en SQLite para manejar operaciones pendientes cuando no hay conexión:
- `id` (INTEGER PRIMARY KEY AUTOINCREMENT)
- `operation_type` (TEXT) - CREATE, UPDATE, DELETE
- `entity_type` (TEXT) - HABIT, SCORE
- `entity_id` (INTEGER)
- `entity_data` (TEXT) - JSON del objeto
- `created_at` (INTEGER)
- `retry_count` (INTEGER DEFAULT 0)
- `last_error` (TEXT)
- `priority` (INTEGER DEFAULT 2)

## ✅ Conclusión

**Todas las estructuras coinciden correctamente:**
- ✅ Todos los campos principales están presentes en ambas bases de datos
- ✅ Los tipos de datos son equivalentes (SQLite usa tipos más simples pero compatibles)
- ✅ Los campos adicionales de sincronización solo existen en SQLite (necesarios para offline)
- ✅ La estructura permite sincronización bidireccional sin pérdida de datos

## Notas Importantes

1. **Conversión de Tipos:**
   - SQL Server `BIT` → SQLite `INTEGER` (0/1)
   - SQL Server `NVARCHAR` → SQLite `TEXT`
   - SQL Server `DATETIME2` → SQLite `INTEGER` (timestamp Unix)
   - SQL Server `BIGINT` → SQLite `INTEGER` (SQLite soporta hasta 64 bits)

2. **Timestamps:**
   - SQL Server usa `DATETIME2` con `GETUTCDATE()`
   - SQLite usa `INTEGER` con `strftime('%s', 'now')` (segundos desde epoch)
   - La conversión se hace en el código Java

3. **IDs:**
   - SQL Server usa `BIGINT IDENTITY(1,1)`
   - SQLite usa `INTEGER PRIMARY KEY AUTOINCREMENT`
   - Ambos generan IDs automáticamente
