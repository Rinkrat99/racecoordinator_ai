import { ComponentFixture, TestBed } from "@angular/core/testing";
import { DriverHeatData } from "@app/race/driver_heat_data";
import { GhostPacingService } from "@app/services/ghost-pacing.service";

import { GhostTrajectoryDialogComponent } from "./ghost-trajectory-dialog.component";

describe("GhostTrajectoryDialogComponent", () => {
  let component: GhostTrajectoryDialogComponent;
  let fixture: ComponentFixture<GhostTrajectoryDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GhostTrajectoryDialogComponent],
      providers: [GhostPacingService],
    }).compileComponents();

    fixture = TestBed.createComponent(GhostTrajectoryDialogComponent);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should not render backdrop when visible is false", () => {
    fixture.componentRef.setInput("visible", false);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector(".trajectory-modal-backdrop")).toBeNull();
  });

  it("should render dialog and driver names when visible is true", () => {
    const mockDriverA = {
      driver: { name: "Lewis Hamilton" },
      lapTimes: [5.2, 5.0, 4.9],
    } as unknown as DriverHeatData;

    const mockDriverB = {
      driver: { name: "Max Verstappen" },
      lapTimes: [5.1, 5.3, 4.8],
    } as unknown as DriverHeatData;

    fixture.componentRef.setInput("visible", true);
    fixture.componentRef.setInput("driverA", mockDriverA);
    fixture.componentRef.setInput("driverB", mockDriverB);
    fixture.detectChanges();

    expect(component.driverAName()).toBe("Lewis Hamilton");
    expect(component.driverBName()).toBe("Max Verstappen");
    expect(component.comparison().points.length).toBe(3);

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector(".trajectory-modal-backdrop")).toBeTruthy();
    expect(compiled.textContent).toContain("Lewis Hamilton");
    expect(compiled.textContent).toContain("Max Verstappen");
  });

  it("should fallback to benchmark ghost name when driverB is null", () => {
    const mockDriverA = {
      driver: { name: "Charles Leclerc" },
      lapTimes: [5.0, 5.0],
    } as unknown as DriverHeatData;

    fixture.componentRef.setInput("visible", true);
    fixture.componentRef.setInput("driverA", mockDriverA);
    fixture.componentRef.setInput("driverB", null);
    fixture.componentRef.setInput("benchmarkLapTime", 4.95);
    fixture.detectChanges();

    expect(component.driverBName()).toContain("Ghost (4.95s)");
    expect(component.comparison().points.length).toBe(2);
  });

  it("should emit close event when dismiss or close button is clicked", () => {
    let closed = false;
    component.close.subscribe(() => {
      closed = true;
    });

    fixture.componentRef.setInput("visible", true);
    fixture.detectChanges();

    const closeBtn = fixture.nativeElement.querySelector(
      ".close-btn",
    ) as HTMLButtonElement;
    closeBtn?.click();

    expect(closed).toBeTrue();
  });
});
