package com.tuempresa.proyecto_01_11_25.utils;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Clase para validar datos de hábitos
 */
public class HabitValidator {
    
    public static class ValidationResult {
        public final boolean isValid;
        public final String errorMessage;
        
        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }
    }
    
    /**
     * Valida el nombre del hábito
     */
    public static ValidationResult validateHabitName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return ValidationResult.error("El nombre del hábito es obligatorio");
        }
        if (name.trim().length() < 3) {
            return ValidationResult.error("El nombre debe tener al menos 3 caracteres");
        }
        if (name.length() > 50) {
            return ValidationResult.error("El nombre no puede exceder 50 caracteres");
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida páginas por día (Leer Libro)
     */
    public static ValidationResult validatePages(Integer pages) {
        if (pages == null) {
            return ValidationResult.error("Debes especificar las páginas por día");
        }
        if (pages <= 0) {
            return ValidationResult.error("Las páginas deben ser mayor a 0");
        }
        if (pages > 500) {
            return ValidationResult.error("Las páginas no pueden exceder 500 por día");
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida vasos de agua
     */
    public static ValidationResult validateWaterGlasses(Integer glasses) {
        if (glasses == null) {
            return ValidationResult.error("Debes especificar los vasos de agua");
        }
        if (glasses < 1) {
            return ValidationResult.error("Debes beber al menos 1 vaso de agua");
        }
        if (glasses > 20) {
            return ValidationResult.error("Los vasos no pueden exceder 20 por día");
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida duración de meditación
     */
    public static ValidationResult validateMeditationDuration(Integer minutes) {
        if (minutes == null) {
            return ValidationResult.error("Debes especificar la duración de meditación");
        }
        if (minutes < 1) {
            return ValidationResult.error("La duración debe ser al menos 1 minuto");
        }
        if (minutes > 120) {
            return ValidationResult.error("La duración no puede exceder 120 minutos");
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida días de gym seleccionados
     */
    public static ValidationResult validateGymDays(String gymDaysJson) {
        if (gymDaysJson == null || gymDaysJson.trim().isEmpty() || gymDaysJson.equals("[]")) {
            return ValidationResult.error("Debes seleccionar al menos un día para el gym");
        }
        try {
            JSONArray days = new JSONArray(gymDaysJson);
            if (days.length() == 0) {
                return ValidationResult.error("Debes seleccionar al menos un día");
            }
            if (days.length() > 7) {
                return ValidationResult.error("No puedes seleccionar más de 7 días");
            }
        } catch (JSONException e) {
            return ValidationResult.error("Error al procesar los días seleccionados");
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida puntos del hábito
     */
    public static ValidationResult validatePoints(Integer points) {
        if (points == null) {
            return ValidationResult.error("Debes especificar los puntos");
        }
        if (points < 1) {
            return ValidationResult.error("Los puntos deben ser al menos 1");
        }
        if (points > 100) {
            return ValidationResult.error("Los puntos no pueden exceder 100");
        }
        return ValidationResult.success();
    }
    
    /**
     * Valida recordatorios (JSON array de horas)
     */
    public static ValidationResult validateReminderTimes(String reminderTimesJson) {
        if (reminderTimesJson == null || reminderTimesJson.trim().isEmpty()) {
            return ValidationResult.success(); // Opcional
        }
        try {
            JSONArray times = new JSONArray(reminderTimesJson);
            if (times.length() > 10) {
                return ValidationResult.error("No puedes tener más de 10 recordatorios");
            }
        } catch (JSONException e) {
            return ValidationResult.error("Error al procesar los recordatorios");
        }
        return ValidationResult.success();
    }
}
