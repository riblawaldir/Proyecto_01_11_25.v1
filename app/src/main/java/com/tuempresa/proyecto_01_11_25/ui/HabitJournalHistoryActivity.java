package com.tuempresa.proyecto_01_11_25.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.tuempresa.proyecto_01_11_25.R;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitJournalHistoryActivity extends AppCompatActivity {

    private HabitDatabaseHelper dbHelper;
    private long habitId;
    private String habitTitle;
    private RecyclerView rvJournalEntries;
    private JournalAdapter adapter;
    private TextView txtEmptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_journal_history);

        habitId = getIntent().getLongExtra("habit_id", -1);
        habitTitle = getIntent().getStringExtra("habit_title");

        if (habitId <= 0) {
            finish();
            return;
        }

        dbHelper = new HabitDatabaseHelper(this);
        rvJournalEntries = findViewById(R.id.rvJournalEntries);
        txtEmptyState = findViewById(R.id.txtEmptyState);

        // Configurar título
        TextView txtTitle = findViewById(R.id.txtTitle);
        if (habitTitle != null) {
            txtTitle.setText("Notas de: " + habitTitle);
        }

        // Botón de volver
        MaterialButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Configurar RecyclerView
        rvJournalEntries.setLayoutManager(new LinearLayoutManager(this));
        loadJournalEntries();
    }

    private void loadJournalEntries() {
        List<HabitDatabaseHelper.DiaryEntry> entries = dbHelper.getDiaryEntriesByHabit(habitId);
        
        if (entries.isEmpty()) {
            rvJournalEntries.setVisibility(View.GONE);
            txtEmptyState.setVisibility(View.VISIBLE);
            txtEmptyState.setText("No hay notas registradas para este hábito.\n\nEscribe tu primera reflexión desde el detalle del hábito.");
        } else {
            rvJournalEntries.setVisibility(View.VISIBLE);
            txtEmptyState.setVisibility(View.GONE);
            adapter = new JournalAdapter(entries);
            rvJournalEntries.setAdapter(adapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadJournalEntries();
    }

    private static class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.VH> {
        private final List<HabitDatabaseHelper.DiaryEntry> entries;

        public JournalAdapter(List<HabitDatabaseHelper.DiaryEntry> entries) {
            this.entries = entries;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_journal_entry, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            HabitDatabaseHelper.DiaryEntry entry = entries.get(position);
            holder.tvContent.setText(entry.getContent());
            
            // Formatear fecha
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            String dateStr = sdf.format(new Date(entry.getDate() * 1000));
            holder.tvDate.setText(dateStr);
        }

        @Override
        public int getItemCount() {
            return entries.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvContent, tvDate;

            VH(@NonNull View v) {
                super(v);
                tvContent = v.findViewById(R.id.tvContent);
                tvDate = v.findViewById(R.id.tvDate);
            }
        }
    }
}

