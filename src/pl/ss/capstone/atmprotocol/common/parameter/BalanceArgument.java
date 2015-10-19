package pl.ss.capstone.atmprotocol.common.parameter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by nulon on 08.10.15.
 */
public class BalanceArgument implements Argument<BigDecimal> {

    private final static String BALANCE_REGEXP = "(0|[1-9][0-9]*)\\.[0-9]{2}";
    private final static Pattern pattern = Pattern.compile(BALANCE_REGEXP);

    private Argument<String> argument;

    private final static DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance();
    private final static String MAX_BALANCE = "4294967295.99";
    private static BigDecimal maxBalance;
    {
        String pattern = "0.00";

        df.applyPattern(pattern);
        df.setParseBigDecimal(true);
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
        otherSymbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(otherSymbols);

        try {
            maxBalance = (BigDecimal)df.parse(MAX_BALANCE);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    public BalanceArgument(Argument<String> argument){
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
            if (matcher.matches()) {
                BigDecimal value = (BigDecimal) df.parse(trimmedValue);
                return (value.doubleValue() > 0.0 && value.compareTo(maxBalance) <= 0);
            }else{
                System.exit(255);
            }
        }catch(Exception ex){
            System.err.println(ex.getMessage());
            System.exit(255);
        }
        return false;
    }

    @Override
    public BigDecimal getValue() {
        String pattern = "0.00";
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance();
        df.setDecimalFormatSymbols(otherSymbols);
        df.applyPattern(pattern);
        df.setParseBigDecimal(true);
        BigDecimal val = null;
        try {
            val = (BigDecimal)df.parse(argument.getValue().trim());
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            System.exit(255);
        }
        return val;
    }

    @Override
    public String getName() {
        return argument.getName();
    }
}
