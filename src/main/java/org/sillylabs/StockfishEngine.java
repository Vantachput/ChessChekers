package org.sillylabs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class StockfishEngine {
    private Process engineProcess;
    private BufferedReader processReader;
    private OutputStreamWriter processWriter;

    // Шлях до виконуваного файлу Stockfish
    private static final String PATH = "engine/stockfish.exe";

    public boolean startEngine() {
        try {
            engineProcess = new ProcessBuilder(PATH).start();
            processReader = new BufferedReader(new InputStreamReader(engineProcess.getInputStream()));
            processWriter = new OutputStreamWriter(engineProcess.getOutputStream());

            // Ініціалізація UCI
            sendCommand("uci");

            // Чекаємо, поки рушій відповість, що він готовий (uciok)
            String line;
            while ((line = processReader.readLine()) != null) {
                if (line.equals("uciok")) {
                    break;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void sendCommand(String command) {
        try {
            processWriter.write(command + "\n");
            processWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getBestMove(String fen, int waitTime) {
        sendCommand("position fen " + fen);
        sendCommand("go movetime " + waitTime);

        String bestMove = null;
        try {
            String line;
            while ((line = processReader.readLine()) != null) {
                if (line.startsWith("bestmove")) {
                    bestMove = line.split(" ")[1];
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bestMove;
    }

    public void stopEngine() {
        try {
            sendCommand("quit");
            processReader.close();
            processWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}