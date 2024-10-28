# Description

Emote Form Element plugin allows users to react to submitted forms using emotes. These emotes are stored in a separate form while maintaining a reference to the original form. Please refer to its [documentation](https://dev.joget.org/community/display/SANDBOX2/Emote+Form+Element).

** If you are using DX7 or older, you will need to update the emote column to support storing emotes in the database. (For more information, DX7 or older is using utf8mb3 encoding)

** For example, after importing the sample app, you should run the SQL code below to change the emote column to utf8mb4 encoding.

```
ALTER TABLE `app_fd_efesa_store` 
CHANGE COLUMN `c_emote` `c_emote` LONGTEXT 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL;
```

# Getting Help

JogetOSS is a community-led team for open source software related to the [Joget](https://www.joget.org) no-code/low-code application platform.
Projects under JogetOSS are community-driven and community-supported.
To obtain support, ask questions, get answers and help others, please participate in the [Community Q&A](https://answers.joget.org/).

# Contributing

This project welcomes contributions and suggestions, please open an issue or create a pull request.

Please note that all interactions fall under our [Code of Conduct](https://github.com/jogetoss/repo-template/blob/main/CODE_OF_CONDUCT.md).

# Licensing

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

NOTE: This software may depend on other packages that may be licensed under different open source licenses.
