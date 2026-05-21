# Battleship – Java Coding Manual
### A Step-by-Step Guide for AP CSA Students

---

## Read This First

Read this whole document before writing a single line of code. Battleship has a lot of moving parts, and knowing the full picture before you start will save you hours of confusion.

**What you are building:** A GUI game where you (the human) play against the computer. Both sides have a 10×10 grid. You take turns firing shots until one side has sunk all five of the other's ships.

**The four files you will create, in this exact order:**

| File | What it does |
|---|---|
| `Ship.java` | Represents one ship — its name, length, and which cells have been hit |
| `Board.java` | Represents one 10×10 ocean grid and all the logic that goes with it |
| `BattleshipGUI.java` | The window, buttons, colors, and all user interaction |
| `BattleshipGame.java` | The main class — just launches the window |

Build and fully test each file before starting the next one. If you skip ahead, errors will be nearly impossible to trace back to their source.

---

## Time Estimates

These are realistic estimates for someone at AP CSA level working carefully and testing as they go. The total for the core game is about 8–13 hours. If you rush, skip tests, or jump ahead, expect it to take longer — not shorter.

| Part | What you are doing | Estimated time |
|---|---|---|
| Part 1 — `Ship.java` | Fields, constructor, 4 methods, 1 test | 30–45 min |
| Part 2 — `Board.java` (structure + placement) | Constants, fields, constructor, printBoard, isValidPlacement, placeShip, tests | 1–1.5 hrs |
| Part 2 — `Board.java` (shooting) | fireAt, alreadyShot, allShipsSunk, getSunkenShipName, test | 45 min–1 hr |
| Part 3 — Computer logic | placeShipsRandomly, computerChooseShot, test | 30–45 min |
| Part 4 — `BattleshipGUI.java` | All fields, constructor, all build methods, all click handlers, refreshBoards | 3–5 hrs |
| Part 5 — `BattleshipGame.java` | Main class | 5 min |
| Integration testing & bug fixing | Playing the game, finding edge cases | 1–2 hrs |
| **Total (core game)** | | **~6.5–10.5 hrs** |

If you choose to implement optional extensions, budget extra time on top of the above:

| Extension | Extra time |
|---|---|
| Smarter computer AI | 1–2 hrs |
| Save and load | 2–3 hrs |
| Two-player pass-and-play | 1.5–2.5 hrs |

---

## A Note on Testing

Each part below ends with a test block you run inside a temporary `main` method. The cleanest way to do this is to write `public static void main(String[] args)` directly inside whichever class you are currently building, run it, then delete the `main` before moving on. You should never have two `main` methods active at the same time — Java will not know which one to run.

---

## Part 1 — `Ship.java`
**Estimated time: 30–45 minutes**

A `Ship` needs to track three things: its name, its length, and which of its cells have been hit.

### 1.1 — Fields

```java
public class Ship {
    String name;
    int length;
    boolean[] hits;
}
```

`hits` is an array with one slot per cell of the ship. `hits[0]` is the first cell, `hits[1]` the second, and so on. When a cell is shot, its slot becomes `true`. A brand-new `boolean[]` is automatically filled with `false` by Java, so no extra setup is needed.

### 1.2 — Constructor

```java
public Ship(String name, int length) {
    this.name = name;
    this.length = length;
    hits = new boolean[length];
}
```

### 1.3 — Method: `getName()`

```java
public String getName() {
    return name;
}
```

### 1.4 — Method: `getLength()`

```java
public int getLength() {
    return length;
}
```

### 1.5 — Method: `hit(int cellIndex)`

Called when a shot lands on this ship at a specific cell position. Sets that slot in `hits` to `true`.

```java
public void hit(int cellIndex) {
    hits[cellIndex] = true;
}
```

### 1.6 — Method: `isSunk()`

Returns `true` only if every slot in `hits` is `true`. Loop through the array — if you find any `false`, return `false` immediately. If the loop finishes without finding one, return `true`.

```java
public boolean isSunk() {
    for (int i = 0; i < hits.length; i++) {
        if (hits[i] == false) {
            return false;
        }
    }
    return true;
}
```

---

### ✅ Test 1 — Run this inside a temporary `main` in `Ship.java`

```java
public static void main(String[] args) {
    Ship s = new Ship("Destroyer", 2);
    System.out.println(s.isSunk());  // expected: false
    s.hit(0);
    System.out.println(s.isSunk());  // expected: false
    s.hit(1);
    System.out.println(s.isSunk());  // expected: true

    Ship c = new Ship("Carrier", 5);
    c.hit(0); c.hit(1); c.hit(2); c.hit(3);
    System.out.println(c.isSunk());  // expected: false
    c.hit(4);
    System.out.println(c.isSunk());  // expected: true
}
```

