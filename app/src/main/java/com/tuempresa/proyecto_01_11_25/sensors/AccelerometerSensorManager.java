package com.tuempresa.proyecto_01_11_25.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Gestor del acelerómetro para detectar ejercicio/movimiento sostenido.
 * Cuando se detecta actividad física por encima de un umbral durante un intervalo,
 * ejecuta el callback proporcionado y se detiene.
 */
public class AccelerometerSensorManager implements SensorEventListener {

    private static final float MOVEMENT_THRESHOLD = 2.2f; // m/s^2 sobre la gravedad
    private static final long REQUIRED_ACTIVE_MS = 4_000; // tiempo acumulado de actividad
    private static final long MAX_IDLE_GAP_MS = 1_500; // tolerancia de inactividad entre picos

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Runnable onExerciseDetected;

    private boolean isListening = false;
    private long activeAccumulatedMs = 0;
    private long lastActiveTs = 0;
    private long lastSampleTs = 0;
    private boolean done = false;

    public AccelerometerSensorManager(Context context, Runnable onExerciseDetected) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.accelerometer = sensorManager != null ? sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) : null;
        this.onExerciseDetected = onExerciseDetected;
    }

    public void start() {
        if (accelerometer != null && !isListening) {
            isListening = true;
            done = false;
            activeAccumulatedMs = 0;
            lastActiveTs = 0;
            lastSampleTs = 0;
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    public void stop() {
        if (isListening) {
            isListening = false;
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // No se requiere manejo especial de precisión
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!isListening || done) return;

        // Magnitud total incluyendo gravedad
        float ax = event.values[0];
        float ay = event.values[1];
        float az = event.values[2];
        float magnitude = (float) Math.sqrt(ax * ax + ay * ay + az * az);

        // Aproximar la porción dinámica restando ~g (9.81)
        float dynamic = Math.abs(magnitude - SensorManager.GRAVITY_EARTH);

        long now = event.timestamp / 1_000_000L; // ns -> ms
        if (lastSampleTs == 0) lastSampleTs = now;
        long delta = Math.max(0, Math.min(100, now - lastSampleTs)); // cap delta para evitar acumulaciones enormes
        lastSampleTs = now;

        boolean isActive = dynamic > MOVEMENT_THRESHOLD;
        if (isActive) {
            // si hubo un gap muy largo, no acumularlo
            if (lastActiveTs == 0 || (now - lastActiveTs) <= MAX_IDLE_GAP_MS) {
                activeAccumulatedMs += delta;
            } else {
                // gap largo, reiniciar acumulado
                activeAccumulatedMs = 0;
            }
            lastActiveTs = now;
        } else {
            // si nos quedamos inactivos más del gap permitido, ir drenando el acumulado
            if (lastActiveTs > 0 && (now - lastActiveTs) > MAX_IDLE_GAP_MS) {
                activeAccumulatedMs = Math.max(0, activeAccumulatedMs - delta);
            }
        }

        if (activeAccumulatedMs >= REQUIRED_ACTIVE_MS) {
            done = true;
            if (onExerciseDetected != null) onExerciseDetected.run();
            stop();
        }
    }
}


