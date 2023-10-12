package WizardTD;

import processing.core.PApplet;

public class Monster {
    private float x, y;  // current position
    private int currentTileX, currentTileY;
    private MonsterType type;
    private App app;
    private int wizardHouseX, wizardHouseY;

    public Monster(float spawnX, float spawnY, MonsterType type, App app, int wizardHouseX, int wizardHouseY) {
        this.x = spawnX;
        this.y = spawnY;
        this.type = type;
        this.app = app;
        this.wizardHouseX = wizardHouseX;
        this.wizardHouseY = wizardHouseY;
        
        this.currentTileX = Math.round(spawnX);
        this.currentTileY = Math.round(spawnY);
    }

    public void draw() {
        app.image(type.getSprite(), x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR, App.CELLSIZE, App.CELLSIZE);
    }

    public void move() {
        for (int i = 0; i < type.getSpeed(); i++) {
            char direction = app.pathDirections[Math.round(y)][Math.round(x)];

            switch(direction) {
                case 'U': y += 1; break;
                case 'D': y -= 1; break;
                case 'L': x += 1; break;
                case 'R': x -= 1; break;
            }

            // Update the monster's current tile after each pixel movement
            currentTileX = Math.round(x);
            currentTileY = Math.round(y);
        }
    }

    public int[] getPosition() {
        return new int[]{Math.round(x), Math.round(y)};
    }

    public float getExactX() {
        return x;
    }

    public float getExactY() {
        return y;
    }

    public MonsterType getType() {
        return type;
    }
}
