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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;

import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.service.user.UserApplication;
import uk.gov.gchq.palisade.service.user.exception.NoSuchUserIdException;
import uk.gov.gchq.palisade.service.user.request.AddUserRequest;
import uk.gov.gchq.palisade.service.user.request.GetUserRequest;
import uk.gov.gchq.palisade.service.user.service.CachedUserService;
import uk.gov.gchq.palisade.service.user.service.UserService;

import java.util.Collections;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;


// When registering data the Audit service must return 200 STATUS else test fails and return STATUS
@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class UserCachingTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void contextLoads() {
        assertNotNull(userService);
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

    @Test(expected = RestClientException.class)
    public void nonExistentUserRetrieveFails() {
        // Given
        UserId userId = new UserId().id("definitely-not-a-real-user");

        // When
        GetUserRequest getUserRequest = GetUserRequest.create(new RequestId().id("getUserRequest")).withUserId(userId);
        restTemplate.postForObject("/getUser", getUserRequest, User.class);
        // Then - throw
    }

    @Test
    public void updateUserTest() {
        // Given
        User user = new User().userId("rest-added-TTL-user").addAuths(Collections.singleton("authorisation")).addRoles(Collections.singleton("role"));
        User user2 = new User().userId("rest-added-TTL-user").addAuths(Collections.singleton("newAuth")).addRoles(Collections.singleton("newRole"));

        // When
        AddUserRequest addUserRequest = AddUserRequest.create(new RequestId().id("addUserRequest")).withUser(user);
        AddUserRequest addUserRequest2 = AddUserRequest.create(new RequestId().id("addUserRequest")).withUser(user2);
        Boolean addUserResponse = restTemplate.postForObject("/addUser", addUserRequest, Boolean.class);
        Boolean addUserResponse2 = restTemplate.postForObject("/addUser", addUserRequest2, Boolean.class);

        // Then
        assertThat(addUserResponse, is(equalTo(true)));
        assertThat(addUserResponse2, is(equalTo(true)));

        // And When
        GetUserRequest getUserRequest = GetUserRequest.create(new RequestId().id("getUserRequest")).withUserId(user.getUserId());
        User getTTLResponse = restTemplate.postForObject("/getUser", getUserRequest, User.class);

        // Then
        assertThat(getTTLResponse, is(equalTo(user2)));
    }

    @Test(expected = NoSuchUserIdException.class)
    public void maxSizeTest() {
        assumeTrue(userService instanceof CachedUserService);
        Function<Integer, User> makeUser = i -> new User().userId(new UserId().id(i.toString()));
        for (int count = 0; count <= 100; ++count) {
            userService.addUser(makeUser.apply(count));
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        userService.getUser(makeUser.apply(0).getUserId());
    }

    @Test(expected = NoSuchUserIdException.class)
    public void ttlTest() {
        assumeTrue(userService instanceof CachedUserService);
        User user = new User().userId("ttlTestUser").addAuths(Collections.singleton("authorisation")).addRoles(Collections.singleton("role"));
        userService.addUser(user);
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        userService.getUser(user.getUserId());
    }
}
