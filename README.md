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


#### Additional Application Properties

* `generate.routes=true` generate sample data on startup of service.

* `cities-csv-processor.display-parsing-info=false` if we would like to display csv parsing information during generation of sample data.

* `route-data-generator.display-storing-info=false` if we would like to display storing information during generation of sample data.

* `route-data-generator.no-of-itineraries-for-selected-root-city=4` how many itineraries we will generate for the random selected root city.


#### Example Request

* POST at: localhost:8080/api/route-info/search
  with body:
  
  ```json
    {
    	"name":"Santander",
    	"country":"Spain"
    }

  ```
  
  Response:
  ```json
    {
        "results": [
            {
                "id": "1c4173b8-9569-44ef-9689-925f4b0e060f",
                "city": {
                    "name": "Santander",
                    "country": "Spain"
                },
                "destinyCity": {
                    "name": "Pamplona",
                    "country": "Spain"
                },
                "departureTime": "2019-03-08@18:28:00",
                "arrivalTime": "2019-03-08@22:28:00"
            },
            {
                "id": "4dd8d64d-8b19-4427-b463-72b2f8cc0dd3",
                "city": {
                    "name": "Santander",
                    "country": "Spain"
                },
                "destinyCity": {
                    "name": "Lorca",
                    "country": "Spain"
                },
                "departureTime": "2019-03-08@18:28:00",
                "arrivalTime": "2019-03-08@20:28:00"
            },
            {
                "id": "e66fd07a-081d-4d36-b9a1-ed403bed122f",
                "city": {
                    "name": "Santander",
                    "country": "Spain"
                },
                "destinyCity": {
                    "name": "Oviedo",
                    "country": "Spain"
                },
                "departureTime": "2019-03-08@18:28:00",
                "arrivalTime": "2019-03-08@19:28:00"
            },
            {
                "id": "ed778e07-5ace-4f0f-a244-202a0985c301",
                "city": {
                    "name": "Santander",
                    "country": "Spain"
                },
                "destinyCity": {
                    "name": "Vigo",
                    "country": "Spain"
                },
                "departureTime": "2019-03-08@18:28:00",
                "arrivalTime": "2019-03-08@19:28:00"
            }
        ]
    }
  ```




#### Useful Docker Commands

* `docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' <container_id>`
