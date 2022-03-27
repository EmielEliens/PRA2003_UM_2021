package main;
import game.Game;
import game.Status;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * GameView class
 */
public class GameView extends JPanel implements KeyListener {
    private final Game game;
    private int rows;
    private int cols;
    private Timer keyTimer = null;
    private int keyFlags = 0;
    ActionListener keyTimerAction;

    /**
     * New constructor so that the game can be accessed in this class
     */
    public GameView( game.State state, Game game) {

        this.game = game;
        this.rows = state.rows();
        this.cols = state.cols();

        final GridLayout layout = new GridLayout(rows, cols,0,0);
        setLayout(layout);
        state.map();
        this.keyTimerAction = e -> {
            keyTimer.stop();
            keyTimer = null;
            game.apply(translateToEnum(keyFlags));
            keyFlags = 0;
            repaint();
        };
        addKeyListener(this);
        setFocusable(true);
    }

    /**
     * load images from project
     */
    private final BufferedImage[] images = new BufferedImage[5];
    public void LoadImages() throws IOException {
        try {
            images[0] = ImageIO.read(new File("Chell.png"));
            images[1] = ImageIO.read(new File("Wheatley.jpg"));
            images[2] = ImageIO.read(new File("glados.jpg"));
            images[3] = ImageIO.read(new File("heap.png"));
            images[4] = ImageIO.read(new File("bones.png"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * paint the graphics into the GUI
     */
    public void paint(Graphics g) {
        final Color gridLight = new Color(240, 240, 240);
        final Color gridDark = new Color(190, 190, 190);
        try {
            LoadImages();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //take the parameters from the game state
        this.rows = game.state().rows();
        this.cols= game.state().cols();
        //new Graphics2D object
        Graphics2D g2dGrid = (Graphics2D) g;
        g2dGrid.setPaint(Color.white);
        //takes parameters from Android class for the GUI height and width
        int width = getWidth();
        int height = getHeight();
        g2dGrid.fillRect(0, 0, width, height);
        int u;
        //take smallest one for unit pixel size, for height leave 20 pixels room for stats
        if ((width / (this.cols + 1))<((height - 20) / (this.rows + 1))){u=(width/(this.cols+1));
        }else {u=((height-20)/(this.rows+1));}
        int x0 = (width - u * this.cols) / 2;
        int y0 = (height - 20 - u * this.rows) / 2;
        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < this.cols; col++) {

                final int x = x0 + u * col;
                final int y = y0 + u * row;

                g2dGrid.setPaint(gridDark);
                g2dGrid.fillRect(x + 1, y + 1, u -1, u - 1);

                g2dGrid.setPaint(gridLight);
                g2dGrid.fillRect(x + 1, y + 1, u - 2, u - 2);

                g2dGrid.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2dGrid.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
            }
        }
        //load the images for the entities, I used the paint method from the GUI lab task to create the switch statements
        for (int row = 0; row < this.rows; row++) {
            for (int col = 0; col < this.cols; col++) {
                BufferedImage image = switch (game.state().valueAt(row, col)) {
                    case 1 -> images[0];
                    case 2 -> images[1];
                    case 3 -> images[2];
                    case 4 -> images[3];
                    case 5 -> images[4];
                    default -> null;
                };
                if (image != null) {
                    final int x = x0 + col * u;
                    final int y = y0 +row * u;

                    g.drawImage
                            (
                                    image, x, y, x + u, y + u, 0, 0,
                                    image.getWidth(), image.getHeight(), null
                            );
                }
            }
        }
        GameInfo(g2dGrid);
    }

    /**
     * method that prints the relevant game stats at the bottom of the screen
     */
    public void GameInfo(Graphics2D g2dGrid){
        g2dGrid.setPaint(new Color(150, 150, 150));
        Font font = new Font("plain", Font.PLAIN, 18);
        g2dGrid.setFont(font);
        //takes margin for the size of the text block below the game
        int Margin=getHeight()-15;
        String info = "level " + game.level();
        //divide by game length to make the position of the stats scalable
        g2dGrid.drawString(info, getWidth()/20, Margin);
        info = " score " + game.state().score();
        g2dGrid.drawString(info,getWidth()/8 , Margin);
        info =(game.state().blaster()) ? "[Blaster]" : "";
        g2dGrid.drawString(info, getWidth()-getWidth()/3, Margin);
        info= "safe jumps " + game.state().safe();
        g2dGrid.drawString(info, getWidth()-getWidth()/5 , Margin);
    }

    /**
     * used the same switch statement for the action flags
     */
    int actionFlag(int keyCode)
    {
        return switch (keyCode) {
            case KeyEvent.VK_LEFT -> 1;
            case KeyEvent.VK_UP -> 2;
            case KeyEvent.VK_RIGHT -> 4;
            case KeyEvent.VK_DOWN -> 8;
            case KeyEvent.VK_SPACE -> 16;
            case KeyEvent.VK_T -> 32;
            case KeyEvent.VK_B -> 64;
            case KeyEvent.VK_L, KeyEvent.VK_S -> 128;
            default -> 0;
        };
    }

    /**
     *checks whether keyboard input is Arrowkey
     */
    boolean checkArrowKey(int keyCode){
        if (keyCode == KeyEvent.VK_LEFT){
            return true;
        }
        else if (keyCode == KeyEvent.VK_UP){
            return true;
        }
        else if (keyCode == KeyEvent.VK_RIGHT){
            return true;
        }
        else if (keyCode == KeyEvent.VK_DOWN){
            return true;
        }
        return false;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    /**
     *method to translate flags to enums, used the same switch statements again
     * added arrow key flags together to create the diagonal movements
     */
    public main.Action translateToEnum (int flag) {
        return switch (flag) {
            case 1 -> main.Action.L;
            case 2 -> main.Action.U;
            case 3 -> main.Action.UL;
            case 4 -> main.Action.R;
            case 6 -> main.Action.UR;
            case 8 -> main.Action.D;
            case 9 -> main.Action.DL;
            case 12 -> main.Action.DR;
            case 16 -> main.Action.Pass;
            case 32 -> main.Action.Teleport;
            case 64 -> main.Action.Blast;
            case 128 -> main.Action.LastStand;
            default -> null;
        };
    }


    /**
     *check which key is pressed and status the game is at.
     * applies action if status is active
     * if status is dead => start new game
     * if status is cleared => call for next level
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (this.game.state().status() == Status.Dead) {
            System.out.println("you died");
            this.game.level(0);
            this.game.state(null);
            this.game.NextLevel();
            repaint();
        }
        else if (this.game.state().status() == Status.Cleared) {
            this.game.NextLevel();
            repaint();
        }
        else {
            int flag = actionFlag(keyCode);
            if (flag != 0) {
                if (checkArrowKey(keyCode)) {
                    keyFlags |= flag;
                    if (keyTimer == null) {
                        keyTimer = new Timer(50, keyTimerAction);
                        keyTimer.start();
                    }
                } else {
                    this.game.apply(translateToEnum(flag));
                    repaint();
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
