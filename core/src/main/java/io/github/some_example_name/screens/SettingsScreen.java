package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.some_example_name.GameResources;
import io.github.some_example_name.Main;
import io.github.some_example_name.components.ButtonView;
import io.github.some_example_name.components.ImageView;
import io.github.some_example_name.components.MovingBackgroundView;
import io.github.some_example_name.components.TextView;

public class SettingsScreen extends ScreenAdapter {
    Main main;

    MovingBackgroundView backgroundView;
    TextView titleTextView;
    ImageView blackoutImageView;
    ButtonView returnButton;
    TextView musicSettingView;
    TextView soundSettingView;
    TextView clearSettingView;

    public SettingsScreen(Main main) {
        this.main = main;

        backgroundView = new MovingBackgroundView(GameResources.BACKGROUND_IMG_PATH);
        titleTextView = new TextView(main.largeWhiteFont, 256, 956, "Settings");
        blackoutImageView = new ImageView(85, 365, GameResources.BLACKOUT_MIDDLE_IMG_PATH);
        musicSettingView = new TextView(main.commonWhiteFont, 173, 717, "music: " + "ON");
        soundSettingView = new TextView(main.commonWhiteFont, 173, 658, "sound: " + "ON");
        clearSettingView = new TextView(main.commonWhiteFont, 173, 599, "clear records");
        returnButton = new ButtonView(
            280, 447,
            160, 70,
            main.commonBlackFont,
            GameResources.BUTTON_SHORT_BG_IMG_PATH,
            "return"
        );
    }

    private String translateStateToText(boolean state) {
        return state ? "ON" : "OFF";
    }

    @Override
    public void render(float delta) {
        handleInput();
        main.camera.update();
        main.batch.setProjectionMatrix(main.camera.combined);
        ScreenUtils.clear(Color.CLEAR);

        main.batch.begin();

        backgroundView.draw(main.batch);
        titleTextView.draw(main.batch);
        blackoutImageView.draw(main.batch);
        returnButton.draw(main.batch);
        musicSettingView.draw(main.batch);
        soundSettingView.draw(main.batch);
        clearSettingView.draw(main.batch);

        main.batch.end();
    }
    @Override
    public void dispose() {
        main.dispose();
        backgroundView.dispose();
        titleTextView.dispose();
        blackoutImageView.dispose();
        returnButton.dispose();
        musicSettingView.dispose();
        soundSettingView.dispose();
        clearSettingView.dispose();
    }

    void handleInput() {
        if (Gdx.input.justTouched()) {
            main.touch = main.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

            if (returnButton.isHit(main.touch.x, main.touch.y)) {
                main.setScreen(main.menuScreen);
            }
            if (clearSettingView.isHit(main.touch.x, main.touch.y)) {
                clearSettingView.setText("clear records (cleared)");
            }
            if (musicSettingView.isHit(main.touch.x, main.touch.y)) {
                main.audioManager.isMusicOn = !main.audioManager.isMusicOn;
                musicSettingView.setText("music: " + translateStateToText(main.audioManager.isMusicOn));
                main.audioManager.updateMusicFlag();
            }
            if (soundSettingView.isHit(main.touch.x, main.touch.y)) {
                main.audioManager.isSoundOn = !main.audioManager.isSoundOn;
                soundSettingView.setText("sound: " + translateStateToText(main.audioManager.isSoundOn));
            }
        }
    }
}
