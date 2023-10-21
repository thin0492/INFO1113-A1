package WizardTD;

import java.util.ArrayList;
import java.util.List;

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
    boolean isBeingPlaced = false;
    List<Fireball> fireballs = new ArrayList<>();
    private float timeSinceLastFire = 0;

    public Tower(int x, int y, float initialRange, float initialFiringSpeed, float initialDamage, PImage tower0Img) {
        this.x = x;
        this.y = y;
        this.range = initialRange;
        this.firingSpeed = initialFiringSpeed;
        this.damage = initialDamage;
        this.tower0Img = tower0Img;
    }


    public boolean isMouseOver(PApplet p) {
        int mouseX = p.mouseX;
        int mouseY = p.mouseY;
        return mouseX >= x * App.CELLSIZE && mouseX <= (x + 1) * App.CELLSIZE && mouseY >= y * App.CELLSIZE + App.TOPBAR && mouseY <= (y + 1) * App.CELLSIZE + App.TOPBAR;
    }
    

    public void update(List<Monster> monsters, PApplet p) {
        timeSinceLastFire += 1.0 / p.frameRate;
        if (timeSinceLastFire >= 1.0 / firingSpeed) {
            for (Monster monster : monsters) {
                float distance = PApplet.dist(x * App.CELLSIZE + App.CELLSIZE/2, y * App.CELLSIZE + App.CELLSIZE/2 + App.TOPBAR, monster.getExactX(), monster.getExactY());
                if (distance <= range) {
                    fire(monster, p);
                    timeSinceLastFire = 0;
                    break;  // Only shoot at one monster for now
                }
            }
        }
    }


    public void fire(Monster monster, PApplet p) {
        Fireball fireball = new Fireball(x * App.CELLSIZE + App.CELLSIZE/2, y * App.CELLSIZE + App.CELLSIZE/2 + App.TOPBAR, monster, damage, p);
        fireballs.add(fireball);
    }


    public void display(PApplet p) {
        p.image(tower0Img, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR, App.CELLSIZE, App.CELLSIZE);

        if (isMouseOver(p)) {
            p.noFill();
            p.stroke(255, 255, 0); // Yellow colour
            p.ellipse(x * App.CELLSIZE + App.CELLSIZE / 2, y * App.CELLSIZE + App.CELLSIZE / 2 + App.TOPBAR, range * 2, range * 2);
            p.stroke(0); // Reset to black colour
        }
        // Here you'd add the visual indications for upgrades.
    }
    
    // ... (other methods for upgrades, firing, etc. will go here)
}
