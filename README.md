### Routes Service - Adidas Backend Assignment

##### Travis Status:
TODO


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
* Execute: `mvn clean integration-test` or `mvn clean verify`


#### Useful Docker Commands

* `docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' <container_id>`
