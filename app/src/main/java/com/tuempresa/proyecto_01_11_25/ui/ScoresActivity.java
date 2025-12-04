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
import com.tuempresa.proyecto_01_11_25.model.UserDto;
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
        loadStreak(); // Refrescar racha al volver a la actividad
        loadFriends();
    }
    
    /**
     * Carga y muestra la información de la racha actual
     * Nota: getCurrentStreak() ya maneja la activación forzada si es necesario
     */
    private void loadStreak() {
        long userId = sessionManager.getUserId();
        
        // CRÍTICO: Recalcular el contador basándose en los hábitos actualmente completados
        // Esto corrige casos donde los hábitos se completaron antes de que se implementara el sistema de rachas
        // o cuando hay inconsistencias en el contador
        dbHelper.recalculateDailyHabitsCompleted(userId);
        
        // Obtener valores directamente después de recalcular (sin llamar a checkAndResetDailyCounter de nuevo)
        int dailyHabitsCompleted = dbHelper.getDailyHabitsCompleted(userId);
        int currentStreak = dbHelper.getCurrentStreak();
        
        android.util.Log.d("ScoresActivity", "📊 loadStreak() - Racha actual: " + currentStreak + ", Hábitos completados hoy: " + dailyHabitsCompleted + ", userId: " + userId);
        
        // Si después de recalcular tenemos 3+ hábitos pero la racha sigue en 0, forzar activación
        if (dailyHabitsCompleted >= 3 && currentStreak == 0) {
            android.util.Log.w("ScoresActivity", "⚠️ Después de recalcular: " + dailyHabitsCompleted + " hábitos completados pero racha es 0. Forzando activación...");
            // Forzar activación directamente usando el método del dbHelper
            // Esto asegura que usamos las constantes correctas
            android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
            android.content.ContentValues values = new android.content.ContentValues();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            long todayTimestamp = cal.getTimeInMillis() / 1000;
            
            // Usar las constantes de HabitDatabaseHelper (accesibles porque son protected)
            values.put("current_streak", 1);
            values.put("last_streak_date", todayTimestamp);
            // IMPORTANTE: usar "user_id" no "id"
            int rowsUpdated = db.update("users", values, "user_id=?", new String[]{String.valueOf(userId)});
            db.close();
            
            android.util.Log.d("ScoresActivity", "🔧 Forzando activación: rowsUpdated=" + rowsUpdated + " para userId=" + userId);
            
            if (rowsUpdated > 0) {
                android.util.Log.d("ScoresActivity", "✅ Racha forzada a 1 día desde ScoresActivity");
                currentStreak = 1;
                // Recargar el valor desde la base de datos
                currentStreak = dbHelper.getCurrentStreak();
            } else {
                android.util.Log.e("ScoresActivity", "❌ No se pudo actualizar la racha. Verificar que el usuario " + userId + " existe en la tabla users.");
            }
        }
        
        // Mostrar información de la racha
        if (currentStreak > 0) {
            txtCurrentStreak.setText(currentStreak + " día" + (currentStreak > 1 ? "s" : ""));
            if (dailyHabitsCompleted >= 3) {
                txtStreakInfo.setText("¡Excelente! Completaste " + dailyHabitsCompleted + " hábitos hoy. Tu racha continúa.");
            } else {
                int remaining = 3 - dailyHabitsCompleted;
                txtStreakInfo.setText("Completa " + remaining + " hábito" + (remaining > 1 ? "s más" : " más") + " hoy para mantener tu racha.");
            }
        } else {
            txtCurrentStreak.setText("Sin racha");
            if (dailyHabitsCompleted >= 3) {
                // Si completó 3 hábitos pero la racha es 0, getCurrentStreak() debería haberla activado
                // Pero por si acaso, mostramos el mensaje correcto
                android.util.Log.w("ScoresActivity", "⚠️ Usuario completó " + dailyHabitsCompleted + " hábitos pero la racha es 0. Esto no debería pasar si getCurrentStreak() funcionó correctamente.");
                txtCurrentStreak.setText("1 día");
                txtStreakInfo.setText("¡Felicidades! Completaste 3 hábitos. Tu racha ha comenzado.");
            } else {
                int remaining = 3 - dailyHabitsCompleted;
                txtStreakInfo.setText("Completa " + remaining + " hábito" + (remaining > 1 ? "s más" : " más") + " para iniciar tu racha.");
            }
        }
    }

    private void loadScores() {
        int totalScore = dbHelper.getTotalScore();
        txtTotalScore.setText(String.valueOf(totalScore));

        loadStreak();

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

        // Crear un layout para el diálogo
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_search_user, null);
        builder.setView(dialogView);

        EditText input = dialogView.findViewById(R.id.etSearchInput);
        android.widget.RadioGroup radioGroup = dialogView.findViewById(R.id.rgSearchType);
        
        // Configurar hint inicial
        input.setHint("Email, nombre o ID del usuario");

        // Listener para cambiar el hint según el tipo de búsqueda seleccionado
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbSearchByEmail) {
                input.setHint("Email del usuario");
                input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            } else if (checkedId == R.id.rbSearchByName) {
                input.setHint("Nombre del usuario");
                input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            } else if (checkedId == R.id.rbSearchById) {
                input.setHint("ID del usuario (número)");
                input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            }
        });

        // Seleccionar búsqueda por email por defecto
        radioGroup.check(R.id.rbSearchByEmail);

        builder.setPositiveButton("Buscar", (dialog, which) -> {
            String searchTerm = input.getText().toString().trim();
            if (searchTerm.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa un término de búsqueda", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int selectedId = radioGroup.getCheckedRadioButtonId();
            if (selectedId == R.id.rbSearchByEmail) {
                searchAndAddFriendByEmail(searchTerm);
            } else if (selectedId == R.id.rbSearchByName) {
                searchAndAddFriendByName(searchTerm);
            } else if (selectedId == R.id.rbSearchById) {
                try {
                    long userId = Long.parseLong(searchTerm);
                    searchAndAddFriendById(userId);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "El ID debe ser un número válido", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    /**
     * Busca y agrega un amigo por email
     */
    private void searchAndAddFriendByEmail(String email) {
        Toast.makeText(this, "Buscando usuario por email...", Toast.LENGTH_SHORT).show();

        Call<UserStatsResponse> call = userApiService.getUserByEmailWithStats(email);
        call.enqueue(new Callback<UserStatsResponse>() {
            @Override
            public void onResponse(Call<UserStatsResponse> call, Response<UserStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserStatsResponse userResponse = response.body();
                    addFriendFromResponse(userResponse);
                } else {
                    if (response.code() == 404) {
                        Toast.makeText(ScoresActivity.this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ScoresActivity.this, "Error al buscar usuario: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                    Log.e(TAG, "Error al buscar usuario por email: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserStatsResponse> call, Throwable t) {
                Log.e(TAG, "Error de red al buscar usuario por email", t);
                Toast.makeText(ScoresActivity.this, "Error de conexión. Verifica tu internet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Busca y agrega un amigo por nombre
     */
    private void searchAndAddFriendByName(String name) {
        Toast.makeText(this, "Buscando usuario por nombre...", Toast.LENGTH_SHORT).show();

        Call<java.util.List<UserDto>> call = userApiService.searchUsersByName(name);
        call.enqueue(new Callback<java.util.List<UserDto>>() {
            @Override
            public void onResponse(Call<java.util.List<UserDto>> call, Response<java.util.List<UserDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    java.util.List<UserDto> users = response.body();
                    if (users.isEmpty()) {
                        Toast.makeText(ScoresActivity.this, "No se encontraron usuarios con ese nombre", Toast.LENGTH_SHORT).show();
                    } else if (users.size() == 1) {
                        // Si hay solo un resultado, agregarlo directamente
                        UserDto user = users.get(0);
                        fetchUserStatsAndAdd(user);
                    } else {
                        // Si hay múltiples resultados, mostrar diálogo para seleccionar
                        showUserSelectionDialog(users);
                    }
                } else {
                    Toast.makeText(ScoresActivity.this, "Error al buscar usuario: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error al buscar usuario por nombre: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<java.util.List<UserDto>> call, Throwable t) {
                Log.e(TAG, "Error de red al buscar usuario por nombre", t);
                Toast.makeText(ScoresActivity.this, "Error de conexión. Verifica tu internet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Busca y agrega un amigo por ID
     */
    private void searchAndAddFriendById(long userId) {
        Toast.makeText(this, "Buscando usuario por ID...", Toast.LENGTH_SHORT).show();

        Call<UserDto> call = userApiService.getUserById(userId);
        call.enqueue(new Callback<UserDto>() {
            @Override
            public void onResponse(Call<UserDto> call, Response<UserDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserDto user = response.body();
                    fetchUserStatsAndAdd(user);
                } else {
                    if (response.code() == 404) {
                        Toast.makeText(ScoresActivity.this, "Usuario no encontrado con ese ID", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ScoresActivity.this, "Error al buscar usuario: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                    Log.e(TAG, "Error al buscar usuario por ID: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserDto> call, Throwable t) {
                Log.e(TAG, "Error de red al buscar usuario por ID", t);
                Toast.makeText(ScoresActivity.this, "Error de conexión. Verifica tu internet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Obtiene las estadísticas de un usuario y lo agrega como amigo
     */
    private void fetchUserStatsAndAdd(UserDto user) {
        Call<UserStatsResponse.UserStats> statsCall = userApiService.getUserStats(user.getId());
        statsCall.enqueue(new Callback<UserStatsResponse.UserStats>() {
            @Override
            public void onResponse(Call<UserStatsResponse.UserStats> call, Response<UserStatsResponse.UserStats> response) {
                UserStatsResponse.UserStats stats = null;
                if (response.isSuccessful() && response.body() != null) {
                    stats = response.body();
                } else {
                    stats = new UserStatsResponse.UserStats();
                }

                // Crear UserStatsResponse con el usuario y sus estadísticas
                UserStatsResponse userResponse = new UserStatsResponse();
                userResponse.setUser(user);
                userResponse.setStats(stats);

                addFriendFromResponse(userResponse);
            }

            @Override
            public void onFailure(Call<UserStatsResponse.UserStats> call, Throwable t) {
                Log.e(TAG, "Error al obtener estadísticas del usuario", t);
                // Agregar sin estadísticas
                UserStatsResponse userResponse = new UserStatsResponse();
                userResponse.setUser(user);
                userResponse.setStats(new UserStatsResponse.UserStats());
                addFriendFromResponse(userResponse);
            }
        });
    }

    /**
     * Muestra un diálogo para seleccionar un usuario de una lista
     */
    private void showUserSelectionDialog(java.util.List<UserDto> users) {
        String[] userNames = new String[users.size()];
        for (int i = 0; i < users.size(); i++) {
            UserDto user = users.get(i);
            String displayName = user.getDisplayName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = user.getEmail() != null ? user.getEmail().split("@")[0] : "Usuario " + user.getId();
            }
            userNames[i] = displayName + " (" + user.getEmail() + ")";
        }

        new AlertDialog.Builder(this)
                .setTitle("Selecciona un usuario")
                .setItems(userNames, (dialog, which) -> {
                    UserDto selectedUser = users.get(which);
                    fetchUserStatsAndAdd(selectedUser);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Agrega un amigo desde una respuesta de UserStatsResponse
     */
    private void addFriendFromResponse(UserStatsResponse userResponse) {
        long userId = sessionManager.getUserId();
        UserDto user = userResponse.getUser();
        String friendEmail = user.getEmail();
        long friendUserId = user.getId();

        // Verificar si el amigo ya existe
        if (dbHelper.friendExists(userId, friendEmail)) {
            Toast.makeText(this, "Este amigo ya está en tu lista", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar que no se agregue a sí mismo
        String currentUserEmail = sessionManager.getUserEmail();
        if (friendEmail != null && friendEmail.equalsIgnoreCase(currentUserEmail)) {
            Toast.makeText(this, "No puedes agregarte a ti mismo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar que no se agregue a sí mismo por ID
        if (friendUserId == userId) {
            Toast.makeText(this, "No puedes agregarte a ti mismo", Toast.LENGTH_SHORT).show();
            return;
        }

        UserStatsResponse.UserStats stats = userResponse.getStats();
        if (stats == null) {
            stats = new UserStatsResponse.UserStats();
        }

        String displayName = user.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = friendEmail != null ? friendEmail.split("@")[0] : "Usuario " + friendUserId;
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

            Toast.makeText(this, "Amigo agregado correctamente", Toast.LENGTH_SHORT).show();
            loadFriends();
        } else {
            Toast.makeText(this, "Error al guardar amigo", Toast.LENGTH_SHORT).show();
        }
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
