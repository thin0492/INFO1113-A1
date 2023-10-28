package WizardTD;


import processing.core.PApplet;
import processing.core.PImage;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.*;

import WizardTD.Interfaces.Drawable;

public class Board implements Drawable{
    private App app;
    private char[][] layout;
    private PImage grassImg, shrub0Img, shrub1Img, shrub2Img, shrub3Img, shrub4Img, wizardHouseImg, 
                path0Img, path1Img, path2Img, path3Img;
    private int[][] shrubTypes;
    private PImage[][] preprocessedPaths;
    private int CELLSIZE = App.CELLSIZE;
    private int BOARD_WIDTH = App.BOARD_WIDTH;
    private int BOARD_HEIGHT = App.BOARD_HEIGHT;
    private int TOPBAR = App.TOPBAR;

    public Board(App app, char[][] layout) {
        this.app = app;
        this.layout = layout;
    }

    public void setup() {
        grassImg = app.loadImage("src/main/resources/WizardTD/grass.png");
        shrub0Img = app.loadImage("src/main/resources/WizardTD/shrub0.png");
        shrub1Img = app.loadImage("src/main/resources/WizardTD/shrub1.png");
        shrub2Img = app.loadImage("src/main/resources/WizardTD/shrub2.png");
        shrub3Img = app.loadImage("src/main/resources/WizardTD/shrub3.png");
        shrub4Img = app.loadImage("src/main/resources/WizardTD/shrub4.png");
        wizardHouseImg = app.loadImage("src/main/resources/WizardTD/wizard_house.png");
        path0Img = app.loadImage("src/main/resources/WizardTD/path0.png");
        path1Img = app.loadImage("src/main/resources/WizardTD/path1.png");
        path2Img = app.loadImage("src/main/resources/WizardTD/path2.png");
        path3Img = app.loadImage("src/main/resources/WizardTD/path3.png");
        shrubTypes = new int[BOARD_HEIGHT][BOARD_WIDTH];

        preprocessedPaths = new PImage[layout.length][layout[0].length];
        determinePaths();

        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (layout[y][x] == 'S') {
                    shrubTypes[y][x] = (int) app.random(0, 5);
                }
            }
        }
    }

    // Method to draw the board
    @Override
    public void draw(PApplet app) {
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                switch (layout[y][x]) {
                    case ' ':
                        app.image(grassImg, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                        break;
                    case 'X':
                        PImage rotatedPathImg = preprocessedPaths[y][x];
                        app.image(rotatedPathImg, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                        break;
                    case 'S':
                        app.image(grassImg, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                        break;
                    case 'W':
                        app.image(grassImg, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                        break;
                }
            }
        }

        // Drawing the wizard's house (drawing it separately to ensure it's on top of other tiles)
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                if (layout[y][x] == 'S') {
                    switch (shrubTypes[y][x]) {
                        case 0:
                            app.image(shrub0Img, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                            break;
                        case 1:
                            app.image(shrub1Img, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                            break;
                        case 2:
                            app.image(shrub2Img, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                            break;
                        case 3:
                            app.image(shrub3Img, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                            break;
                        case 4:
                            app.image(shrub4Img, x * CELLSIZE, y * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                            break;
                    }
                }
                
                else if (layout[y][x] == 'W') {
                    // Center the 48x48 image within the 32x32 tile
                    int xOffset = (CELLSIZE - 48) / 2;
                    int yOffset = (CELLSIZE - 48) / 2;
                    app.image(wizardHouseImg, x * CELLSIZE + xOffset, y * CELLSIZE + TOPBAR + yOffset, 48, 48);
                }
            }
        }
    }
    
    private void determinePaths() {
        PImage image = null;
        float angle = 0;
        for (int i = 0; i < layout.length; i++) {
            for (int j = 0; j < layout[i].length; j++) {
                if (layout[i][j] == 'X') {
                    List<String> directions = new ArrayList<>();
                    if (i > 0 && layout[i-1][j] == 'X') directions.add("above");
                    if (i < layout.length-1 && layout[i+1][j] == 'X') directions.add("below");
                    if (j > 0 && layout[i][j-1] == 'X') directions.add("left");
                    if (j < layout[i].length-1 && layout[i][j+1] == 'X') directions.add("right");
                    if (directions.size() == 1) {
                        image = path0Img;
                        if (directions.contains("right") || directions.contains("left")) {
                            angle = 0;
                        } else if (directions.contains("above") || directions.contains("below")) {
                            angle = 90;
                        }
                    } else if (directions.size() == 2) {
                        if (directions.contains("above") && directions.contains("below")) {
                            image = path0Img;
                            angle = 90;
                        } else if (directions.contains("left") && directions.contains("right")) {
                            image = path0Img;
                            angle = 0;
                        } else if (directions.contains("left") && directions.contains("below")) {
                            image = path1Img;
                            angle = 0;
                        } else if (directions.contains("below") && directions.contains("right")) {
                            image = path1Img;
                            angle = 270;
                        } else if (directions.contains("right") && directions.contains("above")) {
                            image = path1Img;
                            angle = 180;
                        } else if (directions.contains("above") && directions.contains("left")) {
                            image = path1Img;
                            angle = 90;
                        }
                    } else if (directions.size() == 3) {
                        if (!directions.contains("above")) {
                            image = path2Img;
                            angle = 0;
                        } else if (!directions.contains("right")) {
                            image = path2Img;
                            angle = 90;
                        } else if (!directions.contains("below")) {
                            image = path2Img;
                            angle = 180;
                        } else if (!directions.contains("left")) {
                            image = path2Img;
                            angle = 270;
                        }
                    } else if (directions.size() == 4) {
                        image = path3Img;
                        angle = 0;
                    }
                    preprocessedPaths[i][j] = rotateImageByDegrees(image, angle);
                    
                }
            }
        }
    }

    /**
     * Source: https://stackoverflow.com/questions/37758061/rotate-a-buffered-image-in-java
     * @param pimg The image to be rotated
     * @param angle between 0 and 360 degrees
     * @return the new rotated image
     */
    public PImage rotateImageByDegrees(PImage pimg, double angle) {
        BufferedImage img = (BufferedImage) pimg.getNative();
        double rads = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(rads)), cos = Math.abs(Math.cos(rads));
        int w = img.getWidth();
        int h = img.getHeight();
        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        PImage result = app.createImage(newWidth, newHeight, PApplet.RGB);
        BufferedImage rotated = (BufferedImage) result.getNative();
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        int x = w / 2;
        int y = h / 2;

        at.rotate(rads, x, y);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
        for (int i = 0; i < newWidth; i++) {
            for (int j = 0; j < newHeight; j++) {
                result.set(i, j, rotated.getRGB(i, j));
            }
        }

        return result;
    }
}

