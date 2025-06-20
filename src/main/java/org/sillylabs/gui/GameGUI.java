package org.sillylabs.gui;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.sillylabs.*;
import org.sillylabs.pieces.CheckersPiece;
import org.sillylabs.pieces.Color;
import org.sillylabs.pieces.Pawn;
import org.sillylabs.pieces.Piece;

import java.util.HashMap;
import java.util.Map;

public class GameGUI {
    private final Game game;
    private final Stage primaryStage;
    private GridPane boardPane;
    private Scene scene;
    private int selectedRow = -1, selectedColumn = -1;
    private Label statusLabel;
    private Label turnLabel;
    private boolean gameOver = false;
    private TextArea whiteMovesArea;
    private TextArea blackMovesArea;

    private static final String LIGHT_SQUARE = "#F0D9B5";
    private static final String DARK_SQUARE = "#B58863";
    private static final String SELECTED_SQUARE = "#FFD700";
    private static final String POSSIBLE_MOVE = "#90EE90";
    private static final String CAPTURE_MOVE = "#FF6B6B";

    private static final Map<String, Image> pieceImageCache = new HashMap<>();

    static {
        String[] colors = {"w", "b"};
        String[] types = {"k", "q", "r", "b", "n", "p"};
        for (String color : colors) {
            for (String type : types) {
                String imageName = color + type + ".png";
                try {
                    String path = "/pieces/" + imageName;
                    Image image = new Image(GameGUI.class.getResource(path).toExternalForm());
                    pieceImageCache.put(imageName, image);
                    System.out.println("Cached image: " + path);
                } catch (Exception e) {
                    System.out.println("Failed to cache image: " + imageName);
                }
            }
        }
        String[] checkersImages = {"wm.png", "bm.png", "ww.png", "bw.png"};
        for (String imageName : checkersImages) {
            try {
                String path = "/pieces/" + imageName;
                Image image = new Image(GameGUI.class.getResource(path).toExternalForm());
                pieceImageCache.put(imageName, image);
                System.out.println("Cached image: " + path);
            } catch (Exception e) {
                System.out.println("Failed to cache image: " + imageName);
            }
        }
        pieceImageCache.putIfAbsent("wp.png", new Image(GameGUI.class.getResource("/pieces/wp.png").toExternalForm()));
    }

    public GameGUI(Game game, Stage primaryStage) {
        this.game = game;
        this.primaryStage = primaryStage;
        game.setGUI(this);
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        setupGUI();
    }

    private void setupGUI() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2C3E50;");

        DoubleBinding padding = Bindings.createDoubleBinding(
                () -> {
                    double minSize = Math.min(primaryStage.getWidth(), primaryStage.getHeight());
                    return Double.isNaN(minSize) || minSize <= 0 ? 10 : minSize / 40;
                },
                primaryStage.widthProperty(), primaryStage.heightProperty()
        );
        root.paddingProperty().bind(Bindings.createObjectBinding(
                () -> new Insets(padding.get()),
                padding
        ));
        root.spacingProperty().bind(padding);