All five lines must print the expected value before you continue. Delete `main` when done.

---

## Part 2 — `Board.java`
**Estimated time: 1.75–2.5 hours total across all subsections**

This is the largest class. A `Board` is one player's ocean. It stores what is in every cell, tracks all five ships, and handles placement and shooting logic.

### 2.1 — Constants

At the very top of the class body, before the fields, define these four constants. They describe what can be in any cell of the grid.

```java
public class Board {

    public static final int EMPTY = 0;
    public static final int SHIP  = 1;
    public static final int HIT   = 2;
    public static final int MISS  = 3;

    // fields go below...
}
```

Using named constants means you will never have to remember what the number `2` means — you just write `HIT`.

### 2.2 — Fields

```java
    int[][] grid;           // 10x10 — stores EMPTY, SHIP, HIT, or MISS
    Ship[][] shipGrid;      // 10x10 — stores which Ship object occupies each cell (null if none)
    int[][] shipCellIndex;  // 10x10 — stores which cell index (0, 1, 2...) of that ship is here
    Ship[] ships;           // the up-to-5 Ship objects placed on this board
    int shipCount;          // how many ships have been placed so far
```

**Why three grids?** `grid` tells you what is visible (water, ship, hit, miss). `shipGrid` and `shipCellIndex` are hidden bookkeeping: when a shot hits a cell, you need to know *which* ship was hit and *which part* of it, so you can call `ship.hit(cellIndex)` correctly. Storing this information at placement time is the cleanest solution.

### 2.3 — Constructor

```java
public Board() {
    grid          = new int[10][10];    // all zeros = all EMPTY automatically
    shipGrid      = new Ship[10][10];   // all null by default
    shipCellIndex = new int[10][10];    // all zeros by default
    ships         = new Ship[5];
    shipCount     = 0;
}
```

### 2.4 — Temporary `printBoard()` for testing only

Add this method now. You will use it in tests but you do not need it in the final GUI version — delete it when you start Part 4.

```java
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
```

---

### ✅ Test 2a — Run this inside a temporary `main` in `Board.java`

```java
public static void main(String[] args) {
    Board b = new Board();
    b.printBoard(false);
    // Expected output: 10 rows of 10 dots, with "A B C D E F G H I J" header
    // Row numbers 1-10 on the left, aligned so 10 lines up with 1-9
}
```

Delete `main` when this passes, then add it back for the next test.

---

### 2.5 — Method: `isValidPlacement(int row, int col, int length, boolean horizontal)`

Returns `true` if a ship of the given length can legally be placed starting at `(row, col)`.

Write this method inside the `Board` class in two checks, in this order:

**Check 1 — Does it fit on the board without going off the edge?**

```java
public boolean isValidPlacement(int row, int col, int length, boolean horizontal) {
    if (row < 0 || col < 0 || row >= 10 || col >= 10) return false;
    if (horizontal  && col + length > 10) return false;
    if (!horizontal && row + length > 10) return false;
```

Why `> 10` and not `>= 10`? Because columns are 0–9. A length-3 ship at column 8 would occupy indices 8, 9, and 10. Index 10 does not exist. `8 + 3 = 11`, which is `> 10`, so the check correctly blocks it. A length-3 ship at column 7 occupies indices 7, 8, 9. `7 + 3 = 10`, which is not `> 10`, so it correctly passes.

**Check 2 — Does it overlap any ship already on the board?**

```java
    for (int i = 0; i < length; i++) {
        int r = horizontal ? row : row + i;
        int c = horizontal ? col + i : col;
        if (grid[r][c] == SHIP) return false;
    }
    return true;
}
```

### 2.6 — Method: `placeShip(Ship ship, int row, int col, boolean horizontal)`

Only call this *after* `isValidPlacement` returns `true`. This method trusts the placement is legal and does not re-check.

```java
public void placeShip(Ship ship, int row, int col, boolean horizontal) {
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
```

`shipCellIndex[r][c] = i` records which cell of the ship (position 0, 1, 2…) lives at this grid coordinate. `fireAt` will need this later to call `ship.hit(i)` on the right slot.

---

### ✅ Test 2b — Run this inside a temporary `main` in `Board.java`

