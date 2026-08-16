package com.antigravity.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NetworkUtilsTest {

  @Test
  public void testInstantiation() {
    NetworkUtils nu = new NetworkUtils();
    assertNotNull(nu);
  }

  @Test
  public void testIsLocalAddress_Localhost_IPv4() {
    assertTrue(NetworkUtils.isLocalAddress("127.0.0.1", "localhost"));
    assertTrue(NetworkUtils.isLocalAddress("127.0.0.1"));
  }

  @Test
  public void testIsLocalAddress_Localhost_IPv6() {
    assertTrue(NetworkUtils.isLocalAddress("::1", "localhost"));
    assertTrue(NetworkUtils.isLocalAddress("::1"));
    assertTrue(NetworkUtils.isLocalAddress("0:0:0:0:0:0:0:1", "localhost"));
  }

  @Test
  public void testIsLocalAddress_Localhost_Strings() {
    assertTrue(NetworkUtils.isLocalAddress("localhost", "localhost"));
    assertTrue(NetworkUtils.isLocalAddress("localhost"));
  }

  @Test
  public void testIsLocalAddress_LAN_IPv4_192_168() {
    assertTrue(NetworkUtils.isLocalAddress("192.168.1.1", null));
    assertTrue(NetworkUtils.isLocalAddress("192.168.0.100", null));
    assertTrue(NetworkUtils.isLocalAddress("192.168.255.255", null));
    assertTrue(NetworkUtils.isLocalAddress("192.168.1.1"));
  }

  @Test
  public void testIsLocalAddress_LAN_IPv4_10x() {
    assertTrue(NetworkUtils.isLocalAddress("10.0.0.1", null));
    assertTrue(NetworkUtils.isLocalAddress("10.255.255.255", null));
    assertTrue(NetworkUtils.isLocalAddress("10.0.0.50", null));
    assertTrue(NetworkUtils.isLocalAddress("10.0.0.1"));
  }

  @Test
  public void testIsLocalAddress_LAN_IPv4_172x() {
    assertTrue(NetworkUtils.isLocalAddress("172.16.0.1", null));
    assertTrue(NetworkUtils.isLocalAddress("172.20.5.100", null));
    assertTrue(NetworkUtils.isLocalAddress("172.31.255.255", null));
    assertTrue(NetworkUtils.isLocalAddress("172.16.0.1"));
  }

  @Test
  public void testIsLocalAddress_NotLocal_PublicIP() {
    assertFalse(NetworkUtils.isLocalAddress("8.8.8.8", null));
    assertFalse(NetworkUtils.isLocalAddress("1.1.1.1", null));
    assertFalse(NetworkUtils.isLocalAddress("203.0.113.1", null));
    assertFalse(NetworkUtils.isLocalAddress("8.8.8.8"));
  }

  @Test
  public void testIsLocalAddress_NotLocal_172_OutOfRange() {
    // 172.15.x.x and 172.32.x.x are NOT private
    assertFalse(NetworkUtils.isLocalAddress("172.15.0.1", null));
    assertFalse(NetworkUtils.isLocalAddress("172.32.0.1", null));
  }

  @Test
  public void testIsLocalAddress_SpecialAddresses() {
    assertTrue(NetworkUtils.isLocalAddress("0.0.0.0", null));
    assertTrue(NetworkUtils.isLocalAddress("::ffff:127.0.0.1", null));
  }

  @Test
  public void testIsLocalAddress_WithRemoteHost() {
    assertTrue(NetworkUtils.isLocalAddress("127.0.0.1", "127.0.0.1"));
    assertTrue(NetworkUtils.isLocalAddress("192.168.1.100", "mydevice.local"));
    assertTrue(NetworkUtils.isLocalAddress("some.remote.host", "localhost"));
    assertTrue(NetworkUtils.isLocalAddress("some.remote.host", "127.0.0.1"));
    assertTrue(NetworkUtils.isLocalAddress("some.remote.host", "::1"));
    assertTrue(NetworkUtils.isLocalAddress("some.remote.host", "0:0:0:0:0:0:0:1"));
  }

  @Test
  public void testIsLocalhost_IPv4() {
    assertTrue(NetworkUtils.isLocalhost("127.0.0.1", "localhost"));
    assertTrue(NetworkUtils.isLocalhost("127.0.0.1", null));
  }

  @Test
  public void testIsLocalhost_IPv6() {
    assertTrue(NetworkUtils.isLocalhost("::1", "localhost"));
    assertTrue(NetworkUtils.isLocalhost("::1", null));
    assertTrue(NetworkUtils.isLocalhost("0:0:0:0:0:0:0:1", "localhost"));
  }

  @Test
  public void testIsLocalhost_NotLocalNetwork() {
    assertFalse(NetworkUtils.isLocalhost("192.168.1.1", null));
    assertFalse(NetworkUtils.isLocalhost("10.0.0.1", null));
    assertFalse(NetworkUtils.isLocalhost("172.16.0.1", null));
  }

  @Test
  public void testIsLocalhost_InvalidAddressFallback() {
    assertFalse(NetworkUtils.isLocalhost("not-an-ip-or-host", null));
    assertTrue(NetworkUtils.isLocalhost("localhost", null));
  }

  @Test
  public void testIsLocalNetwork_LAN_IPv4_192_168() {
    assertTrue(NetworkUtils.isLocalNetwork("192.168.1.1"));
    assertTrue(NetworkUtils.isLocalNetwork("192.168.0.100"));
    assertTrue(NetworkUtils.isLocalNetwork("192.168.255.255"));
  }

  @Test
  public void testIsLocalNetwork_LAN_IPv4_10x() {
    assertTrue(NetworkUtils.isLocalNetwork("10.0.0.1"));
    assertTrue(NetworkUtils.isLocalNetwork("10.255.255.255"));
    assertTrue(NetworkUtils.isLocalNetwork("10.0.0.50"));
  }

  @Test
  public void testIsLocalNetwork_LAN_IPv4_172x() {
    assertTrue(NetworkUtils.isLocalNetwork("172.16.0.1"));
    assertTrue(NetworkUtils.isLocalNetwork("172.20.5.100"));
    assertTrue(NetworkUtils.isLocalNetwork("172.31.255.255"));
  }

  @Test
  public void testIsLocalNetwork_NotLocalhost() {
    assertFalse(NetworkUtils.isLocalNetwork("127.0.0.1"));
    assertFalse(NetworkUtils.isLocalNetwork("::1"));
    assertFalse(NetworkUtils.isLocalNetwork("localhost"));
  }

  @Test
  public void testIsLocalNetwork_NotPublicIP() {
    assertFalse(NetworkUtils.isLocalNetwork("8.8.8.8"));
    assertFalse(NetworkUtils.isLocalNetwork("1.1.1.1"));
    assertFalse(NetworkUtils.isLocalNetwork("203.0.113.1"));
  }

  @Test
  public void testIsLocalNetwork_InvalidAddress() {
    assertFalse(NetworkUtils.isLocalNetwork(null));
    assertFalse(NetworkUtils.isLocalNetwork("invalid.hostname.here"));
  }
}
