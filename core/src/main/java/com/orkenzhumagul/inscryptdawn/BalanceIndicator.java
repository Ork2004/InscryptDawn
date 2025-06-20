package com.orkenzhumagul.inscryptdawn;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BalanceIndicator {
    private Texture barTexture;
    private Texture pointerTexture;

    private float x, y;
    private int maxSwing = 5;
    private int balance = 0;

    private static final float BAR_WIDTH = 500f;
    private static final float BAR_HEIGHT = 70f;
    private static final float POINTER_WIDTH = 40f;
    private static final float POINTER_HEIGHT = 60f;

    public BalanceIndicator(float x, float y) {
        this.x = x;
        this.y = y;
        barTexture = new Texture("cards/balance_bar.png");
        pointerTexture = new Texture("cards/pointer.png");
    }

    public void render(SpriteBatch batch) {
        batch.draw(barTexture, x, y, BAR_WIDTH, BAR_HEIGHT);

        float percent = Math.max(-maxSwing, Math.min(maxSwing, balance)) / (float) maxSwing;
        float pointerX = x + BAR_WIDTH / 2f - POINTER_WIDTH / 2f + percent * (BAR_WIDTH / 2f - 10); // ограничить смещение

        batch.draw(pointerTexture, pointerX, y - 5, POINTER_WIDTH, POINTER_HEIGHT);
    }

    public void addBalance(int delta) {
        balance += delta;
        balance = Math.max(-maxSwing, Math.min(maxSwing, balance));
    }

    public int getBalance() {
        return balance;
    }

    public void reset() {
        balance = 0;
    }

    public boolean isGameOver() {
        return balance >= maxSwing || balance <= -maxSwing;
    }

    public void dispose() {
        barTexture.dispose();
        pointerTexture.dispose();
    }
}
