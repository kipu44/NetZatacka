package controller;

import java.util.List;
import java.util.Locale;
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

    }

    @Override
    public void run() {

        lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double deltaTime = 1 / 120.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;
        while (running) {
            long newTime = System.nanoTime();
            delta += (newTime - lastTime) / ns;
            lastTime = newTime;

            if (delta > 1) {
                for (Player player : players) {

                    if (player.isAlive()) {
                        Point lastPosition = player.getLastPosition();

                        double x = player.getDirection().getX() * deltaTime;
                        double y = player.getDirection().getY() * deltaTime;
                        Point newPosition = lastPosition.translatedPoint(x, y);
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(String.format(Locale.ENGLISH,
                                                       "Delta: %3.3f,%3.3f DeltaTime: %3.3f Pozycja (%3.3f,%3.3f)",
                                                       x,
                                                       y,
                                                       deltaTime,
                                                       newPosition.getX(),
                                                       newPosition.getY()));
                        }

                        if (collision(newPosition)) {
                            player.setAlive(false);
                            if (collision(newPosition)) {
                                player.setAlive(false);
                                if (LOGGER.isDebugEnabled()) {
                                    LOGGER.debug("gracz przegral (" + player + ")");
                                }
                            } else {
                                player.addPosition(newPosition);
                            }
                        }
                    }
                }

//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                LOGGER.error(e, e);
//            }
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("koniec gry");
        }
//            throw new RuntimeException("koniec gry");
    }

    private boolean collision(Point position) {
        synchronized (players) {
            int x = (int) position.getX();
            int y = (int) position.getY();
            for (Player player : players) {
                boolean[][] visited = player.getVisited();
                if (visited[x][y]) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        running = false;
        super.finalize();
    }

    public final void addPlayer() {
        double angle = Math.toRadians(RANDOM.nextInt(360));
        double s = StrictMath.sin(angle);
        double c = StrictMath.cos(angle);
        Point direction = new Point(s, c);
        Point position = new Point(RANDOM.nextInt(width - 2) + 1, RANDOM.nextInt(height - 2) + 1);
        synchronized (players) {
            players.add(new Player(direction, position, height, width));
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
