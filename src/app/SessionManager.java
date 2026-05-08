package app;

public class SessionManager {
    private static Object currentUser;

    public static void setCurrentUser(Object user) {
        currentUser = user;
    }

    public static Object getCurrentUser() {
        return currentUser;
    }

    public static void logout() {
        currentUser = null;
    }
}
