package com.orkenzhumagul.inscryptdawn;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.*;

import java.util.*;

public class InscryptDawnGame extends ApplicationAdapter {
    private static final int WORLD_WIDTH = 720;
    private static final int WORLD_HEIGHT = 1280;
    private static final float ENEMY_TURN_DURATION = 2f;

    private enum GameState { DRAW_CARD, NORMAL, SACRIFICE, PLACE_CARD }

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    private Stage stage;

    private Texture backgroundTexture;
    private Music backgroundMusic;
    private Sound cardDrawSound, cardPlaceSound, cardAttackSound;

    private Field field;
    private Hand playerHand;
    private Bell bell;
    private CombatManager combatManager;
    private BalanceIndicator balanceIndicator;
    private DeckSelectionWindow deckSelectionWindow;
    private GameOverPanel gameOverPanel;

    private GameState gameState = GameState.NORMAL;

    private boolean isPlayerTurn = true;
    private boolean hasDrawnThisRound = false;
    private boolean isCardSelected = false;
    private boolean isEnemyActing = false;
    private boolean isFirstRound = true;

    private float cameraTargetY = WORLD_HEIGHT / 2f;
    private float enemyActionTimer = 0f;
    private float swipeStartY = -1;

    private int pendingSlotIndex = -1;
    private Card pendingCardToPlace = null;
    private final List<FieldSlot> selectedSacrifices = new ArrayList<>();

    @Override
    public void create() {
        initializeRendering();
        loadSoundEffects();
        initializeGameObjects();
        combatManager.startCombat();
        startFirstRound();
    }

