package kingbattle.util;

import kingbattle.server.GameEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import static kingbattle.util.Constants.*;
import static kingbattle.server.GameEngine.Cell;

public class Utils {
    public static void printBoard(GameEngine.Cell[][] map){
        for(int y = 0; y < map[0].length; y++){
            for(int x = 0; x < map.length; x++){
                GameEngine.Cell c = map[x][y];
                System.out.print(c.cellType + "." +
                        (c.armyCount < 10 ? " " : "") + c.armyCount +
                        "." + (c.player == PLAYER_ZERO ? ' ' : (char)(c.player + 'A')) + " | ");
            }
            System.out.println();
        }
    }

    public static String boardToMessage(Cell[][] map) {
        int xCount = map.length;
        int yCount = map[0].length;
        StringBuilder builder = new StringBuilder();
        builder.append(MESSAGE_BOARD);
        builder.append((char)(xCount*yCount*3 + 2));
        builder.append((char)xCount);
        builder.append((char)(yCount));
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++){
                Cell c = map[x][y];
                builder.append((char)c.cellType);
                builder.append((char)c.armyCount);
                builder.append((char)c.player);
            }
        }
        //System.out.println("Board to message result size = " + builder.length());
        return builder.toString();
    }

    private static Cell[][] messageToBoard(String message) {
        //System.out.print("Board message size=" + message.length() + " ");
        int xLength = (int) message.charAt(0);
        int yLength = (int) message.charAt(1);
        Cell[][] map = new Cell[xLength][yLength];
        //System.out.print("Board dimensions of X:" + xLength + " Y:" + yLength + " ");
        int i = 2;
        for (int x = 0; x < xLength; x++) {
            for (int y = 0; y < yLength; y++) {
                Cell cell = new Cell();
                cell.cellType = message.charAt(i);
                i++;
                cell.armyCount = message.charAt(i);
                i++;
                cell.player = message.charAt(i);
                i++;
                map[x][y] = cell;
            }
        }
        return map;
    }

    public static class Message {
        public char type;
        public Object body;
    }

    public static String scoreBoardToMessage(int[] scores){
        StringBuilder builder = new StringBuilder();
        builder.append(Constants.MESSAGE_SCOREBOARD);
        builder.append((char) scores.length);
        for(int i = 0; i < scores.length; i++){
            builder.append((char) scores[i]);
        }
        //System.out.println("Scoreboard to message length of" + builder.length() +  ", and message" + builder.toString());
        return builder.toString();



    }

    public static Message readMessage(BufferedReader reader) throws IOException {
        StringBuilder builder = new StringBuilder();
        //System.out.println("Reading messages . . .");
        char messageType = (char) reader.read();
        //System.out.println("Read message type of " + messageType);
        char messageLength = (char) reader.read();
        //System.out.println("Length of " + (int)messageLength);
        int i = 0;
        for(; i < messageLength && i < 5000; i++){
            builder.append((char)reader.read());
        }
        if(i != messageLength) {
            throw new IOException("Expected message length was " + (int)messageLength + ", but recieved length of " + i);
        }

        //windows will do \r but not for mac or linux
        if(reader.read() == '\r') {
            reader.read();
        }

        String body = builder.toString();
        //System.out.println("String body " + body);
        Message result = new Message();
        result.type = messageType;

        if(messageType == MESSAGE_BOARD){
            result.body = messageToBoard(body);
        }
        else if(messageType == MESSAGE_LOGIN){
            result.body = body;
        }
        else if(messageType == MESSAGE_MOVE){
            GameEngine.PendingMove pm = new GameEngine.PendingMove();
            pm.id = body.charAt(0);
            pm.x = body.charAt(1);
            pm.y = body.charAt(2);
            pm.dir = body.charAt(3);
            pm.movePercentage = body.charAt(4);
            result.body = pm;
        }
        else if(messageType == MESSAGE_READY){
            result.body = (Boolean) (body.charAt(0) == 1);
        }
        else if(messageType == MESSAGE_SCOREBOARD) {
            result.body = scoreMessageToArray(body);
        }
        else if(messageType == MESSAGE_WIN) {
            int[] winnerAndWinCase = new int[2];
            winnerAndWinCase[0] = body.charAt(0); //winner playerid
            winnerAndWinCase[1] = body.charAt(1); // win or lose
            result.body = winnerAndWinCase;
        }
        else if(messageType == MESSAGE_DELETE_LAST_MOVE || messageType == MESSAGE_CLEAR_QUEUE) {
            //no-op
        }
        else if(messageType == MESSAGE_PLAYER_INFO) {
            result.body = (int) (body.charAt(0));
        }
        return result;
    }

    private static ArrayList<GameEngine.Scores> scoreMessageToArray(String body){
        ArrayList<GameEngine.Scores> scores = new ArrayList<>();
        for(int i = 0; i+3 <= body.length(); i += 3) {
            GameEngine.Scores score = new GameEngine.Scores();
            score.playerId = body.charAt(i);
            score.armyCount = body.charAt(i + 1);
            score.landCount = body.charAt(i + 2);
            scores.add(score);
            //System.out.println("ScoreToArray " + score);
        }
        return scores;
    }
}
