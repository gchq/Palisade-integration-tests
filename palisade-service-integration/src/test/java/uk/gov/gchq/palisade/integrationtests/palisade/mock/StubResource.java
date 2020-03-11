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
package uk.gov.gchq.palisade.integrationtests.palisade.mock;

import uk.gov.gchq.palisade.resource.AbstractLeafResource;
import uk.gov.gchq.palisade.resource.Resource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;

import java.util.Comparator;

public class StubResource extends AbstractLeafResource {

    private static SystemResource parent = new SystemResource().id("file");

    public StubResource() {

    }

    public StubResource(final String type, final String id, final String format) {
        id(id);
        type(type);
        serialisedFormat(format);
        parent(parent);
    }

    private static Comparator<StubResource> comp = Comparator.comparing(StubResource::getSerialisedFormat).thenComparing(StubResource::getType).thenComparing(StubResource::getId);

    /**
     * {@inheritDoc}
     * Implemented to allow this class to be used in TreeMaps in tests.
     */
    @Override
    public int compareTo(final Resource o) {
        return comp.compare(this, (StubResource) o);
    }

}
