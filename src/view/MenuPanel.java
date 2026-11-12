package view;

import model.GameModel;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URL;

public class MenuPanel extends JPanel {

    
    private static final long serialVersionUID = 1L;

	private static final boolean DEBUG = false;

    // Posicao dos botoes (x, y, largura, altura),
    private static final Rectangle NEW_GAME_AREA = new Rectangle(478, 612, 717, 118);
    private static final Rectangle EXIT_AREA = new Rectangle(523, 752, 625, 116);

    private final JButton newGameButton = createButton("NOVO JOGO");
    private final JButton exitButton = createButton("SAIR");
    private final JLabel resultLabel = new JLabel(" ", JLabel.CENTER);
    private BufferedImage background;

    public MenuPanel() {
        setPreferredSize(new Dimension(GameModel.W, GameModel.H));
        setLayout(null); 
        loadBackground();
        failBackground(newGameButton, "Novo Jogo");
        failBackground(exitButton, "Sair");

        resultLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        resultLabel.setForeground(Color.WHITE);

        add(newGameButton);
        add(exitButton);
        add(resultLabel);
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
    
    private void failBackground(JButton b, String text) {
    	if(background != null) return;
    	b.setText(text);
    	b.setOpaque(true);
    	b.setContentAreaFilled(true);
    	b.setBorderPainted(true);
    	b.setFont(new Font("Monospaced", Font.BOLD, 20));
    	b.setBackground(new Color(50,50,50));
    	b.setForeground(Color.WHITE);
    	b.setBorder(BorderFactory.createLineBorder(Color.WHITE,2));
    }

    // Botao invisivel so a area clicavel, o desenho vem da imagem de fundo.
    private static JButton createButton(String text) {
        JButton b = new JButton(DEBUG ? text : "");
        b.setToolTipText(text);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorderPainted(DEBUG);
        if (DEBUG) {
            b.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            b.setForeground(Color.RED);
        }
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    //Converte um retangulo em pixels da imagem para pixels do painel
    private Rectangle scaled(Rectangle r) {
        int imgW = background != null ? background.getWidth() : getWidth();
        int imgH = background != null ? background.getHeight() : getHeight();
        double sx = getWidth() / (double) imgW, sy = getHeight() / (double) imgH;
        return new Rectangle((int) (r.x * sx), (int) (r.y * sy), (int) (r.width * sx), (int) (r.height * sy));
    }

    @Override
    public void doLayout() {
        if(background != null) {
	    	newGameButton.setBounds(scaled(NEW_GAME_AREA));
	        exitButton.setBounds(scaled(EXIT_AREA));
        }else {
        	int cx = getWidth() / 2;
        	newGameButton.setBounds(cx - 100, 200, 220, 50);
        	exitButton.setBounds(cx - 100, 270, 220, 50);
        }
        resultLabel.setBounds(0, getHeight() - 40, getWidth(), 30);
    }

    public void addNewGameListener(ActionListener l) { newGameButton.addActionListener(l); }
    public void addExitListener(ActionListener l) { exitButton.addActionListener(l); }
    public void setLastResult(String text) { resultLabel.setText(text); }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setPaint(new GradientPaint(0, 0, new Color(255, 160, 80), 0, getHeight(), new Color(60, 20, 80)));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setFont(new Font("Monospaced", Font.BOLD, 40));
            String title = "Crocodilos e Dinossauros";
            int x = (getWidth() - g.getFontMetrics().stringWidth(title)) / 2;
            g.setColor(new Color(0,0,0,120));
            g.drawString(title, x + 3, 123);
            g.setColor(Color.WHITE);
            g.drawString(title, x, 120);
        }
    }
}
