package uk.gov.gchq.palisade.integrationtests.audit.service;

import org.mockito.internal.util.collections.Sets;

import uk.gov.gchq.palisade.Context;
import uk.gov.gchq.palisade.RequestId;
import uk.gov.gchq.palisade.User;
import uk.gov.gchq.palisade.UserId;
import uk.gov.gchq.palisade.resource.LeafResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;
import uk.gov.gchq.palisade.rule.Rules;
import uk.gov.gchq.palisade.service.Service;
import uk.gov.gchq.palisade.service.audit.request.ReadRequestCompleteAuditRequest;
import uk.gov.gchq.palisade.service.audit.request.ReadRequestExceptionAuditRequest;
import uk.gov.gchq.palisade.service.audit.request.RegisterRequestCompleteAuditRequest;
import uk.gov.gchq.palisade.service.audit.request.RegisterRequestExceptionAuditRequest;

import java.util.Collections;

class AuditTestCommon {
    static final long TEST_NUMBER_OF_RECORDS_PROCESSED = 20;
    static final long TEST_NUMBER_OF_RECORDS_RETURNED = 5;
    static final String TEST_TOKEN = "token in the form of a UUID";
    private static final String TEST_USER_ID = "an identifier for the user";
    private static final String TEST_RESOURCE_ID = "a pointer to a data resource";
    private static final String TEST_PURPOSE = "the purpose for the data access request";
    private static final String TEST_ORIGINAL_REQUEST_ID = "originalRequestId linking all logs from the same data access request together";
    private static final String TEST_DATA_TYPE = "data type of the resource, e.g. Employee";
    private static final String TEST_EXCEPTION_MESSAGE = "exception message";

    private static final String TEST_RULES_APPLIED = "human readable description of the rules/policies been applied to the data";

    static UserId mockUserID() {
        return new UserId().id(TEST_USER_ID);
    }

    static User mockUser() {
        return new User().userId(TEST_USER_ID);
    }

    static Context mockContext() {
        return new Context(Collections.singletonMap("purpose", TEST_PURPOSE));
    }

    static RequestId mockOriginalRequestId() {
        return new RequestId().id(TEST_TOKEN);
    }

    static LeafResource mockResource() {
        return new FileResource().id(TEST_RESOURCE_ID).type(TEST_DATA_TYPE).serialisedFormat("none").parent(new DirectoryResource().id("resource").parent(new SystemResource().id("root")));
    }

    static Exception mockException() {
        return new Exception(TEST_EXCEPTION_MESSAGE);
    }

    static Rules mockRules() {
        return new Rules<>().message(TEST_RULES_APPLIED);
    }

    static RegisterRequestCompleteAuditRequest registerRequestCompleteAuditRequest() {
        return RegisterRequestCompleteAuditRequest.create(mockOriginalRequestId())
                .withUser(mockUser())
                .withLeafResources(Sets.newSet(mockResource()))
                .withContext(mockContext());
    }

    static RegisterRequestExceptionAuditRequest registerRequestExceptionAuditRequest() {
        return RegisterRequestExceptionAuditRequest.create(mockOriginalRequestId())
                .withUserId(mockUserID())
                .withResourceId(mockResource().getId())
                .withContext(mockContext())
                .withException(mockException())
                .withServiceClass(Service.class);
    }

    static ReadRequestCompleteAuditRequest readRequestCompleteAuditRequest() {
        return ReadRequestCompleteAuditRequest.create(mockOriginalRequestId())
                .withUser(mockUser())
                .withLeafResource(mockResource())
                .withContext(mockContext())
                .withRulesApplied(mockRules())
                .withNumberOfRecordsReturned(TEST_NUMBER_OF_RECORDS_RETURNED)
                .withNumberOfRecordsProcessed(TEST_NUMBER_OF_RECORDS_PROCESSED);
    }

    static ReadRequestExceptionAuditRequest readRequestExceptionAuditRequest() {
        return ReadRequestExceptionAuditRequest.create(mockOriginalRequestId())
                .withToken(TEST_TOKEN)
                .withLeafResource(mockResource())
                .withException(mockException());
    }
}
