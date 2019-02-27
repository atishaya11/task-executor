package com.dscjss.taskexecutor.service;

import com.dscjss.taskexecutor.config.LangConfigProperties;
import com.dscjss.taskexecutor.model.Result;
import com.dscjss.taskexecutor.model.Task;
import com.dscjss.taskexecutor.util.Status;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.stereotype.Component;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@Component
@RunWith(SpringJUnit4ClassRunner.class)
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@TestPropertySource({"classpath:application.yml", "classpath:application.properties"})
@EnableConfigurationProperties(LangConfigProperties.class)
public class ExecutorCppTests {

    @Autowired
    private LangConfigProperties langConfigProperties;
    private Executor executor;
    @InjectMocks
    private Sender sender;

    @Before
    public void before(){
        executor = new Executor(sender, langConfigProperties);
    }

    private Task createTask() {
        Task task = new Task();
        task.setId(1);
        task.setLang("cpp");
        task.setInput("World");
        task.setSource("#include <iostream>\n" +
                "using namespace std;\n" +
                "int main () {\n" +
                "    char in[10];\n" +
                "    cin>>in;\n" +
                "    cout<<\"Hello \"<<in;\n" +
                "    return 0;\n" +
                "}");

        task.setTimeLimit(1000);
        return task;
    }

    @Test
    public void testExecute() {
        Task task = createTask();
        Result result = executor.execute(task);
        assertEquals("Hello World\n", result.getStdOut());
        assertEquals(0, result.getSignal());
    }

    private Task createTleTask() {
        Task task = new Task();
        task.setId(1);
        task.setLang("cpp");
        task.setInput("World");
        task.setSource("#include <iostream>\n" +
                "using namespace std;\n" +
                "int main () {\n" +
                "    char in[10];\n" +
                "    cin>>in;\n" +
                "    while(1) \n" +
                "    cout<<\"Hello \"<<in;\n" +
                "    return 0;\n" +
                "}");

        task.setTimeLimit(1000);
        return task;
    }

    @Test
    public void testExecute_when_timeLimitExceeded(){
        Task task = createTleTask();
        Result result = executor.execute(task);
        assertEquals(124, result.getSignal());
    }

    private Task createLargeOutputTask() {
        Task task = new Task();
        task.setId(1);
        task.setLang("cpp");
        task.setInput("World");
        task.setSource("#include <iostream>\n" +
                "using namespace std;\n" +
                "int main () {\n" +
                "    char in[10];\n" +
                "    cin>>in;\n" +
                "    while(1) \n" +
                "    cout<<\"HelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHelloHello \"<<in;\n" +
                "    return 0;\n" +
                "}");

        task.setTimeLimit(2000);
        return task;
    }

    @Test
    public void testExecute_when_largeOutput(){
        Task task = createLargeOutputTask();
        Result result = executor.execute(task);
        assertEquals(124, result.getSignal());
        assertEquals(Status.RUNTIME_ERROR, result.getStatus());
    }
}

