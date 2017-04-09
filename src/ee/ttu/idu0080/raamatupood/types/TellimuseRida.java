package ee.ttu.idu0080.raamatupood.types;

import java.io.Serializable;

public class TellimuseRida implements Serializable {

    private long kogus;
    private Toode toode;

    public TellimuseRida(long kogus, Toode toode) {
        this.kogus = kogus;
        this.toode = toode;
    }

    public long getKogus() {
        return kogus;
    }

    public void setKogus(long kogus) {
        this.kogus = kogus;
    }

    public Toode getToode() {
        return toode;
    }

    public void setToode(Toode toode) {
        this.toode = toode;
    }

    @Override
    public String toString() {
        return "TellimuseRida [kogus=" + kogus + ", toode=" + toode + "]";
    }


}
