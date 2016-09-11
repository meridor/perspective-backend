package org.meridor.perspective.worker.fetcher.impl;

import org.junit.Test;
import org.meridor.perspective.config.CloudType;

import java.time.Instant;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.meridor.perspective.framework.storage.StorageEvent.ADDED;
import static org.meridor.perspective.framework.storage.StorageEvent.DELETED;

public class LastModificationListenerTest {

    private static final String CLOUD_ID = CloudType.MOCK.value();
    
    private static final String ENTITY_ID = "test-id";
    
    @Test
    public void testPropagate() throws Exception {
        LastModificationListener<ModifiableObject> listener = new MockLastModificationListener();
        ModifiableObject object = new ModifiableObject();
        listener.onEvent(object, null, ADDED);
        assertThat(listener.getIds(CLOUD_ID, LastModified.NOW), contains(ENTITY_ID));
        Thread.sleep(300);
        listener.onEvent(object, null, ADDED);
        assertThat(listener.getIds(CLOUD_ID, LastModified.MOMENTS_AGO), contains(ENTITY_ID));
        Thread.sleep(300);
        listener.onEvent(object, null, ADDED);
        assertThat(listener.getIds(CLOUD_ID, LastModified.SOME_TIME_AGO), contains(ENTITY_ID));
        Thread.sleep(500);
        listener.onEvent(object, null, ADDED);
        assertThat(listener.getIds(CLOUD_ID, LastModified.LONG_AGO), contains(ENTITY_ID));
    }
    
    @Test
    public void testAddAndDelete() {
        LastModificationListener<ModifiableObject> listener = new MockLastModificationListener();
        ModifiableObject object = new ModifiableObject();
        listener.onEvent(object, null, ADDED);
        Set<String> idsFirst = listener.getIds(CLOUD_ID, LastModified.NOW);
        Set<String> idsSecond = listener.getIds(CLOUD_ID, LastModified.NOW);
        assertThat(idsFirst, contains(ENTITY_ID));
        assertThat(idsFirst, equalTo(idsSecond));
        //Should return a new set each time otherwise we have concurrency issues
        assertThat(idsFirst != idsSecond, is(true));
        listener.onEvent(object, null, DELETED);
        assertThat(listener.getIds(CLOUD_ID, LastModified.NOW), not(contains(ENTITY_ID)));
    }
    
    private static class MockLastModificationListener extends LastModificationListener<ModifiableObject> {

        @Override
        protected int getLongTimeAgoLimit() {
            return 1000;
        }

        @Override
        protected String getId(ModifiableObject entity) {
            return entity.getId();
        }

        @Override
        protected String getCloudId(ModifiableObject entity) {
            return CLOUD_ID;
        }

        @Override
        protected Instant getLastModifiedInstant(ModifiableObject entity) {
            return entity.getLastModified();
        }
    }
    
    private static class ModifiableObject {
        
        private Instant lastModified = Instant.now();
        
        String getId() {
            return ENTITY_ID;
        }

        Instant getLastModified() {
            return lastModified;
        }
    }
    
}