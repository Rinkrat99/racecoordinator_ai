package com.antigravity.protocols.interfaces;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;
import org.junit.Test;

public class SerialConnectionTest {

  @Test
  public void testGetAvailableSerialPorts() {
    List<String> ports = SerialConnection.getAvailableSerialPorts();
    assertNotNull("Available serial ports list should not be null", ports);
  }

  @Test
  public void testGetPortNames() {
    String[] names = SerialConnection.getPortNames();
    assertNotNull("Port names array should not be null", names);
  }

  @Test
  public void testConnectNonExistentPort() {
    SerialConnection connection = new SerialConnection();
    assertFalse(connection.isOpen());

    try {
      connection.connect("INVALID_PORT_NAME_123456789");
      fail("Connecting to non-existent port should throw IOException");
    } catch (IOException e) {
      assertTrue(e.getMessage().contains("Port not found"));
    }
  }

  private void assertTrue(boolean condition) {
    org.junit.Assert.assertTrue(condition);
  }
}
