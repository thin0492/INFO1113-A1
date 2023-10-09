package WizardTD;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import processing.core.PImage;

public class Monster {
    private MonsterType monsterType;
    private float currentHp;
    private int[] position; // [x, y]
    private int deathAnimationFrame;
    private App app;
    private int[] currentPos;
    private int[][] DIRECTIONS = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
    private int[] lastDirection = {0, 0};

    public Monster(MonsterType monsterType, App app) {
        this.monsterType = monsterType;
        this.currentHp = monsterType.getHp();
        this.app = app;
        this.position = getBoundaryStartPosition();
        this.currentPos = position.clone();
        this.deathAnimationFrame = 0;
        this.lastDirection = new int[]{0, 0};
    }

    private int[] getBoundaryStartPosition() {
        List<int[]> boundaryStartPositions = app.findBoundaryPathTiles();
        Random rand = new Random();
        return boundaryStartPositions.get(rand.nextInt(boundaryStartPositions.size()));
    }

    public int[] getPosition() {
        return position;
    }

    public void move() {
        for (int[] dir : DIRECTIONS) {
            int newX = currentPos[0] + dir[0];
            int newY = currentPos[1] + dir[1];

            if (newX >= 0 && newX < App.BOARD_WIDTH && newY >= 0 && newY < App.BOARD_HEIGHT 
                && app.layout[newY][newX] == 'X' && !Arrays.equals(dir, oppositeDirection(lastDirection))) {
                currentPos[0] = newX;
                currentPos[1] = newY;
                lastDirection = dir;
                break;
            }
        }
    }

    private int[] oppositeDirection(int[] direction) {
        return new int[]{-direction[0], -direction[1]};
    }

    public void draw() {
        PImage sprite = app.loadImage("src/main/resources/WizardTD/" + monsterType.getType() + ".png");
        //app.image(sprite, position[0], position[1]);
        //app.image(sprite, 4, 5);
        // Additional logic to draw HP bar and death animation
    }

    // Other methods to handle monster-specific logic will be added later
}

