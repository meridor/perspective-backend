package org.meridor.perspective.jdbc;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class UrlInfoTest {

    private static final UrlInfo VALID_URL_PARSER_WITH_USER = new UrlInfo("http://user:password@example.com:1234/");
    private static final UrlInfo VALID_URL_PARSER_WITHOUT_USER = new UrlInfo("http://example.com:1234/");
    private static final UrlInfo INVALID_URL_PARSER = new UrlInfo("anything://anything/");
    
    @Test
    public void testIsValid() throws Exception {
        assertThat(VALID_URL_PARSER_WITHOUT_USER.isValid(), is(true));
        assertThat(INVALID_URL_PARSER.isValid(), is(false));
    }

    @Test
    public void testGetHost() throws Exception {
        assertThat(VALID_URL_PARSER_WITHOUT_USER.getHost(), equalTo("example.com"));
    }

    @Test
    public void testGetPort() throws Exception {
        assertThat(VALID_URL_PARSER_WITHOUT_USER.getPort(), equalTo(1234));
    }
    
    @Test
    public void testGetUser() throws Exception {
        assertThat(VALID_URL_PARSER_WITH_USER.getUserName(), equalTo("user"));
        assertThat(VALID_URL_PARSER_WITHOUT_USER.getUserName(), is(nullValue()));
    }
    
    @Test
    public void testGetPassword() throws Exception {
        assertThat(VALID_URL_PARSER_WITH_USER.getPassword(), equalTo("password"));
        assertThat(VALID_URL_PARSER_WITHOUT_USER.getUserName(), is(nullValue()));
    }
    
    @Test
    public void testGetUrl() throws Exception {
        assertThat(VALID_URL_PARSER_WITH_USER.getUrl(), equalTo("http://user:password@example.com:1234/"));
        assertThat(VALID_URL_PARSER_WITHOUT_USER.getUrl(), equalTo("http://example.com:1234/"));
    }
}