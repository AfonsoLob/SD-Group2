package serverSide.main;

import serverSide.interfaces.GUI.IGUI_all;
import commInfra.ServerCom;
import commInfra.ServerCom.ServerComHandler; // Ensure this import is correct
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.SwingUtilities;
import serverSide.GUI.Gui;
import serverSide.entities.PLoggerProxy;
import serverSide.interfaces.Logger.ILogger_GUI;
import serverSide.interfaces.Logger.ILogger_all;
import serverSide.sharedRegions.Logger;

public class ServerLogger {

    public static volatile boolean waitConnection = false; // Server active flag

    private static IGUI_all gui; // GUI instance
    private static Thread guiUpdateThread;
    private static ILogger_all repos; // Logger instance
    private static ILogger_GUI loggerForGui; // Logger interface for GUI updates
    private static ServerCom scon; // Server communication channel
    private static int portNumb = -1;

    private static final String CONFIG_FILE = "config.properties";

    public static void main(String[] args) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            props.load(fis);
            portNumb = Integer.parseInt(props.getProperty("reposPortNumber"));
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error reading configuration file or port number! Using default port 22120. " + e.getMessage());
            portNumb = 22120;
        }

        // Initialize ServerCom but do not start listening yet
        scon = new ServerCom(portNumb);

        // Initialize and show GUI. The GUI will be responsible for calling startServerLogic.
        SwingUtilities.invokeLater(() -> {
            gui = Gui.getInstance(); // Get instance of your server-side GUI
            Gui.window(); // Make GUI visible
            // Your Gui class MUST be set up to call ServerLogger.startServerLogic
            // with parameters from its input fields when a "Start" button is pressed.
            // Example (to be implemented in your Gui.java class's button action listener):
            /*
            startButton.addActionListener(e -> {
                try {
                    int numVoters = Integer.parseInt(votersField.getText()); // Assuming votersField exists in Gui
                    int queueCapacity = Integer.parseInt(capacityField.getText()); // Assuming capacityField exists
                    int votesToClose = Integer.parseInt(votesField.getText()); // Assuming votesField exists
                    ServerLogger.startServerLogic(numVoters, queueCapacity, votesToClose);
                } catch (NumberFormatException nfe) {
                    // Handle invalid input in GUI, e.g., show error message
                    System.err.println("Invalid configuration input: " + nfe.getMessage());
                    if (gui != null) gui.displayMessage("Error: Invalid configuration input.");
                }
            });
            */
            System.out.println("GUI initialized. Please use the GUI to start the server with desired parameters.");
        });
        // main thread finishes here; server operations await GUI interaction.
    }

    public static synchronized void startServerLogic(int numVoters, int queueCapacity, int votesToClose) {
        // Check if server is already running.
        // Assumes scon.isListening() exists in ServerCom and IGUI_all has displayMessage.
        if (waitConnection) {
            System.out.println("Server is already running or attempting to start.");
            if (gui != null) gui.displayMessage("Server is already running.");
            return;
        }

        System.out.println("Attempting to start server with parameters: Voters=" + numVoters +
                           ", QueueCapacity=" + queueCapacity + ", VotesToClose=" + votesToClose);

        repos = Logger.getInstance(numVoters, queueCapacity, votesToClose); // Logger initialized with GUI parameters
        loggerForGui = (ILogger_GUI) repos;

        // Assumes IGUI_all has setSimulationRunning.
        if (gui != null) gui.setSimulationRunning(true);

        scon.start(); // Server starts listening for client connections NOW
        System.out.println("Service is established!");
        System.out.println("Server is listening for service requests on port " + portNumb);
        if (gui != null) gui.displayMessage("Server started and listening on port " + portNumb);

        waitConnection = true; // Signal that the server is active

        guiUpdateThread = new Thread(() -> {
            try {
                while (waitConnection) {
                    if (loggerForGui != null && gui != null) {
                        gui.updateFromLogger(
                                loggerForGui.getVoteCounts(),
                                loggerForGui.getVotersProcessed(),
                                loggerForGui.isStationOpen());
                        gui.updateQueueAndBoothInfo(
                                loggerForGui.getCurrentQueueSize(),
                                loggerForGui.getCurrentVoterInBooth());
                    }
                    // Assumes IGUI_all has getSimulationSpeed.
                    float speedFactor = (gui != null) ? gui.getSimulationSpeed() : 1.0f;
                    long updateInterval = Math.round(500 / speedFactor);
                    Thread.sleep(Math.max(100, updateInterval));
                }
            } catch (InterruptedException e) {
                System.out.println("GUI update thread interrupted.");
                Thread.currentThread().interrupt(); // Preserve interrupt status
            } catch (Exception e) {
                System.err.println("Exception in GUI update thread: " + e.getMessage());
                // e.printStackTrace(); // Uncomment for detailed debugging
            }
            System.out.println("GUI update thread finished.");
        });
        guiUpdateThread.setDaemon(true);
        guiUpdateThread.start();

        PLoggerProxy cliProxy;
        ServerComHandler sconiHandler;

        System.out.println("Server entering client acceptance loop...");
        while (waitConnection) {
            try {
                sconiHandler = scon.accept(); // Blocking call
                if (sconiHandler != null) {
                    cliProxy = PLoggerProxy.getInstanceLoggerProxy(sconiHandler, repos);
                    cliProxy.start();
                } else if (waitConnection) {
                    // This case might occur if accept() is non-blocking or returns null upon interruption
                    // without throwing an IOException, which is less common for standard ServerSocket.accept().
                    System.out.println("Accepted null connection handler, but server is still waiting.");
                }
            } catch (IOException e) {
                if (waitConnection) {
                    // An unexpected error occurred during accept
                    System.err.println("IOException in accept loop: " + e.getMessage() + ". Server might be shutting down or a network error occurred.");
                } else {
                    // This is the expected path when shutdown() closes the ServerSocket via scon.closeServerSocket()
                    System.out.println("Server accept loop interrupted as server is shutting down.");
                }
            }
        }
        System.out.println("Server loop terminated. Starting shutdown sequence...");
        performShutdownSequence(); // Call cleanup method
    }

    private static void performShutdownSequence() {
        System.out.println("Performing shutdown sequence...");
        if (guiUpdateThread != null && guiUpdateThread.isAlive()) {
            guiUpdateThread.interrupt();
            try {
                guiUpdateThread.join(1000); // Wait for the thread to die
            } catch (InterruptedException e) {
                System.err.println("Interrupted while waiting for GUI update thread to finish.");
                Thread.currentThread().interrupt();
            }
        }

        if (gui != null && loggerForGui != null) {
            try {
                gui.updateFromLogger(
                        loggerForGui.getVoteCounts(),
                        loggerForGui.getVotersProcessed(),
                        false // Station is now closed
                );
                gui.updateQueueAndBoothInfo(0, ""); // Clear queue and booth
            } catch (Exception e) {
                 System.err.println("Error updating GUI during shutdown: " + e.getMessage());
            }
        }
        if (gui != null) {
            try {
                gui.setSimulationRunning(false); // Update GUI state
                gui.displayMessage("Server has been shut down.");
            } catch (Exception e) {
                 System.err.println("Error setting GUI simulation status or displaying shutdown message: " + e.getMessage());
            }
        }

        if (scon != null) {
            scon.end(); // Close server communication channels (client sockets, etc.)
        }
        System.out.println("Server communication channel operations ended.");

        if (repos != null) {
            repos.saveCloseFile(); // Save log file
            System.out.println("Logger file saved.");
        }
        System.out.println("Server was shutdown completely.");
    }

    public static synchronized void shutdown() {
        if (waitConnection) {
            System.out.println("Shutdown signal received. Server will stop accepting new connections.");
            waitConnection = false; // Signal loops to terminate
            if (scon != null) {
                // This method needs to be implemented in ServerCom to close the ServerSocket
                // and interrupt the blocking accept() call.
                scon.end();
            }
        } else {
            System.out.println("Shutdown signal received, but server is not currently running or already shutting down.");
        }
    }
}