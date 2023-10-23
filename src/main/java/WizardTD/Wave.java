package WizardTD;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Wave {
    private float duration;  // List of durations for each wave
    private float preWavePause;  // Time before the wave starts
    private float preWavePauseTime; // Timer for pre wave pause
    private float elapsedTime;  // Time since the wave started
    private float timeSinceLastSpawn;  // Time since the last monster was spawned
    private float spawnInterval;
    private float totalMonsterCount;

    private List<MonsterTypeQuantity> monstersToSpawn = new ArrayList<>();
    private Random random = new Random();

    public Wave(float duration, float preWavePause, PApplet p) {
        this.duration = duration;
        System.out.println("Duration: " + duration);
        this.preWavePause = preWavePause;
        this.preWavePauseTime = 0;
        this.elapsedTime = 0;
        this.timeSinceLastSpawn = 0;
        this.spawnInterval = getCurrentDuration() / totalMonsterCount;
    }


    public void update(float delta) {
        if (preWavePauseTime < preWavePause) {
            preWavePauseTime += delta;
            System.out.println("Pre Wave: " + preWavePauseTime);
            return;
        }
        elapsedTime += delta;
        timeSinceLastSpawn += delta;
        System.out.println("Elapsed Time: " + elapsedTime);
    }

    public void addMonsterType(MonsterType monsterType, int quantity) {
        this.monstersToSpawn.add(new MonsterTypeQuantity(monsterType, quantity));
    }

    public boolean shouldSpawnMonster() {
        //System.out.println(totalMonsterCount);
        if (timeSinceLastSpawn >= duration/totalMonsterCount && !monstersToSpawn.isEmpty()) {
            timeSinceLastSpawn = 0;
            return true;
        }
        return false;
    }

    public Monster spawnMonster(App app, int wizardHouseX, int wizardHouseY) {
        MonsterType monsterType = getNextMonsterTypeToSpawn();
        if (monsterType != null) {
            List<int[]> boundaryTiles = app.findBoundaryPathTiles();
            int[] spawnPosition = boundaryTiles.get(random.nextInt(boundaryTiles.size()));
            int x = spawnPosition[0];
            int y = spawnPosition[1];

            return new Monster(Math.round(x), Math.round(y), monsterType, app, wizardHouseX, wizardHouseY);
        }
        return null;
    }

    private MonsterType getNextMonsterTypeToSpawn() {
        if (monstersToSpawn.isEmpty()) {
            return null;
        }

        MonsterTypeQuantity mtq = monstersToSpawn.get(0);
        MonsterType monsterType = mtq.getMonsterType();

        mtq.decrementQuantity();
        if (mtq.getQuantity() <= 0) {
            monstersToSpawn.remove(0);
        }

        return monsterType;
    }

    // Check if the current wave is over
    public boolean isWaveOver() {
        //if (monstersToSpawn.isEmpty() && timeSinceLastSpawn > getCurrentDuration()) {
        if (elapsedTime > duration) {
            elapsedTime = 0;
            //totalMonsterCount = null;
            return true;
        }
        return false;
    }

    private float getCurrentDuration() {
        return duration;
    }

    public void totalMonstersInWave(float totalQuantity) {
        this.totalMonsterCount = totalQuantity;
    }
}

class MonsterTypeQuantity {
    private MonsterType monsterType;
    private int quantity;

    public MonsterTypeQuantity(MonsterType monsterType, int quantity) {
        this.monsterType = monsterType;
        this.quantity = quantity;
    }

    public MonsterType getMonsterType() {
        return monsterType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void decrementQuantity() {
        this.quantity -= 1;
    }
}

