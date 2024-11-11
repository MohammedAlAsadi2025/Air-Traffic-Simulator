package edu.curtin.saed.assignment1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class App extends Application {
    private GridArea area;
    private List<Airport> airports;
    private boolean isInitialized = false;
    @SuppressWarnings("PMD.UnusedPrivateField") //threadpool is flagged as unused but is required for Plane and Airport to be run through threadpools
    private ExecutorService threadPool; // Thread pool for both airports and planes

    @Override
    public void start(Stage stage) {
        int gridSize = 10;
        area = new GridArea(gridSize, gridSize);
        area.setStyle("-fx-background-color: #006000;");
        airports = new ArrayList<>();
        threadPool = Executors.newFixedThreadPool(110); // Pool for 110 threads (10 airports + 100 planes)

        var startBtn = new Button("Start");
        var endBtn = new Button("End");

        var textArea = new TextArea();
        textArea.appendText("Sidebar\n");
        textArea.appendText("Text\n");

        startBtn.setOnAction((event) -> {
            if (!isInitialized) {
                // Initialize airports
                for (int i = 0; i < 10; i++) {
                    int x, y;
                    do {
                        x = (int) (Math.random() * gridSize);
                        y = (int) (Math.random() * gridSize);
                    } while (isOverlapping(x, y, airports));

                    Airport airport = new Airport(i, x, y, airports, textArea, threadPool);
                    airports.add(airport);

                    area.getIcons().add(new GridAreaIcon(
                        x,
                        y,
                        0.0,
                        1.0,
                        App.class.getClassLoader().getResourceAsStream("airport.png"),
                        "Airport " + i));
                }

                // Initialize planes
                int planeId = 0;
                for (Airport airport : airports) {
                    for (int j = 0; j < 10; j++) {
                        int x = airport.getX();
                        int y = airport.getY();
                        GridAreaIcon planeIcon = new GridAreaIcon(
                            x,
                            y,
                            0.0,
                            1.0,
                            App.class.getClassLoader().getResourceAsStream("plane.png"),
                            "Plane " + planeId + " at Airport " + airport.getId());


                        @SuppressWarnings("PMD.UnusedLocalVariable") //This line is flagged but is needed to run all the planes in the flight sim
                        Plane plane = new Plane(planeId++, x, y, planeIcon, textArea, airport, threadPool);
                        area.getIcons().add(planeIcon);
                    }
                }

                Platform.runLater(() -> area.requestLayout());
                isInitialized = true;
                startBtn.setDisable(true);

                // Start flight requests for each airport
                for (Airport airport : airports) {
                    threadPool.submit(airport::startFlightRequests);
                }
            }
        });

        endBtn.setOnAction((event) -> {
            threadPool.shutdownNow();  // Stop all threads (airport and plane actions)
            
            startBtn.setDisable(true);
            endBtn.setDisable(true);
        });

        var toolbar = new ToolBar();
        toolbar.getItems().addAll(startBtn, endBtn, new Separator());

        var splitPane = new SplitPane();
        splitPane.getItems().addAll(area, textArea);
        splitPane.setDividerPositions(0.75);

        stage.setTitle("Air Traffic Simulator");
        var contentPane = new BorderPane();
        contentPane.setTop(toolbar);
        contentPane.setCenter(splitPane);

        var scene = new Scene(contentPane, 1200, 1000);
        stage.setScene(scene);
        stage.show();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Platform.runLater(() -> area.requestLayout());
            }
        };
        timer.start();
    }

    private boolean isOverlapping(int x, int y, List<Airport> airports) {
        for (Airport airport : airports) {
            if (airport.getX() == x && airport.getY() == y) {
                return true;
            }
        }
        return false;
    }
}
