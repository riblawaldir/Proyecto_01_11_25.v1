# 🔌 Guía: Conectar App Android con API Local

## ✅ Estado Actual

**La URL está configurada para API local:**
```java
BASE_URL = "http://10.0.2.2:5000/api/v1/"; // Emulador Android
```

## ⚠️ IMPORTANTE: Verificaciones Necesarias

### 1. ¿Está la API .NET ejecutándose?

**Ejecuta la API:**
```bash
cd C:\Users\waldi\OneDrive\Área de Trabalho\Api_Habitus
dotnet run
```

**Deberías ver:**
```
Now listening on: http://localhost:5000
Now listening on: https://localhost:5001
```

### 2. ¿Estás usando Emulador o Dispositivo Físico?

#### 📱 Si usas EMULADOR Android:
✅ **Ya está configurado correctamente:**
```java
BASE_URL = "http://10.0.2.2:5000/api/v1/";
```
- `10.0.2.2` es la IP especial del emulador que apunta a `localhost` de tu PC
- ✅ **No necesitas cambiar nada**

#### 📱 Si usas DISPOSITIVO FÍSICO:

**Necesitas cambiar la URL:**

1. **Encuentra la IP de tu PC:**
   ```bash
   # En CMD o PowerShell
   ipconfig
   ```
   Busca "IPv4 Address" (ejemplo: `192.168.1.100`)

2. **Actualiza `HabitApiClient.java`:**
   ```java
   // Comentar la línea del emulador:
   // private static final String BASE_URL = "http://10.0.2.2:5000/api/v1/"; // Emulador
   
   // Descomentar y poner tu IP:
   private static final String BASE_URL = "http://192.168.1.100:5000/api/v1/"; // Cambiar con tu IP
   ```

3. **Asegúrate de que:**
   - Tu PC y tu dispositivo estén en la misma red WiFi
   - El firewall de Windows permita conexiones en el puerto 5000

### 3. Verificar que la API escuche en HTTP (puerto 5000)

La API debe escuchar en **HTTP** (puerto 5000), no solo HTTPS.

**Verifica `launchSettings.json`:**
```json
{
  "applicationUrl": "http://localhost:5000;https://localhost:5001"
}
```

Si solo tiene HTTPS, agrega HTTP.

## 🧪 Cómo Probar la Conexión

### Paso 1: Ejecutar la API
```bash
cd C:\Users\waldi\OneDrive\Área de Trabalho\Api_Habitus
dotnet run
```

### Paso 2: Probar desde el navegador
Abre en tu PC:
```
http://localhost:5000/api/v1/habits
```

Deberías ver una respuesta JSON (aunque esté vacía).

### Paso 3: Probar desde la App Android

1. **Abre Logcat en Android Studio**
2. **Filtra por:** `HabitApiClient` o `OkHttp`
3. **Ejecuta la app y busca hábitos**
4. **Deberías ver logs como:**
   ```
   OkHttp: --> GET http://10.0.2.2:5000/api/v1/habits
   OkHttp: <-- 200 OK http://10.0.2.2:5000/api/v1/habits
   ```

## 🔧 Solución de Problemas

### Error: "Failed to connect to /10.0.2.2:5000"

**Causas posibles:**
1. ❌ La API no está ejecutándose
   - **Solución:** Ejecuta `dotnet run` en la carpeta de la API

2. ❌ La API solo escucha en HTTPS (puerto 5001)
   - **Solución:** Verifica `launchSettings.json` y agrega HTTP

3. ❌ Firewall bloqueando el puerto
   - **Solución:** Permite el puerto 5000 en el firewall de Windows

### Error: "Connection refused" (Dispositivo Físico)

**Causas:**
1. ❌ IP incorrecta
   - **Solución:** Verifica la IP con `ipconfig` y actualiza `BASE_URL`

2. ❌ No están en la misma red
   - **Solución:** Conecta PC y dispositivo a la misma WiFi

3. ❌ Firewall bloqueando
   - **Solución:** Permite conexiones en el puerto 5000

### La app funciona pero no se conecta a la API

**Verifica:**
1. ✅ ¿Estás usando `HabitRepository` o todavía `HabitDatabaseHelper` directamente?
   - Si usas `HabitDatabaseHelper` directamente, **NO se conectará a la API**
   - Debes usar `HabitRepository` para que funcione la sincronización

2. ✅ ¿Hay conexión a internet?
   - `ConnectionMonitor` detecta si hay conexión
   - Si no hay conexión, guarda en SQLite local y cola de operaciones pendientes

## 📋 Checklist Rápido

- [ ] API .NET ejecutándose (`dotnet run`)
- [ ] API escucha en `http://localhost:5000`
- [ ] URL en `HabitApiClient.java` correcta:
  - Emulador: `http://10.0.2.2:5000/api/v1/`
  - Dispositivo: `http://[TU_IP]:5000/api/v1/`
- [ ] Firewall permite puerto 5000
- [ ] App usa `HabitRepository` (no `HabitDatabaseHelper` directamente)
- [ ] Logcat muestra requests HTTP

## 🎯 Respuesta Directa

**¿La app consumirá la API local ahora?**

**SÍ, PERO necesitas:**

1. ✅ **Ejecutar la API .NET** (`dotnet run`)
2. ✅ **Verificar que escuche en puerto 5000** (HTTP)
3. ✅ **Si usas dispositivo físico, cambiar la IP en `BASE_URL`**
4. ✅ **Usar `HabitRepository` en lugar de `HabitDatabaseHelper` directamente**

**Si todo está correcto, la app:**
- ✅ Intentará conectarse a `http://10.0.2.2:5000/api/v1/` (emulador)
- ✅ O a `http://[TU_IP]:5000/api/v1/` (dispositivo físico)
- ✅ Si hay conexión, sincronizará con la API
- ✅ Si no hay conexión, guardará en SQLite local

---

**¿Necesitas ayuda con algún paso específico?**

