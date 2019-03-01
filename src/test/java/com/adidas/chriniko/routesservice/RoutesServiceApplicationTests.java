package com.adidas.chriniko.routesservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RoutesServiceApplicationTests {

    @Test
    public void contextLoads() {
    }


    /*

{
	"city":"origin 1",
	"country":"origin country411"
}


{
    "timestamp": "2019-02-28T22:35:23.065+0000",
    "path": "/api/route-info/",
    "status": 404,
    "error": "Not Found",
    "message": "no record with city name: origin 1 and country: origin country411"
}


     */



    /*
{
	"city":"origin 1",
	"country":"origin country 1"
}


{
    "city": {
        "city": "origin 1",
        "country": "origin country1"
    },
    "destinyCity": {
        "city": "destiny1",
        "country": "destiny country1"
    },
    "departureTime": "2019-02-28T22:31:23Z",
    "arrivalTime": "2019-03-01T00:31:23Z"
}

     */


    /*

{
	"city":" ",
	"country":"origin country1"
}


{
    "timestamp": "2019-02-28T22:36:01.692+0000",
    "path": "/api/route-info/",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed for argument at index 0 in method: public reactor.core.publisher.Mono<com.adidas.chriniko.routesservice.dto.RouteInfo> com.adidas.chriniko.routesservice.resource.RouteResource.find(com.adidas.chriniko.routesservice.dto.CityInfo), with 1 error(s): [Field error in object 'cityInfo' on field 'city': rejected value [ ]; codes [NotBlank.cityInfo.city,NotBlank.city,NotBlank.java.lang.String,NotBlank]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [cityInfo.city,city]; arguments []; default message [city]]; default message [must not be blank]] ",
    "errors": [
        {
            "codes": [
                "NotBlank.cityInfo.city",
                "NotBlank.city",
                "NotBlank.java.lang.String",
                "NotBlank"
            ],
            "arguments": [
                {
                    "codes": [
                        "cityInfo.city",
                        "city"
                    ],
                    "arguments": null,
                    "defaultMessage": "city",
                    "code": "city"
                }
            ],
            "defaultMessage": "must not be blank",
            "objectName": "cityInfo",
            "field": "city",
            "rejectedValue": " ",
            "bindingFailure": false,
            "code": "NotBlank"
        }
    ]
}

     */



    /*
    {
	"country":"origin country1"
}


{
    "timestamp": "2019-02-28T22:36:14.814+0000",
    "path": "/api/route-info/",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed for argument at index 0 in method: public reactor.core.publisher.Mono<com.adidas.chriniko.routesservice.dto.RouteInfo> com.adidas.chriniko.routesservice.resource.RouteResource.find(com.adidas.chriniko.routesservice.dto.CityInfo), with 1 error(s): [Field error in object 'cityInfo' on field 'city': rejected value [null]; codes [NotBlank.cityInfo.city,NotBlank.city,NotBlank.java.lang.String,NotBlank]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [cityInfo.city,city]; arguments []; default message [city]]; default message [must not be blank]] ",
    "errors": [
        {
            "codes": [
                "NotBlank.cityInfo.city",
                "NotBlank.city",
                "NotBlank.java.lang.String",
                "NotBlank"
            ],
            "arguments": [
                {
                    "codes": [
                        "cityInfo.city",
                        "city"
                    ],
                    "arguments": null,
                    "defaultMessage": "city",
                    "code": "city"
                }
            ],
            "defaultMessage": "must not be blank",
            "objectName": "cityInfo",
            "field": "city",
            "rejectedValue": null,
            "bindingFailure": false,
            "code": "NotBlank"
        }
    ]
}

     */



    /*
    {
	"city":"",
	"country":"origin country1"
}


{
    "timestamp": "2019-02-28T22:36:30.459+0000",
    "path": "/api/route-info/",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed for argument at index 0 in method: public reactor.core.publisher.Mono<com.adidas.chriniko.routesservice.dto.RouteInfo> com.adidas.chriniko.routesservice.resource.RouteResource.find(com.adidas.chriniko.routesservice.dto.CityInfo), with 1 error(s): [Field error in object 'cityInfo' on field 'city': rejected value []; codes [NotBlank.cityInfo.city,NotBlank.city,NotBlank.java.lang.String,NotBlank]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [cityInfo.city,city]; arguments []; default message [city]]; default message [must not be blank]] ",
    "errors": [
        {
            "codes": [
                "NotBlank.cityInfo.city",
                "NotBlank.city",
                "NotBlank.java.lang.String",
                "NotBlank"
            ],
            "arguments": [
                {
                    "codes": [
                        "cityInfo.city",
                        "city"
                    ],
                    "arguments": null,
                    "defaultMessage": "city",
                    "code": "city"
                }
            ],
            "defaultMessage": "must not be blank",
            "objectName": "cityInfo",
            "field": "city",
            "rejectedValue": "",
            "bindingFailure": false,
            "code": "NotBlank"
        }
    ]
}
     */




    /*
    {
    "city": {
        "name": "origin 1",
        "country": "origin country 1"
    },
    "destinyCity": {
        "name": "destiny 1",
        "country": "destiny country 1"
    },
    "departureTime": "2019-02-28T22:55:23Z",
    "arrivalTime": "2019-03-01T00:55:23Z"
}


{
    "timestamp": "2019-02-28T22:59:58.724+0000",
    "path": "/api/route-info/create",
    "status": 400,
    "error": "Bad Request",
    "message": "record already exists with info: RouteEntity(id=421708c1-b129-48d9-82cd-5bee627133fd, originCityName=origin 1, originCountry=origin country 1, destinyCityName=destiny 1, destinyCountry=destiny country 1, departureTime=2019-02-28T22:59:56Z, arrivalTime=2019-03-01T00:59:56Z)"
}
     */

}
