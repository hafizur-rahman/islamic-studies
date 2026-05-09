package com.jdreamer;

import com.jdreamer.cache.MediaUrlCache;
import com.jdreamer.service.BookService;
import com.jdreamer.ui.MainWindow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
            SwingUtilities.invokeLater(() -> {
                try {
                    createAndShowGui(bookService);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        };
    }

    private static void createAndShowGui(BookService bookService) throws Exception {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
        }

        JFrame frame = new MainWindow(bookService);

        frame.setLocationRelativeTo(null); // center on screen
        frame.setVisible(true);

        frame.addWindowStateListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                MediaUrlCache.get().shutdown();
            }
        });
    }
}
