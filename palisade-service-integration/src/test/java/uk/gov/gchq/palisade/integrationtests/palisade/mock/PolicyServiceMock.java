package uk.gov.gchq.palisade.integrationtests.palisade.mock;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Autowired;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.rule.Rule;
import uk.gov.gchq.palisade.service.palisade.policy.MultiPolicy;
import uk.gov.gchq.palisade.service.palisade.policy.Policy;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
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

    @Autowired
    static ObjectMapper serializer;

    @ClassRule
    static WireMockRule serviceMock;

    static UserId userId = new UserId().id("user-id");
    static User user = new User().userId(userId);
    static Policy policy = new Policy<>().owner(user).resourceLevelRule("test rule", new StubRule<>());

    static Function<Set<LeafResource>, MultiPolicy> multiPolicyBuilder = resources -> {
        Map<LeafResource, Policy> policies = resources.stream().map(resource -> new SimpleEntry<>(resource, policy)).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
        return new MultiPolicy().policies(policies);
    };
    static Set<LeafResource> resources = Collections.singleton(new FileResource().id("test-resource"));

    static WireMockRule setUp() throws JsonProcessingException {
        serviceMock = new WireMockRule(options().port(8085).notifier(new ConsoleNotifier(true)));
        serviceMock.stubFor(WireMock.post(urlPathMatching("/getPolicy"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(serializer.writeValueAsString(multiPolicyBuilder.apply(resources)))
            ));
        return serviceMock;
    }
}
