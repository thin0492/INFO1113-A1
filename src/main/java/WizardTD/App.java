package WizardTD;

import processing.core.PApplet;
import processing.core.PImage;
import processing.event.MouseEvent;

import java.util.*;

public class App extends PApplet {

    public char[][] layout;

    // Classes
    private Board board;
    private ConfigLoader configLoader;

    //  Images of game elements
    private PImage tower0Img, tower1Img, tower2Img, fireballImg;
    public static PImage death1Img, death2Img, death3Img, death4Img, bombImg;
    public static PImage[] deathImages = new PImage[5];
    public static PImage[] explosionImages = new PImage[10];
    


    //  Monsters & Waves handling
    private List<Wave> waves = new ArrayList<>();
    private List<Monster> activeMonsters = new ArrayList<>();
    private int currentWaveIndex = 0;
    private int wizardHouseX, wizardHouseY;
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
    private float initialTowerRange;
    private float initialTowerFiringSpeed; 
    private float initialTowerDamage;
    public static boolean upgradingRange = false;
    public static boolean upgradingSpeed = false;
    public static boolean upgradingDamage = false;
    public static boolean upgradeDesiredRange, upgradeDesiredSpeed, upgradeDesiredDamage;


    public String configPath;
    public Random random = new Random();
    public char[][] pathDirections = new char[BOARD_HEIGHT][BOARD_WIDTH];
    private Buttons fastForwardButton, pauseButton, buildTowerButton, upgradeRangeButton, upgradeSpeedButton, upgradeDamageButton, manaPoolSpellButton, bombButton;
    public static boolean gamePaused = false;
    public static float gameSpeed = 1.0f;
    private ArrayList<Tower> towers = new ArrayList<>();
    private boolean placingTower = false;
    public List<Bomb> bombs = new ArrayList<>();
    private boolean placingBomb = false;
    
    private float towerCost;
    public float currentMana;
    private float manaCap;
    private float manaRegenRate;
    private float manaPoolSpellInitialCost;
    private float manaPoolSpellCostInc;
    private float manaPoolSpellCapMult;
    private float manaPoolSpellManaGainedMult;


    public App() {
        this.configPath = "config.json";
    }


