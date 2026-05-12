package com.cookmood.controller;

import com.cookmood.dao.RecetteDAO;
import com.cookmood.model.Recette;
import com.cookmood.model.Session;
import com.cookmood.model.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

/**
 * Contrôleur principal : gère filtres, affichage des cartes et favoris.
 *
 * Le filtrage alcool/âge est entièrement géré par RecetteDAO via
 * Session.isMinor() — aucun code spécifique requis ici.
 */
public class MainController {

    // ==== composants liés au FXML ====
    @FXML private Label  greetingLabel;
    @FXML private Button favBtn;
    @FXML private ComboBox<String> moodCombo;
    @FXML private ComboBox<String> weatherCombo;
    @FXML private TextField ingredientsField;
    @FXML private Label resultsCountLabel;
    @FXML private FlowPane recipesPane;

    // ==== DAO ====
    private final RecetteDAO recetteDAO = new RecetteDAO();

    // ==== état ====
    private static final List<String> MOODS = Arrays.asList(
            "", "Joyeux", "Festif", "Fatigué", "En forme", "Triste", "Stressé", "Romantique");
    private static final List<String> WEATHERS = Arrays.asList(
            "", "Ensoleillé", "Pluvieux", "Nuageux", "Froid", "Chaud", "Neigeux");

    @FXML
    public void initialize() {
        // Pré-remplir les combos
        moodCombo.setItems(FXCollections.observableArrayList(MOODS));
        weatherCombo.setItems(FXCollections.observableArrayList(WEATHERS));

        // Greeting personnalisé depuis la session
        User u = Session.getCurrentUser();
        if (u != null) {
            String suffix = Session.isMinor() ? "  🔞" : "";
            greetingLabel.setText("Bonjour " + u.getNom() + " 👋" + suffix);
        }

        // Affichage initial : toutes les recettes
        loadAll();
        updateFavBadge();
    }

    // ==================================================
    //  ACTIONS DES BOUTONS DE LA BARRE PRINCIPALE
    // ==================================================
    @FXML
    public void onSearchTab() {
        moodCombo.requestFocus();
    }

    @FXML
    public void onShowAll() {
        moodCombo.getSelectionModel().clearSelection();
        weatherCombo.getSelectionModel().clearSelection();
        ingredientsField.clear();
        loadAll();
    }

    @FXML
    public void onShowFavorites() {
        List<Recette> favs = recetteDAO.findFavorites();
        renderRecipes(favs);
    }

    @FXML
    public void onSearch() {
        String mood    = moodCombo.getValue();
        String weather = weatherCombo.getValue();
        String ing     = ingredientsField.getText();

        List<Recette> result = recetteDAO.search(mood, weather, ing);
        renderRecipes(result);
    }

