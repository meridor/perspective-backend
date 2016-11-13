package org.meridor.perspective.backend.config.impl;

import org.junit.Test;
import org.meridor.perspective.backend.config.SettingsProvider;

import java.util.Properties;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PropertiesSettingsProviderTest {
    
    private static final String EXISTING_KEY = "existing.key";
    private static final String MISSING_KEY = "missing.key";
    private static final String VALUE = "1,2,3";
    
    
    private static final Properties PROPERTIES = new Properties(){
        {
            setProperty(EXISTING_KEY, VALUE);
        }
    };
    private SettingsProvider settingsProvider = new PropertiesSettingsProvider(PROPERTIES);
    
    @Test
    public void testGet() throws Exception {
        assertThat(settingsProvider.get(MISSING_KEY).isPresent(), is(false));
        assertThat(settingsProvider.get(EXISTING_KEY).isPresent(), is(true));
        assertThat(settingsProvider.get(EXISTING_KEY).get(), equalTo(VALUE));
    }

    @Test
    public void testGetList() throws Exception {
        assertThat(settingsProvider.getList(MISSING_KEY), is(empty()));
        assertThat(settingsProvider.getList(EXISTING_KEY), hasSize(3));
        assertThat(settingsProvider.getList(EXISTING_KEY).get(1), equalTo("2"));
    }

    @Test
    public void testGetAs() throws Exception {
        assertThat(settingsProvider.getAs(MISSING_KEY, StringBuffer.class).isPresent(), is(false));
        assertThat(settingsProvider.getAs(EXISTING_KEY, Math.class).isPresent(), is(false)); //No constructor available
        assertThat(settingsProvider.getAs(EXISTING_KEY, StringBuffer.class).isPresent(), is(true));
        assertThat(settingsProvider.getAs(EXISTING_KEY, StringBuffer.class).get().toString(), equalTo(VALUE));
    }
    
}