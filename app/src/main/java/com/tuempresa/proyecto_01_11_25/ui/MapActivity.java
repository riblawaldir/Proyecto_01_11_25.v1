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
import com.tuempresa.proyecto_01_11_25.model.Habit;
import com.tuempresa.proyecto_01_11_25.model.HabitEvent;
import com.tuempresa.proyecto_01_11_25.model.HabitEventStore;
import com.tuempresa.proyecto_01_11_25.utils.MapMarkerRenderer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap map;
    private Map<Marker, HabitEvent> markerEventMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        HabitEventStore.init(this);

        SupportMapFragment frag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (frag != null)
            frag.getMapAsync(this);

        FloatingActionButton back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(this);
        drawEvents();
    }

    private void drawEvents() {
        List<HabitEvent> list = HabitEventStore.all();
        if (list.isEmpty())
            return;

        LatLng focus = null;
        for (HabitEvent e : list) {
            LatLng p = new LatLng(e.getLat(), e.getLng());

            // Convertir HabitEvent.HabitType a Habit.HabitType para usar MapMarkerRenderer
            Habit.HabitType habitType = convertEventTypeToHabitType(e.getType());

            // Crear un hábito temporal para obtener el ícono
            Habit tempHabit = new Habit(e.getNote(), "", "", habitType);

            // Usar MapMarkerRenderer para obtener el ícono
            BitmapDescriptor icon = MapMarkerRenderer.getMarkerIcon(this, tempHabit);

            Marker marker = map.addMarker(new MarkerOptions()
                    .position(p)
                    .title(e.getNote())
                    .icon(icon));

            // Guardar relación marcador-evento
            if (marker != null) {
                markerEventMap.put(marker, e);
            }

            focus = p;
        }
        if (focus != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(focus, 16f));
        }
    }

    /**
     * Convierte HabitEvent.HabitType a Habit.HabitType
     */
    private Habit.HabitType convertEventTypeToHabitType(HabitEvent.HabitType eventType) {
        switch (eventType) {
            case EXERCISE:
                return Habit.HabitType.EXERCISE;
            case WALK:
                return Habit.HabitType.WALK;
            case READ:
                return Habit.HabitType.READ_BOOK;
            case FOCUS:
                return Habit.HabitType.MEDITATE;
            case DEMO:
            default:
                return Habit.HabitType.DEMO;
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        HabitEvent event = markerEventMap.get(marker);
        if (event != null) {
            showHabitDetailBottomSheet(event);
            return true; // Consumir el evento
        }
        return false;
    }

    /**
     * Muestra el bottom sheet con los detalles del hábito completado
     */
    private void showHabitDetailBottomSheet(HabitEvent event) {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.habit_map_detail, null);

        // Configurar views
        ImageView imgIcon = bottomSheetView.findViewById(R.id.imgHabitIcon);
        TextView txtHabitName = bottomSheetView.findViewById(R.id.txtHabitName);
        TextView txtDate = bottomSheetView.findViewById(R.id.txtDate);
        TextView txtLocation = bottomSheetView.findViewById(R.id.txtLocation);
        TextView txtDetails = bottomSheetView.findViewById(R.id.txtDetails);
        MaterialButton btnViewHabit = bottomSheetView.findViewById(R.id.btnViewHabit);

        // Obtener ícono
        Habit.HabitType habitType = convertEventTypeToHabitType(event.getType());
        Habit tempHabit = new Habit(event.getNote(), "", "", habitType);
        int iconDrawableId = com.tuempresa.proyecto_01_11_25.utils.HabitIconUtils.getDefaultIconForType(habitType);
        imgIcon.setImageResource(iconDrawableId);

        // Configurar textos
        txtHabitName.setText(event.getNote());

        // Formatear fecha
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String dateStr = sdf.format(new Date(event.getTimestamp()));
        txtDate.setText("Completado: " + dateStr);

        // Ubicación aproximada
        txtLocation.setText(String.format(Locale.getDefault(), "Ubicación: %.4f, %.4f",
                event.getLat(), event.getLng()));

        // Detalles adicionales (si aplica)
        txtDetails.setText("Hábito completado exitosamente");

        // Botón para ver hábito (por ahora solo cierra el bottom sheet)
        btnViewHabit.setOnClickListener(v -> {
            // TODO: Abrir HabitDetailActivity si es posible
            // Por ahora solo cerramos
            if (bottomSheetView.getParent() != null) {
                ((android.view.ViewGroup) bottomSheetView.getParent()).removeView(bottomSheetView);
            }
        });

        // Crear y mostrar bottom sheet
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                this);
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();
    }
}
