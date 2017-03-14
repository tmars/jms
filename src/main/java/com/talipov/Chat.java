package com.talipov;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Марсель on 14.03.2017.
 */
public class Chat implements MessageListener {
    private static final String CHAT_TOPIC = "chat";

    private Connection connection;
    private Session pubSession;
    private Session subSession;
    private MessageProducer producer;
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

            connection.start();
        } catch (JMSException e) {
            e.printStackTrace();
        }

        stdin = new BufferedReader(new InputStreamReader(System.in));

        while (username == null || username.equals("")) {
            try {
                System.out.println("Введите пожалуйста имя пользователя: ");
                username = stdin.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Ваше имя пользователя: " + username);
        inited = true;
    }

    public void work() {
        if (!inited) {
            System.out.println("Чат не инициализирован. Вызовите Chat.init()");
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

                TextMessage textMessage = pubSession.createTextMessage();
                textMessage.setText(username + ": " + msg);
                producer.send(textMessage);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    private void close() {
        try {
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void onMessage(Message message) {
        try {
            TextMessage msg = (TextMessage) message;
            System.out.println(" > " + msg.getText());
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
