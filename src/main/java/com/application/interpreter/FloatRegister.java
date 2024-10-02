package com.application.interpreter;

public class FloatRegister {
	private String id;
	private float value;
	
	public FloatRegister(String id, float value) {
		this.id = id;
		this.value = value;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public float getValue() {
		return value;
	}
	
	public void setValue(float value) {
		this.value = value;
	}
}
