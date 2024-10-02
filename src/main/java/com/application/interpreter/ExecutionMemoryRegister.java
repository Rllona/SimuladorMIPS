package com.application.interpreter;

public class ExecutionMemoryRegister extends PipelineRegister{
	
	private InstructionType instructionType;
	private Register destRegister;
	private FloatRegister destFRegister;
	private int destValue;
	private int valueRegToMem;
	private float destFvalue;
	private float fvalueRegToMem;

	public InstructionType getInstructionType() {
		return instructionType;
	}
	
	public void setInstructionType(InstructionType instructionType) {
		this.instructionType = instructionType;
	}
	
	public Register getDestRegister() {
		return destRegister;
	}
	
	public void setDestRegister(Register destRegister) {
		this.destRegister = destRegister;
	}
	
	public FloatRegister getDestFRegister() {
		return destFRegister;
	}
	
	public void setDestFRegister(FloatRegister destRegister) {
		this.destFRegister = destRegister;
	}
	
	public int getDestValue() {
		return destValue;
	}
	
	public void setDestValue(int destValue) {
		this.destValue = destValue;
	}
	
	public int getValueRegToMem() {
		return valueRegToMem;
	}

	public void setValueRegToMem(int valueRegToMem) {
		this.valueRegToMem = valueRegToMem;
	}
	
	public float getDestFvalue() {
		return destFvalue;
	}
	
	public void setDestFvalue(float destFvalue) {
		this.destFvalue = destFvalue;
	}
	
	public float getFvalueRegToMem() {
		return fvalueRegToMem;
	}

	public void setFvalueRegToMem(float fvalueRegToMem) {
		this.fvalueRegToMem = fvalueRegToMem;
	}
	
	public void deepCopy(ExecutionMemoryRegister emregister) {
		this.setInstructionIndex(emregister.getInstructionIndex());
		this.setTotalInsIndex(emregister.getTotalInsIndex());
		this.setOpcode(emregister.getOpcode());
		this.setInstructionType(emregister.getInstructionType());
		this.setDestRegister(emregister.getDestRegister());
		this.setDestValue(emregister.getDestValue());
		this.setValueRegToMem(emregister.getValueRegToMem());
		this.setDestFRegister(emregister.getDestFRegister());
		this.setDestFvalue(emregister.getDestFvalue());
		this.setFvalueRegToMem(emregister.getFvalueRegToMem());
	}
}
