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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.integrationtests.resource.config.ResourceTestConfiguration;
import uk.gov.gchq.palisade.integrationtests.resource.web.ResourceClientWrapper;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.resource.ResourceApplication;
import uk.gov.gchq.palisade.service.resource.service.StreamingResourceServiceProxy;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@EnableFeignClients
@RunWith(SpringRunner.class)
@Import(ResourceTestConfiguration.class)
@SpringBootTest(classes = ResourceApplication.class, webEnvironment = WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableJpaRepositories(basePackages = {"uk.gov.gchq.palisade.service.resource.repository"})
public class ResourcePersistenceTest {

    @Autowired
    private StreamingResourceServiceProxy persistenceProxy;

    @Autowired
    private ResourceClientWrapper client;

    private static final SystemResource SYSTEM_ROOT = new SystemResource().id("/");
    private static final DirectoryResource TEST_DIRECTORY = new DirectoryResource().id("/test").parent(SYSTEM_ROOT);
    private static final SimpleConnectionDetail DETAIL = new SimpleConnectionDetail().uri("test-data-service");
    private static final String EMPLOYEE_FORMAT = "employee";
    private static final String CLIENT_FORMAT = "client";
    private static final String AVRO_TYPE = "avro";
    private static final String CSV_TYPE = "csv";
    private static final FileResource EMPLOYEE_AVRO_FILE = new FileResource()
            .id("/test/employee.avro")
            .type(AVRO_TYPE)
            .serialisedFormat(EMPLOYEE_FORMAT)
            .connectionDetail(DETAIL)
            .parent(TEST_DIRECTORY);
    private static final FileResource EMPLOYEE_CSV_FILE = new FileResource()
            .id("/test/employee.csv")
            .type(CSV_TYPE)
            .serialisedFormat(EMPLOYEE_FORMAT)
            .connectionDetail(DETAIL)
            .parent(TEST_DIRECTORY);
    private static final FileResource CLIENT_AVRO_FILE = new FileResource()
            .id("/test/client.avro")
            .type(AVRO_TYPE)
            .serialisedFormat(CLIENT_FORMAT)
            .connectionDetail(DETAIL)
            .parent(TEST_DIRECTORY);

    @Before
    public void setup() {
        persistenceProxy.addResource(EMPLOYEE_AVRO_FILE);
        persistenceProxy.addResource(EMPLOYEE_CSV_FILE);
        persistenceProxy.addResource(CLIENT_AVRO_FILE);
    }

    @Test
    public void getTestResourceByResource() {
        // Given - setup

        // When
        Stream<LeafResource> resourcesByResource = client.getResourcesByResource(TEST_DIRECTORY);

        // Then
        Set<LeafResource> expected = new HashSet<>(Arrays.asList(EMPLOYEE_AVRO_FILE, EMPLOYEE_CSV_FILE, CLIENT_AVRO_FILE));
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
        Stream<LeafResource> resourcesByResource = client.getResourcesById(TEST_DIRECTORY.getId());

        // Then
        Set<LeafResource> expected = new HashSet<>(Arrays.asList(EMPLOYEE_AVRO_FILE, EMPLOYEE_CSV_FILE, CLIENT_AVRO_FILE));
        assertThat(resourcesByResource.collect(Collectors.toSet()), equalTo(expected));

        // When
        resourcesByResource = client.getResourcesById(EMPLOYEE_AVRO_FILE.getId());

        // Then
        expected = Collections.singleton(EMPLOYEE_AVRO_FILE);
        assertThat(resourcesByResource.collect(Collectors.toSet()), equalTo(expected));
    }

    @Test
    public void getTestResourceByType() {
        // Given - setup

        // When
        Stream<LeafResource> resourcesByType = client.getResourcesByType(AVRO_TYPE);

        // Then
        Set<LeafResource> expected = new HashSet<>(Arrays.asList(EMPLOYEE_AVRO_FILE, CLIENT_AVRO_FILE));
        assertThat(resourcesByType.collect(Collectors.toSet()), equalTo(expected));

        // When
        resourcesByType = client.getResourcesByType(CSV_TYPE);

        // Then
        expected = Collections.singleton(EMPLOYEE_CSV_FILE);
        assertThat(resourcesByType.collect(Collectors.toSet()), equalTo(expected));

    }

    @Test
    public void getTestResourceBySerialisedFormat() {
        // Given - setup

        // When
        Stream<LeafResource> resourcesByType = client.getResourcesByType(AVRO_TYPE);

        // Then
        Set<LeafResource> expected = new HashSet<>(Arrays.asList(EMPLOYEE_AVRO_FILE, CLIENT_AVRO_FILE));
        assertThat(resourcesByType.collect(Collectors.toSet()), equalTo(expected));

        // When
        resourcesByType = client.getResourcesByType(CSV_TYPE);

        // Then
        expected = Collections.singleton(EMPLOYEE_CSV_FILE);
        assertThat(resourcesByType.collect(Collectors.toSet()), equalTo(expected));

    }
}
