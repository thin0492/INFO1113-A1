package WizardTD;

import WizardTD.Interfaces.Drawable;
import processing.core.PApplet;
import processing.core.PImage;

public class Monster implements Drawable{
    public static final int FRAMES_PER_DEATH_IMAGE = 4;
    private float x, y;  // current position
    private MonsterType type;
    private App app;
    private float initialHp;  // Max health the monster spawns with
    private float currentHp;  // Monster's current health
    private int deathAnimationFrame = 0;
    public int imageIndex = 0;
    public float adjustedSize = 0.8f * App.CELLSIZE;
    public float xOffset = (App.CELLSIZE - adjustedSize) / 2;
    public float yOffset = (App.CELLSIZE - adjustedSize) / 2;
    private char lastDir = ' ';

    public boolean isDying = false;
    private boolean manaGiven = false;

    public Monster(float spawnX, float spawnY, MonsterType type, App app, int wizardHouseX, int wizardHouseY) {
        this.x = spawnX;
        this.y = spawnY;
        this.type = type;
        this.app = app;
        this.initialHp = type.getHp();
        this.currentHp = initialHp;  // When the monster spawns, its current health is its maximum health
    }

    @Override
    public void draw(PApplet p) {
    
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
        float moveAmount = (type.getSpeed() * App.gameSpeed) / App.FPS;  // This will be in pixels per frame
        float edgeX = Math.round(x) - xOffset;  // Far edge in the horizontal direction
        float edgeY = Math.round(y) - yOffset;

        char direction = app.pathDirections[Math.round(y)][Math.round(x)];
        Runnable continueLastDirection = () -> {
            switch (lastDir) {
                case 'U': y -= moveAmount; break;
                case 'D': y += moveAmount; break;
                case 'L': x -= moveAmount; break;
                case 'R': x += moveAmount; break;
            }
        };
    
        // Check for direction change

        //if (lastDir != direction) {
            if (lastDir == ' ') {
                lastDir = direction;
            }
            switch (direction) {
                case 'U':
                    if (lastDir == 'L') {
                        if (x - xOffset < edgeX) {
                        
                            lastDir = 'U';
                            y -= moveAmount;
                        } else {
                            continueLastDirection.run();
                        }
                    } else if (lastDir == 'R') {
                        if (x - xOffset > edgeX) {
                        
                            lastDir = 'U';
                            y -= moveAmount;
                        } else {
                            continueLastDirection.run();
                        }
                    }
                    
                     else {
                        continueLastDirection.run();
                    }
                    break;

                case 'D':
                    if (lastDir == 'L') {
                        if (x - xOffset <= edgeX) {
                        
                            lastDir = 'D';
                            y += moveAmount;
                        } else {
                            continueLastDirection.run();
                        }
                    } else if (lastDir == 'R') {
                        if (x - xOffset >= edgeX) {
                        
                            lastDir = 'D';
                            y += moveAmount;
                        } else {
                            continueLastDirection.run();
                        }
                    }
                    
                     else {
                        continueLastDirection.run();
                    }
                    break;

                case 'L':
                    if (lastDir == 'U') {
                        if (y - yOffset <= edgeY) {
                        
                            lastDir = 'L';
                            x -= moveAmount;
                        } else {
                            continueLastDirection.run();
                        }
                    } else if (lastDir == 'D') {
                        if (y - yOffset >= edgeY) {
                        
                            lastDir = 'L';
                            x -= moveAmount;
                        } else {
                            continueLastDirection.run();
                        }
                    }
                    
                     else {
                        continueLastDirection.run();
                    }
                    break;
                case 'R':
                    if (lastDir == 'U') {
                        if (y - yOffset <= edgeY) {
                        
                            lastDir = 'R';
                            x += moveAmount;
                        } else {
                            continueLastDirection.run();
                        }
                    } else if (lastDir == 'D') {
                        if (y - yOffset >= edgeY) {
                        
                            lastDir = 'R';
                            x += moveAmount;
                        } else {
                            continueLastDirection.run();
                        }
                    }
                    
                     else {
                        continueLastDirection.run();
                    }
                    break;
            }
        //} else {
            // If the direction hasn't changed, just continue moving
            //continueLastDirection.run();
        //}
    }

    public float[] getPosition() {
        return new float[]{x, y};
    }

    public float takeDamage(float damage) {
        this.currentHp -= damage * type.getArmour();
        if (this.currentHp < 0) {
            this.currentHp = 0;  // Ensure health doesn't go below 0
            isDying = true;
            manaGiven = false;
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

    public boolean hasManaBeenGiven() {
        return manaGiven;
    }

    public void setManaGiven(boolean given) {
        this.manaGiven = given;
    }
}