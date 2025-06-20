package com.orkenzhumagul.inscryptdawn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

public class Hand {
    private Viewport viewport;
    private ArrayList<Card> cards;
    private Card selectedCard = null;
    private int selectedIndex = -1;
    private int maxSize = 8;
    private float x, y;
    private float cardSpacing = 20f;

    public Hand(float x, float y, Viewport viewport) {
        this.cards = new ArrayList<>();
        this.x = x;
        this.y = y;
        this.viewport = viewport;
    }

    public void render(SpriteBatch batch) {
        int maxPerRow = 4;

        float totalRowWidth = maxPerRow * Card.CARD_WIDTH + (maxPerRow - 1) * cardSpacing;
        float topRowY = y;
        float bottomRowY = y - (Card.CARD_HEIGHT + cardSpacing);

        for (int i = 0; i < cards.size(); i++) {
            float rowY = (i < maxPerRow) ? topRowY : bottomRowY;
            int col = (i < maxPerRow) ? i : i - maxPerRow;

            float rowX = (viewport.getWorldWidth() - totalRowWidth) / 2;
            float cardX = rowX + col * (Card.CARD_WIDTH + cardSpacing);

            cards.get(i).render(batch, cardX, rowY);
        }
    }

    public boolean addCard(Card card) {
        if (cards.size() >= maxSize) return false;
        cards.add(card);
        return true;
    }

    public void selectCard(int index) {
        for (Card card : cards) {
            card.markForPlaySelection(false);
        }

        if (index >= 0 && index < cards.size()) {
            selectedCard = cards.get(index);
            selectedIndex = index;
            selectedCard.markForPlaySelection(true);
        }
    }

    public void deselectCard() {
        if (selectedCard != null) {
            selectedCard.markForPlaySelection(false);
        }
        selectedCard = null;
        selectedIndex = -1;
    }

    public Card getSelectedCard() {
        return selectedCard;
    }

    public Card removeSelectedCard() {
        if (selectedIndex == -1) return null;
        Card removed = cards.remove(selectedIndex);
        removed.markForPlaySelection(false);
        deselectCard();
        return removed;
    }

    public boolean checkTouch(float touchX, float touchY) {
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);

            float cardLeft = card.getX();
            float cardRight = card.getX() + Card.CARD_WIDTH;
            float cardBottom = card.getY();
            float cardTop = card.getY() + Card.CARD_HEIGHT;

            if (touchX >= cardLeft && touchX <= cardRight &&
                touchY >= cardBottom && touchY <= cardTop) {
                selectCard(i);
                return true;
            }
        }
        return false;
    }

    public void reset() {
        deselectCard();
        cards.clear();
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
