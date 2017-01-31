package org.meridor.perspective.shell.common.format.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.meridor.perspective.shell.common.format.DataFormatterAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "/META-INF/spring/format-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class DataFormatterAwareImplTest {

    @Autowired
    private DataFormatterAware dataFormatterAware;

    @Autowired
    private ListFormatter listFormatter;

    @Autowired
    private EnumerationFormatter enumerationFormatter;

    @Autowired
    private BlockFormatter blockFormatter;

    @Test
    public void testGetDataFormatter() {
        assertThat(dataFormatterAware.getDataFormatter(null), equalTo(Optional.empty()));
        assertThat(dataFormatterAware.getDataFormatter("anything"), equalTo(Optional.empty()));
        assertThat(dataFormatterAware.getDataFormatter("anything\\X"), equalTo(Optional.empty()));
        assertThat(dataFormatterAware.getDataFormatter("anything\\E"), equalTo(Optional.of(enumerationFormatter)));
        assertThat(dataFormatterAware.getDataFormatter("anything\\L"), equalTo(Optional.of(listFormatter)));
        assertThat(dataFormatterAware.getDataFormatter("anything\\G"), equalTo(Optional.of(blockFormatter)));
    }

    @Test
    public void testRemoveDelimiter() {
        assertThat(dataFormatterAware.removeDelimiter(null), equalTo(""));
        assertThat(dataFormatterAware.removeDelimiter("anything"), equalTo("anything"));
        assertThat(dataFormatterAware.removeDelimiter("anything\\X"), equalTo("anything\\X"));
        assertThat(dataFormatterAware.removeDelimiter("anything\\E"), equalTo("anything"));
        assertThat(dataFormatterAware.removeDelimiter("\\Eanything"), equalTo("\\Eanything"));
        assertThat(dataFormatterAware.removeDelimiter("anything\\L"), equalTo("anything"));
        assertThat(dataFormatterAware.removeDelimiter("anything\\G"), equalTo("anything"));
    }

}