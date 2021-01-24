![Java CI with Gradle](https://github.com/psurti/crest4j/workflows/Java%20CI%20with%20Gradle/badge.svg?branch=master)

## cREST4j client
cREST ("connected REST") enables execution of dynamic sequence of REST calls connected to a server.
For example, a sequence may look like: login, retrieve items and update an item. In this sequence a login call
is performed via OAuth or Basic Auth then a GET REST call to retrieve a list of items. Using `jsonPath` 
extract a specific `id` and use the extracted `id` in the REST call via dynamic substitution to update the item.

### Features
- Ability drive sequence of calls using property files
- Ability to assign variables of extract data from responses using `jsonPath` expressions
- Ability to substitute variables at runtime on other REST calls in the sequence
- Ability to pass contextual data across REST calls.

## Getting Started

### Building the project
The project uses `gradle` to build. Gradle version: `6.8.1`
To build the project just run the following command:
```shell
gradlew clean bootJar
```

### Running unit tests

### Running integration tests

### Using cREST
#### Defining a `Properties` file
See `blog.properties` for an example. The following is a description of all the properties




#### Other tools
https://github.com/Kortex/jrest-client/
