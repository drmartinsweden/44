package pl.ss.capstone.atmprotocol.common.parameter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nulon on 08.10.15.
 */
public class FilenameArgument implements Argument<String> {

    private final static String FILENAME_REGEXP = "[_\\-\\.0-9a-z]{1,255}";
    private final static Pattern pattern = Pattern.compile(FILENAME_REGEXP);

    private Argument<String> argument;

    public FilenameArgument(Argument<String> argument){
        this.argument = argument;
    }

    @Override
    public boolean isValid() {
        if (!argument.isValid()) {
            System.exit(255);
        }

        String trimmedValue = argument.getValue().trim();
        if (trimmedValue.length() < 1 || trimmedValue.length() > 255){
            System.err.println(String.format("%s does not have length between 1-255 characters",trimmedValue));
            System.exit(255);
        }
        if (trimmedValue.equals(".") || trimmedValue.equals("..")){
            System.err.println(String.format("%s is one of the special filenames",trimmedValue));
            System.exit(255);
        }
        Matcher matcher = pattern.matcher(trimmedValue);
        if (matcher.matches()) {
            return true;
        } else {
            System.err.println(String.format("%s does not match %s",trimmedValue,FILENAME_REGEXP));
            System.exit(255);
        }
        return false;
    }

    @Override
    public String getValue() {
        return argument.getValue().trim();
    }

    @Override
    public String getName() {
        return argument.getName();
    }
}
