package controller;

import model.GameModel;
import util.AudioManager;
import view.MainFrame;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

public class GameController implements ActionListener, KeyListener {
    private static final int END_DELAY_TICKS = 210; // 3,5 s mostrando VITORIA / GAME OVER e voltando para o menu

    // camihos dentro da pasta src/resources
    private static final String MENU_MUSIC = "/resources/menu.wav";
    private static final String GAME_MUSIC = "/resources/game.wav";
    private static final String HIT_SFX = "/resources/hit.wav";
    private static final String LOSE_SFX = "/resources/gameover.wav";
    private static final String WIN_SFX = "/resources/victory.wav";

    private final GameModel model = new GameModel();
    private final MainFrame frame = new MainFrame(model);
    private final Timer timer = new Timer(16, this); // 60 FPS
    private final Set<Integer> keys = new HashSet<>();
    private int endTicks;

    public GameController() {
        frame.getMenuPanel().addNewGameListener(e -> startGame());
        frame.getMenuPanel().addExitListener(e -> System.exit(0));
        frame.getGamePanel().addKeyListener(this);
    }

    public void start() {
        frame.showMenu();
        frame.setVisible(true);
        AudioManager.playMusic(MENU_MUSIC);
    }

    private void startGame() {
        model.reset();
        keys.clear();
        endTicks = 0;
        frame.showGame();
        AudioManager.playMusic(GAME_MUSIC);
        timer.start();
    }

    private void endGame(String message) {
        timer.stop();
        frame.getMenuPanel().setLastResult(message);
        frame.showMenu();
        AudioManager.playMusic(MENU_MUSIC);
    }

    private boolean down(int code) { return keys.contains(code); }

    // game loop
    @Override
    public void actionPerformed(ActionEvent e) {
        int dx = (down(KeyEvent.VK_RIGHT) ? 1 : 0) - (down(KeyEvent.VK_LEFT) ? 1 : 0);
        int dy = (down(KeyEvent.VK_DOWN) ? 1 : 0) - (down(KeyEvent.VK_UP) ? 1 : 0);
        model.update(dx, dy);
        frame.getGamePanel().repaint();

        if (model.consumeHit()) AudioManager.playSound(HIT_SFX);

        if (model.isGameOver()) {
            if (endTicks == 0) { // primeiro frame do fim de jogo
                AudioManager.stopMusic();
                AudioManager.playSound(model.isWin() ? WIN_SFX : LOSE_SFX);
            }
            if (++endTicks > END_DELAY_TICKS) {
                String result = model.isWin() ? "VITORIA!" : "GAME OVER";
                endGame("Ultima partida: " + result + " - Pontos: " + model.getScore());
            }
        }
    }

    // captutando os comandos do teclado
    @Override
    public void keyPressed(KeyEvent e) {
        int c = e.getKeyCode();
        keys.add(c);
        if (c == KeyEvent.VK_Z) model.attack();
        if (c == KeyEvent.VK_X) model.jump();
        if (c == KeyEvent.VK_R) {
            model.reset();
            endTicks = 0;
            AudioManager.playMusic(GAME_MUSIC);
        }
        if (c == KeyEvent.VK_ESCAPE) endGame("Partida abandonada");
    }

    @Override public void keyReleased(KeyEvent e) { keys.remove(e.getKeyCode()); }
    @Override public void keyTyped(KeyEvent e) {}
}
