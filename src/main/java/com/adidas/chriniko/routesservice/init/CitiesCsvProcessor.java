package com.adidas.chriniko.routesservice.init;

import com.adidas.chriniko.routesservice.error.ProcessingException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Log4j2

@Component
public class CitiesCsvProcessor {

    private static final String[] HEADERS = {"city", "city_ascii", "lat", "lng", "country", "iso2", "iso3", "admin_name", "capital", "population", "id"};
    private static final boolean DISPLAY_PARSING_INFO = false;

    public Map<String, List<String>> getCitiesByCountry() {

        try {
            URL resource = this.getClass().getClassLoader().getResource("cities/worldcities.csv");
            Path path = Paths.get(Objects.requireNonNull(resource).toURI());
            try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {

                Iterable<CSVRecord> records = CSVFormat.DEFAULT
                        .withHeader(HEADERS)
                        .withFirstRecordAsHeader()
                        .parse(bufferedReader);

                int sum = 0;

                final Map<String /*country*/, List<String>> citiesGroupByCountry = new LinkedHashMap<>();

                for (CSVRecord record : records) {
                    sum++;

                    String country = record.get("country");

                    citiesGroupByCountry.computeIfPresent(
                            country,
                            (_country, cities) -> {
                                cities.add(record.get("city"));
                                return cities;
                            }
                    );

                    citiesGroupByCountry.computeIfAbsent(
                            country,
                            _country -> {
                                List<String> cities = new LinkedList<>();
                                cities.add(record.get("city"));
                                return cities;
                            }
                    );
                }

                log.debug("total csv records: {}", sum);

                if (DISPLAY_PARSING_INFO) {
                    log.debug("countries: {}", citiesGroupByCountry.keySet());
                    log.debug("countries.size(): {}", citiesGroupByCountry.keySet().size());
                    citiesGroupByCountry.forEach((city, countries) -> log.debug("city: {} --- countries: {}", city, countries));
                }

                return citiesGroupByCountry;
            }
        } catch (Exception error) {
            String msg = "error occurred during reading of world cities csv";
            log.error(msg, error);
            throw new ProcessingException(msg, error);
        }
    }

}
