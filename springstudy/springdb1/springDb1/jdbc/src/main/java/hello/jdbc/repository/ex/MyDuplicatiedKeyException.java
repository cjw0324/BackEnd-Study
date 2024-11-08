package hello.jdbc.repository.ex;

public class MyDuplicatiedKeyException extends MyDbException {
    public MyDuplicatiedKeyException() {
    }

    public MyDuplicatiedKeyException(String message) {
        super(message);
    }

    public MyDuplicatiedKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDuplicatiedKeyException(Throwable cause) {
        super(cause);
    }

    public MyDuplicatiedKeyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
