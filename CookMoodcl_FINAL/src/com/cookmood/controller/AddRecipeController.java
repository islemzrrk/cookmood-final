package com.cookmood.controller;

import com.cookmood.dao.RecetteDAO;
import com.cookmood.model.Recette;
import com.cookmood.model.Session;
import com.cookmood.model.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Arrays;

/**
 * Contrôleur du formulaire d'ajout de recette.
 * Pré-remplit le champ "Auteur" avec l'utilisateur connecté ; pour un mineur,
 * la case "alcool" est verrouillée à false.
 */
public class AddRecipeController {

    @FXML private TextField nomField;
    @FXML private ComboBox<String> moodCombo;
    @FXML private ComboBox<String> weatherCombo;
    @FXML private TextField auteurField;
    @FXML private Spinner<Integer> dureeSpinner;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField iconeField;
    @FXML private TextField ingredientsField;
    @FXML private TextField descriptionField;
    @FXML private TextArea  instructionsArea;
    @FXML private CheckBox  alcoolCheck;

    private final RecetteDAO recetteDAO = new RecetteDAO();

    @FXML
    public void initialize() {
        moodCombo.setItems(FXCollections.observableArrayList(
                "Joyeux", "Festif", "Fatigué", "En forme", "Triste", "Stressé", "Romantique"));
        weatherCombo.setItems(FXCollections.observableArrayList(
                "Ensoleillé", "Pluvieux", "Nuageux", "Froid", "Chaud", "Neigeux"));
        typeCombo.setItems(FXCollections.observableArrayList(
                Arrays.asList("entrée", "plat", "soupe", "dessert", "boisson", "autre")));

        SpinnerValueFactory<Integer> factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 600, 30, 5);
        dureeSpinner.setValueFactory(factory);

        // ---- Pré-remplissage de l'auteur depuis la session ----
        User u = Session.getCurrentUser();
        if (u != null) {
            auteurField.setText(u.getNom());
        }

        // ---- Verrouillage de l'alcool pour un mineur ----
        if (Session.isMinor()) {
            alcoolCheck.setSelected(false);
            alcoolCheck.setDisable(true);
            alcoolCheck.setText("Contient de l'alcool  (réservé aux 18+)");
        }
    }

    @FXML
    public void onSave() {
        if (nomField.getText() == null || nomField.getText().isBlank()) {
            alert(Alert.AlertType.WARNING, "Le nom de la recette est obligatoire.");
            return;
        }
        if (auteurField.getText() == null || auteurField.getText().isBlank()) {
            alert(Alert.AlertType.WARNING, "Veuillez indiquer un auteur.");
            return;
        }

        Recette r = new Recette();
        r.setNom(nomField.getText().trim());
        r.setMood(moodCombo.getValue());
        r.setWeather(weatherCombo.getValue());
        r.setAuteur(auteurField.getText().trim());
        r.setDuree(dureeSpinner.getValue() == null ? 0 : dureeSpinner.getValue());
        r.setType(typeCombo.getValue());
        r.setIcone(iconeField.getText() == null || iconeField.getText().isBlank()
                ? "🍽️" : iconeField.getText().trim());
        r.setIngredients(ingredientsField.getText());
        r.setDescription(descriptionField.getText());
        r.setInstructions(instructionsArea.getText());
        r.setAlcool(!Session.isMinor() && alcoolCheck.isSelected());
        r.setFavori(false);

        if (recetteDAO.insert(r)) {
            alert(Alert.AlertType.INFORMATION, "Recette ajoutée avec succès !");
            close();
        } else {
            alert(Alert.AlertType.ERROR,
                    "Échec de l'enregistrement. Vérifiez la connexion MySQL et réessayez.");
        }
    }

    @FXML
    public void onCancel() { close(); }

    private void close() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }

    private void alert(Alert.AlertType type, String content) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
