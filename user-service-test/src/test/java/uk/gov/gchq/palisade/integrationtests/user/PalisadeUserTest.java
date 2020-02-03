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
package uk.gov.gchq.palisade.integrationtests.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.service.user.UserApplication;
import uk.gov.gchq.palisade.service.user.request.AddUserRequest;
import uk.gov.gchq.palisade.service.user.request.GetUserRequest;
import uk.gov.gchq.palisade.service.user.service.UserService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;


// When registering data the Audit service must return 200 STATUS else test fails and return STATUS
@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class PalisadeUserTest {

    @Autowired
    private UserService userService;

    @Autowired
    private Map<String, UserService> serviceMap;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void contextLoads() {
        assertNotNull(serviceMap);
        assertNotEquals(serviceMap, Collections.emptyMap());
    }

    @Test
    public void isUp() {
        final String health = restTemplate.getForObject("/actuator/health", String.class);
        assertThat(health, is(equalTo("{\"status\":\"UP\"}")));
    }

    @Test
    public void postUserToUserService() {
        AddUserRequest addUserRequest = AddUserRequest.create(new RequestId().id("newRequest")).withUser(new User().userId("Johnny NewBoy"));
        Boolean response = restTemplate.postForObject("/addUser", addUserRequest, Boolean.class);
        assertThat(response, is(equalTo(true)));
    }

    @Test
    public void getCacheWarmedUser() {
        GetUserRequest getUserRequest = GetUserRequest.create(new RequestId().id("newUser")).withUserId(new UserId().id("Yuvon of the Yukon"));
        Map<String, GetUserRequest> params = new HashMap<>();
        params.put("request", getUserRequest);
        User result = restTemplate.getForObject("/getUser2/{request}", User.class, params);
        System.out.println(result);
    }

}
