package app;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class ChatService {
    static final String FANOUT = "fanout";
    static final String EX_PREFIX = "bcast";
    Connection connection;
    Channel channel;
    Consumer consumer;
    String consumerTag;
    List<String> messageBuffer;

    public ChatService(String host) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        connection = factory.newConnection();
        channel = connection.createChannel();
        messageBuffer = new ArrayList<>();
    }


    public void nick(String nickname) throws IOException {
        if (consumerTag != null){
            channel.basicCancel(consumerTag);
        }
        channel.queueDeclare(getQueueName(nickname),false,false,false,null);
        channel.exchangeDeclare(getBroadcastName(nickname),FANOUT);
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                synchronized (this){
                    messageBuffer.add("["+envelope.getExchange()+"]"+ message);
                }
                System.out.println("["+envelope.getExchange()+"]"+ message);
            }
        };
        channel.basicConsume(getQueueName(nickname),consumer);
    }

    public void join(String nickname,String channelName) throws IOException {
        channel.exchangeDeclare(getChannelName(channelName),FANOUT);
        channel.exchangeBind(getChannelName(channelName),getBroadcastName(nickname),"");
        channel.queueBind(getQueueName(nickname),getChannelName(channelName),"");
    }

    public void leave(String nickname,String channelName) throws IOException {
        channel.exchangeUnbind(getChannelName(channelName),getBroadcastName(nickname),"");
        channel.queueUnbind(getQueueName(nickname),getChannelName(channelName),"");
    }

    public void send(String channelName,String nickname,String message) throws IOException {
        StringBuilder builder = new StringBuilder()
                .append("[")
                .append(nickname)
                .append("]")
                .append(" ").append(message);
        channel.basicPublish(getChannelName(channelName),"",null,builder.toString().getBytes());
    }

    public void broadcast(String nickname,String message) throws IOException {
        StringBuilder builder = new StringBuilder()
                .append("[")
                .append(nickname)
                .append("]")
                .append(" ").append(message);
        channel.basicPublish(getBroadcastName(nickname),"",null,builder.toString().getBytes());
    }

    String getBroadcastName(String nickname){
        return "broadcast_"+ nickname;
    }

    String getChannelName(String channelname){
        return "channel_" +channelname;
    }

    String getQueueName(String nickname){
        return "queue_" + nickname;
    }

    public Channel getChannel() {
        return channel;
    }

    public List<String> takeMessages(){
        List<String> ret;
        synchronized (this){
            ret = new ArrayList<>(messageBuffer);
            messageBuffer.clear();
        }
        return ret;
    }
}