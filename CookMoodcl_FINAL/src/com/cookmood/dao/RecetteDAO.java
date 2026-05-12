package com.cookmood.dao;

import com.cookmood.model.Recette;
import com.cookmood.model.Session;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Accès aux données pour la table 'recettes'.
 *
 * Filtrage automatique : si la session courante correspond à un mineur
 * (Session.isMinor()), toutes les requêtes excluent les recettes contenant
 * de l'alcool (alcool = 1).
 */
public class RecetteDAO {

    /** Renvoie le filtre alcool si l'utilisateur est mineur. */
    private String alcoholFilter() {
        return Session.isMinor() ? " AND alcool = 0" : "";
    }

    /** Récupère toutes les recettes (filtrage alcool automatique pour mineurs). */
    public List<Recette> findAll() {
        return query("SELECT * FROM recettes WHERE 1=1" + alcoholFilter() +
                     " ORDER BY id DESC", new Object[0]);
    }

    /** Récupère uniquement les recettes favorites. */
    public List<Recette> findFavorites() {
        return query("SELECT * FROM recettes WHERE favori = 1" + alcoholFilter() +
                     " ORDER BY id DESC", new Object[0]);
    }

    /** Recherche multi-critères. */
    public List<Recette> search(String mood, String weather, String ingredientsKeyword) {
        StringBuilder sql = new StringBuilder("SELECT * FROM recettes WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (mood != null && !mood.isBlank()) {
            sql.append(" AND mood = ?");
            params.add(mood);
        }
        if (weather != null && !weather.isBlank()) {
            sql.append(" AND weather = ?");
            params.add(weather);
        }
        if (ingredientsKeyword != null && !ingredientsKeyword.isBlank()) {
            String[] keywords = ingredientsKeyword.split(",");
            for (String k : keywords) {
                String kw = k.trim();
                if (!kw.isEmpty()) {
                    sql.append(" AND ingredients LIKE ?");
                    params.add("%" + kw + "%");
                }
            }
        }

        sql.append(alcoholFilter());
        sql.append(" ORDER BY id DESC");
        return query(sql.toString(), params.toArray());
    }

    /** Insère une nouvelle recette. */
    public boolean insert(Recette r) {
        String sql = "INSERT INTO recettes (nom, mood, weather, auteur, duree, ingredients, " +
                "description, instructions, icone, type, alcool, favori) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getNom());
            ps.setString(2, r.getMood());
            ps.setString(3, r.getWeather());
            ps.setString(4, r.getAuteur());
            ps.setInt(5, r.getDuree());
            ps.setString(6, r.getIngredients());
            ps.setString(7, r.getDescription());
            ps.setString(8, r.getInstructions());
            ps.setString(9, r.getIcone());
            ps.setString(10, r.getType());
            ps.setBoolean(11, r.isAlcool());
            ps.setBoolean(12, r.isFavori());
            int n = ps.executeUpdate();
            if (n > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) r.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Erreur insert recette : " + e.getMessage());
        }
        return false;
    }

    /** Bascule l'état favori d'une recette. */
    public boolean toggleFavorite(int recetteId) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE recettes SET favori = NOT favori WHERE id = ?")) {
            ps.setInt(1, recetteId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur toggle favori : " + e.getMessage());
            return false;
        }
    }

    /** Compte le nombre de favoris (filtré aussi par alcool si mineur). */
    public int countFavorites() {
        String sql = "SELECT COUNT(*) FROM recettes WHERE favori = 1" + alcoholFilter();
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur count favoris : " + e.getMessage());
        }
        return 0;
    }

    /** Supprime une recette par son id. */
    public boolean delete(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM recettes WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Erreur delete recette : " + e.getMessage());
            return false;
        }
    }

    // ----------------------------------------------------------------
    private List<Recette> query(String sql, Object[] params) {
        List<Recette> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Erreur requête recettes : " + e.getMessage());
        }
        return list;
    }

    private Recette map(ResultSet rs) throws SQLException {
        return new Recette(
                rs.getInt("id"),
                rs.getString("nom"),
                rs.getString("mood"),
                rs.getString("weather"),
                rs.getString("auteur"),
                rs.getInt("duree"),
                rs.getString("ingredients"),
                rs.getString("description"),
                rs.getString("instructions"),
                rs.getString("icone"),
                rs.getString("type"),
                rs.getBoolean("alcool"),
                rs.getBoolean("favori")
        );
    }
}
