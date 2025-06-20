package com.orkenzhumagul.inscryptdawn;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.List;

public class DeckSelectionWindow {
    private Deck playerDeck;
    private Deck squirrelDeck;
    private boolean visible = false;
    private Deck selectedDeck = null;
    private Viewport viewport;
    private Texture panelTexture;
    private Sprite panelSprite;

    public DeckSelectionWindow(Viewport viewport) {
        this.viewport = viewport;

        panelTexture = new Texture("cards/panel.png");
        panelSprite = new Sprite(panelTexture);

        panelSprite.setSize(700, 500);
        panelSprite.setPosition(
            (viewport.getWorldWidth() / 2) - (panelSprite.getWidth() / 2),
            (viewport.getWorldHeight() / 2) - (panelSprite.getHeight() / 2 - 40)
        );


        playerDeck = new Deck("MainDeck", "cards/Base Card/card_back.png");
        playerDeck.setPosition(150, 600);

        squirrelDeck = new Deck("SquirrelDeck", "cards/Base Card/card_back_squirrel.png");
        squirrelDeck.setPosition(450, 600);

        loadDecks();
    }

    private void loadDecks() {
        playerDeck.clear();
        squirrelDeck.clear();

        List<Card> playerCards = DeckLoader.loadDeckFromJson("player_deck.json");
        for (Card card : playerCards) {
            playerDeck.addCard(card);
        }

        for (int i = 0; i < 50; i++) {
            squirrelDeck.addCard(new Card("Squirrel", 0, 1, 0));
        }

        playerDeck.shuffle();
    }

    public void show() {
        visible = true;
        selectedDeck = null;
    }

    public boolean isVisible() {
        return visible;
    }

    public Deck getSelectedDeck() {
        return selectedDeck;
    }

    public Deck getPlayerDeck() {
        return playerDeck;
    }

    public Deck getSquirrelDeck() {
        return squirrelDeck;
    }

    public void updateInput(float screenX, float screenY) {
        if (!visible) return;

        Vector3 worldCoords = new Vector3(screenX, screenY, 0);
        viewport.unproject(worldCoords);

        if (playerDeck.contains(worldCoords.x, worldCoords.y)) {
            selectedDeck = playerDeck;
            visible = false;
        } else if (squirrelDeck.contains(worldCoords.x, worldCoords.y)) {
            selectedDeck = squirrelDeck;
            visible = false;
        }
    }

    public void reset() {
        visible = false;
        selectedDeck = null;

        loadDecks();
    }


    public void render(SpriteBatch batch) {
        if (!visible) return;

        panelSprite.draw(batch);

        playerDeck.render(batch);
        squirrelDeck.render(batch);
    }

    public void dispose() {
        playerDeck.dispose();
        squirrelDeck.dispose();
        panelTexture.dispose();
    }
}
