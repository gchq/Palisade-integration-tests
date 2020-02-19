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

package uk.gov.gchq.palisade.integrationtests.palisade.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;

import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class ResourceServiceMock {

    public static WireMockRule getRule() {
        return new WireMockRule(options().port(8086).notifier(new ConsoleNotifier(true)));
    }

    public static Map<LeafResource, ConnectionDetail> getResources() {
        LeafResource resource = new StubResource("type", "resource-id", "format");
        ConnectionDetail connectionDetail = new SimpleConnectionDetail().uri("data-service-mock");
        return Collections.singletonMap(resource, connectionDetail);
    }

    public static void stubRule(WireMockRule serviceMock, ObjectMapper serializer) throws JsonProcessingException {
        serviceMock.stubFor(post(urlMatching("/getResourcesBy(Id|Resource|Type|SerialisedFormat)"))
            .willReturn(
                okJson(serializer.writeValueAsString(getResources()))
            ));
    }

}