# Race Predictions System

The Race Coordinator AI Prediction System uses Empirical Bayesian Prior Blending and Monte Carlo simulations to forecast race outcomes, win probabilities, and projected lap totals. It adapts dynamically before, during, and after every race based on driver historical performance and live lap timing data.

---

## High-Level Overview (For Racers & Event Directors)

### What Are Race Predictions?
Race predictions estimate each driver's final finishing rank, expected lap total, and probability of winning the overall race. Instead of relying on simple averages or static lap times, Race Coordinator AI simulates thousands of complete races in milliseconds, taking into account lane rotations, heat structures, pit stops, and live lap pace.

---

### Data Scenarios: How Data Availability Affects Predictions

The prediction system gracefully adapts depending on how much historical data is stored for the drivers in the race:

| Data Availability Scenario | How Predictions Are Generated | What You Will See |
| :--- | :--- | :--- |
| **No Historical Data (Cold Start)** | The system uses default global track priors (typical track lap times and lap variances) shared equally among all drivers. | Pre-race win probabilities are equalized (e.g., 25% each in a 4-driver race). As soon as the first heat starts and laps are recorded, predictions rapidly adapt to live empirical pace. |
| **Mixed Data (Some Drivers Have History)** | Drivers with track history receive custom priors based on their historical mean lap time and consistency. Drivers with no history receive baseline priors. | Experienced drivers will show higher win probabilities pre-race. However, if a new driver performs exceptionally well in early heats, live blending quickly updates their projected rank. |
| **Full Historical Data (All Drivers Have History)** | Every driver's prior is built from their past race records on the active track, incorporating lap averages, variance, and sample size. | Highly accurate pre-race forecasts that reflect real driver skill differentials across all lane assignments. |

---

### Race Lifecycle Behavior

Predictions evolve continuously across the three stages of a race:

```
 [ PRE-RACE ]                 [ REAL-TIME / IN-RACE ]                 [ POST-RACE ]
  Historical Priors            Empirical Bayesian Blending              Model Evaluation
  Monte Carlo Simulations   ➔  Live Monte Carlo Updates             ➔   Brier Score & MAE
  Pre-Race Forecast            Deterministic Seed per Lap               Database Update
```

#### 1. Pre-Race (Before the Green Flag)
- **Inputs:** Active race configuration (laps/time per heat, rotation matrix, fuel rules) and historical driver track statistics.
- **Behavior:** The engine runs 1,000 Monte Carlo simulations combining historical priors with heat rotation schedules.
- **Output:** Pre-race leaderboard showing Projected Rank, Projected Laps, Projected Total Time, and Win Probability %.

#### 2. Real-Time / Live (During Heats & Auto-Advance)
- **Inputs:** Live lap completion events, heat progress, current heat index, and accumulated completed heat totals.
- **Behavior:**
  - As drivers complete laps, the system blends their historical priors with their **empirical race pace** in real time.
  - During live heats, every lap completed updates the posterior distribution.
  - Between heats (including during **auto-advance countdowns**), the state is deterministically seeded to prevent prediction values from fluctuating while no new laps are completed.
- **Output:** Live real-time prediction updates on the Raceday UI and Lane Display widgets.

#### 3. Post-Race (After Checkered Flag)
- **Inputs:** Final official race results and all recorded lap time histories.
- **Behavior:**
  - The actual results are compared against pre-race and live predictions.
  - Accuracy metrics (Brier Score, Mean Absolute Error) are calculated to evaluate prediction quality.
  - New lap times and performance data are committed to the driver statistics database, continuously refining future prior estimates.

---

## Mathematical & Technical Specification (For Data Scientists)

The prediction architecture consists of an **Empirical Bayesian Prior Blending Model** coupled to a **Monte Carlo Simulation Engine**.

---

### Stage 1: Prior Distribution Construction

For a driver $i$ on track $T$, historical performance is parameterized by a normal distribution over lap time $X_{i} \sim \mathcal{N}(\mu_{i}, \sigma_{i}^2)$, where:
- $\mu_{i}$ is the historical sample mean lap time.
- $\sigma_{i}^2$ is the historical sample lap time variance.
- $N_{i}$ is the number of historical laps recorded.

#### Baseline Prior (Unseen Drivers)
If driver $i$ has no recorded history on track $T$ ($N_i = 0$), baseline parameters $(\mu_0, \sigma_0^2)$ are inferred from global track statistics:
$$\mu_{i,\text{prior}} = \mu_0, \quad \sigma_{i,\text{prior}}^2 = \sigma_0^2, \quad N_{i,\text{prior}} = 0$$

#### Historical Prior (Experienced Drivers)
If driver $i$ has history ($N_i > 0$), shrinking is applied relative to prior confidence:
$$\mu_{i,\text{prior}} = \mu_i, \quad \sigma_{i,\text{prior}}^2 = \max(\sigma_i^2, \sigma_{\min}^2)$$

---

