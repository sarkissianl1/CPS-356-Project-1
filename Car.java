/*
 * Authors: Quinnie, Liana, Pretti, Juliane
 * 
 * Assignments: CPS 356 - Operating Systems Multi Threated Racing Game
 * 
 * Date: November 3rd, 2025
 */


import java.util.Random;

/**
 * Class Car implements Runnable and represents a single car in the race. 
 * Each Car runs on its own thread inorder to go towards the finish line. 
 *
 * Threading and synchronization:
 * - {@code raceOver} is a static volatile flag shared between Car threads to
 *   indicate when the race has ended.
 * - {@code raceLockKey} is used to synchronize winner so exactly
 *   one thread sets the winner.
 */
public class Car implements Runnable {

    //Instance variables for the Car class
    private static volatile boolean raceOver = false; //votatile allows visibility across threads 
    private static String winner = "";
    private static final Object raceLockKey = new Object();

    private final char carName;
    private int position;
    private final Random random;
    private drawing w;
    private int carNumber;
    private boolean hasPowerUp;
    private long powerUpEndTime;
    
    /**
     * Create a new Car instance.
     *
     * @param carSymbol identifier for the car 
     * @param w reference to the drawing UI used for collision checks and finish line
     */
    public Car(char carSymbol, drawing w) {
        this.carName = carSymbol;
        this.w = w;
        this.position = 0;
        this.random = new Random();
        this.carNumber = (carSymbol == 'A') ? 1 : 2;
        this.hasPowerUp = false;
        this.powerUpEndTime = 0;
        resetRace();
    }

    /**
     * Reset shared race state. 
     * It is called before starting a new race
     */
    public static void resetRace() {
        synchronized (raceLockKey) {
            raceOver = false;
            winner = "";
        }
    }

    //
    /**
     * Method executed by the car thread. 
     * The carcollects power-ups and applies a temporary speed boost,
     * it checks for collisions , and when the finish line is reached, it sets the winner using a synchronized block   
     * so only one thread wins.
     */
    @Override
    public void run() {
        while (!raceOver && position < w.getFinishLine()) {
            drawing.checkPauseState();

            
            if (hasPowerUp && System.currentTimeMillis() > powerUpEndTime) {
                hasPowerUp = false;
            }

            int baseSpeed = random.nextInt(10) + 1;
            int speed = hasPowerUp ? baseSpeed * 3 : baseSpeed;
            int nextPosition = position + speed;

            if (w.checkPowerUp(carNumber, position)) {
                hasPowerUp = true;
                powerUpEndTime = System.currentTimeMillis() + 5000;
            }
            if(w.checkCollision(carNumber, nextPosition)) {
                position +=1;

                try{
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                position = nextPosition;
            }
            
            // If a car reaches the finish lines, it has the raceLockKey
            if (position >= w.getFinishLine()){
                /* 
                 * When a car has raceLockKey, other cars have to wait until
                 * the raceLockKey is released
                 * When a car has raceLockKey and the race is not over,
                 * assign raceOver to true and get the carName for winner
                 */
                synchronized (raceLockKey) {
                    if (!raceOver) {
                        raceOver = true;
                        winner = "Car " + carName;
                    }
                }
            }
            // Slow down the thread to see animation
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    /**
     * @return true if a car has been declared the winner
     */
    public static boolean isRaceOver() {
        return raceOver;
    }

    /**
     * @return winner description 
     */
    public static String getWinner() {
        return winner;
    }

    /**
     * @return current position of this car
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return car identifier character
     */
    public char getCarName() {
        return carName;
    }
}