package ui.components;

import BITalino.AlarmManager;

import javax.swing.*;
import java.awt.*;

public class AreYouOkayPopup {

    public void showAreYouOkayPopup(Runnable onYes) {

        SwingUtilities.invokeLater(() -> {

            JDialog dialog = new JDialog((Frame) null, "¿Estás bien?", true);
            dialog.setSize(300, 150);
            dialog.setLayout(new BorderLayout());
            dialog.setLocationRelativeTo(null);

            JLabel label = new JLabel("¿Te encuentras bien?", SwingConstants.CENTER);
            dialog.add(label, BorderLayout.CENTER);

            JButton yesButton = new JButton("Sí");
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