    @Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }


    @Override
    public void setup() {
        surface.setLocation(0, 0);
        background(255);
        frameRate(FPS);
        
        // Load and setup game configuration
        configLoader = new ConfigLoader(this, this);
        

        configLoader.setupGameConfiguration(configPath);
        configLoader.readBombConfig();
        layout = configLoader.getLayout();
        board = new Board(this, layout);

        initialTowerRange = configLoader.getInitialTowerRange();
        initialTowerFiringSpeed = configLoader.getInitialTowerFiringSpeed(); 
        initialTowerDamage = configLoader.getInitialTowerDamage();
        towerCost = configLoader.getTowerCost();
        currentMana = configLoader.getCurrentMana();
        manaCap = configLoader.getManaCap();
        manaRegenRate = configLoader.getManaRegenRate();
        manaPoolSpellInitialCost = configLoader.getManaPoolSpellInitialCost();
        manaPoolSpellCostInc = configLoader.getManaPoolSpellCostInc();
        manaPoolSpellCapMult = configLoader.getManaPoolSpellCapMult();
        manaPoolSpellManaGainedMult = configLoader.getManaPoolSpellManaGainedMult();
        waves = configLoader.getWaves();

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
        bombButton = new Buttons(startX, startY + 7 * (buttonSize + buttonSpacing), buttonSize, "Place Bomb", "B");

        

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
        bombImg = loadImage("src/main/resources/WizardTD/bomb.png");
        for (int j = 0; j < 10; j++) {
            explosionImages[j] = loadImage("src/main/resources/WizardTD/explosion" + j + ".png");
        }
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
            char[] moveDirections = {'D', 'U', 'R', 'L'};
        
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
                monster.draw(this);
    
                // Check if the monster has reached the wizard's house
                float[] pos = monster.getPosition();
                if (pos[0] >= 0 && pos[0] < BOARD_WIDTH && pos[1] >= 0 && pos[1] < BOARD_HEIGHT) {
                    if (layout[Math.round(pos[1])][Math.round(pos[0])] == 'W') {
                        if (currentMana < monster.getCurrentHp()) {
                            println("Game Over!");
                            noLoop();
                        } else {
                            currentMana -= monster.getCurrentHp();
                            monster.takeDamage(monster.getCurrentHp());
    
                            iterator.remove();
                        }
                    }
                }
            } else {
                monster.draw(this);
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
            pauseButton.update();
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
            upgradeRangeButton.update();
            upgradeDesiredRange = !upgradeDesiredRange;
        } else if (key == '2') {
            upgradeSpeedButton.update();
            upgradeDesiredSpeed = !upgradeDesiredSpeed;
        } else if (key == '3') {
            upgradeDamageButton.update();
            upgradeDesiredDamage = !upgradeDesiredDamage;
        } activateTowerUpgradesButton();
        // Mana pool spell key
        if (key == 'm' || key == 'M') {
            activateManaPoolSpellButton();
        } if (key == 'b' || key == 'B') {
            bombButton.update();
            placingBomb = !placingBomb;
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
            pauseButton.update();
        // Build tower button
        } else if (buildTowerButton.isClicked(mouseX, mouseY)) {
            activateBuildTowerModeButton();
        
        // Mana pool spell button
        } if (manaPoolSpellButton.isClicked(mouseX, mouseY)) {
            manaPoolSpellButton.update();
            activateManaPoolSpellButton();
            manaPoolSpellButton.update();

        // Upgrade tower buttons
        } if (upgradeRangeButton.isClicked(mouseX, mouseY)) {
            upgradeRangeButton.update();
            upgradeDesiredRange = !upgradeDesiredRange;
        } else if (upgradeSpeedButton.isClicked(mouseX, mouseY)) {
            upgradeSpeedButton.update();
            upgradeDesiredSpeed = !upgradeDesiredSpeed;
        } else if (upgradeDamageButton.isClicked(mouseX, mouseY)) {
            upgradeDamageButton.update();
            upgradeDesiredDamage = !upgradeDesiredDamage;
        } activateTowerUpgradesButton();

        if (bombButton.isClicked(mouseX, mouseY)) {
            bombButton.update();
            placingBomb = !placingBomb;
        }
    }


    
    
    @Override
    public void mouseReleased(MouseEvent e) {
        int gridX = mouseX / CELLSIZE;
        int gridY = (mouseY - TOPBAR) / CELLSIZE;
        if (placingTower && currentMana >= towerCost) {
            try {
                if (layout[gridY][gridX] == ' ' && !isTowerAtPosition(gridX, gridY)) {
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
                    buildTowerButton.update();
                    placingTower = false;
                    currentMana -= towerCost;
                
                    upgradingRange = false;
                    upgradingSpeed = false;
                    upgradingDamage = false;
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
            }
        }
        if (placingBomb) {
            activateBombButton(gridX, gridY);
        }
    }

    
    /*@Override
    public void mouseDragged(MouseEvent e) {
        
    }*/

    private boolean isTowerAtPosition(int x, int y) {
        for (Tower tower : towers) {
            if (tower.getX() == x && tower.getY() == y) {
                return true;
            }
        }
        return false;
    }

    public void activateFastForwardButton() {
        if (gameSpeed == 1.0f) {
            gameSpeed = 2.0f;
        } else {
            gameSpeed = 1.0f;
        } fastForwardButton.update();
    }
    
    public void activateTowerUpgradesButton() {
        for (Tower tower : towers) {
            if (tower.isMouseOver(this)) {
                if (upgradeDesiredRange) {
                    float newMana = tower.upgradeRange(currentMana);
                    if (newMana != -1) {
                        currentMana = newMana;
                        upgradeRangeButton.update();
                        upgradeDesiredRange = false;
                    }
                }
                if (upgradeDesiredSpeed) {
                    float newMana = tower.upgradeSpeed(currentMana);
                    if (newMana != -1) {
                        currentMana = newMana;
                        upgradeSpeedButton.update();
                        upgradeDesiredSpeed = false;
                    }
                }
                if (upgradeDesiredDamage) {
                    float newMana = tower.upgradeDamage(currentMana);
                    if (newMana != -1) {
                        currentMana = newMana;
                        upgradeDamageButton.update();
                        upgradeDesiredDamage = false;
                    }
                }
            }
        }
    }
    
    private void activateBuildTowerModeButton() {
        if (placingTower) {
                placingTower = false;
                buildTowerButton.update();  // Reset if it's pressed again while active
            } else {
                buildTowerButton.update();
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

    public void activateBombButton(int gridX, int gridY) {
        try {
            if (layout[gridY][gridX] == 'X') {
                Bomb bomb = new Bomb(gridX, gridY, bombImg, explosionImages, this); // Damage and radius can be adjusted
                bombs.add(bomb);
                System.out.println(ConfigLoader.getBombCost());
                currentMana -= ConfigLoader.getBombCost();
                bombButton.update();
                placingBomb = !placingBomb;
            }
        } catch (Exception e) {
            e.getStackTrace();
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
                currentWaveIndex++;
                configLoader.loadNextWave();

            } if (currentWave.isWaveOver() && isFinalWave && activeMonsters.isEmpty()) {
                // Display "YOU WIN" text
                textAlign(CENTER, CENTER);
                textSize(64); // Adjust text size as needed
                fill(0, 255, 0);
                text("YOU WIN", width / 2, height / 2); // Position the text in the center
            }
        }
        //}
        
    
        // Draw the game board
        board.draw(this);

        // Drawing the UI components
        fill(150, 108, 51);
        rect(0, 0, WIDTH, TOPBAR);
        rect(CELLSIZE * BOARD_WIDTH, TOPBAR, SIDEBAR, HEIGHT - TOPBAR);
        
        // Draw towers and handle tower firing logic
        for (Tower tower : towers) {
            tower.draw(this);

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
                
        
            if (!gamePaused) {
                if (targetMonster != null && tower.fireCooldown <= 0) {
                    tower.fire(targetMonster, this, fireballImg);
                    tower.fireCooldown = 1.0f / tower.firingSpeed;
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
                        hitMonster.takeDamage(fireball.damage);
                        
                        if (hitMonster.isDying) {
                            if (!hitMonster.hasManaBeenGiven()) {
                                hitMonster.setManaGiven(true);
                            }
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
                        fireball.draw(this);
                    }
                }
            
        }
    
        // Move and draw monsters
        updateAndDrawMonsters();
        
        Iterator<Bomb> bombIterator = bombs.iterator();
        while (bombIterator.hasNext()) {
            Bomb bomb = bombIterator.next();
            bomb.checkAndExplode(activeMonsters);
            bomb.draw(this);
            if (bomb.explosionFinished) {
                bombIterator.remove();
            }
        }
        
    
        fastForwardButton.draw(this);
        pauseButton.draw(this);
        buildTowerButton.draw(this);
        upgradeRangeButton.draw(this);
        upgradeSpeedButton.draw(this);
        upgradeDamageButton.draw(this);
        manaPoolSpellButton.draw(this);
        bombButton.draw(this);
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