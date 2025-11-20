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

    private final List<Habit> data;
    private final OnHabitClick listener;
    private final OnHabitEdit editListener;
    private final OnHabitDelete deleteListener;

    public HabitAdapter(List<Habit> data, OnHabitClick listener) {
        this(data, listener, null, null);
    }
    
    public HabitAdapter(List<Habit> data, OnHabitClick listener, OnHabitEdit editListener, OnHabitDelete deleteListener) {
        this.data = data;
        this.listener = listener;
        this.editListener = editListener;
        this.deleteListener = deleteListener;
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
        } else if (item.getTargetValue() > 0 && item.getTargetUnit() != null) {
            goalText = goalText + " (" + item.getTargetValue() + " " + item.getTargetUnit() + ")";
            progressValue = isCompleted ? 100 : 25;
        } else {
            progressValue = isCompleted ? 100 : 25;
        }
        
        h.txtGoal.setText(goalText);
        h.txtType.setText(item.getCategory());
        
        // Configurar progreso y color según estado
        h.progress.setProgress(progressValue);
        
        // Cambiar color de la barra: verde si está completado, naranja si no
        if (isCompleted || progressValue >= 100) {
            // Barra verde para tareas completadas
            h.progress.setProgressDrawable(
                ContextCompat.getDrawable(
                    h.itemView.getContext(), 
                    R.drawable.progress_circle_drawable_green
                )
            );
        } else {
            // Barra naranja para tareas pendientes
            h.progress.setProgressDrawable(
                ContextCompat.getDrawable(
                    h.itemView.getContext(), 
                    R.drawable.progress_circle_drawable
                )
            );
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
        android.widget.ImageButton btnEdit, btnDelete;
        VH(@NonNull View v) {
            super(v);
            txtName  = v.findViewById(R.id.txtHabitName);
            txtGoal  = v.findViewById(R.id.txtGoal);
            txtType  = v.findViewById(R.id.txtType);
            progress = v.findViewById(R.id.progressHabit);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
