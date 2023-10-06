package WizardTD;

import processing.core.PApplet;

import java.util.*;
//import java.util.stream.Collectors;

public class Wave {
    private float pre_wave_pause;
    private float duration;
    private Map<MonsterType, Integer> monsterTypes;
    private App app;
    private float elapsedTime;
    private int monstersSpawned;
    private Random random;

    public Wave(float pre_wave_pause, float duration, App app) {
        this.pre_wave_pause = pre_wave_pause;
        this.duration = duration;
        this.monsterTypes = new HashMap<>();
        this.app = app;
        this.elapsedTime = 0;
        this.monstersSpawned = 0;
        this.random = new Random();
    }

    public void addMonsterType(MonsterType monsterType, int quantity) {
        this.monsterTypes.put(monsterType, quantity);
    }

    public boolean shouldSpawnMonster() {
        float totalMonsters = monsterTypes.values().stream().mapToInt(Integer::intValue).sum();
        float spawnInterval = duration / totalMonsters;
        if (elapsedTime >= spawnInterval) {
            elapsedTime -= spawnInterval;
            System.out.println("MONSTERS SHULD BE SPAWNING");
            return true;
        }
        return false;
    }

    public MonsterType getRandomMonsterType() {
        List<MonsterType> keys = new ArrayList<>(monsterTypes.keySet());
        int index = random.nextInt(keys.size());
        return keys.get(index);
    }

    public void update(float delta) {
        elapsedTime += delta;
    }

    public boolean isWaveComplete() {
        return monstersSpawned >= monsterTypes.values().stream().mapToInt(Integer::intValue).sum();
    }
}

