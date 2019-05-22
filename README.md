# Monk

![license](https://img.shields.io/github/license/truecaller/android-actors-library.svg?colorB=388e3c)

Monk is a java powered PDF document parser. It has the capability to detect and parse tabular structures in PDFs and 
extract information from them. It uses [Yuga](https://github.com/messai-engineering/Yuga) to parse PDFs to output fields 
like date, time, amount etc.

# Maintainers

  - Vishnu Satis ([@vizsatiz](https://github.com/vizsatiz))

# Getting Started

## Build

  - Clone the repository
  - Monk uses `maven` for dependency resolution and for build creation. You can easily create your own jar by running 
  `mvn install` from project home (jar gets created in `target` folder, named `monk-1.0.0.jar`)
  - Use the newly created jar in your project as jar dependency.

## Usage

  - Read your file and pass it as argument to below mentioned API of Monk as `java.io.File`.

      ```
      Monk.getTransactions(java.io.File File)
      ```

      Example:

      ```
      java.io.File input = new File("./my/statement/location/statement.pdf");
      List<Map<String, String>> output = Monk.getTransactions(input)
      ```

  - If your PDF is password protected, you can pass the password as the second argument to the same API

    ```
    Monk.getTransactions(java.io.File File, String password)
    ```

    Example:

    ```
    java.io.File input = new File("./my/statement/location/statement.pdf");
    List<Map<String, String>> output = Monk.getTransactions(input, "mysecretpassword")
    ```

## Output

  - Both the above mentioned APIs will return a `List<Map<String, String>>` which represents the tables in
  the PDF as list of key-value pairs.
  - Below is a sample field list in the output

  > The following example uses metadata to parse financial statement (details below)

    | Field | Details |
    | ------ | ------ |
    | date | The date of the transaction if found |
    | credit | The credited amount if found |
    | debit | The debited amount if found |
    | balance | The account balance if found |
    | description | Any transaction details if available |

  - These field names can be changed from the `normaliser.json` (Discussed in Advanced section)

# Advanced

  - Monk uses a metadata driven architecture to parse pdf statements. So it has a metadata file (`main/resources/raw/normaliser.json`)
   , which can be updated to support more statements.
  - You can override the `normaliser.json` using the following code:

    ```
    Monk.setExternalNormaliser(org.json.JSONObject normaliser)
    ```

  - `nomaliser.json` consists of keys which will be the normalised/tokenized name of the value (which are pipe separated).
  Each of the value represent possible column names which can be normalised to the key.
  - Monk by default logs everything to `sysout` but the logger can be overrided using the below code:

    ```
    Monk.setLogger(new IMonkLogger() {
        @Override
        public void logInfo(String msg) {
            // Custom Logger
        }

        @Override
        public void logError(String msg) {
            // Custom Logger
        }
    });
    ```

# License

Copyright (C) 2019 True Software Scandinavia AB

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    <http://www.apache.org/licenses/LICENSE-2.0>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.