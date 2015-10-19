package pl.ss.capstone.atmprotocol.common.parameter;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nulon on 08.10.15.
 */
public class NumericArgument implements Argument<BigInteger> {

    private final static String NUMERIC_REGEXP = "(0|[1-9][0-9]*)";
    private final static Pattern pattern = Pattern.compile(NUMERIC_REGEXP);

    private Argument<String> argument;

    public NumericArgument(Argument<String> argument){
        this.argument = argument;
    }

    @Override
    public boolean isValid() {
        if (!argument.isValid()) {
            System.exit(255);
        }
        try{
            String trimmedValue = argument.getValue().trim();
            Matcher matcher = pattern.matcher(trimmedValue);
            if (matcher.matches()){
                BigInteger value = new BigInteger(trimmedValue);
                return (value.longValue() >= 0);
            }else{
                System.err.println(String.format("%s does not match %s",trimmedValue,NUMERIC_REGEXP));
                System.exit(255);
            }
        }catch(NumberFormatException ex){
            System.err.println(ex.getMessage());
            System.exit(255);
        }
        return false;
    }

    @Override
    public BigInteger getValue() {
        return new BigInteger(argument.getValue().trim());
    }

    @Override
    public String getName() {
        return argument.getName();
    }
}
