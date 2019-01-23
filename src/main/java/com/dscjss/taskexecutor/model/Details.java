package com.dscjss.taskexecutor.model;


public class Details {
    private String sourceFile;
    private String cpuShare;
    private String memLimit;

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getCpuShare() {
        return cpuShare;
    }

    public void setCpuShare(String cpuShare) {
        this.cpuShare = cpuShare;
    }

    public String getMemLimit() {
        return memLimit;
    }

    public void setMemLimit(String memLimit) {
        this.memLimit = memLimit;
    }
}