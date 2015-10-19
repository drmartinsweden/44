package pl.ss.capstone.atmprotocol.common.parameter;

import java.math.BigInteger;

/**
 * Created by nulon on 08.10.15.
 */
public class PortArgument implements Argument<BigInteger> {

    private Argument<BigInteger> argument;

    public PortArgument(Argument<BigInteger> argument){
        this.argument = argument;
    }

    @Override
    public boolean isValid() {
        if (!argument.isValid()) {
            return false;
        }

        BigInteger value = argument.getValue();
        return value.longValue() >= 1024 && value.longValue() <= 65535;

    }

    @Override
    public BigInteger getValue() {
        return argument.getValue();
    }

    @Override
    public String getName() {
        return argument.getName();
    }
}
