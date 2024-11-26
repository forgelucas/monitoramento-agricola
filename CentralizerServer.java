import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Date;

class CentralizerServer {
    private static final int PORT = 8080;
    private long localTime;

    public CentralizerServer() {
        this.localTime = System.currentTimeMillis();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Centralizer Server is running on port " + PORT);
            ExecutorService threadPool = Executors.newFixedThreadPool(4);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream())) {
            @SuppressWarnings("unchecked")
            List<SensorData> dataBatch = (List<SensorData>) inputStream.readObject();

            for (SensorData data : dataBatch) {
                long timeDifference = Math.abs(data.timestamp - localTime);
                if (timeDifference > 1000) {
                    System.out.println("Desync detected. Starting Berkeley sync.");
                    synchronizeClocks();
                }
                logData(clientSocket.getInetAddress().getHostAddress(), data);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

private void logData(String collectorId, SensorData data) {
    try (FileWriter writer = new FileWriter("sensor_data.txt", true)) {
        // Formatação do timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = sdf.format(new Date(data.timestamp)); // Converte o timestamp para uma data legível

        writer.write("Collector: " + collectorId + ", Data: " + 
                     "Temperature: " + data.temperature + ", " +
                     "Humidity: " + data.humidity + ", " +
                     "CO2: " + data.co2 + ", " +
                     "GPS: " + data.gpsLocation + ", " +
                     "Timestamp: " + formattedDate + ", " +  // Data formatada
                     "Crop: " + data.cropType + "\n");
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    private void synchronizeClocks() {
        System.out.println("Synchronizing clocks...");
        // Implementação do algoritmo de Berkeley pode ser expandida aqui
    }

    public static void main(String[] args) {
        new CentralizerServer().startServer();
    }
}
