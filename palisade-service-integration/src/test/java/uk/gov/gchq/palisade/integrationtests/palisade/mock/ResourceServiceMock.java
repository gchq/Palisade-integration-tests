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

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class ResourceServiceMock {

    private static int port = 8084;

    public static WireMockRule getRule() {
        return new WireMockRule(options().port(8086).notifier(new ConsoleNotifier(true)));
    }

    public static void stubRule(final WireMockRule serviceMock) throws IOException {
        serviceMock.stubFor(post(urlMatching("/getResourcesBy(Id|Resource|Type|SerialisedFormat)"))
                .willReturn(aResponse().proxiedFrom("http://localhost:" + port + "/resource-service-mock")));
    }

}
