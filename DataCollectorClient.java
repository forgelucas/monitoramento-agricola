import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class DataCollectorClient extends Thread {
    private String id;
    private long clockOffset;
    private int cycleTime;
    private String serverAddress;
    private int serverPort;
    private String cropType;
    private int maxRounds; // Número máximo de rodadas
    private int currentRound; // Contador de rodadas

    public DataCollectorClient(String id, String serverAddress, int serverPort, String cropType, int maxRounds) {
        this.id = id;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.cropType = cropType;
        this.clockOffset = randomOffset();
        this.cycleTime = 10000;
        this.maxRounds = maxRounds;
        this.currentRound = 0;
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
        return new SensorData(temperature, humidity, co2, gpsLocation, timestamp, cropType);
    }

    @Override
    public void run() {
        try {
            while (currentRound < maxRounds) {
                List<SensorData> batch = new ArrayList<>();
                int batchSize = 0;
                while (batchSize < 4096) {
                    SensorData data = generateData();
                    batch.add(data);
                    batchSize += data.toString().getBytes().length;
                }

                sendData(batch);
                currentRound++;
                System.out.println("Collector " + id + " completed round " + currentRound);
                Thread.sleep(cycleTime);
            }

            System.out.println("Collector " + id + " finished after " + maxRounds + " rounds.");
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
        String[] crops = {"Milho", "Trigo", "Soja", "Arroz"};
        int maxRounds = 200; // Limite de rodadas

        for (int i = 0; i < crops.length; i++) {
            DataCollectorClient collector = new DataCollectorClient("Collector-" + (i + 1), "localhost", 8080, crops[i], maxRounds);
            collector.start();
        }
    }
}
