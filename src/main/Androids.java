package main;
import game.BadFileFormatException;
import game.Game;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * PRA2003 final assignment
 * author: Emiel Eliens
 * i6171475
 */

/**
 * i wanted to make a method that builds the GUI from gameView and run that via an instance of the androids class, but couldn't get it to work properly
 * therefore, i decided to just plug the code for the GUI in the main method
 * I know this is not pretty, but it was the only way I could get the code to work for now
 */
public class Androids {
    //create a new game and print it
    public static void main(String[] args) throws IOException, BadFileFormatException {
        Game game = new Game();
        game.load();
       GameView gameView = new GameView(game.state(),game);
       JFrame frame = new JFrame("Portal: Escape from Wheatley and GlaDOS");
       frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
       frame.setPreferredSize(new Dimension(900,600));
       frame.getContentPane().add(gameView);
       frame.pack();
       frame.setLocationRelativeTo(null);
       frame.setVisible(true);

       Runtime.getRuntime().addShutdownHook   (new Thread()
        {public void run(){
            try {
                game.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        }
        );
    }
}
