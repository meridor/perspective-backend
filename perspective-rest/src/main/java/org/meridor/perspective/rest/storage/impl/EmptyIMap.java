package org.meridor.perspective.rest.storage.impl;

import com.hazelcast.core.EntryListener;
import com.hazelcast.core.EntryView;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.map.MapInterceptor;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.aggregation.Aggregation;
import com.hazelcast.mapreduce.aggregation.Supplier;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.query.Predicate;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class EmptyIMap<K, V> implements IMap<K, V> {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public V get(Object key) {
        return null;
    }

    @Override
    public V put(K key, V value) {
        return value;
    }

    @Override
    public V remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {

    }

    @Override
    public boolean remove(Object key, Object value) {
        return false;
    }

    @Override
    public void delete(Object key) {

    }

    @Override
    public void flush() {

    }

    @Override
    public Map<K, V> getAll(Set<K> keys) {
        return Collections.emptyMap();
    }

    @Override
    public void loadAll(boolean replaceExistingValues) {

    }

    @Override
    public void loadAll(Set<K> keys, boolean replaceExistingValues) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Future<V> getAsync(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<V> putAsync(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<V> putAsync(K key, V value, long ttl, TimeUnit timeunit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<V> removeAsync(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean tryRemove(K key, long timeout, TimeUnit timeunit) {
        return false;
    }

    @Override
    public boolean tryPut(K key, V value, long timeout, TimeUnit timeunit) {
        return false;
    }

    @Override
    public V put(K key, V value, long ttl, TimeUnit timeunit) {
        return value;
    }

    @Override
    public void putTransient(K key, V value, long ttl, TimeUnit timeunit) {

    }

    @Override
    public V putIfAbsent(K key, V value) {
        return value;
    }

    @Override
    public V putIfAbsent(K key, V value, long ttl, TimeUnit timeunit) {
        return value;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return false;
    }

    @Override
    public V replace(K key, V value) {
        return value;
    }

    @Override
    public void set(K key, V value) {

    }

    @Override
    public void set(K key, V value, long ttl, TimeUnit timeunit) {

    }

    @Override
    public void lock(K key) {

    }

    @Override
    public void lock(K key, long leaseTime, TimeUnit timeUnit) {

    }

    @Override
    public boolean isLocked(K key) {
        return false;
    }

    @Override
    public boolean tryLock(K key) {
        return false;
    }

    @Override
    public boolean tryLock(K key, long time, TimeUnit timeunit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock(K key) {

    }

    @Override
    public void forceUnlock(K key) {

    }

    @Override
    public String addLocalEntryListener(EntryListener<K, V> listener) {
        return getRandomUUID();
    }

    @Override
    public String addLocalEntryListener(EntryListener<K, V> listener, Predicate<K, V> predicate, boolean includeValue) {
        return null;
    }

    @Override
    public String addLocalEntryListener(EntryListener<K, V> listener, Predicate<K, V> predicate, K key, boolean includeValue) {
        return null;
    }

    @Override
    public String addInterceptor(MapInterceptor interceptor) {
        return getRandomUUID();
    }

    @Override
    public void removeInterceptor(String id) {

    }

    @Override
    public String addEntryListener(EntryListener<K, V> listener, boolean includeValue) {
        return getRandomUUID();
    }

    @Override
    public boolean removeEntryListener(String id) {
        return false;
    }

    @Override
    public String addEntryListener(EntryListener<K, V> listener, K key, boolean includeValue) {
        return getRandomUUID();
    }

    @Override
    public String addEntryListener(EntryListener<K, V> listener, Predicate<K, V> predicate, boolean includeValue) {
        return getRandomUUID();
    }

    @Override
    public String addEntryListener(EntryListener<K, V> listener, Predicate<K, V> predicate, K key, boolean includeValue) {
        return getRandomUUID();
    }

    @Override
    public EntryView<K, V> getEntryView(K key) {
        return null;
    }

    @Override
    public boolean evict(K key) {
        return false;
    }

    @Override
    public void evictAll() {

    }

    @Override
    public Set<K> keySet() {
        return Collections.emptySet();
    }

    @Override
    public Collection<V> values() {
        return Collections.emptySet();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return Collections.emptySet();
    }

    @Override
    public Set<K> keySet(Predicate predicate) {
        return Collections.emptySet();
    }

    @Override
    public Set<Entry<K, V>> entrySet(Predicate predicate) {
        return Collections.emptySet();
    }

    @Override
    public Collection<V> values(Predicate predicate) {
        return Collections.emptySet();
    }

    @Override
    public Set<K> localKeySet() {
        return Collections.emptySet();
    }

    @Override
    public Set<K> localKeySet(Predicate predicate) {
        return Collections.emptySet();
    }

    @Override
    public void addIndex(String attribute, boolean ordered) {

    }

    @Override
    public LocalMapStats getLocalMapStats() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object executeOnKey(K key, EntryProcessor entryProcessor) {
        return key;
    }

    @Override
    public Map<K, Object> executeOnKeys(Set<K> keys, EntryProcessor entryProcessor) {
        return Collections.emptyMap();
    }

    @Override
    public void submitToKey(K key, EntryProcessor entryProcessor, ExecutionCallback callback) {

    }

    @Override
    public Future submitToKey(K key, EntryProcessor entryProcessor) {
        return null;
    }

    @Override
    public Map<K, Object> executeOnEntries(EntryProcessor entryProcessor) {
        return Collections.emptyMap();
    }

    @Override
    public Map<K, Object> executeOnEntries(EntryProcessor entryProcessor, Predicate predicate) {
        return Collections.emptyMap();
    }

    @Override
    public <SuppliedValue, Result> Result aggregate(Supplier<K, V, SuppliedValue> supplier, Aggregation<K, SuppliedValue, Result> aggregation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <SuppliedValue, Result> Result aggregate(Supplier<K, V, SuppliedValue> supplier, Aggregation<K, SuppliedValue, Result> aggregation, JobTracker jobTracker) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getId() {
        return getRandomUUID();
    }

    @Override
    public String getPartitionKey() {
        return getRandomUUID();
    }

    @Override
    public String getName() {
        return getRandomUUID();
    }

    @Override
    public String getServiceName() {
        return getRandomUUID();
    }

    @Override
    public void destroy() {

    }
    
    private String getRandomUUID() {
        return UUID.randomUUID().toString();
    }
}
