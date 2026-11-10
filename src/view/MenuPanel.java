package view;

import model.GameModel;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

public class MenuPanel extends JPanel {
    private final JButton newGameButton = createButton("NOVO JOGO");
    private final JButton exitButton = createButton("SAIR");
    private final JLabel resultLabel = new JLabel(" ");
    private BufferedImage background;

    public MenuPanel() {
        setPreferredSize(new Dimension(GameModel.W, GameModel.H));
        setLayout(new GridBagLayout());
        loadBackground();

        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        resultLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        resultLabel.setForeground(Color.YELLOW);
        resultLabel.setAlignmentX(CENTER_ALIGNMENT);

        newGameButton.setAlignmentX(CENTER_ALIGNMENT);
        exitButton.setAlignmentX(CENTER_ALIGNMENT);

        box.add(resultLabel);
        box.add(Box.createVerticalStrut(15));
        box.add(newGameButton);
        box.add(Box.createVerticalStrut(12));
        box.add(exitButton);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(170, 0, 0, 0);
        add(box, c);
    }

    private void loadBackground() {
        try {
            URL url = getClass().getResource("/resources/menu_bg.png");
            if (url != null) background = ImageIO.read(url);
            else System.err.println("Imagem nao encontrada: /resources/menu_bg.png");
        } catch (Exception e) {
            System.err.println("Erro ao carregar imagem: " + e);
        }
    }

    private static JButton createButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Monospaced", Font.BOLD, 20));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBackground(new Color(230, 130, 40));
        b.setForeground(Color.WHITE);
        Dimension d = new Dimension(220, 48);
        b.setPreferredSize(d);
        b.setMaximumSize(d);
        return b;
    }

    public void addNewGameListener(ActionListener l) { newGameButton.addActionListener(l); }
    public void addExitListener(ActionListener l) { exitButton.addActionListener(l); }
    public void setLastResult(String text) { resultLabel.setText(text); }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setPaint(new GradientPaint(0, 0, new Color(255, 160, 80), 0, getHeight(), new Color(60, 20, 80)));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        g.setFont(new Font("Monospaced", Font.BOLD, 50));
        String title = "Crocodilos e Dinossauros ";
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(title)) / 2;
        g.setColor(new Color(0, 0, 0, 120));
        g.drawString(title, x + 4, 124);
        g.setColor(Color.WHITE);
        g.drawString(title, x, 120);
    }
}
