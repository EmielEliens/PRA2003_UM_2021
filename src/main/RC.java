package main;


public class RC {

    private final int row;
    private final int col;

    public RC(final int row, final int col){
        this.row=row;
        this.col = col;
    }
    public int row(){
        return  this.row;
    }
    public int col(){
        return this.col;
    }

}