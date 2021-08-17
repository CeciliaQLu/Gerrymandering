/**
 * This class defines the precinct.
 * Each precinct has the number registered for party L and N.
 *
 * @author luqing (Student ID: 300363602)
 */
public class Precinct {
    private int NumVoteL = 0;
    private int NumVoteN = 0;
    public Precinct(int l, int n){
        this.NumVoteL = l;
        this.NumVoteN = n;
    }

    public int getNumVoteL() {
        return NumVoteL;
    }

    public int getNumVoteN() {
        return NumVoteN;
    }
}