```java
public static void main(String[] args) {
    Board b = new Board();

    // A Destroyer of length 2, placed at row index 3, column index 4, horizontally
    // That means it occupies (row=3, col=4) and (row=3, col=5)
    Ship d = new Ship("Destroyer", 2);

    System.out.println(b.isValidPlacement(3, 4, 2, true));  // expected: true
    b.placeShip(d, 3, 4, true);
    b.printBoard(false);
    // Expected: the printed board shows " 4 . . . . S S . . . ."
    // (There is a leading space before single-digit row numbers; row index 3 prints as " 4 ")

    // Overlap check — this ship would land on the Destroyer
    System.out.println(b.isValidPlacement(3, 5, 3, true));  // expected: false

    // Off-edge checks
    System.out.println(b.isValidPlacement(0, 8, 5, true));  // expected: false (8+5=13 > 10)
    System.out.println(b.isValidPlacement(0, 7, 3, true));  // expected: true  (7+3=10, fits)
    System.out.println(b.isValidPlacement(7, 0, 5, false)); // expected: false (7+5=12 > 10)
    System.out.println(b.isValidPlacement(7, 0, 3, false)); // expected: true  (7+3=10, fits)
}
```

All six `println` calls must match before continuing. Delete `main` when done.

---

### 2.7 — Method: `getCell(int row, int col)`

The GUI will call this to know what color to draw each button.

```java
public int getCell(int row, int col) {
    return grid[row][col];
}
```

### 2.8 — Method: `alreadyShot(int row, int col)`

```java
public boolean alreadyShot(int row, int col) {
    return grid[row][col] == HIT || grid[row][col] == MISS;
}
```

### 2.9 — Method: `fireAt(int row, int col)`

Returns an `int` result code:
- `-1` — already shot here (a safety guard; should not happen if your GUI validates first)
- `0` — miss
- `1` — hit, but the ship is not yet fully sunk
- `2` — hit, and that ship is now sunk

```java
public int fireAt(int row, int col) {
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
```

**Important:** Setting `grid[row][col] = HIT` does not erase `shipGrid[row][col]`. Java keeps the object reference alive. This is what allows `getSunkenShipName` to still look up the ship name after the cell has become `HIT`.

### 2.10 — Method: `getSunkenShipName(int row, int col)`

Call this only when `fireAt` just returned `2`. It returns the name of the ship that occupies (or occupied) that cell.

```java
public String getSunkenShipName(int row, int col) {
    return shipGrid[row][col].getName();
}
```

### 2.11 — Method: `allShipsSunk()`

Loops through every ship on this board and returns `true` only if all of them are sunk.

```java
public boolean allShipsSunk() {
    for (int i = 0; i < shipCount; i++) {
        if (!ships[i].isSunk()) {
            return false;
        }
    }
    return true;
}
```

---

### ✅ Test 2c — Run this inside a temporary `main` in `Board.java`

This test builds on the Destroyer placed in Test 2b. Start a fresh `main` that re-creates the board and ship so it runs independently.

```java
public static void main(String[] args) {
    Board b = new Board();
    Ship d = new Ship("Destroyer", 2);
    b.placeShip(d, 3, 4, true);   // occupies (3,4) and (3,5)

    // --- Miss ---
    int r1 = b.fireAt(0, 0);
    System.out.println(r1);                   // expected: 0
    System.out.println(b.alreadyShot(0, 0));  // expected: true
    System.out.println(b.allShipsSunk());     // expected: false

    // --- Duplicate shot guard ---
    System.out.println(b.fireAt(0, 0));       // expected: -1

    // --- First hit (not sunk) ---
    int r2 = b.fireAt(3, 4);
    System.out.println(r2);                   // expected: 1
    System.out.println(b.allShipsSunk());     // expected: false

    // --- Second hit (sunk) ---
    int r3 = b.fireAt(3, 5);
    System.out.println(r3);                                    // expected: 2
    System.out.println(b.getSunkenShipName(3, 5));             // expected: Destroyer
    System.out.println(b.allShipsSunk());                      // expected: true

    b.printBoard(false);
    // Expected: row "4" shows "X X" at columns E and F; row "1" shows "O" at column A
}
```

All nine `println` calls must match. Delete `main` when done.

---

## Part 3 — Computer Logic (static methods inside `Board.java`)
**Estimated time: 30–45 minutes**

Add these two `static` methods at the bottom of `Board.java`, still inside the class. Making them `static` means they belong to the `Board` class itself rather than to any one board object, so you call them as `Board.placeShipsRandomly(...)` rather than on an instance.

### 3.1 — Method: `placeShipsRandomly(Board board)`

Creates all five ships and places them one at a time. The `while` loop keeps re-rolling random coordinates until it finds a legal spot for each ship. This always terminates because there is always at least one legal placement for even the longest ship on a 10×10 board.

