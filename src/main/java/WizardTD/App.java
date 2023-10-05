package WizardTD;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.MouseEvent;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

//import java.io.*;
import java.util.*;

public class App extends PApplet {

    private PImage[][] preprocessedPaths;
    char[][] layout;
    private PImage grassImg;
    private PImage shrubImg;
    private PImage wizardHouseImg;
    private PImage path0Img;
    private PImage path1Img;
    private PImage path2Img;
    private PImage path3Img;
    private PImage tower0Img;
    private PImage tower1Img;
    private PImage tower2Img;
    private PImage gremlinImg;
    private PImage gremlin1Img;
    private PImage gremlin2Img;
    private PImage gremlin3Img;
    private PImage gremlin4Img;
    private PImage gremlin5Img;
    private PImage beetleImg;
    private PImage wormImg;
    //private PImage rotatedPathImg;

    private List<Wave> waves;
    private List<Monster> activeMonsters;
    private int currentWaveIndex = 0;

    public static final int CELLSIZE = 32;
    public static final int SIDEBAR = 120;
    public static final int TOPBAR = 40;
    public static final int BOARD_WIDTH = 20;
    public static final int BOARD_HEIGHT = 20;
    public static final int FPS = 60;

    public static int WIDTH = CELLSIZE*BOARD_WIDTH + SIDEBAR;
    public static int HEIGHT = CELLSIZE*BOARD_HEIGHT + TOPBAR;

    public String configPath;
    public Random random = new Random();
    

    public App() {
        this.configPath = "config.json";
    }

