import { RaceData } from "@app/proto/antigravity";

/**
 * Shared test helper for DriverResults component.
 * Handles both Jasmine/unit-test mock data and Playwright page injections.
 */
export class DriverResultsHelper {
  /**
   * Helper to construct fully compliant mock individual driver RaceData structure.
   */
  static createMockIndividualDriverData() {
    return {
      race: {
        race: {
          model: { entityId: "r1" },
          name: "Screendiff Grand Prix",
          track: {
            model: { entityId: "t1" },
            name: "Screendiff Raceway",
            lanes: [
              { backgroundColor: "#ef4444", foregroundColor: "#ffffff" },
              { backgroundColor: "#3b82f6", foregroundColor: "#ffffff" },
            ],
          },
        },
        drivers: [
          {
            objectId: "rp1",
            rank: 1,
            totalLaps: 3,
            totalTime: 30.5,
            bestLapTime: 9.8,
            averageLapTime: 10.166,
            medianLapTime: 10.2,
            rankValue: 100,
            seed: 1,
            driver: {
              model: { entityId: "d1" },
              name: "Alice",
              nickname: "Ally",
            },
          },
          {
            objectId: "rp2",
            rank: 2,
            totalLaps: 3,
            totalTime: 32.1,
            bestLapTime: 10.5,
            averageLapTime: 10.7,
            medianLapTime: 10.6,
            rankValue: 80,
            seed: 2,
            driver: {
              model: { entityId: "d2" },
              name: "Bob",
              nickname: "Bobby",
            },
          },
        ],
        heats: [
          {
            objectId: "h1",
            heatNumber: 1,
            started: true,
            completed: true,
            heatDrivers: [
              {
                objectId: "hd1",
                driver: {
                  objectId: "rp1",
                  driver: {
                    model: { entityId: "d1" },
                    name: "Alice",
                    nickname: "Ally",
                  },
                },
                actualDriver: {
                  model: { entityId: "d1" },
                  name: "Alice",
                  nickname: "Ally",
                },
                adjustedLapCount: 3,
                totalTime: 30.5,
                bestLapTime: 9.8,
                averageLapTime: 10.166,
                medianLapTime: 10.2,
                reactionTime: 0.15,
                laps: [
                  { lapTime: 10.5, segments: [3.2, 4.1, 3.2] },
                  { lapTime: 9.8, segments: [2.9, 3.8, 3.1] },
                  { lapTime: 10.2, segments: [3.1, 4.0, 3.1] },
                ],
              },
              {
                objectId: "hd2",
                driver: {
                  objectId: "rp2",
                  driver: {
                    model: { entityId: "d2" },
                    name: "Bob",
                    nickname: "Bobby",
                  },
                },
                actualDriver: {
                  model: { entityId: "d2" },
                  name: "Bob",
                  nickname: "Bobby",
                },
                adjustedLapCount: 3,
                totalTime: 32.1,
                bestLapTime: 10.5,
                averageLapTime: 10.7,
                medianLapTime: 10.6,
                reactionTime: 0.22,
                laps: [
                  { lapTime: 11.0, segments: [3.5, 4.3, 3.2] },
                  { lapTime: 10.5, segments: [3.2, 4.1, 3.2] },
                  { lapTime: 10.6, segments: [3.3, 4.1, 3.2] },
                ],
              },
            ],
          },
        ],
        currentHeat: {
          objectId: "h1",
          heatNumber: 1,
          started: true,
          completed: true,
          heatDrivers: [
            {
              objectId: "hd1",
              driver: {
                objectId: "rp1",
                driver: {
                  model: { entityId: "d1" },
                  name: "Alice",
                  nickname: "Ally",
                },
              },
              actualDriver: {
                model: { entityId: "d1" },
                name: "Alice",
                nickname: "Ally",
              },
              adjustedLapCount: 3,
              totalTime: 30.5,
              bestLapTime: 9.8,
              averageLapTime: 10.166,
              medianLapTime: 10.2,
              reactionTime: 0.15,
              laps: [
                { lapTime: 10.5, segments: [3.2, 4.1, 3.2] },
                { lapTime: 9.8, segments: [2.9, 3.8, 3.1] },
                { lapTime: 10.2, segments: [3.1, 4.0, 3.1] },
              ],
            },
            {
              objectId: "hd2",
              driver: {
                objectId: "rp2",
                driver: {
                  model: { entityId: "d2" },
                  name: "Bob",
                  nickname: "Bobby",
                },
              },
              actualDriver: {
                model: { entityId: "d2" },
                name: "Bob",
                nickname: "Bobby",
              },
              adjustedLapCount: 3,
              totalTime: 32.1,
              bestLapTime: 10.5,
              averageLapTime: 10.7,
              medianLapTime: 10.6,
              reactionTime: 0.22,
              laps: [
                { lapTime: 11.0, segments: [3.5, 4.3, 3.2] },
                { lapTime: 10.5, segments: [3.2, 4.1, 3.2] },
                { lapTime: 10.6, segments: [3.3, 4.1, 3.2] },
              ],
            },
          ],
        },
      },
    };
  }

