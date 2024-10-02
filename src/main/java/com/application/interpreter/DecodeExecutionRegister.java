package com.application.interpreter;

public class DecodeExecutionRegister extends PipelineRegister{
	
	private InstructionType instructionType;
	private Register destRegister;
	private FloatRegister destFRegister;
	private int value1;
	private int value2;
	private int valueRegToMem;
	private float fvalue1;
	private float fvalue2;
	private float fvalueRegToMem;
	private String destJump;
	
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
	
	public int getValue1() {
		return value1;
	}
	
	public void setValue1(int value1) {
		this.value1 = value1;
	}
	
	public int getValue2() {
		return value2;
	}
	
	public void setValue2(int value2) {
		this.value2 = value2;
	}
	
	public float getFvalue1() {
		return fvalue1;
	}
	
	public void setFvalue1(float fvalue1) {
		this.fvalue1 = fvalue1;
	}
	
	public float getFvalue2() {
		return fvalue2;
	}
	
	public void setFvalue2(float fvalue2) {
		this.fvalue2 = fvalue2;
	}
	
	public String getDestJump() {
		return destJump;
	}
	
	public void setDestJump(String destJump) {
		this.destJump = destJump;
	}

	public int getValueRegToMem() {
		return valueRegToMem;
	}

	public void setValueRegToMem(int valueRegToMem) {
		this.valueRegToMem = valueRegToMem;
	}
	
	public float getFvalueRegToMem() {
		return fvalueRegToMem;
	}

	public void setFvalueRegToMem(float fvalueRegToMem) {
		this.fvalueRegToMem = fvalueRegToMem;
	}
	
	public void deepCopy(DecodeExecutionRegister deregister) {
		this.setInstructionIndex(deregister.getInstructionIndex());
		this.setTotalInsIndex(deregister.getTotalInsIndex());
		this.setOpcode(deregister.getOpcode());
		this.setInstructionType(deregister.getInstructionType());
		this.setDestRegister(deregister.getDestRegister());
		this.setValue1(deregister.getValue1());
		this.setValue2(deregister.getValue2());
		this.setValueRegToMem(deregister.getValueRegToMem());
		this.setDestFRegister(deregister.getDestFRegister());
		this.setFvalue1(deregister.getFvalue1());
		this.setFvalue2(deregister.getFvalue2());
		this.setFvalueRegToMem(deregister.getFvalueRegToMem());
		this.setDestJump(deregister.getDestJump());
	}
}
