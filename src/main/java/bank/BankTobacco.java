package bank;

import cigarrate.Tobacco;

import java.util.ArrayList;

public class BankTobacco {
    public ArrayList<Tobacco> tobaccos = new ArrayList<>();

    /**
     * Remueve el último tabaco de la lista de tabacos.
     */
    public void popTobacco() {
        this.tobaccos.remove(0);
    }


    /**
     * Añade un nuevo tabaco a la lista de tabacos.
     *
     * @param tobacco tabaco a añadir.
     */
    public void pushTobacco(Tobacco tobacco) {
        this.tobaccos.add(tobacco);
    }

    /**
     * @return cantidad de tabacos en el banco.
     */
    public int getSize() {
        return this.tobaccos.size();
    }
}