  /**
   * Helper to construct fully compliant mock team driver RaceData structure.
   */
  static createMockTeamDriverData() {
    return {
      race: {
        race: {
          model: { entityId: "r1" },
          name: "Screendiff Grand Prix",
          track: {
            model: { entityId: "t1" },
            name: "Screendiff Raceway",
            lanes: [
              { backgroundColor: "#ef4444", foregroundColor: "#ffffff" },
              { backgroundColor: "#3b82f6", foregroundColor: "#ffffff" },
            ],
          },
        },
        drivers: [
          {
            objectId: "rp_team1",
            rank: 1,
            totalLaps: 4,
            totalTime: 40.8,
            bestLapTime: 9.8,
            averageLapTime: 10.2,
            medianLapTime: 10.1,
            rankValue: 100,
            seed: 1,
            team: {
              model: { entityId: "team1" },
              name: "Apex Assassins",
              driverIds: ["d1", "d2"],
            },
          },
          {
            objectId: "rp1",
            driver: {
              model: { entityId: "d1" },
              name: "Alice",
              nickname: "Ally",
            },
          },
          {
            objectId: "rp2",
            driver: {
              model: { entityId: "d2" },
              name: "Bob",
              nickname: "Bobby",
            },
          },
        ],
        heats: [
          {
            objectId: "h1",
            heatNumber: 1,
            started: true,
            completed: true,
            heatDrivers: [
              {
                objectId: "hd_team1",
                driver: {
                  objectId: "rp_team1",
                  team: {
                    model: { entityId: "team1" },
                    name: "Apex Assassins",
                    driverIds: ["d1", "d2"],
                  },
                },
                adjustedLapCount: 4,
                totalTime: 40.8,
                bestLapTime: 9.8,
                averageLapTime: 10.2,
                medianLapTime: 10.1,
                reactionTime: 0.18,
                laps: [
                  { lapTime: 10.5, driverId: "d1", segments: [3.2, 4.1, 3.2] },
                  { lapTime: 10.3, driverId: "d2", segments: [3.1, 4.0, 3.2] },
                  { lapTime: 9.8, driverId: "d1", segments: [2.9, 3.8, 3.1] },
                  { lapTime: 10.2, driverId: "d2", segments: [3.1, 4.0, 3.1] },
                ],
              },
            ],
          },
        ],
        currentHeat: {
          objectId: "h1",
          heatNumber: 1,
          started: true,
          completed: true,
          heatDrivers: [
            {
              objectId: "hd_team1",
              driver: {
                objectId: "rp_team1",
                team: {
                  model: { entityId: "team1" },
                  name: "Apex Assassins",
                  driverIds: ["d1", "d2"],
                },
              },
              adjustedLapCount: 4,
              totalTime: 40.8,
              bestLapTime: 9.8,
              averageLapTime: 10.2,
              medianLapTime: 10.1,
              reactionTime: 0.18,
              laps: [
                { lapTime: 10.5, driverId: "d1", segments: [3.2, 4.1, 3.2] },
                { lapTime: 10.3, driverId: "d2", segments: [3.1, 4.0, 3.2] },
                { lapTime: 9.8, driverId: "d1", segments: [2.9, 3.8, 3.1] },
                { lapTime: 10.2, driverId: "d2", segments: [3.1, 4.0, 3.1] },
              ],
            },
          ],
        },
      },
    };
  }

