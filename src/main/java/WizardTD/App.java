package WizardTD;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.MouseEvent;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.*;

//import WizardTD.Buttons;
//import WizardTD.Tower;

public class App extends PApplet {

    public char[][] layout;
    // Classes
    private Board board;

    //  Images of game elements
    private PImage tower0Img, tower1Img, tower2Img, fireballImg;
    public static PImage death1Img, death2Img, death3Img, death4Img;
    public static PImage[] deathImages = new PImage[5];
    


    //  Monsters & Waves handling
    private int currentWaveJSONIndex = 0;
    private List<Wave> waves = new ArrayList<>();
    private List<Monster> activeMonsters = new ArrayList<>();
    private int currentWaveIndex = 0;
    private int wizardHouseX, wizardHouseY;
    private boolean monsterDying = false;
    private boolean isFinalWave = false;


    //  Board dimensions
    public static final int CELLSIZE = 32;
    public static final int SIDEBAR = 120;
    public static final int TOPBAR = 40;
    public static final int BOARD_WIDTH = 20;
    public static final int BOARD_HEIGHT = 20;
    public static final int FPS = 60;
    public static final int WIDTH = CELLSIZE * BOARD_WIDTH + SIDEBAR;
    public static final int HEIGHT = CELLSIZE * BOARD_HEIGHT + TOPBAR;

    // Tower Upgrades
    private float initialTowerRange, initialTowerFiringSpeed, initialTowerDamage;
    public static boolean upgradingRange = false;
    public static boolean upgradingSpeed = false;
    public static boolean upgradingDamage = false;
    public static boolean upgradeDesiredRange, upgradeDesiredSpeed, upgradeDesiredDamage;


    public String configPath;
    public Random random = new Random();
    public char[][] pathDirections = new char[BOARD_HEIGHT][BOARD_WIDTH];
    private Buttons fastForwardButton, pauseButton, buildTowerButton, upgradeRangeButton, upgradeSpeedButton, upgradeDamageButton, manaPoolSpellButton;
    public static boolean gamePaused = false;
    public static float gameSpeed = 1.0f;
    private ArrayList<Tower> towers = new ArrayList<>();
    private boolean placingTower = false;
    
    private boolean isBuildingTower = false;
    private float towerCost;
    public float currentMana;
    private float manaCap;
    private float manaRegenRate;
    private float manaPoolSpellInitialCost;
    private float manaPoolSpellCostInc;
    private float manaPoolSpellCapMult;
    private float manaPoolSpellManaGainedMult;
    private float spawnInterval;
    private float totalQuantity;


