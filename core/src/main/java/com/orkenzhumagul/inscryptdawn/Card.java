package com.orkenzhumagul.inscryptdawn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Card {
    public static final float CARD_WIDTH = 120f;
    public static final float CARD_HEIGHT = 180f;

    private static final BitmapFont font = new BitmapFont();
    private static final Texture[] costTextures = new Texture[5];
    private static final Texture defaultTexture = new Texture("cards/Base Card/card_empty.png");
    private static final Texture sacrificeEffect = new Texture("cards/sacrifice_mark.png");

    private static Sound attackSound = Gdx.audio.newSound(Gdx.files.internal("sounds/card_attack_damage.wav"));
    private static Sound deathSound = Gdx.audio.newSound(Gdx.files.internal("sounds/card_death.wav"));
    private static Sound markSound = Gdx.audio.newSound(Gdx.files.internal("sounds/sacrifice_mark.wav"));

    public final String name;
    public int attack;
    public int health;
    public final int cost;

    private float x, y;
    private Texture portrait;

    private float offsetX = 0f, offsetY = 0f;
    private float targetOffsetX = 0f, targetOffsetY = 0f;
    private static final float ANIMATION_SPEED = 8f;

    public boolean isDying = false;
    private float deathTimer = 0f;
    private static final float DEATH_DURATION = 0.5f;

    private boolean isBeingSacrificed = false;
    private float sacrificeTimer = 0f;
    private static final float MAX_SACRIFICE_TIME = 1f;

    public boolean isAttacking = false;
    public boolean isSelectedForSacrifice = false;
    public boolean isSelectedForPlay = false;

    static {
        for (int i = 1; i <= 4; i++) {
            costTextures[i] = new Texture("cards/Costs/cost_" + i + "blood.png");
        }
    }

    public Card(String name, int attack, int health, int cost) {
        this.name = name;
        this.attack = attack;
        this.health = health;
        this.cost = clampCost(cost);
        loadPortrait(name);
    }

    private int clampCost(int c) {
        return Math.max(0, Math.min(c, 4));
    }

    private void loadPortrait(String name) {
        try {
            String path = "cards/Portraits/portrait_" + name.toLowerCase().replaceAll("\\s+", "_") + ".png";
            portrait = new Texture(path);
        } catch (Exception e) {
            portrait = null;
        }
    }

    public void updateAnimation(float delta) {
        offsetX += (targetOffsetX - offsetX) * ANIMATION_SPEED * delta;
        offsetY += (targetOffsetY - offsetY) * ANIMATION_SPEED * delta;

        if (isAttacking && Math.abs(offsetY - targetOffsetY) < 1f) {
            if (targetOffsetY != 0f) {
                targetOffsetY = 0f;
            } else {
                isAttacking = false;
                offsetX = offsetY = 0f;
            }
        }

        if (isDying) {
            deathTimer += delta;
        }

        if (isBeingSacrificed) {
            sacrificeTimer -= delta;
            if (sacrificeTimer <= 0f) {
                isBeingSacrificed = false;
                startDeathAnimation();
            }
        }
    }

    public void startAttackAnimation(boolean isPlayer) {
        isAttacking = true;
        targetOffsetY = isPlayer ? 30f : -30f;
        playSound(attackSound);
    }

    public void startSacrificeAnimation() {
        isBeingSacrificed = true;
        sacrificeTimer = MAX_SACRIFICE_TIME;
        isSelectedForSacrifice = false;
    }

    public void markForSacrificeSelection(boolean selected) {
        isSelectedForSacrifice = selected;
        if (selected) playSound(markSound);
    }

    public void markForPlaySelection(boolean selected) {
        isSelectedForPlay = selected;
        if (selected) playSound(markSound);
    }

    public void startDeathAnimation() {
        isDying = true;
        deathTimer = 0f;
        playSound(deathSound);
    }

    public boolean isDeathAnimationComplete() {
        return isDying && deathTimer >= DEATH_DURATION;
    }

    private void playSound(Sound sound) {
        if (sound != null) sound.play();
    }

    public void render(SpriteBatch batch, float x, float y) {
        this.x = x;
        this.y = y;

        float drawX = x + offsetX;
        float drawY = y + offsetY;

        float alpha = getRenderAlpha();
        if (alpha <= 0f) return;

        if (isSelectedForPlay) {
            batch.setColor(0.7f, 0.7f, 1f, 1f);
            batch.draw(defaultTexture, drawX - 5, drawY - 5, CARD_WIDTH + 10, CARD_HEIGHT + 10);
        }

        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(defaultTexture, drawX, drawY, CARD_WIDTH, CARD_HEIGHT);

        renderPortrait(batch, drawX, drawY);
        renderText(batch, drawX, drawY);
        renderCost(batch, drawX, drawY);
        renderSacrificeEffect(batch, drawX, drawY);

        batch.setColor(Color.WHITE);
    }

    private float getRenderAlpha() {
        return isDying ? Math.max(0f, 1f - (deathTimer / DEATH_DURATION)) : 1f;
    }

    private void renderPortrait(SpriteBatch batch, float drawX, float drawY) {
        if (portrait == null) return;

        float targetW = CARD_WIDTH - 20;
        float targetH = CARD_HEIGHT - 100;
        float scale = Math.min(targetW / portrait.getWidth(), targetH / portrait.getHeight());
        float w = portrait.getWidth() * scale;
        float h = portrait.getHeight() * scale;

        float px = drawX + (CARD_WIDTH - w) / 2f;
        float py = drawY + 65 + (targetH - h) / 2f;

        batch.draw(portrait, px, py, w, h);
    }

    private void renderText(SpriteBatch batch, float drawX, float drawY) {
        font.setColor(Color.BLACK);

        font.getData().setScale(1.4f);
        font.draw(batch, name, drawX + 10, drawY + CARD_HEIGHT - 10);

        font.getData().setScale(2.0f);
        font.draw(batch, String.valueOf(attack), drawX + 10, drawY + 50);
        font.draw(batch, String.valueOf(health), drawX + CARD_WIDTH - 25, drawY + 35);
    }

    private void renderCost(SpriteBatch batch, float drawX, float drawY) {
        if (cost > 0 && cost <= 4 && costTextures[cost] != null) {
            batch.draw(costTextures[cost], drawX + 50, drawY + CARD_HEIGHT - 74, 64, 64);
        }
    }

    private void renderSacrificeEffect(SpriteBatch batch, float drawX, float drawY) {
        if (!isSelectedForSacrifice && !isBeingSacrificed) return;

        float alpha = isBeingSacrificed ? (sacrificeTimer / MAX_SACRIFICE_TIME) : 1f;
        Color tint = new Color(0.5f, 0f, 0f, alpha);

        batch.setColor(tint);
        batch.draw(sacrificeEffect, drawX, drawY, CARD_WIDTH, CARD_HEIGHT);
    }

    public void dispose() {
        if (portrait != null) portrait.dispose();
    }

    public static void disposeStaticResources() {
        defaultTexture.dispose();
        font.dispose();
        for (Texture t : costTextures) if (t != null) t.dispose();
        sacrificeEffect.dispose();
        attackSound.dispose();
        deathSound.dispose();
        markSound.dispose();
    }

    public float getX() { return x; }
    public float getY() { return y; }
}
