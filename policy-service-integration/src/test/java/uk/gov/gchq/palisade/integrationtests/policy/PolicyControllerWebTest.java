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
package uk.gov.gchq.palisade.integrationtests.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import uk.gov.gchq.palisade.integrationtests.policy.config.PolicyTestConfiguration;
import uk.gov.gchq.palisade.service.policy.PolicyApplication;
import uk.gov.gchq.palisade.service.policy.request.CanAccessRequest;
import uk.gov.gchq.palisade.service.policy.request.GetPolicyRequest;
import uk.gov.gchq.palisade.service.policy.request.SetResourcePolicyRequest;
import uk.gov.gchq.palisade.service.policy.request.SetTypePolicyRequest;
import uk.gov.gchq.palisade.service.policy.service.PolicyService;
import uk.gov.gchq.palisade.service.policy.web.PolicyController;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.gchq.palisade.integrationtests.policy.PolicyTestUtil.mockContext;
import static uk.gov.gchq.palisade.integrationtests.policy.PolicyTestUtil.mockOriginalRequestId;
import static uk.gov.gchq.palisade.integrationtests.policy.PolicyTestUtil.mockPolicy;
import static uk.gov.gchq.palisade.integrationtests.policy.PolicyTestUtil.mockResource;
import static uk.gov.gchq.palisade.integrationtests.policy.PolicyTestUtil.mockResources;
import static uk.gov.gchq.palisade.integrationtests.policy.PolicyTestUtil.mockUser;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = PolicyApplication.class)
@Import(PolicyTestConfiguration.class)
@WebMvcTest(PolicyController.class)
@AutoConfigureMockMvc
public class PolicyControllerWebTest {

    public static final String CAN_ACCESS_REQUEST_URL = "/canAccess";
    public static final String GET_POLICY_SYNC_URL = "/getPolicySync";
    public static final String SET_RESOURCE_POLICY_ASYNC_URL = "/setResourcePolicyAsync";
    public static final String SET_TYPE_POLICY_ASYNC_URL = "/setTypePolicyAsync";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    @Qualifier("controller")
    private PolicyService policyService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * Tests the PolicyController for the service endpoint "/canAccess"
     * It tests that the service endpoint for the following:
     * 1) request URL is "/canAccess"
     * 2) request is a doPost process
     * 3) request data is in JSON format for a CanAccessRequest object
     * 4) response data is Json format
     * 5) response includes the text canAccessResources
     * 6) response status is 200 OK
     *
     * @throws Exception if the test fails
     */

    @Test
    public void shouldReturnCanAccess() throws Exception {
        CanAccessRequest canAccessRequest = (new CanAccessRequest())
                .context(mockContext())
                .user(mockUser())
                .resources(mockResources());
        canAccessRequest.originalRequestId(mockOriginalRequestId());

        this.mockMvc.perform(post(CAN_ACCESS_REQUEST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(mapper.writeValueAsString(canAccessRequest)))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string(containsString("canAccessResources")));
    }

    /**
     * Tests the PolicyController for the service endpoint "/getPolicySync"
     * It tests that the service endpoint for the following:
     * 1) request  URL is "/getPolicySync"
     * 2) request is a doPost process
     * 3) request data is in JSON format for a GetPolicyRequest object
     * 4) response data is Json format
     * 5) status is 200 OK
     *
     * @throws Exception if the test fails
     */
    @Test
    public void shouldReturnPolicySync() throws Exception {
        GetPolicyRequest getPolicyRequest = (new GetPolicyRequest())
                .context(mockContext())
                .user(mockUser())
                .resources(mockResources());
        getPolicyRequest.originalRequestId((mockOriginalRequestId()));
        String jsonGetPolicyRequestMessage = mapper.writeValueAsString(getPolicyRequest);

        this.mockMvc.perform(post(GET_POLICY_SYNC_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(jsonGetPolicyRequestMessage))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    /**
     * Tests the PolicyController for the service endpoint "/setResourcePolicyAsync"
     * It tests that the service endpoint for the following:
     * 1) request  URL is "/setResourcePolicyAsync"
     * 2) request is a doPut process
     * 3) request data is in JSON format for a SetResourcePolicyRequest object
     * 4) response status is 200 OK
     * @throws Exception if the test fails
     */
    @Test
    public void shouldSetResourcePolicyAsync() throws Exception {
        SetResourcePolicyRequest getSetResourcePolicyRequest = (new SetResourcePolicyRequest())
                .policy(mockPolicy())
                .resource(mockResource());
        String jsonSetResourcePolicyRequestMessage = mapper.writeValueAsString(getSetResourcePolicyRequest);

        this.mockMvc.perform(put(SET_RESOURCE_POLICY_ASYNC_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(jsonSetResourcePolicyRequestMessage))
                .andDo(print())
                .andExpect(status().isOk());
    }

    /**
     * Tests the PolicyController for the service endpoint "/setTypePolicyAsync"
     * It tests that the service endpoint for the following:
     * 1) request  URL is "/setTypePolicyAsync"
     * 2) request is a doPut process
     * 3) request data is in JSON format for a SetTypePolicyRequest object
     * 4) response status is 200 OK
     * @throws Exception if the test fails
     */
    @Test
    public void shouldSetTypePolicyAsync() throws Exception {
        SetTypePolicyRequest setTypePolicyRequest = (new SetTypePolicyRequest())
                .policy(mockPolicy())
                .type("Test type");
        String jsonSetTypePolicyAsyncMessage = mapper.writeValueAsString(setTypePolicyRequest);

        this.mockMvc.perform(put(SET_TYPE_POLICY_ASYNC_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(StandardCharsets.UTF_8.name())
                .content(jsonSetTypePolicyAsyncMessage))
                .andDo(print())
                .andExpect(status().isOk());
    }

}
