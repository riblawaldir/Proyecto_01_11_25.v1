# 🚀 Guía para Activar el Consumo de la API Local

## ⚠️ Estado Actual

**La aplicación NO está consumiendo la API todavía** porque:
- ✅ La URL está configurada correctamente: `http://10.0.2.2:5000/api/v1/`
- ❌ Las Activities todavía usan `HabitDatabaseHelper` directamente
- ❌ No están usando `HabitRepository` que es el que conecta con la API

---

## ✅ Pasos para Activar la API

### Paso 1: Verificar que la API .NET esté ejecutándose

```bash
cd C:\Users\waldi\OneDrive\Área de Trabalho\Api_Habitus
dotnet run
```

**Debe mostrar:**
```
Now listening on: http://localhost:5000
Now listening on: https://localhost:5001
```

### Paso 2: Verificar la URL según tu dispositivo

**Si usas EMULADOR Android:**
```java
// Ya está configurado en HabitApiClient.java
BASE_URL = "http://10.0.2.2:5000/api/v1/"; // ✅ Correcto
```

**Si usas DISPOSITIVO FÍSICO:**
1. Encontrar la IP de tu PC:
   ```bash
   ipconfig
   # Buscar "IPv4 Address" (ejemplo: 192.168.1.100)
   ```

2. Actualizar en `HabitApiClient.java`:
   ```java
   private static final String BASE_URL = "http://192.168.1.100:5000/api/v1/"; // Cambiar IP
   ```

3. Asegurarse de que el dispositivo y la PC estén en la misma red WiFi

### Paso 3: Actualizar DashboardActivity

**Reemplazar el código actual:**

```java
// ❌ ANTES (solo SQLite local):
dbHelper = new HabitDatabaseHelper(this);
habits = dbHelper.getAllHabits();

// ✅ DESPUÉS (SQLite + API):
HabitRepository repository = HabitRepository.getInstance(this);
repository.getAllHabits(new HabitRepository.RepositoryCallback<List<Habit>>() {
    @Override
    public void onSuccess(List<Habit> habits) {
        runOnUiThread(() -> {
            // Actualizar UI con hábitos (vienen de SQLite primero, luego se sincronizan)
            adapter.updateHabits(habits);
        });
    }

    @Override
    public void onError(String error) {
        runOnUiThread(() -> {
            Toast.makeText(DashboardActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
        });
    }
});
```

### Paso 4: Probar la Conexión

1. **Ejecutar la API .NET** en tu PC
2. **Ejecutar la app Android** (emulador o dispositivo físico)
3. **Verificar logs** en Android Studio Logcat:
   - Filtrar por: `HabitApiClient`, `HabitRepository`, `SyncManager`
   - Deberías ver requests HTTP a `http://10.0.2.2:5000/api/v1/habits`

---

## 🔍 Cómo Verificar que Está Funcionando

### 1. Ver Logs de Retrofit

En Android Studio Logcat, buscar:
```
HabitApiClient: Base URL actualizada a: http://10.0.2.2:5000/api/v1/
OkHttp: --> GET http://10.0.2.2:5000/api/v1/habits
OkHttp: <-- 200 OK http://10.0.2.2:5000/api/v1/habits
```

### 2. Verificar en la API

En la consola de la API .NET deberías ver:
```
info: Microsoft.AspNetCore.Hosting.Diagnostics[1]
      Request starting HTTP/1.1 GET http://localhost:5000/api/v1/habits
info: Microsoft.AspNetCore.Mvc.Infrastructure.ControllerActionInvoker[3]
      Executing action Api_Habitus.Controllers.HabitController.GetAllHabits
```

### 3. Probar Modo Offline

1. Activar **Modo Avión** en el dispositivo
2. Crear un hábito → Se guarda solo en SQLite
3. Desactivar **Modo Avión**
4. El hábito se sincroniza automáticamente con la API

---

## ⚙️ Configuración Actual

### HabitApiClient.java
```java
// ✅ Configurado para emulador
private static final String BASE_URL = "http://10.0.2.2:5000/api/v1/";
```

### appsettings.json (API .NET)
```json
{
  "ConnectionStrings": {
    "conn": "Server=localhost\\SQLEXPRESS;Database=HabitusDB;..."
  }
}
```

---

## 🐛 Solución de Problemas

### Error: "Unable to resolve host"
**Solución:**
- Verificar que la API esté ejecutándose
- Verificar URL correcta según dispositivo (emulador vs físico)
- Verificar firewall de Windows

### Error: "Connection refused"
**Solución:**
- Verificar que la API escuche en `http://localhost:5000`
- Verificar que no haya otro proceso usando el puerto 5000
- Para dispositivo físico: verificar que estén en la misma red

### No se ven datos de la API
**Solución:**
- Verificar que la base de datos SQL Server tenga datos
- Verificar logs de la API para ver si recibe requests
- Verificar que `HabitRepository` esté siendo usado (no `HabitDatabaseHelper`)

---

## 📝 Resumen

**Para que la app consuma la API local:**

1. ✅ API .NET ejecutándose en `http://localhost:5000`
2. ✅ URL configurada en `HabitApiClient.java`
3. ⚠️ **FALTA:** Actualizar Activities para usar `HabitRepository`
4. ⚠️ **FALTA:** Probar la conexión

**Una vez actualizado DashboardActivity, la app:**
- Cargará datos de SQLite primero (rápido)
- Sincronizará con la API en segundo plano
- Funcionará offline guardando en SQLite
- Sincronizará automáticamente cuando vuelva la conexión

