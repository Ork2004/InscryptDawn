package com.orkenzhumagul.inscryptdawn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Bell {
    private Texture texture = new Texture("cards/bell_ringer.png");
    private float x, y, width = 200, height = 200;

    public Bell(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y, width, height);
    }

    public boolean isTouched(float touchX, float touchY) {
        return
            touchX >= x && touchX <= x + width &&
            touchY >= y && touchY <= y + height;
    }

    public void dispose() {
        texture.dispose();
    }
}
