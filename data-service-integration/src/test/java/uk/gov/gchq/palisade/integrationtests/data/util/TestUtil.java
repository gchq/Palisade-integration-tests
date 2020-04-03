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

package uk.gov.gchq.palisade.integrationtests.data.util;

import uk.gov.gchq.palisade.resource.ChildResource;
import uk.gov.gchq.palisade.resource.impl.DirectoryResource;
import uk.gov.gchq.palisade.resource.impl.FileResource;
import uk.gov.gchq.palisade.resource.impl.SystemResource;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

public class TestUtil {

    private TestUtil() {
    }

    public static FileResource createFileResource(final Path path, final String type) {
        FileResource file = fileResource(path).serialisedFormat("avro").type(type);
        resolveParents(path, file);

        return file;
    }

    private static void resolveParents(final Path path, final ChildResource childResource) {
        requireNonNull(path);
        Optional<Path> parent = Optional.ofNullable(path.getParent());
        parent.ifPresentOrElse(parentPath -> {
            DirectoryResource directoryResource = directoryResource(parentPath);
            childResource.setParent(directoryResource);
            resolveParents(parentPath, directoryResource);
        }, () -> {
            SystemResource systemResource = systemResource(path);
            childResource.setParent(systemResource);
        });
    }

    private static FileResource fileResource(final Path path) {
        return new FileResource().id(toURI(path).toString());
    }

    private static DirectoryResource directoryResource(final Path path) {
        return new DirectoryResource().id(toURI(path).toString());
    }

    private static SystemResource systemResource(final Path path) {
        return new SystemResource().id(toURI(path).toString());
    }

    private static URI toURI(final Path path) {
        return convertToFileURI(path.toString());
    }

    /**
     * Convert the given path to an absolute URI. If the given path represents something on the local file system, then
     * the path will be converted to a full absolute path and converted to a {@code file:} URI, if not then it will be returned verbatim.
     *
     * @param path the path to convert
     * @return the URI of the path
     * @throws IllegalArgumentException if {@code path} is empty
     */
    private static URI convertToFileURI(final String path) {
        requireNonNull(path, "path");
        if (path.isEmpty()) {
            throw new IllegalArgumentException("path is empty");
        }
        URI uriPath;
        try {
            uriPath = new URI(path);
        } catch (URISyntaxException e) {
            try {
                uriPath = Paths.get(path).normalize().toUri();
            } catch (IllegalArgumentException | FileSystemNotFoundException f) {
                throw new RuntimeException("Can't parse the given file name: ", f);
            }
        }

        // If the URI scheme is not provided or is not correct for this filesystem attempt to correct it if possible

        if (isNull(uriPath.getScheme()) ||
                FileSystems.getDefault().provider().getScheme().equals(uriPath.getScheme())) {

            Pattern patt = Pattern.compile(FileSystems.getDefault().provider().getScheme() + ":[/]?([^/].*)$");
            Matcher matt = patt.matcher(uriPath.toString());
            Path file;

            if (matt.matches()) {
                return URI.create(FileSystems.getDefault().provider().getScheme() + "://" + matt.group(1));
            } else {
                if (isNull(uriPath.getScheme())) {
                    file = FileSystems.getDefault().getPath(uriPath.getPath());
                } else {
                    file = Paths.get(uriPath).normalize();
                }
            }
            //normalise this against the file system
            try {
                file = file.toRealPath(LinkOption.NOFOLLOW_LINKS);
            } catch (IOException e) {
                //doesn't exist
            }
            return file.toUri();
        }
        return uriPath;
    }
}
