import { Page } from "@playwright/test";
import {
  PredictionEvaluationRecord,
  RacePredictionRecord,
} from "@app/services/race-prediction.service";

/**
 * Shared test helper for PredictionResults component.
 * Provides standard mock data structures for unit and screendiff tests.
 */
export class PredictionResultsHelper {
  /**
   * Mock prediction data for Pre-Race prediction page.
   */
  static createPreRaceMockData(): RacePredictionRecord {
    return {
      race_id: "race_pred_1",
      timestamp: 1700000000,
      pre_race: {
        heat_index: 0,
        completed_laps: 0,
        win_probabilities: {
          d1: 0.65,
          d2: 0.25,
          d3: 0.08,
          d4: 0.02,
        },
        podium_probabilities: {
          d1: 0.95,
          d2: 0.8,
          d3: 0.6,
          d4: 0.2,
        },
        projected_standings: [
          {
            driver_id: "d1",
            driver_name: "Alice Sprint",
            projected_rank: 1,
            projected_laps: 48.5,
            projected_time_seconds: 150.35,
            win_probability: 0.65,
            podium_probability: 0.95,
            prior_median_lap_time: 3.1,
            prior_std_dev: 0.12,
            historical_laps: 150,
            per_lane_medians: { "Lane 1": 3.08, "Lane 2": 3.12 },
            simulated_wins: 650,
            total_simulations: 1000,
          },
          {
            driver_id: "d2",
            driver_name: "Bob Turbo",
            projected_rank: 2,
            projected_laps: 47.2,
            projected_time_seconds: 150.35,
            win_probability: 0.25,
            podium_probability: 0.8,
            prior_median_lap_time: 3.18,
            prior_std_dev: 0.15,
            historical_laps: 120,
            per_lane_medians: { "Lane 1": 3.15, "Lane 2": 3.2 },
            simulated_wins: 250,
            total_simulations: 1000,
          },
          {
            driver_id: "d3",
            driver_name: "Charlie Apex",
            projected_rank: 3,
            projected_laps: 45.8,
            projected_time_seconds: 150.35,
            win_probability: 0.08,
            podium_probability: 0.6,
            prior_median_lap_time: 3.28,
            prior_std_dev: 0.18,
            historical_laps: 95,
            per_lane_medians: { "Lane 1": 3.25, "Lane 2": 3.31 },
            simulated_wins: 80,
            total_simulations: 1000,
          },
          {
            driver_id: "d4",
            driver_name: "Dave Drift",
            projected_rank: 4,
            projected_laps: 43.1,
            projected_time_seconds: 150.35,
            win_probability: 0.02,
            podium_probability: 0.2,
            prior_median_lap_time: 3.48,
            prior_std_dev: 0.25,
            historical_laps: 60,
            per_lane_medians: { "Lane 1": 3.42, "Lane 2": 3.54 },
            simulated_wins: 20,
            total_simulations: 1000,
          },
        ],
        heat_forecasts: [
          {
            heat_number: 1,
            predicted_winner_id: "d1",
            driver_projected_laps: { d1: 24.5, d2: 23.8 },
          },
        ],
      },
      realtime_snapshots: [],
    };
  }

