package view;

import model.Fighter;
import model.GameModel;
import model.Item;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

public class GamePanel extends JPanel {
    private static final long serialVersionUID = 1L;
	private static final int W = GameModel.W, H = GameModel.H, TOP = GameModel.TOP;
    private static final Color[] COL = {
        new Color(40, 140, 255), new Color(230, 130, 40), new Color(160, 60, 200), new Color(200, 30, 30)
    };

    // true = desenha duas linhas vermelhas mostrando onde os personagens andam
    private static final boolean DEBUG_FLOOR = false;

    // velocidade do fundo em relacao a camera (1.0 = acompanha o chao, menor = efeito de distancia)
    private static final double SCROLL_SPEED = 1.0;
    private static final boolean MIRROR_TILES = true;

    private final GameModel model;
    private BufferedImage background;

    public GamePanel(GameModel model) {
        this.model = model;
        setPreferredSize(new Dimension(W, H));
        setFocusable(true);
        loadBackground();
    }
    
    private void loadBackground() {
        try {
            URL url = getClass().getResource("/resources/game_bg.png");
            if (url != null) background = ImageIO.read(url);
            else System.err.println("Imagem nao encontrada: /resources/game_bg.png (usando cenario padrao)");
        } catch (Exception e) {
            System.err.println("Erro ao carregar imagem: " + e);
        }
    }
	
    //Desenha o fundo: a imagem rolando com a camera, ou o cenario padrao se nao houver imagem
    private void drawScenery(Graphics2D g, double cam) {
        if (background != null) {
            double scale = H / (double) background.getHeight();
            int tw = (int) Math.round(background.getWidth() * scale); // largura da imagem na tela
            int iw = background.getWidth(), ih = background.getHeight();
            if (tw >= GameModel.WORLD) {
                int offset = (int) ((tw - W) * cam / (GameModel.WORLD - W));
                g.drawImage(background, -offset, 0, tw, H, null);
            } else {
                int offset = (int) (cam * SCROLL_SPEED);
                for (int i = offset / tw; i * tw - offset < W; i++) {
                    int x = i * tw - offset;
                    if (MIRROR_TILES && i % 2 == 1) {
                        g.drawImage(background, x + tw, 0, x, H, 0, 0, iw, ih, null); // espelhar imagem
                    } else {
                        g.drawImage(background, x, 0, x + tw, H, 0, 0, iw, ih, null);
                    }
                }
            }
        } else {
            g.setPaint(new GradientPaint(0, 0, new Color(255, 160, 80), 0, TOP, new Color(90, 40, 100)));
            g.fillRect(0, 0, W, TOP);
            g.setColor(new Color(50, 25, 70));
            for (int i = 0; i < 20; i++) g.fillRect((int) (i * 180 - cam * 0.5), 120 + (i % 3) * 25, 120, TOP - 120);
            g.setColor(new Color(90, 80, 70));
            g.fillRect(0, TOP, W, H - TOP);
            g.setColor(new Color(60, 55, 50));
            for (int x = -(int) (cam % 80); x < W; x += 80) g.drawLine(x, TOP, x - 40, H);
        }
        if (DEBUG_FLOOR) {
            g.setColor(Color.RED);
            g.drawLine(0, GameModel.TOP, W, GameModel.TOP);
            g.drawLine(0, GameModel.BOT, W, GameModel.BOT);
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double cam = model.getCam();
        Fighter p = model.getPlayer();

        drawScenery(g, cam);

        // itens
        boolean glow = (model.getTick() / 15) % 2 == 0;
        for (Item i : model.getItems()) {
            int sx = (int) (i.x - cam), sy = (int) i.y;
            if (i.type == 0) {
            	//carne
                g.setColor(new Color(170, 80, 50)); g.fillOval(sx - 12, sy - 12, 24, 14);
                g.setColor(Color.WHITE); g.fillOval(sx + 8, sy - 10, 8, 8);
                if (glow) {
                    g.setStroke(new BasicStroke(2));
                    g.drawOval(sx - 12, sy - 12, 24, 14);
                }
            } else {
                // taco
                if (glow) {
                    g.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.setColor(Color.WHITE);
                    g.drawLine(sx - 14, sy - 4, sx + 14, sy - 12);
                }
                g.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(130, 90, 40));
                g.drawLine(sx - 14, sy - 4, sx + 14, sy - 12);
            }
            g.setStroke(new BasicStroke(1));
        }

        // lutadores ordenados por profundidade (y)
        List<Fighter> all = new ArrayList<>(model.getEnemies());
        all.add(p);
        all.sort(Comparator.comparingDouble(f -> f.y));
        for (Fighter f : all) drawFighter(g, f, cam, f == p);

        drawHud(g, p);
    }
    