### Stage 2: Empirical Bayesian Updating (In-Race Adaptation)

During heat execution, driver $i$ completes $n_i$ empirical laps in the current race with sample mean $\bar{y}_i$ and sample variance $s_i^2$.

#### Posterior Hyperparameter Estimation
The posterior mean lap time $\mu_{i,\text{post}}$ and posterior lap time variance $\sigma_{i,\text{post}}^2$ are calculated via inverse-variance weighting (Precision Weighting):

$$\tau_{\text{prior}} = \frac{1}{\sigma_{i,\text{prior}}^2 + \epsilon_{\text{track}}}, \quad \tau_{\text{emp}} = \frac{n_i}{s_i^2 + \epsilon_{\text{lap}}}$$

$$\mu_{i,\text{post}} = \frac{\tau_{\text{prior}} \mu_{i,\text{prior}} + \tau_{\text{emp}} \bar{y}_i}{\tau_{\text{prior}} + \tau_{\text{emp}}}$$

$$\sigma_{i,\text{post}}^2 = \frac{1}{\tau_{\text{prior}} + \tau_{\text{emp}}}$$

As $n_i \to \infty$ (more laps completed during the race), $\tau_{\text{emp}} \gg \tau_{\text{prior}}$, causing $\mu_{i,\text{post}} \to \bar{y}_i$. Empirical live performance smoothly dominates historical priors.

---

### Stage 3: Monte Carlo Simulation Engine

Because slot car races involve non-linear constraints (discrete heat durations, lane rotation matrices, pit stop refueling delays, and partial lap completion rules), analytical prediction equations are intractable. The system evaluates posterior predictions using a Monte Carlo simulation engine.

#### Algorithm Specification
For simulation run $m \in \{1, 2, \dots, M\}$ (where $M = 1,000$):

1. **Parameter Sampling:** For each driver $i$, sample latent heat pace $\tilde{\mu}_{i}^{(m)} \sim \mathcal{N}(\mu_{i,\text{post}}, \sigma_{i,\text{post}}^2)$.
2. **Heat Simulation:** For each heat $h$ in the race schedule:
   - Assign drivers to lanes based on the rotation matrix $R$.
   - Simulate lap-by-lap progression: $t_{i, k}^{(m)} \sim \mathcal{N}(\tilde{\mu}_{i}^{(m)}, \sigma_{i,\text{post}}^2)$.
   - Add fuel consumption and pit stop delays if fuel simulation is enabled.
   - Accumulate heat laps $L_{i, h}^{(m)}$ and heat time $T_{i, h}^{(m)}$ under heat finish rules (Lap-based or Time-based).
3. **Total Calculation:** Compute cumulative race total laps $L_i^{(m)} = \sum_h L_{i, h}^{(m)}$ and total elapsed time $T_i^{(m)} = \sum_h T_{i, h}^{(m)}$.
4. **Rank Evaluation:** Order drivers by race scoring rules to determine simulated rank $r_i^{(m)} \in \{1, \dots, K\}$.

#### Output Metrics Computation
- **Win Probability:**
  $$P(\text{Win}_i) = \frac{1}{M} \sum_{m=1}^{M} \mathbb{I}\left(r_i^{(m)} = 1\right)$$

- **Expected Laps:**
  $$\mathbb{E}[L_i] = \frac{1}{M} \sum_{m=1}^{M} L_i^{(m)}$$

- **Expected Rank:**
  $$\mathbb{E}[R_i] = \frac{1}{M} \sum_{m=1}^{M} r_i^{(m)}$$

#### Deterministic Seeding & Stability
To prevent random fluctuation during real-time updates and auto-advance countdowns, the random number generator seed $S_{\text{seed}}$ is deterministically generated:

$$S_{\text{seed}} = \text{Hash}(\text{RaceID}) + 31 \times h_{\text{current}} + \text{Hash}(\mathbf{S}_{\text{laps}})$$

where $\mathbf{S}_{\text{laps}}$ is the state hash of completed driver heat laps. If no new laps are recorded, $S_{\text{seed}}$ is identical across requests, ensuring 100% deterministic prediction outputs.

---

### Stage 4: Post-Race Model Evaluation

Upon race completion, actual driver ranks $\mathbf{r}^*$ and actual laps $\mathbf{L}^*$ are evaluated against pre-race predictions $\hat{\mathbf{P}}$ using standard diagnostic metrics:

- **Brier Score (Probabilistic Calibration):**
  $$\text{BS} = \frac{1}{K} \sum_{i=1}^{K} \left( P(\text{Win}_i) - y_i \right)^2 \quad \text{where } y_i = \mathbb{I}(r_i^* = 1)$$

- **Mean Absolute Lap Error (MAE):**
  $$\text{MAE} = \frac{1}{K} \sum_{i=1}^{K} \left| \mathbb{E}[L_i] - L_i^* \right|$$

These metrics are stored in `PredictionEvaluationRecord` documents to enable longitudinal tracking of model calibration and accuracy over time.
