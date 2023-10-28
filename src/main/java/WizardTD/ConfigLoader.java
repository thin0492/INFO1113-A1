package WizardTD;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class ConfigLoader {
    private PApplet p;
    private App app;
    private String layoutFilePath;
    private float initialTowerRange;
    private float initialTowerFiringSpeed;
    private float initialTowerDamage;
    private float towerCost;
    private float currentMana;
    private float manaCap;
    private float manaRegenRate;
    private float manaPoolSpellInitialCost;
    private float manaPoolSpellCostInc;
    private float manaPoolSpellCapMult;
    private float manaPoolSpellManaGainedMult;
    private float totalQuantity;

    // Bomb Extension
    private static int blastRadius;
    private static float bombDamage;
    private static int bombCost;

    private char[][] layout;
    private int currentWaveJSONIndex = 0;
    private List<Wave> waves = new ArrayList<>();
    private int currentWaveIndex = 0;
    private boolean isFinalWave = false;

    private int BOARD_HEIGHT = App.BOARD_HEIGHT;
    private int BOARD_WIDTH = App.BOARD_WIDTH;
    
    public ConfigLoader(PApplet p, App app) {
        this.p = p;
        this.app = app;

    }
    public char[][] readLayoutFile(String path) {
        layout = new char[BOARD_HEIGHT][BOARD_WIDTH];
        try {
            String[] lines = p.loadStrings(path);
            for (int i = 0; i < lines.length; i++) {
                
                while (lines[i].length() < BOARD_WIDTH) {
                    lines[i] += " ";
                }
                
                layout[i] = lines[i].substring(0, BOARD_WIDTH).toCharArray();
            }
            
            for (int i = lines.length; i < BOARD_HEIGHT; i++) {
                Arrays.fill(layout[i], ' ');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return layout;
    }
    
    
    public void setupGameConfiguration(String configPath) {
        try {
            JSONObject config = p.loadJSONObject(configPath);
            
            String layoutFilePath = config.getString("layout");
            layout = readLayoutFile(layoutFilePath);

            initialTowerRange = config.getFloat("initial_tower_range");
            initialTowerFiringSpeed = config.getFloat("initial_tower_firing_speed");
            initialTowerDamage = config.getFloat("initial_tower_damage");
            towerCost = config.getFloat("tower_cost");
            currentMana = config.getFloat("initial_mana");
            manaCap = config.getFloat("initial_mana_cap");
            manaRegenRate = config.getFloat("initial_mana_gained_per_second");
            manaPoolSpellInitialCost = config.getFloat("mana_pool_spell_initial_cost");
            manaPoolSpellCostInc = config.getFloat("mana_pool_spell_cost_increase_per_use");
            manaPoolSpellCapMult = config.getFloat("mana_pool_spell_cap_multiplier");
            manaPoolSpellManaGainedMult = config.getFloat("mana_pool_spell_mana_gained_multiplier");

            if (getCurrentWaveJSONIndex() < config.getJSONArray("waves").size()) {
                JSONObject waveData = config.getJSONArray("waves").getJSONObject(getCurrentWaveJSONIndex());
                processWaveData(waveData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private void processWaveData(JSONObject waveData) {
        try {
            Wave wave = new Wave(waveData.getFloat("duration"), waveData.getFloat("pre_wave_pause"), p);

            JSONArray monstersArray = waveData.getJSONArray("monsters");
            for (int j = 0; j < monstersArray.size(); j++) {
                JSONObject monsterData = monstersArray.getJSONObject(j);
                String type = monsterData.getString("type");
                float hp = monsterData.getFloat("hp");
                float speed = monsterData.getFloat("speed");
                float armour = monsterData.getFloat("armour");
                float manaGainedOnKill = monsterData.getFloat("mana_gained_on_kill");
                PImage sprite = p.loadImage("src/main/resources/WizardTD/" + type + ".png");
                MonsterType monsterType = new MonsterType(type, hp, speed, armour, manaGainedOnKill, sprite);
                if (j == 0) {
                    totalQuantity = monsterData.getFloat("quantity");
                } else {
                    totalQuantity = getTotalQuantity() + monsterData.getFloat("quantity");
                }
                wave.addMonsterType(monsterType, monsterData.getInt("quantity"));
                wave.totalMonstersInWave(getTotalQuantity());
            }
            getWaves().add(wave);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void loadNextWave() {
        try {
            JSONObject config = p.loadJSONObject(app.configPath);
            currentWaveJSONIndex = getCurrentWaveJSONIndex() + 1;
            
            if (getCurrentWaveJSONIndex() < config.getJSONArray("waves").size()) {
                isFinalWave = false;
                JSONObject waveData = config.getJSONArray("waves").getJSONObject(getCurrentWaveJSONIndex());
                processWaveData(waveData);
            } else {
                isFinalWave = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void readBombConfig() {
        try {
            String bombConfigPath = "bombConfig.json";
            JSONObject bombConfig = p.loadJSONObject(bombConfigPath);
            
            blastRadius = bombConfig.getInt("blastRadius");
            bombDamage = bombConfig.getFloat("damage");
            bombCost = bombConfig.getInt("manaCost");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    public String getLayoutFilePath() {
        return layoutFilePath;
    }

    public char[][] getLayout() {
        return layout;
    }

    public float getInitialTowerRange() {
        return initialTowerRange;
    }

    public float getInitialTowerFiringSpeed() {
        return initialTowerFiringSpeed;
    }

    public float getInitialTowerDamage() {
        return initialTowerDamage;
    }

    public float getTowerCost() {
        return towerCost;
    }

    public float getCurrentMana() {
        return currentMana;
    }

    public float getManaCap() {
        return manaCap;
    }

    public float getManaRegenRate() {
        return manaRegenRate;
    }

    public float getManaPoolSpellInitialCost() {
        return manaPoolSpellInitialCost;
    }

    public float getManaPoolSpellCostInc() {
        return manaPoolSpellCostInc;
    }

    public float getManaPoolSpellCapMult() {
        return manaPoolSpellCapMult;
    }

    public float getManaPoolSpellManaGainedMult() {
        return manaPoolSpellManaGainedMult;
    }

    public float getTotalQuantity() {
        return totalQuantity;
    }

    public int getCurrentWaveJSONIndex() {
        return currentWaveJSONIndex;
    }

    public List<Wave> getWaves() {
        return waves;
    }

    public int getCurrentWaveIndex() {
        return currentWaveIndex;
    }

    public boolean isFinalWave() {
        return isFinalWave;
    }

    public static int getBlastRadius() {
        return blastRadius;
    }

    public static float getBombDamage() {
        return bombDamage;
    }

    public static int getBombCost() {
        return bombCost;
    }
}
