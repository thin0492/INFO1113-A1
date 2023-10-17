package WizardTD;

import processing.core.PApplet;

public class Buttons {
    //private PApplet p;
    private int x, y, width, height;
    private String label;
    private String innerLabel;
    
    public Buttons(int x, int y, int sideLength, String label, String innerLabel) {
        this.x = x;
        this.y = y;
        this.width = sideLength;
        this.height = sideLength;
        this.label = label;
        this.innerLabel = innerLabel;
    }
    
    public void display(PApplet p) {
        p.stroke(0);
        p.strokeWeight(3);
        p.fill(255);
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




