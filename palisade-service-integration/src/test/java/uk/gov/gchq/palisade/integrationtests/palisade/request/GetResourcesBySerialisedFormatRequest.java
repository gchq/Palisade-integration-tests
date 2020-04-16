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
 * from the resource-service based on the serialisedFormat of those resources.
 * For example getting a list of all {@link uk.gov.gchq.palisade.resource.Resource}'s where the serialisedFormat is CSV.
 */
public class GetResourcesBySerialisedFormatRequest extends Request {
    private String serialisedFormat;

    public GetResourcesBySerialisedFormatRequest() {
        //no-args constructor needed for serialization only
    }

    /**
     * @param serialisedFormat the serialisedFormat of the {@link uk.gov.gchq.palisade.resource.Resource}'s that you want to know about
     * @return the {@link GetResourcesBySerialisedFormatRequest}
     */
    @Generated
    public GetResourcesBySerialisedFormatRequest serialisedFormat(final String serialisedFormat) {
        this.setSerialisedFormat(serialisedFormat);
        return this;
    }

    @Generated
    public String getSerialisedFormat() {
        return serialisedFormat;
    }

    @Generated
    public void setSerialisedFormat(final String serialisedFormat) {
        requireNonNull(serialisedFormat);
        this.serialisedFormat = serialisedFormat;
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GetResourcesBySerialisedFormatRequest)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        GetResourcesBySerialisedFormatRequest that = (GetResourcesBySerialisedFormatRequest) o;
        return serialisedFormat.equals(that.serialisedFormat);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(super.hashCode(), serialisedFormat);
    }

    @Override
    @Generated
    public String toString() {
        return new StringJoiner(", ", GetResourcesBySerialisedFormatRequest.class.getSimpleName() + "[", "]")
                .add("serialisedFormat='" + serialisedFormat + "'")
                .add(super.toString())
                .toString();
    }
}
