package WizardTD;

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
        float adjustedSize = 0.8f * App.CELLSIZE;
        float offsetX = (App.CELLSIZE - adjustedSize) / 2;
        float offsetY = (App.CELLSIZE - adjustedSize) / 2;
    
        // Draw the monster sprite
        app.image(type.getSprite(), x * App.CELLSIZE + offsetX, y * App.CELLSIZE + App.TOPBAR + offsetY, adjustedSize, adjustedSize);
    
        // Draw the health bar
        float healthBarWidth = adjustedSize;  // Health bar width equal to the width of the monster
        float healthBarHeight = 4;  // Arbitrary height for the health bar
        float healthBarX = x * App.CELLSIZE + offsetX;
        float healthBarY = y * App.CELLSIZE + App.TOPBAR + offsetY - healthBarHeight - 2;  // Above the monster, with a small gap
        float currentHealthPercentage = type.getHp() / type.getHp();  // Assuming you have a getMaxHp() method in MonsterType class
    
        app.fill(255, 0, 0);  // Red color for background of health bar
        app.rect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
        app.fill(0, 255, 0);  // Green color for current health
        app.rect(healthBarX, healthBarY, healthBarWidth * currentHealthPercentage, healthBarHeight);
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
