package de.undercouch.citeproc;

import de.undercouch.citeproc.csl.CSLItemData;
import de.undercouch.citeproc.output.Bibliography;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class FixturesTest {
    private static final String FIXTURES_DIR = "/fixtures";

    /**
     * The current test to run
     */
    private File testFile;

    /**
     * {@code true} if the test should be run in experimental mode
     */
    private boolean experimentalMode;

    /**
     * Get all test files
     */
    @Parameterized.Parameters(name = "{0}, {1}")
    public static Iterable<Object[]> data() {
        URL fixturesUrl = CSL.class.getResource(FIXTURES_DIR);
        File fixturesDir = new File(fixturesUrl.getPath());

        // noinspection ConstantConditions
        return Arrays.stream(fixturesDir.listFiles((dir, name) -> name.endsWith(".yaml")))
                .flatMap(f -> Stream.of(
                        new Object[] { f.getName(), true },
                        new Object[] { f.getName(), false }
                ))
                .collect(Collectors.toList());
    }

    /**
     * Create a new test
     * @param name the name of the test file
     * @param experimentalMode {@code true} if the test should be run in
     * experimental mode
     */
    public FixturesTest(String name, boolean experimentalMode) {
        URL fixturesUrl = CSL.class.getResource(FIXTURES_DIR);
        File fixturesDir = new File(fixturesUrl.getPath());
        testFile = new File(fixturesDir, name);
        this.experimentalMode = experimentalMode;
    }

    /**
     * Run a test from the test suite
     * @throws IOException if an I/O error occurred
     */
    @Test
    @SuppressWarnings("unchecked")
    public void run() throws IOException {
        Map<String, Object> data;
        Yaml yaml = new Yaml();
        try (FileInputStream is = new FileInputStream(testFile)) {
            data = yaml.loadAs(is, Map.class);
        }

        String style = (String)data.get("style");
        String expectedResult = (String)data.get("result");

        // convert item data
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>)data.get("items");
        CSLItemData[] items = new CSLItemData[rawItems.size()];
        for (int i = 0; i < items.length; ++i) {
            items[i] = CSLItemData.fromJson(rawItems.get(i));
        }

        // create CSL processor
        ListItemDataProvider itemDataProvider = new ListItemDataProvider(items);
        CSL citeproc = new CSL(itemDataProvider, style, experimentalMode);
        citeproc.setOutputFormat("text");

        // register citation items
        for (CSLItemData item : items) {
            citeproc.registerCitationItems(item.getId());
        }

        // make bibliography
        Bibliography bibl = citeproc.makeBibliography();

        // compare result
        String actualResult = bibl.makeString();
        assertEquals(expectedResult, actualResult);
    }
}
