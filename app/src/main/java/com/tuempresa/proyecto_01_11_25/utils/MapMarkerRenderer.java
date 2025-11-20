package com.tuempresa.proyecto_01_11_25.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.tuempresa.proyecto_01_11_25.model.Habit;
import com.tuempresa.proyecto_01_11_25.R;

/**
 * Utilidades para renderizar marcadores personalizados en el mapa
 */
public class MapMarkerRenderer {
    
    private static final int MARKER_SIZE_DP = 64; // Tamaño del marcador en dp
    private static final float DENSITY = 3.0f; // Factor de densidad para mejor calidad
    
    /**
     * Convierte un drawable vectorial en BitmapDescriptor para Google Maps
     */
    public static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        if (vectorDrawable == null) {
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
        }
        
        int width = (int) (MARKER_SIZE_DP * DENSITY);
        int height = (int) (MARKER_SIZE_DP * DENSITY);
        vectorDrawable.setBounds(0, 0, width, height);
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    
    /**
     * Obtiene el BitmapDescriptor para un hábito específico
     * Usa el ícono personalizado si existe, sino el por defecto según el tipo
     */
    public static BitmapDescriptor getMarkerIcon(Context context, Habit habit) {
        int drawableId;
        
        // Si el hábito tiene un ícono personalizado, usarlo
        if (habit.getHabitIcon() != null && !habit.getHabitIcon().isEmpty()) {
            drawableId = HabitIconUtils.getIconDrawableId(context, habit.getHabitIcon());
        } else {
            // Usar ícono por defecto según el tipo
            drawableId = HabitIconUtils.getDefaultIconForType(habit.getType());
        }
        
        return bitmapDescriptorFromVector(context, drawableId);
    }
    
    /**
     * Obtiene el BitmapDescriptor para un tipo de evento de hábito
     * (Para compatibilidad con HabitEvent)
     */
    public static BitmapDescriptor getMarkerIconForEventType(Context context, Habit.HabitType type) {
        int drawableId = HabitIconUtils.getDefaultIconForType(type);
        return bitmapDescriptorFromVector(context, drawableId);
    }
}

