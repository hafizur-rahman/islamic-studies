package com.jdreamer.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class IconLoader {
    public static Icon loadIcon(String path) {
        try (InputStream is = IconLoader.class.getClassLoader().getResourceAsStream(path)) {
            BufferedImage img = ImageIO.read(is);
            return new ImageIcon(img);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load icon: " + path, e);
        }
    }
}
