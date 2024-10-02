package com.application.interpreter;

public class MulDivRegister extends PipelineRegister {
	private FloatRegister destRegister;
	private float destValue;
	
	public FloatRegister getDestRegister() {
		return destRegister;
	}
	
	public void setDestRegister(FloatRegister destRegister) {
		this.destRegister = destRegister;
	}
	
	public float getDestValue() {
		return destValue;
	}
	
	public void setDestValue(float destValue) {
		this.destValue = destValue;
	}
}
