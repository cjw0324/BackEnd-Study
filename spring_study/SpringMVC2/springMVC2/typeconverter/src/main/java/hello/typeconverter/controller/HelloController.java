package hello.typeconverter.controller;

import hello.typeconverter.type.IpPort;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

@RestController
public class HelloController {
    @GetMapping("/hello-v1")
    public String helloV1(HttpServletRequest request) {
        String data = request.getParameter("data");
        Integer intValue = Integer.valueOf(data);
        System.out.println("int value = " + intValue);
        return "ok";
    }

    @GetMapping("/hello-v2")
    public String helloV2(@RequestParam Integer data) {
        System.out.println("data = " + data);
        return "ok";
    }

    @GetMapping("/hello-my1")
    public String helloMy1(@ModelAttribute UserData data) {
        System.out.println("data = " + data.getData());
        return "ok";
    }

    @GetMapping("/ip-port")
    public String stringToIpPort(@RequestParam IpPort ipPort) {
        System.out.println("ip = " + ipPort.getIp());
        System.out.println("port = " + ipPort.getPort());
        return "ok";
    }

    @Data
    class UserData {
        Integer data;
    }
}
