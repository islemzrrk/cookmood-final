package com.cookmood.model;

/**
 * Modèle représentant une recette de l'application CookMood.
 */
public class Recette {

    private int id;
    private String nom;
    private String mood;
    private String weather;
    private String auteur;
    private int duree;              // en minutes
    private String ingredients;
    private String description;
    private String instructions;
    private String icone;
    private String type;
    private boolean alcool;
    private boolean favori;

    public Recette() { }

    public Recette(int id, String nom, String mood, String weather, String auteur,
                   int duree, String ingredients, String description, String instructions,
                   String icone, String type, boolean alcool, boolean favori) {
        this.id = id;
        this.nom = nom;
        this.mood = mood;
        this.weather = weather;
        this.auteur = auteur;
        this.duree = duree;
        this.ingredients = ingredients;
        this.description = description;
        this.instructions = instructions;
        this.icone = icone;
        this.type = type;
        this.alcool = alcool;
        this.favori = favori;
    }

    // ---- Getters / Setters ----
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getMood() { return mood; }
    public void setMood(String mood) { this.mood = mood; }

    public String getWeather() { return weather; }
    public void setWeather(String weather) { this.weather = weather; }

    public String getAuteur() { return auteur; }
    public void setAuteur(String auteur) { this.auteur = auteur; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getIcone() { return icone; }
    public void setIcone(String icone) { this.icone = icone; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isAlcool() { return alcool; }
    public void setAlcool(boolean alcool) { this.alcool = alcool; }

    public boolean isFavori() { return favori; }
    public void setFavori(boolean favori) { this.favori = favori; }

    @Override
    public String toString() {
        return nom + " (" + mood + " / " + weather + ")";
    }
}
