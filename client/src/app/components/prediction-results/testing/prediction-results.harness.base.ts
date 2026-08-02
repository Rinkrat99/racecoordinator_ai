export abstract class PredictionResultsHarnessBase {
  static readonly hostSelector = "app-prediction-results";

  static readonly selectors = {
    dashboardWrapper: ".dashboard-wrapper",
    headerBar: ".header-bar",
    pageTitle: ".page-title",
    recordsDashboard: ".records-dashboard",
    recordsTitle: ".records-title",
    recordCards: ".record-card",
    standingsTableWrapper: ".standings-table-wrapper",
    predictionTable: ".prediction-table",
    driverRows: ".prediction-table tbody tr",
    driverCellWrapper: ".driver-cell-wrapper",
    driverName: ".driver-name",
    popoverCard: ".diagnostic-card-popover",
    popoverTitle: ".popover-title",
  };

  abstract hasRecordsDashboard(): Promise<boolean>;
  abstract hasStandingsTable(): Promise<boolean>;
  abstract getDriverRowCount(): Promise<number>;
  abstract hasHovercard(): Promise<boolean>;
}
