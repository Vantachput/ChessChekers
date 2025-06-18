package org.sillylabs;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        Game game = new Game();
        GameGUI gui = new GameGUI(game, primaryStage);
        primaryStage.setTitle("Chess-Checkers Game");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}