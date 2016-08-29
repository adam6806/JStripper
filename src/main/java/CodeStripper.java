import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Adam on 8/26/2016.
 */
public class CodeStripper {

    private Logger logger;
    private boolean lastFwdSlash;
    private boolean lastBackSlash;
    private boolean lastQuote;
    private boolean lastStar;
    private boolean currLineComment;
    private boolean currBlockComment;
    private boolean currQuote;
    private boolean currCharLiteral;
    private boolean overrideAppend;
    private ArrayList<String> inputFile;
    private ArrayList<String> outputFile;
    private String inputFileName;

    public CodeStripper(String fileName) {
        setupLogger();
        logger.setLevel(Level.INFO);
        setCharFlagsFalse();
        currLineComment = false;
        currBlockComment = false;
        currQuote = false;
        currCharLiteral = false;
        inputFile = new ArrayList();
        outputFile = new ArrayList();
        inputFileName = fileName;
    }

    public CodeStripper(String filename, String logLevel) {
        this(filename);
        logLevel = StringUtils.lowerCase(logLevel);
        switch(logLevel) {
            case "severe":
                logger.setLevel(Level.SEVERE);
                break;
            case "fine":
                logger.setLevel(Level.FINE);
                break;
            default:
                logger.setLevel(Level.INFO);
        }
    }

    public void run() {
        logger.info("Running...");
        System.out.println("Processing file " + inputFileName + "...");
        readInputFile(inputFileName);
        logger.info("Parsing file...");
        for (String line : inputFile) {
            parseLine(line);
        }
        logger.info("Parsing file complete.");
        writeOutputFile();
        System.out.println("Processing file complete. Output file is in the output directory. See log for details.");
    }

    private void readInputFile(String fileName) {
        logger.info("Reading input file " + fileName + "...");
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            inputFile = stream.collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException e) {
            logger.severe(ExceptionUtils.getStackTrace(e));
            System.out.println("An error occured while reading the input file. Please see log for details.");
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

            if(currLineComment == false && currBlockComment == false && overrideAppend == false) {
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
            if(lastFwdSlash && !currQuote && !currCharLiteral) {
                logger.fine("Inline comment start");
                currLineComment = true;
                deleteLastChar(stringBuilder);
            } else if (lastStar && currBlockComment) {
                logger.fine("Block comment end");
                currBlockComment = false;
                overrideAppend = true;
            }
        } else if (c == '"') {
            if(!lastBackSlash && !currLineComment && !currBlockComment) {
                currQuote = !currQuote;
                logger.fine("Quotation toggled to " + currQuote);
            }
        } else if (c == '*') {
            if(lastFwdSlash && !currLineComment && !currQuote && !currCharLiteral) {
                if(!currBlockComment) {
                    logger.fine("Block comment start");
                    currBlockComment = true;
                    deleteLastChar(stringBuilder);
                }
            }
        } else if (c == '\'') {
            if(!currQuote && !currLineComment && !currBlockComment) {
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

    private void setCharFlagsFalse() {
        lastFwdSlash = false;
        lastBackSlash = false;
        lastQuote = false;
        lastStar = false;
        overrideAppend = false;
    }

    private void parseChar(char c) {
        switch (c) {
            case '\\':
                lastBackSlash = true;
                break;
            case '/':
                lastFwdSlash = true;
                break;
            case '"':
                lastQuote = true;
                break;
            case '*':
                lastStar = true;
        }
    }

    private void writeOutputFile() {
        logger.info("Writing output file...");
        try {
            if(outputFile.size() > 0) {
                String lastLine = outputFile.get(outputFile.size() - 1);
                String substring = lastLine.substring(0, lastLine.length() - 1);
                outputFile.set(outputFile.size() - 1, substring);
            }

            String outputFileName = inputFileName.replace("in", "out");
            FileUtils.writeLines(new File("./output/" + outputFileName), "UTF-8", outputFile, "");
        } catch (IOException e) {
            logger.severe(ExceptionUtils.getStackTrace(e));
            System.out.println("An error occured while writing the output file. Please see log for details.");
        }
        logger.info("Writing output file complete.");
    }

    private void setupLogger() {
        logger = Logger.getLogger(CodeStripper.class.getName());
        FileHandler fh;

        try {
            File file = new File(".\\logs\\");
            if (!file.exists()) {
                if(file.mkdirs() == false) {
                    throw new IOException("Error occured creating log directory!");
                }
            }
            fh = new FileHandler(".\\logs\\Log.txt");
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

    @Override
    public String toString() {
        return "CodeStripper{" +
                "lastFwdSlash=" + lastFwdSlash +
                ", lastBackSlash=" + lastBackSlash +
                ", lastQuote=" + lastQuote +
                ", lastStar=" + lastStar +
                ", currQuote=" + currQuote +
                ", currLineComment=" + currLineComment +
                ", currBlockComment=" + currBlockComment +
                '}';
    }
}
