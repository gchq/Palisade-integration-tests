package uk.gov.gchq.palisade.integrationtests.palisade.mock;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.request.DataRequestResponse;

import java.util.Collections;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class PalisadeServiceMock {

    @JsonPropertyOrder(value = {"class"}, alphabetic = true)
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "class")
    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    static class StubService implements Service {
        @Override
        public boolean equals(final Object obj) {
            return this == obj || obj != null && getClass() == obj.getClass();
        }
    }

    public static WireMockRule getRule() {
        return new WireMockRule(options().port(8084).notifier(new ConsoleNotifier(true)));
    }

    public static void stubRule(WireMockRule serviceMock, ObjectMapper serializer) throws JsonProcessingException {
        LeafResource resource = new FileResource().id("mock-file-resource").parent(new DirectoryResource().id("mock-directory").parent(new SystemResource().id("root")));
        ConnectionDetail connectionDetail = new SimpleConnectionDetail().service(new StubService());
        Map<LeafResource, ConnectionDetail> resources = Collections.singletonMap(resource, connectionDetail);
        DataRequestResponse response = new DataRequestResponse().token("mock-token").resources(resources);

        serviceMock.stubFor(post(urlEqualTo("/registerDataRequest"))
            .willReturn(
                okJson(serializer.writeValueAsString(response))
            ));
    }
}
