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
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import java.util.Scanner;
import javax.swing.JOptionPane;
public class BattleshipGUI extends JFrame {

    Board playerBoard;
    Board computerBoard;

    JButton[][] playerButtons;
    JButton[][] computerButtons;

    JLabel statusLabel;

    boolean playerTurn;
    boolean gameOver;
    boolean placingShipsP1;
    boolean placingShipsP2;
    boolean placingHorizontal;

    int shipsPlaced;
    boolean vsComputer;
    int currentPlayer;

    String[] shipNames   = {"Carrier", "Battleship", "Cruiser", "Submarine", "Destroyer"};
    int[]    shipLengths = {5, 4, 3, 3, 2};
     int lastHitRow = -1;
     int lastHitCol = -1;
    public BattleshipGUI() {
        // Window setup
        setTitle("Battleship");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        int choice = JOptionPane.showConfirmDialog(null, "Play against the Computer? (Select No for local 2-player)", "Game Mode", JOptionPane.YES_NO_OPTION);
        vsComputer = (choice == JOptionPane.YES_OPTION);

        // Create game boards
        playerBoard   = new Board();
        computerBoard = new Board();
        if (vsComputer) {
            Board.placeShipsRandomly(computerBoard);
        }

        // Button arrays are allocated here; the buttons themselves are created in buildBoardsPanel
        playerButtons   = new JButton[10][10];
        computerButtons = new JButton[10][10];

        // Game state
        shipsPlaced       = 0;
        placingShipsP1    = true;
        placingShipsP2    = false;
        placingHorizontal = true;
        currentPlayer     = 1;
        gameOver          = false;

        // Build panels — ORDER MATTERS for BorderLayout
        add(buildTopPanel(),    BorderLayout.NORTH);
        add(buildBoardsPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        pack();
        setVisible(true);
    }
    // Private Helper Methods to Build GUI
    private JPanel buildTopPanel() {
        JPanel panel = new JPanel();

        JButton saveButton = new JButton("Save Game");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveGame();
            }
        });
        
        JButton loadButton = new JButton("Load Game");
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadGame();
            }
        });

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

        panel.add(saveButton);
        panel.add(loadButton);
        panel.add(orientationButton);
        return panel;
    }
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
    private JPanel buildBottomPanel() {
        statusLabel = new JLabel("Player 1: Place your Carrier (length 5) — click LEFT board",
                SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));

        JPanel panel = new JPanel();
        panel.add(statusLabel);
        return panel;
    }
    private void refreshBoards() {
        boolean p1Visible = false;
        boolean p2Visible = false;

        if (vsComputer) {
            p1Visible = true;
            p2Visible = gameOver;
        } else {
            if (placingShipsP1) p1Visible = true;
            else if (placingShipsP2) p2Visible = true;
            else if (currentPlayer == 1) p1Visible = true;
            else if (currentPlayer == 2) p2Visible = true;

            if (gameOver) {
                p1Visible = true;
                p2Visible = true;
            }
        }

        // Player 1's board (Left)
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                int cell = playerBoard.getCell(r, c);
                if (cell == Board.EMPTY || (cell == Board.SHIP && !p1Visible)) {
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

        // Computer's / Player 2's board (Right)
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                int cell = computerBoard.getCell(r, c);
                if (cell == Board.EMPTY || (cell == Board.SHIP && !p2Visible)) {
                    computerButtons[r][c].setBackground(Color.CYAN);
                    computerButtons[r][c].setText("");
                } else if (cell == Board.SHIP) {
                    computerButtons[r][c].setBackground(Color.GRAY);
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

    private void handlePlayerBoardClick(int row, int col) {
        if (gameOver) return;

        if (placingShipsP1) {
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
                placingShipsP1 = false;
                if (!vsComputer) {
                    placingShipsP2 = true;
                    shipsPlaced = 0;
                    currentPlayer = 2; // for refreshBoards visibility
                    JOptionPane.showMessageDialog(this, "Pass to Player 2 to place ships!");
                    refreshBoards();
                    statusLabel.setText("Player 2: Place your Carrier (length 5) - click RIGHT board");
                } else {
                    statusLabel.setText("All ships placed! Click the RIGHT board to fire.");
                }
            } else {
                statusLabel.setText("Player 1: Place your " + shipNames[shipsPlaced]
                        + " (length " + shipLengths[shipsPlaced] + ") — click LEFT board");
            }
        } else if (!placingShipsP1 && !placingShipsP2) {
            if (!vsComputer && currentPlayer == 2) {
                // Player 2 attacking Player 1
                if (playerBoard.alreadyShot(row, col)) {
                    statusLabel.setText("Already fired there! Pick a different cell.");
                    return;
                }

                int result = playerBoard.fireAt(row, col);
                String message = result == 0 ? "Miss!" : (result == 1 ? "Hit!" : "You sunk Player 1's " + playerBoard.getSunkenShipName(row, col) + "!");
                refreshBoards();

                if (playerBoard.allShipsSunk()) {
                    statusLabel.setText("PLAYER 2 WINS! " + message + " All enemy ships destroyed!");
                    gameOver = true;
                    return;
                }

                statusLabel.setText(message);
                JOptionPane.showMessageDialog(this, message + "\nPass the keyboard to Player 1, then click OK!");
                currentPlayer = 1;
                refreshBoards();
                statusLabel.setText("Player 1's turn! Click RIGHT board to fire.");
            }
        }
    }
    private void handleComputerBoardClick(int row, int col) {
        if (gameOver) return;

        if (placingShipsP2) {
            int length = shipLengths[shipsPlaced];
            if (!computerBoard.isValidPlacement(row, col, length, placingHorizontal)) {
                statusLabel.setText("Invalid placement! Try again.");
                return;
            }

            Ship s = new Ship(shipNames[shipsPlaced], length);
            computerBoard.placeShip(s, row, col, placingHorizontal);
            shipsPlaced++;
            refreshBoards();

            if (shipsPlaced == 5) {
                placingShipsP2 = false;
                currentPlayer = 1;
                JOptionPane.showMessageDialog(this, "Pass to Player 1 to start the game!");
                refreshBoards();
                statusLabel.setText("Player 1's turn! Click RIGHT board to fire.");
            } else {
                statusLabel.setText("Player 2: Place your " + shipNames[shipsPlaced]
                        + " (length " + shipLengths[shipsPlaced] + ") — click RIGHT board");
            }
        } else if (!placingShipsP1 && !placingShipsP2) {
            if (currentPlayer == 1) {
                // Player 1 attacking Player 2 / Computer
                if (computerBoard.alreadyShot(row, col)) {
                    statusLabel.setText("Already fired there! Pick a different cell.");
                    return;
                }

                int result = computerBoard.fireAt(row, col);
                String enem = vsComputer ? "computer's" : "Player 2's";
                String message = result == 0 ? "Miss!" : (result == 1 ? "Hit!" : "You sunk the " + enem + " " + computerBoard.getSunkenShipName(row, col) + "!");
                refreshBoards();

                if (computerBoard.allShipsSunk()) {
                    statusLabel.setText("PLAYER 1 WINS! " + message + " All enemy ships destroyed!");
                    gameOver = true;
                    return;
                }

                if (vsComputer) {
                    currentPlayer = 2;
                    statusLabel.setText(message + " Computer is thinking...");
                    Timer timer = new Timer(1000, new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            doComputerTurn();
                            ((Timer) e.getSource()).stop();
                        }
                    });
                    timer.start();
                } else {
                    statusLabel.setText(message);
                    JOptionPane.showMessageDialog(this, message + "\nPass the keyboard to Player 2, then click OK!");
                    currentPlayer = 2;
                    refreshBoards();
                    statusLabel.setText("Player 2's turn! Click LEFT board to fire.");
                }
            }
        }
    }
    private void doComputerTurn() {
        int[] shot = Board.computerChooseShot(playerBoard,lastHitRow,lastHitCol);


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

        currentPlayer = 1;
        statusLabel.setText(message + " Your turn — click the right board.");
    }

    private void saveGame() {
        try {
            PrintWriter out = new PrintWriter(new FileWriter("savegame.txt"));
            out.println(vsComputer ? 1 : 0);
            out.println(currentPlayer);
            out.println(placingShipsP1 ? 1 : 0);
            out.println(placingShipsP2 ? 1 : 0);
            out.println(shipsPlaced);

            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    out.println(playerBoard.getGrid()[r][c]);
                }
            }

            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    out.println(computerBoard.getGrid()[r][c]);
                }
            }

            for (int i = 0; i < shipsPlaced; i++) {
                Ship s = playerBoard.getShips()[i];
                out.println(s.getName());
                out.println(s.getLength());
            }

            for (int i = 0; i < 5; i++) {
                Ship s = computerBoard.getShips()[i];
                out.println(s.getName());
                out.println(s.getLength());
            }

            out.close();
            JOptionPane.showMessageDialog(this, "Game saved to savegame.txt");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving game: " + ex.getMessage());
        }
    }

    private void loadGame() {
        try {
            File f = new File("savegame.txt");
            if (!f.exists()) {
                JOptionPane.showMessageDialog(this, "No save game found.");
                return;
            }
            Scanner in = new Scanner(f);
            
            vsComputer = (in.nextInt() == 1);
            currentPlayer = in.nextInt();
            placingShipsP1 = (in.nextInt() == 1);
            placingShipsP2 = (in.nextInt() == 1);
            shipsPlaced = in.nextInt();

            playerBoard = new Board();
            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    playerBoard.setGridCell(r, c, in.nextInt());
                }
            }

            computerBoard = new Board();
            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    computerBoard.setGridCell(r, c, in.nextInt());
                }
            }

            String[] pNames = new String[shipsPlaced];
            int[] pLengths = new int[shipsPlaced];
            for (int i = 0; i < shipsPlaced; i++) {
                pNames[i] = in.next();
                pLengths[i] = in.nextInt();
            }

            String[] cNames = new String[5];
            int[] cLengths = new int[5];
            for (int i = 0; i < 5; i++) {
                cNames[i] = in.next();
                cLengths[i] = in.nextInt();
            }
            in.close();

            playerBoard.reconstructShips(pNames, pLengths);
            computerBoard.reconstructShips(cNames, cLengths);

            refreshBoards();
            
            if (placingShipsP1) {
                statusLabel.setText("Player 1: Place your " + shipNames[shipsPlaced] + " (length " + shipLengths[shipsPlaced] + ") — click LEFT board");
            } else if (placingShipsP2) {
                statusLabel.setText("Player 2: Place your " + shipNames[shipsPlaced] + " (length " + shipLengths[shipsPlaced] + ") — click RIGHT board");
            } else if (currentPlayer == 2 && vsComputer) {
                statusLabel.setText("Computer's turn...");
                doComputerTurn();
            } else if (currentPlayer == 1) {
                statusLabel.setText("Game loaded! Player 1's turn — click RIGHT board to fire.");
            } else {
                statusLabel.setText("Game loaded! Player 2's turn — click LEFT board to fire.");
            }
            JOptionPane.showMessageDialog(this, "Game loaded from savegame.txt");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading game: " + ex.getMessage());
        }
    }
}
