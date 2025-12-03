package com.tuempresa.proyecto_01_11_25.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.tuempresa.proyecto_01_11_25.R;
import com.tuempresa.proyecto_01_11_25.model.Habit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.VH> {

    public interface OnHabitClick {
        void onHabitClicked(Habit habit);
    }
    
    public interface OnHabitEdit {
        void onHabitEdit(Habit habit);
    }
    
    public interface OnHabitDelete {
        void onHabitDelete(Habit habit);
    }
    
    public interface OnHabitQuickComplete {
        void onHabitQuickComplete(Habit habit);
    }

    private final List<Habit> data;
    private final OnHabitClick listener;
    private final OnHabitEdit editListener;
    private final OnHabitDelete deleteListener;
    private final OnHabitQuickComplete quickCompleteListener;

    public HabitAdapter(List<Habit> data, OnHabitClick listener) {
        this(data, listener, null, null, null);
    }
    
    public HabitAdapter(List<Habit> data, OnHabitClick listener, OnHabitEdit editListener, OnHabitDelete deleteListener) {
        this(data, listener, editListener, deleteListener, null);
    }
    
    public HabitAdapter(List<Habit> data, OnHabitClick listener, OnHabitEdit editListener, OnHabitDelete deleteListener, OnHabitQuickComplete quickCompleteListener) {
        this.data = data;
        this.listener = listener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
        this.quickCompleteListener = quickCompleteListener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_habit_card, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Habit item = data.get(pos);
        h.txtName.setText(item.getTitle());
        
        // Mostrar meta con valor objetivo si existe
        String goalText = item.getGoal();
        
        // Mostrar progreso específico según el tipo
        int progressValue = 0;
        boolean isCompleted = item.isCompleted();
        
        if (item.getType() == Habit.HabitType.READ_BOOK && item.getPagesPerDay() != null) {
            int pagesGoal = item.getPagesPerDay();
            int pagesRead = getTodayProgress(h.itemView.getContext(), "read_" + item.getId(), 0);
            progressValue = pagesGoal > 0 ? (pagesRead * 100 / pagesGoal) : 0;
            if (progressValue > 100) progressValue = 100;
            goalText = goalText + " (" + pagesRead + "/" + pagesGoal + " páginas)";
            // Si alcanzó la meta, marcar como completado
            if (pagesRead >= pagesGoal && !isCompleted) {
                isCompleted = true;
            }
        } else if (item.getType() == Habit.HabitType.WATER && item.getWaterGoalGlasses() != null) {
            int glassesGoal = item.getWaterGoalGlasses();
            int glassesDrunk = getTodayProgress(h.itemView.getContext(), "water_" + item.getId(), 0);
            progressValue = glassesGoal > 0 ? (glassesDrunk * 100 / glassesGoal) : 0;
            if (progressValue > 100) progressValue = 100;
            goalText = goalText + " (" + glassesDrunk + "/" + glassesGoal + " vasos)";
            // Si alcanzó la meta, marcar como completado
            if (glassesDrunk >= glassesGoal && !isCompleted) {
                isCompleted = true;
            }
        } else if (item.getType() == Habit.HabitType.WALK) {
            // Mostrar progreso de caminar (metros o pasos)
            if (item.getWalkGoalSteps() != null && item.getWalkGoalSteps() > 0) {
                int stepsGoal = item.getWalkGoalSteps();
                int stepsWalked = getTodayProgress(h.itemView.getContext(), "walk_steps_" + item.getId(), 0);
                progressValue = stepsGoal > 0 ? (stepsWalked * 100 / stepsGoal) : 0;
                if (progressValue > 100) progressValue = 100;
                goalText = goalText + " (" + stepsWalked + "/" + stepsGoal + " pasos)";
                if (stepsWalked >= stepsGoal && !isCompleted) {
                    isCompleted = true;
                }
            } else if (item.getWalkGoalMeters() != null && item.getWalkGoalMeters() > 0) {
                int metersGoal = item.getWalkGoalMeters();
                int metersWalked = getTodayProgress(h.itemView.getContext(), "walk_meters_" + item.getId(), 0);
                progressValue = metersGoal > 0 ? (metersWalked * 100 / metersGoal) : 0;
                if (progressValue > 100) progressValue = 100;
                goalText = goalText + " (" + metersWalked + "/" + metersGoal + " m)";
                if (metersWalked >= metersGoal && !isCompleted) {
                    isCompleted = true;
                }
            } else {
                // Sin meta configurada, usar valor por defecto
                int defaultMeters = 500;
                int metersWalked = getTodayProgress(h.itemView.getContext(), "walk_meters_" + item.getId(), 0);
                progressValue = defaultMeters > 0 ? (metersWalked * 100 / defaultMeters) : 0;
                if (progressValue > 100) progressValue = 100;
                goalText = goalText + " (" + metersWalked + "/" + defaultMeters + " m)";
                if (metersWalked >= defaultMeters && !isCompleted) {
                    isCompleted = true;
                }
            }
        } else if (item.getTargetValue() > 0 && item.getTargetUnit() != null) {
            goalText = goalText + " (" + item.getTargetValue() + " " + item.getTargetUnit() + ")";
            progressValue = isCompleted ? 100 : 0; // Mostrar 0% si no está completado (más claro)
        } else {
            // Para hábitos sin progreso parcial, mostrar 0% si no está completado
            progressValue = isCompleted ? 100 : 0;
        }
        
        h.txtGoal.setText(goalText);
        
        // Configurar progreso y color según estado
        h.progress.setProgress(progressValue);
        
        // Mostrar información: categoría, puntos y progreso
        String typeText = item.getCategory();
        if (item.getPoints() > 0) {
            typeText = typeText + " • " + item.getPoints() + " pts";
        }
        // Agregar porcentaje de progreso
        String progressText = progressValue + "%";
        if (isCompleted) {
            progressText = "✓ " + progressText;
        }
        typeText = typeText + " • " + progressText;
        h.txtType.setText(typeText);
        
        // Cambiar color de la barra: verde si está completado, naranja si no
        if (isCompleted || progressValue >= 100) {
            // Barra verde para tareas completadas
            h.progress.setProgressDrawable(
                ContextCompat.getDrawable(
                    h.itemView.getContext(), 
                    R.drawable.progress_circle_drawable_green
                )
            );
            h.txtType.setTextColor(ContextCompat.getColor(h.itemView.getContext(), android.R.color.holo_green_dark));
        } else {
            // Barra naranja para tareas pendientes
            h.progress.setProgressDrawable(
                ContextCompat.getDrawable(
                    h.itemView.getContext(), 
                    R.drawable.progress_circle_drawable
                )
            );
            h.txtType.setTextColor(ContextCompat.getColor(h.itemView.getContext(), R.color.orangeEnd));
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onHabitClicked(item);
        });
        
        // Botón editar
        if (h.btnEdit != null) {
            h.btnEdit.setOnClickListener(v -> {
                if (editListener != null) {
                    editListener.onHabitEdit(item);
                }
            });
        }
        
        // Botón eliminar
        if (h.btnDelete != null) {
            h.btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onHabitDelete(item);
                }
            });
        }
        
        // Botón de completado rápido (solo visible si no está completado y es un tipo que permite completado rápido)
        if (h.btnQuickComplete != null) {
            boolean canQuickComplete = !isCompleted && canQuickCompleteHabit(item);
            h.btnQuickComplete.setVisibility(canQuickComplete ? View.VISIBLE : View.GONE);
            
            if (canQuickComplete) {
                h.btnQuickComplete.setOnClickListener(v -> {
                    if (quickCompleteListener != null) {
                        quickCompleteListener.onHabitQuickComplete(item);
                    }
                });
            }
        }
    }
    
    /**
     * Determina si un hábito puede completarse rápidamente desde el Dashboard
     */
    private boolean canQuickCompleteHabit(Habit habit) {
        switch (habit.getType()) {
            case DEMO:
            case VITAMINS:
            case COLD_SHOWER:
            case ENGLISH:
            case CODING:
                return true; // Estos tipos se pueden completar con un click
            case READ_BOOK:
            case WATER:
            case JOURNALING:
            case MEDITATE:
            case GYM:
                return false; // Estos requieren abrir una actividad específica
            default:
                return false;
        }
    }

    @Override public int getItemCount() { return data.size(); }
    
    /**
     * Actualiza la lista completa de hábitos
     */
    public void updateHabits(List<Habit> newHabits) {
        this.data.clear();
        this.data.addAll(newHabits);
        notifyDataSetChanged();
    }
    
    /**
     * Elimina un hábito de la lista
     */
    public void removeHabit(Habit habit) {
        int position = data.indexOf(habit);
        if (position >= 0) {
            data.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    /**
     * Agrega un hábito a la lista
     */
    public void addHabit(Habit habit) {
        data.add(habit);
        notifyItemInserted(data.size() - 1);
    }
    
    /**
     * Actualiza un hábito existente
     */
    public void updateHabit(Habit habit) {
        int position = -1;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getId() == habit.getId()) {
                position = i;
                break;
            }
        }
        if (position >= 0) {
            data.set(position, habit);
            notifyItemChanged(position);
        }
    }
    
    private int getTodayProgress(Context context, String key, int defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences("habit_progress", Context.MODE_PRIVATE);
        String todayKey = key + "_" + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        return prefs.getInt(todayKey, defaultValue);
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtName, txtGoal, txtType;
        ProgressBar progress;
        android.widget.ImageButton btnEdit, btnDelete, btnQuickComplete;
        VH(@NonNull View v) {
            super(v);
            txtName  = v.findViewById(R.id.txtHabitName);
            txtGoal  = v.findViewById(R.id.txtGoal);
            txtType  = v.findViewById(R.id.txtType);
            progress = v.findViewById(R.id.progressHabit);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
            btnQuickComplete = v.findViewById(R.id.btnQuickComplete);
        }
    }
}
