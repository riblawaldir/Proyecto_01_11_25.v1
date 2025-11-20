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

import com.tuempresa.proyecto_01_11_25.R;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScoresActivity extends AppCompatActivity {

    private RecyclerView rvScores;
    private TextView txtTotalScore;
    private HabitDatabaseHelper dbHelper;
    private ScoreAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        dbHelper = new HabitDatabaseHelper(this);
        txtTotalScore = findViewById(R.id.txtTotalScore);
        rvScores = findViewById(R.id.rvScores);

        rvScores.setLayoutManager(new LinearLayoutManager(this));
        loadScores();

        // Bottom Navigation
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_scores);
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_dashboard) {
                    startActivity(new android.content.Intent(this, DashboardActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_scores) {
                    // Ya estamos en scores
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadScores();
    }

    private void loadScores() {
        int totalScore = dbHelper.getTotalScore();
        txtTotalScore.setText(String.valueOf(totalScore));

        List<HabitDatabaseHelper.ScoreEntry> scores = dbHelper.getAllScores();
        adapter = new ScoreAdapter(scores);
        rvScores.setAdapter(adapter);
    }

    private static class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.VH> {
        private final List<HabitDatabaseHelper.ScoreEntry> scores;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        public ScoreAdapter(List<HabitDatabaseHelper.ScoreEntry> scores) {
            this.scores = scores;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_score, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            HabitDatabaseHelper.ScoreEntry entry = scores.get(position);
            holder.txtHabitTitle.setText(entry.getHabitTitle());
            holder.txtScorePoints.setText("+" + entry.getPoints());
            holder.txtScoreDate.setText(dateFormat.format(new Date(entry.getDate() * 1000)));
        }

        @Override
        public int getItemCount() {
            return scores.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView txtHabitTitle, txtScorePoints, txtScoreDate;

            VH(@NonNull View v) {
                super(v);
                txtHabitTitle = v.findViewById(R.id.txtHabitTitle);
                txtScorePoints = v.findViewById(R.id.txtScorePoints);
                txtScoreDate = v.findViewById(R.id.txtScoreDate);
            }
        }
    }
}

