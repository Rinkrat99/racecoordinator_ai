# Customizing XLS Export

Race Coordinator AI uses [Jxls](https://jxls.sourceforge.net/) to generate Excel (`.xlsx`) exports. Jxls allows you to use standard Excel features (like formatting, formulas, and charts) alongside special markup comments to inject data dynamically.

## Uploading a Custom Template

You can override the default export template with your own customized `.xlsx` file:

1. Create a `.xlsx` file and format it exactly how you want your race data to appear.
2. Insert Jxls markup comments into cells where dynamic data should be injected.
3. Open the Race Coordinator AI client.
4. Navigate to the **Settings** menu.
5. In the export settings section, click the button to upload a **Custom Export Template**.
6. Select your `.xlsx` file.

Once uploaded, this template will be used for all future XLS exports. You can revert to the default template at any time by clearing the custom template from the settings.

## Available Variables

The following data variables are available to use within your Jxls markup:

### `race`
The current race object, which includes general configuration and metadata.
- **Example Fields:** `name`, `configuration`, `description`, etc.

### `standings`
A list of drivers (`RaceParticipant` objects) ordered by their current overall standing in the race.
- **Example Usage:** `${driver.rank}`, `${driver.driver.name}`, `${driver.totalLaps}`, `${driver.totalTime}`, `${driver.bestLapTime}`
- **Looping Example:** `jx:each(items="standings" var="driver" lastCell="D5")`

### `heats`
A list of heat objects (`Heat`) that have been run in the race.
- **Example Usage:** `${heat.heatNumber}`, `${heat.group}`
- **Looping Example:** `jx:each(items="heats" var="heat" lastCell="E10")`
- **Heat Drivers:** Each heat contains a `drivers` list (`DriverHeatData`) showing driver performance specific to that heat.

### `heatSheetNames`
A list of string names for the heats, useful if you are using Jxls multisheet features to output each heat onto a separate Excel sheet.

## Basic Example

Here is a simple example of how to use Jxls markup in a cell comment to loop through the standings:

**Cell A1 Comment:**
```text
jx:area(lastCell="D2")
```

**Cell A2 Comment:**
```text
jx:each(items="standings" var="driver" lastCell="D2")
```

**Cell Contents (Row 2):**

| A (Rank) | B (Driver) | C (Laps) | D (Total Time) |
| :--- | :--- | :--- | :--- |
| `${driver.rank}` | `${driver.driver.name}` | `${driver.totalLaps}` | `${driver.totalTime}` |

When exported, Jxls will automatically duplicate Row 2 for every driver in the `standings` list and inject their respective properties into the cells.

For advanced usage, including conditionals and multisheet exports, please refer to the [official Jxls documentation](https://jxls.sourceforge.net/).
