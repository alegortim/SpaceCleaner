package io.github.some_example_name.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;

import io.github.some_example_name.managers.ContactManager;
import io.github.some_example_name.GameResources;
import io.github.some_example_name.GameSession;
import io.github.some_example_name.GameSettings;
import io.github.some_example_name.GameState;
import io.github.some_example_name.Main;
import io.github.some_example_name.components.ButtonView;
import io.github.some_example_name.components.ImageView;
import io.github.some_example_name.components.LiveView;
import io.github.some_example_name.components.MovingBackgroundView;
import io.github.some_example_name.components.TextView;
import io.github.some_example_name.objects.BulletObject;
import io.github.some_example_name.objects.ShipObject;
import io.github.some_example_name.objects.TrashObject;

public class GameScreen extends ScreenAdapter {

    Main main;
    GameSession gameSession;
    ShipObject shipObject;

    ArrayList<TrashObject> trashArray;
    ArrayList<BulletObject> bulletArray;

    ContactManager contactManager;

    MovingBackgroundView backgroundView;
    ImageView topBlackoutView;
    LiveView liveView;
    TextView scoreTextView;
    ButtonView pauseButton;
    ImageView fullBlackoutView;
    ButtonView homeButton;
    ButtonView continueButton;
    TextView pauseTextView;

    public GameScreen(Main main) {
        this.main = main;
        gameSession = new GameSession();

        contactManager = new ContactManager(main.world);

        trashArray = new ArrayList<>();
        bulletArray = new ArrayList<>();

        shipObject = new ShipObject(
            GameSettings.SCREEN_WIDTH / 2, 150,
            GameSettings.SHIP_WIDTH, GameSettings.SHIP_HEIGHT,
            GameResources.SHIP_IMG_PATH,
            main.world
        );

        backgroundView = new MovingBackgroundView(GameResources.BACKGROUND_IMG_PATH);
        topBlackoutView = new ImageView(0, 1180, GameResources.BLACKOUT_TOP_IMG_PATH);
        liveView = new LiveView(305, 1215);
        scoreTextView = new TextView(main.commonWhiteFont, 50, 1215);
        pauseButton = new ButtonView(605, 1200, 46, 54, GameResources.PAUSE_IMG_PATH);

        fullBlackoutView = new ImageView(0,0, GameResources.BLACKOUT_FULL_IMG_PATH);
        homeButton = new ButtonView(138, 695, 200, 70, main.commonBlackFont, GameResources.BUTTON_SHORT_BG_IMG_PATH, "Home");
        continueButton = new ButtonView(393, 695, 200, 70, main.commonBlackFont, GameResources.BUTTON_SHORT_BG_IMG_PATH, "Continue");
        pauseTextView = new TextView(main.largeWhiteFont, 282, 842, "Pause");

    }

    @Override
    public void show() {
        restartGame();
    }

    @Override
    public void render(float delta) {

        handleInput();
        main.stepWorld();

        if (gameSession.gameState == GameState.PLAYING)
        {
            if (gameSession.shouldSpawnTrash()) {
                TrashObject trashObject = new TrashObject(
                    GameSettings.TRASH_WIDTH, GameSettings.TRASH_HEIGHT,
                    GameResources.TRASH_IMG_PATH,
                    main.world
                );
                trashArray.add(trashObject);
            }

            if (shipObject.needToShoot()) {
                BulletObject laserBullet = new BulletObject(
                    shipObject.getX(), shipObject.getY() + shipObject.height / 2,
                    GameSettings.BULLET_WIDTH, GameSettings.BULLET_HEIGHT,
                    GameResources.BULLET_IMG_PATH,
                    main.world
                );
                bulletArray.add(laserBullet);
                main.audioManager.shootSound.play();
            }

            if (!shipObject.isAlive()) {
                System.out.println("Game over!");
            }
            
            updateTrash();
            updateBullets();
            backgroundView.move();
            scoreTextView.setText("Score: " + 100);
            liveView.setLeftLives(shipObject.getLiveLeft());
        }
        draw();
    }

    private void handleInput() {
        if (Gdx.input.isTouched())
        {
            main.touch = main.camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));

            switch (gameSession.gameState) {
                case PLAYING:
                    if (pauseButton.isHit(main.touch.x, main.touch.y)) {
                        gameSession.pauseGame();
                    }
                    shipObject.move(main.touch);
                    break;
                case PAUSED:
                    if (continueButton.isHit(main.touch.x, main.touch.y)) {
                        gameSession.resumeGame();
                    }
                    if (homeButton.isHit(main.touch.x, main.touch.y)) {
                        main.setScreen(main.menuScreen);
                    }
                    break;
            }
        }
    }

    private void draw() {

        main.camera.update();
        main.batch.setProjectionMatrix(main.camera.combined);
        ScreenUtils.clear(Color.CLEAR);

        main.batch.begin();
        backgroundView.draw(main.batch);
        for (TrashObject trash : trashArray) trash.draw(main.batch);
        shipObject.draw(main.batch);
        for (BulletObject bullet : bulletArray) bullet.draw(main.batch);
        topBlackoutView.draw(main.batch);
        scoreTextView.draw(main.batch);
        liveView.draw(main.batch);
        pauseButton.draw(main.batch);

        if (gameSession.gameState == GameState.PAUSED)
        {
            fullBlackoutView.draw(main.batch);
            homeButton.draw(main.batch);
            continueButton.draw(main.batch);
        }

        main.batch.end();
    }

    private void updateTrash() {
        for (int i = 0; i < trashArray.size(); i++) {

            boolean hasToBeDestroyed = !trashArray.get(i).isAlive() || !trashArray.get(i).isInFrame();

            if (!trashArray.get(i).isAlive()) {
                main.audioManager.explosionSound.play(0.2f);
            }

            if (hasToBeDestroyed) {
                main.world.destroyBody(trashArray.get(i).body);
                trashArray.remove(i--);
            }
        }
    }

    private void updateBullets() {
        for (int i = 0; i < bulletArray.size(); i++) {
            if (bulletArray.get(i).hasToBeDestroyed()) {
                main.world.destroyBody(bulletArray.get(i).body);
                bulletArray.remove(i--);
            }
        }
    }

    private void restartGame()
    {
        for (int i = 0; i < trashArray.size(); i++)
        {
            main.world.destroyBody(trashArray.get(i).body);
            trashArray.remove(i--);
        }
        if (shipObject != null)
        {
            main.world.destroyBody(shipObject.body);
        }
        shipObject = new ShipObject(
            GameSettings.SCREEN_WIDTH / 2, 150,
            GameSettings.SHIP_WIDTH, GameSettings.SHIP_HEIGHT,
            GameResources.SHIP_IMG_PATH,
            main.world);

        bulletArray.clear();
        gameSession.startGame();
    }
}
// Создание игровых объектов
// 1. Космический корабль, мусор, пуля: (x, y), текстура, скорость, размеры
// Обработка коллизий
// 1. пуля-мусор, кораль-муссор
