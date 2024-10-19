package root.intelligentasteroidsshooter;

public class RecordHolders {
    private String name;
    private String score;
    private int points;

    public RecordHolders(String name, String score){
        this.name = name;
        this.score = score;
        this.points = 0;
    }

    public void add(int value){
        points = value;
    }

    public int getPoints(){ return this.points;}

    public String getName(){ return this.name;}

    public String getScore(){ return this.score;}
}
