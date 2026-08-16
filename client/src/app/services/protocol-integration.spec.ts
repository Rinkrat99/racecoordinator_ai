import { HttpClientTestingModule } from "@angular/common/http/testing";
import { fakeAsync, flush, TestBed, tick } from "@angular/core/testing";
import { DataService } from "@app/data.service";
import { Driver } from "@app/models/driver";
import { Race } from "@app/models/race";
import { Track } from "@app/models/track";
import {
  ICarData,
  IGroupStandingsUpdate,
  IHeatPositionUpdate,
  ILap,
  InterfaceEvent,
  InterfaceStatus,
  IOverallStandingsUpdate,
  IRecordData,
  ISegment,
  IStandingsUpdate,
  Lap,
  RaceFlag,
  RaceState,
} from "@app/proto/antigravity";
import { DriverHeatData } from "@app/race/driver_heat_data";
import { Heat } from "@app/race/heat";
import { RaceParticipant } from "@app/race/race_participant";

import { LoggerService } from "./logger.service";
import { RaceService } from "./race.service";
import {
  IReactionTime,
  RaceConnectionService,
} from "./race-connection.service";

describe("Protocol Integration End-to-End Test Suite", () => {
  let dataService: DataService;
  let raceConnectionService: RaceConnectionService;
  let raceService: RaceService;

  let activeSockets: any[] = [];

  beforeEach(() => {
    activeSockets = [];

    function MockWs(this: any) {
      this.binaryType = "arraybuffer";
      this.readyState = 1; // OPEN
      this.onopen = null;
      this.onmessage = null;
      this.onclose = null;
      this.onerror = null;
      this.send = jasmine.createSpy("send");
      this.close = jasmine.createSpy("close");
      activeSockets.push(this);
    }

    spyOn(window as any, "WebSocket").and.callFake(MockWs as any);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        DataService,
        RaceService,
        RaceConnectionService,
        LoggerService,
      ],
    });

    dataService = TestBed.inject(DataService);
    raceService = TestBed.inject(RaceService);
    raceConnectionService = TestBed.inject(RaceConnectionService);
  });

  afterEach(() => {
    raceConnectionService.disconnect(true);
    dataService.disconnectFromInterfaceDataSocket();
  });

  describe("1. Multi-Heat Race Simulation & Telemetry Processing", () => {
    it("should process multi-heat progression, lap recording, and standings across heats", fakeAsync(() => {
      // 1. Setup multi-heat race models
      const driver1 = new Driver("d1", "Driver 1", "D1");
      const driver2 = new Driver("d2", "Driver 2", "D2");

      const p1 = new RaceParticipant("d1", driver1);
      const p2 = new RaceParticipant("d2", driver2);

      const d1HeatData = new DriverHeatData("d1", p1, 0, driver1);
      const d2HeatData = new DriverHeatData("d2", p2, 1, driver2);

      const heat1 = new Heat("h1", 1, [d1HeatData, d2HeatData], [], false);
      const heat2 = new Heat("h2", 2, [d2HeatData, d1HeatData], [], false);

      const mockTrack = new Track({
        entity_id: "t1",
        name: "Test Track",
        lanes: [],
      });

      const raceModel = new Race("race_e2e_1", "Grand Prix E2E", mockTrack);

      // Connect connection service
      raceConnectionService.connect();
      tick();

      raceService.setRace(raceModel);
      raceService.setHeats([heat1, heat2]);
      raceService.setCurrentHeat(heat1);

      expect(raceService.getRace()?.name).toBe("Grand Prix E2E");
      expect(raceService.getCurrentHeat()?.heatNumber).toBe(1);

      // Track received telemetry
      const recordedLaps: ILap[] = [];
      const recordedStandings: IStandingsUpdate[] = [];
      const recordedFlags: RaceFlag[] = [];

      raceConnectionService.laps$.subscribe((lap) => recordedLaps.push(lap));
      raceConnectionService.standingsUpdate$.subscribe((s) =>
        recordedStandings.push(s),
      );
      raceConnectionService.raceFlag$.subscribe((f) => recordedFlags.push(f));

      // 2. Start Heat 1 -> Flag Green, Race State RACING
      (dataService as any).flagSubject.next(RaceFlag.GREEN);
      (dataService as any).raceStateSubject.next(RaceState.RACING);
      tick();

      expect(recordedFlags).toContain(RaceFlag.GREEN);

      // 3. Stream Laps for Heat 1
      const lap1Driver1: ILap = {
        objectId: "d1",
        driverId: "d1",
        lapNumber: 1,
        lapTime: 3.456,
        bestLapTime: 3.456,
        type: Lap.LapType.LAP,
      };
      const lap1Driver2: ILap = {
        objectId: "d2",
        driverId: "d2",
        lapNumber: 1,
        lapTime: 3.612,
        bestLapTime: 3.612,
        type: Lap.LapType.LAP,
      };

      (dataService as any).lapSubject.next(lap1Driver1);
      (dataService as any).lapSubject.next(lap1Driver2);
      tick();

      expect(recordedLaps.length).toBe(2);
      expect(recordedLaps[0].driverId).toBe("d1");
      expect(recordedLaps[1].driverId).toBe("d2");

      // 4. Update Standings
      const updates: IHeatPositionUpdate[] = [
        { objectId: "d1", rank: 1 },
        { objectId: "d2", rank: 2 },
      ];
      const standingsUpdate1: IStandingsUpdate = {
        updates: updates,
      };
      (dataService as any).standingsSubject.next(standingsUpdate1);
      tick();

      expect(recordedStandings.length).toBe(1);
      expect(raceConnectionService.driverRankings.get("d1")).toBe(1);
      expect(raceConnectionService.driverRankings.get("d2")).toBe(2);

      // 5. Complete Heat 1 -> Yellow then Checkered
      (dataService as any).flagSubject.next(RaceFlag.YELLOW);
      tick();
      expect(recordedFlags).toContain(RaceFlag.YELLOW);

      (dataService as any).flagSubject.next(RaceFlag.CHECKERED);
      (dataService as any).raceStateSubject.next(RaceState.HEAT_OVER);
      tick();
      expect(recordedFlags).toContain(RaceFlag.CHECKERED);

      // 6. Advance to Heat 2
      raceService.setCurrentHeat(heat2);
      expect(raceService.getCurrentHeat()?.heatNumber).toBe(2);

      (dataService as any).flagSubject.next(RaceFlag.GREEN);
      (dataService as any).raceStateSubject.next(RaceState.RACING);
      tick();

      const heat2Lap1: ILap = {
        objectId: "d2",
        driverId: "d2",
        lapNumber: 1,
        lapTime: 3.205,
        bestLapTime: 3.205,
        type: Lap.LapType.LAP,
      };
      (dataService as any).lapSubject.next(heat2Lap1);
      tick();

      expect(recordedLaps.length).toBe(3);
      expect(recordedLaps[2].lapTime).toBe(3.205);
    }));
  });

  describe("2. Fuel Telemetry and Pit Stop State Flow", () => {
    it("should process fuel consumption, pit entry/exit, refueling, and low fuel states", fakeAsync(() => {
      raceConnectionService.connect();
      tick();

      const driver1 = new Driver("d1", "Driver 1", "D1");
      const p1 = new RaceParticipant("d1", driver1);
      const d1HeatData = new DriverHeatData("d1", p1, 0, driver1);
      const heat1 = new Heat("h1", 1, [d1HeatData], [], false);

      raceService.setCurrentHeat(heat1);

      const receivedCarData: ICarData[] = [];
      raceConnectionService.carData$.subscribe((cd) =>
        receivedCarData.push(cd),
      );

      // 1. Initial full fuel state (100%)
      const fullFuel: ICarData = {
        lane: 0,
        fuelLevel: 100,
        isRefueling: false,
        controllerThrottlePct: 100,
        carThrottlePct: 100,
      };
      (dataService as any).carDataSubject.next(fullFuel);
      tick();

      expect(receivedCarData.length).toBe(1);
      expect(receivedCarData[0].fuelLevel).toBe(100);
      expect(receivedCarData[0].isRefueling).toBeFalse();

      // 2. Telemetry stream - Fuel consumption over laps
      const midFuel: ICarData = {
        lane: 0,
        fuelLevel: 45,
        isRefueling: false,
      };
      (dataService as any).carDataSubject.next(midFuel);
      tick();

      const lowFuel: ICarData = {
        lane: 0,
        fuelLevel: 12,
        isRefueling: false,
      };
      (dataService as any).carDataSubject.next(lowFuel);
      tick();

      expect(receivedCarData.length).toBe(3);
      expect(receivedCarData[2].fuelLevel).toBe(12);

      // 3. Pit Entry & Refueling
      const pitEntry: ICarData = {
        lane: 0,
        fuelLevel: 10,
        isRefueling: true,
      };
      (dataService as any).carDataSubject.next(pitEntry);
      tick();

      expect(receivedCarData[3].isRefueling).toBeTrue();

      // 4. Refueling progression to 95%
      const refueled: ICarData = {
        lane: 0,
        fuelLevel: 95,
        isRefueling: true,
      };
      (dataService as any).carDataSubject.next(refueled);
      tick();

      // 5. Pit Exit
      const pitExit: ICarData = {
        lane: 0,
        fuelLevel: 95,
        isRefueling: false,
      };
      (dataService as any).carDataSubject.next(pitExit);
      tick();

      expect(receivedCarData.length).toBe(6);
      expect(receivedCarData[5].isRefueling).toBeFalse();
      expect(receivedCarData[5].fuelLevel).toBe(95);
    }));
  });

  describe("3. WebSocket Heartbeat, Watchdog & Reconnection Flow", () => {
    it("should handle interface status transitions, watchdog disconnects, and automatic reconnection", fakeAsync(() => {
      // Connect to race data socket and interface data socket
      dataService.connectToRaceDataSocket();
      dataService.connectToInterfaceDataSocket();
      tick();

      const raceSocket = activeSockets[0];
      const interfaceSocket = activeSockets[1];
      expect(raceSocket).toBeDefined();
      expect(interfaceSocket).toBeDefined();

      let isSocketActive = false;
      dataService.socketConnected$.subscribe((connected) => {
        isSocketActive = connected;
      });

      if (raceSocket.onopen) {
        raceSocket.onopen();
      }
      if (interfaceSocket.onopen) {
        interfaceSocket.onopen();
      }
      tick();
      expect(isSocketActive).toBeTrue();

      // 1. Simulate interface event (Status CONNECTED)
      const connectEvent = InterfaceEvent.encode({
        status: { status: InterfaceStatus.CONNECTED },
      }).finish();
      const base64Connect = btoa(
        String.fromCharCode(...Array.from(connectEvent)),
      );
      if (interfaceSocket.onmessage) {
        interfaceSocket.onmessage({ data: `"${base64Connect}"` });
      }
      tick();

      // 2. Stream Laps over WebSocket Base64 Protobuf
      const lapProto = Lap.encode({
        driverId: "d1",
        lapNumber: 5,
        lapTime: 3.123,
        bestLapTime: 3.123,
        type: Lap.LapType.LAP,
      }).finish();
      const base64Lap = btoa(String.fromCharCode(...Array.from(lapProto)));
      if (interfaceSocket.onmessage) {
        interfaceSocket.onmessage({ data: `"${base64Lap}"` });
      }
      tick();

      // 3. Simulate Socket Disconnect / Close
      if (raceSocket.onclose) {
        raceSocket.onclose({ code: 1006, reason: "Abnormal Closure" });
      }
      tick();
      expect(isSocketActive).toBeFalse();

      // 4. Simulate Reconnect via connectToRaceDataSocket
      dataService.connectToRaceDataSocket();
      tick();

      const reconnectedRaceSocket = activeSockets[activeSockets.length - 1];
      expect(reconnectedRaceSocket).toBeDefined();

      if (reconnectedRaceSocket.onopen) {
        reconnectedRaceSocket.onopen();
      }
      tick();
      expect(isSocketActive).toBeTrue();
      flush();
    }));
  });

  describe("4. Yellow Flag Caution, Pause & Resume State Flow", () => {
    it("should process yellow flag cautions, paused states, and race resumption", fakeAsync(() => {
      raceConnectionService.connect();
      tick();

      const receivedFlags: RaceFlag[] = [];
      const receivedStates: RaceState[] = [];

      raceConnectionService.raceFlag$.subscribe((f) => receivedFlags.push(f));
      raceConnectionService.raceState$.subscribe((s) => receivedStates.push(s));

      // 1. Green Flag / Racing
      (dataService as any).flagSubject.next(RaceFlag.GREEN);
      (dataService as any).raceStateSubject.next(RaceState.RACING);
      tick();

      // 2. Caution Triggered -> Yellow Flag & Paused
      (dataService as any).flagSubject.next(RaceFlag.YELLOW);
      (dataService as any).raceStateSubject.next(RaceState.PAUSED);
      tick();

      expect(receivedFlags).toContain(RaceFlag.YELLOW);
      expect(receivedStates).toContain(RaceState.PAUSED);

      // 3. Resumption -> Green Flag & Racing
      (dataService as any).flagSubject.next(RaceFlag.GREEN);
      (dataService as any).raceStateSubject.next(RaceState.RACING);
      tick();

      expect(receivedFlags[receivedFlags.length - 1]).toBe(RaceFlag.GREEN);
      expect(receivedStates[receivedStates.length - 1]).toBe(RaceState.RACING);
    }));
  });

  describe("5. Reaction Time, Min Lap Time & Sector Segments", () => {
    it("should process reaction times, minimum lap time drops, and sector times", fakeAsync(() => {
      raceConnectionService.connect();
      tick();

      const driver1 = new Driver("d1", "Driver 1", "D1");
      const p1 = new RaceParticipant("d1", driver1);
      const d1HeatData = new DriverHeatData("d1", p1, 0, driver1);
      const heat1 = new Heat("h1", 1, [d1HeatData], [], false);

      raceService.setCurrentHeat(heat1);

      const receivedReactionTimes: IReactionTime[] = [];
      const receivedSegments: ISegment[] = [];
      const receivedLaps: ILap[] = [];

      raceConnectionService.reactionTimes$.subscribe((rt) =>
        receivedReactionTimes.push(rt),
      );
      raceConnectionService.segments$.subscribe((seg) =>
        receivedSegments.push(seg),
      );
      raceConnectionService.laps$.subscribe((lap) => receivedLaps.push(lap));

      // 1. Reaction Time event at heat start
      const reactionLap: ILap = {
        objectId: "d1",
        driverId: "d1",
        lapTime: 0.342,
        type: Lap.LapType.REACTION_TIME,
        interfaceId: 1,
      };
      (dataService as any).lapSubject.next(reactionLap);
      tick();

      expect(receivedReactionTimes.length).toBe(1);
      expect(receivedReactionTimes[0].reactionTime).toBe(0.342);
      expect(d1HeatData.reactionTime).toBe(0.342);

      // 2. Sector / Segment times
      const seg1: ISegment = {
        objectId: "d1",
        segmentNumber: 1,
        segmentTime: 1.12,
      };
      const seg2: ISegment = {
        objectId: "d1",
        segmentNumber: 2,
        segmentTime: 1.15,
      };
      (dataService as any).segmentSubject.next(seg1);
      (dataService as any).segmentSubject.next(seg2);
      tick();

      expect(receivedSegments.length).toBe(2);
      expect(d1HeatData.currentLapSegments.length).toBe(2);

      // 3. Min lap time violation
      const minLap: ILap = {
        objectId: "d1",
        driverId: "d1",
        lapTime: 0.85,
        type: Lap.LapType.MIN_LAP_TIME,
      };
      (dataService as any).lapSubject.next(minLap);
      tick();

      expect(receivedLaps).toContain(minLap);
    }));
  });

  describe("6. Overall Standings, Group Standings & Track Records", () => {
    it("should process group standings, overall series standings, and track record updates", fakeAsync(() => {
      raceConnectionService.connect();
      tick();

      const receivedGroupStandings: IGroupStandingsUpdate[] = [];
      const receivedOverallStandings: IOverallStandingsUpdate[] = [];
      const receivedRecordData: (IRecordData | null)[] = [];

      raceConnectionService.groupStandingsUpdate$.subscribe((g) =>
        receivedGroupStandings.push(g),
      );
      raceConnectionService.overallStandingsUpdate$.subscribe((o) =>
        receivedOverallStandings.push(o),
      );
      raceConnectionService.recordData$.subscribe((r) =>
        receivedRecordData.push(r),
      );

      // 1. Group standings update
      const groupUpdate: IGroupStandingsUpdate = {
        group: 1,
        participants: [{ objectId: "d1", rank: 1 }],
      };
      (dataService as any).groupStandingsSubject.next(groupUpdate);
      tick();

      expect(receivedGroupStandings.length).toBe(1);
      expect(receivedGroupStandings[0].group).toBe(1);

      // 2. Overall Standings update
      const overallUpdate: IOverallStandingsUpdate = {
        participants: [{ objectId: "d1", rank: 1 }],
      };
      (dataService as any).overallStandingsSubject.next(overallUpdate);
      tick();

      expect(receivedOverallStandings.length).toBe(1);
      expect(receivedOverallStandings[0].participants?.length).toBe(1);

      // 3. Record Data update (Track / Heat records)
      const recordUpdate: IRecordData = {
        overall: {},
        current: {},
      };
      (dataService as any).recordDataSubject.next(recordUpdate);
      tick();

      expect(receivedRecordData).toContain(recordUpdate);
    }));
  });
});
