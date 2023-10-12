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

public class App extends PApplet {

    char[][] layout;

    //  Images of game elements
    private PImage[][] preprocessedPaths;
    private PImage grassImg, shrubImg, wizardHouseImg, path0Img, path1Img, path2Img, path3Img;

    //  Monsters && Waves handling
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

        // Load images
        grassImg = loadImage("src/main/resources/WizardTD/grass.png");
        shrubImg = loadImage("src/main/resources/WizardTD/shrub.png");
        wizardHouseImg = loadImage("src/main/resources/WizardTD/wizard_house.png");
        path0Img = loadImage("src/main/resources/WizardTD/path0.png");
        path1Img = loadImage("src/main/resources/WizardTD/path1.png");
        path2Img = loadImage("src/main/resources/WizardTD/path2.png");
        path3Img = loadImage("src/main/resources/WizardTD/path3.png");

        preprocessedPaths = new PImage[layout.length][layout[0].length];
        determinePaths();

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
    }


    /*public void calculateDistancesToWizardHouse() {
        distances = new int[BOARD_HEIGHT][BOARD_WIDTH];
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            Arrays.fill(distances[i], Integer.MAX_VALUE);
        }
    
        PriorityQueue<int[]> queue = new PriorityQueue<>((a, b) -> Integer.compare(distances[a[1]][a[0]], distances[b[1]][b[0]]));
        distances[wizardHouseY][wizardHouseX] = 0;
        queue.add(new int[]{wizardHouseX, wizardHouseY});
    
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Right, Down, Left, Up
    
        while (!queue.isEmpty()) {
            int[] current = queue.poll();
    
            for (int[] dir : directions) {
                int newX = current[0] + dir[0];
                int newY = current[1] + dir[1];
    
                if (newX >= 0 && newX < BOARD_WIDTH && newY >= 0 && newY < BOARD_HEIGHT && layout[newY][newX] == 'X' &&
                    distances[newY][newX] > distances[current[1]][current[0]] + 1) {
                    distances[newY][newX] = distances[current[1]][current[0]] + 1;
                    queue.add(new int[]{newX, newY});
                }
            }
        }
    }*/

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


    private void setupGameConfiguration() {
        try {
            JSONObject config = loadJSONObject(configPath);
            String layoutFilePath = config.getString("layout");
            layout = readLayoutFile(layoutFilePath);

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

    //  Receive key pressed signal from the keyboard.
	@Override
    public void keyPressed(){
        
    }

    //  Receive key released signal from the keyboard.
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


    /**
     * Draw all elements in the game by current frame.
     */
	@Override
    public void draw() {
        
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

        // Move and draw monsters
        updateAndDrawMonsters();

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