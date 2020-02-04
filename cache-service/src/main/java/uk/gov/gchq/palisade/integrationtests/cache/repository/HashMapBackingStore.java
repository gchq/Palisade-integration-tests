/*
 * Copyright 2019 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.palisade.integrationtests.cache.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.Util;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * A HashMapBackingStore is a simple implementation of a {@link BackingStore} that simply caches the objects in a
 * ConcurrentHashMap. By default the cache is static so it will be shared across the same JVM. This is designed to be
 * the simplest implementation of {@link BackingStore} suitable for use in examples only.
 */
public class HashMapBackingStore implements BackingStore {
    private static final Logger LOGGER = LoggerFactory.getLogger(HashMapBackingStore.class);

    /**
     * The static cache that will cause all instances of this across a JVM to be shared.
     */
    private static final ConcurrentHashMap<String, CachedPair> CACHE = new ConcurrentHashMap<>();

    /**
     * The static map that contains the removal handles.
     */
    private static final ConcurrentHashMap<String, ScheduledFuture<?>> REMOVAL_HANDLES = new ConcurrentHashMap<>();

    /**
     * The actual backing store for all cached data.
     */
    private final ConcurrentHashMap<String, CachedPair> cache;

    /**
     * The map of removal handles for time to live entries.
     */
    private final ConcurrentHashMap<String, ScheduledFuture<?>> removals;

    /**
     * Is the shared instance in use?
     */
    private final boolean useStatic;

    /**
     * Timer thread to remove cache entries after expiry.
     */
    private static final ScheduledExecutorService REMOVAL_TIMER = Executors.newSingleThreadScheduledExecutor(Util.createDaemonThreadFactory());

    /**
     * Create a {@link HashMapBackingStore} which uses the JVM wide shared object cache.
     */
    public HashMapBackingStore() {
        this(true);
    }

    /**
     * Create a store which may have its own store or may use the JVM shared instance.
     *
     * @param useStatic if true then use the JVM shared backing store
     */
    public HashMapBackingStore(final boolean useStatic) {
        if (useStatic) {
            cache = CACHE;
            removals = REMOVAL_HANDLES;
        } else {
            cache = new ConcurrentHashMap<>();
            removals = new ConcurrentHashMap<>();
        }
        this.useStatic = useStatic;
    }

    /**
     * Simple POJO for pairing together the object's class with the encoded form of the object.
     */
    private static class CachedPair {

        /**
         * Encoded form.
         */
        public final byte[] value;

        /**
         * Class of the value field.
         */
        public final Class<?> clazz;

        /**
         * Create a cache entry pair.
         *
         * @param value encoded object
         * @param clazz Java class object
         */
        CachedPair(final byte[] value, final Class<?> clazz) {
            this.value = value;
            this.clazz = clazz;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CachedPair)) {
                return false;
            }
            CachedPair that = (CachedPair) o;
            return Arrays.equals(value, that.value) &&
                    clazz.equals(that.clazz);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(clazz);
            result = 31 * result + Arrays.hashCode(value);
            return result;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", CachedPair.class.getSimpleName() + "[", "]")
                    .add("value=" + Arrays.toString(value))
                    .add("clazz=" + clazz)
                    .toString();
        }
    }

    public boolean getUseStatic() {
        return useStatic;
    }

    @Override
    public boolean add(final String key, final Class<?> valueClass, final byte[] value, final Optional<Duration> timeToLive) {
        String cacheKey = BackingStore.validateAddParameters(key, valueClass, value, timeToLive);
        LOGGER.debug("Adding to cache key {} of class {}", key, valueClass);
        cache.put(cacheKey, new CachedPair(value, valueClass));
        /*Here we set up a simple timer to deal with the removal of the item from the cache if a duration is present
         *This uses a single timer to remove elements, this is fine for this example, but in production we would want
         *something more performant.
         */
        //remove the old TTL handle if is there
        ScheduledFuture<?> oldHandle = removals.remove(cacheKey);
        //cancel the task
        if (nonNull(oldHandle)) {
            oldHandle.cancel(true);
        }

        timeToLive.ifPresent(duration -> {
            ScheduledFuture<?> removalHandle = REMOVAL_TIMER.schedule(() -> {
                cache.remove(cacheKey);
                removals.remove(cacheKey);
            }, duration.toMillis(), TimeUnit.MILLISECONDS);
            //store new handle
            removals.put(cacheKey, removalHandle);
        });
        return true;
    }

    @Override
    public SimpleCacheObject get(final String key) {
        String cacheKey = BackingStore.keyCheck(key);
        LOGGER.debug("Getting from cache: {}", cacheKey);
        final CachedPair result = cache.getOrDefault(cacheKey, new CachedPair(null, Object.class));
        return new SimpleCacheObject(result.clazz, Optional.ofNullable(result.value));
    }

    @Override
    public Stream<String> list(final String prefix) {
        requireNonNull(prefix, "prefix");
        return cache.keySet()
                .stream()
                .filter(x -> x.startsWith(
                        prefix)
                );
    }

    @Override
    public boolean remove(final String key) {
        String cacheKey = BackingStore.keyCheck(key);
        CachedPair result = cache.remove(cacheKey);
        boolean ret = (result != null);
        LOGGER.debug("Remove cache key {} result {}", cacheKey, ret);
        return ret;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HashMapBackingStore)) {
            return false;
        }
        HashMapBackingStore that = (HashMapBackingStore) o;
        return getUseStatic() == that.getUseStatic() &&
                cache.equals(that.cache) &&
                removals.equals(that.removals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cache, removals, getUseStatic());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", HashMapBackingStore.class.getSimpleName() + "[", "]")
                .add("cache=" + cache)
                .add("removals=" + removals)
                .add("useStatic=" + useStatic)
                .toString();
    }
}

