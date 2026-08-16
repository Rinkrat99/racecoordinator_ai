import { CommonModule } from "@angular/common";
import { Component, computed, inject, input, output } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { DriverHeatData } from "@app/race/driver_heat_data";
import {
  GhostPacingService,
  HeatTrajectoryComparison,
} from "@app/services/ghost-pacing.service";

@Component({
  standalone: true,
  selector: "app-ghost-trajectory-dialog",
  templateUrl: "./ghost-trajectory-dialog.component.html",
  styleUrls: ["./ghost-trajectory-dialog.component.css"],
  imports: [CommonModule, FormsModule],
})
export class GhostTrajectoryDialogComponent {
  private ghostPacingService = inject(GhostPacingService);

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
    return this.driverA()?.driver?.name || "Driver A";
  });

  driverBName = computed(() => {
    if (this.driverB()) {
      return this.driverB()?.driver?.name || "Driver B";
    }
    if (this.benchmarkLapTime() > 0) {
      return `Ghost (${this.benchmarkLapTime().toFixed(2)}s)`;
    }
    return "Benchmark";
  });

  onDismiss() {
    this.close.emit();
  }
}
