package com.orkenzhumagul.inscryptdawn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

public class DeckLoader {

    public static List<Card> loadDeckFromJson(String path) {
        List<Card> cards = new ArrayList<>();
        FileHandle file = Gdx.files.internal(path);
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(file);

        for (JsonValue cardJson : root) {
            String name = cardJson.getString("name");
            int attack = cardJson.getInt("attack");
            int health = cardJson.getInt("health");
            int cost = cardJson.getInt("cost");

            cards.add(new Card(name, attack, health, cost));
        }

        return cards;
    }
}
