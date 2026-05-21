public class Board {
    // States
    public static final int EMPTY = 0;
    public static final int SHIP = 1;
    public static final int HIT = 2;
    public static final int MISS = 3;
    /* grid tells you what is visible (water, ship, hit, miss).
    shipGrid and shipCellIndex are hidden bookkeeping: when a shot hits a cell,
     you need to know which ship was hit and which part of it, so you can call ship.hit(cellIndex) correctly.
      Storing this information at placement time is the cleanest solution
     */
    private int [][] grid;
    private Ship [][] shipGrid;
    private int[][] shipCellIndex;
    private Ship [] ships;
    int shipCount;

    public int[][] getGrid() {
        return grid;
    }

    public void setGridCell(int r, int c, int val) {
        grid[r][c] = val;
    }

    public Ship[] getShips() {
        return ships;
    }

    public void reconstructShips(String[] names, int[] lengths) {
        int[][] savedGrid = new int[10][10];
        boolean[][] target = new boolean[10][10];
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                savedGrid[r][c] = grid[r][c];
                target[r][c] = (grid[r][c] == SHIP || grid[r][c] == HIT);
                grid[r][c] = EMPTY;
            }
        }
        
        shipCount = 0;
        ships = new Ship[names.length];
        shipGrid = new Ship[10][10];
        shipCellIndex = new int[10][10];
        
        boolean[][] used = new boolean[10][10];
        int[] solutionRow = new int[names.length];
        int[] solutionCol = new int[names.length];
        boolean[] solutionHoriz = new boolean[names.length];
        
        solveandPlace(lengths, 0, target, used, solutionRow, solutionCol, solutionHoriz);
        
        for (int i = 0; i < names.length; i++) {
            Ship s = new Ship(names[i], lengths[i]);
            placeShip(s, solutionRow[i], solutionCol[i], solutionHoriz[i]);
        }
        
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                grid[r][c] = savedGrid[r][c];
                if (grid[r][c] == HIT && shipGrid[r][c] != null) {
                    shipGrid[r][c].hit(shipCellIndex[r][c]);
                }
            }
        }
    }

    private boolean solveandPlace(int[] lengths, int shipIdx, boolean[][] target, boolean[][] used, int[] solR, int[] solC, boolean[] solH) {
        if (shipIdx == lengths.length) {
            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    if (target[r][c] && !used[r][c]) return false;
                }
            }
            return true;
        }
        int len = lengths[shipIdx];
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (c + len <= 10) {
                    boolean canPlace = true;
                    for (int i = 0; i < len; i++) {
                        if (!target[r][c+i] || used[r][c+i]) { canPlace = false; break; }
                    }
                    if (canPlace) {
                        for (int i = 0; i < len; i++) used[r][c+i] = true;
                        solR[shipIdx] = r; solC[shipIdx] = c; solH[shipIdx] = true;
                        if (solveandPlace(lengths, shipIdx + 1, target, used, solR, solC, solH)) return true;
                        for (int i = 0; i < len; i++) used[r][c+i] = false;
                    }
                }
                if (len > 1 && r + len <= 10) {
                    boolean canPlace = true;
                    for (int i = 0; i < len; i++) {
                        if (!target[r+i][c] || used[r+i][c]) { canPlace = false; break; }
                    }
                    if (canPlace) {
                        for (int i = 0; i < len; i++) used[r+i][c] = true;
                        solR[shipIdx] = r; solC[shipIdx] = c; solH[shipIdx] = false;
                        if (solveandPlace(lengths, shipIdx + 1, target, used, solR, solC, solH)) return true;
                        for (int i = 0; i < len; i++) used[r+i][c] = false;
                    }
                }
            }
        }
        return false;
    }

    public Board() {
        grid          = new int[10][10];    // all zeros = all EMPTY automatically
        shipGrid    = new Ship[10][10];   // all null by default
        shipCellIndex = new int[10][10];    // all zeros by default
        ships         = new Ship[5];
        shipCount     = 0;
    }
    public void printBoard(boolean hideShips) {
        System.out.println("   A B C D E F G H I J");
        for (int r = 0; r < 10; r++) {
            if (r < 9) System.out.print(" ");   // pad single-digit row numbers
            System.out.print((r + 1) + " ");
            for (int c = 0; c < 10; c++) {
                int cell = grid[r][c];
                if      (cell == EMPTY)              System.out.print(". ");
                else if (cell == SHIP && hideShips)  System.out.print(". ");
                else if (cell == SHIP)               System.out.print("S ");
                else if (cell == HIT)                System.out.print("X ");
                else if (cell == MISS)               System.out.print("O ");
            }
            System.out.println();
        }
    }
    // Is Valid Placement method to check if the ship is placed properly
