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
select origin_city_name, origin_country, destiny_city_name, destiny_country, departure_time, arrival_time
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
select origin_city_name, origin_country, destiny_city_name, destiny_country, departure_time, arrival_time
from routes
where origin_city_name NOT IN (
  select destiny_city_name
  from routes
  where destiny_country = origin_country
);
```


#### How to find terminal routes (appear only as destiny city and not also as origin city)
* Execute:
```mysql
select origin_city_name, origin_country, destiny_city_name, destiny_country, departure_time, arrival_time
from routes
where destiny_city_name NOT IN (
  select origin_city_name
  from routes
  where origin_country = 'Greece'
) and origin_country = 'Greece';
```


#### How to find intermediary routes (appear as destiny city and also as origin city)
* Execute:
```mysql
select origin_city_name, origin_country, destiny_city_name, destiny_country, departure_time, arrival_time
from routes
where destiny_city_name IN (
  select origin_city_name
  from routes
  where origin_country = 'Greece'
) and origin_country = 'Greece';
```


#### Additional Application Properties

* `generate.routes=true` generate sample data on startup of service.

* `cities-csv-processor.display-parsing-info=false` if we would like to display csv parsing information during generation of sample data.

* `route-data-generator.display-storing-info=false` if we would like to display storing information during generation of sample data.

* `route-data-generator.no-of-itineraries-for-selected-root-city=4` how many itineraries we will generate for the random selected root city.


#### Example Request - Response

* POST at: localhost:8080/api/route-info/search
  with body:
  
  ```json
    {
    	"name":"Arrecife",
    	"country":"Spain"
    }

  ```
  
  Response:
  ```json
    {
        "results": [
            {
                "id": "9737ba86-745d-466a-925a-b8312bc81aeb",
                "city": {
                    "name": "Arrecife",
                    "country": "Spain"
                },
                "destinyCity": {
                    "name": "Vitoria",
                    "country": "Spain"
                },
                "departureTime": "2019-03-11T00:15:52Z",
                "arrivalTime": "2019-03-11T03:15:52Z"
            },
            {
                "id": "c14e57a6-f662-4dd8-8e50-8009240735c3",
                "city": {
                    "name": "Arrecife",
                    "country": "Spain"
                },
                "destinyCity": {
                    "name": "La Coruna",
                    "country": "Spain"
                },
                "departureTime": "2019-03-11T00:15:52Z",
                "arrivalTime": "2019-03-11T03:15:52Z"
            },
            {
                "id": "cc68d3d5-3d77-41cf-9a0b-ec0605b5f9ff",
                "city": {
                    "name": "Arrecife",
                    "country": "Spain"
                },
                "destinyCity": {
                    "name": "Malaga",
                    "country": "Spain"
                },
                "departureTime": "2019-03-11T00:15:52Z",
                "arrivalTime": "2019-03-11T02:15:52Z"
            },
            {
                "id": "d355d14e-4f68-43a3-924f-7e6ac8d04305",
                "city": {
                    "name": "Arrecife",
                    "country": "Spain"
                },
                "destinyCity": {
                    "name": "Linares",
                    "country": "Spain"
                },
                "departureTime": "2019-03-11T00:15:52Z",
                "arrivalTime": "2019-03-11T02:15:52Z"
            }
        ]
    }
  ```




#### Useful Docker Commands

* `docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' <container_id>`
