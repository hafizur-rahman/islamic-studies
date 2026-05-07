package com.jdreamer.ui;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class BrowserPanel extends JFXPanel {
    private Scene scene;
    private WebView webView;
    private Scene mediaPlayerScene;

    private static final String YOUTUBE_VIDEO_ID = "l5mauNBTvUU"; // Replace with your video ID

    public BrowserPanel() {
        Platform.runLater(() -> {
            webView = new WebView();
            scene = new Scene(webView);

            webView.getEngine().setJavaScriptEnabled(true);
            webView.setVisible(false);

            showVideo(YOUTUBE_VIDEO_ID);
        });
    }

    public void showVideo(String videoId) {
        final WebEngine webengine = webView.getEngine();

        webengine.getLoadWorker().stateProperty().addListener(
                (ov, oldState, newState) -> {
                    if (webengine.getDocument() != null) {
                        webengine.executeScript("document.querySelector('VIDEO').pause()");
                    }

                    if (newState == Worker.State.SUCCEEDED) {
                        Document doc = webengine.getDocument();

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                        try {
                            Transformer transformer = TransformerFactory.newInstance().newTransformer();
                            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                            transformer.transform(new DOMSource(doc),
                                    new StreamResult(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)));

                            String result = outputStream.toString(StandardCharsets.UTF_8);

                            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
                            XMLEventReader reader = xmlInputFactory.createXMLEventReader(new StringReader(result));

                            while (reader.hasNext()) {
                                XMLEvent nextEvent = reader.nextEvent();

                                if (nextEvent.isStartElement()) {
                                    StartElement startElement = nextEvent.asStartElement();

                                    if ("video".equalsIgnoreCase(startElement.getName().getLocalPart())) {
                                        Attribute url = startElement.getAttributeByName(new QName("src"));

                                        if (url != null) {
                                            setMediaUrl(url.getValue());

                                            break;
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });

        webengine.load("https://www.youtube.com/watch?v=" + videoId);
    }

    private void setMediaUrl(String url) {
        Media media = new Media(url);
        MediaPlayer mediaPlayer = new MediaPlayer(media);

        MediaView mediaView = new MediaView(mediaPlayer);

        BorderPane root = new BorderPane();
        root.setCenter(mediaView);

        mediaPlayerScene = new Scene(root);
        setScene(mediaPlayerScene);

        mediaPlayer.play();
    }

}
