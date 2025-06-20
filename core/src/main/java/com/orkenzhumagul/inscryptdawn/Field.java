package com.orkenzhumagul.inscryptdawn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Field {
    public static final int SLOT_COUNT = 4;
    public FieldSlot[] playerSlots;
    public FieldSlot[] enemySlots;
    public FieldSlot[] enemyQueueSlots;


    public static final float SLOT_WIDTH = 125f;
    public static final float SLOT_HEIGHT = 190f;
    public static final float SLOT_MARGIN = 20f;
    public static final float ROWS_MARGIN = 50f;

    public Field(Viewport viewport) {
        playerSlots = new FieldSlot[SLOT_COUNT];
        enemySlots = new FieldSlot[SLOT_COUNT];
        enemyQueueSlots = new FieldSlot[SLOT_COUNT];

        float totalWidth = SLOT_COUNT * SLOT_WIDTH + (SLOT_COUNT - 1) * SLOT_MARGIN;

        float startX = (viewport.getWorldWidth() - totalWidth) / 2;

        float centerY = viewport.getWorldHeight() / 2f + 400;

        float playerY = centerY - ROWS_MARGIN - SLOT_HEIGHT;

        float enemyY = centerY + ROWS_MARGIN;

        float enemyQueueY = enemyY + SLOT_HEIGHT + ROWS_MARGIN;


        for (int i = 0; i < SLOT_COUNT; i++) {
            float x = startX + i * (SLOT_WIDTH + SLOT_MARGIN);
            playerSlots[i] = new FieldSlot(x, playerY, SLOT_WIDTH, SLOT_HEIGHT, false, false);
            enemySlots[i] = new FieldSlot(x, enemyY, SLOT_WIDTH, SLOT_HEIGHT, true, false);
            enemyQueueSlots[i] = new FieldSlot(x, enemyQueueY, SLOT_WIDTH, SLOT_HEIGHT, false, true);
        }
    }

    public boolean placeCard(Card card, boolean isPlayer, int slotIndex, boolean isQueue) {
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT) return false;

        FieldSlot slot;
        if (isPlayer) {
            slot = playerSlots[slotIndex];
        } else if (isQueue) {
            slot = enemyQueueSlots[slotIndex];
        } else {
            slot = enemySlots[slotIndex];
        }

        if (slot.isEmpty()) {
            slot.card = card;
            return true;
        }
        return false;
    }

    public int checkSlotTouch(float touchX, float touchY, boolean isPlayer) {
        FieldSlot[] slots = isPlayer ? playerSlots : enemySlots;

        for (int i = 0; i < slots.length; i++) {
            FieldSlot slot = slots[i];
            if (touchX >= slot.getX() && touchX <= slot.getX() + SLOT_WIDTH &&
                touchY >= slot.getY() && touchY <= slot.getY() + SLOT_HEIGHT) {
                return i;
            }
        }
        return -1;
    }

    public void update(float delta) {
        for (FieldSlot slot : playerSlots) {
            if (!slot.isEmpty()) {
                slot.card.updateAnimation(delta);
                if (slot.card.isDying && slot.card.isDeathAnimationComplete()) {
                    slot.card = null;
                }
            }
        }

        for (FieldSlot slot : enemySlots) {
            if (!slot.isEmpty()) {
                slot.card.updateAnimation(delta);
            }
        }

        for (FieldSlot slot : enemyQueueSlots) {
            if (!slot.isEmpty()) {
                slot.card.updateAnimation(delta);
            }
        }
    }

    public void reset() {
        for (FieldSlot slot : playerSlots) slot.card = null;
        for (FieldSlot slot : enemySlots) slot.card = null;
        for (FieldSlot slot : enemyQueueSlots) slot.card = null;
    }

    public void render(SpriteBatch batch) {
        for (FieldSlot slot : enemyQueueSlots) slot.render(batch);
        for (FieldSlot slot : enemySlots) slot.render(batch);
        for (FieldSlot slot : playerSlots) slot.render(batch);
    }
    public void dispose() {
        FieldSlot.disposeAssets();
    }
}
