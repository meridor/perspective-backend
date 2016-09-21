package org.meridor.perspective.shell.common.repository.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.shell.common.repository.SettingsRepository;
import org.meridor.perspective.shell.common.validator.Field;
import org.meridor.perspective.shell.common.validator.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/repository-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class SettingsRepositoryImplTest {

    @Autowired
    private SettingsRepository settingsRepository;
    
    @Test
    public void testSetUnsetSetting() {
        Set<String> setErrors = settingsRepository.set("api_url = http://example.com/");
        assertThat(setErrors, is(empty()));
        Map<String, String> settingsMap = settingsRepository.showSettings(false);
        assertThat(settingsMap.keySet(), contains("api_url"));
        assertThat(settingsMap.get("api_url"), equalTo("http://example.com/"));
        Set<String> unsetErrors = settingsRepository.unset("api_url");
        assertThat(unsetErrors, is(empty()));
        assertThat(settingsRepository.showSettings(false).keySet(), is(empty()));
    }
    
    @Test
    public void testSetUnsetFilter() {
        Set<String> setErrors = settingsRepository.set("projects = one,two");
        assertThat(setErrors, is(empty()));
        Map<String, String> settingsMap = settingsRepository.showFilters(false);
        assertThat(settingsMap.keySet(), contains("projects"));
        assertThat(settingsMap.get("projects"), equalTo("one, two"));
        Set<String> unsetErrors = settingsRepository.unset("projects");
        assertThat(unsetErrors, is(empty()));
        assertThat(settingsRepository.showFilters(false).keySet(), is(empty()));
    }
    
    @Test
    public void testSetMissingSetting() {
        assertThat(settingsRepository.set("missing_key = value"), hasSize(1));
    }
    
    @Test
    public void testSetNoValue() {
        assertThat(settingsRepository.set("page_size = "), hasSize(1));
    }
    
    @Test
    public void testUnsetMissingSetting() {
        assertThat(settingsRepository.unset("missing_key"), hasSize(1));
    }
    
    @Test
    public void testShowAllSettings() {
        Map<String, String> allSettings = settingsRepository.showSettings(true);
        assertThat(allSettings.keySet(), hasSize(Setting.values().length));
    }
    
    @Test
    public void testShowAllFilters() {
        Map<String, String> allFilters = settingsRepository.showFilters(true);
        assertThat(allFilters.keySet(), hasSize(Field.values().length));
    }
    
}