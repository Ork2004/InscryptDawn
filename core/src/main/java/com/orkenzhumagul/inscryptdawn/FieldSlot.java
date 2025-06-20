package com.orkenzhumagul.inscryptdawn;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class FieldSlot {
    public Texture slotTexture;
    private static Texture defaultTexture = new Texture("cards/card_slot.png");

    private static Texture queueTexture = new Texture("cards/card_slot_undead_queue.png");

    public Card card;

    public float x, y, width, height;
    public boolean flipped;

    public FieldSlot(float x, float y, float width, float height, boolean flipped, boolean isQueue) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.flipped = flipped;

        this.slotTexture = isQueue ? queueTexture : defaultTexture;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public static void disposeAssets() {
        if (defaultTexture != null) defaultTexture.dispose();
        if (queueTexture != null) queueTexture.dispose();
    }

    public boolean isEmpty() {
        return this.card == null;
    }

    public boolean contains(float x, float y) {
        return x >= getX() && x <= getX() + Field.SLOT_WIDTH &&
            y >= getY() && y <= getY() + Field.SLOT_HEIGHT;
    }


    public void render(SpriteBatch batch) {
        if (slotTexture != null) {
            if (flipped) {
                batch.draw(slotTexture,
                    x + width, y,
                    -width, height,
                    0, 0,
                    slotTexture.getWidth(), slotTexture.getHeight(),
                    false, true);
            } else {
                batch.draw(slotTexture, x, y, width, height);
            }
        }

        if (card != null) {
            card.render(batch, x, y);
        }
    }
}
