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

package uk.gov.gchq.palisade.integrationtests.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import feign.Response;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.data.serialise.AvroSerialiser;
import uk.gov.gchq.palisade.example.hrdatagenerator.types.Employee;
import uk.gov.gchq.palisade.integrationtests.data.config.DataTestConfiguration;
import uk.gov.gchq.palisade.integrationtests.data.mock.AuditServiceMock;
import uk.gov.gchq.palisade.integrationtests.data.mock.PalisadeServiceMock;
import uk.gov.gchq.palisade.integrationtests.data.web.DataClientWrapper;
import uk.gov.gchq.palisade.service.data.DataApplication;
import uk.gov.gchq.palisade.service.data.service.DataService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@EnableFeignClients
@RunWith(SpringRunner.class)
@Import(DataTestConfiguration.class)
@SpringBootTest(classes = DataApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public class DataComponentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataComponentTest.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private Map<String, DataService> serviceMap;
    @Autowired
    private DataClientWrapper client;

    @Rule
    public WireMockRule auditMock = AuditServiceMock.getRule();
    @Rule
    public WireMockRule palisadeMock = PalisadeServiceMock.getRule();

    private AvroSerialiser<Employee> avroSerialiser;

    @Before
    public void setUp() throws JsonProcessingException {
        AuditServiceMock.stubRule(auditMock, objectMapper);
        AuditServiceMock.stubHealthRule(auditMock, objectMapper);
        PalisadeServiceMock.stubRule(palisadeMock, objectMapper);
        PalisadeServiceMock.stubHealthRule(palisadeMock, objectMapper);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        avroSerialiser = new AvroSerialiser<>(Employee.class);
    }

    @Test
    public void contextLoads() {
        assertNotNull(serviceMap);
        assertNotEquals(serviceMap, Collections.emptyMap());
    }

    @Test
    public void isUp() {
        assertTrue(palisadeMock.isRunning());
        assertTrue(auditMock.isRunning());

        Response health = client.getHealth();
        assertThat(health.status(), equalTo(200));
    }

    @Test
    public void allServicesDown() {
        // Given audit and palisade services are down
        auditMock.stop();
        palisadeMock.stop();
        // Then the Data Service also reports down.
        final Response downHealth = client.getHealth();
        assertThat(downHealth.status(), equalTo(503));

        // When only the palisade-service is started
        palisadeMock.start();
        // Then Data service still shows as down
        final Response auditDownHealth = client.getHealth();
        assertThat(auditDownHealth.status(), equalTo(503));

        // When the audit-service is started as well
        auditMock.start();
        // Then Data service shows as up
        final Response allUpHealth = client.getHealth();
        assertThat(allUpHealth.status(), equalTo(200));
    }

    /*@Test
    public void readChunkedTest() throws Exception {
        // Given all the services are mocked
        assertTrue(auditMock.isRunning());
        assertTrue(palisadeMock.isRunning());

        // Given
        Path currentPath = Paths.get("./resources/data/employee_file0.avro").toAbsolutePath().normalize();
        FileResource resource = TestUtil.createFileResource(currentPath, "employee");
        ReadRequest readRequest = new ReadRequest().token("token").resource(resource);
        readRequest.setOriginalRequestId(new RequestId().id("original"));

        Stream<Employee> stream = Stream.of(DataServiceMock.testEmployee());

        byte[] fileBytes = Files.readAllBytes(currentPath);

        // When - using MockMvc
        MvcResult result = mockMvc.perform(post("/read/chunked")
                .accept(APPLICATION_OCTET_STREAM_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(readRequest)))
                .andExpect(request().asyncStarted())
                .andReturn();

        // Then
        mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(result))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect();

    }*/
}
