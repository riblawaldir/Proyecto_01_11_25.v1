package com.tuempresa.proyecto_01_11_25.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tuempresa.proyecto_01_11_25.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper;
import com.tuempresa.proyecto_01_11_25.model.Habit;
import com.tuempresa.proyecto_01_11_25.model.HabitCompletion;
import com.tuempresa.proyecto_01_11_25.repository.HabitRepository;
import com.tuempresa.proyecto_01_11_25.utils.MapMarkerRenderer;
import com.tuempresa.proyecto_01_11_25.utils.SessionManager;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap map;
    private Map<Marker, HabitCompletion> markerCompletionMap = new HashMap<>();
    private SessionManager sessionManager;
    private FusedLocationProviderClient fusedLocationClient;
    private HabitRepository habitRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        sessionManager = new SessionManager(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        habitRepository = HabitRepository.getInstance(this);

        SupportMapFragment frag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (frag != null) {
            frag.getMapAsync(this);
            android.util.Log.d("MapActivity", "Solicitando mapa asíncrono...");
        } else {
            android.util.Log.e("MapActivity", "ERROR: No se encontró el fragmento del mapa");
            android.widget.Toast.makeText(this, "Error: No se pudo cargar el mapa", 
                    android.widget.Toast.LENGTH_LONG).show();
        }

        FloatingActionButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        android.util.Log.d("MapActivity", "✅ Mapa listo (onMapReady llamado)");
        map = googleMap;
        
        // Configurar el mapa
        try {
            map.setOnMarkerClickListener(this);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            android.util.Log.d("MapActivity", "Configuración del mapa completada");
        } catch (Exception e) {
            android.util.Log.e("MapActivity", "Error al configurar el mapa", e);
        }
        
        drawEvents();
    }

    private void drawEvents() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) {
            android.widget.Toast.makeText(this, "No hay usuario logueado", 
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Obtener completados de HOY del usuario actual (local + sincronizar con servidor)
        habitRepository.getTodayCompletions(new HabitRepository.RepositoryCallback<List<HabitCompletion>>() {
            @Override
            public void onSuccess(List<HabitCompletion> completions) {
                android.util.Log.d("MapActivity", "Total de completados de hoy: " + completions.size());
                
                if (completions.isEmpty()) {
                    // Si no hay completados, mostrar ubicación actual del usuario
                    showCurrentLocation();
                    return;
                }
                
                drawCompletionsOnMap(completions);
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("MapActivity", "Error al obtener completados: " + error);
                // Fallback: intentar obtener solo de local
                HabitDatabaseHelper dbHelper = new HabitDatabaseHelper(MapActivity.this);
                List<HabitCompletion> localCompletions = dbHelper.getTodayCompletions(userId);
                if (localCompletions.isEmpty()) {
                    showCurrentLocation();
                } else {
                    drawCompletionsOnMap(localCompletions);
                }
            }
        });
    }
    
    private void drawCompletionsOnMap(List<HabitCompletion> completions) {
        
        // Filtrar solo los que tienen coordenadas válidas
        List<HabitCompletion> validCompletions = new java.util.ArrayList<>();
        double sumLat = 0;
        double sumLng = 0;
        int validCount = 0;
        
        for (HabitCompletion completion : completions) {
            if (completion.getLatitude() != 0.0 || completion.getLongitude() != 0.0) {
                validCompletions.add(completion);
                sumLat += completion.getLatitude();
                sumLng += completion.getLongitude();
                validCount++;
                android.util.Log.d("MapActivity", "Completado válido: " + completion.getHabitTitle() + 
                        " en (" + completion.getLatitude() + ", " + completion.getLongitude() + ")");
            } else {
                android.util.Log.d("MapActivity", "Completado sin GPS: " + completion.getHabitTitle());
            }
        }
        
        android.util.Log.d("MapActivity", "Completados válidos: " + validCount + ", Sin GPS: " + 
                (completions.size() - validCount));
        
        if (validCompletions.isEmpty()) {
            // Si los completados no tienen GPS, mostrar ubicación actual del usuario
            android.widget.Toast.makeText(this, "Los hábitos completados no tienen ubicación GPS. Mostrando tu ubicación actual", 
                    android.widget.Toast.LENGTH_SHORT).show();
            showCurrentLocation();
            return;
        }
        
        // Limpiar marcadores anteriores
        markerCompletionMap.clear();
        map.clear();
        
        // Dibujar marcadores
        int markersAdded = 0;
        for (HabitCompletion completion : validCompletions) {
            LatLng position = new LatLng(
                completion.getLatitude(), 
                completion.getLongitude()
            );
            
            // Obtener ícono del hábito
            Habit.HabitType habitType = completion.getHabitType();
            BitmapDescriptor icon = MapMarkerRenderer.getMarkerIconForEventType(
                this, habitType);
            
            Marker marker = map.addMarker(new MarkerOptions()
                .position(position)
                .title(completion.getHabitTitle())
                .icon(icon));
            
            if (marker != null) {
                markerCompletionMap.put(marker, completion);
                markersAdded++;
            }
        }
        
        android.util.Log.d("MapActivity", "Marcadores agregados: " + markersAdded);
        
        // Centrar mapa
        if (validCount > 0) {
            LatLng center = new LatLng(sumLat / validCount, sumLng / validCount);
            float zoom = validCount == 1 ? 16f : (validCount < 5 ? 14f : 12f);
            android.util.Log.d("MapActivity", "Centrando mapa en: (" + center.latitude + ", " + 
                    center.longitude + ") con zoom: " + zoom);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoom));
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        HabitCompletion completion = markerCompletionMap.get(marker);
        if (completion != null) {
            showHabitDetailBottomSheet(completion);
            return true;
        }
        return false;
    }

    /**
     * Muestra el bottom sheet con los detalles del hábito completado
     */
    private void showHabitDetailBottomSheet(HabitCompletion completion) {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.habit_map_detail, null);

        ImageView imgIcon = bottomSheetView.findViewById(R.id.imgHabitIcon);
        TextView txtHabitName = bottomSheetView.findViewById(R.id.txtHabitName);
        TextView txtDate = bottomSheetView.findViewById(R.id.txtDate);
        TextView txtLocation = bottomSheetView.findViewById(R.id.txtLocation);
        TextView txtDetails = bottomSheetView.findViewById(R.id.txtDetails);
        MaterialButton btnViewHabit = bottomSheetView.findViewById(R.id.btnViewHabit);

        // Obtener ícono
        Habit.HabitType habitType = completion.getHabitType();
        int iconDrawableId = com.tuempresa.proyecto_01_11_25.utils.HabitIconUtils.getDefaultIconForType(habitType);
        imgIcon.setImageResource(iconDrawableId);

        // Configurar textos
        txtHabitName.setText(completion.getHabitTitle());

        // Formatear fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String dateStr = sdf.format(new Date(completion.getCreatedAt()));
        txtDate.setText("Completado: " + dateStr);

        // Ubicación
        if (completion.getLatitude() != 0.0 || completion.getLongitude() != 0.0) {
            txtLocation.setText(String.format(Locale.getDefault(), "Ubicación: %.4f, %.4f",
                    completion.getLatitude(), completion.getLongitude()));
        } else {
            txtLocation.setText("Ubicación: No disponible");
        }

        txtDetails.setText("Hábito completado exitosamente");

        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = 
                new com.google.android.material.bottomsheet.BottomSheetDialog(this);
        bottomSheetDialog.setContentView(bottomSheetView);
        btnViewHabit.setOnClickListener(v -> bottomSheetDialog.dismiss());
        bottomSheetDialog.show();
    }
    
    /**
     * Muestra la ubicación actual del usuario en el mapa
     */
    private void showCurrentLocation() {
        if (!hasFineLocationPermission()) {
            android.widget.Toast.makeText(this, "Se necesitan permisos de ubicación para mostrar tu posición", 
                    android.widget.Toast.LENGTH_LONG).show();
            // Mover a ubicación por defecto si no hay permisos
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(19.4326, -99.1332), 10f));
            return;
        }
        
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(
                        location.getLatitude(), 
                        location.getLongitude()
                    );
                    
                    // Mover cámara a ubicación actual
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f));
                    
                    // Agregar marcador en la ubicación actual
                    map.addMarker(new MarkerOptions()
                        .position(currentLocation)
                        .title("Tu ubicación actual")
                        .snippet("Aquí estás ahora"));
                    
                    android.util.Log.d("MapActivity", "Ubicación actual mostrada: (" + 
                            location.getLatitude() + ", " + location.getLongitude() + ")");
                } else {
                    android.util.Log.w("MapActivity", "No se pudo obtener ubicación actual");
                    android.widget.Toast.makeText(this, "No se pudo obtener tu ubicación. Mostrando ubicación por defecto", 
                            android.widget.Toast.LENGTH_SHORT).show();
                    // Mover a ubicación por defecto
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(19.4326, -99.1332), 10f));
                }
            }).addOnFailureListener(e -> {
                android.util.Log.e("MapActivity", "Error al obtener ubicación actual", e);
                android.widget.Toast.makeText(this, "Error al obtener tu ubicación. Mostrando ubicación por defecto", 
                        android.widget.Toast.LENGTH_SHORT).show();
                // Mover a ubicación por defecto
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(19.4326, -99.1332), 10f));
            });
        } catch (SecurityException e) {
            android.util.Log.e("MapActivity", "Permiso de ubicación denegado", e);
            android.widget.Toast.makeText(this, "Permisos de ubicación denegados", 
                    android.widget.Toast.LENGTH_SHORT).show();
            // Mover a ubicación por defecto
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(19.4326, -99.1332), 10f));
        }
    }
    
    /**
     * Verifica si se tienen permisos de ubicación
     */
    private boolean hasFineLocationPermission() {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
