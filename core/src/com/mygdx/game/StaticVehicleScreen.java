package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class StaticVehicleScreen extends BaseScreen {

    private static final float WORLD_WIDTH = 1376f;
    private static final float WORLD_HEIGHT = 768f;
    private static final float MIN_SCALE = 1f;
    private static final float MAX_SCALE = 3f;
    private static final int SCALE_STEPS = 3;
    private static final float STEP_DURATION = 1f;
    private static final float SCALE_STEP = (MAX_SCALE - MIN_SCALE) / SCALE_STEPS;
    private static final float SCALE_SPEED = SCALE_STEP / STEP_DURATION;
    private static final float DRIFT_DURATION = 1.5f;
    private static final float DRIFT_VOLUME_BASE = 0.75f;
    private static final float DRIFT_VOLUME_STEP = 0.25f;
    private static final float DRIFT_VOLUME_MIN = 0f;
    private static final float DRIFT_VOLUME_MAX = 1f;
    private static final float RUN_DURATION = 30f;
    private static final float SHAKE_AMPLITUDE = 6f;
    private static final float SHAKE_HZ = 18f;
    private static final float SHAKE_ANGULAR_SPEED = MathUtils.PI2 * SHAKE_HZ;

    private final Stage stage;
    private final PauseMenu pauseMenu;
    private final Sound drift;
    private final VehicleType vehicleType;
    private final boolean percussive;
    private Image vehicle;
    private boolean paused;
    private float vehicleBaseX;
    private float vehicleBaseY;
    private float shakeTime;
    private float driftTimer;
    private float driftVolume = DRIFT_VOLUME_BASE;
    private float runTimer;
    private int runPresses;
    private int scaleLevel;
    private int scaleDirection = 1;
    private float currentScale = MIN_SCALE;
    private float targetScale = MIN_SCALE;

    public StaticVehicleScreen(OrquestaGame game, VehicleType vehicle) {
        super(game);
        vehicleType = vehicle;
        percussive = vehicle == VehicleType.BOAT || vehicle == VehicleType.TRAIN;
        stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));
        pauseMenu = new PauseMenu(game);
        drift = game.getManager().get(driftKey(vehicle), Sound.class);

        if (vehicle == VehicleType.CAR) {
            buildCarScene();
        } else if (vehicle == VehicleType.PLANE) {
            buildPlaneScene();
        } else if (vehicle == VehicleType.BOAT) {
            buildBoatScene();
        } else {
            buildTrainScene();
        }
    }

    private String driftKey(VehicleType vehicle) {
        if (vehicle == VehicleType.CAR) {
            return "guitarras/car_drift.mp3";
        }
        if (vehicle == VehicleType.PLANE) {
            return "pianos/avion_drift.mp3";
        }
        if (vehicle == VehicleType.BOAT) {
            return "tambor/bote_drift.mp3";
        }
        return "maracas/tren_drift.mp3";
    }

    private void buildCarScene() {
        addBackdrop("guitarras/mosaico_cesped.jpg");
        addVehicle("guitarras/guitarra.png", 119, 119, 262, 262, 90f);
    }

    private void buildPlaneScene() {
        addBackdrop("pianos/mosaico_cielo.jpg");
        addVehicle("pianos/piano.png", 104, 144, 292, 257, 100f);
    }

    private void buildBoatScene() {
        addBackdrop("tambor/mosaico_mar.jpg");
        addVehicle("tambor/drum.png", 114, 119, 272, 267, 80f);
    }

    private void buildTrainScene() {
        addBackdrop("maracas/mosaico_tren.jpg");
        addVehicle("maracas/maraca.png", 139, 154, 222, 192, 60f);
    }

    private void addBackdrop(String path) {
        Texture texture = game.getManager().get(path, Texture.class);
        Image backdrop = new Image(texture);
        backdrop.setBounds(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        stage.addActor(backdrop);
    }

    private void addVehicle(String path, int cropX, int cropY, int cropWidth, int cropHeight,
            float vehicleWidth) {
        Texture texture = game.getManager().get(path, Texture.class);
        TextureRegion region = new TextureRegion(texture, cropX, cropY, cropWidth, cropHeight);
        vehicle = new Image(new TextureRegionDrawable(region));
        float vehicleHeight = vehicleWidth * region.getRegionHeight() / region.getRegionWidth();
        vehicleBaseX = (WORLD_WIDTH - vehicleWidth) / 2f;
        vehicleBaseY = (WORLD_HEIGHT - vehicleHeight) / 2f;
        vehicle.setBounds(vehicleBaseX, vehicleBaseY, vehicleWidth, vehicleHeight);
        vehicle.setOrigin(Align.center);
        stage.addActor(vehicle);
    }

    @Override
    public void show() {
        paused = false;
        GestorMetricas.iniciarJuego();
        pauseMenu.hide();
        scaleLevel = 0;
        scaleDirection = 1;
        currentScale = MIN_SCALE;
        targetScale = MIN_SCALE;
        vehicle.setScale(currentScale);
        vehicle.setX(vehicleBaseX);
        vehicle.setY(vehicleBaseY);
        shakeTime = 0f;
        driftTimer = 0f;
        driftVolume = DRIFT_VOLUME_BASE;
        runTimer = 0f;
        runPresses = 0;
        drift.stop();
    }

    @Override
    public void hide() {
        paused = false;
        GestorMetricas.finalizarJuego();
        pauseMenu.hide();
        driftTimer = 0f;
        drift.stop();
    }

    @Override
    public void render(float delta) {
        if (paused) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                resumeGame();
                return;
            }
            int action = pauseMenu.update(delta);
            if (action == PauseMenu.ACTION_RESUME) {
                resumeGame();
                return;
            }
            if (action == PauseMenu.ACTION_MENU) {
                game.setScreen(game.getVehicleSelectScreen());
                return;
            }
            drawFrame();
            pauseMenu.draw();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = true;
            GestorMetricas.suspenderJuego();
            driftTimer = 0f;
            drift.stop();
            pauseMenu.show();
            drawFrame();
            pauseMenu.draw();
            return;
        }

        runTimer += delta;
        if (runTimer >= RUN_DURATION) {
            GestorMetricas.registrarJuegoCompletado(runTimer, runPresses);
            game.setScreen(game.getEndingScreen(vehicleType));
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            runPresses++;
            int previousLevel = scaleLevel;
            advanceScale();
            if (scaleLevel > previousLevel) {
                driftVolume = Math.min(DRIFT_VOLUME_MAX, driftVolume + DRIFT_VOLUME_STEP);
            } else if (scaleLevel < previousLevel) {
                driftVolume = Math.max(DRIFT_VOLUME_MIN, driftVolume - DRIFT_VOLUME_STEP);
            }
            drift.stop();
            drift.play(driftVolume);
            driftTimer = DRIFT_DURATION;
        }

        updateDrift(delta);
        updateScale(delta);
        drawFrame();
    }

    private void advanceScale() {
        scaleLevel += scaleDirection;
        if (scaleLevel > SCALE_STEPS) {
            scaleDirection = -1;
            scaleLevel = SCALE_STEPS - 1;
        } else if (scaleLevel < 0) {
            scaleDirection = 1;
            scaleLevel = 1;
        }
        targetScale = MIN_SCALE + scaleLevel * SCALE_STEP;
    }

    private void updateDrift(float delta) {
        if (driftTimer > 0f) {
            driftTimer -= delta;
            if (driftTimer <= 0f) {
                driftTimer = 0f;
                drift.stop();
            }
        }
    }

    private void updateScale(float delta) {
        boolean resizing = currentScale != targetScale;
        if (resizing) {
            if (currentScale < targetScale) {
                currentScale = Math.min(targetScale, currentScale + SCALE_SPEED * delta);
            } else {
                currentScale = Math.max(targetScale, currentScale - SCALE_SPEED * delta);
            }
            vehicle.setScale(currentScale);
        }
        updateShake(delta, resizing);
    }

    private void updateShake(float delta, boolean resizing) {
        if (!percussive) {
            return;
        }
        vehicle.setX(vehicleBaseX);
        vehicle.setY(vehicleBaseY);
        if (resizing) {
            shakeTime += delta;
            float offset = MathUtils.sin(shakeTime * SHAKE_ANGULAR_SPEED) * SHAKE_AMPLITUDE;
            if (vehicleType == VehicleType.BOAT) {
                vehicle.setY(vehicleBaseY + offset);
            } else {
                vehicle.setX(vehicleBaseX + offset);
            }
        }
    }

    private void resumeGame() {
        paused = false;
        GestorMetricas.reanudarJuego();
        pauseMenu.hide();
    }

    private void drawFrame() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        pauseMenu.resize(width, height);
    }

    @Override
    public void dispose() {
        drift.stop();
        pauseMenu.dispose();
        stage.dispose();
    }
}