    private void initializeRendering() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply();
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0);
        camera.update();

        Gdx.graphics.setWindowedMode(WORLD_WIDTH, WORLD_HEIGHT);
        backgroundTexture = new Texture("cards/background.png");

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/01. Deathcard Cabin.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.play();

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
    }

    private void loadSoundEffects() {
        cardDrawSound = Gdx.audio.newSound(Gdx.files.internal("sounds/cardquick#3.wav"));
        cardPlaceSound = Gdx.audio.newSound(Gdx.files.internal("sounds/card#5.wav"));
        cardAttackSound = Gdx.audio.newSound(Gdx.files.internal("sounds/card_attack_damage.wav"));
    }

    private void initializeGameObjects() {
        field = new Field(viewport);
        bell = new Bell(250, 20);
        balanceIndicator = new BalanceIndicator(110, 1010);
        combatManager = new CombatManager(field, balanceIndicator, cardAttackSound, cardPlaceSound);
        deckSelectionWindow = new DeckSelectionWindow(viewport);
        playerHand = new Hand(0, 450, viewport);
        gameOverPanel = new GameOverPanel(viewport);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        updateCameraPosition();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        renderGameElements();
        batch.end();

        if (checkGameOver()) return;
        if (isEnemyActing) updateEnemyTurn();

        field.update(Gdx.graphics.getDeltaTime());
        combatManager.update(Gdx.graphics.getDeltaTime());

        handleInput();
    }

    private boolean checkGameOver() {
        if (!gameOverPanel.isVisible() && balanceIndicator.isGameOver()) {
            boolean isVictory = balanceIndicator.getBalance() > 0;
            gameOverPanel.show(isVictory);
            cameraTargetY = WORLD_HEIGHT / 2f;
        }

        if (gameOverPanel.isVisible()) {
            if (Gdx.input.justTouched()) {
                Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                viewport.unproject(touchPos);
                if (gameOverPanel.isRestartButtonTouched(touchPos.x, touchPos.y)) {
                    restartGame();
                }
            }
            return true;
        }
        return false;
    }

    private void updateEnemyTurn() {
        enemyActionTimer -= Gdx.graphics.getDeltaTime();
        if (enemyActionTimer <= 0f && !combatManager.isRunning()) {
            endTurn();
        }
    }

    private void renderGameElements() {
        batch.draw(backgroundTexture, 0, 0);
        field.render(batch);
        bell.render(batch);
        playerHand.render(batch);
        balanceIndicator.render(batch);
        deckSelectionWindow.render(batch);
        gameOverPanel.draw(batch);
    }

    private void handleInput() {
        if (deckSelectionWindow.isVisible()) {
            if (Gdx.input.justTouched()) {
                deckSelectionWindow.updateInput(Gdx.input.getX(), Gdx.input.getY());
                Deck selected = deckSelectionWindow.getSelectedDeck();
                if (selected != null && !selected.isEmpty()) {
                    playerHand.addCard(selected.drawCard());
                    hasDrawnThisRound = true;
                    cardDrawSound.play(1f);
                }
            }
            return;
        }

        if (Gdx.input.justTouched()) swipeStartY = Gdx.input.getY();
        if (!Gdx.input.isTouched() && swipeStartY != -1) handleSwipe(Gdx.input.getY());

        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(touchPos);
            handleTouch(touchPos.x, touchPos.y);
        }
    }

    private void handleSwipe(float swipeEndY) {
        float deltaY = swipeStartY - swipeEndY;
        final float SWIPE_THRESHOLD = 100f;

        if (deltaY < -SWIPE_THRESHOLD) cameraTargetY = WORLD_HEIGHT * 0.75f;
        else if (deltaY > SWIPE_THRESHOLD) cameraTargetY = WORLD_HEIGHT / 2f;

        swipeStartY = -1;
    }

    private void handleTouch(float x, float y) {
        switch (gameState) {
            case NORMAL:
                handleNormalTouch(x, y);
                break;
            case SACRIFICE:
                handleSacrificeTouch(x, y);
                break;
            case PLACE_CARD:
                handlePlaceCardTouch(x, y);
                break;
        }
    }

    private void handleNormalTouch(float x, float y) {
        if (playerHand.checkTouch(x, y)) {
            Card selected = playerHand.getSelectedCard();
            if (selected != null) {
                pendingCardToPlace = selected;
                gameState = (selected.cost > 0) ? GameState.SACRIFICE : GameState.PLACE_CARD;
                selectedSacrifices.forEach(slot -> slot.card.markForSacrificeSelection(false));
                selectedSacrifices.clear();
                isCardSelected = true;
            }
        }
        if (bell.isTouched(x, y) && !combatManager.isRunning()) endTurn();
    }

    private void handleSacrificeTouch(float x, float y) {
        for (FieldSlot slot : field.playerSlots) {
            if (!slot.isEmpty() && slot.contains(x, y)) {
                if (selectedSacrifices.contains(slot)) {
                    selectedSacrifices.remove(slot);
                    slot.card.markForSacrificeSelection(false);
                } else {
                    selectedSacrifices.add(slot);
                    slot.card.markForSacrificeSelection(true);
                }
                if (selectedSacrifices.size() >= pendingCardToPlace.cost) confirmSacrifices();
                return;
            }
        }
        if (selectedSacrifices.size() < pendingCardToPlace.cost) resetState();
    }

    private void confirmSacrifices() {
        selectedSacrifices.forEach(slot -> {
            slot.card.startSacrificeAnimation();
        });
        gameState = GameState.PLACE_CARD;
    }

    private void handlePlaceCardTouch(float x, float y) {
        int slotIndex = field.checkSlotTouch(x, y, true);
        if (slotIndex != -1 && field.playerSlots[slotIndex].isEmpty() && pendingCardToPlace != null) {
            if (field.placeCard(pendingCardToPlace, true, slotIndex, false)) {
                playerHand.removeSelectedCard();
                cardPlaceSound.play(1f);
                resetState();
            }
        } else if (selectedSacrifices.isEmpty()) {
            resetState();
        }
    }

    private void resetState() {
        isCardSelected = false;
        pendingCardToPlace = null;
        pendingSlotIndex = -1;
        selectedSacrifices.forEach(slot -> {
            if (slot.card != null) {
                slot.card.markForSacrificeSelection(false);
            }
        });        selectedSacrifices.clear();
        gameState = GameState.NORMAL;
        playerHand.deselectCard();
    }

    private void endTurn() {
        isFirstRound = false;
        isPlayerTurn = !isPlayerTurn;

        if (isPlayerTurn) {
            hasDrawnThisRound = false;
            deckSelectionWindow.show();
            cameraTargetY = WORLD_HEIGHT / 2f;
            isEnemyActing = false;
        } else {
            cameraTargetY = WORLD_HEIGHT * 0.75f;
            combatManager.startCombat();
            isEnemyActing = true;
            enemyActionTimer = ENEMY_TURN_DURATION;
        }
    }

    private void startFirstRound() {
        for (int i = 0; i < 3; i++) {
            if (!deckSelectionWindow.getPlayerDeck().isEmpty()) {
                playerHand.addCard(deckSelectionWindow.getPlayerDeck().drawCard());
            }
        }
        if (!deckSelectionWindow.getSquirrelDeck().isEmpty()) {
            playerHand.addCard(deckSelectionWindow.getSquirrelDeck().drawCard());
        }
        hasDrawnThisRound = true;
    }

    private void updateCameraPosition() {
        if (Math.abs(camera.position.y - cameraTargetY) > 1f) {
            camera.position.y += (cameraTargetY - camera.position.y) * 0.1f;
            camera.update();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        backgroundMusic.dispose();
        batch.dispose();
        field.dispose();
        bell.dispose();
        balanceIndicator.dispose();
        deckSelectionWindow.dispose();
        Card.disposeStaticResources();
        cardDrawSound.dispose();
        cardPlaceSound.dispose();
        cardAttackSound.dispose();
        gameOverPanel.dispose();
    }

    private void restartGame() {
        isFirstRound = true;
        gameOverPanel.hide();
        field.reset();
        playerHand.reset();
        deckSelectionWindow.reset();
        balanceIndicator.reset();
        combatManager.reset();
        combatManager.startCombat();
        startFirstRound();
        isPlayerTurn = true;
        hasDrawnThisRound = false;
        isCardSelected = false;
        isEnemyActing = false;
        pendingCardToPlace = null;
        pendingSlotIndex = -1;
        selectedSacrifices.clear();
        gameState = GameState.NORMAL;
        cameraTargetY = WORLD_HEIGHT / 2f;
    }
}
