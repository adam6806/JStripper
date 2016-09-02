package com.github.adam6806.jstripper;

import org.apache.commons.cli.*;

/**
 * Created by Adam on 8/26/2016.
 * Main class for running JStripper
 */
public final class Main {

    private static String logLevel = "info";
    private static String inputPath = ".\\";
    private static String outputPath = ".\\output\\";
    private static String depth = "0";

    private Main() {
    }

    /**
     * @param args Arguments passed
     */
    public static void main(String[] args) {

        Options options = createOptions();
        HelpFormatter formatter = new HelpFormatter();

        // create the command line parser
        CommandLineParser parser = new DefaultParser();

        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);

            if (line.hasOption('h') || line.hasOption("help")) {
                String header = "Strips blank lines and comments from java source files.\n\n";
                String footer = "\nPlease report issues at https://github.com/adam6806/JStripper";
                formatter.printHelp("JStripper", header, options, footer, true);
                System.exit(0);
            }
            inputPath = line.getOptionValue('i', inputPath);
            inputPath = line.getOptionValue("input", inputPath);
            outputPath = line.getOptionValue('o', outputPath);
            outputPath = line.getOptionValue("output", outputPath);
            logLevel = line.getOptionValue('l', logLevel);
            logLevel = line.getOptionValue("loglevel", logLevel);
            depth = line.getOptionValue('d', depth);
            depth = line.getOptionValue("depth", depth);
            JStripper stripper = new JStripper(inputPath, outputPath, logLevel, Integer.valueOf(depth));
            stripper.run();
        } catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
        }
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption(Option.builder("h")
                .longOpt("help")
                .hasArg(false)
                .required(false)
                .desc("prints this message")
                .build());
        options.addOption(Option.builder("i")
                .longOpt("input")
                .hasArg()
                .argName("Path")
                .optionalArg(false)
                .desc("the directory or file path to process")
                .build());
        options.addOption(Option.builder("o")
                .longOpt("output")
                .hasArg()
                .argName("Path")
                .optionalArg(true)
                .desc("the directory to put processed files in")
                .build());
        options.addOption(Option.builder("l")
                .longOpt("loglevel")
                .hasArg()
                .argName("Level")
                .optionalArg(true)
                .desc("the log level to use, valid options are \"info\", \"fine\", and \"severe\"")
                .build());
        options.addOption(Option.builder("d")
                .longOpt("depth")
                .hasArg()
                .argName("Integer")
                .optionalArg(true)
                .desc("the number of levels to explore down with -1 being infinite")
                .build());
        return options;
    }
}
