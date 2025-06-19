package org.sillylabs;

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
import java.util.HashMap;
import java.util.Map;

public class GameGUI {
    private Game game;
    private Stage primaryStage;
    private GridPane boardPane;
    private Scene scene;
    private int selectedX = -1, selectedY = -1;
    private Label statusLabel;
    private Label turnLabel;
    private boolean gameOver = false;
    private TextArea whiteMovesArea;
    private TextArea blackMovesArea;

    // Colors for the interface
    private static final String LIGHT_SQUARE = "#F0D9B5";
    private static final String DARK_SQUARE = "#B58863";
    private static final String SELECTED_SQUARE = "#FFD700";
    private static final String POSSIBLE_MOVE = "#90EE90";
    private static final String CAPTURE_MOVE = "#FF6B6B";

    private static final Map<String, Image> pieceImageCache = new HashMap<>();

    // Initialize image cache
    static {
        // Cache chess pieces
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
        // Cache checkers pieces
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
        // Fallback image
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

        ComboBox<String> modeSelector = new ComboBox<>();
        modeSelector.getItems().addAll("Chess", "Checkers", "Hybrid", "Unified");
        modeSelector.setValue("Chess");
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

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Button square = new Button();
                square.minWidthProperty().bind(squareSize);
                square.minHeightProperty().bind(squareSize);
                square.maxWidthProperty().bind(squareSize);
                square.maxHeightProperty().bind(squareSize);
                final int fx = x, fy = y;
                square.setOnAction(e -> handleClick(fx, fy));
                boardPane.add(square, y, x);
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
        resignButton.setOnAction(e -> primaryStage.close());
        GridPane.setColumnSpan(resignButton, 8);
        boardPane.add(resignButton, 0, 9);
    }

    private void addBoardCoordinates(DoubleBinding squareSize) {
        for (int y = 0; y < 8; y++) {
            Label colLabel = new Label(String.valueOf((char)('a' + y)));
            colLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            colLabel.setAlignment(Pos.CENTER);
            colLabel.minWidthProperty().bind(squareSize);
            colLabel.fontProperty().bind(Bindings.createObjectBinding(
                    () -> Font.font("Arial", FontWeight.BOLD, Math.max(10, squareSize.get() / 5)),
                    squareSize
            ));
            boardPane.add(colLabel, y, 8);
        }

        for (int x = 0; x < 8; x++) {
            Label rowLabel = new Label(String.valueOf(8 - x));
            rowLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            rowLabel.setAlignment(Pos.CENTER);
            rowLabel.minHeightProperty().bind(squareSize);
            rowLabel.fontProperty().bind(Bindings.createObjectBinding(
                    () -> Font.font("Arial", FontWeight.BOLD, Math.max(10, squareSize.get() / 5)),
                    squareSize
            ));
            boardPane.add(rowLabel, 8, x);
        }
    }

    private String getSquareBackgroundStyle(int x, int y, boolean isSelected, boolean isPossibleMove, boolean isCapture) {
        String baseColor;
        if (isSelected) {
            baseColor = SELECTED_SQUARE;
        } else if (isCapture) {
            baseColor = CAPTURE_MOVE;
        } else if (isPossibleMove) {
            baseColor = POSSIBLE_MOVE;
        } else {
            baseColor = (x + y) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE;
        }

        double borderWidth = Math.max(1.0, primaryStage.getWidth() / 400);
        double borderRadius = Math.max(3.0, primaryStage.getWidth() / 160);
        if (Double.isNaN(borderWidth)) borderWidth = 1.0;
        if (Double.isNaN(borderRadius)) borderRadius = 3.0;

        return String.format(
                "-fx-background-color: %s; -fx-border-color: #34495E; -fx-border-width: %.1fpx; " +
                        "-fx-background-radius: %.1fpx; -fx-border-radius: %.1fpx;",
                baseColor, borderWidth, borderRadius, borderRadius
        );
    }

