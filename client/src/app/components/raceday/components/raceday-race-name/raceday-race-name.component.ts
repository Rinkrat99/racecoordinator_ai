import { CommonModule } from "@angular/common";
import {
  AfterViewInit,
  Component,
  effect,
  ElementRef,
  input,
  OnDestroy,
  viewChild,
  ViewEncapsulation,
} from "@angular/core";
import { Race } from "@app/models/race";
import { AbsoluteWidgetNode } from "@app/models/settings";
import { TranslatePipe } from "@app/pipes/translate.pipe";

@Component({
  standalone: true,
  selector: "app-raceday-race-name",
  templateUrl: "./raceday-race-name.component.html",
  styleUrls: ["./raceday-race-name.component.css"],
  encapsulation: ViewEncapsulation.None,
  imports: [CommonModule, TranslatePipe],
})
export class RacedayRaceNameComponent implements AfterViewInit, OnDestroy {
  race = input<Race | undefined>(undefined);
  widget = input<AbsoluteWidgetNode | null>(null);

  private infoPanel = viewChild<ElementRef<HTMLElement>>("infoPanel");
  private labelText = viewChild<ElementRef<HTMLElement>>("labelText");
  private valueText = viewChild<ElementRef<HTMLElement>>("valueText");
  private resizeObserver?: ResizeObserver;

  get isEvent(): boolean {
    return !!(this.race() as any)?.is_event;
  }

  get eventName(): string {
    return (this.race() as any)?.event_name || "";
  }

  get currentEventRaceIndex(): number {
    return ((this.race() as any)?.current_event_race_index || 0) + 1;
  }

  get totalEventRaces(): number {
    return (this.race() as any)?.total_event_races || 0;
  }

  get formattedRaceName(): string {
    const r = this.race();
    if (!r) return "";
    if (this.isEvent) {
      return `${r.name} ${this.currentEventRaceIndex} / ${this.totalEventRaces}`;
    }
    return r.name;
  }

  constructor() {
    effect(() => {
      this.race();
      this.widget();
      setTimeout(() => this.fitText(), 0);
    });
  }

  ngAfterViewInit() {
    const panelEl = this.infoPanel()?.nativeElement;
    if (panelEl && typeof ResizeObserver !== "undefined") {
      this.resizeObserver = new ResizeObserver(() => {
        this.fitText();
      });
      this.resizeObserver.observe(panelEl);
    }
  }

  ngOnDestroy() {
    if (this.resizeObserver) {
      this.resizeObserver.disconnect();
    }
  }

  private fitText() {
    const panelEl = this.infoPanel()?.nativeElement;
    const widgetData = this.widget();

    if (!panelEl) return;

    const isAuto = !widgetData || widgetData.scaleMode === "auto";
    if (!isAuto) {
      panelEl.style.removeProperty("--header-value-font-size");
      panelEl.style.removeProperty("--header-label-font-size");
      return;
    }

    const labelEls = panelEl.querySelectorAll<HTMLElement>(".label-text");
    const valueEls = panelEl.querySelectorAll<HTMLElement>(".value-text");

    if (labelEls.length === 0 && valueEls.length === 0) return;

    const labelStyle =
      labelEls.length > 0 ? window.getComputedStyle(labelEls[0]) : null;
    const valueStyle =
      valueEls.length > 0 ? window.getComputedStyle(valueEls[0]) : null;

    const labelRatio = 55 / 80;

    const canvas = document.createElement("canvas");
    const context = canvas.getContext("2d");
    let totalTextWidth = 100;

    if (context) {
      let labelWidth = 0;
      if (labelStyle) {
        context.font = `${labelStyle.fontWeight || "600"} ${100 * labelRatio}px ${labelStyle.fontFamily || "sans-serif"}`;
        labelEls.forEach((el) => {
          const str = el.textContent?.trim() || "";
          labelWidth += context.measureText(str.toUpperCase()).width || 0;
        });
      }

      let valueWidth = 0;
      if (valueStyle) {
        context.font = `${valueStyle.fontWeight || "700"} 100px ${valueStyle.fontFamily || "sans-serif"}`;
        valueEls.forEach((el) => {
          const str = el.textContent?.trim() || "";
          valueWidth += context.measureText(str).width || 0;
        });
      }

      const marginWidth = (8 / 18) * 100;
      const marginCount = labelEls.length + valueEls.length - 1;
      totalTextWidth =
        labelWidth + valueWidth + Math.max(0, marginCount) * marginWidth;
    }

    const textHeight = 100;
    const containerWidth = panelEl.clientWidth * 0.9;
    const containerHeight = panelEl.clientHeight * 0.8;

    const scaleX = containerWidth / totalTextWidth;
    const scaleY = containerHeight / textHeight;
    const scale = Math.min(scaleX, scaleY);

    const baseScaleFactor = widgetData?.textScaleFactor ?? 1;
    const targetValueSize = Math.floor(100 * scale * baseScaleFactor);
    const targetLabelSize = Math.floor(targetValueSize * labelRatio);

    panelEl.style.setProperty(
      "--header-value-font-size",
      `${targetValueSize}px`,
    );
    panelEl.style.setProperty(
      "--header-label-font-size",
      `${targetLabelSize}px`,
    );
  }
}
