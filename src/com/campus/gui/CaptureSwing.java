package com.campus.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class CaptureSwing {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                CampusSwingApp app = new CampusSwingApp();
                app.setSize(1020, 720);
                app.setVisible(true);

                // Give Swing a short moment to layout and render
                Timer timer = new Timer(800, evt -> {
                    try {
                        BufferedImage image = new BufferedImage(app.getWidth(), app.getHeight(), BufferedImage.TYPE_INT_RGB);
                        Graphics2D g2 = image.createGraphics();
                        app.printAll(g2);
                        g2.dispose();

                        File out = new File("screenshots/08_swing_desktop_gui.png");
                        ImageIO.write(image, "png", out);
                        System.out.println("SUCCESS: Saved Swing screenshot to " + out.getAbsolutePath());
                        app.dispose();
                        System.exit(0);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.exit(1);
                    }
                });
                timer.setRepeats(false);
                timer.start();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
