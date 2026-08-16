package com.antigravity.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.BufferOverflowException;
import org.junit.Before;
import org.junit.Test;

public class CircularBufferTest {

  private CircularBuffer buffer;

  @Before
  public void setUp() {
    buffer = new CircularBuffer(10);
  }

  @Test
  public void testEmptyBuffer() {
    assertEquals("", buffer.toHexString());
    assertEquals(0, buffer.size());
    assertEquals(10, buffer.capacity());
    assertTrue(buffer.isEmpty());
    assertFalse(buffer.isFull());
  }

  @Test
  public void testSingleByte() {
    assertTrue(buffer.add((byte) 0xAB));
    assertEquals(1, buffer.size());
    assertFalse(buffer.isEmpty());
    assertEquals("AB", buffer.toHexString());
    assertEquals((byte) 0xAB, buffer.peek(0));
    assertEquals((byte) 0xAB, buffer.get());
    assertTrue(buffer.isEmpty());
  }

  @Test
  public void testMultipleBytes() {
    int written = buffer.write(new byte[] {0x01, 0x02, 0x0F, (byte) 0xFF});
    assertEquals(4, written);
    assertEquals("01 02 0F FF", buffer.toHexString());
    assertEquals(4, buffer.size());
  }

  @Test
  public void testWrapAround() {
    // Fill 8 bytes
    buffer.write(new byte[] {0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88});
    // Consume 5 bytes
    byte[] readBytes = buffer.read(5);
    assertEquals(5, readBytes.length);
    assertEquals(0x11, readBytes[0]);
    assertEquals((byte) 0x55, readBytes[4]);

    // Write 4 more bytes to force wrap around (capacity is 10)
    buffer.write(new byte[] {(byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD});

    // Current content should be 66 77 88 AA BB CC DD
    assertEquals("66 77 88 AA BB CC DD", buffer.toHexString());
    assertEquals((byte) 0x66, buffer.peek(0));
    assertEquals((byte) 0xDD, buffer.peek(6));
  }

  @Test
  public void testFullBuffer() {
    byte[] data = new byte[10];
    for (int i = 0; i < 10; i++) {
      data[i] = (byte) i;
    }
    buffer.write(data);
    assertTrue(buffer.isFull());
    assertFalse(buffer.add((byte) 0x99)); // Fails because full
    assertEquals("00 01 02 03 04 05 06 07 08 09", buffer.toHexString());
  }

  @Test
  public void testClear() {
    buffer.write(new byte[] {0x01, 0x02, 0x03});
    assertEquals(3, buffer.size());
    buffer.clear();
    assertEquals(0, buffer.size());
    assertTrue(buffer.isEmpty());
    assertEquals("", buffer.toHexString());
  }

  @Test(expected = IllegalStateException.class)
  public void testGetOnEmptyBufferThrows() {
    buffer.get();
  }

  @Test(expected = BufferOverflowException.class)
  public void testWriteOverflowThrows() {
    byte[] overflow = new byte[11];
    buffer.write(overflow);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testPeekNegativeOffsetThrows() {
    buffer.add((byte) 0x01);
    buffer.peek(-1);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void testPeekOutOfBoundsThrows() {
    buffer.add((byte) 0x01);
    buffer.peek(1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidCapacityThrows() {
    new CircularBuffer(0);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeCapacityThrows() {
    new CircularBuffer(-5);
  }
}
