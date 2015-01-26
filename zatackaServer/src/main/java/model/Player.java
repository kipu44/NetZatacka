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
    private final boolean[][] visited;
    private boolean alive;

    public Player(Point direction, Point position, int l1, int l2) {
        this.direction = direction;
        positions = new ArrayList<>(l1 + l2);
        positions.add(position);
        visited = new boolean[l1][l2];
        for (int i = 0; i < l1; i++) {
            visited[i][0] = true;
            visited[i][l2 - 1] = true;
        }
        for (int i = 0; i < l2; i++) {
            visited[0][i] = true;
            visited[l1 - 1][i] = true;
        }
        alive = true;
    }

    @Override
    public String toString() {
        return "Player{" +
                "direction=" + direction +
                ", positions=" + getLastPosition() +
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
        int x = (int) position.getX();
        int y = (int) position.getY();
        visited[x][y] = true;
        visited[x + 1][y] = true;
        visited[x][y + 1] = true;
        if (x > 1) {
            visited[x - 1][y] = true;
        }
        if (y > 1) {
            visited[x][y - 1] = true;
        }
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

    public boolean[][] getVisited() {
        return visited;
    }
}
