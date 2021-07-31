package kingbattle.server;

import kingbattle.util.Constants;

import java.util.concurrent.ThreadLocalRandom;

import static kingbattle.util.Constants.*;
import static kingbattle.server.GameEngine.Cell;

public class GenerateMap {

    static Cell[][] map;
    
    public static Cell[][] createMap() {
        map = new Cell[MAP_ROWS][MAP_COLUMNS];
        for (int x = 0; x < MAP_ROWS; x++) {
            for (int y = 0; y < MAP_COLUMNS; y++) {
                map[x][y] = new Cell();
            }
        }

        int mtCount = MOUNTAIN_COUNT;
        while(mtCount > 0){
            int x = (int) Math.floor(Math.random()*(MAP_ROWS));
            int y = (int) Math.floor(Math.random()*(MAP_COLUMNS));
            if(map[x][y].cellType == EMPTY_CELL){
                map[x][y].cellType = MOUNTAIN;
                mtCount--;
            }
        }

        int cityCount = CITY_COUNT;
        while(cityCount > 0){
            int x = (int) Math.floor(Math.random()*(MAP_ROWS));
            int y = (int) Math.floor(Math.random()*(MAP_COLUMNS));
            if(map[x][y].cellType == EMPTY_CELL){
                map[x][y].cellType = CITY;
                int armyCount = ThreadLocalRandom.current().nextInt(40,50);
                map[x][y].armyCount = armyCount;
                cityCount--;
            }
        }

        return map;
    }

    public static void placeKing(int kingCount){
        int i = 1;
        int count = 0;
        int x1;
        int y1;
        while(i <= kingCount){
            x1 = (int) Math.floor(Math.random()*(MAP_ROWS));
            y1 = (int) Math.floor(Math.random()*(MAP_COLUMNS));
            boolean canPlace = true;
            if (map[x1][y1].cellType != MOUNTAIN && map[x1][y1].cellType != CITY ) {
                for (int x = 0; x < MAP_ROWS; x++) {
                    for (int y = 0; y < MAP_COLUMNS; y++) {
                        if(map[x][y].cellType == KING) {
                            if (!(Math.abs(x1 - x) + Math.abs(y1 - y) > (kingCount <= 2 ? 25 : (kingCount < 4 ? 15 : 10)))){
                                canPlace = false;
                            }
                        }
                    }
                }
                if(canPlace) {
                    map[x1][y1].cellType = KING;
                    map[x1][y1].armyCount = 1;
                    map[x1][y1].player = i;
                    i++;
                }
            }
            count++;
            if(count > 25){
                count = 0;
                map = regenerateMap(kingCount);
                i = 1;
            }
        }
    }

    public static Cell[][] regenerateMap(int kingCount){
        for (int x = 0; x < MAP_ROWS; x++) {
            for (int y = 0; y < MAP_COLUMNS; y++) {
                map[x][y] = new Cell();
            }
        }
        createMap();
        placeKing(kingCount);
        return map;
    }

}
