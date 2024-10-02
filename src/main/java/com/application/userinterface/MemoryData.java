package com.application.userinterface;

public class MemoryData {
    private String memoryValue;
    private String memoryAddress;
    private String memoryVariable;

    public MemoryData(String memoryValue, String memoryAddress, String memoryVariable) {
        this.memoryValue = memoryValue;
        this.memoryAddress = memoryAddress;
        this.memoryVariable = memoryVariable;
    }

    public String getMemoryValue() {
        return memoryValue;
    }

    public void setMemoryValue(String value) {
        memoryValue = value;
    }
    
    public String getMemoryAddress() {
        return memoryAddress;
    }

    public void setMemoryAddress(String value) {
        memoryAddress = value;
    }

    public String getMemoryVariable() {
        return memoryVariable;
    }

    public void setMemoryVariable(String value) {
        memoryVariable = value;
    }
}
