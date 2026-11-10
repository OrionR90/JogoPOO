package model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameModel {
    public static final int W = 800, H = 450, TOP = 280, BOT = 420, WORLD = 3200;
    // tipos: 0 = jogador, 1 = capanga, 2 = rapido, 3 = chefe
    public static final double[] SPD = {0, 1.5, 2.6, 1.8};
    public static final int[] DMG = {0, 8, 6, 18}, REACH = {0, 65, 60, 95}, CD = {0, 90, 60, 70}, HP = {100, 50, 35, 300};
    public static final int[] TRIG = {500, 1300, 2100, 2900}, CNT = {3, 4, 5, 1};

    private Fighter player;
    private final List<Fighter> enemies = new ArrayList<>();
    private final List<Item> items = new ArrayList<>();
    private final Random rnd = new Random();
    private double cam;
    private int score, wave, toSpawn, spawnT, bat, tick;
    private boolean active, win, hitEvent;

    public GameModel() { reset(); }

    public void reset() {
        player = new Fighter(0, 100, 350);
        enemies.clear();
        items.clear();
        cam = 0; score = 0; wave = 0; toSpawn = 0; spawnT = 0; bat = 0; tick = 0;
        active = false; win = false; hitEvent = false;
    }

    // ---------- getters ----------
    public Fighter getPlayer() { return player; }
    public List<Fighter> getEnemies() { return enemies; }
    public List<Item> getItems() { return items; }
    public double getCam() { return cam; }
    public int getScore() { return score; }
    public int getBat() { return bat; }
    public int getTick() { return tick; }
    public int getWave() { return wave; }
    public boolean isActive() { return active; }
    public boolean isWin() { return win; }
    public boolean isGameOver() { return win || player.hp <= 0; }

    /** Retorna true uma unica vez depois que um golpe do jogador acertou um inimigo. */
    public boolean consumeHit() {
        boolean h = hitEvent;
        hitEvent = false;
        return h;
    }

    // ---------- acoes do jogador ----------
    private boolean canAct() {
        return !isGameOver() && player.atk == 0 && player.hurt == 0 && player.down == 0;
    }

    public void attack() {
        if (!canAct()) return;
        player.combo = (player.comboT > 0 && player.combo < 2) ? player.combo + 1 : 0;
        player.atk = 18;
        player.comboT = 0;
    }

    public void jump() {
        if (!isGameOver() && player.z == 0 && player.down == 0) player.vz = 12;
    }

    // ---------- regras ----------
    private double clamp(double v, double a, double b) { return Math.max(a, Math.min(b, v)); }

    private void hit(Fighter f, int dmg, boolean knock, int dir) {
        f.hp -= dmg; f.hurt = 15; f.atk = 0; f.x += dir * 12;
        if (knock || f.hp <= 0) { f.down = 40; f.kdir = dir; }
    }

    private void punch() {
        Fighter p = player;
        boolean big = p.combo == 2 || bat > 0;
        int dmg = bat > 0 ? 30 : (p.combo == 2 ? 25 : 12);
        double reach = bat > 0 ? 90 : 70;
        if (bat > 0) bat--;
        for (Fighter e : enemies) {
            double dx = (e.x - p.x) * p.face;
            if (e.down == 0 && dx > 0 && dx < reach && Math.abs(e.y - p.y) < 25)
                hit(e, dmg, (big && e.type != 3) || e.hp <= dmg, p.face);
        }
    }

    /** dx e dy: direcao pressionada (-1, 0 ou 1). */
    public void update(int dx, int dy) {
        tick++;
        if (isGameOver()) return;
        Fighter p = player;

        p.moving = false;
        if (p.comboT > 0 && p.atk == 0) p.comboT--;
        if (p.hurt > 0) p.hurt--;
        if (p.down > 0) {
            p.down--;
            if (p.down > 20) p.x += p.kdir * 3;
            if (p.down == 0) p.hurt = 25;
        } else if (p.atk == 0 && p.hurt == 0) {
            p.x += dx * 4; p.y += dy * 3;
            if (dx != 0) p.face = dx;
            if (dx != 0 || dy != 0) { p.moving = true; p.walk += 0.3; }
        }
        p.x = clamp(p.x, cam + 20, active ? cam + W - 20 : WORLD - 20);
        p.y = clamp(p.y, TOP, BOT);

        if (p.z > 0 || p.vz > 0) {
            p.z += p.vz; p.vz -= 0.8;
            if (p.z <= 0) { p.z = 0; p.vz = 0; }
        }

        if (p.atk > 0) {
            p.atk--;
            if (p.atk == 10) punch();
            if (p.atk == 0) p.comboT = 25;
        }

        updateEnemies();
        collectDeadAndItems();
        updateWaves();
    }

    private void updateEnemies() {
        Fighter p = player;
        for (Fighter e : enemies) {
            if (e.cool > 0) e.cool--;
            e.moving = false;
            if (e.down > 0) {
                e.down--;
                if (e.down > 20) e.x += e.kdir * 3;
                if (e.down == 0) e.hurt = 20;
                continue;
            }
            if (e.hurt > 0) { e.hurt--; continue; }
            double dx = p.x - e.x, dy = p.y - e.y;
            e.face = dx > 0 ? 1 : -1;
            if (e.atk > 0) {
                e.atk--;
                if (e.atk == 15 && Math.abs(dx) < REACH[e.type] && Math.abs(dy) < 22
                        && p.z < 30 && p.hurt == 0 && p.down == 0)
                    hit(p, DMG[e.type], e.type == 3, e.face);
            } else if (Math.abs(dx) > REACH[e.type] - 15) {
                e.x += Math.signum(dx) * SPD[e.type]; e.moving = true;
            } else if (Math.abs(dy) > 8) {
                e.y += Math.signum(dy) * Math.min(1.2, SPD[e.type]); e.moving = true;
            } else if (e.cool == 0) {
                e.atk = 30; e.cool = CD[e.type];
            }
            if (e.moving) e.walk += 0.25;
        }
    }

    private void collectDeadAndItems() {
        Iterator<Fighter> it = enemies.iterator();
        while (it.hasNext()) {
            Fighter e = it.next();
            if (e.hp <= 0 && e.down <= 0) {
                it.remove();
                score += e.type == 3 ? 1000 : 100;
                int r = rnd.nextInt(100);
                if (e.type != 3 && r < 20) items.add(new Item(e.x, e.y, 0));
                else if (e.type != 3 && r < 30) items.add(new Item(e.x, e.y, 1));
            }
        }
        items.removeIf(i -> {
            if (Math.abs(i.x - player.x) < 30 && Math.abs(i.y - player.y) < 20) {
                if (i.type == 0) player.hp = Math.min(player.maxHp, player.hp + 30);
                else bat = 10;
                return true;
            }
            return false;
        });
    }

    private void updateWaves() {
        if (!active && wave < TRIG.length && player.x > TRIG[wave]) {
            active = true; toSpawn = CNT[wave]; spawnT = 60;
        }
        if (active) {
            if (toSpawn > 0 && enemies.size() < 3 && ++spawnT > 60) {
                spawnT = 0; toSpawn--;
                int type = wave == 3 ? 3 : (rnd.nextInt(3) == 0 ? 2 : 1);
                double sx = rnd.nextBoolean() ? cam + W + 30 : cam - 30;
                enemies.add(new Fighter(type, sx, TOP + rnd.nextInt(BOT - TOP)));
            }
            if (toSpawn == 0 && enemies.isEmpty()) {
                active = false; wave++;
                if (wave >= TRIG.length) win = true;
            }
        } else {
            cam = Math.max(cam, clamp(player.x - W / 2.0, 0, WORLD - W));
        }
    }
}
