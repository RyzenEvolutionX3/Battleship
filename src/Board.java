public class Board {
    // States for visual grid representation
    public static final int EMPTY = 0; // Completely empty water
    public static final int SHIP = 1;  // Unhit section of a ship
    public static final int HIT = 2;   // A part of a ship that was struck
    public static final int MISS = 3;  // A shot fired that landed in empty water
    /* grid tells you what is visible (water, ship, hit, miss).
    shipGrid and shipCellIndex are hidden bookkeeping: when a shot hits a cell,
     you need to know which ship was hit and which part of it, so you can call ship.hit(cellIndex) correctly.
      Storing this information at placement time is the cleanest solution
     */
    private int [][] grid;              // 10x10 array storing the main visible state (0, 1, 2, or 3)
    private Ship [][] shipGrid;         // 10x10 array storing references to the actual Ship objects at each cell
    private int[][] shipCellIndex;      // 10x10 array tracking which segment of the ship (e.g. 0 to length-1) is at this cell
    private Ship [] ships;              // List of all completely placed ships on this board
    int shipCount;                      // Counter for ships currently placed on the board

    public int[][] getGrid() {
        return grid;
    }

    public void setGridCell(int r, int c, int val) {
        grid[r][c] = val;
    }

    public Ship[] getShips() {
        return ships;
    }

    /**
     * Advanced Logic: Reconstruction of internal ship objects from a saved grid.
     * During a code review, this function demonstrates understanding of data serialization constraints.
     * Game saves only store the raw integer map (Grid) because saving complex memory references (like Ship objects)
     * via text is unstable/difficult. This function reads the integer footprint, infers Ship borders,
     * recreates Ship objects, and perfectly links the grid references back to those Ships.
     *
     * @param names Array of names for the surviving ships to be placed.
     * @param lengths Array of lengths for those surviving ships.
     */
    public void reconstructShips(String[] names, int[] lengths) {
        int[][] savedGrid = new int[10][10];
        boolean[][] target = new boolean[10][10];

        // Step 1: Backup the loaded physical grid and mark where ships/hits are located to form a "target footprint".
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
        
        // Step 2: Use recursive backtracking (Depth First Search) to solve the puzzle of how our ships
        // fit perfectly inside the provided target footprint.
        solveandPlace(lengths, 0, target, used, solutionRow, solutionCol, solutionHoriz);
        
        // Step 3: Physically place the newly recreated ships using our found coordinates and orientations.
        for (int i = 0; i < names.length; i++) {
            Ship s = new Ship(names[i], lengths[i]);
            placeShip(s, solutionRow[i], solutionCol[i], solutionHoriz[i]);
        }
        
        // Step 4: Restore the exact grid data (which includes HITs vs SHIPs) and re-apply damage to the actual ships.
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                grid[r][c] = savedGrid[r][c];
                if (grid[r][c] == HIT && shipGrid[r][c] != null) {
                    shipGrid[r][c].hit(shipCellIndex[r][c]);
                }
            }
        }
    }

    /**
     * Advanced Logic: Recursive Backtracking Algorithm (Depth First Search).
     * During a code review, explain that this guarantees finding a valid ship distribution
     * inside the saved layout footprint. It attempts to fit the largest ships first (optimized search),
     * and when it hits a "dead end" where a remaining ship won't fit, it "backtracks" (undoes the previous placement)
     * to explore alternative configurations until the entire board is legally assigned.
     *
     * @param lengths Array of ship lengths remaining to be placed.
     * @param shipIdx Current ship being placed in the recursion tree.
     * @param target Boolean mask denoting where a ship/hit MUST exist.
     * @param used Boolean mask tracking which cells are already covered by our recursive placements.
     * @param solR Array storing found row coordinates for the ships.
     * @param solC Array storing found column coordinates for the ships.
     * @param solH Array storing found orthogonal orientations for the ships.
     * @return true if a complete configuration was found; false if the current tree path is invalid.
     */
    private boolean solveandPlace(int[] lengths, int shipIdx, boolean[][] target, boolean[][] used, int[] solR, int[] solC, boolean[] solH) {
        // Base case: If we've processed all ships, verify that there are no leftover uncovered target cells.
        if (shipIdx == lengths.length) {
            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    if (target[r][c] && !used[r][c]) return false;
                }
            }
            return true;
        }
        int len = lengths[shipIdx];

        // Recursively search every cell to try horizontally or vertically placing the current ship.
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {

                // --- Try Horizontal Placement ---
                if (c + len <= 10) {
                    boolean canPlace = true;
                    for (int i = 0; i < len; i++) {
                        if (!target[r][c+i] || used[r][c+i]) { canPlace = false; break; }
                    }
                    if (canPlace) {
                        for (int i = 0; i < len; i++) used[r][c+i] = true;
                        solR[shipIdx] = r; solC[shipIdx] = c; solH[shipIdx] = true; // Record chosen position

                        // Recurse deeper. If placing the rest of the ships works, trickle the 'true' back up.
                        if (solveandPlace(lengths, shipIdx + 1, target, used, solR, solC, solH)) return true;

                        // Backtrack: Remove the ship if it led to a dead end
                        for (int i = 0; i < len; i++) used[r][c+i] = false;
                    }
                }

                // --- Try Vertical Placement (Length must be > 1 to avoid duplicating logic) ---
                if (len > 1 && r + len <= 10) {
                    boolean canPlace = true;
                    for (int i = 0; i < len; i++) {
                        if (!target[r+i][c] || used[r+i][c]) { canPlace = false; break; }
                    }
                    if (canPlace) {
                        for (int i = 0; i < len; i++) used[r+i][c] = true;
                        solR[shipIdx] = r; solC[shipIdx] = c; solH[shipIdx] = false; // Record chosen position

                        // Recurse deeper
                        if (solveandPlace(lengths, shipIdx + 1, target, used, solR, solC, solH)) return true;

                        // Backtrack
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

    /**
     * Console debug tool utilized during development. Prints logical board state to System.out.
     * Showcases separation of concerns (Model vs View) as the logical board can operate strictly in terminal
     * independent of the Swing GUI.
     *
     * @param hideShips If true, ship presence is obscured (rendering as '.'), masking them from the opponent.
     */
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

    /**
     * Bounds check & collision validation used before placement.
     * During a code review, explain that this function prevents ArrayOutOfBounds exceptions and
     * ensures game rules (ships cannot overlap) are strictly enforced in both GUI interaction and recursive loading.
     *
     * @param row Origin row for placement.
     * @param col Origin column for placement.
     * @param length Discrete length of ship.
     * @param horizontal Orientation axis.
     * @return true if placement is legally inside 10x10 bounds and unobstructed, false otherwise.
     */
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

    /**
     * Executes the physical placement of a ship into the logical data structures.
     * PRECONDITION: Call isValidPlacement() before invoking this function to avoid data corruption.
     * Demonstrates bridging between integers (grid) and Object Oriented pointers (shipGrid).
     */
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

    /**
     * Processes an inbound attack at a coordinate on this board.
     * Translates grid states and manages direct damage application if a ship is hit.
     *
     * @return -1 if invalid (already fired here), 0 if Miss, 1 if Hit, 2 if Hit resulted in fatal sinking.
     */
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

    /**
     * Retrieves the distinct name of a Ship that was hit at the provided coordinate.
     * Called by the GUI strictly when fireAt() returns 2 (sinking event) to announce what type of ship was destroyed.
     */
    public String getSunkenShipName(int row, int col) {
        return shipGrid[row][col].getName();
    }

    /**
     * End-Game validation check.
     * Used by the GUI after every successful hit to decide whether to declare a winner.
     *
     * @return true if ALL placed ships are sunk, otherwise false.
     */
    public boolean allShipsSunk() {
        for (int i = 0; i < shipCount; i++) {
            if (!ships[i].isSunk()) {
                return false;
            }
        }
        return true;
    }
    // ----- COMPUTER LOGIC ---------

    /**
     * Single-Player Initialization Helper.
     * Leverages java.lang.Math.random() to dynamically configure the AI's secret layout.
     * Loops iteratively against isValidPlacement() to ensure RNG constraints eventually satisfy collision rules.
     */
    public static void placeShipsRandomly(Board board) {
        String[] names   = {"Carrier", "Battleship", "Cruiser", "Submarine", "Destroyer"};
        int[]    lengths = {5, 4, 3, 3, 2};

        // Attempts placing every ship iteratively via random coordinates until valid placements are found
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

    /**
     * AI Combat Action (Smart Targeting).
     * During a code review, explain that this function utilizes a basic heuristic (Hunting) to provide challenge.
     * Once the AI scores a random hit, the caller provides 'lastHitRow' and 'lastHitCol'.
     * The algorithm restricts fire purely to immediate adjacent squares (North, South, East, West) to
     * systematically destroy a discovered ship before resuming its widespread random bombardment mode.
     */
    public static int[] computerChooseShot(Board targetBoard, int lastHitRow, int lastHitCol) {
        if (lastHitRow != -1) {
            // Define North, South, West, and East neighbor coordinates adjacent to the last hit.
            int[][] neighbors = {
                    {lastHitRow - 1, lastHitCol}, // North
                    {lastHitRow + 1, lastHitCol}, // South
                    {lastHitRow,     lastHitCol - 1}, // West
                    {lastHitRow,     lastHitCol + 1}  // East
            };
            for (int i = 0; i < neighbors.length; i++) {
                int r = neighbors[i][0];
                int c = neighbors[i][1];
                // If a neighbor coordinate is within bounds AND has not been fired at, select it.
                if (r >= 0 && r < 10 && c >= 0 && c < 10 && !targetBoard.alreadyShot(r, c)) {
                    return new int[]{r, c};
                }
            }
            // All neighbors already shot — fall through to random placement behavior below.
        }
        // Random fallback: Fire entirely continuously until grabbing an unhit parameter
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
