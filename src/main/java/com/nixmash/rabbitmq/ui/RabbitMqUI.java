package com.nixmash.rabbitmq.ui;

import com.google.inject.Inject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import io.bootique.rabbitmq.client.channel.ChannelFactory;
import io.bootique.rabbitmq.client.connection.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMqUI implements IRabbitMqUI {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqUI.class);
    private static final String CONNECTION_NAME = "bqConnection";
    private static final String EXCHANGE_NAME = "bqExchange";
    private static final String QUEUE_NAME = "bqQueue";

    private ConnectionFactory connectionFactory;
    private ChannelFactory channelFactory;

    @Inject
    public RabbitMqUI(ConnectionFactory connectionFactory, ChannelFactory channelFactory) {
        this.connectionFactory = connectionFactory;
        this.channelFactory = channelFactory;
    }

    /*
    Queue and Exchange names must be the same or an IOException is thrown:
            -- channel error; protocol method: #method<channel.close>
            (reply-code=404, reply-text=NOT_FOUND - no exchange 'bqQueue' in vhost '/', class-id=40, method-id=30)
     */
    @Override
    public void init() throws IOException, TimeoutException {
        Connection connection = connectionFactory.forName(CONNECTION_NAME);

        // RabbitMQ Exchange with "bqQueue" must exist or IOException is thrown
        Channel channel = channelFactory.openChannel(connection, EXCHANGE_NAME, QUEUE_NAME, "hello");
        channel.queueDeclare();
        String message = "Hello World!";
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
        System.out.println(" [x] Sent '" + message + "'");
        channel.close();
        connection.close();
    }
}
