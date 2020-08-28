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

package uk.gov.gchq.palisade.integrationtests.attributemask;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import uk.gov.gchq.palisade.service.attributemask.AttributeMaskingApplication;
import uk.gov.gchq.palisade.service.attributemask.request.AttributeMaskingRequest;
import uk.gov.gchq.palisade.service.attributemask.request.AttributeMaskingResponse;
import uk.gov.gchq.palisade.service.attributemask.request.StreamMarker;
import uk.gov.gchq.palisade.service.attributemask.request.Token;
import uk.gov.gchq.palisade.service.attributemask.service.AttributeMaskingService;
import uk.gov.gchq.palisade.service.attributemask.web.AttributeMaskingController;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = AttributeMaskingApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
class AttributeMaskingServiceComponentTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private AttributeMaskingController controller;
    @Autowired
    private AttributeMaskingService service;

    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
        assertThat(service).isNotNull();
    }

    @Test
    void isUp() {
        final String health = restTemplate.getForObject("/actuator/health", String.class);

        assertThat(health).isEqualTo("{\"status\":\"UP\"}");
    }

    @Test
    void postToServiceReturnsMaskedResource() throws JsonProcessingException {
        // Given - request
        AttributeMaskingRequest request = AttributeMaskingApplicationTestData.REQUEST;
        HttpHeaders headers = new HttpHeaders();
        headers.add(Token.HEADER, AttributeMaskingApplicationTestData.REQUEST_TOKEN);

        // When - post to service
        HttpEntity<AttributeMaskingRequest> requestWithHeaders = new HttpEntity<>(request, headers);
        AttributeMaskingResponse response = restTemplate.postForObject("/stream-api/storeRequestResult", requestWithHeaders, AttributeMaskingResponse.class);

        // Then - response is as expected
        // LeafResource is 'masked' by the service
        assertThat(response.getResource())
                .isEqualTo(service.maskResourceAttributes(request.getResource()));
        // Everything else is the same
        assertThat(response.getUserId()).isEqualTo(request.getUserId());
        assertThat(response.getResourceId()).isEqualTo(request.getResourceId());
        assertThat(response.getContext()).isEqualTo(request.getContext());
    }

    @Test
    void streamMarkerIsSkippedByService() {
        // Given - request
        AttributeMaskingRequest request = null;
        HttpHeaders headers = new HttpHeaders();
        headers.add(Token.HEADER, AttributeMaskingApplicationTestData.REQUEST_TOKEN);
        headers.add(StreamMarker.HEADER, StreamMarker.START_OF_STREAM.toString());
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");

        // When - post to service
        HttpEntity<AttributeMaskingRequest> requestWithHeaders = new HttpEntity<>(request, headers);
        AttributeMaskingResponse response = restTemplate.postForObject("/stream-api/storeRequestResult", requestWithHeaders, AttributeMaskingResponse.class);

        // Then - response is as expected
        // LeafResource is 'masked' by the service
        assertThat(response).isNull();
    }

}
