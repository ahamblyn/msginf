package nz.co.pukeko.msginf.client.adapter;

import lombok.extern.slf4j.Slf4j;
import nz.co.pukeko.msginf.infrastructure.data.QueueStatisticsCollector;
import nz.co.pukeko.msginf.infrastructure.exception.MessageException;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestMessenger {

    private static Messenger messenger;

    @BeforeAll
    public static void setUp() {
        messenger = new Messenger();
    }

    @Test
    @Order(1)
    public void submit() throws MessageException {
        // submit so no response required - send 10 messages
        for (int i = 0; i < 10; i++) {
            Object submitReply = messenger.sendMessage("activemq", "activemq_submit_text", "Message[" + (i + 1) + "]");
            assertNull(submitReply);
        }
        log.info(QueueStatisticsCollector.getInstance().toString());
    }

    @Test
    @Order(2)
    public void receive() throws MessageException {
        List<String> messages = messenger.receiveMessages("activemq", "activemq_submit_text", 2000);
        assertNotNull(messages);
        assertEquals(10, messages.size());
        log.info(QueueStatisticsCollector.getInstance().toString());
    }


}
