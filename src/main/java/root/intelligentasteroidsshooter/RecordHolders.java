package root.intelligentasteroidsshooter;

public class RecordHolders {
    private String name;
    private String score;

    public RecordHolders(String name, String score){
        this.name = name;
        this.score = score;
    }

    public String getName(){ return this.name;}

    public String getScore(){ return this.score;}
}