```java
public static void placeShipsRandomly(Board board) {
    String[] names   = {"Carrier", "Battleship", "Cruiser", "Submarine", "Destroyer"};
    int[]    lengths = {5, 4, 3, 3, 2};

    for (int i = 0; i < 5; i++) {
        Ship s = new Ship(names[i], lengths[i]);
        boolean placed = false;
        while (!placed) {
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
```

### 3.2 — Method: `computerChooseShot(Board targetBoard)`

Returns a two-element array `{row, col}` representing the computer's next shot. Keeps picking random coordinates until it finds one that has not already been shot.

```java
public static int[] computerChooseShot(Board targetBoard) {
    while (true) {
        int row = (int)(Math.random() * 10);
        int col = (int)(Math.random() * 10);
        if (!targetBoard.alreadyShot(row, col)) {
            return new int[]{row, col};
        }
    }
}
```

---

### ✅ Test 3 — Run this inside a temporary `main` in `Board.java`

```java
public static void main(String[] args) {
    Board computer = new Board();
    Board.placeShipsRandomly(computer);
    computer.printBoard(false);
    // Expected: exactly 17 S cells (5 + 4 + 3 + 3 + 2 = 17), no overlaps
    // Run the program 3-4 times — ships should appear in different positions each run
}
```

Count the S characters by hand once to confirm you get exactly 17. Delete `main` when done.

---

## Optional Extension A — Smarter Computer AI
**Extra time: 1–2 hours | Do this now, before building the GUI, or skip and come back later**

The basic `computerChooseShot` picks completely random cells. A smarter version targets adjacent cells after a hit, which is much more effective. This is the right time to build it because `computerChooseShot` lives in `Board.java` and all the logic you need already exists.

**How it works:** Add two fields to `BattleshipGUI` (you will create this class in Part 4 — write these fields down and add them then):

```java
int lastHitRow = -1;   // -1 means "no active hit to follow up"
int lastHitCol = -1;
```

Then replace `computerChooseShot` with a smarter version. The logic:
1. If `lastHitRow` is `-1`, pick randomly (same as before).
2. If there is a last hit, try each of the four adjacent cells (up, down, left, right) in order.
3. For each adjacent cell, check it is on the board and has not been shot yet.
4. If a valid adjacent cell exists, return it.
5. If all four neighbors have already been shot (or are off the board), reset `lastHitRow` to `-1` and pick randomly.

Update `doComputerTurn` in `BattleshipGUI` to set `lastHitRow` and `lastHitCol` when the result is `1` (hit but not sunk), and reset them to `-1` when the result is `2` (sunk) or `0` (miss).

Because this logic needs `lastHitRow` and `lastHitCol` from `BattleshipGUI`, the cleanest approach at your level is to pass them as parameters:

```java
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
```

Update the call in `doComputerTurn` to pass the fields:
```java
int[] shot = Board.computerChooseShot(playerBoard, lastHitRow, lastHitCol);
```

And keep the basic one-argument version for Test 3 if you want, or just update the test call to pass `-1, -1`.

---

## Part 4 — `BattleshipGUI.java`
**Estimated time: 3–5 hours**

This class creates the game window and handles all user interaction. It uses Java Swing, which is built into Java — no downloads needed.

### 4.1 — Imports

Put these at the very top of `BattleshipGUI.java`, above the class declaration. List each import separately — this avoids a naming conflict between `javax.swing.Timer` and `java.util.Timer` that would cause subtle bugs if you used a wildcard import.

```java
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
```

### 4.2 — Fields

```java
public class BattleshipGUI extends JFrame {

    Board playerBoard;
    Board computerBoard;

    JButton[][] playerButtons;
    JButton[][] computerButtons;

    JLabel statusLabel;

    boolean playerTurn;
    boolean gameOver;
    boolean placingShips;
    boolean placingHorizontal;

    int shipsPlaced;

    String[] shipNames   = {"Carrier", "Battleship", "Cruiser", "Submarine", "Destroyer"};
    int[]    shipLengths = {5, 4, 3, 3, 2};

    // Only add these if you are doing Extension A (smarter AI):
    // int lastHitRow = -1;
    // int lastHitCol = -1;
}
```

### 4.3 — Constructor

The constructor initializes all fields, places the computer's ships, then calls three helper methods to build the window.

