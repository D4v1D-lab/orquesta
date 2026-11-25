# AGENTS.md

Orquesta is a **Windows-only** libGDX desktop template (package `com.mygdx.game`), derived from **Punch It**. It keeps the menus, vehicle selection, the 5-second gift transition, a frozen static scene per vehicle, the pause menu, and the accessibility metrics. The actual gameplay (obstacles, lanes, timers, endings, driving audio) has been removed from the code and must be rebuilt on top of this template. The README is in Spanish; treat `build.gradle`, `settings.gradle`, `desktop/build.gradle`, `scripts\run.bat`, and `core/src/` as the source of truth.

## Run / build

- Preferred: `scripts\run.bat` from PowerShell (auto-detects JDK 8–15; JDK 11 Temurin). It launches fullscreen; close with `Alt+F4`.
- Compile-only check: `.\gradlew.bat :desktop:compileJava`.
- There are **no tests, lint, or CI**. Verification = compile + launch the game.
- **Stop a running game before any Gradle command.** `desktop:run` keeps the Gradle daemon busy and the next Gradle invocation can hang:
  ```powershell
  Get-CimInstance Win32_Process | Where-Object { $_.Name -eq "java.exe" -and $_.CommandLine -like "*DesktopLauncher*" } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
  ```
- From WSL the Linux `./gradlew` does not work (no JDK in WSL). Call `gradlew.bat` through `powershell.exe`/`cmd.exe` with a Windows `JAVA_HOME` pointing at the Temurin 11 install (`C:\Program Files\Eclipse Adoptium\jdk-11.x.x-hotspot`).
- Versions are pinned intentionally: Gradle **6.7.1** wrapper, libGDX **1.10.0**, JDK **8–15** (11). Do not upgrade; `build.gradle` throws on Java >15. If the wrapper was accidentally upgraded (Gradle 9 fails with “requires JVM 17”), run `git restore gradle/wrapper/gradle-wrapper.properties`.
- `core`/`desktop` use `sourceCompatibility = 1.7`: keep code Java 7 compatible (no lambdas, no try-with-resources, no diamond-free assumptions, etc.).
- Only `:core` and `:desktop` exist; Android/other OS targets were removed. Do not re-add them.

## Sub-agents

- Use the `Task` tool to delegate. Use `explore` for read-only discovery, asset inspection, and independent review; use `general` for a narrowly scoped implementation or a second opinion.
- Parallelize only independent work. Safe in parallel: asset audits, code searches, research, and read-only reviews. Unsafe: two agents editing the same file, or one editing while another runs Gradle.
- Every sub-agent starts with a fresh context. Write a self-contained brief with the objective, exact file paths, already-agreed decisions/constants, whether it may edit, and the exact files it may touch. Never assume it can see this conversation.
- Include the project constraints in implementation briefs: Java 7 only, Windows/libGDX stack, asset keys use the folder prefix (`guitarras/`, `common/`, `pianos/`, `tambor/`, `maracas/`), no comments, and no Gradle while a game is running.
- Assign one owner per file. A reliable split is: the main agent owns shared wiring (`OrquestaGame`, `VehicleSelectScreen`, `GiftScreen`); one sub-agent owns a single new screen/file; another sub-agent reviews it.
- Do not ask sub-agents to commit, push, clean, reset, or run destructive git commands. Final integration, compile, and smoke tests remain with the main agent.
- Require a short report: files touched, key decisions, assumptions, checks performed, and remaining risks. Review every edit with `git diff` and a compile before continuing.

## Flow / architecture

