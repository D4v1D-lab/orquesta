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
- Include the project constraints in implementation briefs: Java 7 only, Windows/libGDX stack, asset keys use the folder prefix (`cars/`, `common/`, `plane/`, `boat/`, `train/`), no comments, and no Gradle while a game is running.
- Assign one owner per file. A reliable split is: the main agent owns shared wiring (`OrquestaGame`, `VehicleSelectScreen`, `GiftScreen`); one sub-agent owns a single new screen/file; another sub-agent reviews it.
- Do not ask sub-agents to commit, push, clean, reset, or run destructive git commands. Final integration, compile, and smoke tests remain with the main agent.
- Require a short report: files touched, key decisions, assumptions, checks performed, and remaining risks. Review every edit with `git diff` and a compile before continuing.

## Flow / architecture

- `OrquestaGame` (extends `Game`) owns the `AssetManager` and pre-creates all screens. Flow: `MainMenuScreen` → `VehicleSelectScreen` (Jugar) or `OptionsScreen` (Opciones); from vehicle select: car: `carGiftScreen` → `carStaticScreen`; plane: `planeGiftScreen` → `planeStaticScreen`; boat: `boatGiftScreen` → `boatStaticScreen`; train: `trainGiftScreen` → `trainStaticScreen`. The frozen scene returns to `VehicleSelectScreen` through the pause menu.
- `GiftScreen` shows `common/load_gift_sheet.png` for 5 seconds and plays the vehicle start sound; then it calls `game.setScreen(game.getStaticScreen(vehicle))`. This is the main hook for new gameplay.
- `StaticVehicleScreen` builds a **frozen** scene per `VehicleType`: backdrop, road/lane art, and the vehicle sprite. It has no obstacles, movement, timer, audio, or metrics. `Esc` opens `PauseMenu`; `Resumir` closes it, `Menu` returns to `VehicleSelectScreen`.
- `PauseMenu` is shared and intact: ESC during the frozen scene opens it, Resumir is selected by default, Menu returns to `VehicleSelectScreen`.
- `OptionsScreen`: Métricas (top), Reiniciar (middle), Acerca (bottom) at `360×160`, Métricas selected by default. Métricas calls `GestorMetricas.abrirPaginaMetricas()`; Acerca opens `assets/artifacts/acerca.html`; both exit the app so the fullscreen game does not remain stuck behind the browser on a single monitor. Reiniciar uses two Enter presses and calls `GestorMetricas.reiniciarMetricas()`. ESC cancels a pending reset or returns to `MainMenuScreen`.
- `VehicleSelectScreen` shows car, plane, boat, and train side by side, equal height (`130`), bottoms at `y = 45`, centers spaced `200` apart at `388`, `588`, `788`, `988`. Left/Up selects the previous vehicle, Right/Down the next (clamped); Enter starts the selected mode. ESC returns to `MainMenuScreen`.
- All screens use `StretchViewport(1376×768)`; the desktop window is fullscreen via LWJGL 2.
- Input map: main menu arrows + Enter select Jugar/Opciones, ESC exits the app; Options arrows + Enter activate, ESC returns to MainMenu; vehicle select arrows + Enter, ESC returns to MainMenu; frozen scene `Esc` opens the pause menu. There is no mouse input.

## Static scene constants (useful when rebuilding gameplay)

`StaticVehicleScreen` uses `WORLD_WIDTH = 1376`, `WORLD_HEIGHT = 768`, `VEHICLE_Y = 45`, `START_LANE = 1`, `LANE_COUNT = 4`.

