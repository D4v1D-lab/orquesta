package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class StaticVehicleScreen extends BaseScreen {

    private static final float WORLD_WIDTH = 1376f;
    private static final float WORLD_HEIGHT = 768f;
    private static final float VEHICLE_Y = 45f;
    private static final int START_LANE = 1;
    private static final int LANE_COUNT = 4;

    private final Stage stage;
    private final PauseMenu pauseMenu;
    private boolean paused;

    public StaticVehicleScreen(OrquestaGame game, VehicleType vehicle) {
        super(game);
        stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));
        pauseMenu = new PauseMenu(game);

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

    private void buildCarScene() {
        addBackdrop("cars/mosaico_cesped.jpg", 1376f);
        float[] lane = addHeightFitRoad("cars/road.jpg", 10f, 398f);
        addVehicle("cars/carro.png", 190, 134, 120, 227, 90f, lane);
    }

    private void buildPlaneScene() {
        addBackdrop("plane/mosaico_cielo.jpg", WORLD_HEIGHT);
        float[] lane = addHeightFitRoad("plane/road_cielo.jpg", 10f, 348f);
        addVehicle("plane/avion.png", 256, 287, 512, 490, 100f, lane);
    }

    private void buildBoatScene() {
        addBackdrop("boat/mosaico_mar.jpg", WORLD_HEIGHT);
        float[] lane = addHeightFitRoad("boat/road_mar.jpg", 20f, 454f);
        addVehicle("boat/bote.png", 179, 80, 142, 341, 80f, lane);
    }

    private void buildTrainScene() {
        addBackdrop("train/mosaico_tren.jpg", 2284.16f);
        float[] lane = addFixedRoad("train/road_tierra.jpg", 330f, 20f, 127f);
        addVehicle("train/tren.png", 209, 47, 82, 391, 60f, lane);
    }

    private void addBackdrop(String path, float height) {
        Texture texture = game.getManager().get(path, Texture.class);
        Image backdrop = new Image(texture);
        backdrop.setBounds(0f, 0f, WORLD_WIDTH, height);
        stage.addActor(backdrop);
    }

    private float[] addHeightFitRoad(String path, float borderPx, float asphaltPx) {
        Texture texture = game.getManager().get(path, Texture.class);
        float roadWidth = WORLD_HEIGHT * texture.getWidth() / texture.getHeight();
        float roadLeft = (WORLD_WIDTH - roadWidth) / 2f;
        Image road = new Image(texture);
        road.setBounds(roadLeft, 0f, roadWidth, WORLD_HEIGHT);
        stage.addActor(road);
        return laneBounds(roadLeft, roadWidth, texture.getWidth(), borderPx, asphaltPx);
    }

    private float[] addFixedRoad(String path, float width, float borderPx, float asphaltPx) {
        Texture texture = game.getManager().get(path, Texture.class);
        float roadLeft = (WORLD_WIDTH - width) / 2f;
        Image road = new Image(texture);
        road.setBounds(roadLeft, 0f, width, WORLD_HEIGHT);
        stage.addActor(road);
        return laneBounds(roadLeft, width, texture.getWidth(), borderPx, asphaltPx);
    }

    private float[] laneBounds(float roadLeft, float roadWidth, float textureWidth,
            float borderPx, float asphaltPx) {
        float textureScale = roadWidth / textureWidth;
        float asphaltLeft = roadLeft + borderPx * textureScale;
        float laneWidth = asphaltPx * textureScale / LANE_COUNT;
        return new float[] {asphaltLeft, laneWidth};
    }

    private void addVehicle(String path, int cropX, int cropY, int cropWidth, int cropHeight,
            float vehicleWidth, float[] lane) {
        Texture texture = game.getManager().get(path, Texture.class);
        TextureRegion region = new TextureRegion(texture, cropX, cropY, cropWidth, cropHeight);
        Image vehicle = new Image(new TextureRegionDrawable(region));
        float vehicleHeight = vehicleWidth * region.getRegionHeight() / region.getRegionWidth();
        float centerX = lane[0] + lane[1] * (START_LANE + 0.5f);
        vehicle.setBounds(centerX - vehicleWidth / 2f, VEHICLE_Y, vehicleWidth, vehicleHeight);
        stage.addActor(vehicle);
    }

    @Override
    public void show() {
        paused = false;
        pauseMenu.hide();
    }

    @Override
    public void hide() {
        paused = false;
        pauseMenu.hide();
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
            pauseMenu.show();
            drawFrame();
            pauseMenu.draw();
            return;
        }

        drawFrame();
    }

    private void resumeGame() {
        paused = false;
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
        pauseMenu.dispose();
        stage.dispose();
    }
}
