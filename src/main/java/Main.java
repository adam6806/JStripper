/**
 * Created by Adam on 8/26/2016.
 */
public final class Main {

    private Main () {};

    public static void main(String[] args) {
        String fileName = args[0];
        CodeStripper stripper;
        if (args.length == 2) {
            stripper = new CodeStripper(fileName, args[1]);
        } else {
            stripper = new CodeStripper(fileName);
        }
        stripper.run();
    }
}
