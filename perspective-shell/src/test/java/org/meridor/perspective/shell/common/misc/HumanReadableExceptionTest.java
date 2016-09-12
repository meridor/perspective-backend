package org.meridor.perspective.shell.common.misc;

import org.junit.Test;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class HumanReadableExceptionTest {

    @Test
    public void testConnectionError() {
        HumanReadableException exception = new HumanReadableException(new RuntimeException(new ConnectException()));
        assertThat(exception.toString().contains("connect"), is(true));
    }

    @Test
    public void testSocketTimeout() {
        HumanReadableException exception = new HumanReadableException(new SocketTimeoutException());
        assertThat(exception.toString().contains("connect"), is(true));
    }

    @Test

    public void testMessage() {
        HumanReadableException exception = new HumanReadableException("test-me");
        assertThat(exception.toString().contains("test"), is(true));
    }

}