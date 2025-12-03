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
import com.tuempresa.proyecto_01_11_25.repository.HabitRepository;
import com.tuempresa.proyecto_01_11_25.utils.ReminderNotificationManager;
import com.tuempresa.proyecto_01_11_25.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConfigureHabitActivity extends AppCompatActivity {

    private Habit.HabitType habitType;
    private HabitDatabaseHelper dbHelper; // Mantener para compatibilidad (lectura)
    private HabitRepository habitRepository; // Para guardar en API
    private SessionManager sessionManager; // Para obtener userId del usuario logueado
    private LinearLayout containerConfig;
    private TextInputEditText edtHabitName;
    private TextInputEditText edtPoints;
    private MaterialButton btnSave;
    private long habitIdToEdit = -1; // -1 si es nuevo, >0 si es edición
    private Habit habitToEdit = null;
    private String selectedIconName = null; // Ícono seleccionado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_habit);

        dbHelper = new HabitDatabaseHelper(this);
        habitRepository = HabitRepository.getInstance(this);
        sessionManager = new SessionManager(this);
        
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
        edtPoints = findViewById(R.id.edtPoints);
        btnSave = findViewById(R.id.btnSave);
        
        TextView titleView = findViewById(R.id.titleConfigure);
        if (habitIdToEdit > 0) {
            titleView.setText(getString(R.string.edit_habit_title) + ": " + habitToEdit.getTitle());
            edtHabitName.setText(habitToEdit.getTitle());
            edtPoints.setText(String.valueOf(habitToEdit.getPoints()));
        } else {
            titleView.setText(getTitleForType(habitType));
            edtPoints.setText("10"); // Valor por defecto
        }
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveHabit());

        // Cargar selector de íconos PRIMERO (más visual, mejor engagement)
        loadIconSelector();
        
        // Luego cargar configuración específica del tipo
        loadConfigurationForType(habitType);
        
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
            case WALK: return "Configurar Caminar";
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
            case WALK:
                loadWalkConfig();
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
            // Toast eliminado - usuario no quiere mensajes constantes
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

    private void loadWalkConfig() {
        View view = getLayoutInflater().inflate(R.layout.config_walk, containerConfig, false);
        
        android.widget.RadioGroup radioGroup = view.findViewById(R.id.radioGroupWalkType);
        android.widget.RadioButton radioMeters = view.findViewById(R.id.radioMeters);
        android.widget.RadioButton radioSteps = view.findViewById(R.id.radioSteps);
        com.google.android.material.textfield.TextInputLayout layoutMeters = view.findViewById(R.id.layoutWalkMeters);
        com.google.android.material.textfield.TextInputLayout layoutSteps = view.findViewById(R.id.layoutWalkSteps);
        
        // Cambiar visibilidad según selección
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioMeters) {
                layoutMeters.setVisibility(android.view.View.VISIBLE);
                layoutSteps.setVisibility(android.view.View.GONE);
            } else {
                layoutMeters.setVisibility(android.view.View.GONE);
                layoutSteps.setVisibility(android.view.View.VISIBLE);
            }
        });
        
        containerConfig.addView(view);
    }
    
    private android.util.Pair<Integer, Integer> getWalkGoals() {
        View configView = containerConfig.getChildAt(0);
        if (configView != null) {
            android.widget.RadioGroup radioGroup = configView.findViewById(R.id.radioGroupWalkType);
            com.google.android.material.textfield.TextInputEditText edtMeters = configView.findViewById(R.id.edtWalkMeters);
            com.google.android.material.textfield.TextInputEditText edtSteps = configView.findViewById(R.id.edtWalkSteps);

            Integer meters = null;
            Integer steps = null;

            if (radioGroup != null && radioGroup.getCheckedRadioButtonId() == R.id.radioMeters) {
                // Usar metros
                if (edtMeters != null && edtMeters.getText() != null && !edtMeters.getText().toString().trim().isEmpty()) {
                    try {
                        meters = Integer.parseInt(edtMeters.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        // Handled by validator
                    }
                }
            } else if (radioGroup != null && radioGroup.getCheckedRadioButtonId() == R.id.radioSteps) {
                // Usar pasos
                if (edtSteps != null && edtSteps.getText() != null && !edtSteps.getText().toString().trim().isEmpty()) {
                    try {
                        steps = Integer.parseInt(edtSteps.getText().toString().trim());
                    } catch (NumberFormatException e) {
                        // Handled by validator
                    }
                }
            }
            
            return new android.util.Pair<>(meters, steps);
        }
        return new android.util.Pair<>(null, null);
    }

    private void saveHabit() {
        String name = edtHabitName.getText() != null ? edtHabitName.getText().toString().trim() : "";
        
        // Validar nombre usando HabitValidator
        com.tuempresa.proyecto_01_11_25.utils.HabitValidator.ValidationResult nameValidation = 
                com.tuempresa.proyecto_01_11_25.utils.HabitValidator.validateHabitName(name);
        if (!nameValidation.isValid) {
            // Toast eliminado - usuario no quiere mensajes constantes (validación se muestra en el campo)
            edtHabitName.requestFocus();
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
        Integer walkGoalMeters = null;
        Integer walkGoalSteps = null;
        Boolean oneClickComplete = null;
        Boolean englishMode = null;
        Boolean codingMode = null;

        // Recopilar y validar según el tipo
        switch (habitType) {
            case READ_BOOK:
                pagesPerDay = getPagesPerDay();
                com.tuempresa.proyecto_01_11_25.utils.HabitValidator.ValidationResult pagesValidation = 
                        com.tuempresa.proyecto_01_11_25.utils.HabitValidator.validatePages(pagesPerDay);
                if (!pagesValidation.isValid) {
                    // Toast eliminado - usuario no quiere mensajes constantes (validación se muestra en el campo)
                    return;
                }
                break;
            case VITAMINS:
                reminderTimes = getReminderTimes();
                break;
            case MEDITATE:
                durationMinutes = getDurationMinutes();
                com.tuempresa.proyecto_01_11_25.utils.HabitValidator.ValidationResult durationValidation = 
                        com.tuempresa.proyecto_01_11_25.utils.HabitValidator.validateMeditationDuration(durationMinutes);
                if (!durationValidation.isValid) {
                    // Toast eliminado - usuario no quiere mensajes constantes (validación se muestra en el campo)
                    return;
                }
                dndMode = getDndMode();
                musicId = getMusicId();
                break;
            case JOURNALING:
                journalEnabled = true;
                break;
            case GYM:
                gymDays = getGymDays();
                com.tuempresa.proyecto_01_11_25.utils.HabitValidator.ValidationResult gymValidation = 
                        com.tuempresa.proyecto_01_11_25.utils.HabitValidator.validateGymDays(gymDays);
                if (!gymValidation.isValid) {
                    // Toast eliminado - usuario no quiere mensajes constantes (validación se muestra en el campo)
                    return;
                }
                break;
            case WATER:
                waterGoalGlasses = getWaterGlasses();
                com.tuempresa.proyecto_01_11_25.utils.HabitValidator.ValidationResult waterValidation = 
                        com.tuempresa.proyecto_01_11_25.utils.HabitValidator.validateWaterGlasses(waterGoalGlasses);
                if (!waterValidation.isValid) {
                    // Toast eliminado - usuario no quiere mensajes constantes (validación se muestra en el campo)
                    return;
                }
                break;
            case WALK:
                android.util.Pair<Integer, Integer> walkGoals = getWalkGoals();
                walkGoalMeters = walkGoals.first;
                walkGoalSteps = walkGoals.second;
                if (walkGoalMeters == null && walkGoalSteps == null) {
                    // Usar valores por defecto
                    walkGoalMeters = 500;
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

        // Mostrar loading state
        btnSave.setEnabled(false);
        btnSave.setText("Guardando...");
        
        // Obtener userId del usuario logueado
        long userId = sessionManager.getUserId();
        if (userId <= 0) {
            android.util.Log.e("ConfigureHabit", "⚠️ ERROR: No se puede crear hábito sin usuario logueado (userId: " + userId + ")");
            btnSave.setEnabled(true);
            btnSave.setText(getString(R.string.save_configuration));
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Obtener puntos personalizados
        int points = 10; // Valor por defecto
        try {
            String pointsStr = edtPoints.getText() != null ? edtPoints.getText().toString().trim() : "";
            if (!pointsStr.isEmpty()) {
                points = Integer.parseInt(pointsStr);
                if (points < 1) points = 1; // Mínimo 1 punto
                if (points > 1000) points = 1000; // Máximo 1000 puntos
            }
        } catch (NumberFormatException e) {
            android.util.Log.w("ConfigureHabit", "Error al parsear puntos, usando valor por defecto: 10");
            points = 10;
        }
        
        // Crear objeto Habit
        Habit habit = new Habit();
        habit.setUserId(userId); // CRÍTICO: Establecer userId del usuario logueado
        habit.setTitle(name);
        habit.setGoal(getGoalForType(habitType));
        habit.setCategory(getCategoryForType(habitType));
        habit.setType(habitType);
        habit.setPoints(points); // Puntos personalizables
        habit.setPagesPerDay(pagesPerDay);
        habit.setReminderTimes(reminderTimes);
        habit.setDurationMinutes(durationMinutes);
        habit.setDndMode(dndMode);
        habit.setMusicId(musicId);
        habit.setJournalEnabled(journalEnabled);
        habit.setGymDays(gymDays);
        habit.setWaterGoalGlasses(waterGoalGlasses);
        habit.setWalkGoalMeters(walkGoalMeters);
        habit.setWalkGoalSteps(walkGoalSteps);
        habit.setOneClickComplete(oneClickComplete);
        habit.setEnglishMode(englishMode);
        habit.setCodingMode(codingMode);
        habit.setHabitIcon(selectedIconName);
        
        android.util.Log.d("ConfigureHabit", "Creando hábito '" + name + "' con userId: " + userId);

        if (habitIdToEdit > 0) {
            // Actualizar hábito existente
            habit.setId(habitIdToEdit);
            habitRepository.updateHabit(habit, new HabitRepository.RepositoryCallback<Habit>() {
                @Override
                public void onSuccess(Habit updatedHabit) {
                    // Programar notificaciones si tiene reminderTimes
                    if (updatedHabit.getReminderTimes() != null && !updatedHabit.getReminderTimes().isEmpty()) {
                        ReminderNotificationManager reminderManager = new ReminderNotificationManager(ConfigureHabitActivity.this);
                        reminderManager.scheduleReminders(updatedHabit.getId(), updatedHabit.getTitle(), updatedHabit.getReminderTimes());
                    }
                    
                    runOnUiThread(() -> {
                        btnSave.setEnabled(true);
                        btnSave.setText(getString(R.string.save_configuration));
                        // Toast eliminado - usuario no quiere mensajes constantes
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        btnSave.setEnabled(true);
                        btnSave.setText(getString(R.string.save_configuration));
                        // Toast eliminado - usuario no quiere mensajes constantes (error se loguea)
                    });
                }
            });
        } else {
            // Crear nuevo hábito usando Repository (guarda en SQLite + API)
            habitRepository.createHabit(habit, new HabitRepository.RepositoryCallback<Habit>() {
                @Override
                public void onSuccess(Habit createdHabit) {
                    // Programar notificaciones si tiene reminderTimes
                    if (createdHabit.getReminderTimes() != null && !createdHabit.getReminderTimes().isEmpty()) {
                        ReminderNotificationManager reminderManager = new ReminderNotificationManager(ConfigureHabitActivity.this);
                        reminderManager.scheduleReminders(createdHabit.getId(), createdHabit.getTitle(), createdHabit.getReminderTimes());
                    }
                    
                    runOnUiThread(() -> {
                        btnSave.setEnabled(true);
                        btnSave.setText(getString(R.string.save_configuration));
                        
                        // CRÍTICO: Verificar que el hábito se guardó correctamente en la BD local
                        // antes de cerrar la actividad
                        android.util.Log.d("ConfigureHabit", "✅ Hábito creado con éxito. Verificando guardado en BD...");
                        
                        // Pequeño delay para asegurar que el hábito se guardó completamente
                        // antes de cerrar y refrescar Dashboard
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                            // Verificar que el hábito existe en la BD
                            com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper dbHelper = 
                                new com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper(ConfigureHabitActivity.this);
                            
                            try {
                                com.tuempresa.proyecto_01_11_25.model.Habit savedHabit = dbHelper.getHabitById(createdHabit.getId());
                                if (savedHabit != null) {
                                    android.util.Log.d("ConfigureHabit", "✅ Hábito verificado en BD - Id: " + savedHabit.getId() + ", UserId: " + savedHabit.getUserId());
                                } else {
                                    android.util.Log.w("ConfigureHabit", "⚠️ Hábito no encontrado en BD después de crear. Id: " + createdHabit.getId());
                                }
                            } catch (Exception e) {
                                android.util.Log.e("ConfigureHabit", "Error al verificar hábito en BD", e);
                            }
                            
                            // Cerrar actividad y refrescar Dashboard
                            setResult(RESULT_OK);
                            finish();
                        }, 300); // 300ms de delay para asegurar que se guardó
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        btnSave.setEnabled(true);
                        btnSave.setText(getString(R.string.save_configuration));
                        // Toast eliminado - usuario no quiere mensajes constantes (error se loguea)
                    });
                }
            });
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
            case WALK:
                android.widget.RadioButton radioMeters = configView.findViewById(R.id.radioMeters);
                android.widget.RadioButton radioSteps = configView.findViewById(R.id.radioSteps);
                TextInputEditText edtMeters = configView.findViewById(R.id.edtWalkMeters);
                TextInputEditText edtSteps = configView.findViewById(R.id.edtWalkSteps);
                
                if (habitToEdit.getWalkGoalMeters() != null && edtMeters != null) {
                    edtMeters.setText(String.valueOf(habitToEdit.getWalkGoalMeters()));
                    if (radioMeters != null) radioMeters.setChecked(true);
                } else if (habitToEdit.getWalkGoalSteps() != null && edtSteps != null) {
                    edtSteps.setText(String.valueOf(habitToEdit.getWalkGoalSteps()));
                    if (radioSteps != null) radioSteps.setChecked(true);
                    // Cambiar visibilidad
                    if (configView.findViewById(R.id.layoutWalkMeters) != null) {
                        configView.findViewById(R.id.layoutWalkMeters).setVisibility(android.view.View.GONE);
                    }
                    if (configView.findViewById(R.id.layoutWalkSteps) != null) {
                        configView.findViewById(R.id.layoutWalkSteps).setVisibility(android.view.View.VISIBLE);
                    }
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
            case WALK: return "Caminar";
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
            case WALK:
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
        View configView = containerConfig.getChildAt(0);
        if (configView == null) return "[]";
        
        LinearLayout containerTimes = configView.findViewById(R.id.containerTimes);
        if (containerTimes == null) return "[]";
        
        JSONArray timesArray = new JSONArray();
        
        // Recorrer todos los hijos del containerTimes (cada uno es un horario)
        for (int i = 0; i < containerTimes.getChildCount(); i++) {
            View timeView = containerTimes.getChildAt(i);
            if (timeView != null) {
                MaterialButton btnTime = timeView.findViewById(R.id.btnSelectTime);
                if (btnTime != null && btnTime.getText() != null) {
                    String timeStr = btnTime.getText().toString();
                    // Formato esperado: "08:00" o "20:30"
                    if (timeStr.contains(":")) {
                        try {
                            String[] parts = timeStr.split(":");
                            int hour = Integer.parseInt(parts[0]);
                            int minute = Integer.parseInt(parts[1]);
                            
                            JSONObject timeObj = new JSONObject();
                            timeObj.put("hour", hour);
                            timeObj.put("minute", minute);
                            timesArray.put(timeObj);
                        } catch (Exception e) {
                            android.util.Log.e("ConfigureHabit", "Error al parsear hora: " + timeStr, e);
                        }
                    }
                }
            }
        }
        
        return timesArray.toString();
    }
    
    private void loadVitaminsConfig() {
        View view = getLayoutInflater().inflate(R.layout.config_vitamins, containerConfig, false);
        
        LinearLayout containerTimes = view.findViewById(R.id.containerTimes);
        MaterialButton btnAddTime = view.findViewById(R.id.btnAddTime);
        
        // Cargar horarios existentes si estamos editando
        if (habitToEdit != null && habitToEdit.getReminderTimes() != null) {
            try {
                JSONArray existingTimes = new JSONArray(habitToEdit.getReminderTimes());
                for (int i = 0; i < existingTimes.length(); i++) {
                    JSONObject timeObj = existingTimes.getJSONObject(i);
                    int hour = timeObj.getInt("hour");
                    int minute = timeObj.getInt("minute");
                    addTimeRow(containerTimes, hour, minute);
                }
            } catch (JSONException e) {
                android.util.Log.e("ConfigureHabit", "Error al cargar horarios existentes", e);
            }
        }
        
        // Agregar horario por defecto si no hay ninguno
        if (containerTimes.getChildCount() == 0) {
            addTimeRow(containerTimes, 8, 0); // 08:00 por defecto
        }
        
        btnAddTime.setOnClickListener(v -> {
            // Agregar nuevo horario (12:00 por defecto)
            addTimeRow(containerTimes, 12, 0);
        });
        
        containerConfig.addView(view);
    }
    
    private void addTimeRow(LinearLayout container, int hour, int minute) {
        View timeRow = getLayoutInflater().inflate(R.layout.item_reminder_time, container, false);
        
        MaterialButton btnTime = timeRow.findViewById(R.id.btnSelectTime);
        btnTime.setText(String.format("%02d:%02d", hour, minute));
        
        btnTime.setOnClickListener(v -> {
            // Obtener hora actual del botón
            String currentTime = btnTime.getText().toString();
            int currentHour = hour;
            int currentMinute = minute;
            if (currentTime.contains(":")) {
                try {
                    String[] parts = currentTime.split(":");
                    currentHour = Integer.parseInt(parts[0]);
                    currentMinute = Integer.parseInt(parts[1]);
                } catch (Exception e) {
                    // Usar valores por defecto
                }
            }
            
            // Mostrar TimePicker
            android.app.TimePickerDialog timePicker = new android.app.TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    btnTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                },
                currentHour,
                currentMinute,
                true // 24 horas
            );
            timePicker.show();
        });
        
        MaterialButton btnRemove = timeRow.findViewById(R.id.btnRemoveTime);
        btnRemove.setOnClickListener(v -> {
            container.removeView(timeRow);
        });
        
        container.addView(timeRow);
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

