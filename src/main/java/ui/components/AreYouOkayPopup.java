package ui.components;

import javax.swing.*;
import java.awt.*;

public class AreYouOkayPopup {

    public void showAreYouOkayPopup(Runnable onYes) {

        SwingUtilities.invokeLater(() -> {

            JDialog dialog = new JDialog((Frame) null, "¿R U OK?", true);
            dialog.setSize(300, 150);
            dialog.setLayout(new BorderLayout());
            dialog.setLocationRelativeTo(null);

            JLabel label = new JLabel("¿Are you feeling fine", SwingConstants.CENTER);
            dialog.add(label, BorderLayout.CENTER);

            JButton yesButton = new JButton("YES");
            yesButton.addActionListener(e -> {
                onYes.run();
                dialog.dispose();
            });

            JPanel p = new JPanel();
            p.add(yesButton);

            dialog.add(p, BorderLayout.SOUTH);
            dialog.setVisible(true);
        });
    }
}

