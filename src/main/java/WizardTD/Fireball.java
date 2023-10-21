package WizardTD;

import processing.core.PApplet;
import processing.core.PImage;

public class Fireball {
    float x, y;   // Current position
    Monster targetMonster;  // Target position (center of the monster)
    float speed = 5;  // Speed in pixels per frame
    float damage;
    PImage img;  // Fireball image

    public Fireball(float x, float y, Monster targetMonster, float damage, PApplet p) {
        this.x = x;
        this.y = y;
        this.targetMonster = targetMonster;
        this.damage = damage;
        this.img = p.loadImage("src/main/resources/WizardTD/fireball.png");
    }

    public void move() {
        float targetX = targetMonster.getExactX();
        float targetY = targetMonster.getExactY();
        float dx = targetX - x;
        float dy = targetY - y;
        float distance = PApplet.dist(x, y, targetX, targetY);
        
        if (hasReachedTarget()) {
            targetMonster.takeDamage(damage);
            // You might also want to remove this fireball from the list in the Tower class
        } else {
            // Normalize
            float nx = dx / distance;
            float ny = dy / distance;
            
            // Move
            x += nx * speed;
            y += ny * speed;
        }
    }

    public boolean hasReachedTarget() {
        float targetX = targetMonster.getExactX();
        float targetY = targetMonster.getExactY();
        return PApplet.dist(x, y, targetX, targetY) < speed;
    }

    public void display(PApplet p) {
        p.image(img, x, y);  // Display the fireball image
    }
}

