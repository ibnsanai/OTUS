package ru.otus.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.NumbersRequest;
import ru.otus.NumbersServiceGrpc;

import java.util.concurrent.CountDownLatch;

public class NumbersClient {

    private static final Logger log = LoggerFactory.getLogger(NumbersClient.class);

    private final ManagedChannel channel;
    private final NumbersServiceGrpc.NumbersServiceStub asyncStub;

    // Атомарные переменные для хранения последнего значения от сервера
    // Используем volatile для видимости между потоками
    private volatile int lastServerValue = 0;
    private volatile boolean hasNewServerValue = false;

    public NumbersClient(String host, int port) {
        this.channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()  // Используем незащищенное соединение
                .build();
        this.asyncStub = NumbersServiceGrpc.newStub(channel);
    }

    public void start() throws InterruptedException {
        log.info("numbers Client is starting...");

        // Счетчик для синхронизации завершения стрима
        CountDownLatch finishLatch = new CountDownLatch(1);

        //генерировать числа от 0 до 30
        NumbersRequest request = NumbersRequest.newBuilder()
                .setFirstValue(0)
                .setLastValue(30)
                .build();

        // Создаем observer для получения стрима от сервера
        ClientStreamObserver responseObserver = new ClientStreamObserver(this);

        // Запускаем стрим
        asyncStub.generateNumbers(request, responseObserver);

        // Основной цикл клиента: от 0 до 50 с шагом в 1 секунду
        int currentValue = 0;

        for (int i = 0; i <= 50; i++) {
            // Проверяем, есть ли новое значение от сервера
            if (hasNewServerValue) {
                currentValue = currentValue + lastServerValue + 1;
                hasNewServerValue = false;  // Сбрасываем флаг
            } else {
                currentValue = currentValue + 1;
            }

            log.info("currentValue:{}", currentValue);

            // Ждем 1 секунду перед следующей итерацией
            Thread.sleep(1000);
        }

        shutdown();

    }

    public void setNewServerValue(int value) {
        this.lastServerValue = value;
        this.hasNewServerValue = true;
    }

    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    public static void main(String[] args) {
        NumbersClient client = new NumbersClient("localhost", 8080);
        try {
            client.start();
        } catch (InterruptedException e) {
            log.error("Client is interrupted", e);
            Thread.currentThread().interrupt();
        } finally {
            client.shutdown();
        }
    }

}
