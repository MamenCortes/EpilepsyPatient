package ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
/**
 * Custom UI delegate for toggle buttons, applying padding and transparent background.
 */
public class StyledToggleButtonUI extends BasicToggleButtonUI {
    @Override
    public void installUI (JComponent c) {
        super.installUI(c);
        AbstractButton button = (AbstractButton) c;
        button.setOpaque(false);
        button.setBorder(new EmptyBorder(5, 15, 5, 15));
    }

    @Override
    public void paint (Graphics g, JComponent c) {
        super.paint(g, c);
    }
}
