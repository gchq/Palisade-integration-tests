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

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

import uk.gov.gchq.palisade.service.palisade.PalisadeApplication;
import uk.gov.gchq.palisade.service.palisade.config.ApplicationConfiguration;

@SpringBootApplication
@EnableFeignClients(basePackages = "uk.gov.gchq.palisade.service.palisade.web")
@Import(ApplicationConfiguration.class)
public class UserServiceIntegrationTestApplication {

    public static void main(final String[] args) {
        new SpringApplicationBuilder(PalisadeApplication.class).web(args.length == 0 ? WebApplicationType.SERVLET : WebApplicationType.NONE)
                .run(args);
    }

}