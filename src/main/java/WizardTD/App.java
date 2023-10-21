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
//import WizardTD.Tower;kk

public class App extends PApplet {

    char[][] layout;

    //  Images of game elements
    private PImage[][] preprocessedPaths;
    private PImage grassImg, shrub0Img, shrub1Img, shrub2Img, shrub3Img, shrub4Img, wizardHouseImg, path0Img, path1Img, path2Img, path3Img, tower0Img;
    private int[][] shrubTypes = new int[BOARD_HEIGHT][BOARD_WIDTH];


    //  Monsters & Waves handling
    private List<Wave> waves = new ArrayList<>();
    private List<Monster> activeMonsters = new ArrayList<>();
    private int currentWaveIndex = 0;
    private int wizardHouseX, wizardHouseY;


    //  Board dimensions
    public static final int CELLSIZE = 32;
    public static final int SIDEBAR = 120;
    public static final int TOPBAR = 40;
    public static final int BOARD_WIDTH = 20;
    public static final int BOARD_HEIGHT = 20;
    public static final int FPS = 60;
    public static int WIDTH = CELLSIZE * BOARD_WIDTH + SIDEBAR;
    public static int HEIGHT = CELLSIZE * BOARD_HEIGHT + TOPBAR;


    public String configPath;
    public Random random = new Random();
    public char[][] pathDirections = new char[BOARD_HEIGHT][BOARD_WIDTH];
    private Buttons fastForwardButton, pauseButton, buildTowerButton, upgradeRangeButton, upgradeSpeedButton, upgradeDamageButton, manaPoolSpellButton;
    private boolean gamePaused = false;
    private float gameSpeed = 1.0f;
    private ArrayList<Tower> towers = new ArrayList<>();
    private boolean placingTower = false;
    private float initialTowerRange;
    private float initialTowerFiringSpeed;
    private float initialTowerDamage;
    private boolean isBuildingTower = false;
    private float towerCost;
    private float currentMana;
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
        background(255);
        frameRate(FPS);
        
        // Load and setup game configuration
        setupGameConfiguration();

        int buttonSize = 50; // Size of the square button
        int buttonSpacing = 10; // Spacing between buttons
        int startX = CELLSIZE * BOARD_WIDTH + (SIDEBAR - buttonSize) / 2 - 20; // Moving buttons slightly to the left
        int startY = TOPBAR + buttonSpacing;

        fastForwardButton = new Buttons(startX, startY, buttonSize, "Fast Forward", "FF");
        pauseButton = new Buttons(startX, startY + (buttonSize + buttonSpacing), buttonSize, "Pause", "P");
        buildTowerButton = new Buttons(startX, startY + 2 * (buttonSize + buttonSpacing), buttonSize, "Build Tower", "T");
        upgradeRangeButton = new Buttons(startX, startY + 3 * (buttonSize + buttonSpacing), buttonSize, "Upgrade Range", "U1");
        upgradeSpeedButton = new Buttons(startX, startY + 4 * (buttonSize + buttonSpacing), buttonSize, "Upgrade Speed", "U2");
        upgradeDamageButton = new Buttons(startX, startY + 5 * (buttonSize + buttonSpacing), buttonSize, "Upgrade Damage", "U3");
        manaPoolSpellButton = new Buttons(startX, startY + 6 * (buttonSize + buttonSpacing), buttonSize, "Mana Spell", "M");

        // Load images
        grassImg = loadImage("src/main/resources/WizardTD/grass.png");
        shrub0Img = loadImage("src/main/resources/WizardTD/shrub0.png");
        shrub1Img = loadImage("src/main/resources/WizardTD/shrub1.png");
        shrub2Img = loadImage("src/main/resources/WizardTD/shrub2.png");
        shrub3Img = loadImage("src/main/resources/WizardTD/shrub3.png");
        shrub4Img = loadImage("src/main/resources/WizardTD/shrub4.png");
        wizardHouseImg = loadImage("src/main/resources/WizardTD/wizard_house.png");
        path0Img = loadImage("src/main/resources/WizardTD/path0.png");
        path1Img = loadImage("src/main/resources/WizardTD/path1.png");
        path2Img = loadImage("src/main/resources/WizardTD/path2.png");
        path3Img = loadImage("src/main/resources/WizardTD/path3.png");
        tower0Img = loadImage("src/main/resources/WizardTD/tower0.png");

