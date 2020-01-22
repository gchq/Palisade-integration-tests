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
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.service.palisade.policy.MultiPolicy;
import uk.gov.gchq.palisade.service.palisade.request.GetPolicyRequest;
import uk.gov.gchq.palisade.service.palisade.service.PolicyService;
import uk.gov.gchq.palisade.service.palisade.PalisadeApplication;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PalisadeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class PalisadePolicyTest extends BaseTestEnvironment {

    @Autowired
    private PolicyService policyService;

    @Autowired
    private ObjectMapper objectMapper;

    private WireMockRule policyServiceMock = POLICY_SERVICE_MOCK;

    private MultiPolicy multiPolicy = MULTI_POLICY;
    private GetPolicyRequest policyRequest = new GetPolicyRequest()
            .user(USER)
            .resources(Collections.singletonList(LEAF_RESOURCE))
            .context(CONTEXT);
    {
        policyRequest.originalRequestId(REQUEST_ID);
    }

    @Test
    public void policyServiceTest() throws Exception {
        policyServiceMock.stubFor(post(urlPathMatching("/getPolicy"))
                .withRequestBody(containing("user-id"))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(multiPolicy))
                ));

        MultiPolicy policies = this.policyService.getPolicy(policyRequest).get();
        assertThat(policies, equalTo(multiPolicy));

    }
}
