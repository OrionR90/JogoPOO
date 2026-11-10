package model;

public class Fighter {
    public double x, y, z, vz, walk;
    public int type, hp, maxHp, face = 1, atk, cool, hurt, down, kdir, combo, comboT;
    public boolean moving;

    public Fighter(int type, double x, double y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.hp = this.maxHp = GameModel.HP[type];
    }
}
