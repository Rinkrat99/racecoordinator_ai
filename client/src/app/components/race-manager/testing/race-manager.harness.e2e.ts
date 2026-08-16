import { Locator } from "@playwright/test";

import { RaceManagerHarnessBase } from "./race-manager.harness.base";

export class RaceManagerHarnessE2e implements RaceManagerHarnessBase {
  constructor(private locator: Locator) {}

  private get base() {
    return RaceManagerHarnessBase;
  }

  get listContainer() {
    return this.locator.locator(this.base.selectors.listContainer);
  }

  get detailPanel() {
    return this.locator.locator(this.base.selectors.detailPanel);
  }

  async exists(): Promise<boolean> {
    return await this.locator.isVisible();
  }

  async selectItem(index: number): Promise<void> {
    await this.locator.locator(this.base.selectors.listItem).nth(index).click();
  }

  async clickDelete(): Promise<void> {
    await this.locator
      .locator("app-manager-header")
      .getByRole("button", { name: /delete/i })
      .click();
  }
}
