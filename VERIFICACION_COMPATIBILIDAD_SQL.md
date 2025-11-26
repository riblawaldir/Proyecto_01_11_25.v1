# ✅ Verificación de Compatibilidad: SQLite vs SQL Server

## 📊 Comparación de Estructuras

### Tabla: Habits

| Campo SQL Server | Tipo SQL Server | Campo SQLite | Tipo SQLite | ✅ Compatible |
|------------------|-----------------|--------------|-------------|--------------|
| Id | BIGINT IDENTITY | id | INTEGER PRIMARY KEY AUTOINCREMENT | ✅ |
| Title | NVARCHAR(200) NOT NULL | title | TEXT NOT NULL | ✅ |
| Goal | NVARCHAR(500) NULL | goal | TEXT | ✅ |
| Category | NVARCHAR(100) NULL | category | TEXT | ✅ |
| Type | NVARCHAR(50) NOT NULL | type | TEXT NOT NULL | ✅ |
| Completed | BIT NOT NULL DEFAULT 0 | completed | INTEGER DEFAULT 0 | ✅ |
| Points | INT NOT NULL DEFAULT 10 | points | INTEGER DEFAULT 10 | ✅ |
| TargetValue | FLOAT NOT NULL DEFAULT 0.0 | target_value | REAL DEFAULT 0 | ✅ |
| TargetUnit | NVARCHAR(50) NULL | target_unit | TEXT | ✅ |
| PagesPerDay | INT NULL | pages_per_day | INTEGER | ✅ |
| ReminderTimes | NVARCHAR(500) NULL | reminder_times | TEXT | ✅ |
| DurationMinutes | INT NULL | duration_minutes | INTEGER | ✅ |
| DndMode | BIT NULL | dnd_mode | INTEGER DEFAULT 0 | ✅ |
| MusicId | INT NULL | music_id | INTEGER | ✅ |
| JournalEnabled | BIT NULL | journal_enabled | INTEGER DEFAULT 0 | ✅ |
| GymDays | NVARCHAR(200) NULL | gym_days | TEXT | ✅ |
| WaterGoalGlasses | INT NULL | water_goal_glasses | INTEGER | ✅ |
| OneClickComplete | BIT NULL | one_click_complete | INTEGER DEFAULT 0 | ✅ |
| EnglishMode | BIT NULL | english_mode | INTEGER DEFAULT 0 | ✅ |
| CodingMode | BIT NULL | coding_mode | INTEGER DEFAULT 0 | ✅ |
| HabitIcon | NVARCHAR(100) NULL | habit_icon | TEXT | ✅ |
| CreatedAt | DATETIME2 NOT NULL | created_at | INTEGER (Unix timestamp) | ✅ |

**Campos adicionales en SQLite para sincronización:**
- `synced` (INTEGER) - Indica si está sincronizado
- `server_id` (INTEGER) - ID del servidor
- `updated_at` (INTEGER) - Timestamp de última actualización

### Tabla: Scores

| Campo SQL Server | Tipo SQL Server | Campo SQLite | Tipo SQLite | ✅ Compatible |
|------------------|-----------------|--------------|-------------|--------------|
| Id | BIGINT IDENTITY | id | INTEGER PRIMARY KEY AUTOINCREMENT | ✅ |
| HabitId | BIGINT NOT NULL | habit_id | INTEGER | ✅ |
| HabitTitle | NVARCHAR(200) NOT NULL | habit_title | TEXT NOT NULL | ✅ |
| Points | INT NOT NULL | points | INTEGER NOT NULL | ✅ |
| Date | DATETIME2 NOT NULL | date | INTEGER (Unix timestamp) | ✅ |

## ✅ Conclusión

**Todas las estructuras son compatibles.** Los tipos de datos se mapean correctamente:
- NVARCHAR → TEXT
- INT → INTEGER
- FLOAT → REAL
- BIT → INTEGER (0/1)
- DATETIME2 → INTEGER (Unix timestamp en segundos)

## 🔄 Mapeo de Datos

### Al enviar a API (SQLite → SQL Server):
- `INTEGER` (0/1) → `BIT`
- `INTEGER` (timestamp) → `DATETIME2` (convertir a DateTime)
- `TEXT` → `NVARCHAR`

### Al recibir de API (SQL Server → SQLite):
- `BIT` → `INTEGER` (0/1)
- `DATETIME2` → `INTEGER` (convertir a Unix timestamp)
- `NVARCHAR` → `TEXT`

## 📝 Notas

1. **Timestamps**: SQLite usa Unix timestamp (segundos desde 1970), SQL Server usa DATETIME2. Se debe convertir en el código.
2. **Booleanos**: SQLite usa INTEGER (0/1), SQL Server usa BIT. Compatible.
3. **IDs**: Ambos usan auto-incremento, pero SQL Server usa BIGINT IDENTITY, SQLite usa INTEGER. Compatible para valores normales.

