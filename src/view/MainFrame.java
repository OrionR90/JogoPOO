package view;

import model.GameModel;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.CardLayout;

public class MainFrame extends JFrame {
    private static final String MENU = "menu", GAME = "game";

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final MenuPanel menuPanel = new MenuPanel();
    private final GamePanel gamePanel;

    public MainFrame(GameModel model) {
        super("Dino Brawler");
        gamePanel = new GamePanel(model);
        root.add(menuPanel, MENU);
        root.add(gamePanel, GAME);
        setContentPane(root);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    public MenuPanel getMenuPanel() { return menuPanel; }
    public GamePanel getGamePanel() { return gamePanel; }

    public void showMenu() { cards.show(root, MENU); }

    public void showGame() {
        cards.show(root, GAME);
        SwingUtilities.invokeLater(gamePanel::requestFocusInWindow);
    }
}
