/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author Admin
 */
public class Game implements Runnable{

    private final int SIZE_X = 100;
    private final int SIZE_Y = 100;    
    private final double ROTATE = 0.05f;
    private final Random RANDOM = new Random();
    private long lastTime;
    private ArrayList<Point> positions = new ArrayList<>();
    private ArrayList<Point> directions = new ArrayList<>();
    
    public Game() {
        lastTime = System.nanoTime();
        positions.add(new Point(RANDOM.nextInt() % SIZE_X, RANDOM.nextInt() % SIZE_Y));
        int angle = RANDOM.nextInt();
        directions.add(new Point(Math.sin(angle), Math.cos(angle)));
    } 
    
    @Override
    public void run() {
        
        while (true) {
            long newTime = System.nanoTime();
            float deltaTime = (float)(newTime - lastTime) / 10000000.0f;
            lastTime = newTime;
            
            for (int i = 0; i < positions.size(); i++) {
                positions.get(i).translate(directions.get(i).getX() * deltaTime, directions.get(i).getY() * deltaTime);
            }
        }
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
