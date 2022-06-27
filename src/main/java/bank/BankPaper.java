package bank;

import cigarrate.Paper;

import java.util.ArrayList;

public class BankPaper {
    public ArrayList<Paper> papers = new ArrayList<>();

    public void popPaper() {
        this.papers.remove(0);
    }

    public void pushPaper(Paper paper) {
        this.papers.add(paper);
    }

    public int getSize() {
        return this.papers.size();
    }
}
