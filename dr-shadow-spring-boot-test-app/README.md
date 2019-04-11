# Spring Boot Test App
This is a simple Spring Boot Test App that uses Dr Shadow's Spring Boot library.

## Test
This test app can be used to do a full fledged end to end test of Dr Shadow Spring Boot changes.

* Run the spring-boot-test-app using the default configuration.
* Run a second copy using a VM override property of: -Dserver.port=8081
* Hit the url in your browser: http://localhost:8080/hello?test=abc
* Observe the log on the first application.
Example:
```aidl
13:24:01.564 [http-nio-8080-exec-1] carlson INFO  hello.HelloController - inside HelloWorld: test=abc
13:24:01.574 [shadowTrafficTaskExecutor-1] carlson INFO  c.e.l.d.s.ShadowTrafficAdapter - Forwarding shadow traffic url: http://localhost:8081/hello?test=abc to host: http://localhost:8081
```
Notice the 'carlson' is set in the first application's logging filter.
* Observe the log on the second application.
Example:
```aidl
13:24:01.744 [http-nio-8081-exec-1] carlson INFO  hello.HelloController - inside HelloWorld: test=abc
```
Notice the 'carlson' MDC log was copied over.

## Run
```bash
mvn clean spring-boot:run
```

