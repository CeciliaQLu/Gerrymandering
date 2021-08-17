import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class defines the gerrymandering.
 * It allows the user to enter the number of precincts, and the number of people in each precinct.
 * It then shows the randomly generated table.
 * It will eventually show if the set of precincts is susceptible.
 *
 * @author luqing (Student ID: 300363602)
 */
public class Gerrymandering {
    /**
     * number of precincts
     */
    int n;

    /**
     * number of people in a precinct
     */
    int m;

    /**
     * This is a simple 4 x 4 case for testing.
     */
    public Precinct[] Pcs_Test = new Precinct[4];

    /**
     * Constructor of Gerrymandering.
     */
    public Gerrymandering() {
        n = getPrecinctNum();
        m = getOnePNum();
    }

    /**
     * This function builds a list of 4 precincts, each of which has 4 voters.
     * Precinct     1   2   3   4
     * party L      3   2   1   4
     * party N      1   2   3   0
     */
    public void simpleTestCase() {
        Pcs_Test[0] = new Precinct(3, 1);
        Pcs_Test[1] = new Precinct(2, 2);
        Pcs_Test[2] = new Precinct(1, 3);
        Pcs_Test[3] = new Precinct(4, 0);

    }

    /**
     * Generate a list of precincts and start gerrymandering.
     */
    public void run() {
        // 0 precincts
        if (n == 0) {
            System.out.println("There is no precinct to consider.");
            return;
        }
        // no person in precincts
        if (m == 0) {
            System.out.println("There is no people in each precinct.");
            return;
        }

        // Generate a list of Precincts with number of voters for party L and party N
        Precinct[] precincts = generatePrecincts();

        // simple case for testing:
        // simpleTestCase();
        // Precinct[] precincts = Pcs_Test;
        // n = 4;
        // m = 4;

        gerrymander(precincts);
    }

    /**
     * Check which party has more voters than another for gerrymandering.
     *
     * @param precincts a valid list of precincts
     */
    public void gerrymander(Precinct[] precincts) {
        // check if one party has more voters than another
        int Lvoters = 0;
        int Nvoters = 0;
        for (Precinct p : precincts) {
            Lvoters += p.getNumVoteL();
            Nvoters += p.getNumVoteN();
        }
        if (Lvoters == Nvoters) {
            System.out.println("Number registered for party L = Number registered for party N, so this set of precincts is NOT susceptible.");
            return;
        }

        // upper limit of the number of people who vote for the party (with more people registered in total) in each direct.
        int upperNum = 0;
        int winnterParty = 0; // default: party L holds the majority
        if (Lvoters > Nvoters) {
            if (Lvoters >= n * m / 2 + 2) {
                upperNum = Lvoters - n * m / 4 - 1;
            } else {
                System.out.println("No, this set of precincts is NOT susceptible.");
                return;
            }
        } else {
            if (Nvoters >= n * m / 2 + 2) {
                upperNum = Nvoters - n * m / 4 - 1;
                winnterParty = 1;
            } else {
                System.out.println("No, this set of precincts is NOT susceptible.");
                return;
            }
        }

        // check if the number of winner party voters in first precinct is beyond the upper bound
        int voteWinPartyP1 = getWinPartyPi(precincts, winnterParty, 0);
        if (voteWinPartyP1 > upperNum) {
            System.out.println("No, this set of precincts is NOT susceptible (first precinct beyonds boundary)!");
            return;
        }
        gerrymandering(precincts, upperNum, winnterParty);
    }