    public App() {
        this.configPath = "config.json";
    }


    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }


    @Override
    public void setup() {
        //surface.setLocation(0, 0);
        background(255);
        frameRate(FPS);
        
        // Load and setup game configuration
        setupGameConfiguration();

        int buttonSize = 50;
        int buttonSpacing = 10;
        int startX = CELLSIZE * BOARD_WIDTH + (SIDEBAR - buttonSize) / 2 - 20;
        int startY = TOPBAR + buttonSpacing;

        fastForwardButton = new Buttons(startX, startY, buttonSize, "Fast Forward", "FF");
        pauseButton = new Buttons(startX, startY + (buttonSize + buttonSpacing), buttonSize, "Pause", "P");
        buildTowerButton = new Buttons(startX, startY + 2 * (buttonSize + buttonSpacing), buttonSize, "Build Tower", "T");
        upgradeRangeButton = new Buttons(startX, startY + 3 * (buttonSize + buttonSpacing), buttonSize, "Upgrade Range", "U1");
        upgradeSpeedButton = new Buttons(startX, startY + 4 * (buttonSize + buttonSpacing), buttonSize, "Upgrade Speed", "U2");
        upgradeDamageButton = new Buttons(startX, startY + 5 * (buttonSize + buttonSpacing), buttonSize, "Upgrade Damage", "U3");
        manaPoolSpellButton = new Buttons(startX, startY + 6 * (buttonSize + buttonSpacing), buttonSize, "Mana Spell", "M");

        board = new Board(this, layout);

        // Load images
        board.setup();
        tower0Img = loadImage("src/main/resources/WizardTD/tower0.png");
        tower1Img = loadImage("src/main/resources/WizardTD/tower1.png");
        tower2Img = loadImage("src/main/resources/WizardTD/tower2.png");
        fireballImg = loadImage("src/main/resources/WizardTD/fireball.png");
        death1Img = loadImage("src/main/resources/WizardTD/gremlin1.png");
        death2Img = loadImage("src/main/resources/WizardTD/gremlin2.png");
        death3Img = loadImage("src/main/resources/WizardTD/gremlin3.png");
        death4Img = loadImage("src/main/resources/WizardTD/gremlin4.png");
        for (int i = 0; i < 5; i++) {
            deathImages[i] = loadImage("src/main/resources/WizardTD/gremlin" + (i + 1) + ".png");
        }
        

        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (layout[y][x] == 'W') {
                    wizardHouseX = x;
                    wizardHouseY = y;
                    break;
                }
            }
        }
        bfs(wizardHouseX, wizardHouseY);
    }


    public char[][] readLayoutFile(String path) {
        char[][] layout = new char[BOARD_HEIGHT][BOARD_WIDTH];
        try {
            String[] lines = loadStrings(path);
            for (int i = 0; i < lines.length; i++) {
                //  If a line is shorter than 20 characters, pad it with spaces
                while (lines[i].length() < BOARD_WIDTH) {
                    lines[i] += " ";
                }
                
                //  Take only the first 20 characters from the line
                layout[i] = lines[i].substring(0, BOARD_WIDTH).toCharArray();
            }
            
            //  If there are less than 20 lines, fill the remaining rows with spaces
            for (int i = lines.length; i < BOARD_HEIGHT; i++) {
                Arrays.fill(layout[i], ' ');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return layout;
    }
    
    
    private void setupGameConfiguration() {
        try {
            JSONObject config = loadJSONObject(configPath);
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

            if (currentWaveJSONIndex < config.getJSONArray("waves").size()) {
                JSONObject waveData = config.getJSONArray("waves").getJSONObject(currentWaveJSONIndex);
                processWaveData(waveData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    private void processWaveData(JSONObject waveData) {
        try {
            Wave wave = new Wave(waveData.getFloat("duration"), waveData.getFloat("pre_wave_pause"), this);

            JSONArray monstersArray = waveData.getJSONArray("monsters");
            for (int j = 0; j < monstersArray.size(); j++) {
                JSONObject monsterData = monstersArray.getJSONObject(j);
                String type = monsterData.getString("type");
                float hp = monsterData.getFloat("hp");
                float speed = monsterData.getFloat("speed");
                float armour = monsterData.getFloat("armour");
                float manaGainedOnKill = monsterData.getFloat("mana_gained_on_kill");
                PImage sprite = loadImage("src/main/resources/WizardTD/" + type + ".png");
                MonsterType monsterType = new MonsterType(type, hp, speed, armour, manaGainedOnKill, sprite);
                if (j == 0) {
                    totalQuantity = monsterData.getFloat("quantity");
                } else {
                    totalQuantity += monsterData.getFloat("quantity");
                }
                wave.addMonsterType(monsterType, monsterData.getInt("quantity"));
                wave.totalMonstersInWave(totalQuantity);
            }
            waves.add(wave);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void loadNextWave() {
        try {
            JSONObject config = loadJSONObject(configPath);
            currentWaveJSONIndex++;
            
            if (currentWaveJSONIndex < config.getJSONArray("waves").size()) {
                isFinalWave = false;
                JSONObject waveData = config.getJSONArray("waves").getJSONObject(currentWaveJSONIndex);
                processWaveData(waveData);
            } else {
                isFinalWave = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void bfs(int wizardHouseX, int wizardHouseY) {
        if (!gamePaused) {
            char[][] directions = new char[BOARD_HEIGHT][BOARD_WIDTH];
            for (int i = 0; i < BOARD_HEIGHT; i++) {
                Arrays.fill(directions[i], 'X');
            }
            directions[wizardHouseY][wizardHouseX] = 'W';
        
            Queue<int[]> queue = new LinkedList<>();
            queue.add(new int[]{wizardHouseX, wizardHouseY});
        
            int[][] moves = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
            char[] moveDirections = {'U', 'D', 'L', 'R'};
        
            while (!queue.isEmpty()) {
                int[] current = queue.poll();
                int x = current[0];
                int y = current[1];
        
                for (int i = 0; i < moves.length; i++) {
                    int newX = x + moves[i][0];
                    int newY = y + moves[i][1];
        
                    if (newX >= 0 && newX < BOARD_WIDTH && newY >= 0 && newY < BOARD_HEIGHT && layout[newY][newX] == 'X' && directions[newY][newX] == 'X') {
                        directions[newY][newX] = moveDirections[i];
                        queue.add(new int[]{newX, newY});
                    }
                }
            } this.pathDirections = directions;
        }
    }
    

    private void updateAndDrawMonsters() {
        Iterator<Monster> iterator = activeMonsters.iterator();
        while (iterator.hasNext()) {
            Monster monster = iterator.next();
            if (!gamePaused) {
                monster.move();
                monster.draw();
    
                // Check if the monster has reached the wizard's house
                int[] pos = monster.getPosition();
                if (pos[0] >= 0 && pos[0] < BOARD_WIDTH && pos[1] >= 0 && pos[1] < BOARD_HEIGHT) {
                    if (layout[pos[1]][pos[0]] == 'W') {
                        if (currentMana < monster.getCurrentHp()) {
                            // End the game
                            println("Game Over!");
                            noLoop(); // Stop the draw loop
                        } else {
                            // Deduct mana and remove monster
                            currentMana -= monster.getCurrentHp();
                            monster.takeDamage(monster.getCurrentHp());
                            PImage deathImg = monster.getDeathAnimationImage();
                            if (deathImg != null) {
                                float adjustedSize = 1f * 32;
                                float xOffset = (App.CELLSIZE - adjustedSize) / 2;
                                float yOffset = (App.CELLSIZE - adjustedSize) / 2;
                                image(deathImg, monster.getExactX() * App.CELLSIZE + xOffset, monster.getExactY() * App.CELLSIZE + App.TOPBAR + yOffset, adjustedSize, adjustedSize);
                            }
                            iterator.remove(); // Remove the monster
                        }
                    }
                }
            } else {
                monster.draw();
            }
        }
    }
        


    @Override
    public void keyPressed() {
        // Fast forward key
        if (key == 'f' || key == 'F') {
            activateFastForwardButton();
        }
        // Pause key
        if (key == 'p' || key == 'P') {
            gamePaused = !gamePaused;
            pauseButton.toggleActive();
        }
        // Build tower key
        if (key == 't' || key == 'T') {
            activateBuildTowerModeButton();
        }
        // Restart game key
        if (key == 'r' || key == 'R') {
            PApplet.main("WizardTD.App");   
        }
        // Upgrade keys
        if (key == '1') {
            upgradeRangeButton.toggleActive();
            upgradeDesiredRange = !upgradeDesiredRange;
        } else if (key == '2') {
            upgradeSpeedButton.toggleActive();
            upgradeDesiredSpeed = !upgradeDesiredSpeed;
        } else if (key == '3') {
            upgradeDamageButton.toggleActive();
            upgradeDesiredDamage = !upgradeDesiredDamage;
        } activateTowerUpgradesButton();
        // Mana pool spell key
        if (key == 'm' || key == 'M') {
            activateManaPoolSpellButton();
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {
        // Fast forward button
        if (fastForwardButton.isClicked(mouseX, mouseY)) {
            activateFastForwardButton();
        // Pause button
        } if (pauseButton.isClicked(mouseX, mouseY)) {
            gamePaused = !gamePaused;
            pauseButton.toggleActive();
        // Build tower button
        } else if (buildTowerButton.isClicked(mouseX, mouseY)) {
            activateBuildTowerModeButton();
        
        // Mana pool spell button
        } if (manaPoolSpellButton.isClicked(mouseX, mouseY)) {
            manaPoolSpellButton.toggleActive();
            activateManaPoolSpellButton();
            manaPoolSpellButton.toggleActive();
        } if (upgradeRangeButton.isClicked(mouseX, mouseY)) {
            upgradeRangeButton.toggleActive();
            upgradeDesiredRange = !upgradeDesiredRange;
        } else if (upgradeSpeedButton.isClicked(mouseX, mouseY)) {
            upgradeSpeedButton.toggleActive();
            upgradeDesiredSpeed = !upgradeDesiredSpeed;
        } else if (upgradeDamageButton.isClicked(mouseX, mouseY)) {
            upgradeDamageButton.toggleActive();
            upgradeDesiredDamage = !upgradeDesiredDamage;
        } activateTowerUpgradesButton();
    }


    
    
    @Override
    public void mouseReleased(MouseEvent e) {
        if (placingTower && currentMana >= towerCost) {
            int gridX = mouseX / CELLSIZE;
            int gridY = (mouseY - TOPBAR) / CELLSIZE;
            
            try {
                if (layout[gridY][gridX] == ' ') {  // Empty grass tile
                    Tower tower = new Tower(gridX, gridY, initialTowerRange, initialTowerFiringSpeed, initialTowerDamage, tower0Img, tower1Img, tower2Img);
                    
                    if (upgradingRange) {
                        float newMana = tower.upgradeRange(currentMana);
                        if (newMana != -1) {
                            currentMana = newMana;
                        } else {
                            upgradingRange = false;
                        }
                    }
                    
                    if (upgradingSpeed) {
                        float newMana = tower.upgradeSpeed(currentMana);
                        if (newMana != -1) {
                            currentMana = newMana;
                        } else {
                            upgradingSpeed = false;
                        }
                    }
                    
                    if (upgradingDamage) {
                        float newMana = tower.upgradeDamage(currentMana);
                        if (newMana != -1) {
                            currentMana = newMana;
                        } else {
                            upgradingDamage = false;
                        }
                    }
                    
                    towers.add(tower);
                    buildTowerButton.toggleActive();
                    placingTower = false;
                    currentMana -= towerCost;
                    
                    // Reset the upgrading flags
                    upgradingRange = false;
                    upgradingSpeed = false;
                    upgradingDamage = false;
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                // Simply do nothing if the mouse is released out of bounds
            }
        }
    }

    
    /*@Override
    public void mouseDragged(MouseEvent e) {
        
    }*/

    public void activateFastForwardButton() {
        if (gameSpeed == 1.0f) {
            gameSpeed = 2.0f;
        } else {
            gameSpeed = 1.0f;
        } fastForwardButton.toggleActive();
    }
    
    public void activateTowerUpgradesButton() {
        // Apply upgrades when a tower is clicked
        for (Tower tower : towers) {
            if (tower.isMouseOver(this)) {
                if (upgradeDesiredRange) {
                    float newMana = tower.upgradeRange(currentMana);
                    if (newMana != -1) {
                        currentMana = newMana;
                        upgradeRangeButton.toggleActive();
                        upgradeDesiredRange = false;
                    }
                }
                if (upgradeDesiredSpeed) {
                    float newMana = tower.upgradeSpeed(currentMana);
                    if (newMana != -1) {
                        currentMana = newMana;
                        upgradeSpeedButton.toggleActive();
                        upgradeDesiredSpeed = false;
                    }
                }
                if (upgradeDesiredDamage) {
                    float newMana = tower.upgradeDamage(currentMana);
                    if (newMana != -1) {
                        currentMana = newMana;
                        upgradeDamageButton.toggleActive();
                        upgradeDesiredDamage = false;
                    }
                }
            }
        }
    }
    
    private void activateBuildTowerModeButton() {
        if (placingTower) {
                placingTower = false;
                buildTowerButton.toggleActive();  // Reset if it's pressed again while active
            } else {
                buildTowerButton.toggleActive();
                placingTower = true;
            }
    }


    private void activateManaPoolSpellButton() {
        if (currentMana >= manaPoolSpellInitialCost) {
            currentMana -= manaPoolSpellInitialCost;
            manaPoolSpellInitialCost += manaPoolSpellCostInc; // Increase the cost for the next use
    
            manaCap *= manaPoolSpellCapMult; // Increase the mana cap
            manaRegenRate += manaRegenRate * manaPoolSpellManaGainedMult; // Increase mana regeneration rate
        }
    }


    public List<int[]> findBoundaryPathTiles() {
        List<int[]> boundaryTiles = new ArrayList<>();
    
        // Check top and bottom boundaries
        for (int x = 0; x < BOARD_WIDTH; x++) {
            if (layout[0][x] == 'X') {
                boundaryTiles.add(new int[]{x, 0});
            }
            if (layout[BOARD_HEIGHT - 1][x] == 'X') {
                boundaryTiles.add(new int[]{x, BOARD_HEIGHT - 1});
            }
        }
        
        // Check left and right boundaries
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            if (layout[y][0] == 'X') {
                boundaryTiles.add(new int[]{0, y});
            }
            if (layout[y][BOARD_WIDTH - 1] == 'X') {
                boundaryTiles.add(new int[]{BOARD_WIDTH - 1, y});
            }
        }
    
        return boundaryTiles;
    }


    // Draw all elements in the game by current frame.
	@Override
    public void draw() {
        if (!gamePaused) {
            currentMana = min(currentMana + (manaRegenRate * gameSpeed) / FPS, manaCap);
            
            //if (currentWaveIndex < waves.size()) {
            Wave currentWave = waves.get(waves.size() - 1);
            currentWave.update((1.0f / FPS) * gameSpeed);

            if (currentWave.shouldSpawnMonster()) {
                Monster monster = currentWave.spawnMonster(this, wizardHouseX, wizardHouseY);
                if (monster != null) {
                    activeMonsters.add(monster);
                }
            }
            if (currentWave.isWaveOver() && !isFinalWave) {
                System.out.println("WAVE OVER");
                currentWaveIndex++;
                loadNextWave();

            }
        }
        //}
        
    
        // Draw the game board
        board.drawGameBoard(this);

        // Drawing the UI components
        fill(150, 108, 51);
        rect(0, 0, WIDTH, TOPBAR);
        rect(CELLSIZE * BOARD_WIDTH, TOPBAR, SIDEBAR, HEIGHT - TOPBAR);
        
        // Draw towers and handle tower firing logic
        for (Tower tower : towers) {
            tower.display(this);

            Monster targetMonster = null;
            float closestDistance = Float.MAX_VALUE;
        
            // Reduce the cooldown for the tower on every frame
            tower.fireCooldown -= (1.0f / FPS) * gameSpeed;
        
            // Find the closest monster within the tower's range
            for (Monster monster : activeMonsters) {
                if (!monster.isDying) {
                    float distance = dist(tower.x * CELLSIZE + CELLSIZE / 2, tower.y * CELLSIZE + CELLSIZE / 2 + TOPBAR, monster.getExactX() * CELLSIZE, monster.getExactY() * CELLSIZE + TOPBAR);
                    if (distance <= tower.range && distance < closestDistance) {
                        targetMonster = monster;
                        closestDistance = distance;
                }
            } 
                }
                
        
            // If the tower's cooldown is up and there's a target, fire at the monster and reset cooldown
            if (!gamePaused) {
                if (targetMonster != null && tower.fireCooldown <= 0) {
                    tower.fire(targetMonster, this, fireballImg);
                    tower.fireCooldown = 1.0f / tower.firingSpeed;  // Reset the cooldown based on firing speed
                }
            } 
            

                // Handle each fireball for the current tower
                Iterator<Fireball> fireballIterator = tower.fireballs.iterator();
                while (fireballIterator.hasNext()) {
                    Fireball fireball = fireballIterator.next();
                    if (!gamePaused) {
                        fireball.move();
                    }
                    
                    if (fireball.hasReachedTarget()) {
                        // Deal damage to the target monster
                        Monster hitMonster = fireball.targetMonster;
                        float monsterHp = hitMonster.takeDamage(fireball.damage);

                        if (hitMonster.isDying) {
                            PImage deathImg = hitMonster.getDeathAnimationImage();
                            if (deathImg != null) {
                                float adjustedSize = 1f * 32;
                                float xOffset = (App.CELLSIZE - adjustedSize) / 2;
                                float yOffset = (App.CELLSIZE - adjustedSize) / 2;
                                image(deathImg, hitMonster.getExactX() * App.CELLSIZE + xOffset, hitMonster.getExactY() * App.CELLSIZE + App.TOPBAR + yOffset, adjustedSize, adjustedSize);
                                activeMonsters.remove(hitMonster);
                            }
                        } else {
                            fireballIterator.remove();
                        }     
                    } else {
                        fireball.display();
                    }
                }
            
        }
    
        // Move and draw monsters
        updateAndDrawMonsters();
        
        
    
        fastForwardButton.display(this);
        pauseButton.display(this);
        buildTowerButton.display(this);
        upgradeRangeButton.display(this);
        upgradeSpeedButton.display(this);
        upgradeDamageButton.display(this);
        manaPoolSpellButton.display(this);
        drawWaveCounter();
        drawManaBar();
    }


    public void drawWaveCounter() {
        fill(0);  // Set text color
        textSize(22);  // Set text size
        stroke(2);
    
        String waveText = "";

        if (currentWaveIndex < waves.size()) {
            Wave currentWave = waves.get(currentWaveIndex);

            if (currentWave.getPreWavePauseTime() < currentWave.getPreWavePause()) {
                // Wave hasn't started yet
                float remainingTime = currentWave.getPreWavePause() - currentWave.getPreWavePauseTime();
                waveText = "Wave " + (currentWaveIndex + 1) + " starts: " + (int)Math.max(0, Math.ceil(remainingTime));
            } else if (!currentWave.isWaveOver()) {
                // Wave is active
                float remainingTime = currentWave.getDuration() - currentWave.getElapsedTime();
                waveText = "Wave " + (currentWaveIndex + 1) + " ends: " + (int)Math.max(0, Math.ceil(remainingTime));
            } else {
                // Check if this is not the last wave
                if (currentWaveIndex + 1 < waves.size()) {
                    Wave nextWave = waves.get(currentWaveIndex + 1);
                    float remainingTime = nextWave.getPreWavePause() - nextWave.getPreWavePauseTime();
                    waveText = "Wave " + (currentWaveIndex + 2) + " starts: " + (int)Math.max(0, Math.ceil(remainingTime));
                } else {
                    // Last wave finished
                    waveText = "Final Wave Completed!";
                }
            }
        } else {
            // No more waves left
            waveText = "All Waves Spawned";
        }

        text(waveText, 10, 20);  // Position text in the top left corner
    }


    private void drawManaBar() {
        int barWidth = 300;
        int barHeight = 20;
        int startX = WIDTH - barWidth - 10;
        int startY = (TOPBAR - barHeight) / 2;

        // Calculate mana fill
        int fillWidth = (int) (barWidth * (currentMana / manaCap));

        fill(7, 222, 250);  // Blue for the filled portion
        rect(startX, startY, fillWidth, barHeight);

        fill(255);  // White for the unfilled portion
        rect(startX + fillWidth, startY, barWidth - fillWidth, barHeight);

        stroke(0);  // Black border for the mana bar
        noFill();
        rect(startX, startY, barWidth, barHeight);

        // Displaying the word "MANA:" to the left of the mana bar
        fill(0);  // Black text color
        textSize(18);
        textAlign(RIGHT, CENTER);
        text("MANA:", startX - 10, startY + barHeight / 2);

        // Displaying the current mana and the max mana within the mana bar
        String manaText = String.format("%.0f / %.0f", currentMana, manaCap);
        textAlign(CENTER, CENTER);
        text(manaText, startX + barWidth / 2, startY + barHeight / 2);
    }

    public float getCurrentMana() {
        return currentMana;
    }


    public static void main(String[] args) {
        PApplet.main("WizardTD.App");
    }
}