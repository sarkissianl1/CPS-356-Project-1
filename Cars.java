/*
 * Authors: Quinnie, Liana, Pretti, Juliane
 * 
 * Assignments: CPS 356 - Operating Systems Multi Threated Racing Game
 * 
 * Date: November 3rd, 2025
 */

import javax.swing.JFrame;

/*
 * Class creates the main JFrame and adds the drawing to it which contains the UI and game.
 */
public class Cars extends JFrame {
    public static void main(String[] args) {
        //GUI creation 
        JFrame app = new JFrame();

        //drawing creation
        drawing w = new drawing();
        
        //Display configurations
        app.add(w);
        app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        app.setSize(1000, 520);
        app.setVisible(true);
        
    }
}