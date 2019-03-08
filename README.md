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


#### Redis Commander (you should run docker-compose up first)
* See redis contents from here: `http://localhost:8081`


#### How to find root(starting city) of itineraries for a specific country
* Execute:

```mysql
select origin_city_name, origin_country
from routes
where origin_city_name NOT IN (
  select destiny_city_name
  from routes
  where destiny_country = 'Spain'
)
and origin_country = 'Spain';
```


#### How to find root(starting city) of itineraties for all countries
* Execute:
```mysql
select origin_city_name, origin_country
from routes
where origin_city_name NOT IN (
  select destiny_city_name
  from routes
  where destiny_country = origin_country
);
```


#### Example Request

* POST at: localhost:8080/api/route-info/search
  with body:
  
  ```json
    {
        "name":"Pirgos",
        "country":"Greece"
    }

  ```
  
  Response:
  ```json
    {
        "results": [
            {
                "id": "02137e84-1fd2-4f93-ba5d-afd028677db6",
                "city": {
                    "name": "Pirgos",
                    "country": "Greece"
                },
                "destinyCity": {
                    "name": "Hios",
                    "country": "Greece"
                },
                "departureTime": "2019-03-08@15:08:16",
                "arrivalTime": "2019-03-08@19:08:16"
            },
            {
                "id": "69a9317c-ef4e-472a-b377-cdcc0c925a65",
                "city": {
                    "name": "Pirgos",
                    "country": "Greece"
                },
                "destinyCity": {
                    "name": "Alexandroupoli",
                    "country": "Greece"
                },
                "departureTime": "2019-03-08@15:08:16",
                "arrivalTime": "2019-03-08@18:08:16"
            },
            {
                "id": "8dc6f777-652b-4ba6-bcf3-fe59365e7cf2",
                "city": {
                    "name": "Pirgos",
                    "country": "Greece"
                },
                "destinyCity": {
                    "name": "Kerkira",
                    "country": "Greece"
                },
                "departureTime": "2019-03-08@15:08:16",
                "arrivalTime": "2019-03-08@16:08:16"
            },
            {
                "id": "abb497a7-82a4-4563-9d57-3cce1c6da27a",
                "city": {
                    "name": "Pirgos",
                    "country": "Greece"
                },
                "destinyCity": {
                    "name": "Alexandroupoli",
                    "country": "Greece"
                },
                "departureTime": "2019-03-08@15:08:16",
                "arrivalTime": "2019-03-08@16:08:16"
            }
        ]
    }
  ```




#### Useful Docker Commands

* `docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' <container_id>`