```java
public BattleshipGUI() {
    // Window setup
    setTitle("Battleship");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    // Create game boards
    playerBoard   = new Board();
    computerBoard = new Board();
    Board.placeShipsRandomly(computerBoard);

    // Button arrays are allocated here; the buttons themselves are created in buildBoardsPanel
    playerButtons   = new JButton[10][10];
    computerButtons = new JButton[10][10];

    // Game state
    shipsPlaced       = 0;
    placingShips      = true;
    placingHorizontal = true;
    playerTurn        = true;
    gameOver          = false;

    // Build panels — ORDER MATTERS for BorderLayout
    add(buildTopPanel(),    BorderLayout.NORTH);
    add(buildBoardsPanel(), BorderLayout.CENTER);
    add(buildBottomPanel(), BorderLayout.SOUTH);

    pack();
    setVisible(true);
}
```

### 4.4 — Method: `buildTopPanel()`

This panel sits at the top of the window. It holds the orientation toggle button, which the player clicks before placing each ship.

```java
private JPanel buildTopPanel() {
    JPanel panel = new JPanel();

    final JButton orientationButton = new JButton("Orientation: Horizontal");
    orientationButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            placingHorizontal = !placingHorizontal;
            if (placingHorizontal) {
                orientationButton.setText("Orientation: Horizontal");
            } else {
                orientationButton.setText("Orientation: Vertical");
            }
        }
    });

    panel.add(orientationButton);
    return panel;
}
```

Note that `orientationButton` is declared `final`. This is required because the `ActionListener` block references it from inside — Java needs the variable to be final (or effectively final) when it is used inside an anonymous class that references an outer local variable.

> **A note on ActionListener:** The `new ActionListener() { ... }` block is called an anonymous class. It is a shortcut Java provides for wiring up event responses without writing a whole separate class. You have not formally studied this syntax yet, so treat the outer shell as fixed boilerplate — the code you write goes inside `actionPerformed`. Every button click in this project uses this same pattern.

### 4.5 — Method: `buildBoardsPanel()`

This panel holds both 10×10 grids side by side. It creates all 200 buttons, colors them, and wires up their click handlers.

```java
private JPanel buildBoardsPanel() {
    JPanel wrapper = new JPanel(new GridLayout(1, 2, 10, 0));

    // --- Player's board (left side) ---
    JPanel playerPanel = new JPanel(new GridLayout(10, 10));
    for (int row = 0; row < 10; row++) {
        for (int col = 0; col < 10; col++) {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(45, 45));
            btn.setBackground(Color.CYAN);
            btn.setOpaque(true);
            btn.setBorderPainted(false);

            final int r = row;
            final int c = col;
            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handlePlayerBoardClick(r, c);
                }
            });

            playerButtons[row][col] = btn;
            playerPanel.add(btn);
        }
    }

    // --- Computer's board (right side) ---
    JPanel computerPanel = new JPanel(new GridLayout(10, 10));
    for (int row = 0; row < 10; row++) {
        for (int col = 0; col < 10; col++) {
            JButton btn = new JButton();
            btn.setPreferredSize(new Dimension(45, 45));
            btn.setBackground(Color.CYAN);
            btn.setOpaque(true);
            btn.setBorderPainted(false);

            final int r = row;
            final int c = col;
            btn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    handleComputerBoardClick(r, c);
                }
            });

            computerButtons[row][col] = btn;
            computerPanel.add(btn);
        }
    }

    wrapper.add(playerPanel);
    wrapper.add(computerPanel);
    return wrapper;
}
```

> **Why `final int r = row`?** Inside the `ActionListener`, you cannot reference `row` and `col` directly because their values change with each loop iteration. By copying them into new `final` variables `r` and `c` before the `ActionListener` block, each button captures its own fixed coordinates. Without this, every button would respond as if it were at the last position the loop reached.

> **Why `setOpaque(true)` and `setBorderPainted(false)`?** On some platforms (especially Mac), `JButton.setBackground()` has no visible effect unless these two lines are present. They must be on every button.

### 4.6 — Method: `buildBottomPanel()`

This panel holds the status label at the bottom of the window. `statusLabel` must be initialized here — this is the only place it is created.

```java
private JPanel buildBottomPanel() {
    statusLabel = new JLabel("Place your Carrier (length 5) — click YOUR board",
                             SwingConstants.CENTER);
    statusLabel.setFont(new Font("Arial", Font.BOLD, 14));

    JPanel panel = new JPanel();
    panel.add(statusLabel);
    return panel;
}
```

### 4.7 — Method: `refreshBoards()`

Redraws every button based on the current state of both boards. Call this after every ship placement and after every shot. The `revalidate()` and `repaint()` calls at the end force Swing to actually update what is shown on screen — without them, color changes may not appear on some systems.

