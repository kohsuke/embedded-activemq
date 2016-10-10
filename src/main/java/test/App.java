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
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) throws Exception {
        // start the server
        BrokerService broker = new BrokerService();
        broker.addConnector("tcp://localhost:61616");
        broker.setUseJmx(false);
        broker.setPersistent(false);
        broker.start();
        new Thread(new HelloWorldConsumer()).start();
        new Thread(new HelloWorldProducer()).start();
    }

    public static class HelloWorldProducer implements Runnable {
        public void run() {
            try {
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616"); // apparently the vm part is all i need
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