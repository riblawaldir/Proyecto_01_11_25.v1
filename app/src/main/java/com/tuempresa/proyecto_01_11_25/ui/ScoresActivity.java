package com.tuempresa.proyecto_01_11_25.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tuempresa.proyecto_01_11_25.R;
import com.tuempresa.proyecto_01_11_25.api.HabitApiClient;
import com.tuempresa.proyecto_01_11_25.api.UserApiService;
import com.tuempresa.proyecto_01_11_25.database.HabitDatabaseHelper;
import com.tuempresa.proyecto_01_11_25.model.Friend;
import com.tuempresa.proyecto_01_11_25.model.UserStatsResponse;
import com.tuempresa.proyecto_01_11_25.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScoresActivity extends AppCompatActivity {

    private RecyclerView rvFriends;
    private TextView txtTotalScore;
    private TextView txtCurrentStreak;
    private TextView txtStreakInfo;
    private HabitDatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private FriendsAdapter friendsAdapter;
    private FloatingActionButton fabAddFriend;
    private UserApiService userApiService;
    private static final String TAG = "ScoresActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scores);

        dbHelper = new HabitDatabaseHelper(this);
        sessionManager = new SessionManager(this);
        userApiService = HabitApiClient.getInstance(this).getUserApiService();
        txtTotalScore = findViewById(R.id.txtTotalScore);
        txtCurrentStreak = findViewById(R.id.txtCurrentStreak);
        txtStreakInfo = findViewById(R.id.txtStreakInfo);
        rvFriends = findViewById(R.id.rvFriends);
        fabAddFriend = findViewById(R.id.fabAddFriend);

        rvFriends.setLayoutManager(new LinearLayoutManager(this));
        loadScores();
        loadFriends();

        fabAddFriend.setOnClickListener(v -> showAddFriendDialog());

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
        loadFriends();
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

    }

    private void loadFriends() {
        long userId = sessionManager.getUserId();
        List<Friend> friends = dbHelper.getAllFriends(userId);
        
        friendsAdapter = new FriendsAdapter(friends);
        rvFriends.setAdapter(friendsAdapter);
        
        // Actualizar estadísticas de amigos desde la API (en segundo plano)
        updateFriendsStats(friends);
    }

    /**
     * Actualiza las estadísticas de los amigos desde la API
     */
    private void updateFriendsStats(List<Friend> friends) {
        int totalFriends = friends.size();
        if (totalFriends == 0) return;
        
        final int[] completedUpdates = {0};
        
        for (Friend friend : friends) {
            if (friend.getFriendUserId() > 0) {
                Call<UserStatsResponse.UserStats> call = userApiService.getUserStats(friend.getFriendUserId());
                call.enqueue(new Callback<UserStatsResponse.UserStats>() {
                    @Override
                    public void onResponse(Call<UserStatsResponse.UserStats> call, Response<UserStatsResponse.UserStats> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            UserStatsResponse.UserStats stats = response.body();
                            dbHelper.updateFriendStats(
                                friend.getId(),
                                stats.getTotalHabits(),
                                stats.getTotalPoints(),
                                stats.getCurrentStreak()
                            );
                            
                            // Actualizar el item específico en el adapter si existe
                            if (friendsAdapter != null) {
                                int position = friends.indexOf(friend);
                                if (position >= 0) {
                                    friend.setTotalHabits(stats.getTotalHabits());
                                    friend.setTotalPoints(stats.getTotalPoints());
                                    friend.setCurrentStreak(stats.getCurrentStreak());
                                    runOnUiThread(() -> friendsAdapter.notifyItemChanged(position));
                                }
                            }
                        }
                        
                        // Refrescar la lista completa solo cuando todas las actualizaciones terminen
                        synchronized (completedUpdates) {
                            completedUpdates[0]++;
                            if (completedUpdates[0] >= totalFriends) {
                                // Todas las actualizaciones completadas, refrescar desde BD
                                runOnUiThread(() -> {
                                    long userId = sessionManager.getUserId();
                                    List<Friend> updatedFriends = dbHelper.getAllFriends(userId);
                                    friendsAdapter = new FriendsAdapter(updatedFriends);
                                    rvFriends.setAdapter(friendsAdapter);
                                });
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<UserStatsResponse.UserStats> call, Throwable t) {
                        Log.e(TAG, "Error al actualizar estadísticas del amigo " + friend.getFriendEmail(), t);
                        
                        // Contar como completado incluso si falla
                        synchronized (completedUpdates) {
                            completedUpdates[0]++;
                        }
                    }
                });
            } else {
                // Si no tiene friendUserId, contar como completado
                synchronized (completedUpdates) {
                    completedUpdates[0]++;
                }
            }
        }
    }

    private void showAddFriendDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Agregar Amigo");

        final EditText input = new EditText(this);
        input.setHint("Email del amigo");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("Agregar", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa un email", Toast.LENGTH_SHORT).show();
                return;
            }
            addFriend(email);
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void addFriend(String friendEmail) {
        long userId = sessionManager.getUserId();
        
        // Verificar si el amigo ya existe
        if (dbHelper.friendExists(userId, friendEmail)) {
            Toast.makeText(this, "Este amigo ya está en tu lista", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar que no se agregue a sí mismo
        String currentUserEmail = sessionManager.getUserEmail();
        if (friendEmail.equalsIgnoreCase(currentUserEmail)) {
            Toast.makeText(this, "No puedes agregarte a ti mismo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mostrar indicador de carga
        Toast.makeText(this, "Buscando usuario...", Toast.LENGTH_SHORT).show();

        // Buscar usuario en la API
        Call<UserStatsResponse> call = userApiService.getUserByEmailWithStats(friendEmail);
        call.enqueue(new Callback<UserStatsResponse>() {
            @Override
            public void onResponse(Call<UserStatsResponse> call, Response<UserStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserStatsResponse userResponse = response.body();
                    UserStatsResponse.UserStats stats = userResponse.getStats();
                    
                    if (stats == null) {
                        stats = new UserStatsResponse.UserStats();
                    }

                    // Guardar amigo en la base de datos local
                    long friendUserId = userResponse.getUser().getId();
                    String displayName = userResponse.getUser().getDisplayName();
                    if (displayName == null || displayName.isEmpty()) {
                        displayName = friendEmail.split("@")[0];
                    }

                    long friendId = dbHelper.addFriend(
                        userId,
                        friendUserId,
                        friendEmail,
                        displayName
                    );

                    if (friendId > 0) {
                        // Actualizar estadísticas del amigo
                        dbHelper.updateFriendStats(
                            friendId,
                            stats.getTotalHabits(),
                            stats.getTotalPoints(),
                            stats.getCurrentStreak()
                        );

                        Toast.makeText(ScoresActivity.this, "Amigo agregado correctamente", Toast.LENGTH_SHORT).show();
                        loadFriends();
                    } else {
                        Toast.makeText(ScoresActivity.this, "Error al guardar amigo", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (response.code() == 404) {
                        Toast.makeText(ScoresActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ScoresActivity.this, "Error al buscar usuario: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                    Log.e(TAG, "Error al buscar usuario: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<UserStatsResponse> call, Throwable t) {
                Log.e(TAG, "Error de red al buscar usuario", t);
                Toast.makeText(ScoresActivity.this, "Error de conexión. Verifica tu internet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.VH> {
        private final List<Friend> friendsList;

        public FriendsAdapter(List<Friend> friendsList) {
            this.friendsList = friendsList;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Friend friend = friendsList.get(position);
            holder.tvFriendName.setText(friend.getFriendName() != null && !friend.getFriendName().isEmpty() 
                    ? friend.getFriendName() : "Amigo");
            holder.tvFriendEmail.setText(friend.getFriendEmail());
            holder.tvFriendHabits.setText(friend.getTotalHabits() + " hábitos");
            holder.tvFriendPoints.setText(friend.getTotalPoints() + " pts");
            holder.tvFriendStreak.setText(friend.getCurrentStreak() + " día" + (friend.getCurrentStreak() != 1 ? "s" : ""));
        }

        @Override
        public int getItemCount() {
            return friendsList.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvFriendName, tvFriendEmail, tvFriendHabits, tvFriendPoints, tvFriendStreak;

            VH(@NonNull View v) {
                super(v);
                tvFriendName = v.findViewById(R.id.tvFriendName);
                tvFriendEmail = v.findViewById(R.id.tvFriendEmail);
                tvFriendHabits = v.findViewById(R.id.tvFriendHabits);
                tvFriendPoints = v.findViewById(R.id.tvFriendPoints);
                tvFriendStreak = v.findViewById(R.id.tvFriendStreak);
            }
        }
    }
}
