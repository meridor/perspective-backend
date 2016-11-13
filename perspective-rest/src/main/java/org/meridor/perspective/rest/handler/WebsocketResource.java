package org.meridor.perspective.rest.handler;

import io.undertow.websockets.core.WebSocketChannel;

public interface WebsocketResource {

    default void onMessage(String message, WebSocketChannel channel) {

    }

    default void onOpen(WebSocketChannel channel) {

    }

    default void onClose(WebSocketChannel channel) {

    }

    default void onError(WebSocketChannel channel, Throwable error) {

    }

}
