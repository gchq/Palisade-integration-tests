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

import org.springframework.cloud.client.ServiceInstance;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.rule.Rules;
import uk.gov.gchq.palisade.service.SimpleConnectionDetail;
import uk.gov.gchq.palisade.service.request.Policy;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * Set of methods and classes used in construction of integration tests in this package.
 */
public class PolicyTestUtil {

    static List<ServiceInstance> listTestServiceInstance(final String[] services) {
        List<ServiceInstance> listServiceInstance = new ArrayList<>();
        for (String service : services) {
            listServiceInstance.add(new TestServiceInstance(service));
        }
        return listServiceInstance;
    }

    static class TestServiceInstance implements ServiceInstance {

        private String serviceId = "Empty string";

        // Empty constructor for JSON instantiation
        TestServiceInstance() {
        }

        TestServiceInstance(final String serviceId) {
            this.serviceId = serviceId;
        }

        @Override
        public String getServiceId() {
            return serviceId;
        }

        public void setServiceId(final String serviceID) {
            this.serviceId = serviceID;
        }

        @Override
        public String getHost() {
            return null;
        }

        @Override
        public int getPort() {
            return 0;
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public URI getUri() {
            return null;
        }

        @Override
        public Map<String, String> getMetadata() {
            return null;
        }
    }

    static User mockUser() {
        return (new User())
                .userId("Alice")
                .roles("HR")
                .auths("private", "public");
    }

    static Context mockContext() {
        return (new Context())
                .contents(new HashMap<String, Object>()).put("purpose", "SALARY")
                .purpose("Testing");
    }

    static RequestId mockOriginalRequestId() {
        return (new RequestId()).id(UUID.randomUUID().toString());
    }

    static LeafResource mockResource() {
        return new FileResource()
                .id("TEST_RESOURCE_ID")
                .type("data type of the resource, e.g. Employee")
                .serialisedFormat("none")
                .connectionDetail(new SimpleConnectionDetail().serviceName("data-service-mock"))
                .parent((new DirectoryResource())
                        .id("resource")
                        .parent((new SystemResource())
                                .id("root")));
    }

    static Collection<LeafResource> mockResources() {
        List resources = new ArrayList<LeafResource>();
        resources.add(mockResource());
        return resources;
    }

    static Policy mockPolicy() {
        return new Policy()
                .owner(mockUser())
                .resourceRules(new Rules<>())
                .recordRules(new Rules<>());
    }
}
