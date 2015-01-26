package model;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Created by rafal on 24.01.15.
 */
public class Player {

    private static final Logger LOGGER = Logger.getLogger(Player.class);

    private final Point direction;
    private final List<Point> positions;
    private boolean alive;

    public Player(Point direction, Point position, int l1, int l2) {
        this.direction = direction;
        positions = new ArrayList<>(l1 + l2);
        positions.add(position);
        alive = true;
    }

    @Override
    public String toString() {
        return "Player{" +
                "direction=" + direction +
                ", position=" + getLastPosition() +
                ", alive=" + alive +
                "}";
    }

    public Point getDirection() {
        return direction;
    }

    public Point getLastPosition() {
        return positions.get(positions.size() - 1);
    }

    public void addPosition(Point position) {
        positions.add(position);
    }

    public List<Point> getPositions() {
        return positions;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

}
