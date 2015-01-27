/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 *
 * @author Admin
 */
public class PredictionCalculator {
    
    private static final Logger LOGGER = Logger.getLogger(PredictionCalculator.class);
    
    private static final double SPEED = 25.0f;
    private static final double ROTATE = 0.5;
    
    private Point position;
    private final Point direction;
    private long lastTime;
    
    public PredictionCalculator(Point startPosition, Point startDirection) {
        this.position = startPosition;
        this.direction = startDirection;
    }
    
    public void initTime() {
        lastTime = System.nanoTime();
    }
    
    public void Update() {
        long newTime = System.nanoTime();
        double deltaTime = 1.0E-9 * (newTime - lastTime);
        
        double x = direction.getX() * deltaTime * SPEED;
        double y = direction.getY() * deltaTime * SPEED;
        Point newPosition = position.translatedPoint(x, y);
        lastTime = newTime;
        position = newPosition;
        
        LOGGER.debug("DeltaTime" + deltaTime);
        
    }

    public Point getPosition() {
        return position;
}

     public void setPosition(Point position) {
         this.position = position;
     }
     
    public void synchronizePosition(Point serverPosition) {
        //if (!position.equals(serverPosition)) {
        //   position = new Point(position.getX() + serverPosition.getX() / 2, position.getY() + serverPosition.getY() / 2);
        //}
        position = serverPosition;
    }
    
    public void rotateLeft() {
        direction.rotate(-ROTATE);
    }
    
    public void rotateRight() {
        direction.rotate(ROTATE);
    }
}