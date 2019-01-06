package com.dscjss.taskexecutor.service;


import com.dscjss.taskexecutor.util.Shell;
import com.dscjss.taskexecutor.util.Status;
import com.dscjss.taskexecutor.config.LangConfigProperties;
import com.dscjss.taskexecutor.model.Details;
import com.dscjss.taskexecutor.model.Result;
import com.dscjss.taskexecutor.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class Executor {

    private final Logger logger = LoggerFactory.getLogger(Executor.class);

    private final String EXECUTION_BOX_DIR = "/usr/execute/box";
    private final String FILE_NAME_INPUT = "run.stdin";
    private final String FILE_NAME_OUTPUT = "run.stdout";
    private final String FILE_NAME_ERROR = "run.stderr";
    private final String FILE_NAME_COMPILE_ERROR = "compile.stderr";
    private final String FILE_NAME_EXECUTION_TIME = "run.time";
    private final String FILE_PATH_EXECUTION_SCRIPT = "/home/atishaya/IntellijProjects/coding-platform-apps/task-executor/src/main/resources/script.sh";

    private Sender sender;

    private LangConfigProperties langConfigProperties;

    @Autowired
    public Executor(Sender sender, LangConfigProperties langConfigProperties) {
        this.sender = sender;
        this.langConfigProperties = langConfigProperties;
    }

    public void executeAndSendResult(Task task) {
        Result result = execute(task);
        sender.send(result);
    }

    public Result execute(Task task){
        Result result = new Result(task.getId());

        File taskExecutionDir = new File(EXECUTION_BOX_DIR, String.valueOf(task.getId()));

        if(taskExecutionDir.exists()){
            delete(taskExecutionDir);
        }
        final boolean directoryCreated = taskExecutionDir.mkdir();

        if(directoryCreated){
            logger.info("Task execution directory {} created successfully", taskExecutionDir.getAbsolutePath());
        }else{
            logger.error("Unable to create task execution directory.");
            result.setStatus(Status.INTERNAL_ERROR);
            return result;
        }

        Details details = langConfigProperties.getMap().get(task.getLang());

        try {
            File sourceFile = new File(taskExecutionDir.getAbsolutePath(), details.getSourceFile());
            FileOutputStream fileOutputStream = new FileOutputStream(sourceFile);
            fileOutputStream.write(task.getSource().getBytes());

        } catch (IOException e) {
            logger.error("Unable to create a source file for execution. Aborting execution.");
            e.printStackTrace();
            return result;
        }
        try {
            File inputFile = new File(taskExecutionDir.getAbsolutePath(), FILE_NAME_INPUT);
            FileOutputStream fileOutputStream = new FileOutputStream(inputFile);
            fileOutputStream.write(task.getInput().getBytes());

        } catch (IOException e) {
            logger.error("Unable to create input file for execution. Aborting execution.");
            e.printStackTrace();
            return result;
        }

        /*
        Running script to execute the code with given input.
         */
        try {
            Shell.exec(FILE_PATH_EXECUTION_SCRIPT, details.getCpuShare(), details.getMemLimit(), String.valueOf(task.getId()));
        } catch (IOException e) {
            logger.error("Script did not run successfully.");
            result.setStatus(Status.INTERNAL_ERROR);
            e.printStackTrace();
            return result;
        }

        File compileErrorFile = new File(taskExecutionDir, FILE_NAME_COMPILE_ERROR);
        if(compileErrorFile.setWritable(true)){
            logger.info("File permissions changed.");
        }
        File stdOutputFile = new File(taskExecutionDir, FILE_NAME_OUTPUT);
        File stdErrFile = new File(taskExecutionDir, FILE_NAME_ERROR);
        File executionTimeFile = new File(taskExecutionDir, FILE_NAME_EXECUTION_TIME);

        try {
            String compileError = getFileContents(compileErrorFile);
            if(compileError.length() > 0){
                result.setStatus(Status.COMPILATION_ERROR);
                result.setCompileErr(compileError);
                return result;
            }
        } catch (IOException e) {
            logger.error("Failed to read compile error file.");
            e.printStackTrace();
        }

        try {
            String output = getFileContents(stdOutputFile);
            int time = Integer.parseInt(getFileContents(executionTimeFile));
            String error = getFileContents(stdErrFile);
            if(time > task.getTimeLimit()){
                result.setStatus(Status.TIME_LIMIT_EXCEEDED);
            }else{
                if(error.length() > 0){
                    result.setStatus(Status.RUNTIME_ERROR);
                }else{
                    result.setStatus(Status.EXECUTED);
                }
            }
            result.setTime(time);
            result.setStdOut(output);
            result.setStdErr(error);
        } catch (IOException e) {
            result.setStatus(Status.INTERNAL_ERROR);
            logger.error("Cannot parse run files.");
            e.printStackTrace();
        } catch (NumberFormatException e){
            result.setStatus(Status.INTERNAL_ERROR);
            logger.error("Cannot parse execution time.");
        }

        delete(taskExecutionDir);
        return result;
    }

    private void delete(File dir) {
        File[] files =  dir.listFiles();
        if(files == null){
            boolean deleted = dir.delete();
            if(deleted){
                logger.info("Directory {} empty. Deleted.", dir.getAbsolutePath());
            }
        }else{
            logger.info("Directory {} non-empty, deleting files.", dir.getAbsolutePath());
            for(File file : files){
                if(file.isDirectory()){
                    delete(file);
                }else{
                    boolean delete = file.delete();
                    if(delete){
                        logger.info("File {} deleted..", file.getAbsolutePath());
                    }
                }
            }
            boolean deleted = dir.delete();
            if(deleted){
                logger.info("Directory {} empty. Deleted.", dir.getAbsolutePath());
            }
        }

    }


    private String getFileContents(File file) throws IOException{
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while((line = bufferedReader.readLine()) != null){
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }
}
