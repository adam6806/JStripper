package com.github.adam6806.jstripper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Created by Adam on 8/26/2016.
 * Code Stripper for PPL
 */
public class JStripper {

    private static final long WAIT_TIME = 500;
    private static final double MS_CONVERSION = 1000.0;
    private final Level level;
    private final String inputFile;
    private final String outputPath;
    private final File outputFile;
    private int depth;
    private Logger logger;
    private ArrayList<File> inputFiles;

    /**
     * @param inputFile  Input file or directory to strip
     * @param outputPath Output path to write all stripped files to
     * @param logLevel   Log level to use for logger
     */
    public JStripper(String inputFile, String outputPath, String logLevel, int depth) {
        inputFiles = new ArrayList<>();
        switch (StringUtils.lowerCase(logLevel)) {
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
        this.outputPath = outputPath;
        this.depth = depth + 1;
        outputFile = new File(outputPath);
    }

    /**
     * @return the list of input files being processed
     */
    public final ArrayList<File> run() {
        System.out.println("Preparing to strip...");
        long startTime = System.currentTimeMillis();
        ExecutorService executor = Executors.newCachedThreadPool();
        System.out.println("Stripping files...");
        startThreads(inputFile, outputPath, depth, executor);
        executor.shutdown();
        // Wait until all threads are finish
        long waitTime = startTime;
        while (!executor.isTerminated()) {
            long currentTime = System.currentTimeMillis();
            if ((currentTime - waitTime) > WAIT_TIME) {
                System.out.println("Processing...");
                waitTime = currentTime;
            }
        }
        long stopTime = System.currentTimeMillis();
        logger.info("All threads complete!");
        System.out.println("Stripping complete! Stripped " + inputFiles.size() + " files. Stripped files are at " + outputPath + " See logs directory for details. Elapsed time: " + (stopTime - startTime) / MS_CONVERSION + " seconds");
        return inputFiles;
    }

    private void startThreads(String inputFile, String destPath, int reqDepth, ExecutorService executor) {
        reqDepth--;
        logger.info("Getting files in " + inputFile + " for processing.");
        File inputFileObj = new File(inputFile);
        if (inputFileObj.isDirectory()) {
            File[] listOfFiles = inputFileObj.listFiles();
            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        executeThread(file, destPath, executor);
                    } else try {
                        if (reqDepth != 0 && !file.getCanonicalPath().equals(outputFile.getCanonicalPath())) {
                            File dest = new File(destPath + file.getName());
                            if (!dest.exists()) {
                                dest.mkdir();
                            }
                            startThreads(file.getPath(), dest.getPath() + "\\", reqDepth, executor);
                        }
                    } catch (IOException e) {
                        logger.severe(ExceptionUtils.getStackTrace(e));
                        System.out.println("An error occurred copying folder structure for " + destPath);
                    }
                }
            }
        } else {
            executeThread(inputFileObj, destPath, executor);
        }
    }

    private void executeThread(File inputFile, String destPath, ExecutorService executor) {
        logger.info("Starting thread for " + inputFile.getName());
        StripperThread thread = new StripperThread(inputFile, level, destPath);
        executor.execute(thread);
        inputFiles.add(inputFile);
    }

    private void setupLogger() {
        logger = Logger.getLogger(JStripper.class.getName());
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
