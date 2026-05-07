package com.jdreamer.ui;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class BrowserPanel extends JFXPanel {
    private Scene scene;
    private WebView webView;

    private static final String YOUTUBE_VIDEO_ID = "dQw4w9WgXcQ"; // Replace with your video ID

    public BrowserPanel() {
        Platform.runLater(() -> {
            webView = new WebView();
            scene = new Scene(webView);

            webView.getEngine().setJavaScriptEnabled(true);

            showVideo(YOUTUBE_VIDEO_ID);

            setScene(scene);
        });
    }

    public void showVideo(String videoId) {
        final WebEngine webengine = webView.getEngine();

        webengine.getLoadWorker().stateProperty().addListener(
                (ov, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        Document doc = webengine.getDocument();

                        try {
                            Transformer transformer = TransformerFactory.newInstance().newTransformer();
                            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                            transformer.transform(new DOMSource(doc),
                                    new StreamResult(new OutputStreamWriter(new FileOutputStream("data.html"), StandardCharsets.UTF_8)));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

        webengine.load("https://www.youtube.com/watch?v=" + videoId);
    }
}