- `OrquestaGame` (extends `Game`) owns the `AssetManager` and pre-creates all screens. Flow: `MainMenuScreen` → `VehicleSelectScreen` (Jugar) or `OptionsScreen` (Opciones); from vehicle select: car: `carGiftScreen` → `carStaticScreen`; plane: `planeGiftScreen` → `planeStaticScreen`; boat: `boatGiftScreen` → `boatStaticScreen`; train: `trainGiftScreen` → `trainStaticScreen`. After 30 seconds the static scene goes to `EndingScreen`, which returns to `VehicleSelectScreen` after the 10-second ending. The frozen scene also returns to `VehicleSelectScreen` through the pause menu.
- `GiftScreen` shows `common/load_gift_sheet.png` for 5 seconds and plays the vehicle start sound; then it calls `game.setScreen(game.getStaticScreen(vehicle))`. This is the main hook for new gameplay.
- `StaticVehicleScreen` builds a **frozen** scene per `VehicleType`: backdrop and a vehicle sprite centered on both axes. There is no road and no lane movement. `Enter` plays 1.5 s of the mode drift sound and advances one animated scale step (`1.00x → 1.67x → 2.33x → 3.00x`); three more presses step back down to `1.00x`. Boat adds a ±6 px, 18 Hz y-axis shake and train an x-axis shake while the scale animates. A 30-second run timer transitions to `EndingScreen`; the timer freezes while the pause menu is open. `Esc` opens `PauseMenu`; `Resumir` closes it, `Menu` returns to `VehicleSelectScreen`.
- `EndingScreen` draws the mode ending over black with no mosaic and returns to `VehicleSelectScreen` after ~10 seconds. Car uses the generated 10×15 `guitarras/car_ending_sheet.png` and plays `guitarras/car_drive.mp3`; piano uses the generated 10×15 `pianos/avion_ending_sheet.png` and plays `pianos/avion_drive.mp3`; boat uses the generated 10×15 `tambor/bote_ending_sheet.png` and plays `tambor/bote_drive.mp3`; train uses the generated 10×15 `maracas/tren_ending_sheet.png` and plays `maracas/tren_drive.mp3`. Input is ignored during the ending.
- `PauseMenu` is shared and intact: ESC during the frozen scene opens it, Resumir is selected by default, Menu returns to `VehicleSelectScreen`.
- `OptionsScreen`: Métricas (top), Reiniciar (middle), Acerca (bottom) at `360×160`, Métricas selected by default. Métricas calls `GestorMetricas.abrirPaginaMetricas()`; Acerca opens `assets/artifacts/acerca.html`; both exit the app so the fullscreen game does not remain stuck behind the browser on a single monitor. Reiniciar uses two Enter presses and calls `GestorMetricas.reiniciarMetricas()`. ESC cancels a pending reset or returns to `MainMenuScreen`.
- `VehicleSelectScreen` shows car, plane, boat, and train side by side, equal height (`110`), bottoms at `y = 75`, centers spaced `200` apart at `388`, `588`, `788`, `988`. Left/Up selects the previous vehicle, Right/Down the next (clamped); Enter starts the selected mode. ESC returns to `MainMenuScreen`.
- All screens use `StretchViewport(1376×768)`; the desktop window is fullscreen via LWJGL 2.
- Input map: main menu arrows + Enter select Jugar/Opciones, ESC exits the app; Options arrows + Enter activate, ESC returns to MainMenu; vehicle select arrows + Enter, ESC returns to MainMenu; frozen scene `Enter` plays 1.5 s drift and advances the scale step, `Esc` opens the pause menu. During the ending, input is ignored. There is no mouse input.

## Static scene constants (useful when rebuilding gameplay)

`StaticVehicleScreen` uses `WORLD_WIDTH = 1376`, `WORLD_HEIGHT = 768`, centers each vehicle on both axes, and starts at scale `1f`. Scale levels are `1f, 1.6667f, 2.3333f, 3f` (`SCALE_STEPS = 3`). Each `Enter` plays 1.5 s of the mode drift sound and advances one level; drift volume starts at `0.75`, rises `0.25` when growing, falls `0.25` when shrinking, and is clamped to `0-1`; at `3f`, three presses step back down to `1f`. Scale animates at `SCALE_SPEED = 0.6667f` per second (1 s per step). Percussion modes shake ±`6` px at `18` Hz during scale transitions: `BOAT` on the y-axis and `TRAIN` on the x-axis, settling back to center. Scale resets to `1f` in `show()`; drift stops on pause/leave. A `RUN_DURATION = 30f` timer transitions to the mode ending when it expires; the timer is frozen while paused. There is no road or lane system anymore.

| Mode | Backdrop | Vehicle crop | Vehicle width |
|---|---|---|---|
| CAR | `guitarras/mosaico_cesped.jpg` at `1376×768` | `guitarras/guitarra.png` `(119,119,262,262)` | 90 |
| PLANE | `pianos/mosaico_cielo.jpg` at `1376×768` | `pianos/piano.png` `(104,144,292,257)` | 100 |
| BOAT | `tambor/mosaico_mar.jpg` at `1376×768` | `tambor/drum.png` `(114,119,272,267)` | 80 |
| TRAIN | `maracas/mosaico_tren.jpg` at `1376×768` | `maracas/maraca.png` `(139,154,222,192)` | 60 |

All four backdrops are 1376×768 images drawn once at full screen. The template has no scrolling copies; if you restore scrolling, re-check the asset dimensions and scroll speed (`90 px/s` in the original).

## Assets

- Runtime assets live in **`/assets`** (not `android/assets`). `desktop/build.gradle` sets `workingDir` to `../assets`. Layout: `assets/guitarras/` car-mode assets, `assets/common/` shared/menu, `assets/pianos/` plane-mode assets, `assets/tambor/` boat-mode assets, `assets/maracas/` train-mode assets. Always use the matching prefix in `AssetManager` keys.
- **Loaded by the template** (`OrquestaGame.create()`):
  - `common/`: `main_menu.jpg`, `jugar_hover.jpg`, `opciones.jpg`, `acerca.jpg`, `metricas.jpg`, `reinicio.jpg`, `journey_awaits.mp3`, `option_change.wav`, `racing_menu.ogg`, `load_gift_sheet.png`, `resumir.png`, `menu.png`.
  - `guitarras/`: `guitarra.png`, `mosaico_cesped.jpg`, `car_start.mp3`, `car_drift.mp3`, `car_ending_sheet.png`, `car_drive.mp3`.
  - `pianos/`: `piano.png`, `mosaico_cielo.jpg`, `avion_start.mp3`, `avion_drift.mp3`, `avion_ending_sheet.png`, `avion_drive.mp3`.
  - `tambor/`: `drum.png`, `mosaico_mar.jpg`, `bote_start.mp3`, `bote_drift.mp3`, `bote_ending_sheet.png`, `bote_drive.mp3`.
  - `maracas/`: `maraca.png`, `mosaico_tren.jpg`, `tren_start.mp3`, `tren_drift.mp3`, `tren_ending_sheet.png`, `tren_drive.mp3`.
