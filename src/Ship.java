public class Ship {
    private String name;        // Type of ship (e.g., "Carrier", "Submarine")
    private int length;         // How many cells this ship occupies on the board
    boolean [] hits;            // Array tracking damage on each specific segment of the ship

    public Ship (String name, int length){
        this.name = name;
        this.length = length;
        // Initialize the hits array. All default to false (undamaged).
        hits = new boolean[length];
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    /**
     * Checks if a specific segment of the ship was hit.
     * @param cellindex The index of the ship segment to check.
     * @return true if the segment was hit, false otherwise.
     */
    public boolean getHit(int cellindex) {
        return hits[cellindex];
    }

    /**
     * Marks a specific segment of the ship as damaged when struck by an attack.
     * Called by the Board class when a coordinate containing this ship is fired upon.
     * 
     * @param cellindex Which discrete part of the ship (0 to length-1) was hit.
     */
    public void hit (int cellindex){
        hits[cellindex] = true;
    }

    /**
     * Iterates over the ship's segments to check if it has been fully destroyed.
     * 
     * @return true if ALL segments in the hits array are marked true (ship sinks), false otherwise.
     */
    public boolean isSunk () {
        for (int i = 0; i< hits.length; i++){
            // If even one part is false (not hit), the ship is still afloat
            if (hits[i] == false){
                return false;
            }

        }
        // All segments are damaged
        return true;
    }
}