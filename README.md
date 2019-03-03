### Routes Service - Adidas Backend Assignment


```
,------.                  ,--.                     ,---.                          ,--.
|  .--. ' ,---. ,--.,--.,-'  '-. ,---.  ,---.     '   .-'  ,---. ,--.--.,--.  ,--.`--' ,---. ,---.
|  '--'.'| .-. ||  ||  |'-.  .-'| .-. :(  .-'     `.  `-. | .-. :|  .--' \  `'  / ,--.| .--'| .-. :
|  |\  \ ' '-' ''  ''  '  |  |  \   --..-'  `)    .-'    |\   --.|  |     \    /  |  |\ `--.\   --.
`--' '--' `---'  `----'   `--'   `----'`----'     `-----'  `----'`--'      `--'   `--' `---' `----'
                     ,-----.,--.            ,--.        ,--.,--.
                    '  .--./|  ,---. ,--.--.`--',--,--, `--'|  |,-. ,---.
                    |  |    |  .-.  ||  .--',--.|      \,--.|     /| .-. |
                    '  '--'\|  | |  ||  |   |  ||  ||  ||  ||  \  \' '-' '
                     `-----'`--' `--'`--'   `--'`--''--'`--'`--'`--'`---'                  
```

##### Assignee: Nikolaos Christidis (nick.christidis@yahoo.com)


#### Prequisities in order to local run
1) Docker Compose


#### How to run service (not dockerized)
* Execute: `docker-compose up`

* Two options:
    * Execute: 
        * `mvn clean install -DskipTests=true`
        * `java -jar -Dspring.profiles.active=dev -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector target/routes-service-1.0.0-SNAPSHOT.jar`
                
    * Execute:
        * `mvn spring-boot:run -Dspring.profiles.active=dev -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector`

* (Optional) When you finish: `docker-compose down`


#### How to run service (dockerized)
* Uncomment the section in `docker-compose.yml` file for service: `routes-service:`

* Execute: `mvn clean install -DskipTests=true`

* Execute: `docker-compose build`

* Execute: `docker-compose up`

* (Optional) When you finish: `docker-compose down`


#### Execute Unit Tests
* Execute: `mvn clean test`


#### Execute Integration Tests (you should run docker-compose up first)
* Execute: `mvn clean integration-test -DskipUTs=true` or `mvn clean verify -DskipUTs=true`


#### Test Coverage (via JaCoCo)
* In order to generate reports execute: `mvn clean verify`
    * In order to see unit test coverage open with browser: `target/site/jacoco-ut/index.html`
    * In order to see integration test coverage open with browser: `target/site/jacoco-it/index.html`


#### Useful Docker Commands

* `docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' <container_id>`