  /**
   * Mock prediction data for Active Race prediction page.
   */
  static createActiveRaceMockData(): RacePredictionRecord {
    const record = this.createPreRaceMockData();
    record.realtime_snapshots = [
      {
        heat_index: 2,
        completed_laps: 24,
        win_probabilities: {
          d1: 0.72,
          d2: 0.22,
          d3: 0.05,
          d4: 0.01,
        },
        podium_probabilities: {
          d1: 0.98,
          d2: 0.85,
          d3: 0.55,
          d4: 0.12,
        },
        projected_standings: [
          {
            driver_id: "d1",
            driver_name: "Alice Sprint",
            projected_rank: 1,
            projected_laps: 49.2,
            projected_time_seconds: 150.35,
            win_probability: 0.72,
            podium_probability: 0.98,
            prior_median_lap_time: 3.1,
            prior_std_dev: 0.12,
            historical_laps: 150,
            per_lane_medians: { "Lane 1": 3.08, "Lane 2": 3.12 },
            empirical_laps: 24,
            empirical_median_lap_time: 3.05,
            simulated_wins: 720,
            total_simulations: 1000,
          },
          {
            driver_id: "d2",
            driver_name: "Bob Turbo",
            projected_rank: 2,
            projected_laps: 46.8,
            projected_time_seconds: 150.35,
            win_probability: 0.22,
            podium_probability: 0.85,
            prior_median_lap_time: 3.18,
            prior_std_dev: 0.15,
            historical_laps: 120,
            per_lane_medians: { "Lane 1": 3.15, "Lane 2": 3.2 },
            empirical_laps: 24,
            empirical_median_lap_time: 3.21,
            simulated_wins: 220,
            total_simulations: 1000,
          },
          {
            driver_id: "d3",
            driver_name: "Charlie Apex",
            projected_rank: 3,
            projected_laps: 45.1,
            projected_time_seconds: 150.35,
            win_probability: 0.05,
            podium_probability: 0.55,
            prior_median_lap_time: 3.28,
            prior_std_dev: 0.18,
            historical_laps: 95,
            per_lane_medians: { "Lane 1": 3.25, "Lane 2": 3.31 },
            empirical_laps: 24,
            empirical_median_lap_time: 3.33,
            simulated_wins: 50,
            total_simulations: 1000,
          },
          {
            driver_id: "d4",
            driver_name: "Dave Drift",
            projected_rank: 4,
            projected_laps: 42.5,
            projected_time_seconds: 150.35,
            win_probability: 0.01,
            podium_probability: 0.12,
            prior_median_lap_time: 3.48,
            prior_std_dev: 0.25,
            historical_laps: 60,
            per_lane_medians: { "Lane 1": 3.42, "Lane 2": 3.54 },
            empirical_laps: 24,
            empirical_median_lap_time: 3.52,
            simulated_wins: 10,
            total_simulations: 1000,
          },
        ],
        heat_forecasts: [],
      },
    ];
    return record;
  }

  /**
   * Mock evaluation data for Post-Race prediction page.
   */
  static createPostRaceEvaluationData(): PredictionEvaluationRecord {
    return {
      race_id: "race_pred_1",
      evaluated_at: 1700003600,
      brier_score: 0.042,
      rank_mae: 0.25,
      lap_projection_mae: 0.8,
      driver_evaluations: [
        {
          driver_id: "d1",
          driver_name: "Alice Sprint",
          pre_race_win_prob: 0.65,
          projected_rank: 1,
          actual_rank: 1,
          projected_laps: 48.5,
          actual_laps: 49,
        },
        {
          driver_id: "d2",
          driver_name: "Bob Turbo",
          pre_race_win_prob: 0.25,
          projected_rank: 2,
          actual_rank: 2,
          projected_laps: 47.2,
          actual_laps: 47,
        },
      ],
    };
  }

  /**
   * Inject prediction and evaluation mock routes into Playwright page.
   */
  static async injectMockPredictionData(
    page: Page,
    options: {
      predictionRecord?: RacePredictionRecord;
      evaluationRecord?: PredictionEvaluationRecord | null;
    } = {},
  ) {
    const predRecord = options.predictionRecord || this.createPreRaceMockData();
    const evalRecord = options.evaluationRecord ?? null;

    await page.route("**/api/predictions/races/*", async (route) => {
      await route.fulfill({
        status: 200,
        contentType: "application/json",
        body: JSON.stringify(predRecord),
      });
    });

    await page.route("**/api/predictions/evaluations/*", async (route) => {
      if (evalRecord) {
        await route.fulfill({
          status: 200,
          contentType: "application/json",
          body: JSON.stringify(evalRecord),
        });
      } else {
        await route.fulfill({
          status: 404,
          contentType: "application/json",
          body: JSON.stringify({ message: "Evaluation not found" }),
        });
      }
    });
  }
}
