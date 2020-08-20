<!---
Copyright 2020 Crown Copyright

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

# <img src="/logos/logo.svg" width="180">

### Scalable Data Access Policy Management and Enforcement

## Hadoop and Windows

Windows users will have problems with Hadoop integration testing.
Included [here](./src/test/resources/hadoop-3.2.1) is a Windows-compatible set of hadoop binaries.

To 'install' on Windows, an additional step is required - copy the [hadoop.dll](./src/test/resources/hadoop-3.2.1/bin/hadoop.dll) to `C:\Windows\System32`.
This should enable the `HadoopResourceServiceTest` to run correctly.
