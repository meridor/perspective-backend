package org.meridor.perspective.rest.resources;

import java.io.IOException;
import java.net.ServerSocket;

public class TestUtil {

    public static int getFreePort() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        return socket.getLocalPort();
    }

}