| Mode | Backdrop | Road | Vehicle crop | Vehicle width | Road border / asphalt (texture px) |
|---|---|---|---|---|---|
| CAR | `cars/mosaico_cesped.jpg` at `1376×1376` | `cars/road.jpg`, height-fit | `cars/carro.png` `(190,134,120,227)` | 90 | 10 / 398 |
| PLANE | `plane/mosaico_cielo.jpg` at `1376×768` | `plane/road_cielo.jpg`, height-fit | `plane/avion.png` `(256,287,512,490)` | 100 | 10 / 348 |
| BOAT | `boat/mosaico_mar.jpg` at `1376×768` | `boat/road_mar.jpg`, height-fit | `boat/bote.png` `(179,80,142,341)` | 80 | 20 / 454 |
| TRAIN | `train/mosaico_tren.jpg` at `1376×2284.16` | `train/road_tierra.jpg`, fixed width `330` | `train/tren.png` `(209,47,82,391)` | 60 | 20 / 127 |

Height-fit road math: `roadWidth = 768 * texW / texH`, centered; `textureScale = roadWidth / texW`; `asphaltLeft = roadLeft + border * textureScale`; `laneWidth = asphalt * textureScale / 4`; `laneCenter(lane) = asphaltLeft + laneWidth * (lane + 0.5f)`.

Original gameplay screens used two scrolling copies of the backdrop (grass/sky/sea/train). The template uses one static copy because nothing moves. If you restore scrolling, re-check the tile heights: car grass `1376`, plane sky `768`, boat sea `768`, train mosaic `2284.16`, with scroll speed `90 px/s` in the original.

## Assets

- Runtime assets live in **`/assets`** (not `android/assets`). `desktop/build.gradle` sets `workingDir` to `../assets`. Layout: `assets/cars/` car-only, `assets/common/` shared/menu, `assets/plane/` plane-only, `assets/boat/` boat-only, `assets/train/` train-only. Always use the matching prefix in `AssetManager` keys.
- **Loaded by the template** (`OrquestaGame.create()`):
  - `common/`: `main_menu.jpg`, `jugar_hover.jpg`, `opciones.jpg`, `acerca.jpg`, `metricas.jpg`, `reinicio.jpg`, `journey_awaits.mp3`, `option_change.wav`, `racing_menu.ogg`, `load_gift_sheet.png`, `resumir.png`, `menu.png`.
  - `cars/`: `carro.png`, `mosaico_cesped.jpg`, `road.jpg`, `car_start.mp3`.
  - `plane/`: `avion.png`, `mosaico_cielo.jpg`, `road_cielo.jpg`, `avion_start.mp3`.
  - `boat/`: `bote.png`, `mosaico_mar.jpg`, `road_mar.jpg`, `bote_start.mp3`.
  - `train/`: `tren.png`, `mosaico_tren.jpg`, `road_tierra.jpg`, `tren_start.mp3`.
