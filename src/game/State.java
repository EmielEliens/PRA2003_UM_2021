package game;
import main.Action;
import main.RC;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class State {
    /**
     * game entities
     */
    public static final int Empty =0;
    public static final int Player=1;
    public static final int Robot1=2;
    public static final int Robot2=3;
    public static final int Heap=4;
    public static final int Dead=5;

    private final int rows;
    private final int cols;
    private final int[][] map;
    private int score = 0;
    private int safe = 0;
    private boolean blaster = false;
    private int robots = 0;
    private Status status=null;
    private RC playerAt=null;


    /**
     * constructor
     */
    public State
    (
            final int rows, final int cols,
            final int score, final int safe
    ) {
        this.rows = rows;
        this.cols = cols;
        this.score = score;
        this.safe = safe;
        this.robots = (rows * cols) / 4;
        //create 2D map
        this.map = new int[rows][cols];
        //call method to randomly place player and robots
        mapPlacement();

    }
    /**
     * getters
     */
    public int rows() {
        return this.rows;
    }

    public int cols() {
        return this.cols;
    }

    public int score() {
        return this.score;
    }

    public int safe() {

        return this.safe;
    }

    public boolean blaster() {
        return this.blaster;
    }

    public int robots() {
        return this.robots;
    }

    public int[][] map() {
        return this.map;
    }

    public Status status(){
        return status;
    }
    public int valueAt(int row, int col) {
        return this.map[row][col];
    }
    /**
     * converts string to int
     */
    public int StringConvert (String line){
        line = line.replaceAll("\\D+", "");
        return Integer.parseInt(line);
    }
    /**
     * loads a game from safe file.
     */
    public void load(final BufferedReader reader) throws IOException {
        String line;
        int n=0;
        while (rows+4>n){
                line = reader.readLine();
                line = line.replaceAll("\\s","");

                    for (int i = 0; i < line.length(); i++) {
                        if (line.contains("Score")){score=StringConvert(line);}
                        else if (line.contains("Safejumps")){safe=(StringConvert((line)));}
                        else if(line.contains("[blaster]")){blaster=true;}
                        else if (line.contains("Status:Active")){status = Status.Active;}
                        else if (line.contains("Status:Cleared")){status =Status.Cleared;}
                        else if (line.contains("Status:Dead")){status = Status.Dead;}
                        else if (line.charAt(i) == '@') {
                            playerAt = new RC(n, i);
                            map[n][i] = Player;
                        } else if (line.charAt(i) == '1') {
                            map[n][i] = Robot1;
                            robots++;
                        } else if (line.charAt(i) == '2') {
                            map[n][i] = Robot2;
                            robots++;
                        } else if (line.charAt(i) == '#') {
                            map[n][i] = Heap;
                        } else if (line.charAt(i) == 'X') {
                            map[n][i] = Dead;
                        } else if (line.charAt(i) == '.') {
                            map[n][i] = Empty;
                        }
                    }
                n++;
        }
    }
    /**
     * implement new state from loaded game
     */
    public State(final int level, final BufferedReader reader) throws BadFileFormatException, IOException {

           rows = level + 4;
           cols = (rows * 3 + 1) / 2;
           map = new int[rows][cols];
           load(reader);
    }

    /**
     * used the logic from Cameron's example state class to make the map placement method, I hope this is okay
     */
    public void mapPlacement() {
        List<RC> cells = new ArrayList<>();
        for (int row=0;row<rows;row++){
            for(int col=0;col<cols;col++){
                cells.add(new RC(row,col));
                Collections.shuffle(cells);
            }
        }
        int robots_2 = Math.max(1, robots / 10);
        int robots_1 = robots - robots_2;

        for (int n=0;n<robots;n++){
            final RC rc = cells.get(0);
            if (robots_1 > 0) {map[rc.row()][rc.col()] =Robot1;
            robots_1--;}
            else if(robots_2>0){map[rc.row()][rc.col()]=Robot2;
            robots_2--;}
            cells.remove(0);
        }
        playerAt= cells.get(0);
        map[playerAt.row()][playerAt.col()]=Player;
        blaster=true;
        status=Status.Active;
        safe= safe()+3;
    }

    /**
     *Apply method,
     */

    public boolean apply(final Action action){
        if (action ==Action.Blast) {
            if(!playerBlast(action)){return false;}}
        else if(action==Action.LastStand){
            while(status==Status.Active){
                robotTurn();
            }
            return true;
        }
        else if(action==Action.Teleport){Teleport();}
         else if (action!= Action.Pass){
             if (!playerStep(action))return false;}
        robotTurn();

        return true;
    }

    /**
     *playerStep method: change rows and cols according to action performed and then check if it is possible to make this move
     */
    boolean playerStep(final Action action){
        int row = playerAt.row();
        int col = playerAt.col();
        //check actions and perform row and col changes accordingly
        if (action == Action.U){row--;}
        if (action == Action.UL){row--; col--;}
        if (action == Action.UR){row--; col++;}
        if (action == Action.D){row++;}
        if (action == Action.DL){row++; col--;}
        if (action == Action.DR){row++; col++;}
        if (action == Action.L){col--;}
        if (action == Action.R){col++;}
        //check whether changes can be applied or not
        if (!InBounds(row, col)){
            System.out.println("outside of map, try again");
            return false;
        }
        else if (RobotAt(map[row][col])||map[row][col]==Heap){
            System.out.println("occupied cell, try again");
            return false;
        }
        //map player to new position
        else {
            this.map[this.playerAt.row()][this.playerAt.col()] = Empty;
            this.playerAt = new RC(row, col);
            this.map[this.playerAt.row()][this.playerAt.col()] = Player;
        }

        return true;
    }

    /**
     *check surroundings at a 2block radius for 2step robots
     */
    private boolean AdjacentRobot2( int row, int col){
        return (InBounds(row-2,col)&&(map[row-2][col]==Robot2)
                ||InBounds(row-2,col-1)&&map[row-2][col-1]==Robot2
                ||InBounds(row-2,col+1)&&map[row-2][col+1]==Robot2
                ||InBounds(row-2,col+2)&&map[row-2][col+2]==Robot2
                ||InBounds(row-2,col-2)&&map[row-2][col-2]==Robot2
                ||InBounds(row+2,col)&&map[row+2][col]==Robot2
                ||InBounds(row+2,col-1)&&map[row+2][col-1]==Robot2
                ||InBounds(row+2,col-2)&&map[row+2][col-2]==Robot2
                ||InBounds(row+2,col+1)&&map[row+2][col+1]==Robot2
                ||InBounds(row+2,col+2)&&map[row+2][col+2]==Robot2
                ||InBounds(row,col+2)&&map[row][col+2]==Robot2
                ||InBounds(row,col-2)&&map[row][col-2]==Robot2
                ||InBounds(row-1,col+2)&&map[row-1][col+2]==Robot2
                ||InBounds(row-1,col-2)&&map[row-1][col-2]==Robot2
                ||InBounds(row+1,col+2)&&map[row+1][col+2]==Robot2
                ||InBounds(row+1,col-2)&&map[row+1][col-2]==Robot2);
    }

    /**
     *check whether a given position is on the map or not
     */
    private boolean InBounds(final int row, final int col) { return row >= 0 && row < rows && col >= 0 && col < cols; }

    /**
     *check whether there is a robot at given position (takes ints, so nop separate rows and cols)
     */
    private boolean RobotAt(int occupied){ return occupied==2||occupied==3; }


    private void checkStatus(){
        if (playerAt == null)status = Status.Dead;
        else if (robots == 0)status = Status.Cleared;
        if(status==Status.Cleared){this.score=score()+3;}
    }

    /**
     * checks which positions are safe from 1 and 2 step robots, and returns a list of these safe cells.
     */
    public List<RC> SafeDestination(){
        final ArrayList<RC> SafeCells = new ArrayList<>();
        for(int i=0; i<rows;i++){
            for(int j=0;j<cols;j++){

                if(map[i][j]==Empty&&!Neighbours(i,j)&&!AdjacentRobot2(i,j)){
                    SafeCells.add(new RC(i,j));
                    Collections.shuffle(SafeCells);
                }
            }
        }
        return SafeCells;
    }

    /**
     *checks whether there are robots in a 1 block radius from the given position (denoted by rows and cols)
     */
    public boolean Neighbours(int row, int col){
        return (InBounds(row-1,col)&&RobotAt(map[row-1][col])
                ||InBounds(row+1,col)&&RobotAt(map[row+1][col])
                ||InBounds(row-1,col-1)&&RobotAt(map[row-1][col-1])
                ||InBounds(row-1,col+1)&&RobotAt(map[row-1][col+1])
                ||InBounds(row+1,col+1)&&RobotAt(map[row+1][col+1])
                ||InBounds(row+1,col-1)&&RobotAt(map[row+1][col-1])
                ||InBounds(row,col+1)&&RobotAt(map[row][col+1])
                ||InBounds(row,col-1)&&RobotAt(map[row][col-1]));
    }

    /**
     * Teleport method
     */
    public void Teleport(){
    //current location of player
    int tempCol =playerAt.col();
    int tempRow=playerAt.row();
    //randomized list of empty cells
        List<RC>EmptyCells = new ArrayList<>();
    List<RC> SafeTP =SafeDestination();
    for(int i=0; i<rows;i++) {
        for (int j = 0; j < cols; j++) {
            if(map[i][j]==Empty){
                EmptyCells.add(new RC(i,j));
                Collections.shuffle(EmptyCells);
            }
        }
    }
//check whether safe jumps are available and whether there are any safe cells, otherwise tp to random empty cell
    if (safe>0&&SafeTP.size()!=0){
//new Player location
       playerAt=SafeTP.get(0);
       //Empty old player location
       map[tempRow][tempCol]=Empty;
        map[playerAt.row()][playerAt.col()]=Player;

        safe--;
    }else{
        System.out.println("no safe Teleport possible, random tp instead");
        //just take one of the empty cells for the new player location
        playerAt=EmptyCells.get(0);
        map[tempRow][tempCol]=Empty;
        map[playerAt.row()][playerAt.col()]=Player;
    }
        }

    /**
     * Used example structure to create the robotTurn and robotStep method.
     */
    private void robotTurn(){
        robotStep(1);
        robotStep(2);


    if (map[playerAt.row()][playerAt.col()] != Player){
        map[playerAt.row()][playerAt.col()] = Dead;
        playerAt = null;
    }
    checkStatus();


}
    /**
     * RobotStep method that moves the robots n number of steps into the direction of the player
     * params: Phase, which determines how many steps are taken
     */
    private void robotStep(final int phase)
    {
        final int[][] temp = new int[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++) {
                final int entity = map[i][j];
                //check whether there is a robot at location
                if (RobotAt(entity)) {
                    //checks which type of robot, which phase of movement is called and skips the movement if the wrong robot or the wrong iteration is called in conjunction
                    if (entity == Robot1 && phase == 1|| entity == Robot2 && phase==1||entity==Robot2 && phase == 2) {
                        int row = 0;
                        int col = 0;
                        //compare robot location to player location and change rows and cols accordingly
                        if (this.playerAt.row() - i < 0) {
                            row = i - 1;
                        }
                        if (this.playerAt.row() - i > 0) {
                            row = i + 1;
                        }
                        if (this.playerAt.col() - j < 0) {
                            col = j - 1;
                        }
                        if (this.playerAt.col() - j > 0) {
                            col = j + 1;
                        }
                        if (this.playerAt.row() - i == 0) {
                            row = i;
                        }
                        if (this.playerAt.col() - j == 0) {
                            col = j;
                        }
                        //The way of checking this is from Cameron's example
                        if (map[row][col] == Heap || temp[row][col] == Heap) {
                            robots--;
                            score++;
                        } else if (RobotAt(temp[row][col]) || phase > 1 && map[row][col] == Robot1) {
                            temp[row][col] = Heap;
                            robots -= 2;
                            score += 2;
                        } else {
                            temp[row][col] = map[i][j];
                        }
                        map[i][j] = Empty;
                    }
                }
            }
        for (int row = 0; row < rows; row++)
            for (int col = 0; col < cols; col++)
                if (temp[row][col] != 0)
                    map[row][col] = temp[row][col];
    }

    /**
     *method to kill robots at given position and to increment score and decrement robots.
     * used to make the blast method more concise.
     */
    public void kill(int row, int col){
        this.map[row][col]=Heap;
        this.robots--;
        this.score++;
    }

    /**
     *blast method
     * takes the player location, checks whether there are adjacent robots (1-block radius).
     * then checks each surrounding position to decide whether there are robots to kill at that position
     * this could have probably been implemented more efficiently, but this made sense to me, which is why I stuck with it
     */
    boolean playerBlast(Action action) {
        int row = playerAt.row();
        int col=playerAt.col();
        if (action == Action.Blast && blaster) {
            if (!Neighbours(row, col)) {
                System.out.println("no robots to blast");
                return false;
            } else {
                if (InBounds(row - 1, col) &&RobotAt(this.map[row - 1][col])) {
                    kill(row-1,col);
                }
                if (InBounds(row - 1, col-1) &&RobotAt(this.map[row - 1][col - 1])) {
                    kill(row-1,col-1);
                }
                if (InBounds(row - 1, col+1) &&RobotAt(this.map[row - 1][col + 1])) {
                    kill(row-1,col+1);
                }
                if (InBounds(row , col+1) &&RobotAt(this.map[row][col + 1])) {
                    kill(row,col+1);
                }
                if (InBounds(row, col-1) &&RobotAt(this.map[row][col - 1])) {
                    kill(row,col-1);
                }
                if (InBounds(row + 1, col) &&RobotAt(this.map[row + 1][col])) {
                    kill(row+1,col);
                }
                if (InBounds(row + 1, col-1) &&RobotAt(this.map[row + 1][col - 1])) {
                    kill(row+1,col-1);
                }
                if (InBounds(row + 1 , col+1) &&RobotAt(this.map[row + 1][col + 1])) {
                    kill(row+1,col+1);
                }
                blaster=false;
                System.out.println(" a blast was performed ");
                return true;
            }
        }
        System.out.println("no Blaster left");
        return  false;
    }

    /**
     * prints map to string in console, for testing purposes
     *
     */

    @Override
    public String toString(){
        //use string builder to display the map in string values.
        StringBuilder fixer = new StringBuilder();
//loop over the map and convert int values to the characters defined in the assignment
        for (int i = 0; i < rows; i++)
        {
            for (int j = 0; j < cols; j++)
            {
                char ch = '?';
                if (map[i][j]==0)
                    ch = '.';
                else if (map[i][j] == 1)
                    ch = '@';
                else if (map[i][j] == 2)
                    ch = '1';
                else if (map[i][j] == 3)
                    ch = '2';
                else if (map[i][j] == 4)
                    ch = '#';
                else if (map[i][j] == 5) {
                    ch = 'X';
                }
                fixer.append(ch + " ");
            }
            fixer.append("\n");
        }
//append the values with contextual text and print each on a new line
        fixer.append("Score:  ").append(score).append("\n");
        fixer.append("Safe jumps: ").append(safe).append("\n");
// test boolean value for blaster and return 1 if blaster is available and 0 if it is not available
        fixer.append(blaster ? "[blaster]\n" : "\n");
        fixer.append("Status: ").append(status).append("\n");
        return fixer.toString();
    }

}
