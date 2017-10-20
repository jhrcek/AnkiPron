package cz.janhrcek.ankipron.anki;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author jhrcek
 */
@RunWith(Parameterized.class)
public class WordExtractorTest {

    @Parameter(value = 0)
    public String flds;
    @Parameter(value = 1)
    public String expectedWord;

    @Test
    public void extractWord() {
        WordExtractor we = new WordExtractor();
        assertEquals(expectedWord, we.extractWord(flds));
    }

    @Parameters
    public static Collection<Object[]> words() {
        return Arrays.asList(new Object[][]{
                {"pralinkae Praline (-, -n)y", "Praline"},
                {"vydání (knihy ap.)e Auflage (-, -n)y", "Auflage"},
                {"zálusk, choutky (na co)s Gelüste (auf 4.p)y", "Gelüste"},
                {"připravit se na něco (2 předložky)(sich) vor/bereiten (auf etw 4.p / für etw)y", "vorbereiten"},
                {"trpět (čím)<br />(min?)leiden (unter etw) - litt - hat gelitten y", "leiden"},
                {"zadržet, zastavit<br />(min?)<br />(er?)an/halten - hielt an - hat angehalten<br />er hält any",
                        "anhalten"},
                {"zemřít (na něco)<br />(er?)<br />(min?)sterben (an etw - 3.p!) - starb - ist gestorben<br />er stirbty",
                        "sterben"},
                {"napadnou (někoho) (o myšlence)<br />(min?)<br />(er?)ein/fallen (j-m) (3.p!) - fiel ein - ist eingefallen<br />fällt einDann <b>fiel</b> ihnen Frederick <b>ein</b>.<br>Es ist uns leider nichts <b>eingefallen</b>.y",
                        "einfallen"},
                {"prosit (někoho o něco)<br />(min?)bitten (j-n um etw) - bat - hat gebeteny", "bitten"}
        });
    }
}
