package WizardTD;

public class Monster {
    private float x, y;  // current position
    private MonsterType type;
    private App app;
    private int wizardHouseX, wizardHouseY;
    private float initialHp;  // Max health the monster spawns with
    private float currentHp;  // Monster's current health

    public Monster(float spawnX, float spawnY, MonsterType type, App app, int wizardHouseX, int wizardHouseY) {
        this.x = spawnX;
        this.y = spawnY;
        this.type = type;
        this.app = app;
        this.wizardHouseX = wizardHouseX;
        this.wizardHouseY = wizardHouseY;
        this.initialHp = type.getHp();
        this.currentHp = initialHp;  // When the monster spawns, its current health is its maximum health
    }

    public void draw() {
        float adjustedSize = 1f * 32;
        float xOffset = (App.CELLSIZE - adjustedSize) / 2;
        float yOffset = (App.CELLSIZE - adjustedSize) / 2;
    
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
        float moveAmount = type.getSpeed();  // This will be in pixels per frame

        char direction = app.pathDirections[Math.round(y)][Math.round(x)];
        switch(direction) {
            case 'U': y += moveAmount / 60; break;
            case 'D': y -= moveAmount / 60; break;
            case 'L': x += moveAmount / 60; break;
            case 'R': x -= moveAmount / 60; break;
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

    public float getCurrentHp() {
        return currentHp;
    }

    // Method to take damage from fireballs
    public boolean takeDamage(float damage) {
        this.currentHp -= damage;
        if (this.currentHp < 0) {
            this.currentHp = 0;  // Ensure health doesn't go below 0
            return true;
        }
        return false;
    }
}