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

package uk.gov.gchq.palisade.integrationtests.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.service.user.UserApplication;
import uk.gov.gchq.palisade.service.user.request.AddUserRequest;
import uk.gov.gchq.palisade.service.user.request.GetUserRequest;
import uk.gov.gchq.palisade.service.user.web.UserController;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@ActiveProfiles("caffeine")
@SpringBootTest(classes = UserApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public class UserControllerWebTest {
    @Autowired
    private UserController controller;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void contextLoads() {
        assertNotNull(controller);
    }

    @Test
    public void isUp() {
        final String health = restTemplate.getForObject("/actuator/health", String.class);
        assertThat(health, is(equalTo("{\"status\":\"UP\"}")));
    }

    @Test
    public void addedUserIsRetrievable() {
        // Given
        User user = new User().userId("rest-added-user").addAuths(Collections.singleton("authorisation")).addRoles(Collections.singleton("role"));

        // When
        AddUserRequest addUserRequest = AddUserRequest.create(new RequestId().id("addUserRequest")).withUser(user);
        Boolean addUserResponse = restTemplate.postForObject("/addUser", addUserRequest, Boolean.class);
        // Then
        assertThat(addUserResponse, is(equalTo(true)));

        // When
        GetUserRequest getUserRequest = GetUserRequest.create(new RequestId().id("getUserRequest")).withUserId(user.getUserId());
        User getUserResponse = restTemplate.postForObject("/getUser", getUserRequest, User.class);
        // Then
        assertThat(getUserResponse, is(equalTo(user)));
    }
}
