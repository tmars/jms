package com.talipov;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Марсель on 14.03.2017.
 */
public class Chat implements MessageListener {
    private static final Logger logger = Logger.getLogger(Chat.class);
    private static final String CHAT_TOPIC = "chat";

    private Connection connection;
    private Session pubSession;
    private Session subSession;
    private Session replySession;
    private MessageProducer producer;
    private MessageProducer replyProducer;
    private MessageConsumer consumer;
    private BufferedReader stdin;
    private String username = "";
    private boolean inited;

    public void init() {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            connection = factory.createConnection();
            pubSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            subSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Topic topic = pubSession.createTopic(CHAT_TOPIC);
            consumer = subSession.createConsumer(topic);
            consumer.setMessageListener(this);
            producer = pubSession.createProducer(topic);
            producer.setDeliveryMode(DeliveryMode.PERSISTENT);

            replySession = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
            replyProducer = replySession.createProducer(null);
            replyProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

            connection.start();
        } catch (JMSException e) {
            logger.error("Ошибка инициализации", e);
            close();
            return;
        }

        stdin = new BufferedReader(new InputStreamReader(System.in));

        while (username == null || username.equals("")) {
            try {
                System.out.println("Введите пожалуйста имя пользователя: ");
                username = stdin.readLine();
            } catch (IOException e) {
                logger.error("Ошибка инициализации", e);
            }
        }
        System.out.println("Ваше имя пользователя: " + username);
        inited = true;
    }

    public void work() {
        if (!inited) {
            System.out.println("Чат не инициализирован.");
            return;
        }

        System.out.println("Можете ввести сообщение");
        while (true) {
            try {
                String msg = stdin.readLine();

                if (msg == null) {
                    close();
                    return;
                }

                sendMessage(msg);
            } catch (IOException e) {
                logger.error("Ошибка чтения данных", e);
            }
        }
    }

    private void sendMessage(String msg) {
        try {
            TextMessage textMessage = pubSession.createTextMessage();
            textMessage.setText(username + ": " + msg);

            Destination dest = subSession.createTemporaryQueue();
            MessageConsumer responseConsumer = subSession.createConsumer(dest);

            textMessage.setJMSReplyTo(dest);
            textMessage.setJMSCorrelationID("sd");

            producer.send(textMessage);
        } catch (JMSException e) {
            logger.error("Ошибка JMS", e);
        }
    }

    private void close() {
        try {
            if (connection != null) connection.close();
            if (pubSession != null) pubSession.close();
            if (subSession != null) subSession.close();
            if (consumer != null) consumer.close();
            if (producer != null) producer.close();
        } catch (JMSException e) {
            logger.error("Ошибка закрытия соединения", e);
        }
    }

    public void onMessage(Message message) {
        try {
            Message response = replySession.createMessage();
            response.setJMSCorrelationID(message.getJMSCorrelationID());
            replyProducer.send(message.getJMSReplyTo(), response);

            TextMessage msg = (TextMessage) message;
            System.out.println(" > " + msg.getText());
        } catch (JMSException e) {
            logger.error("Ошибка чтения данных", e);
        }
    }
}
