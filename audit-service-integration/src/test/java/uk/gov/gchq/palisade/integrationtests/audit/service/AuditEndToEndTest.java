package uk.gov.gchq.palisade.integrationtests.audit.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.service.audit.AuditApplication;
import uk.gov.gchq.palisade.service.audit.request.AuditRequest;
import uk.gov.gchq.palisade.service.audit.service.AuditService;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuditApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public class AuditEndToEndTest extends AuditTestCommon {
    Logger LOGGER = LoggerFactory.getLogger(AuditEndToEndTest.class);

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private Map<String, AuditService> serviceMap;

    List<AuditRequest> requests = List.of(
            readRequestCompleteAuditRequest(),
            readRequestExceptionAuditRequest(),
            registerRequestCompleteAuditRequest(),
            registerRequestExceptionAuditRequest()
    );

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
    public void endToEnd() {
        requests.forEach(request -> {
            Boolean response = restTemplate.postForObject("/audit", request, Boolean.class);

            assertThat(response, is(equalTo(true)));
        });
    }

}
