package model;

public class Item {
    public double x, y;
    public int type; // 0 = carne, 1 = taco

    public Item(double x, double y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }
}
