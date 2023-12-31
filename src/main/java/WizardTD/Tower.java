package WizardTD;

import java.util.ArrayList;
import java.util.List;

import WizardTD.Interfaces.Drawable;



import processing.core.PApplet;
import processing.core.PImage;

public class Tower implements Drawable{
    int x, y;
    float range, firingSpeed, damage;
    int rangeUpgradeLevel = 0;
    int speedUpgradeLevel = 0;
    int damageUpgradeLevel = 0;
    boolean rangeUpgrade = false;
    boolean speedUpgrade = false;
    boolean damageUpgrade = false;
    public int rangeCost, speedCost, damageCost;
    public int towerType;

    PImage tower0Img, tower1Img, tower2Img;
    boolean isBeingPlaced = false;
    public List<Fireball> fireballs = new ArrayList<>();
    public float fireCooldown = 0;
    public Fireball currentFireball = null;

    private static final int UPGRADE_COST_INCREMENT = 10;
    private static final int BASE_UPGRADE_COST = 20;
    private static final float RANGE_UPGRADE_AMOUNT = 32;
    private static final float SPEED_UPGRADE_AMOUNT = 0.5f;

    public Tower(int x, int y, float initialRange, float initialFiringSpeed, float initialDamage, PImage tower0Img, PImage tower1Img, PImage tower2Img) {
        this.x = x;
        this.y = y;
        this.range = initialRange;
        this.firingSpeed = initialFiringSpeed;
        this.damage = initialDamage;
        this.tower0Img = tower0Img; this.tower1Img = tower1Img; this.tower2Img = tower2Img;
        
        
    }

    

    public boolean isMouseOver(PApplet p) {
        int mouseX = p.mouseX;
        int mouseY = p.mouseY;
        return mouseX >= x * App.CELLSIZE && mouseX <= (x + 1) * App.CELLSIZE && mouseY >= y * App.CELLSIZE + App.TOPBAR && mouseY <= (y + 1) * App.CELLSIZE + App.TOPBAR;
    }


    public void fire(Monster monster, PApplet p, PImage fireballImg) {
        Fireball fireball = new Fireball(x * App.CELLSIZE + App.CELLSIZE/2, y * App.CELLSIZE + App.CELLSIZE/2 + App.TOPBAR, monster, damage, p, fireballImg);
        fireballs.add(fireball);
    }
      
    
    public float upgradeRange(float currentMana) {
        if (currentMana >= getUpgradeCostRange()) {
            range += RANGE_UPGRADE_AMOUNT;
            rangeUpgradeLevel++;
            currentMana -= getUpgradeCostRange();
            return currentMana; 
        }
        return -1;
    } public int getUpgradeCostRange() {
        int rangeCost = BASE_UPGRADE_COST + rangeUpgradeLevel * UPGRADE_COST_INCREMENT;
        return rangeCost;
    }
    
    public float upgradeSpeed(float currentMana) {
        if (currentMana >= getUpgradeCostSpeed()) {
            firingSpeed += SPEED_UPGRADE_AMOUNT;
            speedUpgradeLevel++;
            currentMana -= getUpgradeCostSpeed();
            return currentMana;
        }
        return -1;
    } public int getUpgradeCostSpeed() {
        int speedCost = BASE_UPGRADE_COST + speedUpgradeLevel * UPGRADE_COST_INCREMENT;
        return speedCost;
    }
    
    public float upgradeDamage(float currentMana) {
        if (currentMana >= getUpgradeCostDamage()) {
            damage += damage / 2;
            damageUpgradeLevel++;
            currentMana -= getUpgradeCostDamage();
            return currentMana;
        }
        return -1;
    } public int getUpgradeCostDamage() {
        int damageCost = BASE_UPGRADE_COST + damageUpgradeLevel * UPGRADE_COST_INCREMENT;
        return damageCost;
    }

