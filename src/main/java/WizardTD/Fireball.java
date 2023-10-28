package WizardTD;

import WizardTD.Interfaces.Drawable;
import processing.core.PApplet;
import processing.core.PImage;

public class Fireball implements Drawable{
    float x, y;
    Monster targetMonster;
    float damage;
    float speed = 5;
    PApplet p;
    PImage fireballImg;

    public Fireball(float x, float y, Monster targetMonster, float damage, PApplet p, PImage fireballImg) {
        this.x = x;
        this.y = y;
        this.targetMonster = targetMonster;
        this.damage = damage;
        this.p = p;
        this.fireballImg = fireballImg;
    }

    public void move() {
        float targetX = targetMonster.getExactX() * App.CELLSIZE;
        float targetY = targetMonster.getExactY() * App.CELLSIZE + App.TOPBAR;

        float distance = PApplet.dist(x, y, targetX, targetY);
        float dx = (targetX - x) / distance;
        float dy = (targetY - y) / distance;
        x += dx * speed;
        y += dy * speed;
    }

    public boolean hasReachedTarget() {
        float distance = PApplet.dist(x, y, targetMonster.getExactX() * App.CELLSIZE, targetMonster.getExactY() * App.CELLSIZE + App.TOPBAR);
        return distance < speed;
    }

    @Override
    public void draw(PApplet p) {
        p.image(fireballImg, x, y);
    }
}

