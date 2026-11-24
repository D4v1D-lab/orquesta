# Orquesta

Plantilla de juego 2D hecha con [libGDX](https://libgdx.com/), derivada de **Punch It**. El flujo jugable llega hasta la selección de vehículo y una escena estática de muestra; el gameplay real se implementará encima de esta base.

## Estado actual

La plantilla incluye:

- Menú principal (Jugar / Opciones).
- Opciones con Métricas, Reiniciar y Acerca de.
- Selección entre coche, avión, bote y tren.
- Pantalla de carga animada de 5 segundos para cada vehículo.
- Escena estática congelada del vehículo elegido: fondo, vía/carretera y sprite, sin obstáculos, movimiento, temporizador ni audio.
- Menú de pausa en la escena congelada: `Resumir` vuelve a la escena y `Menú` vuelve a la selección de vehículo.
- Sistema de métricas de accesibilidad (pulsaciones de ENTER, retención, informes HTML).

El gameplay de Punch It (obstáculos, carriles, finales animados y sonidos de conducción) fue eliminado del código. Los archivos de arte y audio siguen en `assets/` para que puedas reemplazarlos o reutilizarlos.

## Requisitos

| Qué necesitas | Detalle |
|---|---|
| Sistema | **Windows** (el proyecto está configurado para escritorio) |
| Java | **JDK 8 a 15**. La combinación probada es **Temurin 11** |
| Gradle | No hace falta instalarlo; el repo incluye el wrapper |
| Git | Opcional; el repositorio ya está inicializado |

> No uses Java 16 o superior. El proyecto fija Gradle 6.7.1 y libGDX 1.10.0; el build falla deliberadamente con versiones de Java no soportadas.

## Cómo ejecutar

Desde PowerShell, en la raíz del proyecto:

```powershell
scripts\run.bat
```

El lanzador busca automáticamente un JDK compatible (8–15) y abre el juego en pantalla completa. Para cerrarlo, usa `Alt+F4`.

Alternativas:

```powershell
# Ejecutar directamente con Gradle
.\gradlew.bat desktop:run

# Comprobar que compila sin abrir el juego
.\gradlew.bat :desktop:compileJava
```

La primera ejecución puede tardar mientras Gradle descarga las dependencias.

Si el juego ya está abierto, ciérralo antes de lanzar cualquier comando de Gradle. Un proceso `desktop:run` activo puede dejar el daemon ocupado y hacer que el siguiente build se quede esperando.

## Controles

- **Menú principal:** flechas arriba/abajo o izquierda/derecha para mover la selección; `Enter` para confirmar. `Esc` cierra el juego.
- **Opciones:** flechas para elegir Métricas, Reiniciar o Acerca de; `Enter` activa la opción. Métricas y Acerca abren una página en el navegador y cierran el juego. Reiniciar pide confirmación con un segundo `Enter`; `Esc` vuelve al menú principal.
- **Selección de vehículo:** izquierda/arriba selecciona el vehículo anterior; derecha/abajo el siguiente. El orden es coche, avión, bote y tren; `Enter` empieza. `Esc` vuelve al menú principal.
- **Escena congelada:** no hay movimiento ni entrada de juego. `Esc` abre el menú de pausa.
- **Pausa:** `Resumir` (opción por defecto) vuelve a la escena congelada; `Menú` vuelve a la selección de vehículo. `Esc` también reanuda.

## Flujo

```text
MainMenuScreen
├── Jugar ──> VehicleSelectScreen ──> GiftScreen (5 s) ──> StaticVehicleScreen
│                                                              └── Esc ──> PauseMenu ──> VehicleSelectScreen / Resumir
└── Opciones ──> OptionsScreen ──> MainMenuScreen
```

## Estructura del proyecto

```text
assets/
├── artifacts/  # Plantillas HTML de Métricas y de Acerca de
├── cars/       # Recursos del coche
├── common/     # Menú, botones, pantalla de carga y recursos compartidos
├── plane/      # Recursos del avión
├── boat/       # Recursos del bote
└── train/      # Recursos del tren

core/src/com/mygdx/game/
├── OrquestaGame.java           # AssetManager y creación de pantallas
├── MainMenuScreen.java         # Menú principal
├── OptionsScreen.java          # Acerca de / Reiniciar / Métricas
├── GestorMetricas.java         # Métricas persistentes y generación HTML
├── MetricasInputListener.java  # Pulsaciones de ENTER para métricas
├── VehicleSelectScreen.java    # Selección coche/avión/bote/tren
├── VehicleType.java            # Enumeración de vehículos
├── GiftScreen.java             # Transición compartida de carga
├── StaticVehicleScreen.java    # Escena congelada por vehículo
├── PauseMenu.java              # Menú de pausa (Resumir / Menú)
└── BaseScreen.java             # Base común de pantallas

desktop/src/com/mygdx/game/desktop/
└── DesktopLauncher.java        # Arranque de escritorio (LWJGL 2)

scripts/
└── run.bat                     # Lanzador para Windows
```

## Recursos

Los recursos de runtime viven en `assets/`. Usa siempre el prefijo de la carpeta en las claves del `AssetManager` (`cars/`, `common/`, `plane/`, `boat/`, `train/`).

Recursos que la plantilla carga actualmente:

- **Comunes:** `main_menu.jpg`, `jugar_hover.jpg`, `opciones.jpg`, `acerca.jpg`, `metricas.jpg`, `reinicio.jpg`, `journey_awaits.mp3`, `option_change.wav`, `racing_menu.ogg`, `load_gift_sheet.png`, `resumir.png`, `menu.png`.
- **Coche:** `cars/carro.png`, `cars/mosaico_cesped.jpg`, `cars/road.jpg`, `cars/car_start.mp3`.
- **Avión:** `plane/avion.png`, `plane/mosaico_cielo.jpg`, `plane/road_cielo.jpg`, `plane/avion_start.mp3`.
- **Bote:** `boat/bote.png`, `boat/mosaico_mar.jpg`, `boat/road_mar.jpg`, `boat/bote_start.mp3`.
- **Tren:** `train/tren.png`, `train/mosaico_tren.jpg`, `train/road_tierra.jpg`, `train/tren_start.mp3`.

Los recursos de gameplay (obstáculos, hojas de animación final, sonidos de motor/bocina/derrape) permanecen en disco pero **no se cargan**. Si los usas al implementar el juego, añade sus `manager.load(...)` en `OrquestaGame.create()`.

Los informes de Métricas se generan desde `assets/artifacts/orquesta_metricas.html` y `orquesta_guia.html` hacia `orquesta_metricas.html` y `orquesta_guia.html` en el directorio de ejecución.

## Cómo añadir el gameplay

1. Crea una pantalla nueva que extienda `BaseScreen` (o reutiliza `StaticVehicleScreen` como punto de partida).
2. En `OrquestaGame.create()`, cárgala y añade su getter; en `dispose()`, libera la pantalla.
3. En `GiftScreen.render()`, sustituye `game.setScreen(game.getStaticScreen(vehicle))` por la pantalla de tu modo.
4. Añade en `OrquestaGame.create()` los `manager.load(...)` de los recursos que use tu pantalla.
5. Si registras métricas, usa `GestorMetricas.registrarTiempoJuego(...)`, `registrarObstaculoEsquivado()` y `registrarChoque()` como en la versión original.

En `AGENTS.md` tienes las constantes de cada escena (recortes de sprites, anchos de vía/carretera, carriles) y las notas de adaptación.

## Métricas de accesibilidad

Orquesta mantiene el sistema de métricas de Punch It mediante `Preferences` (clave `Orquesta_Metricas_Save`). Desde **Opciones → Métricas** se genera un informe local y se abre en el navegador. **Reiniciar** borra las métricas con una confirmación de dos pasos.

Mientras no exista gameplay, los valores de obstáculos, choques, rachas, anticipación y tiempo por vehículo se quedarán en cero; las pulsaciones de ENTER y la retención del botón sí se registran.

## Notas técnicas

- **libGDX 1.10.0**, **Gradle 6.7.1** y **LWJGL 2**.
- Ventana en pantalla completa con `StretchViewport(1376×768)`.
- Código compatible con Java 7 (`sourceCompatibility = 1.7`): sin lambdas ni características modernas.
- Solo existen los módulos `:core` y `:desktop`; no hay objetivos Android ni de otras plataformas.
- Box2D está incluido como dependencia, pero actualmente no se usa.
- No hay tests, lint ni CI. La verificación es compilar y abrir el juego.
- En JDK 11 es normal ver una advertencia de *illegal reflective access* de LWJGL 2; no impide jugar.

## Problemas frecuentes

- **`Unsupported class file major version` o error de Gradle al arrancar:** estás usando Java 16 o superior. Instala Temurin 11 y vuelve a ejecutar `scripts\run.bat`.
- **El build se queda colgado:** cierra el juego y cualquier proceso `DesktopLauncher` antes de volver a compilar.
- **No se oye audio:** comprueba la salida de audio del sistema; la plantilla no silencia sonidos de forma intencionada.

## Licencia

Este repositorio no incluye un archivo de licencia. Si vas a reutilizar el código o los recursos, consulta antes con el autor.
