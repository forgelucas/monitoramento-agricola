import java.io.Serializable;

class SensorData implements Serializable {
    private static final long serialVersionUID = 1L;
    double temperature;
    double humidity;
    double co2;
    String gpsLocation;
    long timestamp;
    String cropType; // Novo campo para identificar o tipo de plantação

    public SensorData(double temperature, double humidity, double co2, String gpsLocation, long timestamp, String cropType) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.co2 = co2;
        this.gpsLocation = gpsLocation;
        this.timestamp = timestamp;
        this.cropType = cropType;
    }

    @Override
    public String toString() {
        return String.format("Temperature: %.2f, Humidity: %.2f, CO2: %.2f, GPS: %s, Timestamp: %d, Crop: %s",
                temperature, humidity, co2, gpsLocation, timestamp, cropType);
    }
}
