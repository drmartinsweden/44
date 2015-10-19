package pl.ss.capstone.atmprotocol.common.parameter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nulon on 08.10.15.
 */
public class IPArgument implements Argument<String> {

    private final static String IP_REGEXP =
            "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private final static Pattern pattern = Pattern.compile(IP_REGEXP);

    private Argument<String> argument;

    public IPArgument(Argument<String> argument){
        this.argument = argument;
    }

    @Override
    public boolean isValid() {
        if (!argument.isValid()) {
            System.exit(255);
        }
        String trimmedValue = argument.getValue().trim();
        Matcher matcher = pattern.matcher(trimmedValue);
        if (matcher.matches()){
            return true;
        }else{
            System.err.println(String.format("%s does not match IP pattern",trimmedValue));
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
