#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <winsock2.h>
#include <windows.h>

#define CYCLE_TIME 10000 // Tempo de ciclo em milissegundos
#define BATCH_SIZE_LIMIT 4096 // Tamanho máximo do lote em bytes
#define MAX_ROUNDS 200 // Número máximo de rodadas

typedef struct {
    double temperature;
    double humidity;
    double co2;
    char gpsLocation[64];
    long long timestamp;
    char cropType[32];
} SensorData;

typedef struct {
    char id[32];
    long long clockOffset;
    char serverAddress[64];
    int serverPort;
    char cropType[32];
    int maxRounds;
    int currentRound;
} DataCollectorClient;

// Função para gerar um deslocamento de relógio aleatório
long long randomOffset() {
    return (rand() % 3000) - 1500;
}

// Função para gerar dados de sensores
SensorData generateData(DataCollectorClient* client) {
    SensorData data;
    data.temperature = 15.0 + ((double)rand() / RAND_MAX) * 25.0; // Entre 15 e 40 °C
    data.humidity = 20.0 + ((double)rand() / RAND_MAX) * 80.0;    // Entre 20 e 100%
    data.co2 = 350.0 + ((double)rand() / RAND_MAX) * 150.0;      // Entre 350 e 500 ppm
    snprintf(data.gpsLocation, sizeof(data.gpsLocation), "Lat: %.6f, Lon: %.6f",
             -90.0 + ((double)rand() / RAND_MAX) * 180.0, // Latitude entre -90 e 90
             -180.0 + ((double)rand() / RAND_MAX) * 360.0); // Longitude entre -180 e 180
    data.timestamp = time(NULL) * 1000 + client->clockOffset;
    strncpy(data.cropType, client->cropType, sizeof(data.cropType) - 1);
    data.cropType[sizeof(data.cropType) - 1] = '\0'; // Garantir terminação da string
    return data;
}

// Função para enviar dados via socket
void sendData(DataCollectorClient* client, SensorData* batch, int batchSize) {
    WSADATA wsa;
    SOCKET sock;
    struct sockaddr_in server;

    WSAStartup(MAKEWORD(2, 2), &wsa);
    sock = socket(AF_INET, SOCK_STREAM, 0);
    if (sock == INVALID_SOCKET) {
        printf("Socket creation failed\n");
        return;
    }

    server.sin_family = AF_INET;
    server.sin_addr.s_addr = inet_addr(client->serverAddress);
    server.sin_port = htons(client->serverPort);

    if (connect(sock, (struct sockaddr*)&server, sizeof(server)) < 0) {
        printf("Connection to server failed\n");
        closesocket(sock);
        return;
    }

    send(sock, (char*)batch, batchSize * sizeof(SensorData), 0);
    closesocket(sock);
    WSACleanup();
}

// Função para rodar o coletor de dados
DWORD WINAPI runCollector(LPVOID param) {
    DataCollectorClient* client = (DataCollectorClient*)param;

    while (client->currentRound < client->maxRounds) {
        SensorData batch[BATCH_SIZE_LIMIT];
        int batchSize = 0, dataCount = 0;

        while (batchSize < BATCH_SIZE_LIMIT) {
            SensorData data = generateData(client);
            batch[dataCount++] = data;
            batchSize += sizeof(SensorData);
        }

        sendData(client, batch, dataCount);
        client->currentRound++;
        printf("Collector %s completed round %d\n", client->id, client->currentRound);

        Sleep(CYCLE_TIME);
    }

    printf("Collector %s finished after %d rounds.\n", client->id, client->maxRounds);
    return 0;
}

int main() {
    const char* crops[] = {"Milho", "Trigo", "Soja", "Arroz"};
    int numCollectors = sizeof(crops) / sizeof(crops[0]);
    HANDLE threads[numCollectors];
    DataCollectorClient collectors[numCollectors];

    srand((unsigned int)time(NULL)); // Inicializa o gerador de números aleatórios

    for (int i = 0; i < numCollectors; i++) {
        snprintf(collectors[i].id, sizeof(collectors[i].id), "Collector-%d", i + 1);
        strcpy(collectors[i].serverAddress, "127.0.0.1");
        collectors[i].serverPort = 8080;
        strcpy(collectors[i].cropType, crops[i]);
        collectors[i].clockOffset = randomOffset();
        collectors[i].maxRounds = MAX_ROUNDS;
        collectors[i].currentRound = 0;

        threads[i] = CreateThread(NULL, 0, runCollector, &collectors[i], 0, NULL);
    }

    WaitForMultipleObjects(numCollectors, threads, TRUE, INFINITE);
    printf("All collectors finished.\n");

    return 0;
}