```java
private void refreshBoards() {
    // Player's board — show everything, including ship positions
    for (int r = 0; r < 10; r++) {
        for (int c = 0; c < 10; c++) {
            int cell = playerBoard.getCell(r, c);
            if (cell == Board.EMPTY) {
                playerButtons[r][c].setBackground(Color.CYAN);
                playerButtons[r][c].setText("");
            } else if (cell == Board.SHIP) {
                playerButtons[r][c].setBackground(Color.GRAY);
                playerButtons[r][c].setText("");
            } else if (cell == Board.HIT) {
                playerButtons[r][c].setBackground(Color.RED);
                playerButtons[r][c].setText("X");
            } else if (cell == Board.MISS) {
                playerButtons[r][c].setBackground(Color.WHITE);
                playerButtons[r][c].setText("O");
            }
        }
    }

    // Computer's board — hide ship positions; only show hits and misses
    for (int r = 0; r < 10; r++) {
        for (int c = 0; c < 10; c++) {
            int cell = computerBoard.getCell(r, c);
            if (cell == Board.EMPTY || cell == Board.SHIP) {
                computerButtons[r][c].setBackground(Color.CYAN);
                computerButtons[r][c].setText("");
            } else if (cell == Board.HIT) {
                computerButtons[r][c].setBackground(Color.RED);
                computerButtons[r][c].setText("X");
            } else if (cell == Board.MISS) {
                computerButtons[r][c].setBackground(Color.WHITE);
                computerButtons[r][c].setText("O");
            }
        }
    }

    revalidate();
    repaint();
}
```

### 4.8 — Method: `handlePlayerBoardClick(int row, int col)`

Called when the player clicks their own (left) board. Only active during the ship-placement phase.

```java
private void handlePlayerBoardClick(int row, int col) {
    if (!placingShips || gameOver) return;

    int length = shipLengths[shipsPlaced];

    if (!playerBoard.isValidPlacement(row, col, length, placingHorizontal)) {
        statusLabel.setText("Invalid! Ship goes off the board or overlaps another. Try again.");
        return;
    }

    Ship s = new Ship(shipNames[shipsPlaced], length);
    playerBoard.placeShip(s, row, col, placingHorizontal);
    shipsPlaced++;
    refreshBoards();

    if (shipsPlaced == 5) {
        placingShips = false;
        statusLabel.setText("All ships placed! Click the RIGHT board to fire.");
    } else {
        statusLabel.setText("Place your " + shipNames[shipsPlaced]
            + " (length " + shipLengths[shipsPlaced] + ") — click YOUR board");
    }
}
```

### 4.9 — Method: `handleComputerBoardClick(int row, int col)`

Called when the player clicks the computer's (right) board. Only active after all ships are placed and it is the player's turn.

```java
private void handleComputerBoardClick(int row, int col) {
    if (placingShips || !playerTurn || gameOver) return;

    if (computerBoard.alreadyShot(row, col)) {
        statusLabel.setText("Already fired there! Pick a different cell.");
        return;
    }

    int result = computerBoard.fireAt(row, col);
    String message;

    if (result == 0) {
        message = "Miss!";
    } else if (result == 1) {
        message = "Hit!";
    } else {
        message = "You sunk the computer's " + computerBoard.getSunkenShipName(row, col) + "!";
    }

    refreshBoards();

    if (computerBoard.allShipsSunk()) {
        statusLabel.setText("YOU WIN! " + message + " All enemy ships destroyed!");
        gameOver = true;
        return;
    }

    playerTurn = false;
    statusLabel.setText(message + " Computer is thinking...");

    // Pause 1 second before the computer fires so the turn feels real.
    // The Timer fires once, calls doComputerTurn, then stops itself.
    Timer timer = new Timer(1000, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            doComputerTurn();
            ((Timer) e.getSource()).stop();
        }
    });
    timer.start();
}
```

### 4.10 — Method: `doComputerTurn()`

Fires the computer's shot. This method is called automatically after the 1-second timer in `handleComputerBoardClick` fires — you never call it directly.

