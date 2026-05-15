package ru.otus.client;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.NumbersResponse;

public class ClientStreamObserver implements StreamObserver<NumbersResponse> {

    private static final Logger log = LoggerFactory.getLogger(ClientStreamObserver.class);

    private final NumbersClient client;

    public ClientStreamObserver(NumbersClient client) {
        this.client = client;
    }

    @Override
    public void onNext(NumbersResponse response) {
        int serverValue = response.getValue();
        log.info("new value: {}", serverValue);
        // Передаем значение клиенту
        client.setNewServerValue(serverValue);
    }

    @Override
    public void onError(Throwable t) {
        log.error("Error in stream for server", t);
    }

    @Override
    public void onCompleted() {
        log.info("request completed");
    }

}
