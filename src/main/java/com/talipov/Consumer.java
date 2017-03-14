package com.talipov;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Created by Марсель on 14.03.2017.
 */
public class Consumer implements Runnable {
    public void run() {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://localhost");
            Connection conn = factory.createConnection();
            conn.start();

            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination dest = session.createQueue("Dest");

            MessageConsumer consumer = session.createConsumer(dest);
            Message msg = consumer.receive(10000);

            session.close();
            conn.close();

            System.out.println(msg.getJMSMessageID());
            System.out.println(((TextMessage) msg).getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
