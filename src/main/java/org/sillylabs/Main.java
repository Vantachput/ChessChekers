package org.sillylabs;

import javafx.application.Application;
import javafx.stage.Stage;
import org.sillylabs.gui.GameGUI;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        GameCoordinator coordinator = new GameCoordinator();
        GameGUI gui = new GameGUI(coordinator, primaryStage);
        primaryStage.setTitle("Chess-Checkers");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}