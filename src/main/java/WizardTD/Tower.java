package WizardTD;

import processing.core.PApplet;
import processing.core.PImage;

public class Tower {
    int x, y; // Position on the grid
    float range;
    float firingSpeed;
    float damage;
    int rangeUpgradeLevel = 0;
    int speedUpgradeLevel = 0;
    int damageUpgradeLevel = 0;
    PImage tower0Img;

    public Tower(int x, int y, float initialRange, float initialFiringSpeed, float initialDamage, PImage tower0Img) {
        this.x = x;
        this.y = y;
        this.range = initialRange;
        this.firingSpeed = initialFiringSpeed;
        this.damage = initialDamage;
        this.tower0Img = tower0Img;
    }

    public void display(PApplet p) {
        p.image(tower0Img, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR, App.CELLSIZE, App.CELLSIZE);

        
        // Here you'd add the visual indications for upgrades.
    }
    
    // ... (other methods for upgrades, firing, etc. will go here)
}