        preprocessedPaths = new PImage[layout.length][layout[0].length];
        determinePaths();

        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (layout[y][x] == 'S') {
                    shrubTypes[y][x] = (int) random(0, 5);
                } else {
                    shrubTypes[y][x] = -1; // -1 indicates no shrub
                }
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


            JSONArray wavesArray = config.getJSONArray("waves");
            for (int i = 0; i < wavesArray.size(); i++) {
                JSONObject waveData = wavesArray.getJSONObject(i);
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

                    wave.addMonsterType(monsterType, monsterData.getInt("quantity"));
                }

                waves.add(wave);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void drawGameBoard() {
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                switch (layout[y][x]) {
                    case ' ':
                        image(grassImg, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                        break;
                    case 'X':
                        PImage rotatedPathImg = preprocessedPaths[y][x];
                        image(rotatedPathImg, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                        break;
                    case 'S':
                        image(grassImg, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                        break;
                    case 'W':
                        image(grassImg, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                        break;
                }
            }
        }

        // Drawing the wizard's house (drawing it separately to ensure it's on top of other tiles)
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (layout[y][x] == 'S') {
                    switch (shrubTypes[y][x]) {
                        case 0:
                            image(shrub0Img, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                            break;
                        case 1:
                            image(shrub1Img, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                            break;
                        case 2:
                            image(shrub2Img, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                            break;
                        case 3:
                            image(shrub3Img, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                            break;
                        case 4:
                            image(shrub4Img, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                            break;
                    }
                }
                
                else if (layout[y][x] == 'W') {
                    // Center the 48x48 image within the 32x32 tile
                    int xOffset = (CELLSIZE - 48) / 2;
                    int yOffset = (CELLSIZE - 48) / 2;
                    image(wizardHouseImg, x * CELLSIZE + xOffset, y * CELLSIZE + TOPBAR + yOffset, 48, 48);
                }
            }
        }
    }


    private void bfs(int wizardHouseX, int wizardHouseY) {
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
        }
    
        this.pathDirections = directions;
    }
    

    private void updateAndDrawMonsters() {
        Iterator<Monster> iterator = activeMonsters.iterator();
        while (iterator.hasNext()) {
            Monster monster = iterator.next();
            monster.move();
            monster.draw();

            // Check if the monster has reached the wizard's house
            int[] pos = monster.getPosition();
            if (pos[0] >= 0 && pos[0] < BOARD_WIDTH && pos[1] >= 0 && pos[1] < BOARD_HEIGHT) {
                if (layout[pos[1]][pos[0]] == 'W') {
                    // End the game
                    println("Game Over!");
                    noLoop(); // Stop the draw loop
                }
            }
        }
    }


    //  Receive key pressed signal from the keyboard.
	@Override
    public void keyPressed(){
        if (key == 't' || key == 'T') {
            placingTower = true;
        }
    }


    //  Receive key released signal from the keyboard.
	@Override
    public void keyReleased(){

    }


    // Recive mouse pressed signal from the mouse.
    @Override
    public void mousePressed(MouseEvent e) {
        if (fastForwardButton.isClicked(mouseX, mouseY)) {
            // Handle fast forward button click
        } else if (pauseButton.isClicked(mouseX, mouseY)) {
            // Handle pause button click
        } else if (buildTowerButton.isClicked(mouseX, mouseY)) {
            // Handle build tower button click
            activateBuildTowerMode();
        } else if (manaPoolSpellButton.isClicked(mouseX, mouseY)) {
            activateManaPoolSpell();
        } 
    }


    // Recieve mouse released signal from the mouse.
    @Override
    public void mouseReleased(MouseEvent e) {
        if (buildTowerButton.isClicked(mouseX, mouseY)) {
            isBuildingTower = true; // Set the tower-building mode
            return;
        }

        if (placingTower && currentMana >= towerCost) {
            int gridX = mouseX / CELLSIZE;
            int gridY = (mouseY - TOPBAR) / CELLSIZE;
            
            if (layout[gridY][gridX] == ' ') { // Empty grass tile
                towers.add(new Tower(gridX, gridY, initialTowerRange, initialTowerFiringSpeed, initialTowerDamage, tower0Img));

                placingTower = false;
                currentMana -= towerCost;
            }
        }
    }


    /*@Override
    public void mouseDragged(MouseEvent e) {

    }*/


    private void activateBuildTowerMode() {
        placingTower = true;
    }


    private void activateManaPoolSpell() {
        if (currentMana >= manaPoolSpellInitialCost) {
            currentMana -= manaPoolSpellInitialCost;
            manaPoolSpellInitialCost += manaPoolSpellCostInc; // Increase the cost for the next use
    
            manaCap *= manaPoolSpellCapMult; // Increase the mana cap
            manaRegenRate += manaRegenRate * manaPoolSpellManaGainedMult; // Increase mana regeneration rate
        }
    }
    

    private void determinePaths() {
        PImage image = null;
        float angle = 0;
        for (int i = 0; i < layout.length; i++) {
            for (int j = 0; j < layout[i].length; j++) {
                if (layout[i][j] == 'X') {
                    List<String> directions = new ArrayList<>();
                    if (i > 0 && layout[i-1][j] == 'X') directions.add("above");
                    if (i < layout.length-1 && layout[i+1][j] == 'X') directions.add("below");
                    if (j > 0 && layout[i][j-1] == 'X') directions.add("left");
                    if (j < layout[i].length-1 && layout[i][j+1] == 'X') directions.add("right");
                    if (directions.size() == 1) {
                        image = path0Img;
                        if (directions.contains("right") || directions.contains("left")) {
                            angle = 0;
                        } else if (directions.contains("above") || directions.contains("below")) {
                            angle = 90;
                        }
                    } else if (directions.size() == 2) {
                        if (directions.contains("above") && directions.contains("below")) {
                            image = path0Img;
                            angle = 90;
                        } else if (directions.contains("left") && directions.contains("right")) {
                            image = path0Img;
                            angle = 0;
                        } else if (directions.contains("left") && directions.contains("below")) {
                            image = path1Img;
                            angle = 0;
                        } else if (directions.contains("below") && directions.contains("right")) {
                            image = path1Img;
                            angle = 270;
                        } else if (directions.contains("right") && directions.contains("above")) {
                            image = path1Img;
                            angle = 180;
                        } else if (directions.contains("above") && directions.contains("left")) {
                            image = path1Img;
                            angle = 90;
                        }
                    } else if (directions.size() == 3) {
                        if (!directions.contains("left")) {
                            image = path2Img;
                            angle = 0;
                        } else if (!directions.contains("above")) {
                            image = path2Img;
                            angle = 90;
                        } else if (!directions.contains("right")) {
                            image = path2Img;
                            angle = 90;
                        } else if (!directions.contains("below")) {
                            image = path2Img;
                            angle = 180;
                        }
                    } else if (directions.size() == 4) {
                        image = path3Img;
                        angle = 0;
                    }
                    preprocessedPaths[i][j] = rotateImageByDegrees(image, angle);
                    
                }
            }
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
        currentMana = min(currentMana + manaRegenRate / FPS, manaCap);


        if (currentWaveIndex < waves.size()) {
            Wave currentWave = waves.get(currentWaveIndex);
            currentWave.update(1.0f / FPS);

            if (currentWave.shouldSpawnMonster()) {
                Monster monster = currentWave.spawnMonster(this, wizardHouseX, wizardHouseY);
                if (monster != null) {
                    activeMonsters.add(monster);
                }
            }
        }

        // Draw the game board
        drawGameBoard();

        // Draw towers
        /*for (Tower tower : towers) {
                    tower.display(this);
        }*/
        for (Tower tower : towers) {
            for (Monster monster : activeMonsters) {
                if (dist(tower.x * CELLSIZE + CELLSIZE/2, tower.y * CELLSIZE + CELLSIZE/2 + TOPBAR, monster.getExactX(), monster.getExactY()) <= tower.range) {
                    if (random(1) < tower.firingSpeed / FPS) {  // Random chance to fire based on firing speed
                        tower.fire(monster, this);
                    }
                }
            }
            // Draw and move fireballs for each tower
            Iterator<Fireball> fireballIterator = tower.fireballs.iterator();
            while (fireballIterator.hasNext()) {
                Fireball fireball = fireballIterator.next();
                fireball.move();
                if (fireball.hasReachedTarget()) {
                    // Deal damage to monster and remove the fireball
                    Monster hitMonster = null;
                    for (Monster monster : activeMonsters) {
                        if (dist(fireball.x, fireball.y, monster.getExactX(), monster.getExactY()) <= 5) {  // Example size for monster radius
                            float type.getHp() -= fireball.damage;
                            if (monster.hp <= 0) {
                                hitMonster = monster;  // Monster to be removed
                                break;
                            }
                        }
                    }
                    if (hitMonster != null) {
                        activeMonsters.remove(hitMonster);
                    }
                    fireballIterator.remove();
                } else {
                    fireball.display(this);
                }
            }
        }
                
        // Move and draw monsters
        updateAndDrawMonsters();

        // Drawing the brown topbar
        fill(150, 108, 51);
        rect(0, 0, WIDTH, TOPBAR);

        // Drawing the brown sidebar on the right
        fill(150, 108, 51);
        rect(CELLSIZE * BOARD_WIDTH, TOPBAR, SIDEBAR, HEIGHT - TOPBAR);

        fastForwardButton.display(this);
        pauseButton.display(this);
        buildTowerButton.display(this);
        upgradeRangeButton.display(this);
        upgradeSpeedButton.display(this);
        upgradeDamageButton.display(this);
        manaPoolSpellButton.display(this);

        /*int manaBarWidth = 300;  // Width of the mana bar
        int manaBarHeight = 20;  // Height of the mana bar
        int manaBarX = WIDTH - manaBarWidth - 10;  // 10 pixels from the right edge
        int manaBarY = (TOPBAR - manaBarHeight) / 2;  // Vertically centered in the top bar

        // Draw background (unfilled) mana bar
        fill(255);  // White color for the background
        rect(manaBarX, manaBarY, manaBarWidth, manaBarHeight);

        // Draw filled mana bar based on current mana
        float filledWidth = map(currentMana, 0, manaCap, 0, manaBarWidth);
        fill(7, 222, 250);  // Blue color for filled portion
        rect(manaBarX, manaBarY, filledWidth, manaBarHeight);*/

        drawManaBar();
        

    }


    private void drawManaBar() {
        int barWidth = 300;  // Making the bar 1.5 times wider
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


    public static void main(String[] args) {
        PApplet.main("WizardTD.App");
    }


    /**
     * Source: https://stackoverflow.com/questions/37758061/rotate-a-buffered-image-in-java
     * @param pimg The image to be rotated
     * @param angle between 0 and 360 degrees
     * @return the new rotated image
     */
    public PImage rotateImageByDegrees(PImage pimg, double angle) {
        BufferedImage img = (BufferedImage) pimg.getNative();
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = img.getWidth();
        int h = img.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        PImage result = this.createImage(newWidth, newHeight, RGB);
        //BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        BufferedImage rotated = (BufferedImage) result.getNative();
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
        for (int i = 0; i < newWidth; i++) {
            for (int j = 0; j < newHeight; j++) {
                result.set(i, j, rotated.getRGB(i, j));
            }
        }

        return result;
    }
}