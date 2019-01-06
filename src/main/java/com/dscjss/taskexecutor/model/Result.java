package com.dscjss.taskexecutor.model;

import com.dscjss.taskexecutor.util.Status;

public class Result {

    private int id;
    private String stdOut;
    private String stdErr;
    private String compileErr;
    private int time;
    private double memory;
    private Status status;

    public Result(int id) {
        this.id = id;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStdOut() {
        return stdOut;
    }

    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    public String getStdErr() {
        return stdErr;
    }

    public void setStdErr(String stdErr) {
        this.stdErr = stdErr;
    }

    public String getCompileErr() {
        return compileErr;
    }

    public void setCompileErr(String compileErr) {
        this.compileErr = compileErr;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public double getMemory() {
        return memory;
    }

    public void setMemory(double memory) {
        this.memory = memory;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "{ id: " + id + "}";
    }
}
