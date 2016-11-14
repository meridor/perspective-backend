package org.meridor.perspective.shell.interactive.wizard;

import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class AnswersStorageTest {

    private static final String VALUE = "value";

    @Test
    public void testGetPutAndClear() {
        AnswersStorage answersStorage = new AnswersStorage();
        answersStorage.putAnswer(MockSingleChoiceStep.class, VALUE);
        assertThat(answersStorage.getAnswer(MockSingleChoiceStep.class), equalTo(VALUE));
        answersStorage.clear();
        assertThat(answersStorage.getAnswer(MockSingleChoiceStep.class), is(nullValue()));
    }

}