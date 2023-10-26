package WizardTD;

import processing.core.PApplet;
import processing.core.PImage;

public class Monster extends PApplet {
    private static final int FRAMES_PER_DEATH_IMAGE = 4;
    private float x, y;  // current position
    private MonsterType type;
    private App app;
    private float initialHp;  // Max health the monster spawns with
    private float currentHp;  // Monster's current health
    private int deathAnimationFrame = 0;
    private String monsterType;
    public int imageIndex = 0;
    public float adjustedSize = 1f * 32;
    public float xOffset = (App.CELLSIZE - adjustedSize) / 2;
    public float yOffset = (App.CELLSIZE - adjustedSize) / 2;

    public boolean isDying = false;

    public Monster(float spawnX, float spawnY, MonsterType type, App app, int wizardHouseX, int wizardHouseY) {
        this.x = spawnX;
        this.y = spawnY;
        this.type = type;
        this.app = app;
        this.initialHp = type.getHp();
        this.currentHp = initialHp;  // When the monster spawns, its current health is its maximum health
        this.monsterType = type.getType();
    }

    public void draw() {
        //float adjustedSize = 1f * 32;
        //float xOffset = (App.CELLSIZE - adjustedSize) / 2;
        //float yOffset = (App.CELLSIZE - adjustedSize) / 2;
    
        // Draw the monster sprite
        app.image(type.getSprite(), x * App.CELLSIZE + xOffset, y * App.CELLSIZE + App.TOPBAR + yOffset, adjustedSize, adjustedSize);
    
        // Draw the health bar
        float healthBarWidth = adjustedSize;  // Health bar width equal to the width of the monster
        float healthBarHeight = 4;  // Arbitrary height for the health bar
        float healthBarX = x * App.CELLSIZE + xOffset;
        float healthBarY = y * App.CELLSIZE + App.TOPBAR + yOffset - healthBarHeight - 2;  // Above the monster, with a small gap
        float currentHealthPercentage = currentHp / initialHp;
    
        app.fill(255, 0, 0);  // Red color for background of health bar
        app.rect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
        app.fill(0, 255, 0);  // Green color for current health
        app.rect(healthBarX, healthBarY, healthBarWidth * currentHealthPercentage, healthBarHeight);
    }

    public void move() {
        float moveAmount = type.getSpeed() * App.gameSpeed;  // This will be in pixels per frame

        char direction = app.pathDirections[Math.round(y)][Math.round(x)];
        switch(direction) {
            case 'U': y += moveAmount / App.FPS; break;
            case 'D': y -= moveAmount / App.FPS; break;
            case 'L': x += moveAmount / App.FPS; break;
            case 'R': x -= moveAmount / App.FPS; break;
        }
    }

    public int[] getPosition() {
        return new int[]{Math.round(x), Math.round(y)};
    }

    public float takeDamage(float damage) {
        this.currentHp -= damage * type.getArmour();
        if (this.currentHp < 0) {
            this.currentHp = 0;  // Ensure health doesn't go below 0
            isDying = true;
            return 0;
        }
        return this.currentHp;
    }

    public PImage getDeathAnimationImage() {
        if (isDying) {
            
            deathAnimationFrame++;
    
            if (deathAnimationFrame > FRAMES_PER_DEATH_IMAGE / App.gameSpeed) {
                isDying = false;
                deathAnimationFrame = 0;
                imageIndex++;
                return null;
            }
    
            if (imageIndex < 5) {
                return App.deathImages[imageIndex];
            }
        }
        return null;
    }


    public float getExactX() {
        return x;
    }

    public float getExactY() {
        return y;
    }

    public float getWidth() {
        return xOffset;
    }

    public float getHeight() {
        return yOffset;
    }

    public MonsterType getType() {
        return type;
    }

    public float getCurrentHp() {
        return currentHp;
    }  
}