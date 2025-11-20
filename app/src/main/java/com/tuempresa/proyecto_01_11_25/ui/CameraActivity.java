package com.tuempresa.proyecto_01_11_25.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.tuempresa.proyecto_01_11_25.R;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper;
import com.tuempresa.proyecto_01_11_25.model.Habit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private static final int REQ_CAMERA = 100;

    private PreviewView previewView;
    private TextView hintView;
    private ProgressBar progressBar;

    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;

    private final com.google.mlkit.vision.text.TextRecognizer recognizer =
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

    private boolean isProcessingFrame = false;
    private boolean hasCompleted = false;
    private long habitId = -1;
    private String habitType = null;
    private HabitDatabaseHelper dbHelper;
    private SharedPreferences progressPrefs;
    
    // Control de debounce para evitar duplicación
    private long lastDetectionTime = 0;
    private static final long DETECTION_DEBOUNCE_MS = 300; // 300ms debounce
    private boolean pageAlreadyAdded = false; // Flag para evitar doble incremento

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.cameraPreview);
        hintView = findViewById(R.id.txtHint);
        progressBar = findViewById(R.id.progressDetection);

        // Obtener información del hábito si viene de HabitDetailActivity
        habitId = getIntent().getLongExtra("habit_id", -1);
        habitType = getIntent().getStringExtra("habit_type");
        
        if (habitId > 0) {
            dbHelper = new HabitDatabaseHelper(this);
            progressPrefs = getSharedPreferences("habit_progress", Context.MODE_PRIVATE);
            
            // Actualizar hint para lectura de páginas
            if ("READ_BOOK".equals(habitType)) {
                hintView.setText("Enfoca una página de tu libro para detectarla 📖");
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (hasCameraPermission()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQ_CAMERA
            );
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindUseCases();
            } catch (ExecutionException | InterruptedException e) {
                android.util.Log.e("CameraActivity", "Error al inicializar cámara", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindUseCases() {
        if (cameraProvider == null) return;

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis analysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        analysis.setAnalyzer(cameraExecutor, this::analyzeImage);

        CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;

        try {
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, selector, preview, analysis);
        } catch (Exception e) {
            android.util.Log.e("CameraActivity", "No se pudieron enlazar los casos de uso", e);
        }
    }

    private void analyzeImage(ImageProxy imageProxy) {
        // Si ya se completó y se agregó la página, ignorar todos los frames siguientes
        if (hasCompleted && pageAlreadyAdded) {
            imageProxy.close();
            return;
        }

        // Bloquear si ya se está procesando un frame
        if (isProcessingFrame) {
            imageProxy.close();
            return;
        }

        // Debounce: evitar procesar frames muy seguidos
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDetectionTime < DETECTION_DEBOUNCE_MS) {
            imageProxy.close();
            return;
        }

        Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        isProcessingFrame = true;
        lastDetectionTime = currentTime;
        
        InputImage inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.getImageInfo().getRotationDegrees()
        );

        Task<Text> task = recognizer.process(inputImage)
                .addOnSuccessListener(text -> {
                    String detected = text.getText();
                    // Validar que el texto tenga suficiente contenido y no se haya procesado ya
                    if (!hasCompleted && !pageAlreadyAdded && detected != null && detected.trim().length() >= 6) {
                        // Filtrar texto: considerar solo si tiene al menos 6 caracteres y no es solo números/símbolos
                        String cleanText = detected.trim();
                        if (isValidPageText(cleanText)) {
                            onTextDetected(cleanText);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        android.util.Log.w("CameraActivity", "Fallo reconocimiento de texto", e))
                .addOnCompleteListener(t -> {
                    isProcessingFrame = false;
                    imageProxy.close();
                });
    }
    
    /**
     * Valida si el texto detectado es válido para contar como página
     * Filtra texto que sea solo números, símbolos o muy corto
     */
    private boolean isValidPageText(String text) {
        if (text == null || text.length() < 6) {
            return false;
        }
        
        // Contar letras vs números/símbolos
        int letterCount = 0;
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                letterCount++;
            }
        }
        
        // Debe tener al menos 3 letras para ser considerado texto de página válido
        return letterCount >= 3;
    }

    private void onTextDetected(String rawText) {
        // Bloquear múltiples llamadas - solo procesar una vez
        if (hasCompleted || pageAlreadyAdded) {
            return;
        }
        
        hasCompleted = true;
        pageAlreadyAdded = true; // Marcar que ya se agregó la página ANTES de procesar
        
        runOnUiThread(() -> {
            hintView.setText(getString(R.string.text_detected));
            progressBar.setVisibility(View.VISIBLE);
        });

        // Si es para READ_BOOK, agregar página al progreso (SOLO UNA VEZ)
        if (habitId > 0 && "READ_BOOK".equals(habitType) && dbHelper != null) {
            Habit habit = dbHelper.getHabitById(habitId);
            if (habit != null) {
                String todayKey = "read_" + habitId + "_" + getTodayKey();
                int currentPages = progressPrefs.getInt(todayKey, 0);
                progressPrefs.edit().putInt(todayKey, currentPages + 1).apply();
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "✅ Página detectada! (+1 página)", Toast.LENGTH_SHORT).show();
                });
            }
        }

        // Pequeño delay para mostrar feedback antes de cerrar
        previewView.postDelayed(() -> {
            Intent data = new Intent();
            if (habitId > 0) {
                data.putExtra("habit_id", habitId);
                data.putExtra("pages_added", 1);
            } else {
                data.putExtra("habit_completed", "READ");
            }
            setResult(RESULT_OK, data);
            finish();
        }, 1000);
    }
    
    private String getTodayKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        recognizer.close();
    }
}


