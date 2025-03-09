package com.wgzhao.addax.admin.constant;


public enum ExecStatus
{
    SUCCESS("SUCCESS"),
    FAILED("ERROR"),
    RUNNING("RUNNING"),
    WAITING("WAITING");

    private final String status;

    ExecStatus(String status)
    {
        this.status = status;
    }

    public String getCode()
    {
        return status;
    }

    @Override
    public String toString() {
        return status;
    }
}
