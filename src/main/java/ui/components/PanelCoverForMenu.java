package ui.components;

import net.miginfocom.swing.MigLayout;
import ui.windows.Application;

import javax.swing.*;
import java.awt.*;

public class PanelCoverForMenu extends JPanel {

    private static final long serialVersionUID = 1L;
    private JLabel picLabel;
    private JLabel title;
    private ImageIcon logo;
    private String company_name;

    public PanelCoverForMenu(ImageIcon logo, String comany_name) {

        this.logo = logo;
        this.company_name = comany_name;
        setOpaque(false);
        init();

    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        //GradientPaint gra = new GradientPaint(0, 0, new Color(35, 166, 97), 0, getHeight(), new Color(22, 116, 66)); //Degradado de arriba a abajo
        //GradientPaint gra = new GradientPaint(0, 0, new Color(239, 232, 255), getWidth(), 0, new Color(193, 252, 244)); //degradado de izq a der
        GradientPaint gra = new GradientPaint(0, 0, Application.light_purple, getWidth(), 0, Application.light_turquoise); //degradado de izq a der
        g2.setPaint(gra);
        //g2.setPaint(Color.white);
        g2.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
    }

    private void init() {
        setLayout(new MigLayout("fill", "2%[10%]2%[90%]", ""));
        //layout = new MigLayout("fill, wrap", "push[]10[]push", "");
        picLabel = new JLabel();
        //picLabel.setIcon(new ImageIcon(PanelCoverLogIn.class.getResource("/icons/medical_logo_02.png")));
        //picLabel.setIcon(new ImageIcon(PanelCoverLogIn.class.getResource("/icons/epilepsy64.png")));
        picLabel.setIcon(logo);
        //add(picLabel, "cell 0 0,alignx left,growx");
        add(picLabel, "cell 0 0,align center");

        title = new JLabel(company_name);
        //title.setFont(new Font("Segoe UI", Font.BOLD, 30));
        title.setFont(new Font("sansserif", 1, 20));
        //title.setForeground(new Color(245, 245, 245));
        title.setForeground(Application.dark_purple);
        //add(title, "cell 1 0, align left, growx 0");
        add(title, "cell 1 0, align left, growx 0");

    }

    public void setTitle(String text) {
        title.setText(text);
    }

}
