package WizardTD;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Wave {
    private float duration; 
    private float preWavePause; 
    private float preWavePauseTime; 
    private float elapsedTime;  
    private float timeSinceLastSpawn; 
    private float totalMonsterCount;

    private List<MonsterTypeQuantity> monstersToSpawn = new ArrayList<>();
    private Random random = new Random();

    public Wave(float duration, float preWavePause, PApplet p) {
        this.duration = duration / App.gameSpeed;
        this.preWavePause = preWavePause / App.gameSpeed;
        this.preWavePauseTime = 0;
        this.elapsedTime = 0;
        this.timeSinceLastSpawn = 0;
    }


    public void update(float delta) {
        if (preWavePauseTime < preWavePause) {
            preWavePauseTime += delta;
            return;
        }
        elapsedTime += delta;
        timeSinceLastSpawn += delta;
    }

    public void addMonsterType(MonsterType monsterType, int quantity) {
        this.monstersToSpawn.add(new MonsterTypeQuantity(monsterType, quantity));
    }

    public boolean shouldSpawnMonster() {
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


    public boolean isWaveOver() {
        if (monstersToSpawn.isEmpty() && elapsedTime > duration) {
            elapsedTime = 0;
            return true;
        }
        return false;
    }


    public void totalMonstersInWave(float totalQuantity) {
        this.totalMonsterCount = totalQuantity;
    }

    public float getDuration() {
        return this.duration;
    }

    public float getElapsedTime() {
        return this.elapsedTime;
    }

    public float getPreWavePause() {
        return this.preWavePause;
    }

    public float getPreWavePauseTime() {
        return this.preWavePauseTime;
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

