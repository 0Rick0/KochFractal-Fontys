package timeutil;

import java.util.LinkedList;
import java.util.List;

/**
 * Deze klasse maakt het mogelijk om opeenvolgende tijdsperiodes een naam te
 * geven, deze op te slaan en deze daarna te printen (via toString).
 *
 * Tijdsperiodes worden bepaald door een begintijd en een eindtijd.
 *
 * begintijd van een periode kan gezet worden door setBegin, de eindtijd kan
 * gezet worden door de methode setEind.
 *
 * Zowel bij de begin- als eindtijd van ee periode kan een String meegegeven
 * worden die voor de gebruiker een betekenisvolle aanduiding toevoegt aan dat
 * tijdstip. Indien geen string meegegeven wordt, wordt een teller gebruikt, die
 * automatisch opgehoogd wordt.
 *
 * Na het opgeven van een begintijdstip (via setBegin of eenmalig via init ) kan
 * t.o.v. dit begintijdstip steeds een eindtijdstip opgegeven worden. Zodoende
 * kun je vanaf 1 begintijdstip, meerdere eindtijden opgeven.
 *
 * Een andere mogelijkheid is om een eindtijdstip direct te laten fungeren als
 * begintijdstip voor een volgende periode. Dit kan d.m.v. SetEndBegin of seb.
 *
 * alle tijdsperiodes kunnen gereset worden dmv init()
 *
 * @author erik
 *
 */
public class TimeStamp {

    private static long counter = 0;
    private long curBegin;
    private String curBeginS;
    private List<Period> list;

    public TimeStamp() {
        TimeStamp.counter = 0;
        this.init();
    }

    /**
     * initialiseer klasse. begin met geen tijdsperiodes.
     */
    public void init() {
        this.curBegin = 0;
        this.curBeginS = null;
        this.list = new LinkedList();
    }

    /**
     * zet begintijdstip. gebruik interne teller voor identificatie van het
     * tijdstip
     */
    public void setBegin() {
        this.setBegin(String.valueOf(TimeStamp.counter++));
    }

    /**
     * zet begintijdstip
     *
     * @param timepoint betekenisvolle identificatie van begintijdstip
     */
    public void setBegin(String timepoint) {
        this.curBegin = System.currentTimeMillis();
        this.curBeginS = timepoint;
    }

    /**
     * zet eindtijdstip. gebruik interne teller voor identificatie van het
     * tijdstip
     */
    public void setEnd() {
        this.setEnd(String.valueOf(TimeStamp.counter++));
    }

    /**
     * zet eindtijdstip
     *
     * @param timepoint betekenisvolle identificatie vanhet eindtijdstip
     */
    public void setEnd(String timepoint) {
        this.list.add(new Period(this.curBegin, this.curBeginS, System.currentTimeMillis(), timepoint));
    }

    /**
     * zet eindtijdstip plus begintijdstip
     *
     * @param timepoint identificatie van het eind- en begintijdstip.
     */
    public void setEndBegin(String timepoint) {
        this.setEnd(timepoint);
        this.setBegin(timepoint);
    }

    /**
     * verkorte versie van setEndBegin
     *
     * @param timepoint
     */
    public void seb(String timepoint) {
        this.setEndBegin(timepoint);
    }

    /**
     * interne klasse voor bijhouden van periodes.
     *
     * @author erik
     *
     */
    private class Period {

        long begin;
        String beginS;
        long end;
        String endS;

        public Period(long b, String sb, long e, String se) {
            this.setBegin(b, sb);
            this.setEnd(e, se);
        }

        private void setBegin(long b, String sb) {
            this.begin = b;
            this.beginS = sb;
        }

        private void setEnd(long e, String se) {
            this.end = e;
            this.endS = se;
        }

        @Override
        public String toString() {
            return "From '" + this.beginS + "' till '" + this.endS + "' is " + (this.end - this.begin) + " mSec.";
        }
    }

    /**
     * override van toString methode. Geeft alle tijdsperiode weer.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        for (Period p : this.list) {
            buffer.append(p.toString());
            buffer.append('\n');
        }
        return buffer.toString();
    }
}
