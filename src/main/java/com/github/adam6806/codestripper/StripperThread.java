package com.github.adam6806.codestripper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Adam on 8/29/2016.
 * Runnable class for stripping individual files
 */
public class StripperThread implements Runnable {

    private final String outputPath;
    private final ArrayList<String> outputFile;
    private final File file;
    private Logger logger;
    private boolean lastFwdSlash;
    private boolean lastBackSlash;
    private boolean lastStar;
    private boolean currLineComment;
    private boolean currBlockComment;
    private boolean currQuote;
    private boolean currCharLiteral;
    private boolean overrideAppend;
    private ArrayList<String> inputFile;

    /**
     * @param file       the file to be processed
     * @param logLevel   the log level to be used for the logger
     * @param outputPath the output path to write the processed file to
     */
    public StripperThread(File file, Level logLevel, String outputPath) {
        this.file = file;
        setupLogger();
        logger.setLevel(logLevel);
        setCharFlagsFalse();
        currLineComment = false;
        currBlockComment = false;
        currQuote = false;
        currCharLiteral = false;
        inputFile = new ArrayList<>();
        outputFile = new ArrayList<>();
        this.outputPath = outputPath;
    }

    /**
     * Runs the stripper thread
     */
    @Override
    public void run() {
        logger.info("Running...");
        System.out.println("Processing file " + file.getName() + "...");
        try {
            readInputFile(file);
            logger.info("Parsing file...");
            inputFile.forEach(this::parseLine);
            logger.info("Parsing file complete.");
            writeOutputFile();
            System.out.println("Processing file " + file.getName() + " complete.");
        } catch (Exception e) {
            System.out.println("An error occurred processing file " + file.getName() + ". See logs for details.");
            logger.severe(ExceptionUtils.getStackTrace(e));
        }
    }

    private void readInputFile(File file) throws Exception {
        logger.info("Reading input file " + file.getName() + "...");
        try (Stream<String> stream = Files.lines(file.toPath())) {
            inputFile = stream.collect(Collectors.toCollection(ArrayList::new));
        } catch (UncheckedIOException e) {
            logger.severe(ExceptionUtils.getStackTrace(e));
            throw new Exception(file.getName() + "is not a valid file for processing.");
        } catch (IOException e) {
            logger.severe(ExceptionUtils.getStackTrace(e));
            throw new Exception("An error occured while reading the input file " + file.getName() + ". Please see log for details.");
        }
        logger.info("Reading input file complete.");
    }

    private void parseLine(String line) {
        logger.fine("Parsing next line...");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            logger.fine("Char: " + c);
            evaluateChar(c, stringBuilder);

            if (currLineComment) {
                break;
            } else if (!currBlockComment && !overrideAppend) {
                stringBuilder.append(c);
            }


            setCharFlagsFalse();
            parseChar(c);
        }
        currLineComment = false;
        currQuote = false;
        currCharLiteral = false;
        setCharFlagsFalse();
        String newLine = stringBuilder.append("\n").toString();
        if (StringUtils.isNotBlank(newLine)) {
            outputFile.add(newLine);
        }
    }

    private void evaluateChar(char c, StringBuilder stringBuilder) {
        if (c == '/') {
            if (lastFwdSlash && !currQuote && !currCharLiteral) {
                logger.fine("Inline comment start");
                currLineComment = true;
                deleteLastChar(stringBuilder);
            } else if (lastStar && currBlockComment) {
                logger.fine("Block comment end");
                currBlockComment = false;
                overrideAppend = true;
            }
        } else if (c == '"') {
            if (!lastBackSlash && !currLineComment && !currBlockComment) {
                currQuote = !currQuote;
                logger.fine("Quotation toggled to " + currQuote);
            }
        } else if (c == '*') {
            if (lastFwdSlash && !currLineComment && !currQuote && !currCharLiteral) {
                if (!currBlockComment) {
                    logger.fine("Block comment start");
                    currBlockComment = true;
                    deleteLastChar(stringBuilder);
                }
            }
        } else if (c == '\'') {
            if (!currQuote && !currLineComment && !currBlockComment) {
                currCharLiteral = !currCharLiteral;
                logger.fine("Char literal toggled to " + currCharLiteral);
            }
        }
    }

    private void deleteLastChar(StringBuilder sb) {
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
    }


    private void parseChar(char c) {
        switch (c) {
            case '\\':
                lastBackSlash = true;
                break;
            case '/':
                lastFwdSlash = true;
                break;
            case '*':
                lastStar = true;
        }
    }

    private void writeOutputFile() {
        logger.info("Writing output file...");
        try {
            if (outputFile.size() > 0) {
                String lastLine = outputFile.get(outputFile.size() - 1);
                String substring = lastLine.substring(0, lastLine.length() - 1);
                outputFile.set(outputFile.size() - 1, substring);
            }
            File newFile = new File(outputPath + file.getName());
            FileUtils.writeLines(newFile, "UTF-8", outputFile, "");
        } catch (IOException e) {
            logger.severe(ExceptionUtils.getStackTrace(e));
            System.out.println("An error occured while writing the output file. Please see log for details.");
        }
        logger.info("Writing output file complete.");
    }

    private void setupLogger() {
        logger = Logger.getLogger(StripperThread.class.getName() + "-" + file.getName());
        FileHandler fh;

        try {
            File logDir = new File(".\\logs\\");
            if (!logDir.exists()) {
                if (!logDir.mkdirs()) {
                    throw new IOException("Error occured creating log directory!");
                }
            }
            fh = new FileHandler(".\\logs\\" + file.getName() + "-log.txt");
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

    private void setCharFlagsFalse() {
        lastFwdSlash = false;
        lastBackSlash = false;
        lastStar = false;
        overrideAppend = false;
    }
}
