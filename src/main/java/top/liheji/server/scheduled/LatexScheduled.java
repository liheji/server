package top.liheji.server.scheduled;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.liheji.server.service.LatexAccountService;

/**
 * @author : Galaxy
 * @time : 2023/2/4 23:42
 * @create : IdeaJ
 * @project : gulimall
 * @description : 秒杀商品上架任务
 */
@Component
public class LatexScheduled {

    @Autowired
    LatexAccountService latexAccountService;

    @Async
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateLatexAccountStatus() {
        latexAccountService.updateLatexAccountStatus();
    }
}
