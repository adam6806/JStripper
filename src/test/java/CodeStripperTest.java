import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.File;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * Created by Adam on 8/27/2016.
 * CodeStripper test for testing that output files match verified output files
 */
public class CodeStripperTest {

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void run() throws Exception {
        File output = new File(".\\output\\");
        if (output.exists()) {
            FileUtils.deleteDirectory(output);
        }
        File logs = new File(".\\logs\\");
        if (logs.exists()) {
            FileUtils.deleteDirectory(logs);
        }
        CodeStripper stripper = new CodeStripper(".\\input\\", ".\\output\\", "fine");
        ArrayList<File> files = stripper.run();
        for (File inputFile : files) {
            if (inputFile.getName().endsWith(".txt")) {
                String outputFileName = inputFile.getName();
                File outputFile = new File(".\\output\\" + outputFileName);
                String verifiedOutputFileName = inputFile.getName().replace("in", "out");
                File verifiedOutputFile = new File(".\\verifiedoutput\\" + verifiedOutputFileName);
                boolean areEqual = FileUtils.contentEquals(verifiedOutputFile, outputFile);
                collector.checkThat("Output file for " + outputFileName + " did not match for verified output file " + verifiedOutputFileName, areEqual, equalTo(true));
            }
        }
    }
}