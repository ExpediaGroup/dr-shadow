## dr-shadow-spring-boot

Dr Shadow library for Spring Boot applications.

See: [Changelog](CHANGELOG.md)

## Dependency Versions
* Spring Framework 5.0
* Spring Boot 2.0

## Configuration

Sample:
```yaml
drshadow:
  enabled: true
  ssl: false
  percentage: 10
  filter-order: 3
  custom-headers:
    testHeader: "1234"
  forward-headers:
    - "x-cookie"
  hosts:
    - "localhost:8080"
  invoker-core-pool-size: 5
  http-core-pool-size: 5
  http-connection-timeout-ms: 1000
  http-read-timeout-ms: 500
  inclusion-patterns:
    - requestURI: "^/get*"
      method: "GET"
```
### enabled
Defaults to 'false'. Enables/Disables the Filter. Dr Shadow Spring Auto Configuration won't even be wired if this flag is 'false'.

### hosts
List of Strings - Indicates where to forward shadow traffic to. Ex. 'http://cheihtsect001.karmalab.net:15270'. If protocol is not specified then default protocol *https* will be picked by dr shadow

### filter-order
Specifies the priority of Dr Shadow filter order. If you have an auth filter for example, you may want Dr Shadow to happen after.

### inclusion-patterns
#### requestURI
String - Regex pattern matching for the request URI to perform the shadow traffic on.
#### method
String - Indicates which HTTP method associated for this path. This only takes one HTTP method. To apply to all HTTP methods use the string "*".
#### header-patterns
List - Used to match based on header. All header pattern will be considered as implicitly "&".
##### header-key
String - exact value of the header
##### header-key
String - Regex pattern matching for header value. Please refer Sample config for header filter (Supported 2.7 onwards) in configuration section

### percentage
Integer - Percentage of shadow traffic to be sent to the hosts. Valid values are 0-100 inclusive. 100 indicates all traffic, 0 indicates no traffic. Similar to how Zipkin sampling works.

### invoker-core-pool-size
Default value is 5. Integer - Thread pool size of the ShadowTrafficInvoker. This should be equivalent to the traffic you expect you normally get from your endpoint. ie. tp99 of response time in seconds x number of requests per second. Default value is 5. Updating this value requires a restart of the application because it's read at bean creation time!

### http-core-pool-size
Default value is 5. Integer - Connection pool size for the actual HTTP call the ShadowTrafficAdapter makes. This number doesn't need to be all that high, we are firing and forgetting with a 100ms hardcoded timeout on these shadow traffic calls. The reason is because we don't care if it succeeded or not. This means you will see potential 'ConnectionTimeouts' in your logs but they can be ignored. We just need to make sure the connection happens! Default value is 5. Updating this value requires a restart of the application because it's read at bean creation time!

### http-connection-timeout-ms
Default value is 1000. Integer - HTTP connection timeout for the shadow traffic calls in milliseconds. This indicates how long to wait for a connection handshake to be established. This has to happen for any shadow traffic to occur. Account for network latency. You shouldn't need to change this though.

### http-read-timeout-ms
Default value is 300. Integer - HTTP read timeout for the shadow traffic calls. This indicates how long the application will read the bytes for, this value only needs to be big enough for the request to be sent to the shadow server but not too big since we don't care about the response. There should be no need to update this value! If you change this, you should really know what you are doing.

### custom-headers
HashMap - A hashmap of custom headers to send along with the shadow traffic. Note that a prefix will be appended to these custom headers 'shadow-traffic-' to prevent any potential collision!

### forward-headers
List of Strings - Only these items will be copied from original request to shadow request

## Usage
1. Add dependency
```xml
<dependency>
   <groupId>com.expediagroup</groupId>
   <artifactId>dr-shadow-spring-boot</artifactId>
   <version><!--See here for latest versions: https://mvnrepository.com/artifact/com.expediagroup/dr-shadow-spring-boot--></version>
</dependency>
```
2. Add configuration to your Spring application.yaml/properties file.

## Building
```bash
mvn clean install
```

## Acceptance Tests
Live in the *dr-shadow-acceptance-tests* module

## Sample Spring Boot Application
See the sample app [here](../dr-shadow-spring-boot-test-app/README.md)
