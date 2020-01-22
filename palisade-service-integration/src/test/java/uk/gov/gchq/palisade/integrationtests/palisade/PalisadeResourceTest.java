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

import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.request.GetResourcesByIdRequest;
import uk.gov.gchq.palisade.service.ConnectionDetail;
import uk.gov.gchq.palisade.service.palisade.service.ResourceService;
import uk.gov.gchq.palisade.service.palisade.PalisadeApplication;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PalisadeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class PalisadeResourceTest extends BaseTestEnvironment {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private ObjectMapper objectMapper;

    private WireMockRule resourceServiceMock = RESOURCE_SERVICE_MOCK;

    private LeafResource leafResource = LEAF_RESOURCE;
    private Map<LeafResource, ConnectionDetail> resourceInfo = RESOURCE_INFO;

    private GetResourcesByIdRequest getResourceRequest = new GetResourcesByIdRequest()
            .resourceId(LEAF_RESOURCE.getId());
    {
        getResourceRequest.originalRequestId(REQUEST_ID);
    }

    @Test
    public void resourceServiceTest() throws Exception {
        resourceServiceMock.stubFor(post(urlPathMatching("/getResourcesById"))
                .withRequestBody(containing(leafResource.getId()))
                .willReturn(
                        aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(objectMapper.writeValueAsString(resourceInfo))
                ));

        Map<LeafResource, ConnectionDetail> resource = this.resourceService.getResourcesById(getResourceRequest).get();
        assertThat(resource, equalTo(resourceInfo));
    }
}
