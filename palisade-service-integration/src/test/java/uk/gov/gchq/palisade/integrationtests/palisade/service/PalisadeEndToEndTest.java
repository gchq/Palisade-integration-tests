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

package uk.gov.gchq.palisade.integrationtests.palisade.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.service.palisade.PalisadeApplication;
import uk.gov.gchq.palisade.service.palisade.request.RegisterDataRequest;
import uk.gov.gchq.palisade.service.palisade.service.PalisadeService;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PalisadeApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public class PalisadeEndToEndTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    PalisadeService palisadeService;

    @Test
    public void contextLoads() {
        assertNotNull(palisadeService);
    }

    @Test
    public void isUp() {
        final String health = this.restTemplate.getForObject("/actuator/health", String.class);
        assertThat(health, is(equalTo("{\"status\":\"UP\"}")));
    }

    @Test
    public void endToEnd() {
        // Given all other services are mocked
        RegisterDataRequest request = new RegisterDataRequest().userId(new UserId().id("user-id")).resourceId("resource-id").context(new Context().purpose("purpose"));

        DataRequestResponse response = restTemplate.postForObject("/registerDataRequest", null, DataRequestResponse.class);

        assertThat(response, is(equalTo(true)));
    }
}
