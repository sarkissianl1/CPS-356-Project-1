/*
 * Authors: Quinnie, Liana, Pretti, Juliane
 * 
 * Assignments: CPS 356 - Operating Systems Multi Threated Racing Game
 * 
 * Date: November 3rd, 2025
 */

 //Import all necessary Java libraries 
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Random;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.Timer;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


/**
 * drawing
 *
 * Swing JPanel that displays the racing game UI and controls the race lifecycle.
 *
 * Car instances run on separate threads and interact with drawing through
 * synchronized methods 
 */
public class drawing extends JPanel implements ActionListener {
    private int WIDTH = 1000;
    private int HEIGHT = 500;
    Timer t;
    
    private Rectangle recCar1;
    private Rectangle recCar2;
    private Car car1;
    private Car car2;

    private final List<Rectangle> obstaclesLane1;
    private final List<Rectangle> obstaclesLane2;
    
    private final List<Rectangle> powerUpsLane1;
    private final List<Rectangle> powerUpsLane2;

    // Control buttons
    private JButton startButton;
    private JButton stopButton;
    private JButton pauseButton;
    private JButton resumeButton;
    private JButton startWithObstacles;
    private boolean nextStartWithObstacles = false;
    
    // Thread references
    private Thread car1Thread;
    private Thread car2Thread;
    
    // Race state
    private boolean raceStarted = false;
    private static volatile boolean racePaused = false;
    public static final Object pauseLock = new Object();

    /**
     * Initialize the drawing panel, timer, UI buttons and empty synchronized
     * collections for obstacles and power-ups. 
     */
    public drawing() {
        setLayout(null);
        t = new Timer(10, this);
        
        // Initializing two rectangles for two cars
        recCar1 = new Rectangle(0, 50, 100, 100);
        recCar2 = new Rectangle(0, 320, 100, 100);

        obstaclesLane1 = Collections.synchronizedList(new ArrayList<>());
        obstaclesLane2 = Collections.synchronizedList(new ArrayList<>());
        powerUpsLane1 = Collections.synchronizedList(new ArrayList<>());
        powerUpsLane2 = Collections.synchronizedList(new ArrayList<>());

        initializeButtons();
        initializePowerUps();
        
    }
    
