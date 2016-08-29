import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * Created by Adam on 8/27/2016.
 */
public class CodeStripperTest {

    private ArrayList<File> inputFiles;

    @Before
    public void setUp() throws Exception {
        inputFiles = new ArrayList();
        File folder = new File(".\\");
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith("txt")) {
                inputFiles.add(listOfFiles[i].getAbsoluteFile());
            }
        }
    }

    @Test
    public void run() throws Exception {

        for (File inputFile : inputFiles) {
            CodeStripper stripper = new CodeStripper(inputFile.getName(), "fine");
            stripper.run();
            String outputFileName = inputFile.getName().replace("in", "out");
            File outputFile = new File(".\\output\\" + outputFileName);
            File verifiedOutputFile = new File(".\\verifiedoutput\\" + outputFileName);
            boolean areEqual = FileUtils.contentEquals(verifiedOutputFile, outputFile);
            assertTrue("Output file did not match for " + outputFileName,areEqual);
        }
    }

}