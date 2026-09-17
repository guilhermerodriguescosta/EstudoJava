package endpoint.exception;

public class NumeroForaDoLimiteException extends RuntimeException {
    public NumeroForaDoLimiteException() {
        super("Os números devem estar entre -1000000 e 1000000.");
    }
}
