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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class AuditServiceMock {

    public static WireMockRule getRule() {
        return new WireMockRule(options().port(8088).notifier(new ConsoleNotifier(true)));
    }

    public static Boolean getAuditResult() {
        return true;
    }

    public static void stubRule(final WireMockRule serviceMock, final ObjectMapper serializer) throws JsonProcessingException {
        serviceMock.stubFor(post(urlEqualTo("/audit"))
                .willReturn(
                        okJson(serializer.writeValueAsString(getAuditResult()))
                ));
    }

    public static void stubHealthRule(final WireMockRule serviceMock, final ObjectMapper serializer) throws JsonProcessingException {
        serviceMock.stubFor(get(urlEqualTo("/actuator/health"))
                .willReturn(
                        aResponse()
                ));
    }
}
