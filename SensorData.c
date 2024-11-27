#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Estrutura para armazenar os dados do sensor
typedef struct {
    double temperature;
    double humidity;
    double co2;
    char gpsLocation[100];
    long timestamp;
    char cropType[100]; // Campo para identificar o tipo de plantação
} SensorData;

// Função para inicializar um SensorData
SensorData* createSensorData(double temperature, double humidity, double co2, 
                             const char* gpsLocation, long timestamp, const char* cropType) {
    SensorData* data = (SensorData*)malloc(sizeof(SensorData));
    if (!data) {
        fprintf(stderr, "Erro ao alocar memória.\n");
        exit(EXIT_FAILURE);
    }
    data->temperature = temperature;
    data->humidity = humidity;
    data->co2 = co2;
    strncpy(data->gpsLocation, gpsLocation, sizeof(data->gpsLocation) - 1);
    data->gpsLocation[sizeof(data->gpsLocation) - 1] = '\0'; // Garantir terminação
    data->timestamp = timestamp;
    strncpy(data->cropType, cropType, sizeof(data->cropType) - 1);
    data->cropType[sizeof(data->cropType) - 1] = '\0'; // Garantir terminação
    return data;
}

// Função para exibir os dados de um SensorData
void printSensorData(const SensorData* data) {
    if (data) {
        printf("Temperature: %.2f, Humidity: %.2f, CO2: %.2f, GPS: %s, Timestamp: %ld, Crop: %s\n",
               data->temperature, data->humidity, data->co2, data->gpsLocation,
               data->timestamp, data->cropType);
    } else {
        printf("Dados inválidos.\n");
    }
}

// Função para liberar a memória alocada
void freeSensorData(SensorData* data) {
    free(data);
}
