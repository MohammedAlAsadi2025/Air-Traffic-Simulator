package edu.curtin.saed.assignment1;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class Plane implements Runnable {
    private static final Logger logger = Logger.getLogger(Plane.class.getName());

    private int id;
    private int x;
    private int y;
    private Airport currentAirport;
    private FlightRequest currentRequest;
    private GridAreaIcon icon;
    private boolean isRunning;
    private TextArea textArea;
    @SuppressWarnings("PMD.UnusedPrivateField") //threadpool is flagged as unused but is required for the class to run through a threadpool
    private ExecutorService threadPool;
    

    public Plane(int id, int x, int y, GridAreaIcon icon, TextArea textArea, Airport airport, ExecutorService threadPool) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.icon = icon;
        this.isRunning = true;
        this.textArea = textArea;
        this.currentAirport = airport;
        this.threadPool = threadPool;

        threadPool.submit(this);
        Platform.runLater(() -> icon.setShown(false));
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                currentRequest = currentAirport.getFlightQueue().take();
                Platform.runLater(() -> textArea.appendText("Plane " + id + " received a flight request: from Airport " + currentAirport.getId() + " to Airport " + currentRequest.getDestination().getId()));

                Platform.runLater(() -> icon.setShown(true));
                moveTo(currentRequest.getDestination());
                executePlaneServiceScript();
                currentAirport = currentRequest.getDestination();
                currentRequest = null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void moveTo(Airport destination) {
        while (x != destination.getX() || y != destination.getY()) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            if (x < destination.getX()) { x++; }
            if (x > destination.getX()) { x--; }
            if (y < destination.getY()) { y++; }
            if (y > destination.getY()) { y--; }

            String movementLog = "Plane " + id + " moving to (" + x + ", " + y + ")";
            Platform.runLater(() -> {
                textArea.appendText(movementLog + "\n");
                icon.setPosition(x, y);
                icon.setShown(true);
            });

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void executePlaneServiceScript() {
        try {
            int airportId = currentAirport.getId();
            int planeId = this.id;

            ProcessBuilder pb = new ProcessBuilder("./comms/bin/saed_plane_service.bat", String.valueOf(airportId), String.valueOf(planeId));
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String outputLine = line;
                    Platform.runLater(() -> textArea.appendText(outputLine + "\n"));
                }
            }

            process.waitFor();
            Platform.runLater(() -> textArea.appendText("Plane sleep completed after script execution.\n"));
        } catch (IOException | InterruptedException e) {
            logger.info(() -> "Plane " + id + " moving to (" + x + ", " + y + ")");
        }
    }
}
