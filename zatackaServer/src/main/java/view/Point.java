/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

/**
 *
 * @author Admin
 */
public class Point {
    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void translate(double x, double y) {
        this.x += x;
        this.y += y;
    }
    
    public void rotate(double angle) {
        double s = Math.sin(angle);
        double c = Math.cos(angle);
        double x = this.x * c - this.y * s;
        double y = this.x * s + this.y * c;
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    
    
}
