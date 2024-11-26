import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class DataCollectorClient extends Thread {
    private String id;
    private long clockOffset; // Offset do relógio local em ms
    private int cycleTime; // Tempo de cada ciclo em ms
    private String serverAddress;
    private int serverPort;

    public DataCollectorClient(String id, String serverAddress, int serverPort) {
        this.id = id;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.clockOffset = randomOffset();
        this.cycleTime = 10000; // Valor fixo inicial de 10s
    }

    private long randomOffset() {
        Random rand = new Random();
        return (long) (rand.nextDouble() * 3000 - 1500); // ±1.5s
    }

    private SensorData generateData() {
        Random rand = new Random();
        double temperature = 20 + rand.nextDouble() * 10;
        double humidity = 30 + rand.nextDouble() * 50;
        double co2 = 400 + rand.nextDouble() * 100;
        String gpsLocation = "Lat: " + rand.nextDouble() + ", Lon: " + rand.nextDouble();
        long timestamp = System.currentTimeMillis() + clockOffset;
        return new SensorData(temperature, humidity, co2, gpsLocation, timestamp);
    }

    @Override
    public void run() {
        try {
            while (true) {
                List<SensorData> batch = new ArrayList<>();
                int batchSize = 0;
                while (batchSize < 4096) {
                    SensorData data = generateData();
                    batch.add(data);
                    batchSize += data.toString().getBytes().length;
                }

                sendData(batch);
                Thread.sleep(cycleTime); // Espera pelo próximo ciclo
            }
        } catch (InterruptedException | IOException e) {
            System.out.println("Collector " + id + " interrupted.");
        }
    }

    private void sendData(List<SensorData> dataBatch) throws IOException {
        try (Socket socket = new Socket(serverAddress, serverPort);
             ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream())) {
            outputStream.writeObject(dataBatch);
        }
    }

    public static void main(String[] args) {
        for (int i = 1; i <= 4; i++) {
            DataCollectorClient collector = new DataCollectorClient("Collector-" + i, "localhost", 8080);
            collector.start();
        }
    }
}
