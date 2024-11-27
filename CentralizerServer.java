import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
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

            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClient(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (DataInputStream inputStream = new DataInputStream(clientSocket.getInputStream())) {
            String clientAddress = clientSocket.getInetAddress().getHostAddress();

            while (true) {
                SensorData data = readSensorData(inputStream);
                if (data == null) break; // Conexão encerrada

                long timeDifference = Math.abs(data.timestamp - localTime);
                if (timeDifference > 1000) {
                    System.out.println("Desync detected. Starting Berkeley sync.");
                    synchronizeClocks();
                }
                logData(clientAddress, data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SensorData readSensorData(DataInputStream inputStream) throws IOException {
        try {
            // Lê os campos do SensorData
            double temperature = inputStream.readDouble();
            double humidity = inputStream.readDouble();
            double co2 = inputStream.readDouble();
            String gpsLocation = readString(inputStream, 64);
            long timestamp = inputStream.readLong();
            String cropType = readString(inputStream, 32);

            return new SensorData(temperature, humidity, co2, gpsLocation, timestamp, cropType);
        } catch (EOFException e) {
            // Fim do stream
            return null;
        }
    }

    private String readString(DataInputStream inputStream, int maxLength) throws IOException {
        byte[] buffer = new byte[maxLength];
        inputStream.readFully(buffer);
        return new String(buffer).trim();
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
                         "Timestamp: " + formattedDate + ", " +
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

class SensorData {
    double temperature;
    double humidity;
    double co2;
    String gpsLocation;
    long timestamp;
    String cropType;

    public SensorData(double temperature, double humidity, double co2, String gpsLocation, long timestamp, String cropType) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.co2 = co2;
        this.gpsLocation = gpsLocation;
        this.timestamp = timestamp;
        this.cropType = cropType;
    }
}
