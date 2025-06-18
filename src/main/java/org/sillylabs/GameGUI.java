package org.sillylabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GameGUI {
    private Game game;
    private Stage primaryStage;
    private GridPane boardPane;
    private Scene scene;
    private int selectedX = -1, selectedY = -1;
    private Label statusLabel;

    public GameGUI(Game game, Stage primaryStage) {
        this.game = game;
        this.primaryStage = primaryStage;
        game.setGUI(this);
        setupGUI();
    }

    private void setupGUI() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.CENTER);

        ComboBox<String> modeSelector = new ComboBox<>();
        modeSelector.getItems().addAll("Chess", "Checkers", "Hybrid", "Unified");
        modeSelector.setValue("Chess");
        Button startButton = new Button("Start Game");
        startButton.setOnAction(e -> {
            game.start(modeSelector.getValue());
            primaryStage.setScene(scene);
        });

        Label instruction = new Label("Select mode and click Start Game");
        root.getChildren().addAll(instruction, modeSelector, startButton);

        Scene startScene = new Scene(root, 400, 200);
        primaryStage.setScene(startScene);

        boardPane = new GridPane();
        statusLabel = new Label("");
        setupBoard();
        VBox boardRoot = new VBox(10, boardPane, statusLabel);
        boardRoot.setAlignment(Pos.CENTER);
        scene = new Scene(boardRoot, 480, 550);
    }

    private void setupBoard() {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Button square = new Button();
                square.setMinSize(60, 60);
                square.setFont(new Font(24)); // Larger font for bigger pieces
                square.setStyle((x + y) % 2 == 0 ? "-fx-background-color: white;" : "-fx-background-color: gray;");
                final int fx = x, fy = y;
                square.setOnAction(e -> handleClick(fx, fy));
                boardPane.add(square, y, x); // Swap x, y to align with grid[x][y]
            }
        }
        Button resignButton = new Button("Resign");
        resignButton.setOnAction(e -> primaryStage.close());
        boardPane.add(resignButton, 0, 8, 8, 1);
    }

    public void updateDisplay() {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                // Adjust index to match GridPane column-row order
                Button square = (Button) boardPane.getChildren().get(y * 8 + x);
                Piece piece = game.getBoard().getPiece(x, y);
                square.setText(piece == null ? "" : getPieceSymbol(piece));
            }
        }
        statusLabel.setText("");
    }

    private String getPieceSymbol(Piece piece) {
        String color = piece.getColor();
        String type = piece.getType();
        if (color.equals("White")) {
            return switch (type) {
                case "King" -> "♔";
                case "Queen" -> "♕";
                case "Rook" -> "♖";
                case "Bishop" -> "♗";
                case "Knight" -> "♘";
                case "Pawn" -> "♙";
                case "CheckersMan" -> piece instanceof CheckersPiece && ((CheckersPiece) piece).isKing() ? "⛁" : "⛀";
                default -> "?";
            };
        } else {
            return switch (type) {
                case "King" -> "♚";
                case "Queen" -> "♛";
                case "Rook" -> "♜";
                case "Bishop" -> "♝";
                case "Knight" -> "♞";
                case "Pawn" -> "♟";
                case "CheckersMan" -> piece instanceof CheckersPiece && ((CheckersPiece) piece).isKing() ? "⛃" : "⛂";
                default -> "?";
            };
        }
    }

    private void handleClick(int x, int y) {
        System.out.println("Clicked: (" + x + ", " + y + ")");
        if (selectedX == -1) {
            Piece piece = game.getBoard().getPiece(x, y);
            if (piece != null && piece.getColor().equals(game.isWhiteTurn() ? "White" : "Black")) {
                selectedX = x;
                selectedY = y;
                statusLabel.setText("Selected " + piece.getType() + " at (" + x + ", " + y + ")");
                System.out.println("Selected piece: " + piece.getType() + " at (" + x + ", " + y + ")");
            } else {
                statusLabel.setText("Invalid selection");
                System.out.println("Invalid selection at (" + x + ", " + y + ")");
            }
        } else {
            System.out.println("Attempting move from (" + selectedX + ", " + selectedY + ") to (" + x + ", " + y + ")");
            if (game.makeMove(selectedX, selectedY, x, y)) {
                statusLabel.setText("Move successful");
                System.out.println("Move successful");
                if (game.isGameOver()) {
                    statusLabel.setText("Game over: Checkmate!");
                    primaryStage.close();
                }
            } else {
                statusLabel.setText("Invalid move");
                System.out.println("Move invalid");
            }
            selectedX = -1;
            selectedY = -1;
        }
    }

    public Scene getScene() {
        return scene;
    }
}