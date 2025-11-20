package com.tuempresa.proyecto_01_11_25.utils;

import android.content.Context;
import androidx.core.content.ContextCompat;
import com.tuempresa.proyecto_01_11_25.R;
import com.tuempresa.proyecto_01_11_25.model.Habit;

/**
 * Utilidades para manejar íconos de hábitos
 */
public class HabitIconUtils {
    
    /**
     * Obtiene el drawable ID del ícono según el nombre guardado
     */
    public static int getIconDrawableId(Context context, String iconName) {
        if (iconName == null || iconName.isEmpty()) {
            return getDefaultIconForType(null);
        }
        
        // Mapeo de nombres de íconos a recursos
        switch (iconName) {
            case "ic_habit_book":
                return R.drawable.ic_menu_book_24;
            case "ic_habit_vitamins":
                return R.drawable.ic_habit_vitamins;
            case "ic_habit_meditation":
                return R.drawable.ic_habit_meditation;
            case "ic_habit_journal":
                return R.drawable.ic_habit_journal;
            case "ic_habit_gym":
                return R.drawable.ic_fitness_center_24;
            case "ic_habit_water":
                return R.drawable.ic_habit_water;
            case "ic_habit_cold_shower":
                return R.drawable.ic_habit_cold_shower;
            case "ic_habit_english":
                return R.drawable.ic_habit_english;
            case "ic_habit_coding":
                return R.drawable.ic_habit_coding;
            case "ic_habit_walk":
                return R.drawable.ic_directions_walk_24;
            default:
                return getDefaultIconForType(null);
        }
    }
    
    /**
     * Obtiene el ícono por defecto según el tipo de hábito
     */
    public static int getDefaultIconForType(Habit.HabitType type) {
        if (type == null) {
            return R.drawable.ic_fitness_center_24; // Ícono genérico
        }
        
        switch (type) {
            case READ_BOOK:
            case READ:
                return R.drawable.ic_menu_book_24;
            case VITAMINS:
                return R.drawable.ic_habit_vitamins;
            case MEDITATE:
                return R.drawable.ic_habit_meditation;
            case JOURNALING:
                return R.drawable.ic_habit_journal;
            case GYM:
            case EXERCISE:
                return R.drawable.ic_fitness_center_24;
            case WATER:
                return R.drawable.ic_habit_water;
            case COLD_SHOWER:
                return R.drawable.ic_habit_cold_shower;
            case ENGLISH:
                return R.drawable.ic_habit_english;
            case CODING:
                return R.drawable.ic_habit_coding;
            case WALK:
                return R.drawable.ic_directions_walk_24;
            case DEMO:
            default:
                return R.drawable.ic_fitness_center_24;
        }
    }
    
    /**
     * Obtiene el nombre del ícono por defecto según el tipo de hábito
     */
    public static String getDefaultIconName(Habit.HabitType type) {
        if (type == null) {
            return "ic_habit_gym";
        }
        
        switch (type) {
            case READ_BOOK:
            case READ:
                return "ic_habit_book";
            case VITAMINS:
                return "ic_habit_vitamins";
            case MEDITATE:
                return "ic_habit_meditation";
            case JOURNALING:
                return "ic_habit_journal";
            case GYM:
            case EXERCISE:
                return "ic_habit_gym";
            case WATER:
                return "ic_habit_water";
            case COLD_SHOWER:
                return "ic_habit_cold_shower";
            case ENGLISH:
                return "ic_habit_english";
            case CODING:
                return "ic_habit_coding";
            case WALK:
                return "ic_habit_walk";
            case DEMO:
            default:
                return "ic_habit_gym";
        }
    }
    
    /**
     * Obtiene todos los íconos disponibles para selección
     */
    public static IconOption[] getAvailableIcons() {
        return new IconOption[] {
            new IconOption("ic_habit_book", "Libro", R.drawable.ic_menu_book_24),
            new IconOption("ic_habit_vitamins", "Vitaminas", R.drawable.ic_habit_vitamins),
            new IconOption("ic_habit_meditation", "Meditación", R.drawable.ic_habit_meditation),
            new IconOption("ic_habit_journal", "Diario", R.drawable.ic_habit_journal),
            new IconOption("ic_habit_gym", "Gym", R.drawable.ic_fitness_center_24),
            new IconOption("ic_habit_water", "Agua", R.drawable.ic_habit_water),
            new IconOption("ic_habit_cold_shower", "Ducha Fría", R.drawable.ic_habit_cold_shower),
            new IconOption("ic_habit_english", "Inglés", R.drawable.ic_habit_english),
            new IconOption("ic_habit_coding", "Coding", R.drawable.ic_habit_coding)
        };
    }
    
    /**
     * Clase para representar una opción de ícono
     */
    public static class IconOption {
        private final String iconName;
        private final String displayName;
        private final int drawableId;
        
        public IconOption(String iconName, String displayName, int drawableId) {
            this.iconName = iconName;
            this.displayName = displayName;
            this.drawableId = drawableId;
        }
        
        public String getIconName() { return iconName; }
        public String getDisplayName() { return displayName; }
        public int getDrawableId() { return drawableId; }
    }
}