- **Present on disk but NOT loaded** (gameplay leftovers; safe to replace or delete once you no longer need them): `cars/car_horn.mp3`, `cars/car_drive.wav`, `cars/car_drift.mp3`, `cars/car_ending_sheet.png`, `cars/cono.png`; `plane/cono_cielo.png`, `plane/avion_drive.mp3`, `plane/avion_drift.mp3`, `plane/avion_ending_sheet.png`; `boat/cono_mar.png`, `boat/bote_horn.mp3`, `boat/bote_drive.mp3`, `boat/bote_drift.mp3`, `boat/bote_ending_sheet.png`; `train/cono_tierra.png`, `train/tren_horn.mp3`, `train/tren_drive.mp3`, `train/tren_drive_loop.mp3`, `train/tren_drift.mp3`, `train/tren_ending_sheet.png`.
- libGDX cannot load GIFs. The gift transition uses the pre-generated `common/load_gift_sheet.png` (75 frames, 8×10 grid, 480×270, 1/15 s). Mode ending sheets are 150 frames, 15×10 grid, 288×511 each, 1/15 s (~10 s) if you restore endings.
- Cropped sprite regions: car `cars/carro.png` = `(190,134,120,227)`; cone `cars/cono.png` = `(192,169,116,162)`; plane `plane/avion.png` = `(256,287,512,490)`; sky obstacle `plane/cono_cielo.png` = `(159,194,182,112)`; boat `boat/bote.png` = `(179,80,142,341)`; sea obstacle `boat/cono_mar.png` = `(199,144,102,212)`; train `train/tren.png` = `(209,47,82,391)` including rails; ground obstacle `train/cono_tierra.png` = `(145,89,206,262)` including smoke.
- `cars/road.jpg` is 418×768 (`ROAD_BORDER_TEX_PX = 10`, `ROAD_ASPHALT_TEX_PX = 398`); `plane/road_cielo.jpg` is 368×765 (`10`, `348`); `boat/road_mar.jpg` is 494×768 (`20`, `454`); `train/road_tierra.jpg` is 167×754 (`20`, `127`), drawn at fixed width `330`. Changing any road art requires re-checking those constants.
- `plane/mosaico_cielo.jpg` and `boat/mosaico_mar.jpg` are 1376×768; `train/mosaico_tren.jpg` is 800×1328 drawn at `1376` wide and `2284.16` tall; `cars/mosaico_cesped.jpg` is the car grass tile. None of these mosaics is perfectly seamless top-to-bottom.
- `common/resumir.png` and `common/menu.png` are the pause buttons, cropped `(39,155,422,190)` and `(79,155,342,191)` and rendered at `360×160` by `PauseMenu`.
- `common/acerca.jpg`, `common/metricas.jpg`, and `common/reinicio.jpg` are the Options buttons, standardized to `360×160` by `OptionsScreen`. `assets/artifacts/acerca.html` is the styled about/dedication page opened by Acerca. Metrics reports are generated from `assets/artifacts/orquesta_metricas.html` and `orquesta_guia.html` into `Gdx.files.local("orquesta_metricas.html")` and `orquesta_guia.html`.
- `GestorMetricas` persists metrics in `Orquesta_Metricas_Save`; `MetricasInputListener` counts ENTER presses/holds. Gameplay screens in the original also recorded resolved threats, dodges/hits, streaks, anticipation, and driving time per mode; that code was removed with the screens.
- Audio currently used: `common/journey_awaits.mp3` (main menu, `0.5`), `common/racing_menu.ogg` (vehicle select and Options, `0.75`), `common/option_change.wav` (`0.75`), and the four vehicle start sounds in `GiftScreen`. Volumes are tuned manually by the user; do not “fix” them without being asked.
- Box2D is a dependency but currently unused in code.

## How to add new gameplay

1. Create a screen extending `BaseScreen` (or copy `StaticVehicleScreen` as a starting point).
2. Add the screen field/getter/creation in `OrquestaGame`, and dispose it in `OrquestaGame.dispose()`.
3. In `GiftScreen.render()`, replace `game.setScreen(game.getStaticScreen(vehicle))` with `game.setScreen(game.getYourScreen(vehicle))` (or route per `VehicleType`).
4. Add every new resource with `manager.load(...)` in `OrquestaGame.create()` before `finishLoading()`.
5. Restore metrics hooks with `GestorMetricas.registrarTiempoJuego(...)`, `registrarObstaculoEsquivado()`, `registrarChoque()`, and `registrarAnticipacion(...)`.
6. Keep the pause flow: on `Esc`, call `pauseMenu.show()`; on `ACTION_RESUME`, resume; on `ACTION_MENU`, `game.setScreen(game.getVehicleSelectScreen())`.

## Current state / next steps

- The template compiles and the full menu → vehicle select → gift → frozen scene → pause flow is wired.
- Gameplay is intentionally absent. The four original mode screens (`GameScreen`, `AirplaneScreen`, `BoatScreen`, `TrainScreen`) were deleted.
- Assets for the old gameplay remain on disk but are not loaded; they are free to be replaced.
- Metrics remain functional but mode time/dodge counters stay at zero until gameplay is re-added.
- The repository was re-initialized fresh (no upstream `punchit/punch-it` remote).
