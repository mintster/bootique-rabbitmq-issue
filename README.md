## Bootique RabbitMQ ChannelFactory Issue 

A RabbitMQ Exchange with the same name of the Queue must exist to avoid throwing an exception with `channelFactory.openChannel()`.

[LINK TO ISSUE](https://github.com/bootique/bootique-rabbitmq-client/issues/1)

### Exception

With Queue Name `bqQueue` and Exchange Name `bqExchange` the following exception is thrown.

```
channel error; protocol method: #method<channel.close>
    (reply-code=404, reply-text=NOT_FOUND - no exchange 'bqQueue' in vhost '/', class-id=40, method-id=30)
```

If both Queue and Exchange were named `bqQueue` then the application runs without error.

### To View the Exception

1- Run the Application in your IDE to confirm the exception. `Launcher` class contains main. 

Or with Maven:

```
$ mvn clean install
$ java -jar target/bqrabbitmq.jar
```

If you look in your RabbitMQ Management Console, both `bqExchange` and `bqQueue` are created despite the exception.

### To Run without Exception

1- Change the Exchange name to `bqQueue` in the `bootique.yml` and Java Class `RabbitMqUI.`

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

Run application again and no Exception will occur

### To Reset the app and RabbitMq status to repeat exception

1- To view the exception again, remove RabbitMQ Exchange and Queue named `bqQueue`. This can be done in RabbitMQ Managment Console or at command line:

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

### Exception Details

This is the complete runtime exception display:

```
INFO  [2017-11-10 16:04:21,397] main i.b.r.c.c.ConnectionConfig: Creating RabbitMQ connection.
Exception in thread "main" java.lang.RuntimeException: java.io.IOException
	at io.bootique.rabbitmq.client.channel.ChannelFactory.openChannel(ChannelFactory.java:36)
	at com.nixmash.rabbitmq.ui.RabbitMqUI.init(RabbitMqUI.java:40)
	at com.nixmash.rabbitmq.Launcher.main(Launcher.java:25)
Caused by: java.io.IOException
	at com.rabbitmq.client.impl.AMQChannel.wrap(AMQChannel.java:105)
	at com.rabbitmq.client.impl.AMQChannel.wrap(AMQChannel.java:101)
	at com.rabbitmq.client.impl.AMQChannel.exnWrappingRpc(AMQChannel.java:123)
	at com.rabbitmq.client.impl.ChannelN.exchangeBind(ChannelN.java:873)
	at com.rabbitmq.client.impl.recovery.AutorecoveringChannel.exchangeBind(AutorecoveringChannel.java:297)
	at com.rabbitmq.client.impl.recovery.AutorecoveringChannel.exchangeBind(AutorecoveringChannel.java:292)
	at io.bootique.rabbitmq.client.channel.ChannelFactory.openChannel(ChannelFactory.java:33)
	... 2 more
Caused by: com.rabbitmq.client.ShutdownSignalException: channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no exchange 'bqQueue' in vhost '/', class-id=40, method-id=30)
	at com.rabbitmq.utility.ValueOrException.getValue(ValueOrException.java:66)
	at com.rabbitmq.utility.BlockingValueOrException.uninterruptibleGetValue(BlockingValueOrException.java:32)
	at com.rabbitmq.client.impl.AMQChannel$BlockingRpcContinuation.getReply(AMQChannel.java:366)
	at com.rabbitmq.client.impl.AMQChannel.privateRpc(AMQChannel.java:229)
	at com.rabbitmq.client.impl.AMQChannel.exnWrappingRpc(AMQChannel.java:117)
	... 6 more
Caused by: com.rabbitmq.client.ShutdownSignalException: channel error; protocol method: #method<channel.close>(reply-code=404, reply-text=NOT_FOUND - no exchange 'bqQueue' in vhost '/', class-id=40, method-id=30)
	at com.rabbitmq.client.impl.ChannelN.asyncShutdown(ChannelN.java:505)
	at com.rabbitmq.client.impl.ChannelN.processAsync(ChannelN.java:336)
	at com.rabbitmq.client.impl.AMQChannel.handleCompleteInboundCommand(AMQChannel.java:143)
	at com.rabbitmq.client.impl.AMQChannel.handleFrame(AMQChannel.java:90)
	at com.rabbitmq.client.impl.AMQConnection.readFrame(AMQConnection.java:634)
	at com.rabbitmq.client.impl.AMQConnection.access$300(AMQConnection.java:47)
	at com.rabbitmq.client.impl.AMQConnection$MainLoop.run(AMQConnection.java:572)
	at java.lang.Thread.run(Thread.java:745)
```


