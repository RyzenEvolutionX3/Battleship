import javax.swing.SwingUtilities;

public class GameRunner {
    public static void main(String[] args) {
        // Swing GUI applications should ideally run on the Event Dispatch Thread (EDT).
        // SwingUtilities.invokeLater queues the GUI creation task safely, preventing threading issues.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Instantiates and displays the main game window
                new BattleshipGUI();
            }
        });
    }
}
