import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static java.lang.System.currentTimeMillis;

/**
 * Created by Adam on 8/26/2016.
 * Code Stripper for PPL
 */
public class CodeStripper {

    private Logger logger;
    private ArrayList<File> inputFiles;
    private Level level;
    private String inputFile;

    public CodeStripper(String inputFile) {
        this(inputFile, "info");
    }

    public CodeStripper(String inputFile, String logLevel) {
        logLevel = StringUtils.lowerCase(logLevel);
        switch(logLevel) {
            case "severe":
                level = Level.SEVERE;
                break;
            case "fine":
                level = Level.FINE;
                break;
            default:
                level = Level.INFO;
        }
        setupLogger();
        logger.setLevel(level);
        this.inputFile = inputFile;
    }

    public ArrayList<File> run() {
        System.out.println("Preparing to strip...");
        long startTime = currentTimeMillis();
        getFiles(inputFile);
        System.out.println("Stripping " + inputFiles.size() + " files...");
        ExecutorService executor = Executors.newCachedThreadPool();
        for (File file : inputFiles) {
            logger.info("Starting thread for " + file.getName());
            StripperThread thread = new StripperThread(file, level);
            executor.execute(thread);
        }
        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated()) {
        }
        long stopTime = System.currentTimeMillis();
        logger.info("All threads complete!");
        System.out.println("Stripping complete! Stripped files are in the output directory. See logs directory for details. Elapsed time: " + (stopTime - startTime) / 1000.0 + " seconds");
        return inputFiles;
    }

    private void getFiles(String inputFile) {
        logger.info("Getting files in " + inputFile + " for processing.");
        inputFiles = new ArrayList();
        File inputFileObj = new File(inputFile);
        if (inputFileObj.isDirectory()) {
            File[] listOfFiles = inputFileObj.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    inputFiles.add(listOfFiles[i]);
                }
            }
        } else {
            inputFiles.add(inputFileObj);
        }
    }

    private void setupLogger() {
        logger = Logger.getLogger(CodeStripper.class.getName());
        FileHandler fh;

        try {
            File file = new File(".\\logs\\");
            if (!file.exists()) {
                if (!file.mkdirs()) {
                    throw new IOException("Error occured creating log directory!");
                }
            }
            fh = new FileHandler(".\\logs\\MainLog.txt");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.setUseParentHandlers(false);
        } catch (SecurityException | IOException e) {
            System.out.println("An error occured while initializing logging.");
            e.printStackTrace();
        }

        logger.info("Initializing...");
    }
}
