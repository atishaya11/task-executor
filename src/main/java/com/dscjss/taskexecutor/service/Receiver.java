package com.dscjss.taskexecutor.service;

import com.dscjss.taskexecutor.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "${queues.receive-task}")
public class Receiver {

    private final Executor executor;

    private static final Logger logger = LoggerFactory.getLogger(Receiver.class);


    @Autowired
    public Receiver(Executor executor) {
        this.executor = executor;
    }


    @RabbitHandler
    public void receiveMessage(Task task) {
        logger.info("Task with task content {} received and sent for execution", task);
        executor.executeAndSendResult(task);
    }

   /* docker run  --cpus=${1} --memory=${2} \
            --ulimit nofile=64:64 \
            --rm    \
            --read-only \
            -v /usr/execute/box/${3}:/usr/src/runbox   \
            -w /usr/src/runbox atishaya/judge-worker-${4}  \
    bash -c "/bin/compile.sh && /bin/run.sh ${5}"*/
}
