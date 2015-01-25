package controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import model.Player;
import model.Point;
import org.apache.log4j.Logger;

/**
 * @author Lukasz
 */
public class Game implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Game.class);

    private static final double ROTATE = 0.25;
    private static final Random RANDOM = new Random();

    private final int width;
    private final int height;

    private boolean running = true;

    private long lastTime;
    private final List<Player> players = new CopyOnWriteArrayList<>();

    public Game(int width, int height) {
        this.width = width;
        this.height = height;
        lastTime = System.nanoTime();
        addPlayer();
    }

    @Override
    public void run() {
        while (running) {
            long newTime = System.nanoTime();
            float deltaTime = 1.0E-08f * (newTime - lastTime);
            lastTime = newTime;
            for (Player player : players) {
                Point lastPosition = player.getLastPosition();

                double x = player.getDirection().getX() * deltaTime;
                double y = player.getDirection().getY() * deltaTime;
                Point newPosition = lastPosition.translatedPoint(x, y);

                if (collision(newPosition)) {
                    players.remove(player);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("gracz przegral (" + player + ")");
                    }
                } else {
                    player.addPosition(newPosition);
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.error(e, e);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("koniec gry");
        }
    }

    private boolean collision(Point position) {
        synchronized (players) {
            double x = position.getX();
            double y = position.getY();
            if (x < 0 || x >= width || y < 0 || y >= height) {
                return true;
            }

            Collection<Point> surrounding = createSurrounding(x, y);
            for (Player player : players) {
                for (Point point : player.getPositions()) {
                    for (Point position1 : surrounding) {
                        if (position1.equals(point)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    private Collection<Point> createSurrounding(double x, double y) {
        Collection<Point> surrounding = new ArrayList<>(5);
        surrounding.add(new Point(x, y));
        if (x > 1) {
            surrounding.add(new Point(x - 1, y));
        }
        if (x < width - 1) {
            surrounding.add(new Point(x + 1, y));
        }
        if (y > 1) {
            surrounding.add(new Point(x, y - 1));
        }
        if (y < height - 1) {
            surrounding.add(new Point(x, y + 1));
        }
        return surrounding;
    }

    @Override
    protected void finalize() throws Throwable {
        running = false;
        super.finalize();
    }

    public final void addPlayer() {
        synchronized (players) {
            double angle = Math.toRadians(RANDOM.nextInt(360));
            double s = StrictMath.sin(angle);
            double c = StrictMath.cos(angle);
            Point direction = new Point(s, c);
            Point position = new Point(RANDOM.nextInt(width), RANDOM.nextInt(height));
            players.add(new Player(direction, position));
        }
    }

    public List<Player> getPlayers() {
        synchronized (players) {
            return players;
        }
    }

    public void rotateLeft(int i) {
        synchronized (players) {
            players.get(i).getDirection().rotate(ROTATE);
        }
    }

    public void rotateRight(int i) {
        synchronized (players) {
            players.get(i).getDirection().rotate(-ROTATE);
        }
    }
}
