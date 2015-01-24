package model;

import org.apache.log4j.Logger;

/**
 * @author Lukasz
 */
public class Point {

    private static final Logger LOGGER = Logger.getLogger(Point.class);

    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point translatedPoint(double x, double y) {
        synchronized (this) {
            return new Point(this.x + x, this.y + y);
        }
    }

    public synchronized void rotate(double angle) {
        synchronized (this) {
            double s = StrictMath.sin(angle);
            double c = StrictMath.cos(angle);

            double x = this.x * c - this.y * s;
            double y = this.x * s + this.y * c;

            this.x = x;
            this.y = y;
        }
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Point)) {
            return false;
        }

        Point point = (Point) o;

        if (Double.compare(point.x, x) != 0) {
            return false;
        }
        if (Double.compare(point.y, y) != 0) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public synchronized double getX() {
        synchronized (this) {
            return x;
        }
    }

    public synchronized double getY() {
        synchronized (this) {
            return y;
        }
    }
}
