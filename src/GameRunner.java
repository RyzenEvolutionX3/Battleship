import javax.swing.SwingUtilities;

public class GameRunner {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new BattleshipGUI();
            }
        });
    }
}
