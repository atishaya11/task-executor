package com.dscjss.taskexecutor.service;


import com.dscjss.taskexecutor.model.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Sender {


    private final RabbitTemplate rabbitTemplate;
    private final Logger logger = LoggerFactory.getLogger(Sender.class);

    @Value("${rabbitmq.exchange}")
    private static String EXCHANGE;

    @Value("${rabbitmq.routing-key}")
    private static String ROUTING_KEY;


    @Autowired
    public Sender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }


    public void send(Result result){
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, result,  m -> {
            m.getMessageProperties().getHeaders().remove("__TypeId__");
            return m;
        });
        logger.info("Task executed and result {} sent to the success queue", result);
    }
}
