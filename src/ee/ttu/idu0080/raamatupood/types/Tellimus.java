package ee.ttu.idu0080.raamatupood.types;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Tellimus implements Serializable {
    private static final long serialVersionUID = 1L;
    public List<TellimuseRida> tellimuseRead;

    public Tellimus() {
        TellimuseRida r1 = new TellimuseRida(serialVersionUID, new Toode(1, "Macbook PRO", new BigDecimal(10)));
        TellimuseRida r2 = new TellimuseRida(serialVersionUID, new Toode(2, "Macbook AIR", new BigDecimal(11)));
        TellimuseRida r3 = new TellimuseRida(serialVersionUID, new Toode(3, "Macbook 12'", new BigDecimal(12)));
        TellimuseRida r4 = new TellimuseRida(serialVersionUID, new Toode(4, "Mac PRO", new BigDecimal(13)));

        tellimuseRead = new ArrayList<TellimuseRida>();

        tellimuseRead.add(r1);
        tellimuseRead.add(r2);
        tellimuseRead.add(r3);
        tellimuseRead.add(r4);
    }

    public void addTellimuseRida(TellimuseRida tellimuserida) {
        this.tellimuseRead.add(tellimuserida);
    }

    public List<TellimuseRida> getTellimuseRead() {
        return tellimuseRead;
    }

    @Override
    public String toString() {
        return "Tellimus [tellimuseRead=" + tellimuseRead + "]";
    }

}
