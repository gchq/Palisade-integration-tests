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

/**
 * This class is used to request a list of {@link uk.gov.gchq.palisade.resource.Resource}'s
 * from the resource-service based on the type of a {@link uk.gov.gchq.palisade.resource.Resource}.
 * For example getting a list of all {@link uk.gov.gchq.palisade.resource.Resource}'s with the given type.
 */
public class GetResourcesByTypeRequest extends Request {
    private String type;

    public GetResourcesByTypeRequest() {
        //no-args constructor needed for serialization only
    }

    /**
     * @param type the type of the {@link uk.gov.gchq.palisade.resource.Resource}'s that you want to know about
     * @return the {@link GetResourcesByTypeRequest}
     */
    @Generated
    public GetResourcesByTypeRequest type(final String type) {
        this.setType(type);
        return this;
    }

    @Generated
    public String getType() {
        return type;
    }

    @Generated
    public void setType(final String type) {
        requireNonNull(type);
        this.type = type;
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GetResourcesByTypeRequest)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        GetResourcesByTypeRequest that = (GetResourcesByTypeRequest) o;
        return type.equals(that.type);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(super.hashCode(), type);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", GetResourcesByTypeRequest.class.getSimpleName() + "[", "]")
                .add("type='" + type + "'")
                .add(super.toString())
                .toString();
    }
}
