package com.cookmood.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gestion de la connexion à la base de données MySQL (XAMPP).
 *
 * Configuration par défaut XAMPP :
 *   - Hôte    : localhost
 *   - Port    : 3306
 *   - User    : root
 *   - Mot de passe : (vide)
 *   - Base    : moodcook
 *
 * Avant d'exécuter l'application :
 *   1. Démarrer Apache + MySQL dans XAMPP Control Panel.
 *   2. Importer le script /database/moodcook.sql dans phpMyAdmin
 *      (ou exécuter : mysql -u root < database/moodcook.sql).
 */
public class DBConnection {

    private static final String HOST     = "localhost";
    private static final String PORT     = "3306";
    private static final String DATABASE = "moodcook";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static final String URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8";

    private static Connection connection;

    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                        "Driver MySQL introuvable. Ajoutez mysql-connector-j-x.x.x.jar au projet.",
                        e);
            }
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[CookMood] Connexion MySQL établie : " + URL);
        }
        return connection;
    }

    public static void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[CookMood] Connexion MySQL fermée.");
            } catch (SQLException e) {
                System.err.println("Erreur fermeture connexion : " + e.getMessage());
            }
        }
    }
}