  /**
   * Helper to construct fully compliant mock race data with multi-lap pacing comparison.
   */
  static createMockPacingDriverData() {
    return {
      race: {
        race: {
          model: { entityId: "r1" },
          name: "Screendiff Grand Prix",
          track: {
            model: { entityId: "t1" },
            name: "Screendiff Raceway",
            lanes: [
              { backgroundColor: "#ef4444", foregroundColor: "#ffffff" },
              { backgroundColor: "#3b82f6", foregroundColor: "#ffffff" },
              { backgroundColor: "#10b981", foregroundColor: "#ffffff" },
            ],
          },
        },
        drivers: [
          {
            objectId: "rp1",
            rank: 1,
            totalLaps: 8,
            totalTime: 80.0,
            bestLapTime: 9.6,
            averageLapTime: 10.0,
            medianLapTime: 10.0,
            rankValue: 100,
            seed: 1,
            driver: {
              model: { entityId: "d1" },
              name: "Alice",
              nickname: "Ally",
            },
          },
          {
            objectId: "rp2",
            rank: 2,
            totalLaps: 8,
            totalTime: 80.4,
            bestLapTime: 9.8,
            averageLapTime: 10.05,
            medianLapTime: 10.05,
            rankValue: 80,
            seed: 2,
            driver: {
              model: { entityId: "d2" },
              name: "Bob",
              nickname: "Bobby",
            },
          },
          {
            objectId: "rp3",
            rank: 3,
            totalLaps: 8,
            totalTime: 82.1,
            bestLapTime: 10.0,
            averageLapTime: 10.26,
            medianLapTime: 10.25,
            rankValue: 60,
            seed: 3,
            driver: {
              model: { entityId: "d3" },
              name: "Chloe",
              nickname: "Chlo",
            },
          },
        ],
        heats: [
          {
            objectId: "h1",
            heatNumber: 1,
            started: true,
            completed: true,
            heatDrivers: [
              {
                objectId: "hd1",
                driver: {
                  objectId: "rp1",
                  driver: {
                    model: { entityId: "d1" },
                    name: "Alice",
                    nickname: "Ally",
                  },
                },
                actualDriver: {
                  model: { entityId: "d1" },
                  name: "Alice",
                  nickname: "Ally",
                },
                adjustedLapCount: 8,
                totalTime: 80.0,
                bestLapTime: 9.6,
                averageLapTime: 10.0,
                medianLapTime: 10.0,
                reactionTime: 0.15,
                laps: [
                  { lapTime: 10.2, segments: [3.3, 3.8, 3.1] },
                  { lapTime: 9.8, segments: [3.1, 3.6, 3.1] },
                  { lapTime: 10.4, segments: [3.4, 3.9, 3.1] },
                  { lapTime: 9.6, segments: [3.0, 3.5, 3.1] },
                  { lapTime: 10.1, segments: [3.2, 3.8, 3.1] },
                  { lapTime: 9.9, segments: [3.1, 3.7, 3.1] },
                  { lapTime: 10.3, segments: [3.3, 3.9, 3.1] },
                  { lapTime: 9.7, segments: [3.0, 3.6, 3.1] },
                ],
              },
              {
                objectId: "hd2",
                driver: {
                  objectId: "rp2",
                  driver: {
                    model: { entityId: "d2" },
                    name: "Bob",
                    nickname: "Bobby",
                  },
                },
                actualDriver: {
                  model: { entityId: "d2" },
                  name: "Bob",
                  nickname: "Bobby",
                },
                adjustedLapCount: 8,
                totalTime: 80.4,
                bestLapTime: 9.8,
                averageLapTime: 10.05,
                medianLapTime: 10.05,
                reactionTime: 0.22,
                laps: [
                  { lapTime: 9.9, segments: [3.1, 3.7, 3.1] },
                  { lapTime: 10.3, segments: [3.3, 3.9, 3.1] },
                  { lapTime: 10.1, segments: [3.2, 3.8, 3.1] },
                  { lapTime: 10.0, segments: [3.2, 3.7, 3.1] },
                  { lapTime: 9.8, segments: [3.1, 3.6, 3.1] },
                  { lapTime: 10.2, segments: [3.3, 3.8, 3.1] },
                  { lapTime: 10.0, segments: [3.2, 3.7, 3.1] },
                  { lapTime: 10.1, segments: [3.2, 3.8, 3.1] },
                ],
              },
              {
                objectId: "hd3",
                driver: {
                  objectId: "rp3",
                  driver: {
                    model: { entityId: "d3" },
                    name: "Chloe",
                    nickname: "Chlo",
                  },
                },
                actualDriver: {
                  model: { entityId: "d3" },
                  name: "Chloe",
                  nickname: "Chlo",
                },
                adjustedLapCount: 8,
                totalTime: 82.1,
                bestLapTime: 10.0,
                averageLapTime: 10.26,
                medianLapTime: 10.25,
                reactionTime: 0.25,
                laps: [
                  { lapTime: 10.5, segments: [3.4, 4.0, 3.1] },
                  { lapTime: 10.4, segments: [3.3, 4.0, 3.1] },
                  { lapTime: 10.2, segments: [3.3, 3.8, 3.1] },
                  { lapTime: 10.1, segments: [3.2, 3.8, 3.1] },
                  { lapTime: 10.3, segments: [3.3, 3.9, 3.1] },
                  { lapTime: 10.0, segments: [3.2, 3.7, 3.1] },
                  { lapTime: 10.2, segments: [3.3, 3.8, 3.1] },
                  { lapTime: 10.4, segments: [3.4, 3.9, 3.1] },
                ],
              },
            ],
          },
        ],
        currentHeat: {
          objectId: "h1",
          heatNumber: 1,
          started: true,
          completed: true,
          heatDrivers: [
            {
              objectId: "hd1",
              driver: {
                objectId: "rp1",
                driver: {
                  model: { entityId: "d1" },
                  name: "Alice",
                  nickname: "Ally",
                },
              },
              actualDriver: {
                model: { entityId: "d1" },
                name: "Alice",
                nickname: "Ally",
              },
              adjustedLapCount: 8,
              totalTime: 80.0,
              bestLapTime: 9.6,
              averageLapTime: 10.0,
              medianLapTime: 10.0,
              reactionTime: 0.15,
              laps: [
                { lapTime: 10.2, segments: [3.3, 3.8, 3.1] },
                { lapTime: 9.8, segments: [3.1, 3.6, 3.1] },
                { lapTime: 10.4, segments: [3.4, 3.9, 3.1] },
                { lapTime: 9.6, segments: [3.0, 3.5, 3.1] },
                { lapTime: 10.1, segments: [3.2, 3.8, 3.1] },
                { lapTime: 9.9, segments: [3.1, 3.7, 3.1] },
                { lapTime: 10.3, segments: [3.3, 3.9, 3.1] },
                { lapTime: 9.7, segments: [3.0, 3.6, 3.1] },
              ],
            },
            {
              objectId: "hd2",
              driver: {
                objectId: "rp2",
                driver: {
                  model: { entityId: "d2" },
                  name: "Bob",
                  nickname: "Bobby",
                },
              },
              actualDriver: {
                model: { entityId: "d2" },
                name: "Bob",
                nickname: "Bobby",
              },
              adjustedLapCount: 8,
              totalTime: 80.4,
              bestLapTime: 9.8,
              averageLapTime: 10.05,
              medianLapTime: 10.05,
              reactionTime: 0.22,
              laps: [
                { lapTime: 9.9, segments: [3.1, 3.7, 3.1] },
                { lapTime: 10.3, segments: [3.3, 3.9, 3.1] },
                { lapTime: 10.1, segments: [3.2, 3.8, 3.1] },
                { lapTime: 10.0, segments: [3.2, 3.7, 3.1] },
                { lapTime: 9.8, segments: [3.1, 3.6, 3.1] },
                { lapTime: 10.2, segments: [3.3, 3.8, 3.1] },
                { lapTime: 10.0, segments: [3.2, 3.7, 3.1] },
                { lapTime: 10.1, segments: [3.2, 3.8, 3.1] },
              ],
            },
            {
              objectId: "hd3",
              driver: {
                objectId: "rp3",
                driver: {
                  model: { entityId: "d3" },
                  name: "Chloe",
                  nickname: "Chlo",
                },
              },
              actualDriver: {
                model: { entityId: "d3" },
                name: "Chloe",
                nickname: "Chlo",
              },
              adjustedLapCount: 8,
              totalTime: 82.1,
              bestLapTime: 10.0,
              averageLapTime: 10.26,
              medianLapTime: 10.25,
              reactionTime: 0.25,
              laps: [
                { lapTime: 10.5, segments: [3.4, 4.0, 3.1] },
                { lapTime: 10.4, segments: [3.3, 4.0, 3.1] },
                { lapTime: 10.2, segments: [3.3, 3.8, 3.1] },
                { lapTime: 10.1, segments: [3.2, 3.8, 3.1] },
                { lapTime: 10.3, segments: [3.3, 3.9, 3.1] },
                { lapTime: 10.0, segments: [3.2, 3.7, 3.1] },
                { lapTime: 10.2, segments: [3.3, 3.8, 3.1] },
                { lapTime: 10.4, segments: [3.4, 3.9, 3.1] },
              ],
            },
          ],
        },
      },
    };
  }

  /**
   * Inject mock race data buffer into the Playwright page.
   */
  static async injectMockRaceData(page: any, mockData: any) {
    const buffer = RaceData.encode(mockData).finish();
    const dataArray = Array.from(buffer);
    await page.addInitScript((data: number[]) => {
      // @ts-ignore
      window.mockRaceDataBuffer = new Uint8Array(data).buffer;
    }, dataArray);
  }
}
