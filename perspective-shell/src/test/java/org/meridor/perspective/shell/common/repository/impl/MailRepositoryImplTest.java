package org.meridor.perspective.shell.common.repository.impl;

import org.glassfish.tyrus.server.Server;
import org.glassfish.tyrus.test.tools.TestContainer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.meridor.perspective.common.events.EventBus;
import org.meridor.perspective.shell.common.events.MailReceivedEvent;
import org.meridor.perspective.shell.common.events.ShellStartedEvent;
import org.meridor.perspective.shell.common.repository.MailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.meridor.perspective.shell.common.repository.impl.MockMailEndpoint.LETTER;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;

@ContextConfiguration(locations = "/META-INF/spring/repository-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = AFTER_CLASS)
public class MailRepositoryImplTest extends TestContainer {

    @Rule
    public Timeout timeout = new Timeout(5, TimeUnit.SECONDS);

    @Autowired
    private MailRepository mailRepository;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private MockApiProvider apiProvider;

    @Before
    public void before() {
        setContextPath("/");
    }

    @Test
    public void testRepository() throws Exception {
        Server server1 = startServer(MockMailEndpoint.class);
        apiProvider.setBaseUri(String.format("http://localhost:%d/", server1.getPort()));
        CountDownLatch latch1 = new CountDownLatch(1);
        eventBus.addListener(MailReceivedEvent.class, e -> latch1.countDown());
        eventBus.fireAsync(new ShellStartedEvent());
        latch1.await();
        stopServer(server1);
        assertThat(mailRepository.getLetters(), contains(LETTER));
        mailRepository.deleteLetter(LETTER.getId());
        assertThat(mailRepository.getLetters(), is(empty()));

        CountDownLatch latch2 = new CountDownLatch(1);
        eventBus.addListener(MailReceivedEvent.class, e -> latch2.countDown());
        Server server2 = startServer(MockMailEndpoint.class);
        assertThat(server1.getPort(), equalTo(server2.getPort()));
        latch2.await();
        stopServer(server2);
        assertThat(mailRepository.getLetters(), contains(LETTER));
    }

}