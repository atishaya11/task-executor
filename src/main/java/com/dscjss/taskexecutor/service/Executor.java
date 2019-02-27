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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class Executor {

    private final Logger logger = LoggerFactory.getLogger(Executor.class);

    private final String EXECUTION_BOX_DIR = "/usr/execute/box";
    private final String FILE_NAME_INPUT = "run.stdin";
    private final String FILE_NAME_OUTPUT = "run.stdout";
    private final String FILE_NAME_ERROR = "run.stderr";
    private final String FILE_NAME_COMPILE_ERROR = "compile.stderr";
    private final String FILE_NAME_EXECUTION_TIME = "run.time";
    private final String FILE_NAME_SIGNAL = "run.signal";
    private final String FILE_PATH_EXECUTION_SCRIPT = "/usr/script.sh";
    private final int MAX_STREAM_LENGTH = 104857600; //100 MB


    private final int SIGNAL_TIME_LIMIT_EXCEEDED = 124;
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

    public Result execute(Task task) {
        Result result = new Result(task.getId());

        File taskExecutionDir = new File(EXECUTION_BOX_DIR, String.valueOf(task.getId()));

        if (taskExecutionDir.exists()) {
            delete(taskExecutionDir);
        }
        final boolean directoryCreated = taskExecutionDir.mkdirs();

        if (directoryCreated) {
            logger.info("Task execution directory {} created successfully", taskExecutionDir.getAbsolutePath());
        } else {
            logger.error("Unable to create task execution directory.");
            result.setStatus(Status.INTERNAL_ERROR);
            delete(taskExecutionDir);
            return result;
        }

        Details details = langConfigProperties.getMap().get(task.getLang());
        String sourceFileName = details.getSourceFile();
        logger.info("Source file name: {}", sourceFileName);
        if(task.getLang().equals("java8")){
            Pattern pattern = Pattern.compile("class\\s+([^\\n\\s*]+)(.*)\\{(.*)\\s*(.*)public\\s+static\\s+void\\s+main\\s*");
            Matcher matcher = pattern.matcher(task.getSource());
            if(matcher.find()){
                String className = matcher.group(1);
                sourceFileName = className + ".java";
                logger.info("Changing java file name to : " + sourceFileName);
            }
        }

        try {
            File sourceFile = new File(taskExecutionDir.getAbsolutePath(),sourceFileName);
            FileOutputStream fileOutputStream = new FileOutputStream(sourceFile);
            fileOutputStream.write(task.getSource().getBytes());

        } catch (IOException e) {
            logger.error("Unable to create a source file for execution. Aborting execution.");
            e.printStackTrace();
            delete(taskExecutionDir);
            return result;
        }
        try {
            File inputFile = new File(taskExecutionDir.getAbsolutePath(), FILE_NAME_INPUT);
            FileOutputStream fileOutputStream = new FileOutputStream(inputFile);
            fileOutputStream.write(task.getInput().getBytes());

        } catch (IOException e) {
            logger.error("Unable to create input file for execution. Aborting execution.");
            e.printStackTrace();
            delete(taskExecutionDir);
            return result;
        }

        /*
        Running script to execute the code with given input.
         */
        try {
            Shell.exec(FILE_PATH_EXECUTION_SCRIPT, details.getCpuShare(), details.getMemLimit(), String.valueOf(task.getId()), task.getLang(), String.valueOf(task.getTimeLimit()/1000.0));
        } catch (IOException | InterruptedException e) {
            logger.error("Script did not run successfully.");
            result.setStatus(Status.INTERNAL_ERROR);
            e.printStackTrace();
            delete(taskExecutionDir);
            return result;
        }

        File compileErrorFile = new File(taskExecutionDir, FILE_NAME_COMPILE_ERROR);
        File stdOutputFile = new File(taskExecutionDir, FILE_NAME_OUTPUT);
        File stdErrFile = new File(taskExecutionDir, FILE_NAME_ERROR);
        File executionTimeFile = new File(taskExecutionDir, FILE_NAME_EXECUTION_TIME);
        File signalFile = new File(taskExecutionDir, FILE_NAME_SIGNAL);

        try {
            String content = getFileContents(compileErrorFile);
            if (content.length() > 0) {
                String compileError = content.substring(0, Math.min(content.length(), MAX_STREAM_LENGTH));
                result.setStatus(Status.COMPILATION_ERROR);
                result.setCompileErr(compileError);
                delete(taskExecutionDir);
                return result;
            }
        } catch (IOException e) {
            logger.error("Failed to read compile error file.");
            e.printStackTrace();
        }

        try {
            int signal = Integer.parseInt(getFileContents(signalFile).trim());
            String timeString = getFileContents(executionTimeFile);
            int time = Integer.parseInt(timeString.trim());

            if(stdOutputFile.length() > MAX_STREAM_LENGTH){
                result.setStatus(Status.RUNTIME_ERROR);
            } else {
                String output = getFileContents(stdOutputFile);
                String error = getFileContents(stdErrFile);

                if(signal == SIGNAL_TIME_LIMIT_EXCEEDED) {
                    result.setStatus(Status.TIME_LIMIT_EXCEEDED);
                } else if(signal != 0){
                    String stdErr = error.substring(0, Math.min(error.length(), MAX_STREAM_LENGTH));
                    result.setStdErr(stdErr);
                    result.setStatus(Status.RUNTIME_ERROR);
                } else {
                    result.setStatus(Status.EXECUTED);
                }
                if(output.length() > MAX_STREAM_LENGTH){
                    result.setStatus(Status.RUNTIME_ERROR);
                    result.setStdOut(output.substring(0, MAX_STREAM_LENGTH));
                }else {
                    result.setStdOut(output);
                }
            }
            result.setSignal(signal);
            result.setTime(time);
        } catch (IOException e) {
            result.setStatus(Status.INTERNAL_ERROR);
            logger.error("Cannot parse run files.");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
            result.setStatus(Status.INTERNAL_ERROR);
            logger.error("Cannot parse execution time or signal file.");
        }

        delete(taskExecutionDir);
        return result;
    }

    private void delete(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            boolean deleted = dir.delete();
            if (deleted) {
                logger.info("Directory {} empty. Deleted.", dir.getAbsolutePath());
            }
        } else {
            logger.info("Directory {} non-empty, deleting files.", dir.getAbsolutePath());
            for (File file : files) {
                if (file.isDirectory()) {
                    delete(file);
                } else {
                    boolean delete = file.delete();
                    if (delete) {
                        logger.info("File {} deleted..", file.getAbsolutePath());
                    }
                }
            }
            boolean deleted = dir.delete();
            if (deleted) {
                logger.info("Directory {} empty. Deleted.", dir.getAbsolutePath());
            }
        }
    }


    private String getFileContents(File file) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
