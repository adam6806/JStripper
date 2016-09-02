# JStripper

School project for stripping line comments, multi-line comments, and empty lines from Java code files.

## Use

Run java -jar with executable jar file.
<pre>
usage: JStripper [-d <Integer>] [-h] [-i <Path>] [-l <Level>] [-o <Path>]
Strips blank lines and comments from java source files.

 -d,--depth <Integer>    the number of levels to explore down with -1
                         being infinite
 -h,--help               prints this message
 -i,--input <Path>       the directory or file path to process
 -l,--loglevel <Level>   the log level to use, valid options are "info",
                         "fine", and "severe"
 -o,--output <Path>      the directory to put processed files in


</pre>
