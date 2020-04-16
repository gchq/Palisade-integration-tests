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
package uk.gov.gchq.palisade.integrationtests.palisade.request;

import uk.gov.gchq.palisade.Generated;
import uk.gov.gchq.palisade.service.request.Request;

import java.util.Objects;
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

public class GetResourcesByIdRequest extends Request {

    private String resourceId;

    public GetResourcesByIdRequest() {
        //no-args constructor needed for serialization only
    }

    /**
     * @param resourceId the unique identifier of the resource that you want to {@code ls}
     * @return the {@link GetResourcesByIdRequest}
     */
    @Generated
    public GetResourcesByIdRequest resourceId(final String resourceId) {
        this.setResourceId(resourceId);
        return this;
    }

    @Generated
    public String getResourceId() {
        return resourceId;
    }

    @Generated
    public void setResourceId(final String resourceId) {
        requireNonNull(resourceId);
        this.resourceId = resourceId;
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GetResourcesByIdRequest)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        GetResourcesByIdRequest that = (GetResourcesByIdRequest) o;
        return resourceId.equals(that.resourceId);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(super.hashCode(), resourceId);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", GetResourcesByIdRequest.class.getSimpleName() + "[", "]")
                .add("resourceId='" + resourceId + "'")
                .add(super.toString())
                .toString();
    }
}
