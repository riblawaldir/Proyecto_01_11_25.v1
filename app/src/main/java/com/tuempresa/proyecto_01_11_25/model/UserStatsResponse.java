package com.tuempresa.proyecto_01_11_25.model;

/**
 * Modelo de respuesta de estadísticas de usuario de la API.
 */
public class UserStatsResponse {
    private UserDto user;
    private UserStats stats;

    public UserStatsResponse() {
    }

    public UserDto getUser() {
        return user;
    }

    public void setUser(UserDto user) {
        this.user = user;
    }

    public UserStats getStats() {
        return stats;
    }

    public void setStats(UserStats stats) {
        this.stats = stats;
    }

    /**
     * Clase interna para las estadísticas del usuario
     */
    public static class UserStats {
        private int totalHabits;
        private int totalPoints;
        private int currentStreak;
        private int longestStreak;

        public UserStats() {
        }

        public int getTotalHabits() {
            return totalHabits;
        }

        public void setTotalHabits(int totalHabits) {
            this.totalHabits = totalHabits;
        }

        public int getTotalPoints() {
            return totalPoints;
        }

        public void setTotalPoints(int totalPoints) {
            this.totalPoints = totalPoints;
        }

        public int getCurrentStreak() {
            return currentStreak;
        }

        public void setCurrentStreak(int currentStreak) {
            this.currentStreak = currentStreak;
        }

        public int getLongestStreak() {
            return longestStreak;
        }

        public void setLongestStreak(int longestStreak) {
            this.longestStreak = longestStreak;
        }
    }
}

