package com.tuempresa.proyecto_01_11_25.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.tuempresa.proyecto_01_11_25.R;
import com.tuempresa.proyecto_01_11_25.model.Habit;

import java.util.ArrayList;
import java.util.List;

public class SelectHabitTypeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HabitTypeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_habit_type);

        recyclerView = findViewById(R.id.rvHabitTypes);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        
        List<HabitTypeItem> habitTypes = getHabitTypes();
        adapter = new HabitTypeAdapter(habitTypes, this::onHabitTypeSelected);
        recyclerView.setAdapter(adapter);
        
        // Botón de volver
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        // Si viene con auto_select_type, seleccionar automáticamente
        String autoSelectType = getIntent().getStringExtra("auto_select_type");
        if (autoSelectType != null) {
            for (HabitTypeItem item : habitTypes) {
                if (item.getType().name().equals(autoSelectType)) {
                    // Pequeño delay para que la UI se renderice
                    recyclerView.post(() -> onHabitTypeSelected(item));
                    return;
                }
            }
        }
    }

    private List<HabitTypeItem> getHabitTypes() {
        List<HabitTypeItem> types = new ArrayList<>();
        // Usar iconos disponibles o genéricos
        int iconBook = R.drawable.ic_menu_book_24;
        int iconFitness = R.drawable.ic_fitness_center_24;
        int iconWalk = R.drawable.ic_directions_walk_24;
        
        // Hábitos manuales/configurables
        types.add(new HabitTypeItem(Habit.HabitType.READ_BOOK, getString(R.string.habit_type_read_book), iconBook));
        types.add(new HabitTypeItem(Habit.HabitType.VITAMINS, getString(R.string.habit_type_vitamins), iconFitness));
        types.add(new HabitTypeItem(Habit.HabitType.MEDITATE, getString(R.string.habit_type_meditate), iconFitness));
        types.add(new HabitTypeItem(Habit.HabitType.JOURNALING, getString(R.string.habit_type_journaling), iconBook));
        types.add(new HabitTypeItem(Habit.HabitType.WATER, getString(R.string.habit_type_water), iconFitness));
        types.add(new HabitTypeItem(Habit.HabitType.COLD_SHOWER, getString(R.string.habit_type_cold_shower), iconFitness));
        
        // Hábitos automáticos con sensores
        types.add(new HabitTypeItem(Habit.HabitType.EXERCISE, getString(R.string.habit_type_exercise), iconFitness));
        types.add(new HabitTypeItem(Habit.HabitType.WALK, getString(R.string.habit_type_walk), iconWalk));
        types.add(new HabitTypeItem(Habit.HabitType.DEMO, getString(R.string.habit_type_demo), iconFitness));
        
        return types;
    }

    private void onHabitTypeSelected(HabitTypeItem item) {
        Intent intent = new Intent(this, ConfigureHabitActivity.class);
        intent.putExtra("habit_type", item.getType().name());
        startActivityForResult(intent, 600);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 600 && resultCode == RESULT_OK) {
            // Hábito creado, notificar al Dashboard
            setResult(RESULT_OK);
            finish();
        }
    }

    public static class HabitTypeItem {
        private final Habit.HabitType type;
        private final String name;
        private final int iconRes;

        public HabitTypeItem(Habit.HabitType type, String name, int iconRes) {
            this.type = type;
            this.name = name;
            this.iconRes = iconRes;
        }

        public Habit.HabitType getType() { return type; }
        public String getName() { return name; }
        public int getIconRes() { return iconRes; }
    }

    private static class HabitTypeAdapter extends RecyclerView.Adapter<HabitTypeAdapter.ViewHolder> {
        private final List<HabitTypeItem> items;
        private final OnHabitTypeClickListener listener;

        public interface OnHabitTypeClickListener {
            void onHabitTypeClick(HabitTypeItem item);
        }

        public HabitTypeAdapter(List<HabitTypeItem> items, OnHabitTypeClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_habit_type, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HabitTypeItem item = items.get(position);
            holder.txtName.setText(item.getName());
            if (holder.imgIcon != null) {
                holder.imgIcon.setImageResource(item.getIconRes());
            }
            holder.cardView.setOnClickListener(v -> listener.onHabitTypeClick(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardView;
            TextView txtName;
            android.widget.ImageView imgIcon;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = (MaterialCardView) itemView;
                txtName = itemView.findViewById(R.id.txtHabitTypeName);
                imgIcon = itemView.findViewById(R.id.imgIcon);
            }
        }
    }
}

