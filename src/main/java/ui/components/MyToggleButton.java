package ui.components;

import ui.windows.Application;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MyToggleButton extends JToggleButton {

    private static final long serialVersionUID = 5848952178038888829L;

    private final Color backgroundColor = Application.light_purple;
    private final Color foregroundColor = Application.darker_purple;


    private ImageIcon image;
    private final Font font = new Font("sansserif", 1, 15);

    public MyToggleButton(String text, String imageSource){
        try {
            Image img = ImageIO.read(getClass().getResource(imageSource));
            image = new ImageIcon(img);
            this.setIcon(image);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        this.setText(text);
        this.setBackground(backgroundColor);
        this.setForeground(foregroundColor.darker());
        //System.out.println(foregroundColor.darker());
        this.setFont(font);
        this.setUI(new StyledToggleButtonUI());
    }

    public MyToggleButton(String text) {
        this.setBackground(backgroundColor);
        //this.setForeground(foregroundColor.darker());
        this.setForeground(foregroundColor);
        this.setText(text);
        this.setFont(font);
        this.setUI(new StyledToggleButtonUI());
    }

    public MyToggleButton(String text, Color backgroundColor, Color foregroundColor) {
        this.setBackground(backgroundColor);
        this.setForeground(foregroundColor);
        this.setText(text);
        this.setFont(font);
        this.setUI(new StyledToggleButtonUI());
    }

    public MyToggleButton() {
        this.setBackground(backgroundColor);
        this.setForeground(foregroundColor.darker());
        this.setText("");
        this.setFont(font);
        this.setUI(new StyledToggleButtonUI());
    }

    /*@Override
    protected void paintComponent(Graphics grphcs) {
        int width = getWidth();
        int height = getHeight();

        Graphics2D g2 = (Graphics2D) grphcs.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Change background if selected
        if (isSelected()) {
            g2.setColor(Application.darker_purple);
        } else {
            g2.setColor(Application.light_purple);
        }

        g2.fillRoundRect(0, 0, width, height, height, height);
        g2.dispose();

        // paint text and icon normally
        super.paintComponent(grphcs);
    }*/

    @Override
    protected void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = isSelected()
                ? Application.light_turquoise    // selected
                : Application.light_purple;    // default

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, w, h, 20, 20);

        g2.dispose();
        super.paintComponent(g);
    }




}