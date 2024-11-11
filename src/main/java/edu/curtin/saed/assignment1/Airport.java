package edu.curtin.saed.assignment1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class Airport {

    private int id;
    private int x;
    private int y;
    private List<Airport> airports;
    private BlockingQueue<FlightRequest> flightQueue;
    private TextArea textArea;
    @SuppressWarnings("PMD.UnusedPrivateField")
    private ExecutorService threadPool; //threadpool is flagged as unused but is required for the class to run through a threadpool

    public Airport(int id, int x, int y, List<Airport> airports, TextArea textArea, ExecutorService threadPool) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.airports = airports;
        this.flightQueue = new LinkedBlockingQueue<>();
        this.textArea = textArea;
        this.threadPool = threadPool; 
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public BlockingQueue<FlightRequest> getFlightQueue() {
        return flightQueue;
    }

    public void startFlightRequests() {
        while (true) {
            try {
                Thread.sleep(2000);

                ProcessBuilder pb = new ProcessBuilder("./comms/bin/saed_flight_requests.bat", String.valueOf(airports.size()), String.valueOf(id));
                pb.redirectErrorStream(true);
                Process process = pb.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        int destinationId = Integer.parseInt(line.trim());
                        Airport destination = airports.stream().filter(airport -> airport.getId() == destinationId).findFirst().orElse(null);

                        if (destination != null && !destination.equals(this)) { 
                            flightQueue.put(new FlightRequest(this, destination));

                            String logMessage = "Flight request enqueued: from Airport " + id + " to Airport " + destination.getId();
                            System.out.println(logMessage);
                            Platform.runLater(() -> textArea.appendText(logMessage + "\n"));
                        }
                    }
                }

                process.waitFor();
            } catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
