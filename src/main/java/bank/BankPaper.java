package bank;

import cigarrate.Paper;

import java.util.ArrayList;

public class BankPaper {
    public ArrayList<Paper> papers = new ArrayList<>();

    /**
     * Remueve el último papel de la lista de papeles.
     */
    public void popPaper() {
        this.papers.remove(0);
    }

    /**
     * Añade un nuevo papel a la lista de papeles.
     *
     * @param paper papel a añadir.
     */
    public void pushPaper(Paper paper) {
        this.papers.add(paper);
    }

    /**
     * @return cantidad de papeles en el banco.
     */
    public int getSize() {
        return this.papers.size();
    }
}
