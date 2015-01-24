package controller;

import java.util.ArrayList;
import java.util.Random;
import model.Player;
import model.Point;
import org.apache.log4j.Logger;

/**
 * @author Lukasz
 */
public class Game implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Game.class);

    private final int width;
    private final int height;
    private final double ROTATE = 0.25f;
    private final Random RANDOM = new Random();

    private boolean running = true;

    private long lastTime;
    private final ArrayList<Player> players = new ArrayList<>();

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
            float deltaTime = (float) (newTime - lastTime) / 20000000.0f;
            lastTime = newTime;
            for (Player player : players) {
                Point lastPosition = player.getLastPosition();
                double x = player.getDirection().getX() * deltaTime;
                double y = player.getDirection().getY() * deltaTime;
                lastPosition.translate(x, y);
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("koniec gry");
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        running = false;
        super.finalize();
    }

    public final synchronized void addPlayer() {
        double angle = Math.toRadians(RANDOM.nextInt(360));
        Point direction = new Point(StrictMath.sin(angle), StrictMath.cos(angle));
        Point position = new Point(RANDOM.nextInt(width), RANDOM.nextInt(height));
        players.add(new Player(direction, position));
    }

    public ArrayList<Player> getPlayers() {
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
