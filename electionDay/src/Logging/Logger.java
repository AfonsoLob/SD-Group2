package Logging;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    //private String logFile;
    private PrintWriter writer;
    
    public Logger() {
        try {
            // true parameter enables append mode
            this.writer = new PrintWriter(new FileWriter("log.txt", true));
        } catch (IOException e) {
            System.err.println("Error creating log file: " + e.getMessage());
        }
    }
    
    public void log(String message) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            writer.println("[" + timestamp + "] " + message);
            writer.flush(); // Ensure the message is written immediately
        } catch (Exception e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
    
    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
}