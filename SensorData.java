import java.io.Serializable;
import java.util.Random;

@SuppressWarnings("unused")
class SensorData implements Serializable {
    private double temperature;
    private double humidity;
    private double co2;
    private String gpsLocation;
    long timestamp;

    public SensorData(double temperature, double humidity, double co2, String gpsLocation, long timestamp) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.co2 = co2;
        this.gpsLocation = gpsLocation;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("Temperature: %.2f, Humidity: %.2f, CO2: %.2f, GPS: %s, Timestamp: %d",
                temperature, humidity, co2, gpsLocation, timestamp);
    }
}
