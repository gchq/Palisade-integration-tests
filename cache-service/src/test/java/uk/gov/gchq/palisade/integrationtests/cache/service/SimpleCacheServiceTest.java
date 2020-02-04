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

package uk.gov.gchq.palisade.integrationtests.cache.service;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.gchq.palisade.integrationtests.cache.repository.BackingStore;
import uk.gov.gchq.palisade.integrationtests.cache.repository.HashMapBackingStore;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Ignore
@RunWith(JUnit4.class)
public class SimpleCacheServiceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCacheServiceTest.class);

    private SimpleCacheService cacheService = new SimpleCacheService();
    private BackingStore store = new HashMapBackingStore();
    private Duration maxLocalTTL = Duration.of(5, ChronoUnit.MINUTES);
}
