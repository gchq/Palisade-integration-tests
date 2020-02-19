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

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.rule.Rule;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.palisade.policy.MultiPolicy;
import uk.gov.gchq.palisade.service.palisade.policy.Policy;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class PolicyServiceMock {

    @JsonPropertyOrder(value = {"class"}, alphabetic = true)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "class")
    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class StubRule<T> implements Rule<T> {
        @Override
        public T apply(T data, User user, Context context) {
            return null;
        }
    }

    public static WireMockRule getRule() {
        return new WireMockRule(options().port(8085).notifier(new ConsoleNotifier(true)));
    }

    public static MultiPolicy getPolicies() {
        User user = UserServiceMock.getUser();
        Map<LeafResource, ConnectionDetail> resources = ResourceServiceMock.getResources();

        Policy policy = new Policy<>().owner(user).resourceLevelRule("test rule", new StubRule<>());
        Function<Set<LeafResource>, MultiPolicy> policyBuilder = resourceSet -> {
            Map<LeafResource, Policy> policies = resourceSet.stream().map(resource -> new SimpleEntry<>(resource, policy)).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
            return new MultiPolicy().policies(policies);
        };

        return policyBuilder.apply(resources.keySet());
    }

    public static void stubRule(WireMockRule serviceMock, ObjectMapper serializer) throws JsonProcessingException {
        serviceMock.stubFor(post(urlEqualTo("/getPolicySync"))
            .willReturn(
                okJson(serializer.writeValueAsString(getPolicies()))
            ));
    }
}