import { Locator } from "@playwright/test";

import { PredictionResultsHarnessBase } from "./prediction-results.harness.base";

export class PredictionResultsHarnessE2e implements PredictionResultsHarnessBase {
  constructor(private locator: Locator) {}

  private get base() {
    return PredictionResultsHarnessBase;
  }

  private get recordsDashboard() {
    return this.locator.locator(this.base.selectors.recordsDashboard).first();
  }

  private get predictionTable() {
    return this.locator.locator(this.base.selectors.predictionTable).first();
  }

  private get driverRows() {
    return this.locator.locator(this.base.selectors.driverRows);
  }

  private get popoverCard() {
    return this.locator.locator(this.base.selectors.popoverCard).first();
  }

  async hasRecordsDashboard(): Promise<boolean> {
    return await this.recordsDashboard.isVisible();
  }

  async hasStandingsTable(): Promise<boolean> {
    return await this.predictionTable.isVisible();
  }

  async getDriverRowCount(): Promise<number> {
    return await this.driverRows.count();
  }

  async hasHovercard(): Promise<boolean> {
    return await this.popoverCard.isVisible();
  }

  async hoverDriverRow(index: number = 0): Promise<void> {
    const row = this.driverRows.nth(index);
    const driverCell = row.locator(this.base.selectors.driverCellWrapper);
    await driverCell.hover();
  }
}