        Label title = new Label("Шахматы-Шашки");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        title.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font("Arial", FontWeight.BOLD, padding.get() * 2.4),
                padding
        ));

        ComboBox<GameMode> modeSelector = new ComboBox<>();
        modeSelector.getItems().addAll(GameMode.values());
        modeSelector.setValue(GameMode.CHESS);
        modeSelector.styleProperty().bind(Bindings.createStringBinding(
                () -> "-fx-font-size: " + Math.max(12, padding.get() * 1.4) + "px;",
                padding
        ));

        Button startButton = new Button("Начать игру");
        startButton.styleProperty().bind(Bindings.createStringBinding(
                () -> {
                    double fontSize = Math.max(14, padding.get() * 1.6);
                    double padX = Math.max(5, padding.get() / 2);
                    double padY = Math.max(10, padding.get());
                    return String.format(
                            "-fx-font-size: %.1fpx; -fx-background-color: #3498DB; -fx-text-fill: white; " +
                                    "-fx-background-radius: 5px; -fx-padding: %.1fpx %.1fpx;",
                            fontSize, padX, padY
                    );
                },
                padding
        ));
        startButton.setOnAction(e -> {
            gameOver = false;
            setupBoard();
            game.start(modeSelector.getValue());
            primaryStage.setScene(scene);
            updateTurnLabel();
            statusLabel.setText("");
            whiteMovesArea.setText("");
            blackMovesArea.setText("");
        });

        Label instruction = new Label("Выберите режим игры и нажмите 'Начать игру'");
        instruction.setStyle("-fx-text-fill: white;");
        instruction.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font("Arial", Math.max(12, padding.get() * 1.4)),
                padding
        ));

        root.getChildren().addAll(title, instruction, modeSelector, startButton);

        Scene startScene = new Scene(root);
        primaryStage.setScene(startScene);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(500);

        boardPane = new GridPane();
        boardPane.setAlignment(Pos.CENTER);
        boardPane.setStyle("-fx-background-color: #34495E;");

        statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
        statusLabel.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font("Arial", FontWeight.BOLD, Math.max(12, padding.get() * 1.4)),
                padding
        ));

        turnLabel = new Label();
        turnLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        turnLabel.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font("Arial", FontWeight.BOLD, Math.max(14, padding.get() * 1.8)),
                padding
        ));

        VBox boardRoot = new VBox();
        boardRoot.setAlignment(Pos.CENTER);
        boardRoot.setStyle("-fx-background-color: #2C3E50;");
        boardRoot.paddingProperty().bind(Bindings.createObjectBinding(
                () -> new Insets(padding.get()),
                padding
        ));
        boardRoot.spacingProperty().bind(padding);
        boardRoot.getChildren().addAll(turnLabel, boardPane, statusLabel);

        VBox movesPanel = new VBox();
        movesPanel.setAlignment(Pos.TOP_CENTER);
        movesPanel.setStyle("-fx-background-color: #2C3E50; -fx-border-color: #34495E; -fx-border-width: 2px;");
        movesPanel.paddingProperty().bind(Bindings.createObjectBinding(
                () -> new Insets(padding.get()),
                padding
        ));
        movesPanel.spacingProperty().bind(padding);
        movesPanel.prefWidthProperty().bind(primaryStage.widthProperty().multiply(0.25));

        Label whiteMovesLabel = new Label("Ходы Белых");
        whiteMovesLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        whiteMovesLabel.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font("Arial", FontWeight.BOLD, Math.max(12, padding.get() * 1.4)),
                padding
        ));

        whiteMovesArea = new TextArea();
        whiteMovesArea.setEditable(false);
        whiteMovesArea.setStyle("-fx-background-color: #ECF0F1; -fx-text-fill: black;");
        whiteMovesArea.prefHeightProperty().bind(primaryStage.heightProperty().multiply(0.4));
        whiteMovesArea.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font("Arial", Math.max(10, padding.get() * 1.2)),
                padding
        ));

        Label blackMovesLabel = new Label("Ходы Чёрных");
        blackMovesLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        blackMovesLabel.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font("Arial", FontWeight.BOLD, Math.max(12, padding.get() * 1.4)),
                padding
        ));

        blackMovesArea = new TextArea();
        blackMovesArea.setEditable(false);
        blackMovesArea.setStyle("-fx-background-color: #ECF0F1; -fx-text-fill: black;");
        blackMovesArea.prefHeightProperty().bind(primaryStage.heightProperty().multiply(0.4));
        blackMovesArea.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font("Arial", Math.max(10, padding.get() * 1.2)),
                padding
        ));

        movesPanel.getChildren().addAll(whiteMovesLabel, whiteMovesArea, blackMovesLabel, blackMovesArea);

        HBox mainLayout = new HBox();
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.getChildren().addAll(boardRoot, movesPanel);

        scene = new Scene(mainLayout);
    }

    private void setupBoard() {
        boardPane.getChildren().clear();

        DoubleBinding squareSize = Bindings.createDoubleBinding(
                () -> {
                    double minSize = Math.min(scene.getWidth() * 0.75, scene.getHeight());
                    return Double.isNaN(minSize) || minSize <= 0 ? 40 : minSize / 10;
                },
                scene.widthProperty(), scene.heightProperty()
        );
        boardPane.hgapProperty().bind(squareSize.divide(40));
        boardPane.vgapProperty().bind(squareSize.divide(40));
        boardPane.paddingProperty().bind(Bindings.createObjectBinding(
                () -> new Insets(squareSize.get() / 10),
                squareSize
        ));

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Button square = new Button();
                square.minWidthProperty().bind(squareSize);
                square.minHeightProperty().bind(squareSize);
                square.maxWidthProperty().bind(squareSize);
                square.maxHeightProperty().bind(squareSize);
                final int finalRow = row, finalColumn = column;
                square.setOnAction(e -> handleClick(finalRow, finalColumn));
                boardPane.add(square, column, row);
            }
        }

        addBoardCoordinates(squareSize);

        Button resignButton = new Button("Сдаться");
        resignButton.styleProperty().bind(Bindings.createStringBinding(
                () -> String.format(
                        "-fx-font-size: %.1fpx; -fx-background-color: #E74C3C; -fx-text-fill: white; " +
                                "-fx-background-radius: 5px; -fx-padding: %.1fpx %.1fpx;",
                        Math.max(10, squareSize.get() / 5),
                        Math.max(2, squareSize.get() / 10),
                        Math.max(4, squareSize.get() / 5)
                ),
                squareSize
        ));
        resignButton.setOnAction(e -> {
            gameOver = true;
            selectedRow = -1;
            selectedColumn = -1;
            setupGUI();
            setStatusMessage((game.isWhiteTurn() ? Color.BLACK : Color.WHITE) + " победили по сдаче!");
        });
        GridPane.setColumnSpan(resignButton, 8);
        boardPane.add(resignButton, 0, 9);
    }

    private void addBoardCoordinates(DoubleBinding squareSize) {
        for (int column = 0; column < 8; column++) {
            Label colLabel = new Label(String.valueOf((char) ('a' + column)));
            colLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            colLabel.setAlignment(Pos.CENTER);
            colLabel.minWidthProperty().bind(squareSize);
            colLabel.fontProperty().bind(Bindings.createObjectBinding(
                    () -> Font.font("Arial", FontWeight.BOLD, Math.max(10, squareSize.get() / 5)),
                    squareSize
            ));
            boardPane.add(colLabel, column, 8);
        }

        for (int row = 0; row < 8; row++) {
            Label rowLabel = new Label(String.valueOf(8 - row));
            rowLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            rowLabel.setAlignment(Pos.CENTER);
            rowLabel.minHeightProperty().bind(squareSize);
            rowLabel.fontProperty().bind(Bindings.createObjectBinding(
                    () -> Font.font("Arial", FontWeight.BOLD, Math.max(10, squareSize.get() / 5)),
                    squareSize
            ));
            boardPane.add(rowLabel, 8, row);
        }
    }

    private void handleClick(int row, int column) {
        if (gameOver || game.isWaitingForPromotion()) {
            return;
        }

        Piece piece = game.getBoard().getPiece(row, column);

        if (selectedRow == -1) {
            // No piece selected yet, select a piece of the current player's color
            if (piece != null && piece.getColor() == (game.isWhiteTurn() ? Color.WHITE : Color.BLACK)) {
                selectedRow = row;
                selectedColumn = column;
                updateDisplay();
            }
        } else {
            // A piece is already selected
            if (piece != null && piece.getColor() == (game.isWhiteTurn() ? Color.WHITE : Color.BLACK)) {
                // Clicked another piece of the same color, reselect it
                selectedRow = row;
                selectedColumn = column;
                updateDisplay();
            } else {
                // Attempt to move or end multi-jump
                if (row == selectedRow && column == selectedColumn && game.isMultiJump()) {
                    game.makeMove(row, column, row, column);
                    selectedRow = -1;
                    selectedColumn = -1;
                } else {
                    boolean moveMade = game.makeMove(selectedRow, selectedColumn, row, column);
                    if (moveMade && !game.isWaitingForPromotion()) {
                        selectedRow = -1;
                        selectedColumn = -1;
                        if (game.isMultiJump()) {
                            selectedRow = row;
                            selectedColumn = column;
                        }
                    }
                }
                updateDisplay();
            }
        }
    }

    public void updateDisplay() {
        Piece[][] grid = game.getGrid();
        boolean isMultiJump = game.isMultiJump();
        int multiJumpRow = -1, multiJumpColumn = -1;
        if (isMultiJump) {
            multiJumpRow = selectedRow;
            multiJumpColumn = selectedColumn;
        }

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Button square = (Button) boardPane.getChildren().get(row * 8 + column);
                Piece piece = grid[row][column];
                boolean isSelected = (row == selectedRow && column == selectedColumn);
                boolean isPossibleMove = false;
                boolean isCaptureMove = false;

                if (selectedRow != -1 && !game.isWaitingForPromotion()) {
                    Piece selectedPiece = grid[selectedRow][selectedColumn];
                    if (selectedPiece != null) {
                        boolean isValidMove = game.getBoard().isValidMove(selectedRow, selectedColumn, row, column,
                                game.isWhiteTurn(), game.getGameMode(), isMultiJump);
                        if (isValidMove) {
                            isPossibleMove = true;
                            if ((selectedPiece instanceof CheckersPiece && Math.abs(row - selectedRow) >= 2 && Math.abs(column - selectedColumn) >= 2) ||
                                    grid[row][column] != null ||
                                    (selectedPiece instanceof Pawn && Math.abs(column - selectedColumn) == 1 &&
                                            row == game.getEnPassantTargetRow() && column == game.getEnPassantTargetColumn() &&
                                            game.isEnPassantPossible())) {
                                isCaptureMove = true;
                            }
                        }
                    }
                }

                square.setStyle(getSquareBackgroundStyle(row, column, isSelected, isPossibleMove, isCaptureMove));

                square.setGraphic(null);
                if (piece != null) {
                    String imageKey = getImageKey(piece);
                    Image image = pieceImageCache.get(imageKey);
                    if (image != null) {
                        ImageView imageView = new ImageView(image);
                        imageView.fitWidthProperty().bind(square.widthProperty().multiply(0.8));
                        imageView.fitHeightProperty().bind(square.heightProperty().multiply(0.8));
                        imageView.setPreserveRatio(true);
                        square.setGraphic(imageView);
                    } else {
                        System.out.println("Image not found for key: " + imageKey);
                    }
                }
            }
        }

        updateTurnLabel();
        updateMoveHistory();
    }

    private String getSquareBackgroundStyle(int row, int column, boolean isSelected, boolean isPossibleMove, boolean isCapture) {
        String baseColor;
        if (isSelected) {
            baseColor = SELECTED_SQUARE;
        } else if (isCapture) {
            baseColor = CAPTURE_MOVE;
        } else if (isPossibleMove) {
            baseColor = POSSIBLE_MOVE;
        } else {
            baseColor = (row + column) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE;
        }
        return String.format("-fx-background-color: %s;", baseColor);
    }

    private String getImageKey(Piece piece) {
        String colorPrefix = piece.getColor() == Color.WHITE ? "w" : "b";
        String type = piece.getType().toLowerCase();
        if (piece instanceof CheckersPiece checkersPiece) {
            if (checkersPiece.isKing()) {
                return colorPrefix + "w.png";
            } else {
                return colorPrefix + "m.png";
            }
        }
        return switch (type) {
            case "king" -> colorPrefix + "k.png";
            case "queen" -> colorPrefix + "q.png";
            case "rook" -> colorPrefix + "r.png";
            case "bishop" -> colorPrefix + "b.png";
            case "knight" -> colorPrefix + "n.png";
            case "pawn" -> colorPrefix + "p.png";
            default -> colorPrefix + "p.png";
        };
    }

    private void updateTurnLabel() {
        String turnText = game.isWhiteTurn() ? "Ход Белых" : "Ход Чёрных";
        turnLabel.setText(turnText);
    }

    private void updateMoveHistory() {
        StringBuilder whiteMoves = new StringBuilder();
        StringBuilder blackMoves = new StringBuilder();
        int moveNumber = 1;
        for (int i = 0; i < game.getMoveHistory().size(); i += 2) {
            whiteMoves.append(moveNumber).append(". ").append(game.getMoveHistory().get(i)).append("\n");
            if (i + 1 < game.getMoveHistory().size()) {
                blackMoves.append(moveNumber).append(". ").append(game.getMoveHistory().get(i + 1)).append("\n");
            }
            moveNumber++;
        }
        whiteMovesArea.setText(whiteMoves.toString());
        blackMovesArea.setText(blackMoves.toString());
    }

    public void showPromotionDialog(Color color) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Превращение пешки");

        VBox dialogVBox = new VBox(10);
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.setPadding(new Insets(20));
        dialogVBox.setStyle("-fx-background-color: #34495E;");

        Label label = new Label("Выберите фигуру для превращения:");
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        HBox pieceButtons = new HBox(10);
        pieceButtons.setAlignment(Pos.CENTER);

        String[] pieceTypes = {"Queen", "Rook", "Bishop", "Knight"};
        String colorPrefix = color == Color.WHITE ? "w" : "b";
        for (String pieceType : pieceTypes) {
            Button button = new Button();
            String imageKey = switch (pieceType.toLowerCase()) {
                case "queen" -> colorPrefix + "q.png";
                case "rook" -> colorPrefix + "r.png";
                case "bishop" -> colorPrefix + "b.png";
                case "knight" -> colorPrefix + "n.png";
                default -> colorPrefix + "q.png";
            };
            Image image = pieceImageCache.get(imageKey);
            if (image != null) {
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(40);
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true);
                button.setGraphic(imageView);
            }
            button.setStyle("-fx-background-color: #3498DB; -fx-background-radius: 5px;");
            button.setOnAction(e -> {
                game.completePawnPromotion(pieceType);
                dialog.close();
            });
            pieceButtons.getChildren().add(button);
        }

        dialogVBox.getChildren().addAll(label, pieceButtons);
        Scene dialogScene = new Scene(dialogVBox);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    public void setStatusMessage(String message) {
        statusLabel.setText(message);
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
}