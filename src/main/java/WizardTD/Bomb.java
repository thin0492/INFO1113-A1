package WizardTD;

import WizardTD.Interfaces.Drawable;
import processing.core.PApplet;
import processing.core.PImage;

import java.util.ArrayList;
import java.util.List;

public class Bomb implements Drawable{
    private PApplet p;
    private int x, y;
    private float damage = ConfigLoader.getBombDamage();
    private float radius = ConfigLoader.getBlastRadius();
    private float cost = ConfigLoader.getBombCost();
    public boolean hasExploded;
    private PImage bombImg;
    private PImage[] explosionImages;
    private int explosionFrameCounter = 0; // To track the number of frames since the explosion started
    public boolean explosionFinished = false;
    
    public Bomb(int x, int y, PImage bombImg, PImage[] explosionImages, PApplet p) {
        this.p = p;
        this.x = x;
        this.y = y;
        this.hasExploded = false;
        this.bombImg = bombImg;
        this.explosionImages = explosionImages;
    }

    

    public void checkAndExplode(List<Monster> monsters) {
        if (hasExploded || explosionFinished) return;

        for (Monster monster : monsters) {
            float distance = PApplet.dist(x * App.CELLSIZE + App.CELLSIZE / 2, y * App.CELLSIZE + App.CELLSIZE / 2 + App.TOPBAR, monster.getExactX() * App.CELLSIZE, monster.getExactY() * App.CELLSIZE + App.TOPBAR);
            if (distance <= radius) {
                monster.takeDamage(damage);
                if (monster.isDying) {
                    monsters.remove(monster);
                }
                
                hasExploded = true;
                break;
            }
        }
    }

    @Override
    public void draw(PApplet p) {
        if (!hasExploded) {
            // Draw the bomb before exploding
            p.image(bombImg, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR, App.CELLSIZE, App.CELLSIZE);
        } else if (!explosionFinished) {
            // Draw explosion animation
            int explosionIndex = explosionFrameCounter / 4 ;
            if (explosionIndex  < explosionImages.length) {
                float adjustedSize = 5f * 32;
                float xOffset = (App.CELLSIZE - adjustedSize) / 2;
                float yOffset = (App.CELLSIZE - adjustedSize) / 2;
                p.image(explosionImages[explosionIndex], x * App.CELLSIZE + xOffset, y * App.CELLSIZE + App.TOPBAR + yOffset, adjustedSize, adjustedSize);
                explosionFrameCounter++;
            }

            // Check if the explosion animation is finished
            if (explosionFrameCounter >= 4 * explosionImages.length) {
                explosionFinished = true;
            }
        }
    }
}
