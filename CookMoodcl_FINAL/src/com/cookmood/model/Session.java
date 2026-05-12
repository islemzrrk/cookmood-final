package com.cookmood.model;

/**
 * Représente la session de l'utilisateur connecté.
 * Conservée en mémoire pendant toute l'exécution de l'application.
 */
public class Session {

    private static User currentUser;

    private Session() { }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    /** Vrai si l'utilisateur connecté est mineur (< 18 ans). */
    public static boolean isMinor() {
        return currentUser != null && currentUser.getAge() < 18;
    }

    /** Renvoie le nom complet pour affichage, ou "Invité" si non connecté. */
    public static String displayName() {
        if (currentUser == null) return "Invité";
        return currentUser.getNom();
    }
}
