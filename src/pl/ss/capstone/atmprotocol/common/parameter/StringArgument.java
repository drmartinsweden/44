package pl.ss.capstone.atmprotocol.common.parameter;

/**
 * This class verifies whether a generic string argument is valid.
 * Its length should not exceed 4096 characters.
 */
public class StringArgument implements Argument<String> {

    private String value;
    private String name;

    public StringArgument(String name, String value){
        this.name = name;
        this.value = value;
    }

    @Override
    public boolean isValid() {
        //Lowered by 2 as argument name and - are removed
        return value.length() <= 4094 ;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }
}
