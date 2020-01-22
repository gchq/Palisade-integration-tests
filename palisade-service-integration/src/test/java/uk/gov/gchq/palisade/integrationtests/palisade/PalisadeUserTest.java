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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.service.palisade.request.GetUserRequest;
import uk.gov.gchq.palisade.service.palisade.service.UserService;
import uk.gov.gchq.palisade.service.palisade.PalisadeApplication;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PalisadeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class PalisadeUserTest extends BaseTestEnvironment {

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private WireMockRule userServiceMock = USER_SERVICE_MOCK;

    private GetUserRequest getUserRequest = new GetUserRequest().userId(USER_ID);
    {
        getUserRequest.originalRequestId(REQUEST_ID);
    }
    private User user = USER;

    @Test
    public void userServiceTest() throws IOException, ExecutionException, InterruptedException {
        userServiceMock.stubFor(post(urlPathMatching("/getUser"))
                .withRequestBody(containing("user-id"))
                .willReturn(
                        aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(user))
                ));

        final User subject = this.userService.getUser(getUserRequest).get();
        assertThat(subject.getUserId().getId(), is(equalTo("user-id")));
    }

}
