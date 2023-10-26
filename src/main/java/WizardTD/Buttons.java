package WizardTD;

import processing.core.PApplet;
import WizardTD.ButtonFunctions.ManaState;

public class Buttons {
    //private PApplet p;
    private int x, y, width, height;
    private String label;
    private String innerLabel;
    private boolean isActive = false;

    
    public Buttons(int x, int y, int sideLength, String label, String innerLabel) {
        this.x = x;
        this.y = y;
        this.width = sideLength;
        this.height = sideLength;
        this.label = label;
        this.innerLabel = innerLabel;
    }   


    public void toggleActive() {
        this.isActive = !this.isActive;
    }


    /*public void toggleFastForward() {
        // Implement the logic for toggling fast forward here
        this.toggleActive();
    }


    public void togglePause() {
        // Implement the logic for toggling pause here
        this.toggleActive();
    }


    public void toggleBuildTowerMode() {
        // Implement the logic for toggling build tower mode here
        this.toggleActive();
    }


    public void toggleUpgradeRange() {
        // Implement the logic for toggling upgrade range here
        this.toggleActive();
    }


    public void toggleUpgradeSpeed() {
        // Implement the logic for toggling upgrade speed here
        this.toggleActive();
    }


    public void toggleUpgradeDamage() {
        // Implement the logic for toggling upgrade damage here
        this.toggleActive();
    }


    public void activateManaPoolSpell(ManaState manaState) {
        if (manaState.currentMana >= manaState.manaPoolSpellInitialCost) {
            manaState.currentMana -= manaState.manaPoolSpellInitialCost;
            manaState.manaPoolSpellInitialCost += ManaState.manaPoolSpellCostInc;

            manaState.manaCap *= ManaState.manaPoolSpellCapMult;
            manaState.manaRegenRate += manaState.manaRegenRate * ManaState.manaPoolSpellManaGainedMult;
            
            this.toggleActive();
        }
    }*/


    public void display(PApplet p) {
        p.stroke(0);
        p.strokeWeight(3);
        if (isActive) {
            p.fill(255, 255, 0); // Yellow color for active state
        } else {
            p.fill(150, 108, 51); // Original color
        }
        p.rect(x, y, width, height);
        p.strokeWeight(1);
        
        p.textSize(24);  // Larger font for inner label
        p.textAlign(PApplet.CENTER, PApplet.CENTER);
        p.fill(0);
        p.text(innerLabel, x + width/2, y + height/2);
        
        p.textSize(12);  // Regular font for label to the right
        p.textAlign(PApplet.LEFT, PApplet.CENTER);
        
        String[] labelLines = label.split(" ");
        for (int i = 0; i < labelLines.length; i++) {
            p.text(labelLines[i], x + width + 5, y + height/2 - (labelLines.length - 1) * 6 + i * 12);
        }
    }
    

    public boolean isClicked(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}




