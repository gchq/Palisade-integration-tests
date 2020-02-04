package uk.gov.gchq.palisade.integrationtests.palisade.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class UserServiceMock {

    public static WireMockRule getRule() {
        return new WireMockRule(options().port(8087).notifier(new ConsoleNotifier(true)));
    }

    public static void stubRule(WireMockRule serviceMock, ObjectMapper serializer) throws JsonProcessingException {
        UserId userId = new UserId().id("user-id");
        User user = new User().userId(userId);

            serviceMock.stubFor(post(urlEqualTo("/getUser"))
                .willReturn(
                    okJson(serializer.writeValueAsString(user))
                ));
    }
}
