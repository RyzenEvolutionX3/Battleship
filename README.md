# Battleship - Java GUI Game

Welcome to the **Battleship** project! This is a fully-featured, interactive graphical recreation of the classic board game, built entirely in Java using Swing. Conceived as an AP Computer Science A project, this program has been expanded with advanced mechanics including AI, serialization, and a local multiplayer mode.

## 🌟 Features

* **Polished Interactive GUI**: A clean Java Swing interface that mimics the physical board game, complete with A-J / 1-10 coordinate grids, tactile hit/miss colors, and intuitive ship placement logic.
* **Single Player Mode (Smarter AI)**: Play against a computer opponent that utilizes a heuristic "Hunt and Target" algorithm. It fires randomly to search the board, but once it scores a hit, it intelligently focuses fire on adjacent cells to wipe out discovered ships.
* **Local Multiplayer (Pass-and-Play)**: Play against a friend on the same computer. The game seamlessly manages states, dynamically hiding your fleet layout and prompting players to swap seats between turns to ensure fairness.
* **Save & Load Functionality**: Need to leave mid-game? You can pause, save your progress to a file, and resume exactly where you left off later. The system writes the active board footprint to text and uses a recursive backtracking algorithm (`Depth First Search`) to perfectly reconstruct complex ship objects upon loading.

## 🛠️ Technologies Used

* **Java (JDK 8+)**: The core programming language.
* **Java Swing & AWT**: Used for rendering all graphical components, layout management, and event-driven click handling.
* **Java I/O**: Leverages `java.util.Scanner` and `java.io.PrintWriter` for saving and loading game states to a local `.txt` file.

## 🚀 How to Run

1. **Clone or Download** the project to your local machine.
2. Ensure you have the **Java Development Kit (JDK)** installed.
3. Open the project in an IDE (like IntelliJ IDEA, Eclipse, or VS Code).
4. Locate and run **`GameRunner.java`** in the `src` folder.

*(If running from the command line, navigate to the `src` folder, compile with `javac *.java`, and execute with `java GameRunner`.)*

## 📂 Project Structure

* **`GameRunner.java`**: The main entry point. Safely bootstraps the Swing application on the Event Dispatch Thread (EDT) to prevent freezing.
* **`BattleshipGUI.java`**: The Window/Controller. Manages the UI layouts, colors, user clicks, popups, and the phase-by-phase turn machine.
* **`Board.java`**: The core Model. Manages the 10x10 grids, placement overlap verification, hit detection, AI targeting logic, and the recursive grid deserialization.
* **`Ship.java`**: The entity class representing individual ships. It meticulously tracks its length, name, and specific segment damage to reliably determine when it has completely sunk.
