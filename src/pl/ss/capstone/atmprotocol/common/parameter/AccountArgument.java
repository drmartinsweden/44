package pl.ss.capstone.atmprotocol.common.parameter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nulon on 08.10.15.
 */
public class AccountArgument implements Argument<String> {

    private final static String ACCOUNT_REGEXP = "[_\\\\-\\\\.0-9a-z]{1,250}";
    private final static Pattern pattern = Pattern.compile(ACCOUNT_REGEXP);

    private Argument<String> argument;

    public AccountArgument(Argument<String> argument){
        this.argument = argument;
    }

    @Override
    public boolean isValid() {
        if (!argument.isValid()) {
            System.exit(255);
        }

        String trimmedValue = argument.getValue().trim();
        if (trimmedValue.length() < 1 || trimmedValue.length() > 250){
            System.err.println(String.format("%s does not have length between 1-250 characters",trimmedValue));
            System.exit(255);
        }
        Matcher matcher = pattern.matcher(trimmedValue);
        if (matcher.matches()) {
            return true;
        } else {
            System.err.println(String.format("%s does not match %s",trimmedValue,ACCOUNT_REGEXP));
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
