package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;

public class OrquestaGame extends Game {

    private AssetManager manager;
    private MainMenuScreen mainMenuScreen;
    private OptionsScreen optionsScreen;
    private VehicleSelectScreen vehicleSelectScreen;
    private GiftScreen carGiftScreen;
    private GiftScreen planeGiftScreen;
    private GiftScreen boatGiftScreen;
    private GiftScreen trainGiftScreen;
    private StaticVehicleScreen carStaticScreen;
    private StaticVehicleScreen planeStaticScreen;
    private StaticVehicleScreen boatStaticScreen;
    private StaticVehicleScreen trainStaticScreen;
    private EndingScreen carEndingScreen;
    private EndingScreen planeEndingScreen;
    private EndingScreen boatEndingScreen;
    private EndingScreen trainEndingScreen;

    public AssetManager getManager() {
        return manager;
    }

    public MainMenuScreen getMainMenuScreen() {
        return mainMenuScreen;
    }

    public OptionsScreen getOptionsScreen() {
        return optionsScreen;
    }

    public VehicleSelectScreen getVehicleSelectScreen() {
        return vehicleSelectScreen;
    }

    public GiftScreen getCarGiftScreen() {
        return carGiftScreen;
    }

    public GiftScreen getPlaneGiftScreen() {
        return planeGiftScreen;
    }

    public GiftScreen getBoatGiftScreen() {
        return boatGiftScreen;
    }

    public GiftScreen getTrainGiftScreen() {
        return trainGiftScreen;
    }

    public StaticVehicleScreen getStaticScreen(VehicleType vehicle) {
        if (vehicle == VehicleType.CAR) {
            return carStaticScreen;
        }
        if (vehicle == VehicleType.PLANE) {
            return planeStaticScreen;
        }
        if (vehicle == VehicleType.BOAT) {
            return boatStaticScreen;
        }
        return trainStaticScreen;
    }

    public EndingScreen getEndingScreen(VehicleType vehicle) {
        if (vehicle == VehicleType.CAR) {
            return carEndingScreen;
        }
        if (vehicle == VehicleType.PLANE) {
            return planeEndingScreen;
        }
        if (vehicle == VehicleType.BOAT) {
            return boatEndingScreen;
        }
        return trainEndingScreen;
    }

    @Override
    public void create() {
        manager = new AssetManager();
        manager.load("common/main_menu.jpg", Texture.class);
        manager.load("common/jugar_hover.jpg", Texture.class);
        manager.load("common/opciones.jpg", Texture.class);
        manager.load("common/acerca.jpg", Texture.class);
        manager.load("common/metricas.jpg", Texture.class);
        manager.load("common/reinicio.jpg", Texture.class);
        manager.load("common/journey_awaits.mp3", Music.class);
        manager.load("common/option_change.wav", Sound.class);
        manager.load("common/racing_menu.ogg", Music.class);
        manager.load("common/load_gift_sheet.png", Texture.class);
        manager.load("common/resumir.png", Texture.class);
        manager.load("common/menu.png", Texture.class);
        manager.load("guitarras/guitarra.png", Texture.class);
        manager.load("guitarras/mosaico_cesped.jpg", Texture.class);
        manager.load("guitarras/car_start.mp3", Sound.class);
        manager.load("guitarras/car_drift.mp3", Sound.class);
        manager.load("pianos/piano.png", Texture.class);
        manager.load("pianos/mosaico_cielo.jpg", Texture.class);
        manager.load("pianos/avion_start.mp3", Sound.class);
        manager.load("pianos/avion_drift.mp3", Sound.class);
        manager.load("tambor/drum.png", Texture.class);
        manager.load("tambor/mosaico_mar.jpg", Texture.class);
        manager.load("tambor/bote_start.mp3", Sound.class);
        manager.load("tambor/bote_drift.mp3", Sound.class);
        manager.load("maracas/maraca.png", Texture.class);
        manager.load("maracas/mosaico_tren.jpg", Texture.class);
        manager.load("maracas/tren_start.mp3", Sound.class);
        manager.load("maracas/tren_drift.mp3", Sound.class);
        manager.load("guitarras/car_ending_sheet.png", Texture.class);
        manager.load("pianos/avion_ending_sheet.png", Texture.class);
        manager.load("tambor/bote_ending_sheet.png", Texture.class);
        manager.load("maracas/tren_ending_sheet.png", Texture.class);
        manager.load("guitarras/car_drive.mp3", Sound.class);
        manager.load("pianos/avion_drive.mp3", Sound.class);
        manager.load("tambor/bote_drive.mp3", Sound.class);
        manager.load("maracas/tren_drive.mp3", Sound.class);
        manager.finishLoading();

        mainMenuScreen = new MainMenuScreen(this);
        optionsScreen = new OptionsScreen(this);
        vehicleSelectScreen = new VehicleSelectScreen(this);
        carGiftScreen = new GiftScreen(this, VehicleType.CAR);
        planeGiftScreen = new GiftScreen(this, VehicleType.PLANE);
        boatGiftScreen = new GiftScreen(this, VehicleType.BOAT);
        trainGiftScreen = new GiftScreen(this, VehicleType.TRAIN);
        carStaticScreen = new StaticVehicleScreen(this, VehicleType.CAR);
        planeStaticScreen = new StaticVehicleScreen(this, VehicleType.PLANE);
        boatStaticScreen = new StaticVehicleScreen(this, VehicleType.BOAT);
        trainStaticScreen = new StaticVehicleScreen(this, VehicleType.TRAIN);
        carEndingScreen = new EndingScreen(this, VehicleType.CAR);
        planeEndingScreen = new EndingScreen(this, VehicleType.PLANE);
        boatEndingScreen = new EndingScreen(this, VehicleType.BOAT);
        trainEndingScreen = new EndingScreen(this, VehicleType.TRAIN);

        Gdx.input.setInputProcessor(new MetricasInputListener());
        setScreen(mainMenuScreen);
    }

    @Override
    public void dispose() {
        GestorMetricas.guardarMetricas();
        mainMenuScreen.dispose();
        optionsScreen.dispose();
        vehicleSelectScreen.dispose();
        carGiftScreen.dispose();
        planeGiftScreen.dispose();
        boatGiftScreen.dispose();
        trainGiftScreen.dispose();
        carStaticScreen.dispose();
        planeStaticScreen.dispose();
        boatStaticScreen.dispose();
        trainStaticScreen.dispose();
        carEndingScreen.dispose();
        planeEndingScreen.dispose();
        boatEndingScreen.dispose();
        trainEndingScreen.dispose();
        if (manager != null) {
            manager.dispose();
        }
    }
}
