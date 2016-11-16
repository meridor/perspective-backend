package org.meridor.perspective.shell.interactive.wizard;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class ChoicesStorageTest {

    @Test
    public void testStorage() {
        final YesOrNo YES = new YesOrNo(true);
        final YesOrNo NO = new YesOrNo(false);

        ChoicesStorage<YesOrNo> choicesStorage = new ChoicesStorage<>(
                YesOrNo::toString,
                Arrays.asList(YES, NO)
        );

        assertThat(choicesStorage.getAnswersMap().keySet(), contains(1, 2));
        assertThat(choicesStorage.getAnswersMap().get(1), equalTo("yes"));
        assertThat(choicesStorage.getAnswersMap().get(2), equalTo("no"));

        assertThat(choicesStorage.getChoicesMap().keySet(), contains(1, 2));
        assertThat(choicesStorage.getChoicesMap().get(1), equalTo(YES));
        assertThat(choicesStorage.getChoicesMap().get(2), equalTo(NO));
    }

    private static class YesOrNo {
        private final boolean value;

        private YesOrNo(boolean value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value ? "yes" : "no";
        }

        @Override
        public boolean equals(Object another) {
            return another instanceof YesOrNo && another.toString().equals(toString());
        }
    }

}