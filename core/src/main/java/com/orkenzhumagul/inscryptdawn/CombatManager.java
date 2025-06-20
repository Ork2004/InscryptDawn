package com.orkenzhumagul.inscryptdawn;

import com.badlogic.gdx.audio.Sound;
import java.util.List;

public class CombatManager {
    private enum Phase { PLAYER_ATTACK, ENEMY_MOVE, ENEMY_ATTACK, DONE }

    private Phase phase = Phase.ENEMY_MOVE;
    private boolean running = false;
    private int currentIndex = 0;
    private int currentRound = -1;
    private float timer = 0f;

    private final Field field;
    private final BalanceIndicator balanceIndicator;
    private final EnemyRounds enemyRounds;
    private final Sound cardAttackSound;
    private final Sound cardMoveSound;

    public CombatManager(Field field, BalanceIndicator balanceIndicator,
                         Sound cardAttackSound, Sound cardMoveSound) {
        this.field = field;
        this.balanceIndicator = balanceIndicator;
        this.cardAttackSound = cardAttackSound;
        this.cardMoveSound = cardMoveSound;
        this.enemyRounds = new EnemyRounds("enemy_rounds.json");
    }

    public void startCombat() {
        phase = Phase.PLAYER_ATTACK;
        currentIndex = 0;
        timer = 0f;
        running = true;
        currentRound++;
    }

    public void update(float delta) {
        if (!running) return;

        timer -= delta;
        if (timer > 0) return;

        switch (phase) {
            case PLAYER_ATTACK:
                handlePlayerAttack();
                break;
            case ENEMY_MOVE:
                handleEnemyMove();
                break;
            case ENEMY_ATTACK:
                handleEnemyAttack();
                break;
            case DONE:
                running = false;
                break;
        }

        cleanupDeadCards();
    }

    private void handlePlayerAttack() {
        if (currentIndex < Field.SLOT_COUNT) {
            FieldSlot playerSlot = field.playerSlots[currentIndex];
            FieldSlot enemySlot = field.enemySlots[currentIndex];
            FieldSlot enemyQueueSlot = field.enemyQueueSlots[currentIndex];

            if (!playerSlot.isEmpty()) {
                Card attacker = playerSlot.card;
                attacker.startAttackAnimation(true);
                cardAttackSound.play(0.6f);

                if (!enemySlot.isEmpty()) {
                    Card defender = enemySlot.card;
                    applyDamage(defender, attacker.attack);

                    if (defender.health < 0 && !enemyQueueSlot.isEmpty()) {
                        Card queuedCard = enemyQueueSlot.card;
                        applyDamage(queuedCard, Math.abs(defender.health));
                    }

                } else if (!enemyQueueSlot.isEmpty()) {
                    if (enemyQueueSlot.card.health <= 0) {
                        enemyQueueSlot.card.startDeathAnimation();
                    }
                } else {
                    balanceIndicator.addBalance(attacker.attack);
                }

                timer = 0.5f;
            }

            currentIndex++;
        } else {
            currentIndex = 0;
            phase = Phase.ENEMY_MOVE;
            timer = 0.5f;
        }
    }

    private void handleEnemyMove() {
        boolean cardMoved = false;

        for (int i = 0; i < Field.SLOT_COUNT; i++) {
            if (field.enemySlots[i].isEmpty() && !field.enemyQueueSlots[i].isEmpty()) {
                field.enemySlots[i].card = field.enemyQueueSlots[i].card;
                field.enemyQueueSlots[i].card = null;
                cardMoved = true;
            }
        }

        if (cardMoved) cardMoveSound.play(0.7f);

        List<Card> enemyQueue = enemyRounds.getQueueForRound(currentRound);
        for (int i = 0; i < enemyQueue.size(); i++) {
            Card card = enemyQueue.get(i);
            if (card != null) {
                field.placeCard(card, false, i, true);
            }
        }

        currentIndex = 0;
        phase = Phase.ENEMY_ATTACK;
        timer = 0.5f;
    }

    private void handleEnemyAttack() {
        if (currentIndex < Field.SLOT_COUNT) {
            FieldSlot enemySlot = field.enemySlots[currentIndex];
            FieldSlot playerSlot = field.playerSlots[currentIndex];

            if (!enemySlot.isEmpty()) {
                Card attacker = enemySlot.card;
                attacker.startAttackAnimation(false);
                cardAttackSound.play(0.6f);

                if (!playerSlot.isEmpty()) {
                    Card defender = playerSlot.card;
                    applyDamage(defender, attacker.attack);
                } else {
                    balanceIndicator.addBalance(-attacker.attack);
                }

                timer = 0.5f;
            }

            currentIndex++;
        } else {
            phase = Phase.DONE;
            running = false;
        }
    }

    private void applyDamage(Card card, int damage) {
        card.health -= damage;
        if (card.health <= 0) {
            card.startDeathAnimation();
        }
    }

    private void cleanupDeadCards() {
        for (int i = 0; i < Field.SLOT_COUNT; i++) {
            if (!field.playerSlots[i].isEmpty() && isDead(field.playerSlots[i].card)) {
                field.playerSlots[i].card = null;
            }
            if (!field.enemySlots[i].isEmpty() && isDead(field.enemySlots[i].card)) {
                field.enemySlots[i].card = null;
            }
        }
    }

    private boolean isDead(Card card) {
        return card.isDying && card.isDeathAnimationComplete();
    }

    public void reset() {
        phase = Phase.ENEMY_MOVE;
        currentIndex = 0;
        currentRound = -1;
        running = false;
        timer = 0f;
        enemyRounds.reset();
    }

    public boolean isRunning() {
        return running;
    }
}
