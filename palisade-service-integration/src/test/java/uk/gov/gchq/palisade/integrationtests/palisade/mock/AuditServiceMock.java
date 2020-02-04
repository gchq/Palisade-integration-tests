package uk.gov.gchq.palisade.integrationtests.palisade.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class AuditServiceMock {

    public static WireMockRule getRule() {
        return new WireMockRule(options().port(8081).notifier(new ConsoleNotifier(true)));
    }

    public static void stubRule(WireMockRule serviceMock, ObjectMapper serializer) throws JsonProcessingException {
        Boolean success = Boolean.TRUE;

        serviceMock.stubFor(post(urlEqualTo("/audit"))
            .willReturn(
                okJson(serializer.writeValueAsString(success))
            ));
    }
}
