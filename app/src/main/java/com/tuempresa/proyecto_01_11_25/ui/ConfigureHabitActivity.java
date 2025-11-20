package com.tuempresa.proyecto_01_11_25.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.tuempresa.proyecto_01_11_25.R;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper;
import com.tuempresa.proyecto_01_11_25.model.Habit;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ConfigureHabitActivity extends AppCompatActivity {

    private Habit.HabitType habitType;
    private HabitDatabaseHelper dbHelper;
    private LinearLayout containerConfig;
    private TextInputEditText edtHabitName;
    private MaterialButton btnSave;
    private long habitIdToEdit = -1; // -1 si es nuevo, >0 si es edición
    private Habit habitToEdit = null;
    private String selectedIconName = null; // Ícono seleccionado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_habit);

        dbHelper = new HabitDatabaseHelper(this);
        
        // Verificar si es edición
        habitIdToEdit = getIntent().getLongExtra("habit_id", -1);
        if (habitIdToEdit > 0) {
            habitToEdit = dbHelper.getHabitById(habitIdToEdit);
            if (habitToEdit != null) {
                habitType = habitToEdit.getType();
            } else {
                finish();
                return;
            }
        } else {
            String typeStr = getIntent().getStringExtra("habit_type");
            if (typeStr == null) {
                finish();
                return;
            }
            try {
                habitType = Habit.HabitType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                finish();
                return;
            }
        }

        containerConfig = findViewById(R.id.containerConfig);
        edtHabitName = findViewById(R.id.edtHabitName);
        btnSave = findViewById(R.id.btnSave);
        
        TextView titleView = findViewById(R.id.titleConfigure);
        if (habitIdToEdit > 0) {
            titleView.setText(getString(R.string.edit_habit_title) + ": " + habitToEdit.getTitle());
            edtHabitName.setText(habitToEdit.getTitle());
        } else {
            titleView.setText(getTitleForType(habitType));
        }
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveHabit());

        loadConfigurationForType(habitType);
        
        // Cargar selector de íconos
        loadIconSelector();
        
        // Si es edición, cargar valores existentes
        if (habitIdToEdit > 0) {
            loadExistingValues();
        }
    }
    
    /**
     * Carga el selector de íconos
     */
    private void loadIconSelector() {
        View iconSelectorView = getLayoutInflater().inflate(R.layout.selector_habit_icon, containerConfig, false);
        androidx.recyclerview.widget.RecyclerView rvIcons = iconSelectorView.findViewById(R.id.rvIcons);
        
        // Configurar RecyclerView con GridLayoutManager (3 columnas)
        androidx.recyclerview.widget.GridLayoutManager layoutManager = 
            new androidx.recyclerview.widget.GridLayoutManager(this, 3);
        rvIcons.setLayoutManager(layoutManager);
        
        // Obtener íconos disponibles
        com.tuempresa.proyecto_01_11_25.utils.HabitIconUtils.IconOption[] icons = 
            com.tuempresa.proyecto_01_11_25.utils.HabitIconUtils.getAvailableIcons();
        
        // Si es edición y tiene ícono, seleccionarlo por defecto
        if (habitIdToEdit > 0 && habitToEdit != null && habitToEdit.getHabitIcon() != null) {
            selectedIconName = habitToEdit.getHabitIcon();
        } else {
            // Usar ícono por defecto según el tipo
            selectedIconName = com.tuempresa.proyecto_01_11_25.utils.HabitIconUtils.getDefaultIconName(habitType);
        }
        
        // Crear adapter
        IconSelectorAdapter[] adapterRef = new IconSelectorAdapter[1]; // Array para referencia final
        IconSelectorAdapter adapter = new IconSelectorAdapter(
            java.util.Arrays.asList(icons),
            selectedIconName,
            iconName -> {
                selectedIconName = iconName;
                if (adapterRef[0] != null) {
                    adapterRef[0].setSelectedIcon(iconName);
                }
            }
        );
        adapterRef[0] = adapter; // Guardar referencia
        rvIcons.setAdapter(adapter);
        
        containerConfig.addView(iconSelectorView);
    }
    
    /**
     * Adapter para el selector de íconos
     */
    private static class IconSelectorAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<IconSelectorAdapter.ViewHolder> {
        private final java.util.List<com.tuempresa.proyecto_01_11_25.utils.HabitIconUtils.IconOption> icons;
        private String selectedIconName;
        private final OnIconSelectedListener listener;
        
        interface OnIconSelectedListener {
            void onIconSelected(String iconName);
        }
        
        IconSelectorAdapter(java.util.List<com.tuempresa.proyecto_01_11_25.utils.HabitIconUtils.IconOption> icons, 
                           String selectedIconName, OnIconSelectedListener listener) {
            this.icons = icons;
            this.selectedIconName = selectedIconName;
            this.listener = listener;
        }
        
        void setSelectedIcon(String iconName) {
            String oldSelected = selectedIconName;
            selectedIconName = iconName;
            // Notificar cambios para actualizar UI
            int oldPos = -1, newPos = -1;
            for (int i = 0; i < icons.size(); i++) {
                if (icons.get(i).getIconName().equals(oldSelected)) oldPos = i;
                if (icons.get(i).getIconName().equals(iconName)) newPos = i;
            }
            if (oldPos >= 0) notifyItemChanged(oldPos);
            if (newPos >= 0) notifyItemChanged(newPos);
        }
        
        @androidx.annotation.NonNull
        @Override
        public ViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            android.view.View view = android.view.LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit_icon_item, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@androidx.annotation.NonNull ViewHolder holder, int position) {
            com.tuempresa.proyecto_01_11_25.utils.HabitIconUtils.IconOption icon = icons.get(position);
            holder.imgIcon.setImageResource(icon.getDrawableId());
            holder.txtIconName.setText(icon.getDisplayName());
            
            // Marcar como seleccionado
            boolean isSelected = icon.getIconName().equals(selectedIconName);
            holder.cardIcon.setStrokeWidth(isSelected ? 4 : 2);
            holder.cardIcon.setStrokeColor(isSelected ? 
                androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), com.tuempresa.proyecto_01_11_25.R.color.orange) :
                android.graphics.Color.TRANSPARENT);
            
            holder.cardIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIconSelected(icon.getIconName());
                }
            });
        }
        
        @Override
        public int getItemCount() {
            return icons.size();
        }
        
        static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            com.google.android.material.card.MaterialCardView cardIcon;
            android.widget.ImageView imgIcon;
            TextView txtIconName;
            
            ViewHolder(@androidx.annotation.NonNull android.view.View itemView) {
                super(itemView);
                cardIcon = itemView.findViewById(R.id.cardIcon);
                imgIcon = itemView.findViewById(R.id.imgIcon);
                txtIconName = itemView.findViewById(R.id.txtIconName);
            }
        }
    }

    private String getTitleForType(Habit.HabitType type) {
        switch (type) {
            case READ_BOOK: return getString(R.string.configure_read_book_title);
            case VITAMINS: return getString(R.string.configure_vitamins_title);
            case MEDITATE: return getString(R.string.configure_meditate_title);
            case JOURNALING: return getString(R.string.configure_journaling_title);
            case GYM: return getString(R.string.configure_gym_title);
            case WATER: return getString(R.string.configure_water_title);
            case COLD_SHOWER: return getString(R.string.configure_cold_shower_title);
            case ENGLISH: return getString(R.string.configure_english_title);
            case CODING: return getString(R.string.configure_coding_title);
            default: return getString(R.string.create_habit_title);
        }
    }

    private void loadConfigurationForType(Habit.HabitType type) {
        containerConfig.removeAllViews();
        
        switch (type) {
            case READ_BOOK:
                loadReadBookConfig();
                break;
            case VITAMINS:
                loadVitaminsConfig();
                break;
            case MEDITATE:
                loadMeditateConfig();
                break;
            case JOURNALING:
                loadJournalingConfig();
                break;
            case GYM:
                loadGymConfig();
                break;
            case WATER:
                loadWaterConfig();
                break;
            case COLD_SHOWER:
                loadColdShowerConfig();
                break;
            case ENGLISH:
                loadEnglishConfig();
                break;
            case CODING:
                loadCodingConfig();
                break;
        }
    }

    private void loadReadBookConfig() {
        View view = getLayoutInflater().inflate(R.layout.config_read_book, containerConfig, false);
        
        TextInputEditText edtPagesPerDay = view.findViewById(R.id.edtPagesPerDay);
        TextInputEditText edtBookName = view.findViewById(R.id.edtBookName);
        MaterialButton btnDetectPages = view.findViewById(R.id.btnDetectPages);
        
        btnDetectPages.setOnClickListener(v -> {
            // Preparar para ML Kit (no implementar aún)
            Toast.makeText(this, "Función de detección de páginas próximamente", Toast.LENGTH_SHORT).show();
        });
        
        containerConfig.addView(view);
    }

    private void loadVitaminsConfig() {
        View view = getLayoutInflater().inflate(R.layout.config_vitamins, containerConfig, false);
        
        LinearLayout containerTimes = view.findViewById(R.id.containerTimes);
        MaterialButton btnAddTime = view.findViewById(R.id.btnAddTime);
        SwitchMaterial switchDaily = view.findViewById(R.id.switchDaily);
        
        btnAddTime.setOnClickListener(v -> {
            // Agregar selector de hora (simplificado por ahora)
            Toast.makeText(this, "Agregar horario próximamente", Toast.LENGTH_SHORT).show();
        });
        
        containerConfig.addView(view);
    }

    private void loadMeditateConfig() {
        View view = getLayoutInflater().inflate(R.layout.config_meditate, containerConfig, false);
        
        TextInputEditText edtDuration = view.findViewById(R.id.edtDuration);
        SwitchMaterial switchDnd = view.findViewById(R.id.switchDnd);
        Spinner spinnerMusic = view.findViewById(R.id.spinnerMusic);
        
        // Lista simple de música
        List<String> musicOptions = new ArrayList<>();
        musicOptions.add("Ninguna");
        musicOptions.add("Naturaleza");
        musicOptions.add("Meditación");
        musicOptions.add("Binaural");
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, musicOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMusic.setAdapter(adapter);
        
        containerConfig.addView(view);
    }

    private void loadJournalingConfig() {
        View view = getLayoutInflater().inflate(R.layout.config_journaling, containerConfig, false);
        
        TextView txtQuestion = view.findViewById(R.id.txtQuestion);
        txtQuestion.setText(getString(R.string.journal_question));
        
        containerConfig.addView(view);
    }

    private void loadGymConfig() {
        View view = getLayoutInflater().inflate(R.layout.config_gym, containerConfig, false);
        
        LinearLayout containerDays = view.findViewById(R.id.containerDays);
        String[] days = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        
        for (String day : days) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(day);
            checkBox.setPadding(0, 16, 0, 16);
            containerDays.addView(checkBox);
        }
        
        containerConfig.addView(view);
    }

    private void loadWaterConfig() {
        View view = getLayoutInflater().inflate(R.layout.config_water, containerConfig, false);
        
        TextInputEditText edtGlasses = view.findViewById(R.id.edtGlasses);
        edtGlasses.setHint(getString(R.string.water_glasses_hint));
        
        containerConfig.addView(view);
    }

    private void loadColdShowerConfig() {
        View view = getLayoutInflater().inflate(R.layout.config_cold_shower, containerConfig, false);
        
        TextView txtDesc = view.findViewById(R.id.txtDesc);
        txtDesc.setText(getString(R.string.one_click_complete_desc));
        
        containerConfig.addView(view);
    }

    private void loadEnglishConfig() {
        View view = getLayoutInflater().inflate(R.layout.config_english, containerConfig, false);
        containerConfig.addView(view);
    }

    private void loadCodingConfig() {
        View view = getLayoutInflater().inflate(R.layout.config_coding, containerConfig, false);
        containerConfig.addView(view);
    }

    private void saveHabit() {
        String name = edtHabitName.getText() != null ? edtHabitName.getText().toString().trim() : "";
        
        if (name.isEmpty()) {
            Toast.makeText(this, getString(R.string.habit_name_required), Toast.LENGTH_SHORT).show();
            return;
        }

        // Recopilar datos según el tipo
        Integer pagesPerDay = null;
        String reminderTimes = null;
        Integer durationMinutes = null;
        Boolean dndMode = null;
        Integer musicId = null;
        Boolean journalEnabled = null;
        String gymDays = null;
        Integer waterGoalGlasses = null;
        Boolean oneClickComplete = null;
        Boolean englishMode = null;
        Boolean codingMode = null;

        switch (habitType) {
            case READ_BOOK:
                pagesPerDay = getPagesPerDay();
                break;
            case VITAMINS:
                reminderTimes = getReminderTimes();
                break;
            case MEDITATE:
                durationMinutes = getDurationMinutes();
                dndMode = getDndMode();
                musicId = getMusicId();
                break;
            case JOURNALING:
                journalEnabled = true;
                break;
            case GYM:
                gymDays = getGymDays();
                break;
            case WATER:
                waterGoalGlasses = getWaterGlasses();
                if (waterGoalGlasses == null || waterGoalGlasses <= 0) {
                    Toast.makeText(this, getString(R.string.invalid_water_glasses), Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
            case COLD_SHOWER:
                oneClickComplete = true;
                break;
            case ENGLISH:
                englishMode = true;
                break;
            case CODING:
                codingMode = true;
                break;
        }

        // Guardar o actualizar en base de datos
        boolean success;
        if (habitIdToEdit > 0) {
            // Actualizar hábito existente
            success = dbHelper.updateHabitFull(
                habitIdToEdit,
                name,
                getGoalForType(habitType),
                getCategoryForType(habitType),
                habitType.name(),
                10, // puntos por defecto
                null, null, // targetValue, targetUnit
                pagesPerDay, reminderTimes, durationMinutes,
                dndMode, musicId, journalEnabled,
                gymDays, waterGoalGlasses, oneClickComplete,
                englishMode, codingMode, selectedIconName
            );
            if (success) {
                Toast.makeText(this, getString(R.string.habit_updated_toast), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, getString(R.string.habit_update_error_toast), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Crear nuevo hábito
            long id = dbHelper.insertHabitFull(
                name,
                getGoalForType(habitType),
                getCategoryForType(habitType),
                habitType.name(),
                10, // puntos por defecto
                null, null, // targetValue, targetUnit
                pagesPerDay, reminderTimes, durationMinutes,
                dndMode, musicId, journalEnabled,
                gymDays, waterGoalGlasses, oneClickComplete,
                englishMode, codingMode, selectedIconName
            );

            if (id > 0) {
                Toast.makeText(this, getString(R.string.habit_saved_toast), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, getString(R.string.habit_save_error_toast), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Carga los valores existentes del hábito para edición
     */
    private void loadExistingValues() {
        if (habitToEdit == null) return;
        
        View configView = containerConfig.getChildAt(0);
        if (configView == null) return;
        
        switch (habitType) {
            case READ_BOOK:
                TextInputEditText edtPages = configView.findViewById(R.id.edtPagesPerDay);
                if (edtPages != null && habitToEdit.getPagesPerDay() != null) {
                    edtPages.setText(String.valueOf(habitToEdit.getPagesPerDay()));
                }
                break;
            case WATER:
                TextInputEditText edtGlasses = configView.findViewById(R.id.edtGlasses);
                if (edtGlasses != null && habitToEdit.getWaterGoalGlasses() != null) {
                    edtGlasses.setText(String.valueOf(habitToEdit.getWaterGoalGlasses()));
                }
                break;
            case MEDITATE:
                TextInputEditText edtDuration = configView.findViewById(R.id.edtDuration);
                if (edtDuration != null && habitToEdit.getDurationMinutes() != null) {
                    edtDuration.setText(String.valueOf(habitToEdit.getDurationMinutes()));
                }
                SwitchMaterial switchDnd = configView.findViewById(R.id.switchDnd);
                if (switchDnd != null && habitToEdit.getDndMode() != null) {
                    switchDnd.setChecked(habitToEdit.getDndMode());
                }
                Spinner spinnerMusic = configView.findViewById(R.id.spinnerMusic);
                if (spinnerMusic != null && habitToEdit.getMusicId() != null) {
                    spinnerMusic.setSelection(habitToEdit.getMusicId());
                }
                break;
            case GYM:
                // Cargar días seleccionados desde JSON
                if (habitToEdit.getGymDays() != null) {
                    try {
                        JSONArray daysArray = new JSONArray(habitToEdit.getGymDays());
                        LinearLayout containerDays = configView.findViewById(R.id.containerDays);
                        if (containerDays != null) {
                            for (int i = 0; i < containerDays.getChildCount() && i < daysArray.length(); i++) {
                                View child = containerDays.getChildAt(i);
                                if (child instanceof CheckBox) {
                                    int dayIndex = daysArray.getInt(i);
                                    if (dayIndex == i) {
                                        ((CheckBox) child).setChecked(true);
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        // Ignorar error
                    }
                }
                break;
        }
    }

    private String getGoalForType(Habit.HabitType type) {
        switch (type) {
            case READ_BOOK: return "Leer páginas diarias";
            case VITAMINS: return "Tomar vitaminas";
            case MEDITATE: return "Meditar";
            case JOURNALING: return "Escribir en el diario";
            case GYM: return "Ir al gym";
            case WATER: return "Beber agua";
            case COLD_SHOWER: return "Ducha fría";
            case ENGLISH: return "Practicar inglés";
            case CODING: return "Practicar coding";
            default: return "Hábito personalizado";
        }
    }

    private String getCategoryForType(Habit.HabitType type) {
        switch (type) {
            case READ_BOOK:
            case JOURNALING:
            case ENGLISH:
            case CODING:
                return "educación";
            case VITAMINS:
            case MEDITATE:
            case GYM:
            case WATER:
            case COLD_SHOWER:
                return "salud";
            default:
                return "general";
        }
    }

    private Integer getPagesPerDay() {
        View configView = containerConfig.getChildAt(0);
        if (configView != null) {
            TextInputEditText edt = configView.findViewById(R.id.edtPagesPerDay);
            if (edt != null && edt.getText() != null) {
                try {
                    return Integer.parseInt(edt.getText().toString().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private String getReminderTimes() {
        // Por ahora retornar JSON vacío, se implementará después
        return "[]";
    }

    private Integer getDurationMinutes() {
        View configView = containerConfig.getChildAt(0);
        if (configView != null) {
            TextInputEditText edt = configView.findViewById(R.id.edtDuration);
            if (edt != null && edt.getText() != null) {
                try {
                    return Integer.parseInt(edt.getText().toString().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private Boolean getDndMode() {
        View configView = containerConfig.getChildAt(0);
        if (configView != null) {
            SwitchMaterial sw = configView.findViewById(R.id.switchDnd);
            return sw != null && sw.isChecked();
        }
        return false;
    }

    private Integer getMusicId() {
        View configView = containerConfig.getChildAt(0);
        if (configView != null) {
            Spinner spinner = configView.findViewById(R.id.spinnerMusic);
            if (spinner != null) {
                return spinner.getSelectedItemPosition();
            }
        }
        return 0;
    }

    private String getGymDays() {
        View configView = containerConfig.getChildAt(0);
        if (configView != null) {
            LinearLayout containerDays = configView.findViewById(R.id.containerDays);
            if (containerDays != null) {
                JSONArray days = new JSONArray();
                for (int i = 0; i < containerDays.getChildCount(); i++) {
                    View child = containerDays.getChildAt(i);
                    if (child instanceof CheckBox && ((CheckBox) child).isChecked()) {
                        days.put(i);
                    }
                }
                return days.toString();
            }
        }
        return "[]";
    }

    private Integer getWaterGlasses() {
        View configView = containerConfig.getChildAt(0);
        if (configView != null) {
            TextInputEditText edt = configView.findViewById(R.id.edtGlasses);
            if (edt != null && edt.getText() != null) {
                try {
                    return Integer.parseInt(edt.getText().toString().trim());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }
}

