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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.service.policy.PolicyApplication;
import uk.gov.gchq.palisade.service.policy.web.PolicyController;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Import(PolicyTestConfiguration.class)
@SpringBootTest(classes = PolicyApplication.class)
public class PolicyControllerTest {

    @Autowired
    private PolicyController policyController;

    /* Smoke test,
     * 1) check to see that the application context is loading
     * 2) and that the Controller can be retrieved
     */
    @Test

    public void testContextLoads() throws Exception {
        assertThat(policyController).isNotNull();
    }


}
