import { CommonModule } from "@angular/common";
import { Component, computed, inject, input, output } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { TranslatePipe } from "@app/pipes/translate.pipe";
import { DriverHeatData } from "@app/race/driver_heat_data";
import {
  GhostPacingService,
  HeatTrajectoryComparison,
} from "@app/services/ghost-pacing.service";
import { TranslationService } from "@app/services/translation.service";

@Component({
  standalone: true,
  selector: "app-ghost-trajectory-dialog",
  templateUrl: "./ghost-trajectory-dialog.component.html",
  styleUrls: ["./ghost-trajectory-dialog.component.css"],
  imports: [CommonModule, FormsModule, TranslatePipe],
})
export class GhostTrajectoryDialogComponent {
  private ghostPacingService = inject(GhostPacingService);
  private translationService = inject(TranslationService);

  visible = input<boolean>(false);
  driverA = input<DriverHeatData | null>(null);
  driverB = input<DriverHeatData | null>(null);
  benchmarkLapTime = input<number>(0);

  close = output<void>();

  comparison = computed<HeatTrajectoryComparison>(() => {
    const da = this.driverA();
    const db = this.driverB();
    const benchmark = this.benchmarkLapTime();

    const lapsA = da?.lapTimes || [];
    let lapsB: number[] = [];

    if (db && db.lapTimes && db.lapTimes.length > 0) {
      lapsB = db.lapTimes;
    } else if (benchmark > 0 && lapsA.length > 0) {
      lapsB = new Array(lapsA.length).fill(benchmark);
    }

    return this.ghostPacingService.compareTrajectories(lapsA, lapsB);
  });

  driverAName = computed(() => {
    return (
      this.driverA()?.driver?.name ||
      this.translationService.translate("GTD_DRIVER_A")
    );
  });

  driverBName = computed(() => {
    if (this.driverB()) {
      return (
        this.driverB()?.driver?.name ||
        this.translationService.translate("GTD_DRIVER_B")
      );
    }
    if (this.benchmarkLapTime() > 0) {
      const ghostLabel = this.translationService.translate("GTD_GHOST");
      return `${ghostLabel} (${this.benchmarkLapTime().toFixed(2)}s)`;
    }
    return this.translationService.translate("GTD_BENCHMARK");
  });

  onDismiss() {
    this.close.emit();
  }
}
