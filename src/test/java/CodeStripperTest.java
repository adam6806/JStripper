import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * Created by Adam on 8/27/2016.
 */
public class CodeStripperTest {

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
        CodeStripper stripper = new CodeStripper(".\\input\\", "fine");
        ArrayList<File> files = stripper.run();
        for (File inputFile : files) {
            if (inputFile.getName().endsWith(".txt")) {
                String outputFileName = inputFile.getName();
                File outputFile = new File(".\\output\\" + outputFileName);
                File verifiedOutputFile = new File(".\\verifiedoutput\\" + outputFileName);
                boolean areEqual = FileUtils.contentEquals(verifiedOutputFile, outputFile);
                assertTrue("Output file did not match for " + outputFileName, areEqual);
            }
        }
    }
}