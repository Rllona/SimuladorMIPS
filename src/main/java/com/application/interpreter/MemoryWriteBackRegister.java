package com.application.interpreter;

public class MemoryWriteBackRegister extends PipelineRegister{

	private Register destRegister;
	private FloatRegister destFRegister;
	private int destValue;
	private float destFvalue;
	
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
	
	public float getDestFvalue() {
		return destFvalue;
	}
	
	public void setDestFvalue(float destFvalue) {
		this.destFvalue = destFvalue;
	}
}
