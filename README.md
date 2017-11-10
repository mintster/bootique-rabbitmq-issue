## Bootique RabbitMQ ChannelFactory Issue 

A temporary repository to support investigating issue using a Bootique RabbitMQ ChannelFactory issue where it appears that a RabbitMQ Exchange with the same name of the Queue must exist to use `channelFactory.openChannel()` to create a RabbitMQ Channel.

### The Issue

When using `channelFactory.openChannel()` to create a RabbitMQ Channel, a RabbitMQ Exchange with the same name as the Queue must be present or an IOException is thrown. With Queue Name `bqQueue` and Exchange Name `bqExchange` the following exception is thrown.

```
channel error; protocol method: #method<channel.close>
    (reply-code=404, reply-text=NOT_FOUND - no exchange 'bqQueue' in vhost '/', class-id=40, method-id=30)
```

### Duplicating the issue

1- Run the Application in your IDE to confirm the exception. Launcher class contains main. Or with Maven:

```
$ mvn clean install
$ java -jar target/bqrabbitmq.jar
```

2- If you look in your RabbitMQ Management Console, both `bqExchange` and `bqQueue` are created despite the exception.
3- Change the Exchange name to `bqQueue` in the `bootique.yml` and Java Class `RabbitMqUI.`

```yaml
  exchanges:
   bqExchange:  <--- Change to 'bqQueue'
     autoDelete: false
     durable: true
     internal: true
     type: TOPIC
  queues:
   bqQueue:
     autoDelete: true
     durable: true
     exclusive: false
```

In Java Class **RabbitMqUI**:

```java
public class RabbitMqUI implements IRabbitMqUI {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMqUI.class);
    private static final String CONNECTION_NAME = "bqConnection";
    private static final String EXCHANGE_NAME = "bqExchange";  // Change to bqQueue
    private static final String QUEUE_NAME = "bqQueue";

    private ConnectionFactory connectionFactory;
    private ChannelFactory channelFactory;
```

4- Run application again and no Exception will occur

### To Reset the app and RabbitMq status to repeat exception

1- Remove RabbitMQ Exchange and Queue named `bqQueue`. Can be done in RabbitMQ Managment Console or at command line:

```bash
$ rabbitmqadmin delete queue name=bqQueue
$ rabbitmqadmin delete exchange name=bqQueue
```

### Other Notes

1- The error occurs in `ChannelFactory.openChannel()` when binding the Exchange

```java
 channel.exchangeBind(queueName, exchangeName, routingKey);
```
2- It is quite possible I am missing something in the Bootique configuration and it's on me

