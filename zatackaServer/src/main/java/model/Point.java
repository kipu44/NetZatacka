package model;

import java.util.Locale;
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

    public synchronized Point translatedPoint(double dx, double dy) {
        return new Point(x + dx, y + dy);
    }

    public synchronized void rotate(double angle) {
        double s = StrictMath.sin(angle);
        double c = StrictMath.cos(angle);

        double newX = x * c - y * s;
        double newY = x * s + y * c;

        x = newX;
        y = newY;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "Point{x=%.3f, y=%.3f}", x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Point)) {
            return false;
        }

        Point point = (Point) obj;

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
        return x;
    }

    public synchronized double getY() {
        return y;
    }
}
