package com.cookmood.controller;

import com.cookmood.dao.UserDAO;
import com.cookmood.model.Session;
import com.cookmood.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Contrôleur de l'écran de connexion.
 * Crée ou récupère l'utilisateur en BDD puis ouvre l'écran principal.
 */
public class LoginController {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private Spinner<Integer> ageSpinner;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        SpinnerValueFactory<Integer> factory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 20, 1);
        ageSpinner.setValueFactory(factory);
    }

    @FXML
    public void onLogin() {
        // ----- validation -----
        String nom    = nomField.getText() == null ? "" : nomField.getText().trim();
        String prenom = prenomField.getText() == null ? "" : prenomField.getText().trim();
        Integer age   = ageSpinner.getValue();

        if (nom.isBlank()) {
            alert(Alert.AlertType.WARNING, "Veuillez saisir votre nom.");
            return;
        }
        if (prenom.isBlank()) {
            alert(Alert.AlertType.WARNING, "Veuillez saisir votre prénom.");
            return;
        }
        if (age == null || age < 1 || age > 120) {
            alert(Alert.AlertType.WARNING, "Veuillez saisir un âge valide.");
            return;
        }

        // ----- créer ou retrouver l'utilisateur -----
        User user = userDAO.findOrCreate(nom, prenom, age);
        if (user == null) {
            alert(Alert.AlertType.ERROR,
                    "Connexion impossible. Vérifiez la base MySQL et réessayez.");
            return;
        }

        // ----- enregistrer dans la session -----
        Session.setCurrentUser(user);

        // ----- basculer vers l'écran principal -----
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cookmood/view/Main.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) nomField.getScene().getWindow();
            Scene scene = new Scene(root, 1100, 780);
            scene.getStylesheets().add(
                    getClass().getResource("/com/cookmood/view/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("CookMood — " + Session.displayName());
            stage.setMinWidth(900);
            stage.setMinHeight(700);
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
            alert(Alert.AlertType.ERROR,
                    "Impossible de charger l'écran principal : " + e.getMessage());
        }
    }

    private void alert(Alert.AlertType type, String content) {
        Alert a = new Alert(type);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