    public void updateDisplay() {
        DoubleBinding squareSize = Bindings.createDoubleBinding(
                () -> {
                    double minSize = Math.min(scene.getWidth() * 0.75, scene.getHeight());
                    return Double.isNaN(minSize) || minSize <= 0 ? 40 : minSize / 10;
                },
                scene.widthProperty(), scene.heightProperty()
        );
        DoubleBinding imageSize = squareSize.multiply(0.8);

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Button square = (Button) boardPane.getChildren().get(x * 8 + y);
                Piece piece = game.getBoard().getPiece(x, y);

                boolean isSelected = (x == selectedX && y == selectedY);
                boolean isPossibleMove = false;
                boolean isCapture = false;

                if (selectedX != -1 && selectedY != -1 && !isSelected) {
                    if (game.getBoard().isValidMove(selectedX, selectedY, x, y, game.isWhiteTurn(), game.getGameMode())) {
                        if (piece != null) {
                            isCapture = true;
                        } else {
                            isPossibleMove = true;
                        }
                    }
                }

                String backgroundStyle = getSquareBackgroundStyle(x, y, isSelected, isPossibleMove, isCapture);

                if (piece == null) {
                    square.setGraphic(null);
                    square.setStyle(backgroundStyle);
                } else {
                    Image pieceImage = getPieceImage(piece);
                    ImageView imageView = new ImageView(pieceImage);
                    imageView.fitWidthProperty().bind(imageSize);
                    imageView.fitHeightProperty().bind(imageSize);
                    square.setGraphic(imageView);
                    square.setStyle(backgroundStyle);
                }
            }
        }
        updateTurnLabel();
        updateMovesDisplay();
    }

    private Image getPieceImage(Piece piece) {
        String color = piece.getColor().toLowerCase().charAt(0) + "";
        String type = piece.getType().toLowerCase();
        String imageName;

        if (piece instanceof CheckersPiece) {
            if (((CheckersPiece) piece).isKing()) {
                imageName = color + "w.png"; // ww.png for White king, bw.png for Black king
            } else {
                imageName = color + "m.png"; // wm.png for White man, bm.png for Black man
            }
        } else {
            imageName = switch (type) {
                case "king" -> color + "k.png";
                case "queen" -> color + "q.png";
                case "rook" -> color + "r.png";
                case "bishop" -> color + "b.png";
                case "knight" -> color + "n.png";
                case "pawn" -> color + "p.png";
                default -> "wp.png";
            };
        }

        Image image = pieceImageCache.get(imageName);
        if (image == null) {
            System.out.println("Image not found in cache: " + imageName + ", using fallback");
            image = pieceImageCache.getOrDefault("wp.png", new Image(getClass().getResource("/pieces/wp.png").toExternalForm()));
        }
        return image;
    }

    private void updateTurnLabel() {
        String currentPlayer = game.isWhiteTurn() ? "Белые" : "Чёрные";
        if (game.isWaitingForPromotion()) {
            turnLabel.setText("🔄 Выберите фигуру для превращения пешки!");
            turnLabel.setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;");
        } else {
            turnLabel.setText("Ход: " + currentPlayer + " " + (game.isWhiteTurn() ? "⚪" : "⚫"));
            turnLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        }
    }

    public void setStatusMessage(String message) {
        statusLabel.setText(message);
        if (message.contains("мат") || message.contains("победили")) {
            statusLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
        } else if (message.contains("шах")) {
            statusLabel.setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;");
        } else if (message.contains("превращена")) {
            statusLabel.setStyle("-fx-text-fill: #27AE60; -fx-font-weight: bold;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
        }
    }

    public void setGameOver(boolean isCheckmate) {
        this.gameOver = true;
        for (int i = 0; i < 64; i++) {
            if (boardPane.getChildren().get(i) instanceof Button) {
                ((Button) boardPane.getChildren().get(i)).setDisable(true);
            }
        }
    }

    public void showPromotionDialog(String color) {
        Stage promotionStage = new Stage();
        promotionStage.initModality(Modality.APPLICATION_MODAL);
        promotionStage.initOwner(primaryStage);
        promotionStage.setTitle("Превращение пешки");
        promotionStage.setResizable(false);

        promotionStage.setWidth(primaryStage.getWidth() * 0.75);
        promotionStage.setHeight(primaryStage.getHeight() * 0.4);

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) ->
                promotionStage.setWidth(newVal.doubleValue() * 0.75));
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) ->
                promotionStage.setHeight(newVal.doubleValue() * 0.4));

        VBox layout = new VBox();
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #2C3E50;");

        DoubleBinding dialogPadding = Bindings.createDoubleBinding(
                () -> {
                    double width = primaryStage.getWidth();
                    return Double.isNaN(width) || width <= 0 ? 10 : width / 40;
                },
                primaryStage.widthProperty()
        );
        layout.paddingProperty().bind(Bindings.createObjectBinding(
                () -> new Insets(dialogPadding.get() * 3),
                dialogPadding
        ));
        layout.spacingProperty().bind(dialogPadding.multiply(2));

        Label title = new Label("Во что превратить пешку?");
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        title.fontProperty().bind(Bindings.createObjectBinding(
                () -> Font.font("Arial", FontWeight.BOLD, Math.max(12, dialogPadding.get() * 1.8)),
                dialogPadding
        ));

        HBox buttonsBox = new HBox();
        buttonsBox.setAlignment(Pos.CENTER);
        buttonsBox.spacingProperty().bind(dialogPadding.multiply(1.5));

        String[] pieces = {"Queen", "Rook", "Bishop", "Knight"};
        String[] names = {"Ферзь", "Ладья", "Слон", "Конь"};
        String colorPrefix = color.toLowerCase().charAt(0) + "";
        String[] imageNames = {colorPrefix + "q.png", colorPrefix + "r.png", colorPrefix + "b.png", colorPrefix + "n.png"};
        DoubleBinding buttonSize = Bindings.createDoubleBinding(
                () -> {
                    double width = primaryStage.getWidth();
                    return Double.isNaN(width) || width <= 0 ? 50 : width / 8;
                },
                primaryStage.widthProperty()
        );

        for (int i = 0; i < pieces.length; i++) {
            final String pieceType = pieces[i];
            String imageName = imageNames[i];

            Button pieceButton = new Button();
            Image pieceImage = pieceImageCache.get(imageName);
            if (pieceImage != null) {
                ImageView imageView = new ImageView(pieceImage);
                imageView.fitWidthProperty().bind(buttonSize.multiply(0.75));
                imageView.fitHeightProperty().bind(buttonSize.multiply(0.75));
                pieceButton.setGraphic(imageView);
            } else {
                System.out.println("Failed to load promotion image: " + imageName);
                pieceButton.setText(names[i]);
            }

            pieceButton.setMinWidth(buttonSize.get());
            pieceButton.setMinHeight(buttonSize.get());
            String buttonStyle = "-fx-background-color: " + LIGHT_SQUARE + "; -fx-border-color: #8B4513; -fx-border-width: 2px; " +
                    "-fx-background-radius: 10px; -fx-border-radius: 10px;";
            pieceButton.setStyle(buttonStyle);

            pieceButton.setOnMouseEntered(e -> {
                String hoverStyle = "-fx-background-color: " + SELECTED_SQUARE + "; -fx-border-color: #8B4513; -fx-border-width: 2px; " +
                        "-fx-background-radius: 10px; -fx-border-radius: 10px;";
                pieceButton.setStyle(hoverStyle);
            });

            pieceButton.setOnMouseExited(e -> pieceButton.setStyle(buttonStyle));

            pieceButton.setOnAction(e -> {
                game.completePawnPromotion(pieceType);
                try {
                    java.lang.reflect.Method switchTurn = game.getClass().getMethod("switchTurn");
                    switchTurn.invoke(game);
                    updateTurnLabel();
                    updateDisplay();
                    setStatusMessage("Пешка превращена в " + getPieceNameInRussian(pieceType));
                } catch (NoSuchMethodException | IllegalAccessException | java.lang.reflect.InvocationTargetException ex) {
                    System.out.println("Warning: switchTurn method not found in Game class, turn not switched");
                }
                promotionStage.close();
            });

            Label nameLabel = new Label(names[i]);
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            nameLabel.fontProperty().bind(Bindings.createObjectBinding(
                    () -> Font.font("Arial", FontWeight.BOLD, Math.max(10, dialogPadding.get() * 1.2)),
                    dialogPadding
            ));

            VBox pieceBox = new VBox();
            pieceBox.setAlignment(Pos.CENTER);
            pieceBox.spacingProperty().bind(dialogPadding.divide(2));
            pieceBox.getChildren().addAll(pieceButton, nameLabel);

            buttonsBox.getChildren().add(pieceBox);
        }

        layout.getChildren().addAll(title, buttonsBox);

        Scene promotionScene = new Scene(layout);
        promotionStage.setScene(promotionScene);
        promotionStage.showAndWait();
    }

    private void handleClick(int x, int y) {
        if (gameOver) {
            setStatusMessage("Игра окончена!");
            return;
        }

        System.out.println("Clicked: (" + x + ", " + y + ")");
        if (selectedX == -1) {
            Piece piece = game.getBoard().getPiece(x, y);
            if (piece != null && piece.getColor().equals(game.isWhiteTurn() ? "White" : "Black")) {
                selectedX = x;
                selectedY = y;
                setStatusMessage("Выбрана фигура " + getPieceNameInRussian(piece.getType()) + " на " +
                        getSquareName(x, y));
                updateDisplay();
                System.out.println("Selected piece: " + piece.getType() + " at (" + x + ", " + y + ")");
            } else {
                setStatusMessage("Выберите свою фигуру");
                System.out.println("Invalid selection at (" + x + ", " + y + ")");
            }
        } else {
            System.out.println("Attempting move from (" + selectedX + ", " + selectedY + ") to (" + x + ", " + y + ")");
            if (game.makeMove(selectedX, selectedY, x, y)) {
                System.out.println("Move successful");
            } else {
                setStatusMessage("Недопустимый ход");
                System.out.println("Move invalid");
            }
            selectedX = -1;
            selectedY = -1;
            updateDisplay();
        }
    }

    private String getPieceNameInRussian(String pieceType) {
        return switch (pieceType) {
            case "King" -> "король";
            case "Queen" -> "ферзь";
            case "Rook" -> "ладья";
            case "Bishop" -> "слон";
            case "Knight" -> "конь";
            case "Pawn" -> "пешка";
            case "CheckersMan" -> "шашка";
            default -> "фигура";
        };
    }

    private String getSquareName(int x, int y) {
        char file = (char)('a' + y);
        int rank = 8 - x;
        return "" + file + rank;
    }

    public Scene getScene() {
        return scene;
    }

    public void updateMovesDisplay() {
        StringBuilder whiteMoves = new StringBuilder();
        StringBuilder blackMoves = new StringBuilder();
        int moveNumber = 1;

        for (int i = 0; i < game.getMoveHistory().size(); i += 2) {
            String whiteMove = game.getMoveHistory().get(i);
            whiteMoves.append(moveNumber).append(". ").append(whiteMove).append("\n");
            if (i + 1 < game.getMoveHistory().size()) {
                String blackMove = game.getMoveHistory().get(i + 1);
                blackMoves.append(moveNumber).append(". ").append(blackMove).append("\n");
            }
            moveNumber++;
        }

        whiteMovesArea.setText(whiteMoves.toString());
        blackMovesArea.setText(blackMoves.toString());
    }
}