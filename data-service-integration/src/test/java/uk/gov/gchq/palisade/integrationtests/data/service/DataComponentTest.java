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

package uk.gov.gchq.palisade.integrationtests.data.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import uk.gov.gchq.palisade.integrationtests.data.mock.AuditServiceMock;
import uk.gov.gchq.palisade.integrationtests.data.mock.PalisadeServiceMock;
import uk.gov.gchq.palisade.integrationtests.data.util.TestUtil;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.service.data.DataApplication;
import uk.gov.gchq.palisade.service.data.request.ReadRequest;
import uk.gov.gchq.palisade.service.data.web.DataController;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DataApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public class DataComponentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataComponentTest.class);

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private DataController dataController;

    @Rule
    public WireMockRule auditMock = AuditServiceMock.getRule();
    @Rule
    public WireMockRule palisadeMock = PalisadeServiceMock.getRule();

    private ObjectMapper serializer = JSONSerialiser.createDefaultMapper();

    @Before
    public void setUp() throws JsonProcessingException {
        AuditServiceMock.stubRule(auditMock, serializer);
        AuditServiceMock.stubHealthRule(auditMock, serializer);
        PalisadeServiceMock.stubRule(palisadeMock, serializer);
        PalisadeServiceMock.stubHealthRule(palisadeMock, serializer);
        serializer.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void contextLoads() {
        assertNotNull(dataController);
    }

    @Test
    public void isUp() {
        final String health = restTemplate.getForObject("/actuator/health", String.class);
        assertThat(health, is(equalTo("{\"status\":\"UP\"}")));
    }

    @Test
    public void readChunkedTest() {
        // Given all the services are mocked
        assertTrue(auditMock.isRunning());
        assertTrue(palisadeMock.isRunning());

        // Given


        // When
        Path currentPath = Paths.get("./resources/data/test_file.avro");
        FileResource resource = TestUtil.createFileResource(currentPath.toAbsolutePath().normalize(), "test");
        System.out.println(resource.toString());
        ReadRequest readRequest = new ReadRequest().token("token").resource(resource);
        ResponseEntity<StreamingResponseBody> streamResponse = restTemplate.postForObject("read/chunked", readRequest, ResponseEntity.class);

        // Then
        assertTrue(streamResponse.hasBody());
    }
}