    public void drawUpgradeCosts(PApplet p) {
        int tableWidth = 100; 
        int tableHeight = 140; 
        int startX = App.WIDTH - tableWidth - 10;
        int startY = App.HEIGHT - tableHeight - 10;
        int rowHeight = 10; 
        int padding = 10; 
        
        p.fill(255);
        p.rect(startX, startY, tableWidth, tableHeight);
        
        p.textSize(12);
        p.fill(0);
        p.text("Upgrade cost", startX + padding + 35, startY + rowHeight + 5);
        
        p.line(startX, startY + 2*padding + rowHeight, startX + tableWidth, startY + 2*padding + rowHeight);
        
        if (App.upgradeDesiredRange) {
            p.text("Range:", startX + padding + 17, startY + 3*padding + 2*rowHeight); p.text(getUpgradeCostRange(), startX + tableWidth - padding, startY + 3*padding + 2*rowHeight);
            rangeCost = getUpgradeCostRange();  
        } else {
            rangeCost = 0; 
        } 

        if (App.upgradeDesiredSpeed) {
            p.text("Speed:", startX + padding + 17, startY + 4*padding + 3*rowHeight); p.text(getUpgradeCostSpeed(), startX + tableWidth - padding, startY + 4*padding + 3*rowHeight);
            speedCost = getUpgradeCostSpeed();
        } else {
            speedCost = 0;
        } 

        if (App.upgradeDesiredDamage) {
            p.text("Damage:", startX + padding + 23, startY + 5*padding + 4*rowHeight); p.text(getUpgradeCostDamage(), startX + tableWidth - padding, startY + 5*padding + 4*rowHeight); 
            damageCost = getUpgradeCostDamage();
        } else {
            damageCost = 0;
        }
        
        p.line(startX, startY + 6*padding + 5*rowHeight, startX + tableWidth, startY + 6*padding + 5*rowHeight);
        
        int totalCost = rangeCost + speedCost + damageCost;
        p.text("Total:", startX + padding + 11, startY + 7*padding + 6*rowHeight - 3); p.text(totalCost, startX + tableWidth - padding, startY + 7*padding + 6*rowHeight - 3);
    }


    private void drawUpgradeIndications(PApplet p) {
        int towerCenterX = x * App.CELLSIZE + App.CELLSIZE / 2;
        int towerBaseX = x * App.CELLSIZE;
        int towerCenterY = y * App.CELLSIZE + App.TOPBAR + App.CELLSIZE / 2;
        int upgradeSymbolSize = 3; 
        int spacing = 4; 
        int xOffset = 5; 
        int yOffset = 3;
        

        int displayRangeLevels = rangeUpgradeLevel - 2 * towerType;
        int displaySpeedLevels = speedUpgradeLevel - towerType;
        int displayDamageLevels = damageUpgradeLevel - 2 * towerType;
    
        
        if (speedUpgradeLevel > 0) {
            p.noFill();
            p.stroke(61, 196, 245);
            p.strokeWeight(displaySpeedLevels); 
            p.rect(towerCenterX - App.CELLSIZE / 2, towerCenterY - App.CELLSIZE / 2, App.CELLSIZE, App.CELLSIZE);
            p.strokeWeight(1);
            p.stroke(0); 
        }
    
        
        p.fill(240, 70, 220); 
        for (int i = 0; i < Math.max(0, displayRangeLevels); i++) {
            p.textSize(10);
            p.text('O', towerBaseX + xOffset + i * (upgradeSymbolSize + spacing), y * App.CELLSIZE + App.TOPBAR + upgradeSymbolSize - yOffset);
        }
    
        
        for (int i = 0; i < Math.max(0, displayDamageLevels); i++) {
            p.text('X', towerBaseX + xOffset + i * (upgradeSymbolSize + spacing), (y + 1) * App.CELLSIZE + App.TOPBAR - upgradeSymbolSize - yOffset + 2);
        }
    }

    @Override
    public void draw(PApplet p) {
        
        if (rangeUpgradeLevel >= 2 && speedUpgradeLevel >= 2 && damageUpgradeLevel >= 2) {
            p.image(tower2Img, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR, App.CELLSIZE, App.CELLSIZE);
            towerType = 2;
        } else if (rangeUpgradeLevel >= 1 && speedUpgradeLevel >= 1 && damageUpgradeLevel >= 1) {
            p.image(tower1Img, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR, App.CELLSIZE, App.CELLSIZE);
            towerType = 1;
        } else {
            p.image(tower0Img, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR, App.CELLSIZE, App.CELLSIZE);
            towerType = 0;
        }

        if (isMouseOver(p)) {
            p.noFill();
            p.stroke(255, 255, 0); 
            p.ellipse(x * App.CELLSIZE + App.CELLSIZE / 2, y * App.CELLSIZE + App.CELLSIZE / 2 + App.TOPBAR, range * 2, range * 2);
            p.stroke(0); 
            if (App.upgradeDesiredRange || App.upgradeDesiredSpeed || App.upgradeDesiredDamage) {
               drawUpgradeCosts(p);
            }
            
        }
        if (rangeUpgrade) {
            rangeUpgrade = false;
        } if (speedUpgrade) {
            speedUpgrade = false;
        } if (damageUpgrade) {
            damageUpgrade = false;
        }
        drawUpgradeIndications(p);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
