![Java CI with Gradle](https://github.com/psurti/crest4j/workflows/Java%20CI%20with%20Gradle/badge.svg?branch=master)
![CodeQL](https://github.com/psurti/crest4j/workflows/CodeQL/badge.svg)
<img src="doc/logo/iDino2.png" width="27%"  height="27%" style="float: left" align="left">

## iREST4j client

iREST4j ("*interconnect* REST") is a java tool that allows execution of a sequence of REST calls to a server. A REST
sequence can be Login (OAuth/Basic Auth), GET (call to retrieve a list of items) and POST (call to update an item given
parameters from the previous call). iREST4j inter-connects REST calls by passing extracted parameters from a previous
call and provide variable subsititions on the following REST calls by sharing context in a sequence.

### Features

- Ability drive sequence of calls using property or YAML files
- Ability to assign variables of extract data from responses using `jsonPath` expressions
- Ability to substitute variables at runtime on other REST calls in the sequence
- Ability to pass contextual data across REST calls.

### TODOs

- Support HTTPs
- Support Branching

## Getting Started

### Building the project

The project uses `gradle` to build. Gradle version: `6.8.1`
To build the project just run the following command:

```shell
gradlew clean bootJar run --args=src\main\resources\blog2.properties
gradlew clean bootJar run --args=src\main\resources\blog2.yaml -Dlogback.configurationFile=build\resources\main\logback.xml
```

### Running the tool

```
java -jar irest4j-x.y.jar <path-to-properties-file>
# eg. java -jar build\libs\irest4j-2.0.jar build\resources\main\blog2.properties

## To override logging level 
#  Change log level in logback.xml 
#  java -Dlogback.configurationFile=build\resources\main\logback.xml -jar build\libs\irest4j-2.0.jar build\resources\main\blog2.properties
```

### Configuration Properties

#### Global Level

Configuration can be specified using YAML or properties file. Please see `blog2.properties` or `blog2.yaml` for examples.

Property **host** is the URL used to identify the REST server.

**Example:**

```properties
host=https://httpbin.org:80
```

Property **actions** is a list of REST actions to execute in the order defined. The name of the actions are user-defined
and must prefix with: *"get" , "post" , "put" , "delete" , or  "form"*. The prefixes *"get", "post", "put", "delete"*
map to *HTTP GET, HTTP POST, HTTP PUT and HTTP DELETE* operations respectively. Prefix *"form"* is for form-based *POST*
requests that does *"application/x-www-form-url-encoded"* encoding automatically. A built-in *"login"* action is also
supported that enables OAuth and Basic Authentication using authorization "Bearer" and "Basic" headers.

**Example:**

```properties
actions=login,getAllItems,putItem
```

Property **pretty** outputs JSON responses in a well-indented format. The valid values are true or false (default).

**Example:**

```properties
pretty=true
```

Property **ctx** is an application context used for assigning constant values to user-defined variable names. Using
format "{{ .. }}", these variables are substituted on request calls.

There is also a built-in variable part of the context such as:
"ctx.seq.id ": This is an auto-sequence generated number for unique identifiers during request calls.

**Example:**

```properties
ctx.foo=bar 
ctx.page=5
getBlogPosts.path=/posts/{{ctx.seq.id}}
```

#### Request/Response Level

For each REST action in the actions list above, these properties are specific to the endpoint:

Property **path** represents a static of dynamic URI endpoint to call. Additionally, URI can have parameters. Variables
are dynamically substituted.

**Example:**

```properties
getItem.path=/items/{{item.id}}
getItem.path=/items/10
```

Property **jsonPath.{user-defined variable name}** evaluates JsonPath expression and assign to the user-defined
variable. Variables can have multiple dot notations, as shown in the examples. Please refer to JsonPath expressions for
more information.

**Example:**

```properties
getAllItems.jsonPath.userId=$[?(@.title == 'laboriosam dolor voluptates')].userId 
getAllItems.jsonPath.user.id=$..id
```

Property **encodeUrl** performs URI encoding on the entire URL. It can be enabled/disabled by setting `true`
or `false` (default) respectively.

**Example:**

```properties
postNewBlog.encodeUrl=true
```

#### Other tools

https://github.com/Kortex/jrest-client/
