package test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.network.DiscoveryNetworkConnector;
import org.apache.activemq.network.NetworkConnector;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.net.URI;
import java.util.Arrays;

/**
 * Use two brokers on each side, and have the "broker federation" cross an unreliable network
 *
 */
public class TwoBrokersClient {

    private static BrokerService clientBroker;

    public static void main(String[] args) throws Exception {
        clientBroker = new BrokerService();
        clientBroker.addConnector("tcp://localhost:61615");
        clientBroker.addConnector("vm://localhost");
        NetworkConnector nc = new DiscoveryNetworkConnector(new URI("static:(tcp://192.168.1.9:61616)"));
        nc.setStaticallyIncludedDestinations(Arrays.<ActiveMQDestination>asList(new ActiveMQQueue("TEST.FOO")));
        clientBroker.addNetworkConnector(nc);
        clientBroker.setUseJmx(false);
        clientBroker.setPersistent(false);
        clientBroker.setAdvisorySupport(true);
        clientBroker.start();

        new Thread(new HelloWorldProducer()).start();
    }

    public static class HelloWorldProducer implements Runnable {
        public void run() {
            try {
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost"); // apparently the vm part is all i need
                Connection connection = connectionFactory.createConnection();
                connection.start();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Destination destination = session.createQueue("TEST.FOO");
                MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                while (true) {
                    String text = "Hello world! From: " + Thread.currentThread().getName() + " : " + this.hashCode();
                    TextMessage message = session.createTextMessage(text);
                    System.out.println("Sent message: " + message.hashCode() + " : " + Thread.currentThread().getName());
                    producer.send(message);
                    Thread.sleep(1000);
                }
//                session.close();
//                connection.close();
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }
    }
}