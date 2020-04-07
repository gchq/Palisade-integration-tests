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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import uk.gov.gchq.palisade.service.policy.PolicyApplication;
import uk.gov.gchq.palisade.service.policy.web.ServiceInstanceRestController;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.gchq.palisade.integrationtests.policy.PolicyTestUtil.listTestServiceInstance;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {PolicyApplication.class})
@Import(PolicyTestConfiguration.class)
@WebMvcTest(ServiceInstanceRestController.class)
@AutoConfigureMockMvc
public class ServiceInstanceRestControllerWebTest {


    public static final String SERVICE_INSTANCES_URL = "/service-instances/{ApplicationName}";
    public static final String APPLICATION_NAME = "ApplicationName";


    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private DiscoveryClient discoveryClient;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

    }


    @Test
    public void shouldReturnServiceInstance() throws Exception {

        when(discoveryClient.getInstances(anyString())).thenReturn(listTestServiceInstance());

        MvcResult mvcResult = mockMvc.perform(get(SERVICE_INSTANCES_URL, APPLICATION_NAME)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andReturn();

        String jsonResponseString = mvcResult.getResponse().getContentAsString();
        List<ServiceInstance> responseList = Arrays.asList(mapper.readValue(jsonResponseString, PolicyTestUtil.TestServiceInstance[].class));
        assertTrue(responseList.size() == 2);
        PolicyTestUtil.TestServiceInstance firstInstance = (PolicyTestUtil.TestServiceInstance) responseList.get(0);
        PolicyTestUtil.TestServiceInstance secondInstance = (PolicyTestUtil.TestServiceInstance) responseList.get(1);
        assertTrue((firstInstance.getServiceId().equals("A Service") && secondInstance.getServiceId().equals("Another Service")) || (secondInstance.getServiceId().equals("A Service") && firstInstance.getServiceId().equals("Another Service")));

    }


}
