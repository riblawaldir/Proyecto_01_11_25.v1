package com.tuempresa.proyecto_01_11_25.utils;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Adaptador flexible para parsear fechas en diferentes formatos.
 * Maneja fechas ISO 8601 con y sin zona horaria.
 */
public class FlexibleDateAdapter extends TypeAdapter<Date> {
    
    // Formatos de fecha que intentaremos parsear
    private static final String[] DATE_FORMATS = {
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS",  // Formato de la API: 2025-12-02T15:04:55.1293097
        "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",   // Con 6 dígitos de microsegundos
        "yyyy-MM-dd'T'HH:mm:ss.SSSSS",    // Con 5 dígitos
        "yyyy-MM-dd'T'HH:mm:ss.SSSS",     // Con 4 dígitos
        "yyyy-MM-dd'T'HH:mm:ss.SSS",      // Con 3 dígitos (milisegundos)
        "yyyy-MM-dd'T'HH:mm:ss.SS",       // Con 2 dígitos
        "yyyy-MM-dd'T'HH:mm:ss.S",        // Con 1 dígito
        "yyyy-MM-dd'T'HH:mm:ss",          // Sin fracciones de segundo
        "yyyy-MM-dd'T'HH:mm:ss'Z'",       // Con Z al final
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",   // Con milisegundos y Z
        "yyyy-MM-dd'T'HH:mm:ssXXX",       // Con offset de zona horaria (+00:00)
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",  // Con milisegundos y offset
    };
    
    @Override
    public void write(JsonWriter out, Date value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }
        // Escribir en formato ISO 8601 estándar
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        out.value(sdf.format(value));
    }
    
    @Override
    public Date read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        
        String dateString = in.nextString();
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        
        // Intentar parsear con cada formato
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);
                // Si el formato no tiene zona horaria, asumir UTC
                if (!format.contains("XXX") && !format.contains("'Z'")) {
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                Date date = sdf.parse(dateString);
                if (date != null) {
                    return date;
                }
            } catch (ParseException e) {
                // Continuar con el siguiente formato
                continue;
            }
        }
        
        // Si ningún formato funcionó, intentar parsear como timestamp (milisegundos)
        try {
            long timestamp = Long.parseLong(dateString);
            return new Date(timestamp);
        } catch (NumberFormatException e) {
            // Si tampoco es un número, lanzar excepción
            throw new IOException("No se pudo parsear la fecha: " + dateString, e);
        }
    }
}

