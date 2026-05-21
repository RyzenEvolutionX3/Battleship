public class Ship {
    private String name;
    private int length;
    boolean [] hits;
    public Ship (String name, int length){
        this.name = name;
        this.length = length;
        hits = new boolean[length];
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }
    public void hit (int cellindex){
        hits[cellindex] = true;
    }
    public boolean isSunk () {
        for (int i = 0; i< hits.length; i++){
            if (hits[i] == false){
                return false;
            }

        }
        return true;
    }
}