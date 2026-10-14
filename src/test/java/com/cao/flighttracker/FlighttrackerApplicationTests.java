package com.cao.flighttracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "flighttracker.collector.enabled=false")
class FlighttrackerApplicationTests {

    @Test
    void contextLoads() {
    }
}