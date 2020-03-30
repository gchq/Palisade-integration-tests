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
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.integrationtests.data.mock.AuditServiceMock;
import uk.gov.gchq.palisade.integrationtests.data.mock.PalisadeServiceMock;
import uk.gov.gchq.palisade.jsonserialisation.JSONSerialiser;
import uk.gov.gchq.palisade.service.data.DataApplication;
import uk.gov.gchq.palisade.service.data.service.DataService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DataApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public class DataServiceComponentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataServiceComponentTest.class);

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private DataService dataService;

    @Rule
    public WireMockRule auditMock = AuditServiceMock.getRule();
    @Rule
    public WireMockRule palisadeMock = PalisadeServiceMock.getRule();

    private ObjectMapper serializer;

    @Before
    public void setUp() throws JsonProcessingException {
        serializer = JSONSerialiser.createDefaultMapper();
        AuditServiceMock.stubRule(auditMock, serializer);
        AuditServiceMock.stubHealthRule(auditMock, serializer);
        PalisadeServiceMock.stubRule(palisadeMock, serializer);
        PalisadeServiceMock.stubHealthRule(palisadeMock, serializer);
        serializer.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    public void contextLoads() {
        assertNotNull(dataService);
    }

    @Test
    public void isUp() {
        final String health = this.restTemplate.getForObject("/actuator/health", String.class);
        assertThat(health, is(equalTo("{\"status\":\"UP\"}")));
    }

    @Test
    public void allServicesDown() {
        // Given that all services are down
        auditMock.stop();
        palisadeMock.stop();
        // Then the Data Service also reports as down.
        final String downHealth = this.restTemplate.getForObject("/actuator/health", String.class);
        LOGGER.info(downHealth);
        assertThat(downHealth, is(equalTo("{\"status\":\"DOWN\"}")));

        // When services are started one by one
        auditMock.start();
        // Then Data Service still reports as down
        final String auditDownHealth = this.restTemplate.getForObject("/actuator/health", String.class);
        LOGGER.info(auditDownHealth);
        assertThat(auditDownHealth, is(equalTo("{\"status\":\"DOWN\"}")));

        // When services are started one by one
        palisadeMock.start();
        // The Data Service reports as up
        final String allUpHealth = this.restTemplate.getForObject("/actuator/health", String.class);
        LOGGER.info(allUpHealth);
        assertThat(allUpHealth, is(equalTo("{\"status\":\"UP\"}")));
    }

    @Test
    public void readChunkedTest() {
        // Given all the services are mocked
        assertTrue(auditMock.isRunning());
        assertTrue(palisadeMock.isRunning());

        // Given a data request has been registered

    }
}
