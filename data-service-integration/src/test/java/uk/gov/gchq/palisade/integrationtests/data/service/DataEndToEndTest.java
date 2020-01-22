package uk.gov.gchq.palisade.integrationtests.data.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.gchq.palisade.reader.common.DataReader;
import uk.gov.gchq.palisade.service.data.DataApplication;
import uk.gov.gchq.palisade.service.data.service.DataService;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DataApplication.class, webEnvironment = WebEnvironment.DEFINED_PORT)
public class DataEndToEndTest extends DataTestCommon {
    Logger LOGGER = LoggerFactory.getLogger(DataEndToEndTest.class);

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private Map<String, DataService> serviceMap;
    @Autowired
    private Map<String, DataReader> readerMap;

    @Test
    public void contextLoads() {
        // The data service is both parts service and reader
        assertNotNull(serviceMap);
        assertNotEquals(serviceMap, Collections.emptyMap());

        assertNotNull(readerMap);
        assertNotEquals(readerMap, Collections.emptyMap());
    }

    @Test
    public void isUp() {
        final String health = this.restTemplate.getForObject("/actuator/health", String.class);

        assertThat(health, is(equalTo("{\"status\":\"UP\"}")));
    }

    @Test
    public void endToEnd() {
        //requests.forEach(request -> {
        //    Boolean response = restTemplate.postForObject("/data", request, Boolean.class);

        //    assertThat(response, is(equalTo(true)));
        //});
    }

}
