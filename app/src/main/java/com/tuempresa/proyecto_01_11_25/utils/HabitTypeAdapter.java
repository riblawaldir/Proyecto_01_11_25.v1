package com.tuempresa.proyecto_01_11_25.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.tuempresa.proyecto_01_11_25.model.Habit;

import java.io.IOException;

/**
 * Adaptador personalizado de Gson para serializar/deserializar HabitType enum.
 * Convierte el enum a String y viceversa para la comunicación con la API.
 */
public class HabitTypeAdapter extends TypeAdapter<Habit.HabitType> {

    @Override
    public void write(JsonWriter out, Habit.HabitType value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.name());
        }
    }

    @Override
    public Habit.HabitType read(JsonReader in) throws IOException {
        String value = in.nextString();
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Habit.HabitType.valueOf(value);
        } catch (IllegalArgumentException e) {
            // Si el valor no coincide con ningún enum, retornar null o un valor por defecto
            return Habit.HabitType.DEMO;
        }
    }
}

