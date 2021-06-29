<!---
Copyright 2018-2021 Crown Copyright

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--->
# <img src="logos/logo.svg" width="180">

## A Tool for Complex and Scalable Data Access Policy Enforcement

# Integration Tests for Palisade Services

### Prerequisites
1. [Git](https://git-scm.com/)
2. [Maven](https://maven.apache.org/)

## Getting started
The integration test suite is to be run after the build but before the deployment as part of the CI/CD pipeline.

To get started, clone the Palisade Common repo:

```bash
git clone https://github.com/gchq/Palisade-integration-tests.git
cd Palisade-integration-tests
```

You are then ready to build with Maven:
```bash
mvn install
```

## License
Palisade-Common is licensed under the [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0) and is covered by [Crown Copyright](https://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/copyright-and-re-use/crown-copyright/).


## Contributing
We welcome contributions to the project. Detailed information on our ways of working can be found [here](https://gchq.github.io/Palisade/doc/other/ways_of_working.html).


## FAQ
Q: What versions of Java are supported?  
A: We are currently using Java 11.

