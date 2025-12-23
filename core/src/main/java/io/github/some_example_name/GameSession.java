package io.github.some_example_name;


import com.badlogic.gdx.utils.TimeUtils;

public class GameSession {

    long nextTrashSpawnTime;
    long sessionStartTime;
    public GameState gameState;
    long pauseStartTime;

    public GameSession() {
    }

    public void startGame() {
        gameState = GameState.PLAYING;
        sessionStartTime = TimeUtils.millis();
        nextTrashSpawnTime = sessionStartTime + (long) (GameSettings.STARTING_TRASH_APPEARANCE_COOL_DOWN
            * getTrashPeriodCoolDown());
    }

    public boolean shouldSpawnTrash() {
        if (nextTrashSpawnTime <= TimeUtils.millis()) {
            nextTrashSpawnTime = TimeUtils.millis() + (long) (GameSettings.STARTING_TRASH_APPEARANCE_COOL_DOWN
                * getTrashPeriodCoolDown());
            return true;
        }
        return false;
    }

    private float getTrashPeriodCoolDown() {
        return (float) Math.exp(-0.001 * (TimeUtils.millis() - sessionStartTime + 1) / 1000);
    }

    public void pauseGame()
    {
        gameState = GameState.PAUSED;
        pauseStartTime = TimeUtils.millis();
    }

    public void resumeGame()
    {
        gameState = GameState.PLAYING;
        sessionStartTime += TimeUtils.millis() - pauseStartTime;
    }
}
