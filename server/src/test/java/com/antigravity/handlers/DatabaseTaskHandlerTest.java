package com.antigravity.handlers;

import static org.junit.Assert.*;

import com.antigravity.models.Race;
import com.antigravity.models.Track;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

public class DatabaseTaskHandlerTest {

  @Test
  public void testRaceResponseSerialization() throws Exception {
    Race race = new Race.Builder().withName("Test Race").withAdjustDriftLaps(true).build();
    Track track = new Track.Builder().name("Test Track").build();

    DatabaseTaskHandler.RaceResponse response = new DatabaseTaskHandler.RaceResponse(race, track);
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(response);

    assertTrue("JSON must flatten Race properties", json.contains("\"adjust_drift_laps\":true"));
    assertTrue("JSON must flatten Race properties", json.contains("\"name\":\"Test Race\""));
    assertTrue("JSON must include Track object under 'track'", json.contains("\"track\":{\"@id\""));
  }
}
