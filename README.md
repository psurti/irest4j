![Java CI with Gradle](https://github.com/psurti/crest4j/workflows/Java%20CI%20with%20Gradle/badge.svg?branch=master)
![CodeQL](https://github.com/psurti/crest4j/workflows/CodeQL/badge.svg)
![iDino logo](doc/logo/iDino.png)
## iREST4j client
iREST4j ("*integration* REST") is a java tool that allows execution of a sequence of REST calls to a server. A REST sequence can be Login (OAuth/Basic Auth), GET (call to retrieve a list of items) and POST (call to update an item given parameters from the previous call). iREST4j inter-connects REST calls by passing extracted parameters from a previous call and provide variable subsititions on the following REST calls by sharing context in a sequence. 

### Features
- Ability drive sequence of calls using property or YAML files
- Ability to assign variables of extract data from responses using `jsonPath` expressions
- Ability to substitute variables at runtime on other REST calls in the sequence
- Ability to pass contextual data across REST calls.

### Future
- Support HTTPs

## Getting Started

### Building the project
The project uses `gradle` to build. Gradle version: `6.8.1`
To build the project just run the following command:
```shell
gradlew clean bootJar
```

### Running the tool
```
java -jar irest4j-1.0-SNAPSHOT.jar <path-to-properties-file>
# eg. java -jar build\libs\crest4j-1.0-SNAPSHOT.jar build\resources\main\blog.properties
```

### Running unit tests

### Running integration tests

### Using iREST
#### Defining a `Properties` file
See `blog.properties` for an example. 

The property file consists of main property values.

Property Name | Description | Example
:----- | :---- | :-----
host   |  HTTP url of the server | host=https://httpbin.org
actions | Comma-separated list of REST actions. REST actions are names prefixed with `get`,`login`, `post`, `put`, `delete`, `form` | actions=login,getAllItems,putItem
pretty | Ouput JSON data in well-indented format. Valid values are `true` or `false`| pretty=true |
ctx | Define constant values to  variable names that can be substituted in the requests. See example | ctx.foo=bar or ctx.step=5
ctx.seqid | This is a builtin sequence number variable that can be substiuted in other REST calls| getPost.path=/posts/{{ctx.seqId}}

For each REST action in the actions list above these properties are specific to the endpoint:

Property Name | Description | Example
:----- | :---- | :-----
path   | The REST endpoint to call. In the example `item.id` is a variable that is defined and will be substituted in the URL | getItem.path=/items/{{item.id}} or path=/items/10
jsonPath.{name}| Assign a variable `name` after evaluating the REST response with JsonPath expression. Please refer to [JsonPath expressions](http://jsonpath.com)|getAllItems.jsonPath.userId=$[?(@.title == 'laboriosam dolor voluptates')].userId or getAllItems.jsonPath.user.id=$..id
encodeUrl| URI encode the entire URL by setting `true` or `false` (default)| postNewBlog.encodeUrl=true

#### Builtin behaviour

#### Other tools
https://github.com/Kortex/jrest-client/
