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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScoresActivity extends AppCompatActivity {

    private RecyclerView rvScores;
    private TextView txtTotalScore;
    private HabitDatabaseHelper dbHelper;
    private RankingAdapter adapter;

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
        com.google.android.material.bottomnavigation.BottomNavigationView bottomNav = findViewById(
                R.id.bottomNavigation);
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
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
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
