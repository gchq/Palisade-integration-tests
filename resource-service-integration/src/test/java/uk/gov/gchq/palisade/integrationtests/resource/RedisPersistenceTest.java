/*
 * Copyright 2020 Crown Copyright
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

package uk.gov.gchq.palisade.integrationtests.resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import uk.gov.gchq.palisade.integrationtests.resource.config.RedisTestConfiguration;
import uk.gov.gchq.palisade.integrationtests.resource.config.ResourceTestConfiguration;
import uk.gov.gchq.palisade.integrationtests.resource.web.ResourceClientWrapper;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.resource.ResourceApplication;
import uk.gov.gchq.palisade.service.resource.domain.TypeEntity;
import uk.gov.gchq.palisade.service.resource.repository.JpaPersistenceLayer;
import uk.gov.gchq.palisade.service.resource.repository.ResourceRepository;
import uk.gov.gchq.palisade.service.resource.repository.SerialisedFormatRepository;
import uk.gov.gchq.palisade.service.resource.repository.TypeRepository;
import uk.gov.gchq.palisade.util.ResourceBuilder;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@EnableFeignClients
@Import({ResourceTestConfiguration.class, RedisTestConfiguration.class})
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ResourceApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
@EnableJpaRepositories(basePackages = {"uk.gov.gchq.palisade.service.resource.repository"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ActiveProfiles("redis")
public class RedisPersistenceTest {

    @Autowired
    private JpaPersistenceLayer persistenceLayer;

    @Autowired
    private ResourceClientWrapper client;

    /**
     * Scenario as follows, where (F)iles, (D)irectories and (S)ystems are annotated respectively
     *
     *     S
     *     |
     *     D
     *   / | \
     * F   F  F
     */

    private static final SimpleConnectionDetail DETAIL = new SimpleConnectionDetail().serviceName("data-service-mock");
    private static final String EMPLOYEE_TYPE = "employee";
    private static final String CLIENT_TYPE = "client";
    private static final String AVRO_FORMAT = "avro";
    private static final String JSON_FORMAT = "json";
    private static final SystemResource SYSTEM_ROOT = (SystemResource) ResourceBuilder.create("file:/");
    private static final DirectoryResource TEST_DIRECTORY = (DirectoryResource) ResourceBuilder.create("file:/test/");
    private static final FileResource EMPLOYEE_AVRO_FILE = ((FileResource) ResourceBuilder.create("file:/test/employee.avro"))
            .type(EMPLOYEE_TYPE)
            .serialisedFormat(AVRO_FORMAT)
            .connectionDetail(DETAIL);
    private static final FileResource EMPLOYEE_JSON_FILE = ((FileResource) ResourceBuilder.create("file:/test/employee.json"))
            .type(EMPLOYEE_TYPE)
            .serialisedFormat(JSON_FORMAT)
            .connectionDetail(DETAIL);
    private static final FileResource CLIENT_AVRO_FILE = ((FileResource) ResourceBuilder.create("file:/test/client.avro"))
            .type(CLIENT_TYPE)
            .serialisedFormat(AVRO_FORMAT)
            .connectionDetail(DETAIL);

    @Before
    @Transactional
    public void setup() {
        for (FileResource file: Arrays.asList(EMPLOYEE_JSON_FILE, EMPLOYEE_AVRO_FILE, CLIENT_AVRO_FILE)) {
            Stream<LeafResource> fileStream = Stream.of(file);
            fileStream = persistenceLayer.withPersistenceById(SYSTEM_ROOT.getId(), fileStream);
            fileStream = persistenceLayer.withPersistenceByType(file.getType(), fileStream);
            fileStream = persistenceLayer.withPersistenceBySerialisedFormat(file.getSerialisedFormat(), fileStream);
            fileStream.forEach(x -> { });
        }
    }

    @Test
    public void getTestResourceByResource() {
        // Given - setup

        // When
        Stream<LeafResource> resourcesByResource = client.getResourcesByResource(TEST_DIRECTORY);

        // Then
        Set<LeafResource> expected = new HashSet<>(Arrays.asList(EMPLOYEE_AVRO_FILE, EMPLOYEE_JSON_FILE, CLIENT_AVRO_FILE));
        assertThat(resourcesByResource.collect(Collectors.toSet()), equalTo(expected));

        // When
        resourcesByResource = client.getResourcesByResource(EMPLOYEE_AVRO_FILE);

        // Then
        expected = Collections.singleton(EMPLOYEE_AVRO_FILE);
        assertThat(resourcesByResource.collect(Collectors.toSet()), equalTo(expected));
    }

    @Test
    public void getTestResourceById() {
        // Given - setup

        // When
        Stream<LeafResource> resourcesById = client.getResourcesById(TEST_DIRECTORY.getId());

        // Then
        Set<LeafResource> expected = new HashSet<>(Arrays.asList(EMPLOYEE_AVRO_FILE, EMPLOYEE_JSON_FILE, CLIENT_AVRO_FILE));
        assertThat(resourcesById.collect(Collectors.toSet()), equalTo(expected));

        // When
        resourcesById = client.getResourcesById(EMPLOYEE_AVRO_FILE.getId());

        // Then
        expected = Collections.singleton(EMPLOYEE_AVRO_FILE);
        assertThat(resourcesById.collect(Collectors.toSet()), equalTo(expected));
    }

    @Test
    public void getTestResourceByType() {
        // Given - setup

        // When
        Stream<LeafResource> resourcesByType = client.getResourcesByType(EMPLOYEE_TYPE);

        // Then
        Set<LeafResource> expected = new HashSet<>(Arrays.asList(EMPLOYEE_AVRO_FILE, EMPLOYEE_JSON_FILE));
        assertThat(resourcesByType.collect(Collectors.toSet()), equalTo(expected));

        // When
        resourcesByType = client.getResourcesByType(CLIENT_TYPE);

        // Then
        expected = Collections.singleton(CLIENT_AVRO_FILE);
        assertThat(resourcesByType.collect(Collectors.toSet()), equalTo(expected));

    }

    @Test
    public void getTestResourceBySerialisedFormat() {
        // Given - setup

        // When
        Stream<LeafResource> resourcesBySerialisedFormat = client.getResourcesBySerialisedFormat(AVRO_FORMAT);

        // Then
        Set<LeafResource> expected = new HashSet<>(Arrays.asList(EMPLOYEE_AVRO_FILE, CLIENT_AVRO_FILE));
        assertThat(resourcesBySerialisedFormat.collect(Collectors.toSet()), equalTo(expected));

        // When
        resourcesBySerialisedFormat = client.getResourcesBySerialisedFormat(JSON_FORMAT);

        // Then
        expected = Collections.singleton(EMPLOYEE_JSON_FILE);
        assertThat(resourcesBySerialisedFormat.collect(Collectors.toSet()), equalTo(expected));

    }
}
