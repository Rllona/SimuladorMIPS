package com.application.interpreter;

public class DecodeMulDivRegister extends PipelineRegister{

	private FloatRegister destRegister;
	private float value1;
	private float value2;
	
	public FloatRegister getDestRegister() {
		return destRegister;
	}
	
	public void setDestRegister(FloatRegister destRegister) {
		this.destRegister = destRegister;
	}
	
	public float getValue1() {
		return value1;
	}
	
	public void setValue1(float value1) {
		this.value1 = value1;
	}
	
	public float getValue2() {
		return value2;
	}
	
	public void setValue2(float value2) {
		this.value2 = value2;
	}
	
	public void deepCopy(DecodeExecutionRegister deregister) {
		this.setInstructionIndex(deregister.getInstructionIndex());
		this.setTotalInsIndex(deregister.getTotalInsIndex());
		this.setOpcode(deregister.getOpcode());
		this.setDestRegister(deregister.getDestFRegister());
		this.setValue1(deregister.getFvalue1());
		this.setValue2(deregister.getFvalue2());
	}
}
