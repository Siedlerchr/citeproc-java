package de.undercouch.citeproc.bibtex;

import de.undercouch.citeproc.CSL;
import de.undercouch.citeproc.output.Bibliography;
import de.undercouch.citeproc.output.Citation;
import org.jbibtex.BibTeXDatabase;
import org.jbibtex.Key;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests the BibTeX citation item provider
 * @author Michel Kraemer
 */
public class BibTeXItemDataProviderTest extends AbstractBibTeXTest {
    private static BibTeXDatabase db;
    private static final BibTeXItemDataProvider sys = new BibTeXItemDataProvider();

    /**
     * Set up this test
     * @throws Exception if something goes wrong
     */
    @BeforeClass
    public static void setUp() throws Exception {
        db = loadUnixDatabase();
        sys.addDatabase(db);
    }

    /**
     * Tests if a valid bibliography can be generated through the item provider
     * @throws Exception if something goes wrong
     */
    @Test
    public void bibliography() throws Exception {
        CSL citeproc = new CSL(sys, "ieee");
        citeproc.setOutputFormat("text");

        String id0 = "Johnson:1973:PLB";
        String id1 = "Ritchie:1973:UTS";
        String id2 = "Ritchie:1974:UTS";
        String id3 = "Lycklama:1978:UTSb";
        List<Citation> a = citeproc.makeCitation(id0);
        assertEquals(0, a.get(0).getIndex());
        assertEquals("[1]", a.get(0).getText());

        a = citeproc.makeCitation(id1);
        assertEquals(1, a.get(0).getIndex());
        assertEquals("[2]", a.get(0).getText());

        a = citeproc.makeCitation(id0, id1);
        assertEquals(2, a.get(0).getIndex());
        assertEquals("[1], [2]", a.get(0).getText());

        a = citeproc.makeCitation(id2, id0);
        assertEquals(3, a.get(0).getIndex());
        assertEquals("[1], [3]", a.get(0).getText());

        a = citeproc.makeCitation(id3);
        assertEquals(4, a.get(0).getIndex());
        assertEquals("[4]", a.get(0).getText());

        Bibliography b = citeproc.makeBibliography();
        assertEquals(4, b.getEntries().length);
        assertEquals("[1]S. C. Johnson and B. W. Kernighan, \u201cThe Programming Language B,\u201d "
                + "Bell Laboratories, Murray Hill, NJ, USA, Technical report 8, 1973.\n", b.getEntries()[0]);
        assertEquals("[2]D. M. Ritchie and K. Thompson, \u201cThe UNIX time-sharing system,\u201d "
                + "Operating Systems Review, vol. 7, Art. no. 4, Oct. 1973.\n", b.getEntries()[1]);
        assertEquals("[3]D. W. Ritchie and K. Thompson, \u201cThe UNIX Time-Sharing System,\u201d "
                + "Communications of the Association for Computing Machinery, vol. 17, Art. no. 7, "
                + "Jul. 1974.\n", b.getEntries()[2]);
        assertEquals("[4]H. Lycklama, \u201cUNIX Time-Sharing System: UNIX on a Microprocessor,\u201d "
                + "The Bell System Technical Journal, vol. 57, Art. no. 6, "
                + "Jul.\u2013Aug. 1978, [Online]. Available: "
                + "http://bstj.bell-labs.com/BSTJ/images/Vol57/bstj57-6-2087.pdf\n", b.getEntries()[3]);
    }

    /**
     * Tests if a valid bibliography can be generated through the item provider
     * @throws Exception if something goes wrong
     */
    @Test
    public void numericAlphabetical() throws Exception {
        CSL citeproc = new CSL(sys, "din-1505-2-numeric-alphabetical");
        citeproc.setOutputFormat("text");

        List<Key> keys = new ArrayList<>(db.getEntries().keySet());
        List<String> result = new ArrayList<>();
        List<Integer> rnds = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            int j = (int)(Math.random() * keys.size());
            rnds.add(j);
            Key k = keys.get(j);
            List<Citation> cs = citeproc.makeCitation(k.getValue());
            for (Citation c : cs) {
                while (result.size() <= c.getIndex()) {
                    result.add("");
                }
                result.set(c.getIndex(), c.getText());
            }
        }

