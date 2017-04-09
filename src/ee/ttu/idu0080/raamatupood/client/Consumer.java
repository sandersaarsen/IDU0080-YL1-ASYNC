package ee.ttu.idu0080.raamatupood.client;

import ee.ttu.idu0080.raamatupood.server.EmbeddedBroker;
import ee.ttu.idu0080.raamatupood.types.Tellimus;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import javax.jms.*;
import java.math.BigDecimal;

/**
 * JMS sõnumite tarbija. Ühendub broker-i urlile
 */
public class Consumer {
    private static final Logger log = Logger.getLogger(Consumer.class);
    private String EDASTAMINE = "tellimuse.edastamine";
    private String VASTUS = "tellimuse.vastus";
    private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
    private String url = EmbeddedBroker.URL;

    private long timeToLive = 1000000;

    public static void main(String[] args) {
        Consumer consumerTool = new Consumer();
        consumerTool.run();
    }

    public void run() {
        Connection connection = null;
        try {
            log.info("Connecting to URL: " + url);
            log.info("Consuming queue : " + EDASTAMINE);

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
            Destination destination = session.createQueue(EDASTAMINE);

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

                } else if (message instanceof ObjectMessage) {
                    ObjectMessage objectMessage = (ObjectMessage) message;
                    Tellimus msg = (Tellimus) objectMessage.getObject();
                    for (int i = 0; i < msg.getTellimuseRead().size(); i++) {
                        log.info("Received toode: " + msg.getTellimuseRead().get(i).getToode().getNimetus());
                        log.info("id:" + msg.getTellimuseRead().get(i).getToode().getKood());
                        log.info("hind:" + msg.getTellimuseRead().get(i).getToode().getHind());
                        log.info("kogus:" + msg.getTellimuseRead().get(i).getKogus());
                    }
                    sendAnswer(msg);

                } else {
                    log.info("Received: " + message);
                }

            } catch (JMSException e) {
                log.warn("Caught: " + e);
                e.printStackTrace();
            }
        }
    }

    protected void sendAnswer(Tellimus msg) {
        Connection connection = null;
        try {
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
            Destination destination = session.createQueue(VASTUS);

            // 3. Loome teadete saatja
            MessageProducer producer = session.createProducer(destination);

            // producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            producer.setTimeToLive(timeToLive);

            // 4. teadete saatmine
            createAnswer(session, producer, msg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void createAnswer(Session session, MessageProducer producer, Tellimus msg) throws Exception {

        ObjectMessage objectMessage = session.createObjectMessage();
        objectMessage.setObject(msg); // peab olema Serializable
        producer.send(objectMessage);

        TextMessage message = session.createTextMessage(createMessageText(msg));
        log.debug("Sending message: " + message.getText());
        producer.send(message);
    }

    private String createMessageText(Tellimus msg) {
        return "toodete arv kokku: " + getCount(msg) + " summa kokku: " + getSum(msg);
    }

    protected int getCount(Tellimus tellimus) {
        int count = 0;
        for (int i = 0; i < tellimus.getTellimuseRead().size(); i++) {
            count += tellimus.getTellimuseRead().get(i).getKogus();
        }
        return count;
    }

    protected Double getSum(Tellimus tellimus) {
        double sum = 0.0;
        for (int i = 0; i < tellimus.getTellimuseRead().size(); i++) {
            BigDecimal hind = tellimus.getTellimuseRead().get(i).getToode().getHind();
            BigDecimal kogus = new BigDecimal(tellimus.getTellimuseRead().get(i).getKogus());
            BigDecimal summa = hind.multiply(kogus);
            sum += summa.doubleValue();
        }
        return sum;
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

}