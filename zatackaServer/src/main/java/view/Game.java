/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.util.ArrayList;
import java.util.Random;
import org.apache.log4j.Logger;

/**
 *
 * @author Admin
 */
public class Game implements Runnable{

    public static final Logger LOGGER = Logger.getLogger(Game.class);
    
    private final int width;
    private final int height;    
    private final double ROTATE = 0.25f;
    private final Random RANDOM = new Random();
    private long lastTime;
    private final ArrayList<Point> positions = new ArrayList<>();
    private final ArrayList<Point> directions = new ArrayList<>();
    
    public Game(int width, int height) {
        this.width = width;
        this.height = height;
        lastTime = System.nanoTime();
        addPlayer();
    } 
    
    @Override
    public void run() {
        
        while (true) {
            long newTime = System.nanoTime();
            float deltaTime = (float)(newTime - lastTime) / 20000000.0f;
            lastTime = newTime;
            
            for (int i = 0; i < positions.size() && i < directions.size(); i++) {
                positions.get(i).translate(directions.get(i).getX() * deltaTime, directions.get(i).getY() * deltaTime);
            }
        }
    }
    
    public final void addPlayer() {
        positions.add(new Point(RANDOM.nextInt() % width, RANDOM.nextInt() % height));
        //positions.add(new Point(200, 200));
        int angle = RANDOM.nextInt();
        directions.add(new Point(Math.sin(angle), Math.cos(angle)));
    }

    public ArrayList<Point> getPositions() {
        return positions;
    }
    
    public void rotateLeft(int i) {
        directions.get(i).rotate(ROTATE);
    }
    
        public void rotateRight(int i) {
        directions.get(i).rotate(-ROTATE);
    }
    
    
}
