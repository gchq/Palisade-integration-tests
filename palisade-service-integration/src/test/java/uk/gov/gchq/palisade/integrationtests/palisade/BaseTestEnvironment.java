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

package uk.gov.gchq.palisade.integrationtests.palisade;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.ClassRule;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.rule.Rule;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.palisade.policy.MultiPolicy;
import uk.gov.gchq.palisade.service.palisade.policy.Policy;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class BaseTestEnvironment {

    @JsonPropertyOrder(value = {"class"}, alphabetic = true)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "class")
    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    public static class StubService implements Service {

        @Override
        public boolean equals(final Object obj) {
            return this == obj || obj != null && getClass() == obj.getClass();
        }
    }

    @JsonPropertyOrder(value = {"class"}, alphabetic = true)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "class")
    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class StubRule implements Rule<String> {
        @Override
        public String apply(String s, User user, Context context) {
            return null;
        }
    }

    @ClassRule
    public static WireMockRule AUDIT_SERVICE_MOCK = new WireMockRule(options().port(8081).notifier(new ConsoleNotifier(true)));
    @ClassRule
    public static WireMockRule DATA_SERVICE_MOCK = new WireMockRule(options().port(8082).notifier(new ConsoleNotifier(true)));
    @ClassRule
    public static WireMockRule DISCOVERY_SERVICE_MOCK = new WireMockRule(options().port(8083).notifier(new ConsoleNotifier(true)));
    @ClassRule
    public static WireMockRule POLICY_SERVICE_MOCK = new WireMockRule(options().port(8085).notifier(new ConsoleNotifier(true)));
    @ClassRule
    public static WireMockRule RESOURCE_SERVICE_MOCK = new WireMockRule(options().port(8086).notifier(new ConsoleNotifier(true)));
    @ClassRule
    public static WireMockRule USER_SERVICE_MOCK = new WireMockRule(options().port(8087).notifier(new ConsoleNotifier(true)));

    RequestId REQUEST_ID = new RequestId().id("request-id");

    DirectoryResource DIRECTORY = new DirectoryResource().id("home");
    LeafResource LEAF_RESOURCE = new FileResource().id("data-to-access");
    {
        DIRECTORY.parent(new SystemResource().id("nix"));
        LEAF_RESOURCE.parent(DIRECTORY);
        LEAF_RESOURCE.setSerialisedFormat("json");
        LEAF_RESOURCE.setType("string");
    }
    SimpleConnectionDetail CONNECTION_DETAIL = new SimpleConnectionDetail().service(new StubService());
    Map<LeafResource, ConnectionDetail> RESOURCE_INFO = Stream.of(new AbstractMap.SimpleEntry<>(LEAF_RESOURCE, CONNECTION_DETAIL)).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

    UserId USER_ID = new UserId().id("user-id");
    User USER = new User().userId(USER_ID);
    Context CONTEXT = new Context().purpose("test-purpose");

    Policy POLICY = new Policy().owner(USER).resourceLevelRule("test rule", new StubRule());
    MultiPolicy MULTI_POLICY = new MultiPolicy().policies(Stream.of(new AbstractMap.SimpleEntry<>(LEAF_RESOURCE, POLICY)).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));

    DataRequestResponse DATA_RESPONSE = new DataRequestResponse().token("data-token").resources(RESOURCE_INFO);
    {
        DATA_RESPONSE.originalRequestId(REQUEST_ID);
    }
}
