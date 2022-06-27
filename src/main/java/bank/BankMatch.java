package bank;

import cigarrate.Match;

import java.util.ArrayList;

public class BankMatch {
    public ArrayList<Match> matches = new ArrayList<>();

    public void popMatch() {
        matches.remove(0);
    }

    public BankMatch() {

    }

    public void pushMatch(Match match) {
        matches.add(match);

    }

    public int getSize() {
        return matches.size();
    }

}
