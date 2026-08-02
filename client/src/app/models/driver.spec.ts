import { Driver, EMPTY_DRIVER_ID } from "./driver";

describe("Driver Model", () => {
  it("should identify EMPTY_DRIVER_ID correctly", () => {
    const emptyDriver = new Driver(EMPTY_DRIVER_ID, "Empty", "Empty");
    expect(emptyDriver.isEmpty()).toBeTrue();
    expect(Driver.isEmpty(EMPTY_DRIVER_ID)).toBeTrue();
  });

  it("should return true for null or undefined drivers", () => {
    expect(Driver.isEmpty(null)).toBeTrue();
    expect(Driver.isEmpty(undefined)).toBeTrue();
    expect(Driver.isEmpty("")).toBeTrue();
  });

  it("should return false for valid drivers with entity_id", () => {
    const driver = new Driver("d_alice_123", "Alice", "The Rocket");
    expect(driver.isEmpty()).toBeFalse();
    expect(Driver.isEmpty(driver)).toBeFalse();
    expect(driver.entity_id).toBe("d_alice_123");
  });

  it("should return false for transient driver objects with a name but no id", () => {
    const mockDriver = { name: "Mock Driver" };
    expect(Driver.isEmpty(mockDriver)).toBeFalse();
  });
});
