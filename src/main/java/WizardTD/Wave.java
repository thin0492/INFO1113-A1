package WizardTD;

import processing.core.PApplet;
import java.util.List;
import java.util.Random;

public class Wave {
    private float pre_wave_pause;
    private float duration;
    private List<MonsterType> monsters;
    private App app;
    private float elapsedTime;
    private int monstersSpawned;
    private Random random;

    public Wave(float pre_wave_pause, float duration, List<MonsterType> monsters, App app) {
        this.pre_wave_pause = pre_wave_pause;
        this.duration = duration;
        this.monsters = monsters;
        this.app = app;
        this.elapsedTime = 0;
        this.monstersSpawned = 0;
        this.random = new Random();
    }

    public boolean shouldSpawnMonster() {
        float totalMonsters = monsters.stream().mapToInt(m -> (int) m.getHp()).sum();
        float spawnInterval = duration / totalMonsters;
        if (elapsedTime >= spawnInterval) {
            elapsedTime -= spawnInterval;
            return true;
        }
        return false;
    }

    public MonsterType getRandomMonsterType() {
        int index = random.nextInt(monsters.size());
        return monsters.get(index);
    }

    public void update(float delta) {
        elapsedTime += delta;
    }

    public boolean isWaveComplete() {
        return monstersSpawned >= monsters.stream().mapToInt(m -> (int) m.getHp()).sum();
    }
}

