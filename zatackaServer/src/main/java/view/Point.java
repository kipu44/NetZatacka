package view;

/**
 *
 * @author Lukasz
 */
public class Point {

    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public synchronized void translate(double x, double y) {
        this.x += x;
        this.y += y;
    }
    
    public synchronized void rotate(double angle) {
        double s = Math.sin(angle);
        double c = Math.cos(angle);
        double x = this.x * c - this.y * s;
        double y = this.x * s + this.y * c;
        this.x = x;
        this.y = y;
    }

    public synchronized double getX() {
        return x;
    }

    public synchronized double getY() {
        return y;
    }

}
