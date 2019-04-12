# Dr Shadow
![License](https://img.shields.io/hexpm/l/plug.svg)
[![Build Status](https://travis-ci.org/egencia/dr-shadow.svg?branch=master)](https://travis-ci.org/egencia/dr-shadow)

Dr Shadow is a library developed by Egencia (part of Expedia Group) that enables shadow traffic (ie. mirroring). It is a valuable tool for having good hygiene for service operations (ie. testing, resiliency, performance).

Dr Shadow works really well when you have metrics and logging in place to observe. If Dr Shadow is doing it's job, you should see a rough percentage of traffic being mirrored in HTTP request graphs for the canary/pre-prod fleet in relation to production traffic.

See [Contributing](CONTRIBUTING.md) and [Code of Conduct](CODE_OF_CONDUCT.md) to contribute.

## Modules
* [Dr Shadow for Spring Boot](dr-shadow-spring-boot/README.md)
* [Dr Shadow Spring Boot Test App](dr-shadow-spring-boot-test-app/README.md)

## Protocol Support
* HTTP

## Use Cases
At Egencia, we used Dr Shadow to perform the following use cases.
### CI/CD Canary/Pre-Prod Testing
Dr Shadow will assist you in testing in a Canary/Pre-Prod type environment with real live site traffic and you can configure the percentage of traffic you wish to funnel to it. No matter how much testing you do, nothing beats using actual customers of your application.

The recommendation here is to have machines out of rotation that have a new version of the code you wish to release that is accessible by the Production machines to forward traffic to. It's also recommended to allow for baking time in which developers will monitor these out of rotation machines over a time period before officially releasing the new code.

### Cloud Migration Analysis
As part of cloud migration we configured Dr Shadow to analyze our application in on prem vs cloud by sending real traffic over to the cloud servers. This was immense in helping us understand latency differences and helped us steer in the right direction of what to address before moving to cloud 100% for production traffic.

### Performance/Scaling
Dr Shadow can assist you in figuring out how much traffic your service can actually take by sending 100% of prod traffic to your Canary/Pre-Prod fleet. This is useful in cases where you're over/under scaled and you want to know how much you can actually handle while maximizing your resources. It can also be used for testing out auto scaling features.

For example, let's say you have one machine in Canary/Pre-Prod and 10 machines in Production. By sending 10% shadow traffic you are mimicking production traffic, but if you want to see if you can put more traffic on it then dial the shadow traffic up.

## Words of Caution
* Please be careful when adding in this feature into your application. The advice here is that you should limit the percentage of how much shadow traffic you send out at first and then dial up as needed. Start small!
* Please be careful with endpoints that change state. The reason why there is an inclusion pattern matcher is to ensure some endpoints can be configured to not send shadow traffic. There is no good strategy for shadow traffic testing endpoints that change state, it's a case by case basis. I think some ideas around endpoints that change state would involve mocking the parts that do change state based on the shadow traffic header. Shadow testing something is better than nothing!

## Important Info
* The header *is-shadow-traffic*: *true* is added for all shadow traffic to prevent infinite looping.
* All custom headers are pre-pended with *shadow-traffic-* to prevent any collisions with existing headers.

## Release
* Update all modules pom.xml to have new versions
* Once PR approved and merged to master, tag the release.
Example:
```bash
git tag -a 1.0.1 -m "release 1.0.1"
git push origin 1.0.1
```
