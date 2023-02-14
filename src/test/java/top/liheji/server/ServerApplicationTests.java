package top.liheji.server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.liheji.server.service.AccountService;

@SpringBootTest
class ServerApplicationTests {

    @Autowired
    private AccountService accountService;

    @Test
    void contextLoads() {
    }
}
