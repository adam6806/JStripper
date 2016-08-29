/**
 * Created by Adam on 8/26/2016.
 * Main class for running CodeStripper
 */
public final class Main {

    private Main () {};

    public static void main(String[] args) {
        String inputFile = ".\\";
        if (args.length > 0) {
            inputFile = args[0];
        }
        CodeStripper stripper;
        if (args.length == 2) {
            stripper = new CodeStripper(inputFile, args[1]);
        } else {
            stripper = new CodeStripper(inputFile);
        }
        stripper.run();
    }
}
