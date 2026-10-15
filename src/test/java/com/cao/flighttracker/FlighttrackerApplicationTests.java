package com.cao.flighttracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "flighttracker.collector.enabled=false")
@Import(TestcontainersConfiguration.class)
class FlighttrackerApplicationTests {

    @Test
    void contextLoads() {
    }
}