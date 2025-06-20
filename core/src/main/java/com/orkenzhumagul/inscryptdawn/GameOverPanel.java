package com.orkenzhumagul.inscryptdawn;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameOverPanel {
    private boolean isVisible = false;
    private boolean isVictory;
    private BitmapFont font;
    private GlyphLayout layout;
    private Sprite panelSprite;
    private Texture panelTexture;
    private Viewport viewport;

    private Texture restartButtonTexture;
    private Sprite restartButtonSprite;

    private static final float PANEL_WIDTH = 700f;
    private static final float PANEL_HEIGHT = 350f;
    private static final float BUTTON_WIDTH = 300f;
    private static final float BUTTON_HEIGHT = 200f;

    public GameOverPanel(Viewport viewport) {
        this.viewport = viewport;

        font = new BitmapFont();
        font.getData().setScale(3);
        layout = new GlyphLayout();

        panelTexture = new Texture("cards/panel.png");
        panelSprite = new Sprite(panelTexture);
        panelSprite.setSize(PANEL_WIDTH, PANEL_HEIGHT);

        restartButtonTexture = new Texture("cards/restart_button.png");
        restartButtonSprite = new Sprite(restartButtonTexture);
        restartButtonSprite.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);

        centerPanel();
    }

    private void centerPanel() {
        float x = (viewport.getWorldWidth() - panelSprite.getWidth()) / 2f;
        float y = (viewport.getWorldHeight() - panelSprite.getHeight()) / 2f;
        panelSprite.setPosition(x, y);

        float buttonX = panelSprite.getX() + (PANEL_WIDTH - BUTTON_WIDTH) / 2f;
        float buttonY = panelSprite.getY() + (PANEL_HEIGHT - BUTTON_HEIGHT) / 4f;
        restartButtonSprite.setPosition(buttonX, buttonY);
    }

    public void show(boolean isVictory) {
        this.isVictory = isVictory;
        this.isVisible = true;
    }

    public void hide() {
        this.isVisible = false;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void draw(SpriteBatch batch) {
        if (!isVisible) return;

        String message = isVictory ? "YOU WIN" : "YOU LOSE";
        font.setColor(isVictory ? Color.GREEN : Color.RED);
        layout.setText(font, message);

        panelSprite.draw(batch);

        float textX = panelSprite.getX() + (PANEL_WIDTH - layout.width) / 2f;
        float textY = panelSprite.getY() + (PANEL_HEIGHT * 0.75f) + layout.height / 2f;

        font.draw(batch, layout, textX, textY);

        restartButtonSprite.draw(batch);
    }

    public boolean isRestartButtonTouched(float x, float y) {
        return isVisible && restartButtonSprite.getBoundingRectangle().contains(x, y);
    }

    public void dispose() {
        font.dispose();
        panelTexture.dispose();
        restartButtonTexture.dispose();
    }
}
