package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class EndingScreen extends BaseScreen {

    private static final float WORLD_WIDTH = 1376f;
    private static final float WORLD_HEIGHT = 768f;
    private static final float FRAME_DURATION = 1f / 15f;
    private static final float SOUND_VOLUME = 0.5f;

    private final Stage stage;
    private final TextureRegionDrawable drawable;
    private final Animation<TextureRegion> animation;
    private final Sound sound;
    private final boolean loopSound;
    private final float duration;
    private float elapsed;

    public EndingScreen(OrquestaGame game, VehicleType vehicle) {
        super(game);
        stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));

        int frameCount;
        int cols;
        int rows;
        int frameWidth;
        int frameHeight;
        String sheetKey;
        String soundKey;
        boolean fullScreen;
        boolean loop;

        if (vehicle == VehicleType.CAR) {
            frameCount = 150;
            cols = 10;
            rows = 15;
            frameWidth = 480;
            frameHeight = 270;
            sheetKey = "guitarras/car_ending_sheet.png";
            soundKey = "guitarras/car_drive.mp3";
            fullScreen = true;
            loop = false;
        } else if (vehicle == VehicleType.PLANE) {
            frameCount = 150;
            cols = 10;
            rows = 15;
            frameWidth = 480;
            frameHeight = 270;
            sheetKey = "pianos/avion_ending_sheet.png";
            soundKey = "pianos/avion_drive.mp3";
            fullScreen = true;
            loop = false;
        } else if (vehicle == VehicleType.BOAT) {
            frameCount = 150;
            cols = 10;
            rows = 15;
            frameWidth = 480;
            frameHeight = 270;
            sheetKey = "tambor/bote_ending_sheet.png";
            soundKey = "tambor/bote_drive.mp3";
            fullScreen = true;
            loop = false;
        } else {
            frameCount = 150;
            cols = 10;
            rows = 15;
            frameWidth = 480;
            frameHeight = 270;
            sheetKey = "maracas/tren_ending_sheet.png";
            soundKey = "maracas/tren_drive.mp3";
            fullScreen = true;
            loop = false;
        }

        Texture sheet = game.getManager().get(sheetKey, Texture.class);
        TextureRegion[][] grid = TextureRegion.split(sheet, frameWidth, frameHeight);
        TextureRegion[] frames = new TextureRegion[frameCount];
        int index = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (index < frameCount) {
                    frames[index++] = grid[row][col];
                }
            }
        }

        animation = new Animation<TextureRegion>(FRAME_DURATION, frames);
        animation.setPlayMode(Animation.PlayMode.NORMAL);

        drawable = new TextureRegionDrawable(frames[0]);
        Image image = new Image(drawable);
        if (fullScreen) {
            image.setBounds(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT);
        } else {
            float width = WORLD_HEIGHT * frameWidth / frameHeight;
            image.setBounds((WORLD_WIDTH - width) / 2f, 0f, width, WORLD_HEIGHT);
        }
        stage.addActor(image);

        sound = game.getManager().get(soundKey, Sound.class);
        loopSound = loop;
        duration = frameCount * FRAME_DURATION;
    }

    @Override
    public void show() {
        elapsed = 0f;
        drawable.setRegion(animation.getKeyFrame(0f));
        sound.stop();
        if (loopSound) {
            sound.loop(SOUND_VOLUME);
        } else {
            sound.play(SOUND_VOLUME);
        }
    }

    @Override
    public void hide() {
        sound.stop();
    }

    @Override
    public void render(float delta) {
        elapsed += delta;
        drawable.setRegion(animation.getKeyFrame(elapsed));

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();

        if (elapsed >= duration) {
            sound.stop();
            game.setScreen(game.getVehicleSelectScreen());
        }
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        sound.stop();
        stage.dispose();
    }
}
