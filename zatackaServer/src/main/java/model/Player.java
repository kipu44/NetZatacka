package model;

import java.util.ArrayList;

/**
 * Created by rafal on 24.01.15.
 */
public class Player {

    private final Point direction;
    private final ArrayList<Point> positions;

    public Player(Point direction, Point position) {
        this.direction = direction;
        positions = new ArrayList<>();
        positions.add(position);
    }

    public Point getDirection() {
        return direction;
    }

    public Point getLastPosition() {
        return positions.get(positions.size() - 1);
    }

    public ArrayList<Point> getPositions() {
        return positions;
    }

    public Point getPosition(int i) {
        return positions.get(i);
    }
}
