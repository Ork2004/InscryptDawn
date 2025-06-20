package com.orkenzhumagul.inscryptdawn;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EnemyRounds {
    private final List<List<List<Card>>> allVariants = new ArrayList<>();
    private List<List<Card>> selectedVariant = new ArrayList<>();
    private final Random random = new Random();

    public EnemyRounds(String jsonFilePath) {
        loadVariantsFromJson(jsonFilePath);
        selectRandomVariant();
    }

    private void loadVariantsFromJson(String filePath) {
        FileHandle file = Gdx.files.internal(filePath);
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(file);

        for (JsonValue variant : root.get("variants")) {
            List<List<Card>> rounds = new ArrayList<>();

            for (JsonValue roundArray : variant) {
                List<Card> round = new ArrayList<>();

                for (JsonValue cardJson : roundArray) {
                    if (cardJson.isNull()) {
                        round.add(null);
                    } else {
                        String name = cardJson.getString("name");
                        int attack = cardJson.getInt("attack");
                        int health = cardJson.getInt("health");
                        int cost = cardJson.getInt("cost");
                        round.add(new Card(name, attack, health, cost));
                    }
                }

                rounds.add(round);
            }

            allVariants.add(rounds);
        }

        Gdx.app.log("ENEMY_ROUNDS", "Loaded " + allVariants.size() + " variants.");
    }

    private void selectRandomVariant() {
        if (allVariants.isEmpty()) return;
        selectedVariant = allVariants.get(random.nextInt(allVariants.size()));
        Gdx.app.log("ENEMY_ROUNDS", "Selected variant with " + selectedVariant.size() + " rounds.");
    }

    public List<Card> getQueueForRound(int round) {
        if (round < selectedVariant.size()) {
            return selectedVariant.get(round);
        } else {
            return new ArrayList<>();
        }
    }

    public void reset() {
        selectRandomVariant();
    }
}
