package com.dscjss.taskexecutor.service;


import com.dscjss.taskexecutor.config.LangConfigProperties;
import com.dscjss.taskexecutor.model.Details;
import com.dscjss.taskexecutor.model.Result;
import com.dscjss.taskexecutor.model.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes= LangConfigProperties.class)
@TestPropertySource(properties = { "lang.map.java8.source_file=Main.java" })
public class ExecutorJava8Tests {

    private static LangConfigProperties langConfigProperties = new LangConfigProperties();
    static {
        Details details = new Details();
        details.setSourceFile("Main.java");
        details.setCpuShare("1.2");
        details.setMemLimit("500m");
        langConfigProperties.getMap().put("java8", details);
    }
    @InjectMocks
    private Sender sender;
    private Executor executor = new Executor(sender, langConfigProperties);

    private Task createTask(){
        Task task = new Task();
        task.setId(1);
        task.setLang("java8");
        task.setInput("Hello World");
        task.setSource("import java.util.*;\n" +
                "public class Main{\n" +
                "\n" +
                "    public static void main(String[] args) {\n" +
                "        Scanner in = new Scanner(System.in);\n" +
                "        String s = in.nextLine();\n" +
                "        System.out.println(s);\n" +
                "    }\n" +
                "}");

        task.setTimeLimit(1000);
        return task;
    }

    @Test
    public void testExecute(){
        Task task = createTask();
        Result result = executor.execute(task);
        assertEquals("Hello World", result.getStdOut());
    }

}
