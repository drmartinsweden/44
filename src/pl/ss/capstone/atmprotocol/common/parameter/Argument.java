package pl.ss.capstone.atmprotocol.common.parameter;

/**
 * Created by nulon on 08.10.15.
 */
public interface Argument<T> {
    boolean isValid();
    T getValue();
    String getName();
}