- libGDX cannot load GIFs. The gift transition uses the pre-generated `common/load_gift_sheet.png` (75 frames, 8×10 grid, 480×270, 1/15 s). Mode ending sheets are pre-generated 150-frame 10×15 grids of 480×270 at 1/15 s (~10 s); no source GIFs are kept.
- Cropped instrument regions: car `guitarras/guitarra.png` = `(119,119,262,262)`; plane `pianos/piano.png` = `(104,144,292,257)`; boat `tambor/drum.png` = `(114,119,272,267)`; train `maracas/maraca.png` = `(139,154,222,192)`.
- All four mosaics (`guitarras/mosaico_cesped.jpg`, `pianos/mosaico_cielo.jpg`, `tambor/mosaico_mar.jpg`, `maracas/mosaico_tren.jpg`) are 1376×768 and are drawn full-screen.
- `common/resumir.png` and `common/menu.png` are the pause buttons, cropped `(39,155,422,190)` and `(79,155,342,191)` and rendered at `360×160` by `PauseMenu`.
- `common/acerca.jpg`, `common/metricas.jpg`, and `common/reinicio.jpg` are the Options buttons, standardized to `360×160` by `OptionsScreen`. `assets/artifacts/acerca.html` is the styled about/dedication page opened by Acerca. Metrics reports are generated from `assets/artifacts/orquesta_metricas.html` and `orquesta_guia.html` into `Gdx.files.local("orquesta_metricas.html")` and `orquesta_guia.html`.
- `GestorMetricas` persists metrics in `Orquesta_Metricas_Save`; `MetricasInputListener` measures ENTER hold time only while a run is active. Completed 30 s runs commit total games, gameplay ENTER presses, duration, and buffered hold samples; early exits are ignored. The report and guide are generated from `assets/artifacts/orquesta_metricas.html` and `orquesta_guia.html` into `Gdx.files.local("orquesta_metricas.html")` and `orquesta_guia.html`.
- Audio currently used: `common/journey_awaits.mp3` (main menu, `0.5`), `common/racing_menu.ogg` (vehicle select and Options, `0.50`), `common/option_change.wav` (`0.75`), the four vehicle start sounds in `GiftScreen`, the four vehicle drift sounds in `StaticVehicleScreen` (dynamic volume starting at `0.75`, ±`0.25` per scale step, clamped `0-1`, cut off after `1.5 s`), and the ending sounds in `EndingScreen` at `0.5` (`car_drive.mp3`, `avion_drive.mp3`, `bote_drive.mp3`, and `tren_drive.mp3` once). Volumes are tuned manually by the user; do not “fix” them without being asked.

## How to add new gameplay

1. Create a screen extending `BaseScreen` (or copy `StaticVehicleScreen` as a starting point).
2. Add the screen field/getter/creation in `OrquestaGame`, and dispose it in `OrquestaGame.dispose()`.
3. In `GiftScreen.render()`, replace `game.setScreen(game.getStaticScreen(vehicle))` with `game.setScreen(game.getYourScreen(vehicle))` (or route per `VehicleType`).
4. Add every new resource with `manager.load(...)` in `OrquestaGame.create()` before `finishLoading()`.
5. Metrics: `StaticVehicleScreen` already records completed runs via `GestorMetricas.registrarJuegoCompletado(...)`; extend `GestorMetricas` only if you need more metrics.
6. Keep the pause flow: on `Esc`, call `pauseMenu.show()`; on `ACTION_RESUME`, resume; on `ACTION_MENU`, `game.setScreen(game.getVehicleSelectScreen())`.

## Current state / next steps

- The template compiles and the full menu → vehicle select → gift → frozen scene → pause flow is wired.
- Gameplay is intentionally absent. The four original mode screens (`GameScreen`, `AirplaneScreen`, `BoatScreen`, `TrainScreen`) were deleted.
- Unused legacy assets and the Box2D dependency were removed; the remaining assets are the runtime set loaded by `OrquestaGame`.
- The road art and lane helpers were removed; instruments are centered on both axes over the full-screen backdrop.
- `Enter` plays 1.5 s of the mode drift sound and animates the instrument through `1.00x → 1.67x → 2.33x → 3.00x`, then back down after three more presses; drift volume rises/falls `0.25` with the scale direction; there is no lane movement.
- Each run lasts 30 s, then the mode ending plays for 10 s over black (piano full-screen with `avion_drive`) before returning to vehicle selection.
- Metrics track completed-run ENTER presses (per game and total), average ENTER hold time measured only during those runs, and average completed-run duration; Reiniciar zeroes them all.
- The repository was re-initialized fresh (no upstream `punchit/punch-it` remote).