```java
private void doComputerTurn() {
    int[] shot = Board.computerChooseShot(playerBoard);
    // If you built Extension A, use this line instead:
    // int[] shot = Board.computerChooseShot(playerBoard, lastHitRow, lastHitCol);

    int row = shot[0];
    int col = shot[1];

    int result = playerBoard.fireAt(row, col);
    String message;

    if (result == 0) {
        message = "Computer missed!";
        // Extension A: no update needed on a miss
    } else if (result == 1) {
        message = "Computer hit your ship!";
        // Extension A: update lastHitRow = row; lastHitCol = col;
    } else {
        message = "Computer sunk your " + playerBoard.getSunkenShipName(row, col) + "!";
        // Extension A: reset lastHitRow = -1; lastHitCol = -1;
    }

    refreshBoards();

    if (playerBoard.allShipsSunk()) {
        statusLabel.setText("COMPUTER WINS. " + message + " All your ships are gone.");
        gameOver = true;
        return;
    }

    playerTurn = true;
    statusLabel.setText(message + " Your turn — click the right board.");
}
```

---

## Part 5 — `BattleshipGame.java`
**Estimated time: 5 minutes**

This is the entry point of the program. It contains only `main`. Create this file last, after everything else compiles.

```java
import javax.swing.SwingUtilities;

public class BattleshipGame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new BattleshipGUI();
            }
        });
    }
}
```

`SwingUtilities.invokeLater` is standard Swing boilerplate — it ensures the window is created on the correct internal thread. Treat it as a fixed template. The only line you control is `new BattleshipGUI()`.

---

## Part 6 — How to Compile and Run

Put all four `.java` files in the same folder. Open a terminal (command prompt on Windows, Terminal on Mac) in that folder.

**Compile all files at once:**
```
javac *.java
```

**Run the game:**
```
java BattleshipGame
```

If you see compile errors, read each error carefully — Java always shows the file name and line number. Fix errors starting from the very top of the error list. One error often causes a cascade of fake errors below it, so fixing the first one frequently clears several others.

---

## Part 7 — Optional Extensions (Remaining)

Extension A (smarter AI) was covered in Part 3 above, at the right point in the build order. The remaining three extensions are described here. None of them require learning anything beyond what you already know.

---

### Optional Extension B — Save and Load
**Extra time: 2–3 hours | Add after the core game works**

**What it means:** Write the board state to a text file so the player can quit and resume later.

**Where to add it:** Add a Save button to `buildTopPanel()` and a Load button alongside it.

**What to save:** You need to record enough information to fully reconstruct the game. A straightforward approach is to write one number per line representing each cell of each grid, plus the ship names and lengths, plus the current game phase flags.

Save format suggestion — write these values to a file called `savegame.txt`:
```
playerTurn (0 or 1)
placingShips (0 or 1)
shipsPlaced (0-5)
then 100 numbers for playerBoard.grid (row by row)
then 100 numbers for computerBoard.grid (row by row)
then for each ship on playerBoard: name and length
then for each ship on computerBoard: name and length
```

Use `java.io.PrintWriter` and `new java.io.FileWriter("savegame.txt")` to write. Use `java.util.Scanner` and `new java.io.File("savegame.txt")` to read. Both classes are ones you have likely seen in AP CSA.

The hardest part of save/load is that `shipGrid` and `shipCellIndex` are not saved — you must reconstruct them by re-calling `placeShip` for each ship using the positions you can infer from the saved `grid`. This is a planning challenge, not a Java knowledge challenge.

---

### Optional Extension C — Two-Player Pass-and-Play
**Extra time: 1.5–2.5 hours | Add after the core game works**

**What it means:** Two human players share one screen, taking turns without seeing each other's board.

