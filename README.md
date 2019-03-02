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
        * `mvn clean package`
        * `java -jar target/sdk-backend-test-1.0.0-SNAPSHOT.jar -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector`
                
    * Execute:
        * `mvn spring-boot:run -DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector`

* (Optional) When you finish: `docker-compose down`


#### Execute Unit Tests
* Execute: `mvn clean test`


#### Execute Integration Tests (you should run docker-compose up first)
* Execute: `mvn clean integration-test` or `mvn clean verify`


#### Useful Docker Commands

* In order to connect to sonarqube postgresql

    * Execute: `docker ps` and find the id of your running container
    * Then in order to find the host of postgre running container execute: `docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' <container_id>`
