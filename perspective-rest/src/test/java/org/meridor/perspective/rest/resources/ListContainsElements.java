package org.meridor.perspective.rest.resources;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ListContainsElements<T> extends TypeSafeMatcher<List<T>> {
    
    private final Predicate<T> predicate;

    public ListContainsElements(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    @Override
    protected boolean matchesSafely(List<T> instances) {
        return instances.stream().filter(predicate).collect(Collectors.toList()).size() > 0;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("matches given predicate");
    }
    
    public static <T> Matcher<List<T>> containsElements(Predicate<T> predicate) {
        return new ListContainsElements<T>(predicate);
    }
}
