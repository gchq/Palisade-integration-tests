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
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.rule.Rule;
import uk.gov.gchq.palisade.service.palisade.policy.MultiPolicy;
import uk.gov.gchq.palisade.service.palisade.policy.Policy;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
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

    public static void stubRule(WireMockRule serviceMock, ObjectMapper serializer) throws JsonProcessingException {
        UserId userId = new UserId().id("user-id");
        User user = new User().userId(userId);
        Policy policy = new Policy<>().owner(user).resourceLevelRule("test rule", new StubRule<>());
        Function<Set<LeafResource>, MultiPolicy> policyBuilder = resources -> {
            Map<LeafResource, Policy> policies = resources.stream().map(resource -> new SimpleEntry<>(resource, policy)).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
            return new MultiPolicy().policies(policies);
        };
        Set<LeafResource> resources = Collections.singleton(new StubResource("type", "resource-id", "format"));

        serviceMock.stubFor(post(urlEqualTo("/getPolicySync"))
            .willReturn(
                okJson(serializer.writeValueAsString(policyBuilder.apply(resources)))
            ));
    }
}
