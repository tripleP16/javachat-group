package cigarrate;

import java.util.concurrent.TimeUnit;

public class Cigarrate {
    public Tobacco tobacco;
    public Match match;
    public Paper paper;

    public Cigarrate(Tobacco tobacco, Match match, Paper paper) {
        this.tobacco = tobacco;
        this.match = match;
        this.paper = paper;
    }

    /**
     * Espera 3 segundos a que se fume el cigarro.
     *
     * @throws InterruptedException si se produce un error al esperar.
     */
    public void smoke() throws InterruptedException {
        System.out.println("FUMANDO........");
        TimeUnit.SECONDS.sleep(3);
        System.out.println("Volviendo a armar cigarrillo....");
    }
}