    //Initialise the setting of the window size.
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    //Load all resources such as images. Initialise the elements such as the player, enemies and map elements.
	@Override
    public void setup() {
        background(255);
        frameRate(FPS);
        try {
            JSONObject config = loadJSONObject(configPath);
            String layoutFilePath = config.getString("layout");
            layout = readLayoutFile(layoutFilePath);

            JSONArray waveData = config.getJSONArray("waves");
            waves = new ArrayList<>();
            for (int i = 0; i < waveData.size(); i++) {
                JSONObject waveObject = waveData.getJSONObject(i);
                float pre_wave_pause = waveObject.getFloat("pre_wave_pause");
                float duration = waveObject.getFloat("duration");
                
                JSONArray monstersData = waveObject.getJSONArray("monsters");
                List<MonsterType> monsterTypes = new ArrayList<>();
                for (int j = 0; j < monstersData.size(); j++) {
                    JSONObject monsterObject = monstersData.getJSONObject(j);
                    monsterTypes.add(new MonsterType(
                        monsterObject.getString("type"),
                        monsterObject.getFloat("hp"),
                        monsterObject.getFloat("speed"),
                        monsterObject.getFloat("armour"),
                        monsterObject.getFloat("mana_gained_on_kill")
                    ));
                } waves.add(new Wave(pre_wave_pause, duration, monsterTypes, this));
            } activeMonsters = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Load images during setup
        grassImg = loadImage("src/main/resources/WizardTD/grass.png");
        shrubImg = loadImage("src/main/resources/WizardTD/shrub.png");
        wizardHouseImg = loadImage("src/main/resources/WizardTD/wizard_house.png");
        tower0Img = loadImage("src/main/resources/WizardTD/tower0.png");
        tower1Img = loadImage("src/main/resources/WizardTD/tower1.png");
        tower2Img = loadImage("src/main/resources/WizardTD/tower2.png");
        path0Img = loadImage("src/main/resources/WizardTD/path0.png");
        path1Img = loadImage("src/main/resources/WizardTD/path1.png");
        path2Img = loadImage("src/main/resources/WizardTD/path2.png");
        path3Img = loadImage("src/main/resources/WizardTD/path3.png");
        gremlinImg = loadImage("src/main/resources/WizardTD/gremlin.png");
        gremlin1Img = loadImage("src/main/resources/WizardTD/gremlin1.png");
        gremlin2Img = loadImage("src/main/resources/WizardTD/gremlin2.png");
        gremlin3Img = loadImage("src/main/resources/WizardTD/gremlin3.png");
        gremlin4Img = loadImage("src/main/resources/WizardTD/gremlin4.png");
        gremlin5Img = loadImage("src/main/resources/WizardTD/gremlin5.png");
        beetleImg = loadImage("src/main/resources/WizardTD/beetle.png");
        wormImg = loadImage("src/main/resources/WizardTD/worm.png");

        
        preprocessedPaths = new PImage[layout.length][layout[0].length];
        determinePaths();
    
    }

    public char[][] readLayoutFile(String path) {
        char[][] layout = new char[BOARD_HEIGHT][BOARD_WIDTH];
        try {
            String[] lines = loadStrings(path);
            for (int i = 0; i < lines.length; i++) {
                // If a line is shorter than 20 characters, pad it with spaces
                while (lines[i].length() < BOARD_WIDTH) {
                    lines[i] += " ";
                }
                
                // Take only the first 20 characters from the line
                layout[i] = lines[i].substring(0, BOARD_WIDTH).toCharArray();
            }
            
            // If there are less than 20 lines, fill the remaining rows with spaces
            for (int i = lines.length; i < BOARD_HEIGHT; i++) {
                Arrays.fill(layout[i], ' ');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return layout;
    }

    /*
     * Receive key pressed signal from the keyboard.
     */
	@Override
    public void keyPressed(){
        
    }

    /*
     * Receive key released signal from the keyboard.
     */
	@Override
    public void keyReleased(){

    }

    @Override
    public void mousePressed(MouseEvent e) {
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    /*@Override
    public void mouseDragged(MouseEvent e) {

    }*/






    private void determinePaths() {
        //PImage rotatedPathImg = null;
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
                    //System.out.println(directions);
                    //PImage image = null;
                    //float angle = 0;
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

    private List<int[]> findBoundaryPathTiles() {
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










    /**
     * Draw all elements in the game by current frame.
     */
	@Override
    public void draw() {
        if (currentWaveIndex < waves.size()) {
            Wave currentWave = waves.get(currentWaveIndex);
            currentWave.update(1.0f / FPS);
            
            if (currentWave.shouldSpawnMonster()) {
                MonsterType monsterType = currentWave.getRandomMonsterType();
                
                // Get a list of valid starting positions
                List<int[]> validStartingPositions = findBoundaryPathTiles();
                if (!validStartingPositions.isEmpty()) {
                    // Choose a random starting position from the list
                    int randomIndex = (int) (Math.random() * validStartingPositions.size());
                    int[] startPosition = validStartingPositions.get(randomIndex);
                    
                    activeMonsters.add(new Monster(monsterType, this, startPosition));
                }
                
                if (currentWave.isWaveComplete()) {
                    currentWaveIndex++; // Move to the next wave
                }
            }
        }
        
        // Draw the board based on layout
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
                        image(shrubImg, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                        break;
                    case 'W':
                        image(shrubImg, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                        break;
                }
            }
        }

        // Drawing the wizard's house (drawing it separately to ensure it's on top of other tiles)
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (layout[y][x] == 'W') {
                    // Center the 48x48 image within the 32x32 tile
                    int xOffset = (CELLSIZE - 48) / 2;
                    int yOffset = (CELLSIZE - 48) / 2;
                    image(wizardHouseImg, x * CELLSIZE + xOffset, y * CELLSIZE + TOPBAR + yOffset, 48, 48);
                }
            }
        }

        // Move and draw monsters
        Iterator<Monster> iterator = activeMonsters.iterator();
        while (iterator.hasNext()) {
            Monster monster = iterator.next();
            monster.move();
            monster.draw();

            // Check if the monster has reached the wizard's house
            if (layout[monster.getPosition()[1]][monster.getPosition()[0]] == 'W') {
                // End the game
                println("Game Over!");
                noLoop(); // Stop the draw loop
            }
        }

        // Drawing the brown topbar
        fill(150, 108, 51);
        rect(0, 0, WIDTH, TOPBAR);

        // Drawing the brown sidebar on the right
        fill(150, 108, 51);
        rect(CELLSIZE * BOARD_WIDTH, 0, SIDEBAR, HEIGHT);
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