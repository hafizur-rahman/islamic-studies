package com.jdreamer.ui;

import com.jdreamer.cache.MediaUrlCache;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.swing.event.ChangeListener;
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
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class VideoPlayer extends JPanel {
    private JFXPanel jfxPanel;
    private Label videoLink;

    private Scene mediaPlayerScene;
    private BorderPane root;
    private MediaView mediaView;
    private WebView webView;

    private MediaPlayer mediaPlayer;
    private Duration totalDuration = Duration.ZERO;

    private JSlider progressSlider;
    private JButton playPauseBtn;
    private JLabel playTime;

    public VideoPlayer() {
        setLayout(new BorderLayout());

        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        add(controlPanel, BorderLayout.SOUTH);

        playPauseBtn = new JButton(">");
        playPauseBtn.addActionListener(e -> togglePlayPause(playPauseBtn));
        controlPanel.add(playPauseBtn);

        progressSlider = new JSlider(0, 100, 0);
        progressSlider.setPreferredSize(new Dimension(400, 30));

        // Seek on slider click
        progressSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (mediaPlayer != null) {
                    mediaPlayer.seek(javafx.util.Duration.seconds(progressSlider.getValue()));
                }
            }
        });

        progressSlider.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent arg0) {
                try{
                    if (mediaPlayer != null) {
                        Duration d =  javafx.util.Duration.seconds(progressSlider.getValue());
                        progressSlider.setValue((int) d.toSeconds());

                        mediaPlayer.seek(d);
                    }
                } catch (Exception e3){
                }
            }
        });
        
        controlPanel.add(progressSlider);

        playTime = new JLabel("0:00/0:00");
        controlPanel.add(playTime);

        initJFXPanel();
    }

    private void initJFXPanel() {
        Platform.runLater(() -> {
            mediaView = new MediaView();
            webView = new WebView();

            final WebEngine webengine = webView.getEngine();

            webengine.setJavaScriptEnabled(true);
            webView.setVisible(false);

            webengine.getLoadWorker().stateProperty().addListener(
                    (ov, oldState, newState) -> {
                        pauseVideo(webengine);

                        if (newState == Worker.State.SUCCEEDED) {
                            Document doc = webengine.getDocument();

                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                            try {
                                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                                transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

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
                                                String pageUrl = webengine.getLocation();

                                                MediaUrlCache.get().put(pageUrl, url.getValue());   // url == <video src>

                                                setMediaUrl(url.getValue());

                                                Thread.sleep(10);

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

            mediaView.setFitWidth(900);
            mediaView.setFitHeight(750);
            mediaView.setPreserveRatio(true);

            HBox topPanel = new HBox();
            topPanel.setAlignment(Pos.CENTER);

            videoLink = new Label();
            topPanel.getChildren().add(videoLink);

            root = new BorderPane();
            root.setTop(topPanel);
            root.setCenter(mediaView);

            mediaPlayerScene = new Scene(root);
            jfxPanel.setScene(mediaPlayerScene);
        });
    }

    public void showVideo(String url) {
        Platform.runLater(() -> {
            videoLink.setText(url);

            // 1. Look for a cached media URL for this page
            String cachedMediaUrl = MediaUrlCache.get().get(url);

            if (cachedMediaUrl != null) {
                // 2a. Cache hit – play the cached video directly
                setMediaUrl(cachedMediaUrl);
            } else {
                // 2b. Cache miss – load the page normally
                final WebEngine webengine = webView.getEngine();
                webengine.load(url);

                try {
                    Thread.sleep(100);

                    if (mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.PLAYING) {
                        mediaPlayer.play();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void togglePlayPause(JButton button) {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();

                button.setText(">");
            } else {
                mediaPlayer.play();

                button.setText("||");
            }
        }
    }

    private static void pauseVideo(WebEngine webengine) {
        String pageUrl = webengine.getLocation();

        if (pageUrl != null && pageUrl.startsWith("https://www.youtube.com/") && webengine.getDocument() != null) {
            try {
                webengine.executeScript("document.querySelector('VIDEO').pause()");
            } catch (Exception e) {
                // Suppress the error
            }
        }
    }

    private void setMediaUrl(String url) {
        Platform.runLater(() -> {
            pauseVideo(webView.getEngine());

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            mediaPlayer = prepareMediaPlayer(url);
            mediaPlayer.play();

            jfxPanel.setScene(mediaPlayerScene);
        });
    }

    private MediaPlayer prepareMediaPlayer(String url) {
        MediaPlayer mediaPlayer = new MediaPlayer(new Media(url));
        mediaView.setMediaPlayer(mediaPlayer);

        // Bind slider to video duration and current time
        mediaPlayer.totalDurationProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                totalDuration = newVal;

                progressSlider.setMaximum((int) newVal.toSeconds());
            }
        });

        mediaPlayer.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                progressSlider.setValue((int) newVal.toSeconds());

                playTime.setText(formatTime(newVal, totalDuration));
            }
        });

        return mediaPlayer;
    }

    private static String formatTime(Duration elapsed, Duration duration) {
        int intElapsed = (int) Math.floor(elapsed.toSeconds());
        int elapsedHours = intElapsed / (60 * 60);
        if (elapsedHours > 0) {
            intElapsed -= elapsedHours * 60 * 60;
        }
        int elapsedMinutes = intElapsed / 60;
        int elapsedSeconds = intElapsed - elapsedHours * 60 * 60
                - elapsedMinutes * 60;

        if (duration.greaterThan(Duration.ZERO)) {
            int intDuration = (int) Math.floor(duration.toSeconds());
            int durationHours = intDuration / (60 * 60);
            if (durationHours > 0) {
                intDuration -= durationHours * 60 * 60;
            }
            int durationMinutes = intDuration / 60;
            int durationSeconds = intDuration - durationHours * 60 * 60
                    - durationMinutes * 60;
            if (durationHours > 0) {
                return String.format("%d:%02d:%02d/%d:%02d:%02d",
                        elapsedHours, elapsedMinutes, elapsedSeconds,
                        durationHours, durationMinutes, durationSeconds);
            } else {
                return String.format("%02d:%02d/%02d:%02d",
                        elapsedMinutes, elapsedSeconds, durationMinutes,
                        durationSeconds);
            }
        } else {
            if (elapsedHours > 0) {
                return String.format("%d:%02d:%02d", elapsedHours,
                        elapsedMinutes, elapsedSeconds);
            } else {
                return String.format("%02d:%02d", elapsedMinutes,
                        elapsedSeconds);
            }
        }
    }
}
