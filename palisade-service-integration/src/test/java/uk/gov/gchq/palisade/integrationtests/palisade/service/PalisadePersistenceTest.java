package uk.gov.gchq.palisade.integrationtests.palisade.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.service.palisade.PalisadeApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PalisadeApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD) // reset db after each test
@EnableJpaRepositories(basePackages = {"uk.gov.gchq.palisade.service.palisade.repository"})
public class PalisadePersistenceTest {

}