public boolean isValidPlacement(int row, int col, int length, boolean horizontal){
    if (row < 0 || col < 0 || row >= 10 || col >= 10) return false;
    if (horizontal  && col + length > 10) return false;
    if (!horizontal && row + length > 10) return false;
    for (int i = 0; i < length; i++) {
        int r = horizontal ? row : row + i;
        int c = horizontal ? col + i : col;
        if (grid[r][c] == SHIP) return false;
    }
    return true;
}
// Method underneath must be called assuiming that isValidPlacement is True
public void placeShip (Ship ship, int row, int col, boolean horizontal){
    for (int i = 0; i < ship.getLength(); i++) {
        int r = horizontal ? row : row + i;
        int c = horizontal ? col + i : col;
        grid[r][c]          = SHIP;
        shipGrid[r][c]      = ship;
        shipCellIndex[r][c] = i;     // i=0 for first cell, i=1 for second, etc.
    }
    ships[shipCount] = ship;
    shipCount++;
}
    public int fireAt(int row, int col) {
        // GUI Check for color display
        if (grid[row][col] == HIT || grid[row][col] == MISS) {
            return -1;
        }
        if (grid[row][col] == EMPTY) {
            grid[row][col] = MISS;
            return 0;
        }
        // Only remaining case: grid[row][col] == SHIP
        Ship hitShip = shipGrid[row][col];
        int cellIdx  = shipCellIndex[row][col];
        hitShip.hit(cellIdx);
        grid[row][col] = HIT;
        if (hitShip.isSunk()) {
            return 2;
        }
        return 1;
    }
    // Helper Method
    public int getCell(int row, int col) {
        return grid[row][col];
    }
    public boolean alreadyShot(int row, int col) {
        return grid[row][col] == HIT || grid[row][col] == MISS;
    }
    // Called when FiredAt == 2 to retrieve sunken Ship
    public String getSunkenShipName(int row, int col) {
        return shipGrid[row][col].getName();
    }
    public boolean allShipsSunk() {
        for (int i = 0; i < shipCount; i++) {
            if (!ships[i].isSunk()) {
                return false;
            }
        }
        return true;
    }
    // ----- COMPUTER LOGIC ---------
    public static void placeShipsRandomly(Board board) {
        String[] names   = {"Carrier", "Battleship", "Cruiser", "Submarine", "Destroyer"};
        int[]    lengths = {5, 4, 3, 3, 2};

        for (int i = 0; i < 5; i++) {
            Ship s = new Ship(names[i], lengths[i]);
            boolean placed = false;
            while (!placed) {
                // Math.Random for Random cordinate Ship Placement
                int row       = (int)(Math.random() * 10);
                int col       = (int)(Math.random() * 10);
                boolean horiz = Math.random() < 0.5;
                if (board.isValidPlacement(row, col, lengths[i], horiz)) {
                    board.placeShip(s, row, col, horiz);
                    placed = true;
                }
            }
        }
    }
    public static int[] computerChooseShot(Board targetBoard, int lastHitRow, int lastHitCol) {
        if (lastHitRow != -1) {
            int[][] neighbors = {
                    {lastHitRow - 1, lastHitCol},
                    {lastHitRow + 1, lastHitCol},
                    {lastHitRow,     lastHitCol - 1},
                    {lastHitRow,     lastHitCol + 1}
            };
            for (int i = 0; i < neighbors.length; i++) {
                int r = neighbors[i][0];
                int c = neighbors[i][1];
                if (r >= 0 && r < 10 && c >= 0 && c < 10 && !targetBoard.alreadyShot(r, c)) {
                    return new int[]{r, c};
                }
            }
            // All neighbors already shot — fall through to random
        }
        // Random fallback
        while (true) {
            int row = (int)(Math.random() * 10);
            int col = (int)(Math.random() * 10);
            if (!targetBoard.alreadyShot(row, col)) {
                return new int[]{row, col};
            }
        }
    }
    public static int[] computerChooseShot(Board targetBoard) {
        while (true) {
            int row = (int)(Math.random() * 10);
            int col = (int)(Math.random() * 10);
            if (!targetBoard.alreadyShot(row, col)) {
                return new int[]{row, col};
            }
        }
    }


}
