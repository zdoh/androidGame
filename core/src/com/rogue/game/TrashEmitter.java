package com.rogue.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.rogue.game.screens.GameScreen;

/**
 * Created by FlameXander on 12.01.2018.
 */

public class TrashEmitter {
    private GameScreen gameScreen;
    private Trash[] trashes;
    private TextureRegion trashTexture;

    public Trash[] getTrash() {
        return trashes;
    }

    public TrashEmitter(GameScreen gameScreen, TextureRegion trashTexture, int size) {
        this.gameScreen = gameScreen;
        this.trashTexture = trashTexture;
        this.trashes = new Trash[size];
        for (int i = 0; i < trashes.length; i++) {
            trashes[i] = new Trash(trashTexture);
            recreateTrash(i);
        }
    }

    public void render(SpriteBatch batch) {
        for (int i = 0; i < trashes.length; i++) {
            trashes[i].render(batch);
        }
    }

    public void update(float dt) {
        for (int i = 0; i < trashes.length; i++) {
            trashes[i].update(dt);
            if (trashes[i].getPosition().y < -100) {
                recreateTrash(i);
            }
        }
    }

    public void recreateTrash(int index) {
        trashes[index].prepare((int) gameScreen.getHero().getCenterX());
    }
}
