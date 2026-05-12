package com.cookmood.dao;

import com.cookmood.model.User;

import java.sql.*;

/**
 * Accès aux données pour la table 'users'.
 */
public class UserDAO {

    /** Cherche un utilisateur par nom + prénom (insensible à la casse). */
    public User findByNomPrenom(String nom, String prenom) {
        String sql = "SELECT * FROM users WHERE LOWER(nom) = LOWER(?) AND LOWER(prenom) = LOWER(?) LIMIT 1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            System.err.println("Erreur findByNomPrenom : " + e.getMessage());
        }
        return null;
    }

    /**
     * Cherche l'utilisateur (par nom+prénom). S'il n'existe pas, l'insère.
     * Si l'âge a changé pour un même nom+prénom, on met à jour l'âge.
     */
    public User findOrCreate(String nom, String prenom, int age) {
        User existing = findByNomPrenom(nom, prenom);
        if (existing != null) {
            if (existing.getAge() != age) {
                updateAge(existing.getId(), age);
                existing.setAge(age);
            }
            return existing;
        }

        // Création
        String sql = "INSERT INTO users (nom, prenom, age) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nom);
            ps.setString(2, prenom);
            ps.setInt(3, age);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new User(keys.getInt(1), nom, prenom, age);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur création utilisateur : " + e.getMessage());
        }
        return null;
    }

    private void updateAge(int id, int age) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE users SET age = ? WHERE id = ?")) {
            ps.setInt(1, age);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur updateAge : " + e.getMessage());
        }
    }

    /** Renvoie le premier utilisateur (utilisé comme valeur par défaut). */
    public User findFirst() {
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM users ORDER BY id ASC LIMIT 1")) {
            if (rs.next()) return map(rs);
        } catch (SQLException e) {
            System.err.println("Erreur findFirst : " + e.getMessage());
        }
        return null;
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getInt("age"));
    }
}
