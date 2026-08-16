import { ComponentFixture, TestBed } from "@angular/core/testing";
import { DriverHeatData } from "@app/race/driver_heat_data";
import { GhostPacingService } from "@app/services/ghost-pacing.service";
import { TranslationService } from "@app/services/translation.service";

import { RacedayGhostPacingComponent } from "./raceday-ghost-pacing.component";

describe("RacedayGhostPacingComponent", () => {
  let component: RacedayGhostPacingComponent;
  let fixture: ComponentFixture<RacedayGhostPacingComponent>;

  beforeEach(async () => {
    const mockTranslationService = {
      translate: jasmine.createSpy("translate").and.callFake((key: string) => {
        const map: Record<string, string> = {
          RD_GHOST_LANE_RECORD: "Lane Record",
          RD_GHOST_PERSONAL_BEST: "Personal Best",
          RD_GHOST_HEAT_LEADER: "Heat Leader",
          RD_GHOST_NO_BENCHMARK: "No ghost benchmark",
          RD_GHOST_TELEMETRY_POSITION: "Live Telemetry Position",
        };
        return map[key] || key;
      }),
    };

    await TestBed.configureTestingModule({
      imports: [RacedayGhostPacingComponent],
      providers: [
        GhostPacingService,
        { provide: TranslationService, useValue: mockTranslationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(RacedayGhostPacingComponent);
    component = fixture.componentInstance;
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should render empty state when targetGhostLapTime is 0", () => {
    fixture.componentRef.setInput("laneRecord", 0);
    fixture.componentRef.setInput("personalBest", 0);
    fixture.detectChanges();

    expect(component.targetGhostLapTime()).toBe(0);
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector(".pacing-empty")).toBeTruthy();
  });

  it("should compute ghost gap and format ahead delta correctly", () => {
    const mockHd = {
      laneIndex: 0,
      currentLapTime: 2.0,
      driver: { name: "Driver 1" },
    } as unknown as DriverHeatData;

    fixture.componentRef.setInput("driverHeatData", mockHd);
    fixture.componentRef.setInput("laneRecord", 5.0);
    fixture.componentRef.setInput("benchmarkType", "LANE_RECORD");
    fixture.componentRef.setInput("lapProgress", 0.4);
    fixture.detectChanges();

    expect(component.targetGhostLapTime()).toBe(5.0);
    expect(component.benchmarkLabel()).toBe("Lane Record");
    expect(component.progressWidthPct()).toBe(40);
  });

  it("should render ahead badge with green formatting when delta is positive", () => {
    // Current lap elapsed is 1.5s out of expected 2.5s (50% of 5.0s ghost)
    const mockHd = {
      laneIndex: 0,
      currentLapTime: 1.5,
      driver: { name: "Driver 1" },
    } as unknown as DriverHeatData;

    fixture.componentRef.setInput("driverHeatData", mockHd);
    fixture.componentRef.setInput("laneRecord", 5.0);
    fixture.componentRef.setInput("lapProgress", 0.5);
    fixture.detectChanges();

    expect(component.ghostGap().isAhead).toBeTrue();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector(".delta-badge.ahead")).toBeTruthy();
    expect(component.formattedDelta()).toContain("+");
  });

  it("should format behind delta when driver is slower than ghost", () => {
    // Current lap elapsed is 4.0s out of expected 3.0s (60% of 5.0s ghost)
    const mockHd = {
      laneIndex: 0,
      currentLapTime: 4.0,
      driver: { name: "Driver 1" },
    } as unknown as DriverHeatData;

    fixture.componentRef.setInput("driverHeatData", mockHd);
    fixture.componentRef.setInput("laneRecord", 5.0);
    fixture.componentRef.setInput("lapProgress", 0.6);
    fixture.detectChanges();

    expect(component.ghostGap().isAhead).toBeFalse();
    expect(component.formattedDelta()).toContain("-");
  });

  it("should render -- for empty driver lanes", () => {
    const mockEmptyHd = {
      laneIndex: 0,
      isEmpty: true,
    } as unknown as DriverHeatData;

    fixture.componentRef.setInput("driverHeatData", mockEmptyHd);
    fixture.componentRef.setInput("laneRecord", 5.0);
    fixture.detectChanges();

    expect(component.isEmptyDriver()).toBeTrue();
    expect(component.targetGhostLapTime()).toBe(0);
    expect(component.formattedDelta()).toBe("--");
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain("--");
  });

  it("should support Personal Best and Heat Leader benchmark types", () => {
    const mockHd = {
      laneIndex: 0,
      driver: { name: "Driver 1" },
    } as unknown as DriverHeatData;
    fixture.componentRef.setInput("driverHeatData", mockHd);
    fixture.componentRef.setInput("benchmarkType", "PERSONAL_BEST");
    fixture.componentRef.setInput("personalBest", 4.8);
    fixture.detectChanges();
    expect(component.benchmarkLabel()).toBe("Personal Best");
    expect(component.targetGhostLapTime()).toBe(4.8);

    fixture.componentRef.setInput("benchmarkType", "HEAT_LEADER");
    fixture.componentRef.setInput("heatLeaderBest", 4.5);
    fixture.detectChanges();
    expect(component.benchmarkLabel()).toBe("Heat Leader");
    expect(component.targetGhostLapTime()).toBe(4.5);
  });
});
