package ru.otus.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.NumbersRequest;
import ru.otus.NumbersResponse;
import ru.otus.NumbersServiceGrpc;

import java.io.IOException;

public class NumbersServer {

    private static final Logger log = LoggerFactory.getLogger(NumbersServer.class);

    private final int port;
    private final Server server;

    public NumbersServer(int port) {
        this.port = port;
        this.server = ServerBuilder
                .forPort(port)
                .addService(new NumbersServiceImpl())
                .build();
    }

    public void start() throws IOException {
        server.start();
        log.info("Server start on port {}", port);

        //хук для корректного завершения
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Finish shutting down server...");
            NumbersServer.this.stop();
        }));
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        try {
            NumbersServer server = new NumbersServer(port);
            server.start();
            server.blockUntilShutdown();
        } catch (IOException | InterruptedException e) {
            log.error("Error when starting the server", e);
            Thread.currentThread().interrupt();
        }
    }

    private class NumbersServiceImpl extends NumbersServiceGrpc.NumbersServiceImplBase {

        private static final Logger log = LoggerFactory.getLogger(NumbersServiceImpl.class);

        @Override
        public void generateNumbers(NumbersRequest request,
                                    StreamObserver<NumbersResponse> responseObserver) {
            int firstValue = request.getFirstValue();
            int lastValue = request.getLastValue();

            log.info("Request: generating numbers from {} to {}", firstValue, lastValue);

            try {
                for (int i = firstValue + 1; i <= lastValue; i++) {
                    // Проверяем, не прервал ли клиент соединение
                    if (Thread.currentThread().isInterrupted()) {
                        log.info("Generation was interrupted by the client");
                        break;
                    }

                    // Отправляем число клиенту
                    NumbersResponse response = NumbersResponse.newBuilder()
                            .setValue(i)
                            .build();
                    responseObserver.onNext(response);
                    log.info("Generated number: {}", i);

                    // Имитируем задержку в 2 секунды между генерацией чисел
                    Thread.sleep(2000);
                }

                // Завершаем стрим
                responseObserver.onCompleted();
                log.info("Generation is completed");

                //Автоматическая остановка сервера, убрать, если нужно запустить несколько клиентов
                Thread.sleep(2000);
                NumbersServer.this.stop();
                System.exit(0);

            } catch (InterruptedException e) {
                log.warn("Generation interrupted");
                responseObserver.onError(e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Error when generating numbers", e);
                responseObserver.onError(e);
            }
        }
    }
}
