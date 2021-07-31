/*
* King spawn fix
* Need a lobby/force start thing
* need to fix obstacles
 */

package kingbattle.server;

import kingbattle.util.Constants;
import kingbattle.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static kingbattle.util.Constants.*;

public class GameEngine {
    
    boolean isGameOver = false;
    final Color openedBackgroundColor = new Color(255,229,204);
    final Color unopenedBackgroundColor = new Color(229,255,204);
    final Color headerBackgroundColor = new Color(51,102,0);
    final String assetPath = "C:\\players\\Willi\\IdeaProjects\\Summer\\assets\\";
    public static boolean isGameStarted = false;
    public static Cell[][] map;
    HashMap<Integer,PlayerInfo> players;
    public static int tickCount = 0;
    boolean firstGameStart = true;
    boolean firstPlacementOfKings = true;
    final static int WON = 1;
    final static int LOST = -1;
    final static int CONTINUE = 0;
    static int xCount;
    static int yCount;

    public GameEngine() {
        map = GenerateMap.createMap();
        xCount = map.length;
        yCount = map[0].length;
        players = new HashMap<>();
        //Utils.printBoard(map);
        TimerThread timerThread = new TimerThread(this);
        timerThread.start();
    }

    protected static class PlayerInfo {
        int playerID;
        String username = "username line 30 gameengine";
        boolean[][] mask = new boolean[xCount][yCount];
        LinkedList<PendingMove> movesQueue = new LinkedList<>();
        PlayerThread playerThread;
        boolean isReady = false;
        Scores score = new Scores();
        int winOrLose = CONTINUE;
    }
    
    public static class PendingMove implements Cloneable{
        public int x = -1; // this is the x coordinate of the spot from which they are trying to move
        public int y = -1; // this is the y coordinate of the spot from which they are trying to move.
        public int dir; // this is the direction that they want to move in
        public int id = 1;
        public boolean isExecuted = false;
        public int movePercentage = 100;

        public String toString() {
            return "X=" + x + ", Y=" + y + ", dir=" + dir + ", id=" + id + ", executed=" + isExecuted;
        }

        public PendingMove clone() {
            PendingMove result = new PendingMove();
            result.x = x;
            result.y = y;
            result.dir = dir;
            result.id = id;
            result.isExecuted = isExecuted;
            result.movePercentage = movePercentage;
            return result;
        }
    }

    public static class Cell {
        public char cellType = EMPTY_CELL;
        public int armyCount = 0;
        public int player = PLAYER_ZERO; //PLAYER_ONE, PLAYER_TWO for players, PLAYER_ZERO for empty/unowned

    }

    public int addPlayer(String username, PlayerThread playerThread) {
        PlayerInfo newPlayer = new PlayerInfo();
        newPlayer.playerID = players.size() + 1;
        newPlayer.username = username;
        newPlayer.playerThread = playerThread;
        players.put(newPlayer.playerID,newPlayer);
        return newPlayer.playerID;
    }

    public void removePlayer(int playerId) {
        players.remove(playerId);
        System.out.println("Removed player " + playerId);
    }

    public void startGame(){
        Iterator it = players.entrySet().iterator();
        if(players.size() > 1){
            isGameStarted = true;
        }
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            PlayerInfo p = (PlayerInfo) pair.getValue();
            if(!p.isReady){
                isGameStarted = false;
            }
        }

