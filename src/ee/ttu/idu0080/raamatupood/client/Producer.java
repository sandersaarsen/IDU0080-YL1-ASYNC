package ee.ttu.idu0080.raamatupood.client;

import ee.ttu.idu0080.raamatupood.server.EmbeddedBroker;
import ee.ttu.idu0080.raamatupood.types.Tellimus;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import javax.jms.*;

/**
 * JMS sõnumite tootja. Ühendub brokeri url-ile
 */
public class Producer {
    private static final Logger log = Logger.getLogger(Producer.class);
    public static final String EDASTAMINE = "tellimuse.edastamine"; // järjekorra
    // nimi
    public static final String VASTUS = "tellimuse.vastus";
    private String user = ActiveMQConnection.DEFAULT_USER;// brokeri jaoks vaja
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;

    long sleepTime = 1000; // 1000ms

    private int messageCount = 10;
    private long timeToLive = 1000000;
    private String url = EmbeddedBroker.URL;

    public static void main(String[] args) {
        Producer producerTool = new Producer();
        producerTool.run();
    }

    public void run() {
        Connection connection = null;
        try {
            log.info("Connecting to URL: " + url);
            log.debug("Sleeping between publish " + sleepTime + " ms");
            if (timeToLive != 0) {
                log.debug("Messages time to live " + timeToLive + " ms");
            }
            log.info("Consuming queue : " + EDASTAMINE);

            // 1. Loome ühenduse
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
            connection = connectionFactory.createConnection();
            // Käivitame yhenduse
            connection.start();

            // 2. Loome sessiooni
            /*
			 * createSession võtab 2 argumenti: 1. kas saame kasutada
			 * transaktsioone 2. automaatne kinnitamine
			 */
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Loome teadete sihtkoha (järjekorra). Parameetriks järjekorra nimi
            Destination destination = session.createQueue(EDASTAMINE);

            // 3. Loome teadete saatja
            MessageProducer producer = session.createProducer(destination);

            // producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.setTimeToLive(timeToLive);

            // 4. teadete saatmine
            sendTellimus(session, producer);

            getAnswer();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void sendTellimus(Session session, MessageProducer producer) throws Exception {

        // for (int i = 0; i < messageCount || messageCount == 0; i++) {
        ObjectMessage objectMessage = session.createObjectMessage();
        objectMessage.setObject(new Tellimus()); // peab olema Serializable
        producer.send(objectMessage);

        TextMessage message = session.createTextMessage(createMessageText("Tellimus"));
        log.debug("Sending message: " + message.getText());
        producer.send(message);

        // ootab 1 sekundi
        // Thread.sleep(sleepTime);
        // }
    }

    protected void getAnswer() {
        Connection connection = null;
        try {
            log.info("Connecting to URL: " + url);
            log.info("Consuming queue : " + VASTUS);

            // 1. Loome ühenduse
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user, password, url);
            connection = connectionFactory.createConnection();

            // Kui ühendus kaob, lõpetatakse Consumeri töö veateatega.
            connection.setExceptionListener(new ExceptionListenerImpl());

            // Käivitame ühenduse
            connection.start();

            // 2. Loome sessiooni
			/*
			 * createSession võtab 2 argumenti: 1. kas saame kasutada
			 * transaktsioone 2. automaatne kinnitamine
			 */
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            // Loome teadete sihtkoha (järjekorra). Parameetriks järjekorra nimi
            Destination destination = session.createQueue(VASTUS);

            // 3. Teadete vastuvõtja
            MessageConsumer consumer = session.createConsumer(destination);

            // Kui teade vastu võetakse käivitatakse onMessage()
            consumer.setMessageListener(new MessageListenerImpl());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Käivitatakse, kui tuleb sõnum
     */
    class MessageListenerImpl implements javax.jms.MessageListener {

        public void onMessage(Message message) {
            try {
                if (message instanceof TextMessage) {
                    TextMessage txtMsg = (TextMessage) message;
                    String msg = txtMsg.getText();
                    log.info("Received: " + msg);
                } else if (message instanceof ObjectMessage) {
                    ObjectMessage objectMessage = (ObjectMessage) message;
                    Tellimus msg = (Tellimus) objectMessage.getObject();

                } else {
                    log.info("Received: " + message);
                }

            } catch (JMSException e) {
                log.warn("Caught: " + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Käivitatakse, kui tuleb viga.
     */
    class ExceptionListenerImpl implements javax.jms.ExceptionListener {

        public synchronized void onException(JMSException ex) {
            log.error("JMS Exception occured. Shutting down client.");
            ex.printStackTrace();
        }
    }

    private String createMessageText(String string) {
        return string;
    }
}
