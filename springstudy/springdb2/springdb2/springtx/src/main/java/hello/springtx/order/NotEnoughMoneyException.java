package hello.springtx.order;

public class NotEnoughMoneyException extends Exception {
    //Exception 을 상속 받았기 때문에 체크 예외
    //해당 예외는 롤백 하지 않고 커밋 하고 싶다.
    public NotEnoughMoneyException(String message) {
        super(message);
    }
}
