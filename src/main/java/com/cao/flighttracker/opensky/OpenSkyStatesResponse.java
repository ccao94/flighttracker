package com.cao.flighttracker.opensky;

import java.util.List;

public record OpenSkyStatesResponse(Long time, List<List<Object>> states) {
}