    /**
     * Populate the synchronized obstacle lists with randomly placed obstacles.
     */
    private void initializeObstacles() {
        // clear existing obstacles to reuse the same list objects
        obstaclesLane1.clear();
        obstaclesLane2.clear();

        Random rand = new Random();

        // choose between 1 and 4 obstacles (inclusive)
        int numObstacles = 1 + rand.nextInt(4);

        for (int i = 0; i < numObstacles; i++) {
            int x1 = 150 + i * 150 + rand.nextInt(50);
            obstaclesLane1.add(new Rectangle(x1, 75, 40, 40));

            int x2 = 200 + i * 150 + rand.nextInt(50);
            obstaclesLane2.add(new Rectangle(x2, 345, 40, 40));
        }
    }
    /**
     * Populate the synchronized power-up lists with randomly placed items.
     */
    private void initializePowerUps() {
        powerUpsLane1.clear();
        powerUpsLane2.clear();

        Random rand = new Random();

        int numPowerUps = 2 + rand.nextInt(2);

        for (int i = 0; i < numPowerUps; i++) {
            int x1 = 250 + i * 200 + rand.nextInt(60);
            powerUpsLane1.add(new Rectangle(x1, 80, 30, 30));

            int x2 = 300 + i * 200 + rand.nextInt(60);
            powerUpsLane2.add(new Rectangle(x2, 350, 30, 30));

        }

    }
    /**
     * Create and initialize buttons for controlling the race
     */
    private void initializeButtons() {
        startButton = new JButton("Start Race");
        startButton.setBounds(50, 420, 120, 40);
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startRace();
            }
        });
        add(startButton);
        
        stopButton = new JButton("Stop Race");
        stopButton.setBounds(200, 420, 120, 40);
        stopButton.setEnabled(false);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopRace();
            }
        });
        add(stopButton);
        
        pauseButton = new JButton("Pause");
        pauseButton.setBounds(350, 420, 120, 40);
        pauseButton.setEnabled(false);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseRace();
            }
        });
        add(pauseButton);
      
        resumeButton = new JButton("Resume");
        resumeButton.setBounds(500, 420, 120, 40);
        resumeButton.setEnabled(false);
        resumeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resumeRace();
            }
        });
        add(resumeButton);
        
        startWithObstacles = new JButton("Start Obstacles");
        startWithObstacles.setBounds(650, 420, 140, 40);
        startWithObstacles.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // mark next start to include obstacles, then start
                nextStartWithObstacles = true;
                startRace();
            }
        });
        add(startWithObstacles);
    }   

    
    
    /**
     * Start a new race if one is not already running.
     */
    private void startRace() {
        if (!raceStarted) {

            racePaused = false;
            // initialize obstacles only if requested; otherwise start with none
            if (nextStartWithObstacles) {
                powerUpsLane1.clear();
                powerUpsLane2.clear();
                initializeObstacles();
                nextStartWithObstacles = false;
            } else {
                obstaclesLane1.clear();
                obstaclesLane2.clear();
                initializePowerUps();
            }

            // Create two car instances and their threads
            car1 = new Car('A', this);
            car2 = new Car('B', this);
            
            car1Thread = new Thread(car1);
            car2Thread = new Thread(car2);
            
            car1Thread.start();
            car2Thread.start();
            
            // Timer starts
            t.start();
            
            raceStarted = true;
            
            // Updating button states
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            pauseButton.setEnabled(true);
            resumeButton.setEnabled(false);
            
        }
    }
    
    /**
     * Stop the current race. This method returns the UI to its pre-race state.
     */
    private void stopRace() {
        if (t.isRunning()) {
            t.stop();
        }

        if (racePaused) {
            racePaused = false;
            synchronized (pauseLock) {
                pauseLock.notifyAll();
            }
        }

        if (car1Thread != null && car1Thread.isAlive()) {
            car1Thread.interrupt();
        }
        if (car2Thread != null && car2Thread.isAlive()) {
            car2Thread.interrupt();
        }
        
        // Resets the race state
        raceStarted = false;
        racePaused = false;
        
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        pauseButton.setEnabled(false);
        resumeButton.setEnabled(false);
       
        repaint();
    }

    /**
     * Pause the race
     */
    private void pauseRace() {
        if (!racePaused) {
            racePaused = true;
            t.stop();
            pauseButton.setEnabled(false);
            resumeButton.setEnabled(true);
        }
    }
  
    /**
     * Resume a previously paused race: notify waiting car threads and restart
     */
    private void resumeRace() {
        if (racePaused) {
            synchronized (pauseLock) {
                racePaused = false;
                pauseLock.notifyAll();
            }
            t.start();
            pauseButton.setEnabled(true);
            resumeButton.setEnabled(false);
        }
    }

    /*
     * Check if the race is paused; if so, wait until resumed.
     */
    public static void checkPauseState() {
        synchronized (pauseLock) {
            while (racePaused) {
                try {
                    pauseLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    /*
     * Check for collision between a car and obstacles.
     */
    public boolean checkCollision(int carNumber, int carPosition) {
        Rectangle carRect;
        List<Rectangle> obstacles;

        if (carNumber == 1) {
            carRect = new Rectangle(carPosition, recCar1.y, recCar1.width, recCar1.height);
            obstacles = obstaclesLane1;
        } else {
            carRect = new Rectangle(carPosition, recCar2.y, recCar2.width, recCar2.height);
            obstacles = obstaclesLane2;
        }

        synchronized (obstacles) {
            for (Rectangle obstacle : obstacles) {
                if (carRect.intersects(obstacle)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkPowerUp(int carNumber, int carPosition) {
        Rectangle carRect;
        List<Rectangle> powerUps;

        if (carNumber == 1) {
            carRect = new Rectangle(carPosition, recCar1.y, recCar1.width, recCar1.height);
            powerUps = powerUpsLane1;
        } else {
            carRect = new Rectangle(carPosition, recCar2.y, recCar2.width, recCar2.height);
            powerUps = powerUpsLane2;
        }

        synchronized (powerUps) {
            for (int i = powerUps.size() - 1; i >= 0; i--) {
                Rectangle powerUp = powerUps.get(i);
                if (carRect.intersects(powerUp)) {
                    powerUps.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.white);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.pink);
        g.fillRect(0, 0, WIDTH, HEIGHT/2);
       
        g.setColor(Color.RED);
        g.drawLine(WIDTH - 200, 0, WIDTH - 200, HEIGHT);
        g.drawString("FINISH LINE", WIDTH - 190, 20);

        g.setColor(Color.ORANGE);
        synchronized (obstaclesLane1) {
            for (Rectangle obs : obstaclesLane1) {
                 g.fillRect(obs.x, obs.y, obs.width, obs.height);

                g.setColor(Color.BLACK);
                g.drawLine(obs.x, obs.y, obs.x + obs.width, obs.y + obs.height);
                g.drawLine(obs.x + obs.width, obs.y,obs.x, obs.y + obs.height);
                g.setColor(Color.ORANGE);
            }
        }

         g.setColor(Color.DARK_GRAY);
         synchronized (obstaclesLane2) {
             for (Rectangle obsRectangle : obstaclesLane2) {
                 g.fillRect(obsRectangle.x, obsRectangle.y, obsRectangle.width, obsRectangle.height);

                g.setColor(Color.BLACK);
                g.drawLine(obsRectangle.x, obsRectangle.y, obsRectangle.x + obsRectangle.width, obsRectangle.y + obsRectangle.height);
                g.drawLine(obsRectangle.x + obsRectangle.width, obsRectangle.y,obsRectangle.x, obsRectangle.y + obsRectangle.height);
                g.setColor(Color.DARK_GRAY);
             }
         }

         
        synchronized (powerUpsLane1) {
            for (Rectangle powerUp : powerUpsLane1) {
                drawPowerUp(g, powerUp.x, powerUp.y);
            }
        }

        synchronized (powerUpsLane2) {
            for (Rectangle powerUp : powerUpsLane2) {
                drawPowerUp(g, powerUp.x, powerUp.y);
            }
        }
        
        
        if (raceStarted && car1 != null && car2 != null) {
            
            recCar1.x = car1.getPosition();
            recCar2.x = car2.getPosition();

            drawCar(g, recCar1.x, recCar1.y, Color.LIGHT_GRAY, Color.DARK_GRAY);
            drawCar(g, recCar2.x, recCar2.y, Color.RED, Color.DARK_GRAY);
            
            g.setColor(Color.BLACK);
            g.setFont(g.getFont().deriveFont(16F).deriveFont(java.awt.Font.BOLD));
            g.drawString(String.valueOf(car1.getCarName()), recCar1.x + 45, recCar1.y +55);
            g.drawString(String.valueOf(car2.getCarName()), recCar2.x + 45, recCar2.y +55);
            
            
            if (racePaused) {
                g.setColor(Color.BLACK);
                g.setFont(g.getFont().deriveFont(30f));
                g.drawString("PAUSED", WIDTH/2 - 60, HEIGHT/2);
            }
        } else {
            g.setColor(Color.BLACK);
            g.setFont(g.getFont().deriveFont(24f));
            g.drawString("Press 'Start Race' to begin!", WIDTH/2 - 150, HEIGHT/2);
            g.setFont(g.getFont().deriveFont(16F));
            g.drawString("Watch out for obstacles!", WIDTH/2 - 100, HEIGHT/2 +40);
            g.drawString("Collect power-ups for speed boost!", WIDTH/2 - 130, HEIGHT/2 +65);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
        if (raceStarted && Car.isRaceOver() && t.isRunning()) {
            t.stop();
            pauseButton.setEnabled(false);
            resumeButton.setEnabled(false);
            // Show winner on the Event Dispatch Thread so it appears reliably every race
            SwingUtilities.invokeLater(() -> {
                String winner = Car.getWinner();
                JOptionPane.showMessageDialog(
                    drawing.this,
                    " The winner is: " + winner,
                    "Race Results",
                    JOptionPane.INFORMATION_MESSAGE
                );
            });
        }
    }
    private void drawPowerUp(Graphics g, int x, int y) {
        g.setColor(Color.YELLOW);
        g.fillOval(x, y, 30, 30);

        g.setColor(Color.ORANGE);
        g.fillOval(x + 5, y + 5, 20, 20);

        g.setColor(Color.WHITE);
        int[] xPoints = {x + 15, x + 12, x + 18, x + 10, x + 16, x + 14, x + 20, x + 13};
        int[] yPoints = {y + 5, y + 12, y + 12, y + 20, y + 20, y + 25, x + 18, x + 12};
        g.fillPolygon(xPoints, yPoints, 8);

        g.setColor(Color.ORANGE.darker());
        g.drawOval(x, y, 30, 30);

    }



    public void drawCar(Graphics g, int x, int y, Color bodyColor, Color windowColor) {
        g.setColor(bodyColor);
        g.fillRoundRect(x + 10, y + 30, 80, 40, 10, 10);

        g.fillRoundRect(x, y + 40, 20, 25, 8, 8);

        g.setColor(windowColor);
        g.fillRect(x + 20, y + 35, 25, 20);
        g.fillRect(x + 55, y + 35, 25, 20);

        g.setColor(Color.BLACK);
        g.fillOval(x + 15, y + 65, 20, 20);
        g.fillOval(x + 65, y + 65, 20, 20);

        g.setColor(Color.GRAY);
        g.fillOval(x + 19, y + 69, 12, 12);
        g.fillOval(x + 69, y + 69, 12, 12);

        g.setColor(Color.YELLOW);
        g.fillOval(x + 2, y + 42, 8, 8);
        g.fillOval(x + 2, y + 55, 8, 8);

        g.setColor(Color.BLACK);
        g.drawRoundRect(x + 10, y + 30, 80, 40, 10, 10);
        g.drawRoundRect(x , y + 40, 20, 25, 8, 8);

    }

    public int getFinishLine() {
        return WIDTH - 200;
    }
}

    





