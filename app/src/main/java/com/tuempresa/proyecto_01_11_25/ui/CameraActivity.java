package com.tuempresa.proyecto_01_11_25.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.cameraPreview);
        hintView = findViewById(R.id.txtHint);
        progressBar = findViewById(R.id.progressDetection);

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
        if (hasCompleted) {
            imageProxy.close();
            return;
        }

        if (isProcessingFrame) {
            imageProxy.close();
            return;
        }

        Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }

        isProcessingFrame = true;
        InputImage inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.getImageInfo().getRotationDegrees()
        );

        Task<Text> task = recognizer.process(inputImage)
                .addOnSuccessListener(text -> {
                    String detected = text.getText();
                    if (!hasCompleted && detected != null && detected.trim().length() >= 6) {
                        // Consideramos texto válido cuando detectamos al menos 6 caracteres
                        onTextDetected(detected);
                    }
                })
                .addOnFailureListener(e ->
                        android.util.Log.w("CameraActivity", "Fallo reconocimiento de texto", e))
                .addOnCompleteListener(t -> {
                    isProcessingFrame = false;
                    imageProxy.close();
                });
    }

    private void onTextDetected(String rawText) {
        hasCompleted = true;
        runOnUiThread(() -> {
            hintView.setText("Texto detectado ✅");
            progressBar.setVisibility(View.VISIBLE);
        });

        // Pequeño delay para mostrar feedback antes de cerrar
        previewView.postDelayed(() -> {
            Intent data = new Intent();
            data.putExtra("habit_completed", "READ");
            setResult(RESULT_OK, data);
            finish();
        }, 600);
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


