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

    private static final double SPEED = 32.0f;
    private static final double ROTATE = 0.5;
    private static final Random RANDOM = new Random();
    private static final double TICK = 1 / 30.0;

    private final int width;
    private final int height;

    private boolean running = true;

    private long lastTime;
    private final List<Player> players = new CopyOnWriteArrayList<>();
    private boolean[][] painted;

    public Game(int width, int height) {
        this.width = width;
        this.height = height;
        painted = new boolean[width][height];
        painted = new boolean[width][height];
        for (int i = 0; i < width; i++) {
            painted[i][height - 1] = true;
            painted[i][0] = true;
        }
        for (int i = 0; i < height; i++) {
            painted[width - 1][i] = true;
            painted[0][i] = true;
        }
    }

    @Override
    public void run() {

        lastTime = System.nanoTime();
        double delta = 0;

        while (running) {
            long newTime = System.nanoTime();
            float deltaTime = 1.0E-9f * (newTime - lastTime);
            delta += deltaTime;
            lastTime = newTime;

            if (delta > TICK) {
                for (Player player : players) {

                    if (player.isAlive()) {
                        Point lastPosition = player.getLastPosition();

                        double x = player.getDirection().getX() * delta * SPEED;
                        double y = player.getDirection().getY() * delta * SPEED;
                        Point newPosition = lastPosition.translatedPoint(x, y);

                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(String.format(Locale.ENGLISH,
                                    "Delta: %3.3f,%3.3f DeltaTime: %3.6f Pozycja (%3.3f,%3.3f)",
                                    x,
                                    y,
                                    delta,
                                    newPosition.getX(),
                                    newPosition.getY()));
                        }

                        if (collision(lastPosition, newPosition)) {
                            player.setAlive(false);
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("gracz przegral (" + player + ")");
                            }
                        } else {
                            player.addPosition(newPosition);
                        }
                    }
                }

                delta -= TICK;
            }
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("koniec gry");
        }
    }

    private boolean collision(Point lastPosition, Point position) {
        synchronized (players) {
            int x0 = (int) lastPosition.getX();
            int y0 = (int) lastPosition.getY();
            int x1 = (int) position.getX();
            int y1 = (int) position.getY();

            return !interpolate(x0, y0, x1, y1);
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
            players.get(i).getDirection().rotate(-ROTATE);
        }
    }

    public void rotateRight(int i) {
        synchronized (players) {
            players.get(i).getDirection().rotate(ROTATE);
        }
    }

    private boolean interpolate(int oldRow, int oldColumn, int row, int column) {
        
        boolean isSuccess;
        
        painted[oldRow][oldColumn] = false;
        
        int dx = Math.abs(row - oldRow), sx = oldRow < row ? 1 : -1;
        int dy = Math.abs(column - oldColumn), sy = oldColumn < column ? 1 : -1;
        int err = (dx > dy ? dx : -dy) / 2, e2;

        int x0 = oldRow;
        int y0 = oldColumn;

        for (;;) {
            
            if (painted[x0][y0]) {
                return false;
            } else {
                painted[x0][y0] = true;
            }
            
            if (x0 == row && y0 == column) {
                break;
            }
            e2 = err;
            if (e2 > -dx) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dy) {
                err += dx;
                y0 += sy;
            }
        }
        
        return true;
    }
}
