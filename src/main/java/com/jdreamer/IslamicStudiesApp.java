package com.jdreamer;

import com.jdreamer.service.BookService;
import com.jdreamer.ui.MainWindow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.MaterialLiteTheme;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.swing.*;

@SpringBootApplication
public class IslamicStudiesApp {
    @PersistenceContext
    private EntityManager em;

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");

        SpringApplication.run(IslamicStudiesApp.class, args);
    }

    /**
     * After the Spring context has started, launch the Swing UI.
     */
    @Bean
    CommandLineRunner startUi(BookService bookService) {
        return args -> {
            SwingUtilities.invokeLater(() -> createAndShowGui(bookService));
        };
    }

    private static void createAndShowGui(BookService bookService) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {

        }

        JFrame frame = new MainWindow(bookService);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setLocationRelativeTo(null); // center on screen
        frame.setVisible(true);
    }
}
