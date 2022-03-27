package game;

public class BadFileFormatException extends Exception {
    private final String errorMessage;
    private final int errorRow;
    private final int errorCol;

    public BadFileFormatException(String errorMessage, int errorRow, int errorCol){
        this.errorMessage=errorMessage;
        this.errorRow=errorRow;
        this.errorCol=errorCol;
    }




    public String toString(){
        return "BadFileFormatException: " + errorMessage + " in row " + errorRow + " and col " + errorCol + ". A new level will now be started";
    }
}