**How to implement it:**
- Remove the computer AI entirely (or make it optional at start).
- Add a second player board and a second set of player buttons.
- After Player 1 fires, show a "Pass the keyboard to Player 2, then click OK" dialog before revealing Player 2's board. Use `JOptionPane.showMessageDialog(this, "Pass to Player 2!")` — this is a one-line popup.
- On Player 2's turn, hide Player 1's ship positions (already handled by `refreshBoards` hiding SHIP cells on the opponent's grid).
- Alternate which player is active by tracking a `currentPlayer` int field (1 or 2).

The main challenge is keeping each player's ship positions invisible to the other. Your `refreshBoards` already hides ships on the right-side board — you just need to extend that logic to cover both players depending on whose turn it is.

---

### Optional Extension D — Polished Graphical Version
**Extra time: 3+ hours | Only attempt if core game is fully working**

You are already building a graphical version using Swing buttons. A more polished version could use `paintComponent` to draw custom shapes, images, or animations on a canvas instead of colored buttons. This requires learning `JPanel.paintComponent`, `Graphics`, and the Java2D drawing API, which is beyond what AP CSA covers. Attempt this only if you have extra time and want to explore on your own.

---

## Part 8 — Full Build and Test Checklist

Work through this list in order. Do not check a box until the behavior described actually works.

**Part 1 — Ship.java**
- [ ] Class compiles with no errors
- [ ] Test 1: all five `println` lines print the correct expected value

**Part 2 — Board.java (structure)**
- [ ] Constants, fields, and constructor compile
- [ ] Test 2a: `printBoard(false)` outputs a clean 10×10 dot grid with correct labels

**Part 2 — Board.java (placement)**
- [ ] Test 2b: all six `isValidPlacement` calls return the correct value
- [ ] Test 2b: `printBoard` shows S in the correct cells after `placeShip`

**Part 2 — Board.java (shooting)**
- [ ] Test 2c: all nine `println` calls print the correct expected value
- [ ] Test 2c: `printBoard` shows X at hit cells and O at the missed cell

**Part 3 — Computer logic**
- [ ] Test 3: printed board has exactly 17 S cells and no overlaps
- [ ] Running Test 3 three or more times produces different ship layouts each time

**Part 4 — BattleshipGUI.java**
- [ ] All 13 imports are listed individually (not as wildcards)
- [ ] Window opens with two 10×10 grids of cyan buttons side by side
- [ ] Orientation button toggles label between Horizontal and Vertical
- [ ] Clicking the left board places ships correctly; gray buttons appear in the right cells
- [ ] Invalid placements show the error message and place nothing
- [ ] After all 5 ships are placed, left board clicks do nothing
- [ ] Orientation button has no effect after all ships are placed (clicking right board fires)
- [ ] Clicking a cell on the right board fires a shot and changes that button's color
- [ ] Clicking the same right-board cell again shows the "already fired" message
- [ ] Computer fires back automatically after approximately 1 second
- [ ] Status label correctly says "Hit!", "Miss!", or "You sunk the [name]!"
- [ ] Sinking a ship displays the correct ship name
- [ ] Game correctly ends and stops accepting clicks when all computer ships are sunk (player win)
- [ ] Game correctly ends and stops accepting clicks when all player ships are sunk (computer win)

**If you built Extension A (smarter AI)**
- [ ] After a hit, the computer targets an adjacent cell on its next turn
- [ ] After a sunk ship, the computer goes back to random targeting

**If you built Extension B (save/load)**
- [ ] Saving writes a file to disk
- [ ] Loading restores the board state, ship positions, and game phase correctly
- [ ] Loading a saved game and continuing produces a valid game outcome

**If you built Extension C (two-player)**
- [ ] A dialog appears between turns asking players to switch
- [ ] Neither player can see the other's ship positions during the game

---

## Common Mistakes and How to Fix Them

**Buttons are not changing color.**
`setOpaque(true)` and `setBorderPainted(false)` must be set on every button when it is created. This is already in the `buildBoardsPanel` code above. If colors still do not appear, also make sure `refreshBoards()` is being called after every state change.

**Colors change in the data but not on screen.**
Make sure `refreshBoards()` ends with `revalidate(); repaint();`. Without those two lines, Swing may not visually update the window after `setBackground()` calls.

**`NullPointerException` when firing.**
This almost always means `shipGrid[row][col]` is `null`, which means `placeShip` did not assign `shipGrid[r][c] = ship` inside the loop. Open `placeShip` and verify all three grid assignments are inside the for loop, not outside it.

**Ship sinks immediately on the first hit, or never sinks.**
Check `shipCellIndex`. Every cell of a ship needs a unique index matching its position (0, 1, 2...). If `shipCellIndex[r][c]` was never set, it defaults to `0` for every cell — so every hit calls `ship.hit(0)`, meaning only `hits[0]` ever becomes `true` and `isSunk()` never triggers. Confirm that `shipCellIndex[r][c] = i` is inside the loop in `placeShip` and uses the loop variable `i`.

**Computer shoots the same cell twice.**
Check that `computerChooseShot` calls `alreadyShot` and only returns a cell when it returns `false`. If you edited the method and accidentally removed that check, every shot becomes random regardless of history.

**Off-by-one errors in placement.**
Coordinates are 0-indexed (rows 0–9, columns 0–9). The bound check `col + length > 10` is intentionally `>` not `>=`. Do not change it.

**Compile error: "variable used in inner class must be final or effectively final".**
This appears when you try to use a loop variable like `row` or `col` inside an `ActionListener`. Fix it by adding `final int r = row; final int c = col;` before the `new ActionListener()` block and using `r` and `c` inside instead. This pattern is already shown in `buildBoardsPanel` — copy it exactly.

**Two `main` methods cause "ambiguous" errors.**
You should only ever have one `main` active at a time. When testing inside `Ship.java` or `Board.java`, delete the temporary `main` before compiling the rest of the project.