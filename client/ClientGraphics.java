package kingbattle.client;

import kingbattle.server.GameEngine;
import kingbattle.server.GameEngine.Cell;
import kingbattle.util.Constants;
import kingbattle.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import static java.awt.Color.WHITE;
import static java.awt.Color.red;


public class ClientGraphics extends JPanel implements MouseListener, KeyListener {
    GameClient gameClient;
    static int xCount = 10;
    static int yCount = 10;
    static int cellLength = Constants.CELL_LENGTH;
    static int headerHeight = Constants.HEADER_HEIGHT;
    static int frameWidth = xCount*cellLength;
    static int frameHeight = yCount*cellLength+headerHeight;
    final static Color exploredColor = new Color(200,200,200);
    final static Color unexploredColor = new Color(60,60,60);
    final static Color headerBackgroundColor = new Color(25,25,25);
    final static String assetPath = "assets\\kingbattle\\newimages\\";
    static Image obstacleImage;
    static int turnCount = 0;
    Sender sender;
    static int tickCount = 0;
    Checkbox checkbox = new Checkbox();
    Button button = new Button();
    JFrame frame;
    boolean isReady = false;
    public boolean isGameStarted = false;
    ArrayList<GameEngine.Scores> scores = new ArrayList<>();
    final static int LOST = -1;
    final static int WON = 1;
    final static int CONTINUE = 0;
    int gameResult = CONTINUE;
    LinkedList<GameEngine.PendingMove> movesQueue = new LinkedList<>();
    int moveCounter = 1;


    Color emptyCellColor = Constants.EMPTY_CELL_COLOR;
    static Color textColor = Constants.TEXT_COLOR;
    ArrayList<PlayerGraphics> playerGraphics = new ArrayList<>();
    Cell[][] map;
    final static Image mountainImage = Toolkit.getDefaultToolkit().getImage(assetPath + "blankmountain.png");
    final static Color[] playerColors = {
            Constants.PLAYER_ZERO_COLOR, Constants.PLAYER_ONE_COLOR, Constants.PLAYER_TWO_COLOR,
            Constants.PLAYER_THREE_COLOR, Constants.PLAYER_FOUR_COLOR, Constants.PLAYER_FIVE_COLOR,
            Constants.PLAYER_SIX_COLOR, Constants.PLAYER_SEVEN_COLOR, Constants.PLAYER_EIGHT_COLOR};
    final static String[] playerImageFileNamePrefix = {"blank","blue","green","maroon","orange","pink","purple","red","teal"};
    GameEngine.PendingMove currentMove = new GameEngine.PendingMove();

