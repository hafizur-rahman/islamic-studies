package com.jdreamer;

import com.jdreamer.ui.MainWindow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.swing.*;

@SpringBootApplication
public class Main {
    @PersistenceContext
    private EntityManager em;

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");

        SpringApplication.run(Main.class, args);
    }

    /**
     * After the Spring context has started, launch the Swing UI.
     */
    @Bean
    CommandLineRunner startUi() {
        return args -> {
            SwingUtilities.invokeLater(Main::createAndShowGui);
        };
    }

    private static void createAndShowGui() {
        JFrame frame = new MainWindow();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLocationRelativeTo(null); // center on screen
        frame.setVisible(true);
    }
}
