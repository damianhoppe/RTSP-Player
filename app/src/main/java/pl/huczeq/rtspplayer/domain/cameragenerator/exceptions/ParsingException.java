package pl.huczeq.rtspplayer.domain.cameragenerator.exceptions;

public class ParsingException extends RuntimeException {

    private Error error;
    private String data;

    public ParsingException(Error error) {
        super(error.toString());
        this.error = error;
        this.data = null;
    }

    public ParsingException(Error error, String data) {
        super(error.toString());
        this.error = error;
        this.data = data;
    }

    public Error getError() {
        return this.error;
    }

    public String getData() {
        return data;
    }

    public enum Error {
        DATA_EMPTY,
        DATA_INCORRECT,
        FORMAT_NUMBER_ERROR,
        NUMBER_INTERVAL_ARRAY_ERROR,
        NUMBER_INTERVAL_ORDER_ERROR,
        FIRST_LENGHT_ARRAY_ERROR,
        NEGATIVE_NUMBER_ERROR,
        EXCEEDED_MAX_NUMBER_OF_VARIATIONS,
        EMPTY_EXPRESSION,
        DATA_EXPECTED,
        NAME_IS_TAKEN
    }
}
