package bank;

import cigarrate.Match;

import java.util.ArrayList;

public class BankMatch {
    public ArrayList<Match> matches = new ArrayList<>();

    /**
     * Remueve el último fósforo de la lista de fósforos.
     */
    public void popMatch() {
        matches.remove(0);
    }

    public BankMatch() {

    }


    /**
     * Añade un nuevo fósforo a la lista de fósforos.
     *
     * @param match fósforo a añadir.
     */
    public void pushMatch(Match match) {
        matches.add(match);

    }

    /**
     * @return cantidad de fósforos en el banco.
     */
    public int getSize() {
        return matches.size();
    }

}