    /**
     * This is the code for gerrymandering algorithm.
     * It builds a 3-dimension table.
     * Pseudocode of this function is provided in the report.
     *
     * @param precincts a list of precincts
     * @param maxNum    the upper bound of the number of voters who vote the winner party in each district.
     * @param winParty  if party L holds a majority in total, winParty = 0;
     *                  if party N holds a majority in total, winParty = 1.
     */
    public void gerrymandering(Precinct[] precincts, int maxNum, int winParty) {
        // barometer
        int barometer = 0;
        // Initialise a new array, and set G[i][0][0] to be true, others to be false
        boolean[][][] G = new boolean[n + 1][n / 2 + 1][maxNum + 1];
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= n / 2; j++) {
                for (int k = 0; k <= maxNum; k++) {
                    if (j == 0 && k == 0) {
                        G[i][j][k] = true;
                        barometer++;
                    } else {
                        G[i][j][k] = false;
                        barometer++;
                    }
                }
            }
        }

        // i = 1, j = 1, G[i][j][w] = true, where w is the number of voters voting the winner party in the first precinct
        int voteWinPartyP1 = getWinPartyPi(precincts, winParty, 0);
        G[1][1][voteWinPartyP1] = true;

        // go through all possible combinations that a certain amount of precincts can achieve the certain number of voters voting the winner party
        for (int i = 2; i <= n; i++) {
            for (int j = 1; j <= n / 2; j++) {
                for (int k = 0; k <= maxNum; k++) {
                    int numExPi = k - getWinPartyPi(precincts, winParty, i - 1);
                    if (numExPi >= 0) {
                        if (G[i - 1][j - 1][numExPi]) {
                            G[i][j][k] = true;
                            barometer++;
                        }
                    }
                    if (G[i - 1][j][k]) {
                        G[i][j][k] = true;
                        barometer++;
                    }
                }
            }
        }

        // print out the table for checking
        // printTable(G);

        // check if there is any division in such a way that the same party holds a majority in both districts
        int lowerBound = n * m / 4 + 1;
        System.out.println("n = " + n + "\nm = " + m + "\nLower Bound = " + lowerBound + "\nUpper Bound = " + maxNum + "\n");
        for (int s = lowerBound; s <= maxNum; s++) {
            if (G[n][n / 2][s]) {
                System.out.println("Yes, this set of precincts is susceptible!");
                System.out.println("barometer: " + barometer);
                return;
            }
        }
        System.out.println("No, this set of precincts is NOT susceptible.");
        System.out.println("barometer: " + barometer);
    }

    /**
     * Print out the 3-D boolean table.
     *
     * @param G the 3D table with the type boolean
     */
    public void printTable(boolean[][][] G) {
        for (int i = 0; i < G.length; i++) {
            for (int j = 0; j < G[0].length; j++) {
                for (int k = 0; k < G[0][0].length; k++) {
                    if (G[i][j][k]) {
                        System.out.print("T");
                    } else {
                        System.out.print("F");
                    }
                }
                System.out.print("    ");
            }
            System.out.println();
        }
    }

    /**
     * Get the number registered for winner party in the ith precinct.
     *
     * @param precincts a list of precinct.
     * @param flag      if party L holds the majority in total, flag = 0; otherwise, flag = 1.
     * @param i         the index of precinct.
     * @return the number registered for winner party in the ith precinct
     */
    public int getWinPartyPi(Precinct[] precincts, int flag, int i) {
        if (flag == 0) { // party L has more voters
            return precincts[i].getNumVoteL();
        } else { // party L has more voters
            return precincts[i].getNumVoteN();
        }
    }

    /**
     * Generate a list of precincts.
     * For each precinct:
     * number of voters who vote party L: random an integer within the range [0, m];
     * number of voters who vote party N: m - number of voters who vote party L.
     *
     * @return a list of precincts
     */
    public Precinct[] generatePrecincts() {
        Precinct[] precincts = new Precinct[n];
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            int randomeNum = random.nextInt(m + 1);
            precincts[i] = new Precinct(randomeNum, m - randomeNum);
        }
        printPrecincts(precincts);
        return precincts;
    }

    /**
     * Print the precincts.
     *
     * @param ps
     */
    public void printPrecincts(Precinct[] ps) {
        // First line: title, e.g., "Precincts   1  2  3  4"
        String firstLine = "\nPrecinct                        ";
        for (int i = 0; i < n; i++) {
            firstLine += ((i + 1) + "\t\t");
        }

        // Second line: number registered for party L in each precinct
        String secondLine = "Number registered for Party L:\t";
        for (Precinct p : ps) {
            secondLine += (p.getNumVoteL() + "\t\t");
        }

        // third line: number registered for party N in each precinct
        String thirdLine = "Number registered for Party N:\t";
        for (Precinct p : ps) {
            thirdLine += (p.getNumVoteN() + "\t\t");
        }
        System.out.println(firstLine);
        System.out.println(secondLine);
        System.out.println(thirdLine);
        System.out.println();
    }

    /**
     * Check if an input string is an integer.
     *
     * @param str the entered string
     * @return true if it is a valid integer;
     * false otherwise.
     */
    public boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher matcher = pattern.matcher(str);
        if (!matcher.matches()) {
            return false;
        }
        return true;
    }

    /**
     * Get the number of precincts.
     * Ask the user to enter an even integer.
     * If the user enters the wrong type, it will keep asking for the correct one.
     *
     * @return the number of precincts
     */
    public int getPrecinctNum() {
        System.out.print("Number of Precints (even): ");
        Scanner scan = new Scanner(System.in);
        String str = scan.nextLine(); // get the input as a string
        while (true) {
            if (!isNumeric(str)) {
                scan = new Scanner(System.in); // get the new input as a string
                System.out.print("Please enter an even integer for precinct number: ");
                str = scan.nextLine();
            } else {
                if (Integer.valueOf(str) % 2 == 1) {
                    System.out.print("An even integer is required: ");
                    scan = new Scanner(System.in); // get the new input as a string
                    str = scan.nextLine();
                    continue;
                } else {
                    return Integer.valueOf(str);
                }
            }
        }
    }

    /**
     * Get the number of people in each precinct.
     * Ask the user to enter an integer.
     * If the user enters the wrong type, it will keep asking for the correct one.
     *
     * @return the number of people in each precinct
     */
    public int getOnePNum() {
        System.out.print("Number of people in each precinct: ");
        Scanner scan = new Scanner(System.in);
        String str = scan.nextLine(); // get the input as a string
        while (!isNumeric(str)) {
            scan = new Scanner(System.in); // get the new input as a string
            System.out.print("Please enter an integer for the number of people in each precinct: ");
            str = scan.nextLine();
        }
        return Integer.valueOf(str);
    }
}