    @FXML
    public void onAddRecipe() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cookmood/view/AddRecipe.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("CookMood — Ajouter une recette");
            stage.initModality(Modality.APPLICATION_MODAL);
            Scene scene = new Scene(root, 620, 720);
            scene.getStylesheets().add(getClass().getResource("/com/cookmood/view/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

            // Au retour : recharger les recettes
            loadAll();
            updateFavBadge();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR,
                    "Erreur",
                    "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    // ==================================================
    //  AFFICHAGE DES CARTES
    // ==================================================
    private void loadAll() {
        renderRecipes(recetteDAO.findAll());
    }

    private void renderRecipes(List<Recette> list) {
        recipesPane.getChildren().clear();
        resultsCountLabel.setText(list.size() + " recette(s) trouvée(s)");
        for (Recette r : list) {
            recipesPane.getChildren().add(buildCard(r));
        }
        if (list.isEmpty()) {
            Label empty = new Label("Aucune recette ne correspond aux critères. Essayez d'autres filtres.");
            empty.setStyle("-fx-text-fill: #888; -fx-font-style: italic; -fx-padding: 30 0 0 0;");
            recipesPane.getChildren().add(empty);
        }
    }

    /** Construit une carte recette identique au mock-up. */
    private VBox buildCard(Recette r) {
        VBox card = new VBox();
        card.getStyleClass().add("recipe-card");
        card.setSpacing(10);

        // -------- ligne du haut : icône + bouton favori --------
        Label icon = new Label(r.getIcone() == null || r.getIcone().isBlank() ? "🍽️" : r.getIcone());
        icon.getStyleClass().add("recipe-icon");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button favBtn = new Button(r.isFavori() ? "★" : "☆");
        favBtn.getStyleClass().add("fav-btn");
        if (r.isFavori()) favBtn.getStyleClass().add("fav-btn-active");
        favBtn.setOnAction(e -> {
            if (recetteDAO.toggleFavorite(r.getId())) {
                r.setFavori(!r.isFavori());
                favBtn.setText(r.isFavori() ? "★" : "☆");
                if (r.isFavori()) {
                    if (!favBtn.getStyleClass().contains("fav-btn-active"))
                        favBtn.getStyleClass().add("fav-btn-active");
                } else {
                    favBtn.getStyleClass().remove("fav-btn-active");
                }
                updateFavBadge();
            }
        });

        HBox topRow = new HBox(icon, spacer, favBtn);
        topRow.setAlignment(Pos.TOP_LEFT);

        // -------- nom de la recette --------
        Label name = new Label(r.getNom());
        name.getStyleClass().add("recipe-name");
        name.setWrapText(true);

        // -------- ligne des tags --------
        FlowPane tagsRow = new FlowPane(8, 6);
        tagsRow.setAlignment(Pos.CENTER_LEFT);
        if (r.getMood() != null && !r.getMood().isBlank())
            tagsRow.getChildren().add(makeTag(moodEmoji(r.getMood()) + "  " + r.getMood(), "tag-mood"));
        if (r.getWeather() != null && !r.getWeather().isBlank())
            tagsRow.getChildren().add(makeTag(weatherEmoji(r.getWeather()) + "  " + r.getWeather(), "tag-weather"));
        if (r.getAuteur() != null && !r.getAuteur().isBlank())
            tagsRow.getChildren().add(makeTag("👤  " + r.getAuteur(), "tag-author"));

        if (r.getDuree() > 0) {
            Label time = new Label("⏱ " + r.getDuree() + " min");
            time.getStyleClass().add("tag-time");
            tagsRow.getChildren().add(time);
        }

        // -------- ingrédients --------
        TextFlow ingFlow = new TextFlow();
        Text lbl = new Text("Ingrédients : ");
        lbl.getStyleClass().add("recipe-section-label");
        lbl.setStyle("-fx-font-weight: bold; -fx-fill: #2b2b2b;");
        Text ing = new Text(r.getIngredients() == null ? "" : r.getIngredients());
        ing.setStyle("-fx-fill: #4a4a4a;");
        ingFlow.getChildren().addAll(lbl, ing);

        // -------- description --------
        Label desc = new Label(r.getDescription() == null ? "" : r.getDescription());
        desc.getStyleClass().add("recipe-description");
        desc.setWrapText(true);

        VBox.setMargin(desc, new Insets(4, 0, 0, 0));

        card.getChildren().addAll(topRow, name, tagsRow, ingFlow, desc);
        return card;
    }

    private Label makeTag(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().addAll("tag", styleClass);
        return l;
    }

    // Emojis associés aux humeurs (clin d'œil au mock-up)
    private String moodEmoji(String mood) {
        switch (mood == null ? "" : mood.toLowerCase()) {
            case "joyeux":     return "🤪";
            case "festif":     return "🎉";
            case "fatigué":    return "🥱";
            case "en forme":   return "🥗";
            case "triste":     return "😔";
            case "stressé":    return "😣";
            case "romantique": return "💖";
            default:           return "🎭";
        }
    }

    private String weatherEmoji(String weather) {
        switch (weather == null ? "" : weather.toLowerCase()) {
            case "ensoleillé": return "🌤";
            case "pluvieux":   return "🌧";
            case "nuageux":    return "⛅";
            case "froid":      return "❄";
            case "chaud":      return "🔥";
            case "neigeux":    return "🌨";
            default:           return "🌦";
        }
    }

    private void updateFavBadge() {
        int count = recetteDAO.countFavorites();
        favBtn.setText("⭐  Mes Favoris  " + count);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
