package com.jdreamer.ui;

import com.jdreamer.cache.MediaUrlCache;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
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
    private WebView webView;

    private Scene mediaPlayerScene;
    private BorderPane root;
    private MediaView mediaView;
    private MediaPlayer mediaPlayer;

    private Label videoLink;
    private Duration duration;
    private Button playButton;
    private Slider timeSlider;
    private Label playTime;

    private boolean stopRequested = false;
    private boolean atEndOfMedia = false;

    public BrowserPanel() {
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


            HBox bottomPanel = new HBox();
            bottomPanel.setAlignment(Pos.CENTER);

            playButton = new Button(">");

            timeSlider = new Slider();
            timeSlider.setPrefWidth(500);

            timeSlider.setMin(0);
            timeSlider.setMax(100);
            timeSlider.setValue(0);          // start at the beginning

            timeSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                // Only act when the slider is *not* being dragged (i.e. the
                // user released the mouse button or touched‑ended).
                if (!isChanging && mediaPlayer != null) {
                    duration = mediaPlayer.getTotalDuration();

                    // Guard against division by zero in case duration is unknown.
                    if (duration == null || duration.isUnknown() || !duration.greaterThan(Duration.ZERO)) {
                        return;
                    }

                    // Convert 0‑100 slider value into a fraction 0‑1.
                    double fraction = timeSlider.getValue() / 100.0;

                    // Seek to the corresponding position in the media.
                    Duration target = duration.multiply(fraction);
                    mediaPlayer.seek(target);
                }
            });

            playTime = new Label("0:00/0:00");
            playTime.setPrefWidth(130);
            playTime.setMinWidth(50);

            playButton.setOnAction(e -> {
                if (mediaPlayer == null) {
                    return;
                }

                MediaPlayer.Status status = mediaPlayer.getStatus();
                if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED) {
                    // don't do anything in these states
                    return;
                }

                if (status == MediaPlayer.Status.PAUSED
                        || status == MediaPlayer.Status.READY
                        || status == MediaPlayer.Status.STOPPED) {
                    // rewind the movie if we're sitting at the end
                    if (atEndOfMedia) {
                        mediaPlayer.seek(mediaPlayer.getStartTime());
                        atEndOfMedia = false;
                    }
                    mediaPlayer.play();
                } else {
                    mediaPlayer.pause();
                }
            });

            bottomPanel.getChildren().addAll(playButton, new Separator(), timeSlider, new Separator(), playTime);
            bottomPanel.autosize();

            mediaView.setFitWidth(900);
            mediaView.setFitHeight(750);

            HBox topPanel = new HBox();
            topPanel.setAlignment(Pos.CENTER);

            videoLink = new Label();
            topPanel.getChildren().add(videoLink);

            root = new BorderPane();

            root.setTop(topPanel);
            root.setCenter(mediaView);
            root.setBottom(bottomPanel);

            mediaPlayerScene = new Scene(root);

            setScene(mediaPlayerScene);
        });
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

    private MediaPlayer prepareMediaPlayer(String url) {
        MediaPlayer mediaPlayer = new MediaPlayer(new Media(url));
        mediaView.setMediaPlayer(mediaPlayer);
        mediaPlayer.play();

        mediaPlayer.currentTimeProperty().addListener(ov -> updateValues());

        mediaPlayer.setOnReady(() -> {
            duration = mediaPlayer.getMedia().getDuration();
            updateValues();
        });

        mediaPlayer.setOnPlaying(() -> {
            if (stopRequested) {
                mediaPlayer.pause();
                stopRequested = false;
            } else {
                playButton.setText("||");
            }
        });

        mediaPlayer.setOnPaused(() -> {
            //System.out.println("onPaused");
            playButton.setText(">");
        });

        mediaPlayer.setOnReady(() -> {
            duration = mediaPlayer.getMedia().getDuration();
            updateValues();
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            playButton.setText(">");
            stopRequested = true;
        });

        return mediaPlayer;
    }

    protected void updateValues() {
        if (mediaPlayer != null && playTime != null && timeSlider != null) {
            Platform.runLater(() -> {
                Duration currentTime = mediaPlayer.getCurrentTime();
                playTime.setText(formatTime(currentTime, duration));

                timeSlider.setDisable(duration.isUnknown());

                if (!timeSlider.isDisabled()
                        && duration.greaterThan(Duration.ZERO)
                        && !timeSlider.isValueChanging()) {
                    timeSlider.setValue(currentTime.divide(duration).toMillis()
                            * 100.0);
                }
            });
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

            setScene(mediaPlayerScene);
        });
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