        int j = 0;
        for (Integer r : rnds) {
            Key k = keys.get(r);
            List<Citation> cs = citeproc.makeCitation(k.getValue());
            for (Citation c : cs) {
                if (c.getIndex() < result.size()) {
                    assertEquals(result.get(c.getIndex()), c.getText());
                } else if (c.getIndex() == result.size()) {
                    result.add(c.getIndex(), c.getText());
                } else {
                    fail("New index must not be larger than " + result.size());
                }
            }
            String nc = cs.get(0).getText();
            String pc = result.get(j);
            assertEquals(nc, pc);
            ++j;
        }
    }

    /**
     * Test if an invalid month can be handled correctly (i.e. if it will be ignored)
     * @throws Exception if something goes wrong
     */
    @Test
    public void issue34() throws Exception {
        String entry = "@inproceedings{ICIP99inv," +
                "author = \"M.G. Strintzis and I. Kompatsiaris\"," +
                "title = \"{3D Model-Based Segmentation of Videoconference Image Sequences}\"," +
                "address = \"Kobe, Japan\"," +
                "month = \"October 25-28\"," +
                "year = \"1999\"," +
                "note = {invited paper}," +
                "pages = \"\"," +
                "}";
        ByteArrayInputStream bais = new ByteArrayInputStream(
                entry.getBytes(StandardCharsets.UTF_8));

        BibTeXDatabase db = new BibTeXConverter().loadDatabase(bais);
        BibTeXItemDataProvider sys = new BibTeXItemDataProvider();
        sys.addDatabase(db);

        CSL citeproc = new CSL(sys, "ieee");
        citeproc.setOutputFormat("text");
        sys.registerCitationItems(citeproc);

        Bibliography bibl = citeproc.makeBibliography();
        for (String e : bibl.getEntries()) {
            assertEquals("[1]M. G. Strintzis and I. Kompatsiaris, "
                    + "\u201c3D Model-Based Segmentation of Videoconference "
                    + "Image Sequences,\u201d Kobe, Japan, 1999.", e.trim());
        }
    }

    /**
     * Check that we never set the "collection-author" attribute (see issue #38)
     * @throws Exception if something goes wrong
     */
    @Test
    public void noCollectionAuthor() throws Exception {
        // compare with an item from the unix database
        CSL citeproc = new CSL(sys, "apa");
        citeproc.setOutputFormat("text");

        List<Citation> a = citeproc.makeCitation("Sterling:2001:BCCa");
        assertEquals("(Sterling, 2001)", a.get(0).getText());

        Bibliography b = citeproc.makeBibliography();
        assertEquals(1, b.getEntries().length);
        assertEquals("Sterling, T. L. (2001). Beowulf Cluster Computing with Linux "
                + "(p. xxxiii + 496). MIT Press.\n", b.getEntries()[0]);

        // compare with another item from the unix database
        String entry = "@book{rice_five_1993," +
                "address = {Santa Fe, NM}," +
                "title = {Five steps to {HP}-{UX}}," +
                "isbn = {0-934605-24-6}," +
                "publisher = {OnWord Press}," +
                "editor = {Rice, Jim}," +
                "year = {1993}," +
                "lccn = {QA76.8.H48 F58 1993}" +
                "}";

        ByteArrayInputStream bais = new ByteArrayInputStream(
                entry.getBytes(StandardCharsets.UTF_8));

        BibTeXDatabase db = new BibTeXConverter().loadDatabase(bais);
        BibTeXItemDataProvider sys = new BibTeXItemDataProvider();
        sys.addDatabase(db);

        citeproc = new CSL(sys, "apa");
        citeproc.setOutputFormat("text");
        sys.registerCitationItems(citeproc);

        Bibliography b2 = citeproc.makeBibliography();
        assertEquals(1, b2.getEntries().length);
        assertEquals("Rice, J. (1993). Five steps to HP-UX. OnWord Press.\n", b2.getEntries()[0]);
    }

    /**
     * Check that we set the "genre" attribute (see issue #63)
     * @throws Exception if something goes wrong
     */
    @Test
    public void techReportWithExplicitType() throws Exception {
        // compare with the item from issue #63. Example from
        // http://tug.ctan.org/tex-archive/biblio/bibtex/contrib/IEEEtran/IEEEexample.bib
        String entry = "@techreport{IEEEexample:techreptype,\n" +
                "  author        = \"J. Padhye and V. Firoiu and D. Towsley\",\n" +
                "  title         = \"A Stochastic Model of {TCP} {R}eno Congestion Avoidance\n" +
                "                   and Control\",\n" +
                "  institution   = \"Univ. of Massachusetts\",\n" +
                "  address       = \"Amherst, MA\",\n" +
                "  type          = \"CMPSCI Tech. Rep.\",\n" +
                "  number        = \"99-02\",\n" +
                "  year          = \"1999\"\n" +
                "}\n" +
                "\n" +
                "@techreport{IEEEexample:techreptypeii,\n" +
                "  author        = \"D. Middleton and A. D. Spaulding\",\n" +
                "  title         = \"A Tutorial Review of Elements of Weak Signal Detection\n" +
                "                   in Non-{G}aussian {EMI} Environments\",\n" +
                "  institution   = \"National Telecommunications and Information\n" +
                "                   Administration ({NTIA}), U.S. Dept. of Commerce\",\n" +
                "  type          = \"NTIA Report\",\n" +
                "  number        = \"86-194\",\n" +
                "  month         = may,\n" +
                "  year          = \"1986\"\n" +
                "}";

        ByteArrayInputStream bais = new ByteArrayInputStream(
                entry.getBytes(StandardCharsets.UTF_8));

        BibTeXDatabase db = new BibTeXConverter().loadDatabase(bais);
        BibTeXItemDataProvider sys = new BibTeXItemDataProvider();
        sys.addDatabase(db);

        CSL citeproc = new CSL(sys, "ieee");
        citeproc.setOutputFormat("text");
        sys.registerCitationItems(citeproc);

        Bibliography b = citeproc.makeBibliography();
        assertEquals(2, b.getEntries().length);
        assertEquals("[1]J. Padhye, V. Firoiu, and D. Towsley, " +
                "\u201cA Stochastic Model of TCP Reno Congestion Avoidance and Control,\u201d " +
                "Univ. of Massachusetts, Amherst, MA, CMPSCI Tech. Rep. " +
                "99\u201302, 1999.\n", b.getEntries()[0]);
        assertEquals("[2]D. Middleton and A. D. Spaulding, " +
                "\u201cA Tutorial Review of Elements of Weak Signal Detection in Non-Gaussian EMI Environments,\u201d " +
                "National Telecommunications and Information Administration (NTIA), " +
                "U.S. Dept. of Commerce, NTIA Report 86\u2013194, May 1986.\n", b.getEntries()[1]);
    }

    /**
     * Test if LaTeX statements are handled correctly
     * @throws Exception if something goes wrong
     */
    @Test
    public void issue84() throws Exception {
        String entry = "@Article{,\n" +
                "  author = {Test},\n" +
                "  title  = {New information on \\textit{Riograndia guaibensis} Bonaparte,},\n" +
                "}";

        ByteArrayInputStream bais = new ByteArrayInputStream(
                entry.getBytes(StandardCharsets.UTF_8));

        BibTeXDatabase db = new BibTeXConverter().loadDatabase(bais);
        BibTeXItemDataProvider sys = new BibTeXItemDataProvider();
        sys.addDatabase(db);

        CSL citeproc = new CSL(sys, "ieee");
        citeproc.setOutputFormat("text");
        sys.registerCitationItems(citeproc);

        Bibliography bibl = citeproc.makeBibliography();
        for (String e : bibl.getEntries()) {
            assertEquals("[1]Test, \u201cNew information on Riograndia " +
                    "guaibensis Bonaparte,.\u201d", e.trim());
        }
    }
}
