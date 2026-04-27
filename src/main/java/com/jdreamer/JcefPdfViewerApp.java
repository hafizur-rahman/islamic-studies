package com.jdreamer;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefLifeSpanHandlerAdapter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class JcefPdfViewerApp {

    public static void main(String[] args) throws UnsupportedPlatformException, CefInitializationException, IOException, InterruptedException {
        //Create a new CefAppBuilder instance
        CefAppBuilder builder = new CefAppBuilder();

        builder.setInstallDir(new File("jcef-bundle")); //Default
        builder.setProgressHandler(new ConsoleProgressHandler()); //Default
        builder.addJcefArgs("--disable-gpu");
        builder.getCefSettings().windowless_rendering_enabled = false; //Don't select OSR mode

        //Set an app handler. Do not use CefApp.addAppHandler(...), it will break your code on MacOSX!
        builder.setAppHandler(new MavenCefAppHandlerAdapter() {

        });

        //Build a CefApp instance using the configuration above
        CefApp cefApp = builder.build();

        // Step 2: Create a CEF client and browser
        CefClient client = cefApp.createClient();

        // Optional: Handle window lifecycle events (e.g., close)
        client.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public void onAfterCreated(CefBrowser browser) {
                System.out.println("Browser initialized!");
            }
        });

        // Create a browser instance and load a URL
        String url = "file:///C:\\Users\\bibag\\work\\islamic-studies\\Library\\7. School Book\\Palestine\\G01\\arabic G1_P1.pdf";
        CefBrowser browser = client.createBrowser(url, false, false); // (URL, isPopup, isWindowless)

        // Step 3: Embed the browser in a Swing window
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Java CEF Browser");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1024, 768); // Window dimensions

            // Add the browser component to the frame
            Component browserComponent = browser.getUIComponent();
            frame.add(browserComponent, BorderLayout.CENTER);

            // Make the window visible
            frame.setVisible(true);
        });

        // Step 4: Shutdown CEF when the app exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cefApp.dispose(); // Clean up CEF resources
            System.out.println("CEF shutdown complete.");
        }));

        //JcefPdfViewer viewer = new JcefPdfViewer(app, );

    }
}

