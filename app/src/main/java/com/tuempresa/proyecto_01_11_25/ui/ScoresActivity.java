package com.tuempresa.proyecto_01_11_25.ui;

import android.content.Intent;
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
import com.tuempresa.proyecto_01_11_25.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScoresActivity extends AppCompatActivity {

    private RecyclerView rvScores;
    private TextView txtTotalScore;
    private TextView txtCurrentStreak;
    private TextView txtStreakInfo;
    private HabitDatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private RankingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        dbHelper = new HabitDatabaseHelper(this);
        sessionManager = new SessionManager(this);
        txtTotalScore = findViewById(R.id.txtTotalScore);
        txtCurrentStreak = findViewById(R.id.txtCurrentStreak);
        txtStreakInfo = findViewById(R.id.txtStreakInfo);
        rvScores = findViewById(R.id.rvScores);

        rvScores.setLayoutManager(new LinearLayoutManager(this));
        loadScores();

        // Bottom Navigation
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(
                R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_scores);
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_dashboard) {
                    Intent intent = new Intent(this, DashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_scores) {
                    // Ya estamos en scores
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(this, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    startActivity(intent);
                    finish();
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

        // Cargar racha actual
        int currentStreak = dbHelper.getCurrentStreak();
        long userId = sessionManager.getUserId();
        int dailyHabitsCompleted = dbHelper.getDailyHabitsCompleted(userId);
        
        if (currentStreak > 0) {
            txtCurrentStreak.setText(currentStreak + " día" + (currentStreak > 1 ? "s" : ""));
            txtStreakInfo.setText("¡Sigue así! Completa 3 hábitos hoy para mantener tu racha.");
        } else {
            txtCurrentStreak.setText("Sin racha");
            if (dailyHabitsCompleted >= 3) {
                txtStreakInfo.setText("¡Completaste 3 hábitos! Tu racha comenzará mañana.");
            } else {
                int remaining = 3 - dailyHabitsCompleted;
                txtStreakInfo.setText("Completa " + remaining + " hábito" + (remaining > 1 ? "s más" : " más") + " para iniciar tu racha.");
            }
        }

        List<com.tuempresa.proyecto_01_11_25.model.UserRanking> ranking = dbHelper.getUsersRanking();
        adapter = new RankingAdapter(ranking);
        rvScores.setAdapter(adapter);
    }

    private static class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.VH> {
        private final List<com.tuempresa.proyecto_01_11_25.model.UserRanking> rankingList;

        public RankingAdapter(List<com.tuempresa.proyecto_01_11_25.model.UserRanking> rankingList) {
            this.rankingList = rankingList;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            com.tuempresa.proyecto_01_11_25.model.UserRanking item = rankingList.get(position);
            holder.tvRankPosition.setText("#" + (position + 1));
            holder.tvUserName.setText(item.getUserName());
            holder.tvHabitsCount.setText(item.getHabitsCompletedCount() + " hábitos completados");
            holder.tvTotalScore.setText(item.getTotalScore() + " pts");
        }

        @Override
        public int getItemCount() {
            return rankingList.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvRankPosition, tvUserName, tvHabitsCount, tvTotalScore;

            VH(@NonNull View v) {
                super(v);
                tvRankPosition = v.findViewById(R.id.tvRankPosition);
                tvUserName = v.findViewById(R.id.tvUserName);
                tvHabitsCount = v.findViewById(R.id.tvHabitsCount);
                tvTotalScore = v.findViewById(R.id.tvTotalScore);
            }
        }
    }
}
