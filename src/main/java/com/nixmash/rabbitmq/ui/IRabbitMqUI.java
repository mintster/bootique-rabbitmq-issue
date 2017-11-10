package com.nixmash.rabbitmq.ui;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface IRabbitMqUI {
    void init() throws IOException, TimeoutException;
}
