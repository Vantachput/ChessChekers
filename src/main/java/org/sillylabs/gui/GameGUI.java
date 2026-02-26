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
import org.sillylabs.pieces.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Alert;

public class GameGUI implements GameObserver {
    private final GameCoordinator coordinator;
    private final Stage primaryStage;
    private GridPane boardPane;
    private Scene scene;
    private int selectedRow = -1, selectedColumn = -1;
    private Label statusLabel;
    private Label turnLabel;
    private boolean gameOver = false;
    private TextArea whiteMovesArea;
    private TextArea blackMovesArea;

    // --- НОВЕ: Прапорець для перевірки режиму гри (Проти ШІ чи 2 гравця) ---
    private boolean playAgainstAI = false;

    private StockfishEngine stockfishEngine;

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

    public GameGUI(GameCoordinator coordinator, Stage primaryStage) {
        this.coordinator = coordinator;
        this.primaryStage = primaryStage;
        coordinator.addObserver(this);
        primaryStage.setWidth(1000);
        primaryStage.setHeight(800);

        this.stockfishEngine = new StockfishEngine();
        this.stockfishEngine.startEngine();

        this.primaryStage.setOnCloseRequest(event -> {
            if (stockfishEngine != null) {
                stockfishEngine.stopEngine();
            }
        });

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

        Label title = new Label("Шахи-Шашки");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        title.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font("Arial", FontWeight.BOLD, padding.get() * 2.4),
                padding
        ));

        // Вибір гри
        ComboBox<GameMode> modeSelector = new ComboBox<>();
        modeSelector.getItems().addAll(GameMode.CHESS, GameMode.CHECKERS);
        modeSelector.setValue(GameMode.CHESS);
        modeSelector.styleProperty().bind(Bindings.createStringBinding(
                () -> "-fx-font-size: " + Math.max(12, padding.get() * 1.4) + "px;",
                padding
        ));

        // --- НОВЕ: Вибір суперника ---
        ComboBox<String> opponentSelector = new ComboBox<>();
        opponentSelector.getItems().addAll("2 гравця", "Проти ШІ");
        opponentSelector.setValue("2 гравця"); // За замовчуванням 2 гравця
        opponentSelector.styleProperty().bind(Bindings.createStringBinding(
                () -> "-fx-font-size: " + Math.max(12, padding.get() * 1.4) + "px;",
                padding
        ));

        // Блокуємо ШІ, якщо обрано Шашки
        modeSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == GameMode.CHECKERS) {
                opponentSelector.setValue("2 гравця");
                opponentSelector.setDisable(true); // Stockfish грає тільки в шахи
            } else {
                opponentSelector.setDisable(false);
            }
        });
        // ------------------------------

        Button startButton = new Button("Почати гру");
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
            // Зберігаємо вибір користувача
            playAgainstAI = opponentSelector.getValue().equals("Проти ШІ");

            gameOver = false;
            setupBoard();
            coordinator.start(modeSelector.getValue());
            primaryStage.setScene(scene);

            triggerBotMoveIfNeeded();
        });

        Label instruction = new Label("Виберіть режим, суперника та натисніть 'Почати гру'");
        instruction.setStyle("-fx-text-fill: white;");
        instruction.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font("Arial", Math.max(12, padding.get() * 1.4)),
                padding
        ));

        // Додали opponentSelector у контейнер
        root.getChildren().addAll(title, instruction, modeSelector, opponentSelector, startButton);

        Scene startScene = new Scene(root);
        primaryStage.setScene(startScene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(700);

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

        Label whiteMovesLabel = new Label("Ходи Білих");
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
                () -> Font.font("Monospaced", Math.max(10, padding.get() * 0.9)),
                padding
        ));
        whiteMovesArea.textProperty().addListener((observable, oldValue, newValue) -> {
            whiteMovesArea.setScrollTop(Double.MAX_VALUE);
        });

        Label blackMovesLabel = new Label("Ходи Чорних");
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
                () -> Font.font("Monospaced", Math.max(10, padding.get() * 0.9)),
                padding
        ));
        blackMovesArea.textProperty().addListener((observable, oldValue, newValue) -> {
            blackMovesArea.setScrollTop(Double.MAX_VALUE);
        });

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
        boardPane.hgapProperty().bind(squareSize.divide(10));
        boardPane.vgapProperty().bind(squareSize.divide(10));
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

        Button drawButton = new Button("Нічия");
        Button resignButton = new Button("Здатися");
        Button mainMenuButton = new Button("Головне меню");

        javafx.beans.binding.StringBinding buttonStyle = Bindings.createStringBinding(
                () -> String.format(
                        "-fx-font-size: %.1fpx; -fx-text-fill: white; -fx-background-radius: 5px; -fx-padding: %.1fpx %.1fpx; -fx-cursor: hand;",
                        Math.max(10, squareSize.get() / 5),
                        Math.max(2, squareSize.get() / 5),
                        Math.max(4, squareSize.get() / 2)
                ),
                squareSize
        );

        drawButton.styleProperty().bind(Bindings.concat("-fx-background-color: #F39C12; ", buttonStyle));
        resignButton.styleProperty().bind(Bindings.concat("-fx-background-color: #E74C3C; ", buttonStyle));
        mainMenuButton.styleProperty().bind(Bindings.concat("-fx-background-color: #3498DB; ", buttonStyle));

        drawButton.setOnAction(e -> {
            if (!gameOver) {
                gameOver = true;
                selectedRow = -1;
                selectedColumn = -1;
                onGameOver(true, null);
            }
        });

        resignButton.setOnAction(e -> {
            if (!gameOver) {
                gameOver = true;
                selectedRow = -1;
                selectedColumn = -1;
                Color winner = coordinator.isWhiteTurn() ? Color.BLACK : Color.WHITE;
                onGameOver(true, winner);
            }
        });

        mainMenuButton.setOnAction(e -> {
            gameOver = true;
            selectedRow = -1;
            selectedColumn = -1;
            setupGUI();
        });

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        buttonBox.getChildren().addAll(drawButton, resignButton, spacer, mainMenuButton);

        GridPane.setColumnSpan(buttonBox, 8);
        boardPane.add(buttonBox, 0, 9);
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
        // --- НОВЕ: Забороняємо кліки, тільки якщо зараз хід бота І увімкнено режим Проти ШІ ---
        if (gameOver || coordinator.isWaitingForPromotion() ||
                (playAgainstAI && !coordinator.isWhiteTurn() && coordinator.getGameMode() == GameMode.CHESS)) {
            return;
        }

        Piece piece = coordinator.getBoardState()[row][column];

        if (selectedRow == -1 && piece != null && piece.getColor() == (coordinator.isWhiteTurn() ? Color.WHITE : Color.BLACK)) {
            if (coordinator.isMultiJump() && (row != coordinator.getMultiJumpFromRow() || column != coordinator.getMultiJumpFromColumn())) {
                onStatusUpdate("Завершіть серію взяттів!");
                return;
            }
            selectedRow = row;
            selectedColumn = column;
            updateBoardDisplay();
            highlightPossibleMoves(row, column);
        } else if (selectedRow != -1) {
            if (row == selectedRow && column == selectedColumn && coordinator.isMultiJump()) {
                coordinator.makeMove(selectedRow, selectedColumn, row, column);
                selectedRow = -1;
                selectedColumn = -1;
                updateBoardDisplay();
            } else {
                boolean moveMade = coordinator.makeMove(selectedRow, selectedColumn, row, column);
                if (moveMade && !coordinator.isWaitingForPromotion()) {
                    selectedRow = -1;
                    selectedColumn = -1;
                    updateBoardDisplay();
                    updateTurnLabel();
                    updateMoveHistory();

                    triggerBotMoveIfNeeded();

                } else if (moveMade && coordinator.isMultiJump()) {
                    selectedRow = row;
                    selectedColumn = column;
                    updateBoardDisplay();
                    highlightPossibleMoves(row, column);
                } else {
                    selectedRow = -1;
                    selectedColumn = -1;
                    updateBoardDisplay();
                }
            }
        }
    }

    private void highlightPossibleMoves(int fromRow, int fromColumn) {
        Piece piece = coordinator.getBoardState()[fromRow][fromColumn];
        if (piece == null) return;

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Button square = getSquareButton(row, column);
                if (square == null) continue;

                if (coordinator.isValidMove(fromRow, fromColumn, row, column)) {
                    boolean isCapture = false;
                    if (piece instanceof CheckersPiece checkersPiece) {
                        List<int[]> captureMoves = checkersPiece.getCaptureMoves(fromRow, fromColumn, coordinator.getBoardState(), coordinator.getGameMode());
                        for (int[] move : captureMoves) {
                            if (move[0] == row && move[1] == column) {
                                isCapture = true;
                                break;
                            }
                        }
                    } else if (piece instanceof Pawn &&
                            Math.abs(column - fromColumn) == 1 &&
                            coordinator.getBoardState()[row][column] == null &&
                            row == coordinator.getEnPassantTargetRow() &&
                            column == coordinator.getEnPassantTargetColumn() &&
                            coordinator.isEnPassantPossible()) {
                        isCapture = true;
                    } else if (coordinator.getBoardState()[row][column] != null &&
                            coordinator.getBoardState()[row][column].getColor() != piece.getColor()) {
                        isCapture = true;
                    }

                    square.setStyle(getSquareStyle(row, column, isCapture ? CAPTURE_MOVE : POSSIBLE_MOVE));
                }
            }
        }
    }

    private Button getSquareButton(int row, int column) {
        for (var node : boardPane.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column && node instanceof Button) {
                return (Button) node;
            }
        }
        return null;
    }

    private String getSquareStyle(int row, int column, String highlightColor) {
        String baseColor = (row + column) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE;
        if (row == selectedRow && column == selectedColumn) {
            return "-fx-background-color: " + SELECTED_SQUARE + ";";
        }
        return "-fx-background-color: " + (highlightColor != null ? highlightColor : baseColor) + ";";
    }

    @Override
    public void onBoardChanged() {
        updateBoardDisplay();
        updateMoveHistory();
        updateTurnLabel();
    }

    @Override
    public void onStatusUpdate(String message) {
        statusLabel.setText(message);
    }

    @Override
    public void onPromotionRequested(int row, int column, Color color) {
        // Якщо грає бот - перетворює автоматично
        if (playAgainstAI && color == Color.BLACK && coordinator.getGameMode() == GameMode.CHESS) {
            // Діалог не показуємо, обробка перетворення виконується у triggerBotMoveIfNeeded
        } else {
            showPromotionDialog(row, column, color);
        }
    }

    @Override
    public void onGameOver(boolean isGameOver, Color winner) {
        this.gameOver = isGameOver;
        if (isGameOver) {
            String title = "Кінець гри";
            String headerText;

            if (winner == null) {
                headerText = "Нічия!";
                statusLabel.setText("Гра закінчена: Нічия!");
            } else {
                String winnerText = (winner == Color.WHITE) ? "Білі" : "Чорні";
                headerText = "Перемога " + winnerText + "!";
                statusLabel.setText("Гра закінчена: Перемога " + winnerText + "!");
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(headerText);
            alert.setContentText("Щоб почати нову гру, натисніть 'Головне меню'.");
            alert.showAndWait();
        }
    }

    private void updateBoardDisplay() {
        Piece[][] grid = coordinator.getBoardState();

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Button square = getSquareButton(row, column);
                if (square == null) continue;

                Piece piece = grid[row][column];
                square.setGraphic(null);
                square.setText("");

                if (piece != null) {
                    String imageKey;
                    if (piece instanceof CheckersPiece checkersPiece) {
                        String colorPrefix = piece.getColor() == Color.WHITE ? "w" : "b";
                        String typeSuffix = checkersPiece.isKing() ? "w" : "m";
                        imageKey = colorPrefix + typeSuffix + ".png";
                    } else {
                        String colorPrefix = piece.getColor() == Color.WHITE ? "w" : "b";
                        String typeKey = switch (piece.getType().toLowerCase()) {
                            case "king" -> "k";
                            case "queen" -> "q";
                            case "rook" -> "r";
                            case "bishop" -> "b";
                            case "knight" -> "n";
                            case "pawn" -> "p";
                            default -> "";
                        };
                        imageKey = colorPrefix + typeKey + ".png";
                    }

                    Image image = pieceImageCache.get(imageKey);
                    if (image != null) {
                        ImageView imageView = new ImageView(image);
                        imageView.fitWidthProperty().bind(square.widthProperty().multiply(0.8));
                        imageView.fitHeightProperty().bind(square.heightProperty().multiply(0.8));
                        imageView.setPreserveRatio(true);
                        square.setGraphic(imageView);
                    } else {
                        String symbol = getPieceSymbol(piece);
                        square.setText(symbol);
                        square.setStyle(getSquareStyle(row, column, null) + getPieceTextStyle(piece.getColor()));
                    }
                }
                square.setStyle(getSquareStyle(row, column, null));
            }
        }
    }

    private String getPieceSymbol(Piece piece) {
        if (piece instanceof CheckersPiece checkersPiece) {
            return checkersPiece.isKing() ? "⛁" : "⛂";
        }
        return switch (piece.getType()) {
            case "King" -> piece.getColor() == Color.WHITE ? "♔" : "♚";
            case "Queen" -> piece.getColor() == Color.WHITE ? "♕" : "♛";
            case "Rook" -> piece.getColor() == Color.WHITE ? "♖" : "♜";
            case "Bishop" -> piece.getColor() == Color.WHITE ? "♗" : "♝";
            case "Knight" -> piece.getColor() == Color.WHITE ? "♘" : "♞";
            case "Pawn" -> piece.getColor() == Color.WHITE ? "♙" : "♟";
            default -> "";
        };
    }

    private String getPieceTextStyle(Color pieceColor) {
        return "-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " +
                (pieceColor == Color.WHITE ? "white" : "black") + "; -fx-alignment: center;";
    }

    private void updateMoveHistory() {
        List<String> moves = coordinator.getMoveHistory();
        StringBuilder whiteMoves = new StringBuilder();
        StringBuilder blackMoves = new StringBuilder();

        for (int i = 0; i < moves.size(); i++) {
            if (i % 2 == 0) {
                whiteMoves.append((i / 2 + 1)).append(". ").append(moves.get(i)).append("\n");
            } else {
                blackMoves.append((i / 2 + 1)).append(". ").append(moves.get(i)).append("\n");
            }
        }

        whiteMovesArea.setText(whiteMoves.toString());
        blackMovesArea.setText(blackMoves.toString());
    }

    private void updateTurnLabel() {
        turnLabel.setText("Хід: " + (coordinator.isWhiteTurn() ? "Білі" : "Чорні"));
    }

    private void showPromotionDialog(int row, int column, Color color) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Перетворення пішака");

        VBox dialogVbox = new VBox(10);
        dialogVbox.setAlignment(Pos.CENTER);
        dialogVbox.setPadding(new Insets(20));
        dialogVbox.setStyle("-fx-background-color: #34495E;");

        Label label = new Label("Виберіть фігуру для перетворення:");
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        ComboBox<String> pieceSelector = new ComboBox<>();
        pieceSelector.getItems().addAll("Ферзь", "Тура", "Слон", "Кінь");
        pieceSelector.setValue("Ферзь");
        pieceSelector.setStyle("-fx-font-size: 12px;");

        Button confirmButton = new Button("Підтвердити");
        confirmButton.setStyle("-fx-font-size: 12px; -fx-background-color: #3498DB; -fx-text-fill: white; -fx-background-radius: 5px;");
        confirmButton.setOnAction(e -> {
            String selected = pieceSelector.getValue();
            String englishPieceType = switch (selected) {
                case "Ферзь" -> "Queen";
                case "Тура" -> "Rook";
                case "Слон" -> "Bishop";
                case "Кінь" -> "Knight";
                default -> "Queen";
            };
            coordinator.completePawnPromotion(englishPieceType);
            dialog.close();

            triggerBotMoveIfNeeded();
        });

        dialogVbox.getChildren().addAll(label, pieceSelector, confirmButton);
        Scene dialogScene = new Scene(dialogVbox, 250, 150);
        dialog.setScene(dialogScene);
        dialog.show();
    }

    private void triggerBotMoveIfNeeded() {
        // --- НОВЕ: Перевіряємо змінну playAgainstAI ---
        if (playAgainstAI && !gameOver && !coordinator.isWhiteTurn() && coordinator.getGameMode() == GameMode.CHESS) {

            new Thread(() -> {
                String fen = generateFEN(coordinator.getBoardState(), coordinator.isWhiteTurn());
                String bestMove = stockfishEngine.getBestMove(fen, 500);

                if (bestMove != null && bestMove.length() >= 4) {
                    int fromCol = bestMove.charAt(0) - 'a';
                    int fromRow = 8 - Character.getNumericValue(bestMove.charAt(1));
                    int toCol = bestMove.charAt(2) - 'a';
                    int toRow = 8 - Character.getNumericValue(bestMove.charAt(3));

                    String promotionPiece = bestMove.length() > 4 ? String.valueOf(bestMove.charAt(4)) : null;

                    javafx.application.Platform.runLater(() -> {
                        boolean botMoveMade = coordinator.makeMove(fromRow, fromCol, toRow, toCol);

                        if (botMoveMade && coordinator.isWaitingForPromotion()) {
                            String pieceName = "Queen";
                            if (promotionPiece != null) {
                                switch (promotionPiece) {
                                    case "r": pieceName = "Rook"; break;
                                    case "b": pieceName = "Bishop"; break;
                                    case "n": pieceName = "Knight"; break;
                                    case "q": pieceName = "Queen"; break;
                                }
                            }
                            coordinator.completePawnPromotion(pieceName);
                        }

                        updateBoardDisplay();
                        updateTurnLabel();
                        updateMoveHistory();
                    });
                }
            }).start();
        }
    }

    private String generateFEN(Piece[][] board, boolean isWhiteTurn) {
        StringBuilder fen = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            int emptySquares = 0;
            for (int col = 0; col < 8; col++) {
                Piece p = board[row][col];
                if (p == null) {
                    emptySquares++;
                } else {
                    if (emptySquares > 0) {
                        fen.append(emptySquares);
                        emptySquares = 0;
                    }
                    char pieceChar = getPieceChar(p);
                    if (p.getColor() == Color.WHITE) {
                        fen.append(Character.toUpperCase(pieceChar));
                    } else {
                        fen.append(Character.toLowerCase(pieceChar));
                    }
                }
            }
            if (emptySquares > 0) {
                fen.append(emptySquares);
            }
            if (row < 7) {
                fen.append("/");
            }
        }
        fen.append(isWhiteTurn ? " w" : " b").append(" - - 0 1");
        return fen.toString();
    }

    private char getPieceChar(Piece p) {
        String name = p.getType();
        return switch (name) {
            case "Pawn" -> 'p';
            case "Knight" -> 'n';
            case "Bishop" -> 'b';
            case "Rook" -> 'r';
            case "Queen" -> 'q';
            case "King" -> 'k';
            default -> 'p';
        };
    }
}