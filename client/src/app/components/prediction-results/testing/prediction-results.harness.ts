import { ComponentHarness } from "@angular/cdk/testing";

import { PredictionResultsHarnessBase } from "./prediction-results.harness.base";

export class PredictionResultsHarness
  extends ComponentHarness
  implements PredictionResultsHarnessBase
{
  static hostSelector = PredictionResultsHarnessBase.hostSelector;

  protected getRecordsDashboardEl = this.locatorForOptional(
    PredictionResultsHarnessBase.selectors.recordsDashboard,
  );
  protected getPredictionTableEl = this.locatorForOptional(
    PredictionResultsHarnessBase.selectors.predictionTable,
  );
  protected getDriverRowsEl = this.locatorForAll(
    PredictionResultsHarnessBase.selectors.driverRows,
  );
  protected getPopoverCardEl = this.locatorForOptional(
    PredictionResultsHarnessBase.selectors.popoverCard,
  );

  async hasRecordsDashboard(): Promise<boolean> {
    return (await this.getRecordsDashboardEl()) !== null;
  }

  async hasStandingsTable(): Promise<boolean> {
    return (await this.getPredictionTableEl()) !== null;
  }

  async getDriverRowCount(): Promise<number> {
    return (await this.getDriverRowsEl()).length;
  }

  async hasHovercard(): Promise<boolean> {
    return (await this.getPopoverCardEl()) !== null;
  }
}
