public class Main {
    public static void main(String[] args) {
        Board computer = new Board();
        Board.placeShipsRandomly(computer);
        computer.printBoard(false);
}}