        if(isGameStarted && firstPlacementOfKings){
            GenerateMap.placeKing(players.size());
            firstPlacementOfKings = false;
        }
        //System.out.println("Startgame =" + isGameStarted);

    }

    private void calculateScoreBoard() {
        //iterates through once to calculate army and land counts
        Iterator it = players.entrySet().iterator();
        List<Scores> scoresList = new ArrayList<>();
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            PlayerInfo p = (PlayerInfo) pair.getValue();
            p.score = new Scores();
            p.score.playerId = p.playerID;
            for (int x = 0; x < xCount; x++) {
                for (int y = 0; y < yCount; y++) {
                    Cell cell = map[x][y];
                    if(p.playerID == cell.player){
                        //System.out.println("Player " + p.playerID + cell.player + " has " + scoresList);
                        p.score.armyCount += cell.armyCount;
                        p.score.landCount++;
                    }
                }
            }
            scoresList.add(0,p.score);
        }

        int[] scores = sortScoreboard(scoresList);
        //iterates through to send army/land counts
        Iterator it2 = players.entrySet().iterator();
        while(it2.hasNext()) {
            Map.Entry pair = (Map.Entry)it2.next();
            PlayerInfo p = (PlayerInfo) pair.getValue();
            p.playerThread.sendScoreBoard(scores);
        }
    }

    public static class Scores {
        public int armyCount;
        public int landCount;
        public int playerId;
        public String toString() {
            return "Scores: armycount=" + armyCount + ", landcount=" + landCount + ", playerId=" + playerId;
        }
    }

    private int[] sortScoreboard(List<Scores> scoresList) {

        Collections.sort(scoresList, new Comparator<Scores>() {
            @Override
            public int compare(Scores o1, Scores o2) {
                return (o1.armyCount != o2.armyCount) ? (o2.armyCount - o1.armyCount) : (o2.landCount-o1.landCount);
            }
        });

        //turns it into an integer array of first playerId then army then land
        int[] res = new int[scoresList.size()*3];
        int j = 0;
        for (int i = 0; i < scoresList.size(); i++){
            res[j] = scoresList.get(i).playerId;
            res[j+1] = scoresList.get(i).armyCount;
            res[j+2] = scoresList.get(i).landCount;
            j += 3;
        }
        return res;

    }

    private void sendStart() {
        Iterator it = players.entrySet().iterator();
        if(players.size() > 0){
            isGameStarted = true;
        }
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            PlayerInfo p = (PlayerInfo) pair.getValue();
            p.playerThread.sendStart();
        }
    }

    public void setReady(Boolean isReady, int playerId) {
        PlayerInfo p = players.get(playerId);
        p.isReady = isReady;
    }

    public boolean tick() {
        if(isGameStarted && firstGameStart){
            sendStart();
            firstGameStart = false;
        }
        if(isGameOver){
            return true;
        }
        else if(isGameStarted) {
            //System.out.println("Game started");
            growArmy();

            Iterator it = players.entrySet().iterator();
            boolean hasAnyMoved = false;
            while(it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
                PlayerInfo p = (PlayerInfo) pair.getValue();
                if(move(p)){
                    hasAnyMoved = true;
                }
                p.playerThread.sendBoard();
                p.playerThread.sendPlayerInfo(p);
            }
            if(hasAnyMoved){
                //Utils.printBoard(map);
            }
            calculateScoreBoard();
            tickCount++;
        }
        else{
            startGame();
        }
        //System.out.println("Tick = " + tickCount);
        return false;
    }



    private void growArmy(){
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                Cell cell = map[x][y];
                if(cell.player > 0 && cell.cellType != Constants.KING && tickCount % 25 == 0){
                    cell.armyCount++;
                }
                else if(cell.cellType == Constants.KING || cell.cellType == CITY && cell.player > 0){
                    cell.armyCount++;
                }

            }
        }
    }

    public Cell[][] getBoardForPlayer(int playerId) {
        return exportMap(players.get(playerId));
    }

    public Cell[][] exportMap(PlayerInfo p) {
        playerExploredMap(p);
        Cell[][] res = new Cell[xCount][yCount];
        boolean[][] mask = p.mask;
        for(int x = 0; x < xCount; x++){
            for(int y = 0; y < yCount; y++){
                if(mask[x][y] || p.winOrLose == LOST || p.winOrLose == WON){
                    res[x][y] = map[x][y];
                }
                else{
                    res[x][y] = new Cell();
                    if(map[x][y].cellType == MOUNTAIN || map[x][y].cellType == CITY){
                        res[x][y].cellType = UNEXPLORED_OBSTACLE;
                    }
                    else{
                        res[x][y].cellType = UNEXPLORED_LAND;
                    }
                }
            }
        }
        return res;
    }

    private void playerExploredMap(PlayerInfo p){
        p.mask = new boolean[xCount][yCount];
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                Cell cell = map[x][y];
                if(cell.player == p.playerID){
                    //left column
                    exploreCell(x-1,y-1, p);
                    exploreCell(x-1,y, p);
                    exploreCell(x-1,y+1, p);
                    //middle column
                    exploreCell(x,y-1, p);
                    exploreCell(x,y+1, p);
                    exploreCell(x,y,p);
                    //right column
                    exploreCell(x+1,y-1, p);
                    exploreCell(x+1,y, p);
                    exploreCell(x+1,y+1, p);
                }
            }
        }
    }

    private void exploreCell(int x, int y, PlayerInfo p) {
        if(x >= 0  && y >= 0 && x < xCount && y < yCount){
            p.mask[x][y] = true;
        }
    }

    public void addMove(int playerId, PendingMove pm){
        PlayerInfo p = players.get(playerId);
        synchronized(p.movesQueue){
            p.movesQueue.add(pm);
        }
        System.out.println("Added move:" + pm);
    }

    public boolean move(PlayerInfo p) {
        PendingMove q;
        synchronized(p.movesQueue) {
            if (p.movesQueue.size() == 0) {
                return false;
            }
            q = p.movesQueue.remove(0);
        }
        Cell cell = map[q.x][q.y];
        if(cell.player != p.playerID){
            move(p);
            return false;
        }
        if(cell.armyCount <= 1){
            move(p);
            return false;
        }

        if(q.dir == UP){
            return moveTo(cell, p, q.x,q.y - 1,q);
        }
        if(q.dir == RIGHT){
            return moveTo(cell, p,q.x + 1,q.y,q);
        }
        if(q.dir == DOWN){
            return moveTo(cell, p,q.x,q.y + 1,q);
        }
        if(q.dir == LEFT){
            return moveTo(cell, p,q.x - 1,q.y,q);
        }
        return false;
    }

    public boolean moveTo(Cell cellFrom, PlayerInfo p, int x, int y, PendingMove q){
        if(x < 0 || y < 0 || x >= xCount || y >= yCount){
            move(p);
            return false;
        }
        Cell cellTo = map[x][y]; // there is a bug here where you can move through mountains
        boolean hasMoved = false;

        int halfArmy = -1;
        if(q.movePercentage == 50) {
            if (cellFrom.armyCount % 2 == 0) {
                halfArmy = cellFrom.armyCount / 2;
                cellFrom.armyCount = halfArmy;
                System.out.println("EVEN GE 368 Half=" + halfArmy + ", ArmyC=" + cellFrom.armyCount);
            } else {
                halfArmy = cellFrom.armyCount / 2 + 1;
                cellFrom.armyCount = halfArmy - 1; // might be wrong since leave 1 army back?
                System.out.println("ODD GE 368 Half=" + halfArmy + ", ArmyC=" + cellFrom.armyCount);
            }
        }
        if(cellTo.cellType == EMPTY_CELL){
            if(cellFrom.armyCount >= 1){
                cellTo.armyCount = (halfArmy == -1 ? (cellFrom.armyCount - 1) : cellFrom.armyCount);
                cellFrom.armyCount = (halfArmy == -1 ? 1 : halfArmy); //then after that we need to do some other stuff
                cellTo.cellType = OWNED_LAND;
                cellTo.player = p.playerID;
                hasMoved = true;
            }
        }
        //moving into mountain, need to clear queue
        else if(cellTo.cellType == MOUNTAIN){
            if(q.movePercentage == 50) {
                if (cellFrom.armyCount % 2 == 0) {
                    cellFrom.armyCount = halfArmy*2;
                } else {
                    cellFrom.armyCount = halfArmy*2 -1;
                }

            }
            move(p);
            return false;
        }
        //own land
        else if(cellTo.player == cellFrom.player){
            cellTo.armyCount += (halfArmy == -1 ? (cellFrom.armyCount - 1) : cellFrom.armyCount);
            cellFrom.armyCount = (halfArmy == -1 ? 1 : halfArmy);
            hasMoved = true;
        }
        else if(cellTo.cellType == KING){
            if(cellFrom.armyCount >= cellTo.armyCount + 2 ){ // 7 taking a  2 king bcomes 5 city, so just need +1
                cellTo.cellType = CITY;
                final int loserId = cellTo.player;
                cellTo.player = cellFrom.player;
                cellTo.armyCount = (halfArmy == -1 ? (cellFrom.armyCount - cellTo.armyCount - 1) :
                        cellFrom.armyCount - cellTo.armyCount);
                cellFrom.armyCount = (halfArmy == -1 ? 1 : halfArmy);
                takeKing(cellFrom.player,loserId);
            }
            else {
                cellTo.armyCount -= (halfArmy == -1 ? (cellFrom.armyCount - 1) : cellFrom.armyCount);
                cellFrom.armyCount = (halfArmy == -1 ? 1 : halfArmy);
            }
        }
        //moving to city or moving into enemy land
        else if(cellTo.cellType == CITY || cellTo.player != cellFrom.player){ // might need a city class
            if(cellFrom.armyCount >= cellTo.armyCount + 2){ // 52 army to 47 city = 4 city, so 48 army to 47 city is 0 army, need 49
                cellTo.armyCount = (halfArmy == -1 ? (cellFrom.armyCount - cellTo.armyCount - 1) :
                        cellFrom.armyCount - cellTo.armyCount);
                cellFrom.armyCount = (halfArmy == -1 ? 1 : halfArmy);
                cellTo.player = cellFrom.player;
            }
            else{
                cellTo.armyCount -= (halfArmy == -1 ? (cellFrom.armyCount - 1) : cellFrom.armyCount);
                cellFrom.armyCount = (halfArmy == -1 ? 1 : halfArmy);

            }
            hasMoved = true;
        }

        if(hasMoved) {
            //System.out.println("Player " + p.playerID + " moved from: " + q + " to: X=" + x + ", Y=" + y);
        }
        return hasMoved;
    }

    private void takeKing(int winnerId, int loserId) {
        PlayerInfo p = players.get(loserId);
        p.winOrLose = LOST;
        p.score.landCount = 0;
        p.score.armyCount = 0;
        for(int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                if(map[x][y].player == loserId){
                    map[x][y].player = winnerId;
                    map[x][y].armyCount = (map[x][y].armyCount+1)/2;
                }
                //System.out.println("Taken king, but mask = true");
            }
        }

        if(checkWin(winnerId)){
            isGameOver = true;
            sendWin(winnerId);
        }
        //maybe need to call a send lose thing? win/lose is a new message needed
    }

    private boolean checkWin(int playerId) {
        Iterator it = players.entrySet().iterator();
        PlayerInfo pWinner = players.get(playerId);
        if(players.size() < 1){
            return false;
        }
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            PlayerInfo p = (PlayerInfo) pair.getValue();
            if(pWinner == p){
            }
            else if(p.score.armyCount != 0 || p.score.landCount != 0){
                System.out.println("Checked win, player " + p.playerID + " still has land/army" );
                System.out.println("Player army=" + p.score.armyCount + ", land=" + p.score.landCount);
                return false;
            }
        }

        return true;
    }

    private void sendWin(int winnerId) {
        Iterator it = players.entrySet().iterator();
        if(players.size() > 0){
            isGameStarted = true;
        }
        while(it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            PlayerInfo p = (PlayerInfo) pair.getValue();
            if(p.playerID == winnerId){
                p.winOrLose = WON;
                System.out.println("Player " + winnerId + " is the winner in sendwin GE");
            }
            p.playerThread.sendBoard();

            p.playerThread.sendWin(winnerId);
        }
    }

    public void clearQueue(int playerId) {
        PlayerInfo p = players.get(playerId);
        synchronized(p.movesQueue) {
            p.movesQueue.clear();
        }
    }

    public void deleteLastMove(int playerId) {
        PlayerInfo p = players.get(playerId);
        synchronized(p.movesQueue) {
            if(p.movesQueue.size() > 0) {
                p.movesQueue.removeLast();
            }
        }
    }



}
