package com.talipov;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Created by Марсель on 14.03.2017.
 */
public class Producer implements Runnable {
    public void run() {
        try {
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory("vm://localhost");
            Connection conn = factory.createConnection();
            conn.start();

            Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination dest = session.createQueue("Dest");

            MessageProducer producer = session.createProducer(dest);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT); // иначе будет кеширование

            TextMessage msg = session.createTextMessage("test");
            producer.send(msg);

            session.close();
            conn.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