    // desenhando o HUD com os comandos e a ponrtuação
    private void drawHud(Graphics2D g, Fighter p) {
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.DARK_GRAY); g.fillRect(15, 12, 204, 18);
        g.setColor(new Color(60, 200, 80)); g.fillRect(17, 14, 200 * Math.max(0, p.hp) / p.maxHp, 14);
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.drawString("SCORE " + model.getScore() + (model.getBat() > 0 ? "   TACO x" + model.getBat() : ""), 15, 50);
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.drawString("Setas: mover | Z: soco (3x = combo) | X: pulo | R: reiniciar | ESC: menu", 15, 68);

        if (!model.isActive() && !model.isWin() && model.getWave() < GameModel.TRIG.length
                && (model.getTick() / 30) % 2 == 0) {
            g.setFont(new Font("Monospaced", Font.BOLD, 36));
            g.setColor(Color.YELLOW);
            g.drawString("GO >", W - 130, 60);
        }
        g.setFont(new Font("Monospaced", Font.BOLD, 48));
        g.setColor(Color.WHITE);
        if (p.hp <= 0) g.drawString("GAME OVER", 240, 230);
        if (model.isWin()) g.drawString("VITORIA!", 270, 230);
    }
    
    // desenhado os lutadores 
    private void drawFighter(Graphics2D g, Fighter f, double cam, boolean isPlayer) {
        int sx = (int) (f.x - cam), gy = (int) f.y;
        double s = f.type == 3 ? 1.5 : 1;
        g.setColor(new Color(0, 0, 0, 90));
        g.fillOval(sx - (int) (24 * s), gy - 6, (int) (48 * s), 12);

        boolean flash = f.hurt > 0 && f.hurt % 4 < 2;
        Color shirt = flash ? Color.WHITE : COL[f.type];
        Color skin = flash ? Color.WHITE : new Color(235, 190, 140);
        Color pants = flash ? Color.WHITE : new Color(50, 50, 70);

        Graphics2D h = (Graphics2D) g.create();
        h.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        h.translate(sx, gy - f.z);
        h.scale(f.face * s, s);
        if (f.down > 0) {
            h.translate(0, -6);
            h.rotate(-Math.PI / 2 * Math.min(1, Math.min((40 - f.down) / 6.0, f.down / 6.0)));
        }
        double sw = f.moving ? Math.sin(f.walk) * 12 : 0;
        boolean kick = f.atk > 0 && f.combo == 2;

        h.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        h.setColor(pants);
        h.drawLine(0, -34, (int) sw, -4);
        if (kick && f.atk < 14 && f.atk > 5) h.drawLine(0, -34, 42, -40);
        else h.drawLine(0, -34, (int) -sw, -4);

        h.setStroke(new BasicStroke(16, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        h.setColor(shirt);
        h.drawLine(0, -40, 0, -60);
        h.setColor(skin);
        h.fillOval(-9, -90, 18, 20);
        h.setColor(Color.BLACK);
        h.fillRect(3, -82, 3, 3);

        h.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        h.setColor(skin);
        if (f.atk > 0 && !kick) {
            int lo = f.type == 0 ? 6 : 10, hi = f.type == 0 ? 14 : 22;
            boolean ext = f.atk < hi && f.atk > lo;
            h.drawLine(0, -62, ext ? 38 : 14, ext ? -62 : -52);
            if (isPlayer && model.getBat() > 0 && ext) {
                h.setColor(new Color(130, 90, 40));
                h.drawLine(38, -62, 72, -82);
            }
        } else {
            h.drawLine(0, -62, (int) (-sw * 0.6), -40);
        }
        h.dispose();

        int bw = (int) (40 * s), top = (int) (gy - f.z - 100 * s);
        g.setColor(Color.RED); g.fillRect(sx - bw / 2, top, bw, 5);
        g.setColor(Color.GREEN); g.fillRect(sx - bw / 2, top, bw * Math.max(0, f.hp) / f.maxHp, 5);
    }
}
