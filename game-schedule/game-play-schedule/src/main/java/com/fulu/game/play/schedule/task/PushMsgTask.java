package com.fulu.game.play.schedule.task;

import com.fulu.game.core.entity.PushMsg;
import com.fulu.game.core.service.PushMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class PushMsgTask {

    @Autowired
    private PushMsgService pushMsgService;

    /**
     *  推送消息
     */
    @Scheduled(cron = "0 0/1 * * * ? ")  //cron接受cron表达式，根据cron表达式确定定时规则
    public void autoCompleteOrder() {
       List<PushMsg> pushMsgList = pushMsgService.findTodayNotPushMsg();
           for(PushMsg pushMsg :pushMsgList ){
               log.info("查询到今天未执行的消息推送:{}",pushMsg);
               try {
                   pushMsgService.appointPush(pushMsg);
               }catch (Exception e){
                   log.error("推送任务出错:pushMsg:{};{}",pushMsg,e.getMessage());
               }
           }
    }

}
