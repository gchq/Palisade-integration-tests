package uk.gov.gchq.palisade.integrationtests.palisade.mock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.ClassRule;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class UserServiceMock {

    @ClassRule
    static WireMockRule serviceMock;

    static WireMockRule setUp() {
        serviceMock = new WireMockRule(options().port(8081).notifier(new ConsoleNotifier(true)));
        serviceMock.stubFor(WireMock.post(urlPathMatching("/audit"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("true")
            ));
        return serviceMock;
    }
}
