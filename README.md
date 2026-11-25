# Orquesta

Plantilla de juego 2D hecha con [libGDX](https://libgdx.com/), derivada de **Punch It**. El flujo jugable llega hasta la selección de vehículo y una escena estática de muestra; el gameplay real se implementará encima de esta base.

## Estado actual

La plantilla incluye:

- Menú principal (Jugar / Opciones).
- Opciones con Métricas, Reiniciar y Acerca de.
- Selección entre coche, avión, bote y tren.
- Pantalla de carga animada de 5 segundos para cada vehículo.
- Escena estática centrada del vehículo elegido: fondo y sprite, sin carretera, obstáculos ni desplazamiento de carril. `Enter` reproduce 1.5 s de derrape y agranda el instrumento con una transición suave de 1 s por paso: 1.00x → 1.67x → 2.33x → 3.00x; tres pulsaciones más lo devuelven a 1.00x. El volumen del derrape sube o baja 0.25 según crezca o se reduzca (base 0.75, máximo 1.00). El bote vibra en el eje Y y el tren en el eje X mientras cambian de tamaño.
- Cada partida dura 30 s; al terminar se reproduce el final del modo (10 s) sobre fondo negro, sin mosaico, y se vuelve a la selección de vehículo. Los finales de coche, piano, bote y tren ocupan toda la pantalla y suenan `car_drive`, `avion_drive`, `bote_drive` y `tren_drive`.
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
- **Escena congelada:** el instrumento está centrado y no se desplaza de carril. Cada `Enter` reproduce 1.5 s de derrape y avanza un paso de escala con una transición de 1 s: 1.00x → 1.67x → 2.33x → 3.00x; tras 3 pulsaciones en 3.00x, tres pulsaciones más vuelven a 1.00x. El volumen del derrape sube 0.25 al crecer y baja 0.25 al reducirse (base 0.75, máximo 1.00). El bote vibra brevemente en el eje Y y el tren en el eje X durante cada cambio de tamaño. `Esc` abre el menú de pausa. La partida dura 30 s; después se reproduce el final del modo (10 s) sin poder saltarlo y se vuelve a la selección de vehículo.
- **Pausa:** `Resumir` (opción por defecto) vuelve a la escena congelada; `Menú` vuelve a la selección de vehículo. `Esc` también reanuda.

## Flujo

```text
MainMenuScreen
├── Jugar ──> VehicleSelectScreen ──> GiftScreen (5 s) ──> StaticVehicleScreen
│                                                              ├── Esc ──> PauseMenu ──> VehicleSelectScreen / Resumir
│                                                              └── 30 s ──> EndingScreen (10 s) ──> VehicleSelectScreen
└── Opciones ──> OptionsScreen ──> MainMenuScreen
```

## Estructura del proyecto

```text
assets/
├── artifacts/  # Plantillas HTML de Métricas y de Acerca de
├── common/     # Menú, botones, pantalla de carga y recursos compartidos
├── guitarras/  # Recursos del coche (guitarra)
├── pianos/     # Recursos del avión (piano)
├── tambor/     # Recursos del bote (tambor)
└── maracas/    # Recursos del tren (maracas)

core/src/com/mygdx/game/
├── OrquestaGame.java           # AssetManager y creación de pantallas
├── MainMenuScreen.java         # Menú principal
├── OptionsScreen.java          # Acerca de / Reiniciar / Métricas
├── GestorMetricas.java         # Métricas persistentes y generación HTML
├── MetricasInputListener.java  # Retención de ENTER para métricas
├── VehicleSelectScreen.java    # Selección coche/avión/bote/tren
├── VehicleType.java            # Enumeración de vehículos
├── GiftScreen.java             # Transición compartida de carga
├── StaticVehicleScreen.java    # Escena congelada por vehículo (30 s)
├── EndingScreen.java           # Final animado por vehículo
├── PauseMenu.java              # Menú de pausa (Resumir / Menú)
└── BaseScreen.java             # Base común de pantallas

desktop/src/com/mygdx/game/desktop/
└── DesktopLauncher.java        # Arranque de escritorio (LWJGL 2)

scripts/
└── run.bat                     # Lanzador para Windows
```

## Recursos

Los recursos de runtime viven en `assets/`. Usa siempre el prefijo de la carpeta en las claves del `AssetManager` (`guitarras/`, `common/`, `pianos/`, `tambor/`, `maracas/`).

Recursos que la plantilla carga actualmente:

- **Comunes:** `main_menu.jpg`, `jugar_hover.jpg`, `opciones.jpg`, `acerca.jpg`, `metricas.jpg`, `reinicio.jpg`, `journey_awaits.mp3`, `option_change.wav`, `racing_menu.ogg`, `load_gift_sheet.png`, `resumir.png`, `menu.png`.
- **Coche:** `guitarras/guitarra.png`, `guitarras/mosaico_cesped.jpg`, `guitarras/car_start.mp3`, `guitarras/car_drift.mp3`, `guitarras/car_ending_sheet.png`, `guitarras/car_drive.mp3`.
- **Avión:** `pianos/piano.png`, `pianos/mosaico_cielo.jpg`, `pianos/avion_start.mp3`, `pianos/avion_drift.mp3`, `pianos/avion_ending_sheet.png`, `pianos/avion_drive.mp3`.
- **Bote:** `tambor/drum.png`, `tambor/mosaico_mar.jpg`, `tambor/bote_start.mp3`, `tambor/bote_drift.mp3`, `tambor/bote_ending_sheet.png`, `tambor/bote_drive.mp3`.
- **Tren:** `maracas/maraca.png`, `maracas/mosaico_tren.jpg`, `maracas/tren_start.mp3`, `maracas/tren_drift.mp3`, `maracas/tren_ending_sheet.png`, `maracas/tren_drive.mp3`.

Los cuatro finales usan hojas pre-generadas de 150 fotogramas en rejilla 10×15 (480×270, 1/15 s). La única GIF fuente que queda es `guitarras/carro_ending.gif`. Los sprites antiguos (`carro.png`, `avion.png`, `bote.png`, `tren.png`) siguen en las carpetas nuevas pero no se cargan. Los recursos de gameplay que no se usan (obstáculos, bocinas y algunos sonidos de motor) permanecen en disco pero **no se cargan**. Si los usas al implementar el juego, añade sus `manager.load(...)` en `OrquestaGame.create()`.

Los informes de Métricas se generan desde `assets/artifacts/orquesta_metricas.html` y `orquesta_guia.html` hacia `orquesta_metricas.html` y `orquesta_guia.html` en el directorio de ejecución.

## Cómo añadir el gameplay

1. Crea una pantalla nueva que extienda `BaseScreen` (o reutiliza `StaticVehicleScreen` como punto de partida).
2. En `OrquestaGame.create()`, cárgala y añade su getter; en `dispose()`, libera la pantalla.
3. En `GiftScreen.render()`, sustituye `game.setScreen(game.getStaticScreen(vehicle))` por la pantalla de tu modo.
4. Añade en `OrquestaGame.create()` los `manager.load(...)` de los recursos que use tu pantalla.
5. Si necesitas métricas adicionales, extiende `GestorMetricas`; la partida ya registra pulsaciones y duración al completar los 30 s.

En `AGENTS.md` tienes las constantes de cada escena (recortes de sprites, tamaños y posiciones) y las notas de adaptación.

## Métricas de accesibilidad

Orquesta mantiene las métricas mediante `Preferences` (clave `Orquesta_Metricas_Save`). Desde **Opciones → Métricas** se genera un informe local y se abre en el navegador con cuatro valores: pulsaciones por juego, pulsaciones totales, retención de botón y duración media. Solo cuentan las partidas completadas de 30 s; las salidas anticipadas se ignoran. La retención de botón también se mide únicamente dentro de esas partidas. **Reiniciar** pone todas las métricas a cero tras una confirmación de dos pasos.

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
