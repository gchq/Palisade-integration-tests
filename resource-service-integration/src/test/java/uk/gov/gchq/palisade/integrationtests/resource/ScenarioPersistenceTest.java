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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.integrationtests.resource.config.ResourceTestConfiguration;
import uk.gov.gchq.palisade.integrationtests.resource.web.ResourceClientWrapper;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.resource.ResourceApplication;
import uk.gov.gchq.palisade.service.resource.repository.JpaPersistenceLayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

@EnableFeignClients
@RunWith(SpringRunner.class)
@Import(ResourceTestConfiguration.class)
@SpringBootTest(classes = ResourceApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
@EnableJpaRepositories(basePackages = {"uk.gov.gchq.palisade.service.resource.repository"})
public class ScenarioPersistenceTest {

    @Autowired
    private JpaPersistenceLayer persistenceLayer;

    private static final Method getResourceById;
    static {
        try {
            getResourceById = JpaPersistenceLayer.class.getDeclaredMethod("getResourceById", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        getResourceById.setAccessible(true);
    }

    @Autowired
    private ResourceClientWrapper client;

    /**
     * Scenario as follows, where (F)iles, (D)irectories and (S)ystems are annotated respectively and numbered in query order
     *
     *       5 -> S
     *           / \
     *     3 -> D   D <- 4
     *         / \
     *   2 -> D   D
     *       / \   \
     * 1 -> F   F   F
     */
    private static final ConnectionDetail DETAIL = new SimpleConnectionDetail().uri("test-data-service");
    private static final String ROOT_PATH = "./src/test/resources/root";

    private static final SystemResource ROOT = new SystemResource().id(ROOT_PATH);

    private static final DirectoryResource TOP_LEVEL_DIR = new DirectoryResource().id(ROOT_PATH + "/top-level-dir").parent(ROOT);
    private static final DirectoryResource EMPTY_DIR = new DirectoryResource().id(ROOT_PATH + "/empty-dir").parent(ROOT);

    private static final DirectoryResource MULTI_FILE_DIR = new DirectoryResource().id(ROOT_PATH + "/top-level-dir/multi-file-dir").parent(TOP_LEVEL_DIR);
    private static final DirectoryResource SINGLE_FILE_DIR = new DirectoryResource().id(ROOT_PATH + "/top-level-dir/single-file-dir").parent(TOP_LEVEL_DIR);

    private static final FileResource MULTI_FILE_ONE = new FileResource().id(ROOT_PATH + "/top-level-dir/multi-file-dir/multiFileOne.txt").type("txt").serialisedFormat("txt").connectionDetail(DETAIL).parent(MULTI_FILE_DIR);
    private static final FileResource MULTI_FILE_TWO = new FileResource().id(ROOT_PATH + "/top-level-dir/multi-file-dir/multiFileTwo.txt").type("txt").serialisedFormat("txt").connectionDetail(DETAIL).parent(MULTI_FILE_DIR);

    private static final FileResource SINGLE_FILE = new FileResource().id(ROOT_PATH + "/top-level-dir/single-file-dir/singleFile.txt").type("txt").serialisedFormat("txt").connectionDetail(DETAIL).parent(SINGLE_FILE_DIR);

    @Test
    public void runThroughTestScenario() throws InvocationTargetException, IllegalAccessException {
        List<Resource> expectedPersisted;

        // When - Pt 1
        client.getResourcesByResource(MULTI_FILE_ONE).collect(Collectors.toSet());
        // Then
        expectedPersisted = Arrays.asList(MULTI_FILE_ONE);
        for (Resource resource: expectedPersisted) {
            Optional<Resource> expectedResource = extractResourceFromJpaPersistence(resource, persistenceLayer);
            assertTrue(resource.getId(), expectedResource.isPresent());
        }

        // When - Pt 2
        client.getResourcesByResource(MULTI_FILE_DIR).collect(Collectors.toSet());
        // Then
        expectedPersisted = Arrays.asList(MULTI_FILE_ONE, MULTI_FILE_TWO, MULTI_FILE_DIR);
        for (Resource resource: expectedPersisted) {
            Optional<Resource> expectedResource = extractResourceFromJpaPersistence(resource, persistenceLayer);
            assertTrue(resource.getId(), expectedResource.isPresent());
        }

        // When - Pt 3
        client.getResourcesByResource(TOP_LEVEL_DIR).collect(Collectors.toSet());
        // Then
        expectedPersisted = Arrays.asList(MULTI_FILE_ONE, MULTI_FILE_TWO, MULTI_FILE_DIR, SINGLE_FILE, SINGLE_FILE_DIR, TOP_LEVEL_DIR);
        for (Resource resource: expectedPersisted) {
            Optional<Resource> expectedResource = extractResourceFromJpaPersistence(resource, persistenceLayer);
            assertTrue(resource.getId(), expectedResource.isPresent());
        }

        // When - Pt 4
        client.getResourcesByResource(EMPTY_DIR).collect(Collectors.toSet());
        // Then
        expectedPersisted = Arrays.asList(MULTI_FILE_ONE, MULTI_FILE_TWO, MULTI_FILE_DIR, SINGLE_FILE, SINGLE_FILE_DIR, TOP_LEVEL_DIR, EMPTY_DIR);
        for (Resource resource: expectedPersisted) {
            Optional<Resource> expectedResource = extractResourceFromJpaPersistence(resource, persistenceLayer);
            assertTrue(resource.getId(), expectedResource.isPresent());
        }

        // When - Pt 5
        client.getResourcesByResource(ROOT).collect(Collectors.toSet());
        // Then
        expectedPersisted = Arrays.asList(MULTI_FILE_ONE, MULTI_FILE_TWO, MULTI_FILE_DIR, SINGLE_FILE, SINGLE_FILE_DIR, TOP_LEVEL_DIR, EMPTY_DIR, ROOT);
        for (Resource resource: expectedPersisted) {
            Optional<Resource> expectedResource = extractResourceFromJpaPersistence(resource, persistenceLayer);
            assertTrue(resource.getId(), expectedResource.isPresent());
        }
    }

    private Optional<Resource> extractResourceFromJpaPersistence(Resource resource, JpaPersistenceLayer persistenceLayer) throws InvocationTargetException, IllegalAccessException {
        return (Optional<Resource>) getResourceById.invoke(persistenceLayer, resource.getId());
    }

}
