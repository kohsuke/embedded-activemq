package test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Use two brokers on each side, and have the "broker federation" cross an unreliable network
 *
 */
public class TwoBrokersServer {

    private static BrokerService serverBroker;

    public static void main(String[] args) throws Exception {
        // start the server
        serverBroker = new BrokerService();
        serverBroker.addConnector("tcp://localhost:61616");
//        serverBroker.addConnector("vm://localhost");
        serverBroker.setUseJmx(false);
        serverBroker.setPersistent(false);
        serverBroker.start();

        new Thread(new HelloWorldConsumer()).start();
    }

    public static class HelloWorldConsumer implements Runnable {
        public void run() {
            try {
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
                Connection connection = connectionFactory.createConnection(); // exception happens here...
                connection.start();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Destination destination = session.createQueue("TEST.FOO");
                MessageConsumer consumer = session.createConsumer(destination);
                while (true) {
                    Message message = consumer.receive();
                    if (message instanceof TextMessage) {
                        TextMessage textMessage = (TextMessage) message;
                        String text = textMessage.getText();
                        System.out.println("*****Received: " + text);
                    } else {
                        System.out.println("*****Received obj: " + message);
                    }
                }
//                consumer.close();
//                session.close();
//                connection.close();
            } catch (Exception e) {
                System.out.println("Caught: " + e);
                e.printStackTrace();
            }
        }
    }
}