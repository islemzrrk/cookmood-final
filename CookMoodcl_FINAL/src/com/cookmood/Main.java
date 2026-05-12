package com.cookmood;

import com.cookmood.dao.DBConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Point d'entrée de l'application CookMood.
 * Charge l'écran de connexion en premier ; le LoginController bascule
 * ensuite vers l'écran principal après connexion réussie.
 */
public class Main extends Application {

    public static final String APP_TITLE = "CookMood — Connexion";

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cookmood/view/Login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 500, 580);
        scene.getStylesheets().add(getClass().getResource("/com/cookmood/view/styles.css").toExternalForm());

        stage.setTitle(APP_TITLE);
        stage.setScene(scene);
        stage.setMinWidth(420);
        stage.setMinHeight(540);
        stage.show();
    }

    @Override
    public void stop() {
        DBConnection.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
