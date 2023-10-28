package WizardTD;

import WizardTD.Interfaces.Drawable;
import processing.core.PApplet;
import processing.core.PImage;

public class Monster implements Drawable{
    public static final int FRAMES_PER_DEATH_IMAGE = 4;
    private float x, y; 
    private MonsterType type;
    private App app;
    private float initialHp;  
    private float currentHp;  
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
        this.currentHp = initialHp;
    }

    @Override
    public void draw(PApplet p) {
        
        app.image(type.getSprite(), x * App.CELLSIZE + xOffset, y * App.CELLSIZE + App.TOPBAR + yOffset, adjustedSize, adjustedSize);
    
        float healthBarWidth = adjustedSize;  
        float healthBarHeight = 4;  
        float healthBarX = x * App.CELLSIZE + xOffset;
        float healthBarY = y * App.CELLSIZE + App.TOPBAR + yOffset - healthBarHeight - 2;  
        float currentHealthPercentage = currentHp / initialHp;
    
        app.fill(255, 0, 0);  
        app.rect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
        app.fill(0, 255, 0); 
        app.rect(healthBarX, healthBarY, healthBarWidth * currentHealthPercentage, healthBarHeight);
    }

    public void move() {
        float moveAmount = (type.getSpeed() * App.gameSpeed) / App.FPS; 
        float edgeX = Math.round(x) - xOffset;  
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
      
    }

    public float[] getPosition() {
        return new float[]{x, y};
    }

    public float takeDamage(float damage) {
        this.currentHp -= damage * type.getArmour();
        if (this.currentHp < 0) {
            this.currentHp = 0; 
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