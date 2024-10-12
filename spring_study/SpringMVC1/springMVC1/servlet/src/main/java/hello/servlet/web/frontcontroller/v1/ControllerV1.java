package hello.servlet.web.frontcontroller.v1;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface ControllerV1 {
    void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    //servlet - service 와 똑같은 모양의 컨트롤러 인터페이스를 만든다. 그리고 하위 클래스들은 이 인터페이스를 구현하면 된다.
}
