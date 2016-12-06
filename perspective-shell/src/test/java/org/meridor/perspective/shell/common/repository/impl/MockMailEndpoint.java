package org.meridor.perspective.shell.common.repository.impl;

import org.meridor.perspective.backend.EntityGenerator;
import org.meridor.perspective.beans.Letter;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

import static org.meridor.perspective.api.SerializationUtils.serialize;

@ServerEndpoint("/mail")
public class MockMailEndpoint {

    static final Letter LETTER = EntityGenerator.getLetter();

    @OnOpen
    public void onOpen(Session session) {
        try {
            String serializedLetter = serialize(LETTER);
            for (Session s : session.getOpenSessions()) {
                s.getBasicRemote().sendText(serializedLetter);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}