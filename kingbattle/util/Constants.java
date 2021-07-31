package kingbattle.util;

import java.awt.*;

public class Constants {
    public final static int PORT_NUMBER = 8357;

    public final static int MAX_PLAYER_COUNT = 8;
    public final static int MAP_ROWS = 30;
    public final static int MAP_COLUMNS = 30;
    public final static int MOUNTAIN_COUNT = MAP_ROWS*MAP_COLUMNS/7;
    public final static int CITY_COUNT = MAP_ROWS*MAP_COLUMNS/30;
    public final static char CITY = 'C';
    public final static int KING = 'K';
    public final static char EMPTY_CELL = ' ';
    public final static char UNEXPLORED_OBSTACLE = '*';
    public final static char MOUNTAIN = 'M';
    public final static char UNEXPLORED_LAND = 'U';
    public final static char OWNED_LAND = 'O';
    public final static int CELL_LENGTH = 35;
    public final static int HEADER_HEIGHT = 75;
    public final static int UP = 1;
    public final static int RIGHT = 2;
    public final static int DOWN = 3;
    public final static int LEFT = 4;
    public final static int PLAYER_ZERO = 0;
    public final static Color PLAYER_ZERO_COLOR = new Color(240,240,240);
    public final static Color PLAYER_ONE_COLOR = new Color(67,99,216,255);  //blue
    public final static Color PLAYER_TWO_COLOR = new Color(0,128,0,255); //green
    public final static Color PLAYER_THREE_COLOR = new Color(128,0,0,255); //maroon
    public final static Color PLAYER_FOUR_COLOR = new Color(245,130,49,255); //orange
    public final static Color PLAYER_FIVE_COLOR = new Color(240,50,230,255); //pink
    public final static Color PLAYER_SIX_COLOR = new Color(128,0,128,255); //purple
    public final static Color PLAYER_SEVEN_COLOR = new Color(255,0,0,255); //red
    public final static Color PLAYER_EIGHT_COLOR = new Color(0,128,128,255); //teal
    public final static Color EMPTY_CELL_COLOR = Color.WHITE;
    public final static Color TEXT_COLOR = Color.WHITE;


    public final static char MESSAGE_LOGIN = 'L';
    public final static char MESSAGE_MOVE = 'M';
    public final static char MESSAGE_BOARD = 'B';
    public final static char MESSAGE_READY = 'R';
    public final static char MESSAGE_SCOREBOARD = 'S';
    public final static char MESSAGE_WIN = 'W';
    public final static char MESSAGE_DELETE_LAST_MOVE = 'D';
    public final static char MESSAGE_CLEAR_QUEUE = 'C';
    public final static char MESSAGE_PLAYER_INFO = 'P';

    /*
    How to send messages:
    [Prefix/MessageType | Message Length(calculate before sending| Message Body (username, cells, etc)]

    each cell turn to 3 characters, 1 for each ( Celltype, armycount, player), use numerical values for char

    */
}
