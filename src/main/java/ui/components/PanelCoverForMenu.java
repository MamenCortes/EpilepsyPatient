package ui.components;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class PanelCoverForMenu extends JPanel {

    private static final long serialVersionUID = 1L;
    private JLabel picLabel;
    private JLabel title;

    public PanelCoverForMenu() {

        setOpaque(false);
        init();

    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        GradientPaint gra = new GradientPaint(0, 0, new Color(35, 166, 97), 0, getHeight(), new Color(22, 116, 66));
        g2.setPaint(gra);
        g2.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    private void init() {
        setLayout(new MigLayout("fill", "[5%][95%]", ""));
        //layout = new MigLayout("fill, wrap", "push[]10[]push", "");
        picLabel = new JLabel();
        //picLabel.setIcon(new ImageIcon(PanelCoverLogIn.class.getResource("/icons/medical_logo_02.png")));
        picLabel.setIcon(new ImageIcon(PanelCoverLogIn.class.getResource("/icons/epilepsy64.png")));
        add(picLabel, "cell 0 0,alignx left,growx");

        title = new JLabel("EpiCare");
        title.setFont(new Font("sansserif", 1, 20));
        title.setForeground(new Color(245, 245, 245));
        add(title, "cell 1 0, align left, grow x");

    }

    public void setTitle(String text) {
        title.setText(text);
    }

}
