package com.orkenzhumagul.inscryptdawn;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    public static final float DECK_WIDTH = 120f;
    public static final float DECK_HEIGHT = 180f;

    private ArrayList<Card> cards;
    private String name;
    private Texture texture;
    private float x = 0, y = 0;

    public Deck(String name, String texturePath) {
        this.name = name;
        this.cards = new ArrayList<>();
        this.texture = new Texture(texturePath);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public void addCards(List<Card> newCards) {
        cards.addAll(newCards);
    }

    public void clear() {
        cards.clear();
    }

    public void reset(List<Card> newCards) {
        clear();
        addCards(newCards);
        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card drawCard() {
        if (cards.isEmpty()) return null;
        return cards.remove(0);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }

    public boolean contains(float x, float y) {
        return x >= this.x && x <= this.x + DECK_WIDTH &&
            y >= this.y && y <= this.y + DECK_HEIGHT;
    }

    public void render(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, x, y, DECK_WIDTH, DECK_HEIGHT);
        }
    }

    public void dispose() {
        if (texture != null) {
            texture.dispose();
        }
    }

    public List<Card> getCards() {
        return new ArrayList<>(cards);
    }
}