    public ClientGraphics (GameClient client) {
        gameClient = client;
        obstacleImage = Toolkit.getDefaultToolkit().getImage(assetPath + "blankobstacle.png");
        for (int i = 0; i <= Constants.MAX_PLAYER_COUNT; i++) {
            PlayerGraphics pg = new PlayerGraphics();
            pg.city = Toolkit.getDefaultToolkit().getImage(assetPath + playerImageFileNamePrefix[i] + "city.png");
            pg.king = Toolkit.getDefaultToolkit().getImage(assetPath + playerImageFileNamePrefix[i] + "king.png");
            pg.color = playerColors[i];
            playerGraphics.add(pg);
        }

        frame = new JFrame();
        frame.add(this);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setTitle("King Battle");
        frame.setResizable(false);
        frame.setBackground(unexploredColor);
        frame.setSize(new Dimension(frameWidth + cellLength/2, frameHeight + 6*cellLength/5));
        this.addMouseListener(this);
        this.addKeyListener(this);
        this.setFocusable(true);


        button.setLabel("Not Ready");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isReady = !isReady;
                try {
                    sender.sendReadyState(isReady);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                button.setLabel(isReady ? "Ready" : "Not Ready");
            }
        });
        this.add(button);

        frame.setVisible(true);
    }


    public void setSender(Sender sender) {
        this.sender = sender;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        int x = mouseXToColumn(e.getX());
        int y = mouseYToRow(e.getY());
        if(y >= 0 && x >=0) {
            //System.out.println("Curr move % before is " + currentMove.movePercentage);
            if(currentMove.x == x && currentMove.y == y) {
                currentMove.movePercentage = (currentMove.movePercentage == 100 ? 50 : 100);
                //System.out.println("Curr move % changed to " + currentMove.movePercentage);
            }
            else {
                currentMove.movePercentage = 100;
            }
            currentMove.x = x;
            currentMove.y = y;;
        }
        repaint();

    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private int mouseXToColumn(int x){
        return x/cellLength;
    }

    private int mouseYToRow(int y){
        int res = y <= headerHeight ? -1 : (y-headerHeight)/cellLength;
        //System.out.println("Mouse y =" + y + ", Row = " + res);
        return res;

    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        try {
            //Remember to change it so that after you click the key it moves the selected cell
            //System.out.println("Key released");
            if (currentMove.x == -1 || currentMove.y == -1) {
                return;
            }
            if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
                currentMove.dir = Constants.LEFT;
                moveCurrentCell(-1, 0);
            } else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W) {
                currentMove.dir = Constants.UP;
                moveCurrentCell(0, -1);
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
                currentMove.dir = Constants.RIGHT;
                moveCurrentCell(1, 0);
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S) {
                currentMove.dir = Constants.DOWN;
                moveCurrentCell(0, 1);
            } else if (e.getKeyCode() == KeyEvent.VK_E) {
                sender.sendDeleteMove();
                if (movesQueue.size() > 0) {
                    //System.out.println("Current move before remove:" + currentMove);
                    if (!movesQueue.getLast().isExecuted) {
                        currentMove = movesQueue.removeLast();
                        moveCounter++;
                        currentMove.id = moveCounter;
                        currentMove.movePercentage = 100;
                    }
                }
                repaint();
            } else if (e.getKeyCode() == KeyEvent.VK_Q) {
                //System.out.println("Current move before Q is " + currentMove);
                //setPlayerInfo(movesQueue.getFirst().id);
                sender.sendClearQueue();
            /*synchronized (movesQueue) {
                while(!movesQueue.isEmpty()) {
                    if(!movesQueue.getLast().isExecuted) {
                        currentMove = movesQueue.removeLast();
                        moveCounter++;
                        //currentMove.id = moveCounter;
                    }
                    else {
                        break;
                    }
                }
            }*/
                //System.out.println("Current move after Q is " + currentMove);
                repaint();
            }
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void moveCurrentCell(int deltaX, int deltaY) throws IOException {
        movesQueue.add(currentMove.clone());
        sender.sendMove(currentMove);

        currentMove = currentMove.clone();
        moveCounter++;
        currentMove.id = moveCounter;
        currentMove.isExecuted = false;

        if(currentMove.x + deltaX >=0 && currentMove.y + deltaY >=0 &&
                currentMove.x + deltaX < xCount && currentMove.y + deltaY <yCount){
            currentMove.x += deltaX;
            currentMove.y += deltaY;
            currentMove.movePercentage = 100;
        }
        repaint();
        revalidate();
    }


    private class PlayerGraphics {
        Image city;
        Image king;
        Color color;
    }

    @Override
    public void paint(Graphics g){
        if(!isGameStarted){
            //System.out.println("Drawing lobby");
            drawLobby(g);
        }
        else {
            if(gameResult == CONTINUE){
                drawGrid(g);
                drawHeader(g);
                drawMap(g);
                highlightCurrentCell(g);
                drawScoreBoard(g);

            }
            else {
                //System.out.println("Drawing grid now");
                drawGrid(g);
                drawHeader(g);
                drawMap(g);
                drawScoreBoard(g);
                drawEnd(g);
            }
        }
    }

    private void drawEnd(Graphics g) {
        if(gameResult == WON) {
            g.setColor(exploredColor);
            g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
            g.drawString("You win!", 150, 52);
        }
        if(gameResult == LOST){
            g.setColor(exploredColor);
            g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
            g.drawString("You lost.", 150, 52);
        }
    }



    private void drawLobby(Graphics g){
        g.setColor(unexploredColor);
        g.fillRect(0,0,frameWidth,frameHeight);


    }

    public void highlightCurrentCell(Graphics g) {
        if(currentMove.x == -1 || currentMove.y == -1){
            return;
        }
        g.setColor(Color.RED);
        g.drawRect(xToCoordinate(currentMove.x) + 1,yToCoordinate(currentMove.y) + 1,cellLength-1,cellLength-1);
    }

    public static void drawGrid(Graphics g) {
        g.setColor(exploredColor);
        g.fillRect(0,0,frameWidth,frameHeight);
        for(int i = 0; i <= xCount; i++){
            g.drawLine(i*cellLength,headerHeight,i*cellLength,frameHeight);
        }
        for(int i = 0; i <= yCount; i++){
            g.drawLine(0,i*cellLength+headerHeight,frameWidth,i*cellLength+headerHeight);
        }

    }

    public static void drawHeader(Graphics g) {
        g.setColor(headerBackgroundColor);
        g.fillRect(0, 0, frameWidth, headerHeight);
        g.setColor(WHITE);
        g.setFont(new Font("TimesRoman", Font.PLAIN, 20));
        g.drawString("Turn " + tickCount, 20, 35);
        //System.out.println(tickCount);

    }


    private void drawMap(Graphics g) {
        if(map == null){
            return;
        }
        //System.out.println("Drawing map...");
        for(int x = 0; x < xCount; x++){
            for(int y = 0; y < yCount; y++){
                Cell cell = map[x][y];
                boolean isHalved = false;
                if (currentMove.x == x && currentMove.y == y && currentMove.movePercentage == 50) {
                    System.out.println("isHalved true for x=" + x + ",y=" + y);
                    isHalved = true;
                }
                int x1 = xToCoordinate(x);
                int y1 = yToCoordinate(y);
                if(cell.cellType == Constants.EMPTY_CELL){
                    g.setColor(emptyCellColor);
                    g.fillRect(x1 + 1,y1 + 1,cellLength - 1,cellLength - 1);
                    //System.out.println("Drew empty cell");
                }
                else if(cell.cellType == Constants.CITY){
                    g.drawImage(playerGraphics.get(cell.player).city,x1+1,y1+1,cellLength-1,cellLength-1,this);
                    g.setColor(textColor);
                    int shift = getDigitsShift(cell, isHalved);
                    g.setFont(new Font("SANS_SERIF", Font.PLAIN, 12));
                    g.drawString(drawArmyCount(cell.armyCount, isHalved), x1 + cellLength/2 + shift, y1 + cellLength/2 + 5);
                    //System.out.println("Drew city");
                }
                else if(cell.cellType == Constants.MOUNTAIN){
                    g.drawImage(mountainImage,x1 + 1,y1 + 1,cellLength - 1,cellLength - 1,this);
                    //System.out.println("Drew mountain");
                }
                else if(cell.cellType == Constants.UNEXPLORED_OBSTACLE){
                    g.drawImage(obstacleImage,x1 + 1,y1 + 1,cellLength - 1,cellLength - 1,this);
                    //System.out.println("Drew Obstacle");
                }
                else if(cell.cellType == Constants.KING){
                    g.drawImage(playerGraphics.get(cell.player).king,x1 +1,y1 + 1,cellLength -1 ,cellLength -1,this);
                    g.setColor(exploredColor);
                    int shift = getDigitsShift(cell, isHalved);
                    g.setFont(new Font("SANS_SERIF", Font.PLAIN, 12));
                    g.drawString(drawArmyCount(cell.armyCount, isHalved), x1 + cellLength/2 + shift, y1 + cellLength/2 + 5);
                    //System.out.println("Drew king");
                }
                else if(cell.cellType == Constants.OWNED_LAND){
                    //System.out.println("Drew land of" + cell.player + " containing " + cell.armyCount + " army.");
                    g.setColor(playerGraphics.get(cell.player).color);
                    g.fillRect(x1 + 1,y1 + 1,cellLength - 1,cellLength - 1);
                    g.setColor(exploredColor);
                    int shift = getDigitsShift(cell, isHalved);
                    g.setFont(new Font("SANS_SERIF", Font.PLAIN, 12));
                    g.drawString(drawArmyCount(cell.armyCount, isHalved), x1 + cellLength/2 + shift, y1 + cellLength/2 + 5);
                }
                else if(cell.cellType == Constants.UNEXPLORED_LAND){
                    g.setColor(unexploredColor);
                    g.fillRect(x1+1,y1+1,cellLength-1,cellLength-1);
                }

            }
        }
    }

    private String drawArmyCount(int armyCount, boolean isHalved) {
        return (isHalved ? "50%" : ("" + armyCount));
    }

    private int getDigitsShift(Cell cell, boolean isHalved){
        int army = cell.armyCount;
        int countDigits = 0;
        if(army == 0){
            countDigits++;
        }
        while(army > 0){
            army /= 10;
            countDigits++;
        }
        int res = 0;
        if(isHalved) {
            countDigits = 3;
        }
        for(int i = countDigits; i > 0; i--){
            res -= 3;
        }
        return res;
    }

    private int xToCoordinate(int x){
        return x*cellLength;
    }

    private int yToCoordinate(int y){
        return y*cellLength + headerHeight;
    }

    public void updateMap(Cell[][] map) {
        xCount = map.length;
        yCount = map[0].length;
        frameWidth = xCount*cellLength;
        frameHeight = yCount*cellLength+headerHeight;
        frame.setSize(new Dimension(frameWidth + cellLength/2, frameHeight + 6*cellLength/5));
        if(gameResult == CONTINUE) {
            //Utils.printBoard(map);
            tickCount++;
            this.map = map;
            revalidate();
        }
        repaint();
    }

    public void startGame() {
        isGameStarted = true;
        //checkbox.setFocusable(false);
        //this.remove(checkbox);
        this.remove(button);

        this.setFocusable(true);
        frame.setVisible(true);
        this.setVisible(true);
        //System.out.println("Added key listener");
    }

    public void updateScore(ArrayList<GameEngine.Scores> scores) {
        this.scores = scores;
        //System.out.println("Score updated in update score, length of "  + scores.size());
        revalidate();
        repaint();
    }

    public void drawScoreBoard(Graphics g) {
        g.setColor(exploredColor);
        g.drawRect(2*frameWidth/3 - 85,5,frameWidth/3 + 80,headerHeight-10);
        int x = 2*frameWidth/3 - 70;
        int y = 16;
        int xShift = 145;
        int yShift = 20;
        int count = 1;
        for(int i = 0; i < scores.size(); i++, count++){
            GameEngine.Scores score = scores.get(i);
            //System.out.println("Drawing " + score +  ", Color " + playerColors[score.playerId]);
            g.setColor(playerColors[score.playerId]);
            g.fillRect(x,y,8,8);
            g.setColor(exploredColor);
            g.setFont(new Font("SANS_SERIF", Font.PLAIN, 12));
            g.drawString("Army: " + score.armyCount + "  Land: " + score.landCount, x + 13, y + 9);
            y += yShift;
            if(count % 3 == 0){
                x += xShift;
                y -= (yShift*3 + 1);
            }
        }
    }

    public void endGame(int[] winOrLoseAndId) {
        if(winOrLoseAndId[1] == 1){
            System.out.println("Won in client graphics endgame");
            gameResult = WON;
        }
        else{
            System.out.println("Lost in client graphics endgame");
            gameResult = LOST;
        }
        revalidate();
        repaint();
    }

    public void setPlayerInfo(Integer oldestMoveId) {
        //System.out.println("First CurrentMove =" + currentMove);
        synchronized (movesQueue) {
            for(GameEngine.PendingMove pm : movesQueue) {
                if(pm.id < oldestMoveId) {
                    pm.isExecuted = true;
                }
            }
        }
    }


}
