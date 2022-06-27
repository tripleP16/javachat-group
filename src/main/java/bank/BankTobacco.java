package bank;

import cigarrate.Tobacco;

import java.util.ArrayList;

public class BankTobacco {
    public ArrayList<Tobacco> tobaccos = new ArrayList<>();

    public void popTobacco() {
        this.tobaccos.remove(0);
    }

    public void pushTobacco(Tobacco tobacco) {
        this.tobaccos.add(tobacco);
    }

    public int getSize() {
        return this.tobaccos.size();
    }
}
