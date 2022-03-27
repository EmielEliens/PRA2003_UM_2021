package game;
import main.Action;
import java.io.*;
public class Game {
    /**
     * set level, and create a new game state
     */
    private int level =0;

    private State state = null;
    private final String saveFileName = "savedLevel.txt";


    /**
     * method to create next level
     */
    public void NextLevel(){
        level++;
        int row = level+4;
        int col = ((level+4)*3+1)/2;
        int score = (this.state==null)? 0 :(this.state.score());
        int safe = (this.state==null)? 0 :(this.state.safe());

        this.state = new State(row,col,score,safe);
        System.out.println(toString());
    }

    public void apply(final Action action){


        this.state.apply(action);
        System.out.println(toString());
    }

    /**
     *accessors
     */
    public int level(){

        return this.level;
    }
    public void level(int level){
        this.level=level;
    }
    public State state(){
        return this.state;
    }
    public void state(State state){
        this.state=state;
    }
    public String SaveFileName(){ return this.saveFileName;}

    /**
     *load method
     */
    public void load() throws BadFileFormatException, IOException {
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(saveFileName)))) {
            String string = reader.readLine();
            level = Integer.parseInt(string);
            state = new State(level, reader);
            System.out.println(toString());

        }
        catch (BadFileFormatException e){
            System.out.println(e);
            level=0;
            state = null;
            NextLevel();
        } catch (IOException e){
            System.out.println("corrupted safe file");
            level=0;
            state = null;
            NextLevel();
        }
    }

    /**
     *save method
     */
    public void save() throws IOException {
        try(final PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(saveFileName, false)))){
            out.println(level);
            out.println(state.toString());

        }
        catch (IOException e){
            System.out.println("something went wrong during the saving process ");
            e.printStackTrace();
        }
    }


    /**
     * print level to string
     *
     */
    @Override
    public String toString(){
        System.out.println("level " + this.level);
        return this.state.toString();

    }



}