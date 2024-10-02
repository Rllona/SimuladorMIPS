package com.application.interpreter;

import java.util.HashMap;
import java.util.List;

public class InstructionSetManager{
	
	MIPSInterpreter interpreter;
	List<String> instructionsTypeRList = List.of("add", "sub", "addi", "and", "or", "xor", "nor", "li", "add.d", "sub.d", "mul.d", "div.d");
	List<String> instructionsTypeMemList = List.of("lw", "sw", "lb", "sb", "lh", "sh", "ld", "sd", "l.d", "s.d");
	List<String> instructionsTypeJumpList = List.of("j");
	List<String> instructionsTypeBranchList = List.of("beq", "bne");
	
	public InstructionSetManager(MIPSInterpreter interpreter){
		this.interpreter = interpreter;
	}
	
	
	//Getters y Setters para la lista de registros del interprete
	
	public Register getRegister(String reg) {
		Register r = interpreter.registers.get(getIndexOfRegister(reg)); 
		return r;
	}
	
	public void setRegister(Register reg, int value) {
		reg.setValue(value);
		interpreter.registers.set(getIndexOfRegister(reg.getId()), reg);
	}
	
	public int getRegisterValue(String reg) {
		int value = interpreter.registers.get(getIndexOfRegister(reg)).getValue();
		return value;
	}
	
	public FloatRegister getFregister(String reg) {
		FloatRegister f = interpreter.fregisters.get(getIndexOfRegister(reg)); 
		return f;
	}
	
	public void setFregister(FloatRegister reg, float value) {
		reg.setValue(value);
		interpreter.fregisters.set(getIndexOfRegister(reg.getId()), reg);
	}
	
	public float getFregisterValue(String reg) {
		float value = interpreter.fregisters.get(getIndexOfRegister(reg)).getValue();
		return value;
	}
	
	public int getIndexOfRegister(String reg) {
		if(reg.charAt(0) == 'R' || reg.charAt(0) == 'r') {
			int index = Integer.parseInt(reg.substring(1));
			return index;
		}else if(reg.charAt(0) == 'F' || reg.charAt(0) == 'f') {
			int index = Integer.parseInt(reg.substring(1));
			return index;
		} else {
			return -1;
		}
	}
	
	public void resetPipelineRegister() {
		interpreter.fdregister.setInstructionIndex(-1);
		interpreter.fdregister.setTotalInsIndex(-1);
	}
	
	
	//Identificación Tipos de Instrucción
	
	public boolean isTypeR(String opcode) {
		return (instructionsTypeRList.contains(opcode));
	}
	
	public boolean isTypeMem(String opcode) {
		return (instructionsTypeMemList.contains(opcode));
	}
	
	public boolean isTypeBranch(String opcode) {
		return (instructionsTypeBranchList.contains(opcode));
	}
	
	public boolean isTypeJump(String opcode) {
		return (instructionsTypeJumpList.contains(opcode));
	}
	
	
	//Repertorio de Instrucciones
	
	public int add(int value1, int value2) {
		return value1 + value2;
	}
	
	public int sub(int value1, int value2) {
		return value1 - value2;
	}
	
	public float fadd(float value1, float value2) {
		return value1 + value2;
	}
	
	public float fsub(float value1, float value2) {
		return value1 - value2;
	}
	
	public int mul(int value1, int value2) {
		return value1 * value2;
	}
	
	public int div(int value1, int value2) {
		return value1 / value2;
	}
	
	public float mul(float value1, float value2) {
		return value1 * value2;
	}
	
	public float div(float value1, float value2) {
		return value1 / value2;
	}
	
	public int and(int value1, int value2) {
		return value1 & value2;
	}
	
	public int or(int value1, int value2) {
		return value1 | value2;
	}
	
	public int xor(int value1, int value2) {
		return value1 ^ value2;
	}
	
	public int nor(int value1, int value2) {
		return ~(value1 | value2);
	}
	
	public int lw(int address) {
		String hexValue = interpreter.memory[address/4];
		return Integer.parseUnsignedInt(hexValue, 16);
	}
	
	public void sw(int address, int value) {
		String hexValue = Integer.toHexString(value);
		hexValue = interpreter.hexStringDigits(hexValue, 8);
		interpreter.memory[address/4] = hexValue;
	}
	
	public float fl(int address) {
		String hexValue = interpreter.memory[address/4];
		int intBits = Integer.parseUnsignedInt(hexValue, 16);
		return Float.intBitsToFloat(intBits);
	}
	
	public void fs(int address, float value) {
		int intBits = Float.floatToIntBits(value);
		String hexValue = String.format("%08x", intBits);
		hexValue = interpreter.hexStringDigits(hexValue, 8);
		interpreter.memory[address/4] = hexValue;
	}
	
	public int lb(int address) {
		String hexValue = interpreter.memory[address/4];
		if(address%4 == 0) {
			hexValue = hexValue.substring(6);
		}else if(address%4 == 1) {
			hexValue = hexValue.substring(4, 6);
		}else if(address%4 == 2) {
			hexValue = hexValue.substring(2, 4);
		}else if(address%4 == 3) {
			hexValue = hexValue.substring(0, 2);
		}
		return Integer.parseUnsignedInt(hexValue, 16);
	}
	
	public void sb(int address, int value) {
		String hexValue = Integer.toHexString(value);
		hexValue = interpreter.hexStringDigits(hexValue, 2);
		if(address%4 == 0) {
			interpreter.memory[address/4] = interpreter.memory[address/4].substring(0, 6) + hexValue;
		}else if(address%4 == 1) {
			interpreter.memory[address/4] = interpreter.memory[address/4].substring(0, 4) + hexValue + interpreter.memory[address/4].substring(6);
		}else if(address%4 == 2) {
			interpreter.memory[address/4] = interpreter.memory[address/4].substring(0, 2) + hexValue + interpreter.memory[address/4].substring(4);
		}else if(address%4 == 3) {
			interpreter.memory[address/4] = hexValue + interpreter.memory[address/4].substring(2);
		}
	}
	
	public int lh(int address) {
		String hexValue = interpreter.memory[address/4];
		if(address%4 == 0 || address%4 == 1) {
			hexValue = hexValue.substring(4);
		}else if(address%4 == 2 || address%4 == 3) {
			hexValue = hexValue.substring(0, 4);
		}
		return Integer.parseUnsignedInt(hexValue, 16);
	}
	
	public void sh(int address, int value) {
		String hexValue = Integer.toHexString(value);
		hexValue = interpreter.hexStringDigits(hexValue, 4);
		if(address%4 == 0 || address%4 == 1) {
			interpreter.memory[address/4] = interpreter.memory[address/4].substring(0, 4) + hexValue;
		}else if(address%4 == 1 || address%4 == 3) {
			interpreter.memory[address/4] = hexValue + interpreter.memory[address/4].substring(4);
		}
	}
	
	public int ld(int address) {
		String hexValue = interpreter.memory[address/4];
		String hexValue2 = interpreter.memory[(address+4)/4];
		hexValue = hexValue2 + hexValue;
		return Integer.parseUnsignedInt(hexValue, 16);
	}
	
	public void sd(int address, int value) {
		String hexValue = Integer.toHexString(value);
		String hexValue2 = "00000000";
		if(hexValue.length() > 8) {
			hexValue2 = hexValue.substring(0, hexValue.length()-8);
		}
		interpreter.memory[address/4] = interpreter.hexStringDigits(hexValue, 8);
		interpreter.memory[(address+4)/4] = interpreter.hexStringDigits(hexValue2, 8);
	}
	
	public void jump(String label) {
		if(!interpreter.branchBuffer.containsKey(interpreter.deregister.getInstructionIndex())) {
			HashMap<String, Integer> labels = interpreter.getLabels();
			interpreter.pc = labels.get(label);
			interpreter.branchMispredictStallsCounter++;
			interpreter.cancelledInsCounter++;
			resetPipelineRegister();
		}else {
			BranchTargetBuffer buffer = interpreter.branchBuffer.get(interpreter.deregister.getInstructionIndex());
			if (interpreter.branchPredictor == 2) {
				if(buffer.getPredictionState().equals("0")) {
					HashMap<String, Integer> labels = interpreter.getLabels();
					interpreter.pc = labels.get(label);
					interpreter.branchMispredictStallsCounter++;
					interpreter.cancelledInsCounter++;
					resetPipelineRegister();
				}
			}else if (interpreter.branchPredictor == 3) {
				if (buffer.getPredictionState().equals("00") || buffer.getPredictionState().equals("01")) {
					HashMap<String, Integer> labels = interpreter.getLabels();
					interpreter.pc = labels.get(label);
					interpreter.branchMispredictStallsCounter++;
					interpreter.cancelledInsCounter++;
					resetPipelineRegister();
				}
			}
		}
	}
	
	public boolean beq(int value1, int value2, String label) {
		boolean branchTaken = false;
		if(value1 == value2) {
			jump(label);
			branchTaken = true;
		}else {
			evaluateBranchNotTaken();
		}
		return branchTaken;
	}
	
	public boolean bne(int value1, int value2, String label) {
		boolean branchTaken = false;
		if(value1 != value2) {
			jump(label);
			branchTaken = true;
		}else {
			evaluateBranchNotTaken();
		}
		return branchTaken;
	}
	
	public void evaluateBranchNotTaken() {
		if(interpreter.branchBuffer.containsKey(interpreter.deregister.getInstructionIndex())) {
			BranchTargetBuffer buffer = interpreter.branchBuffer.get(interpreter.deregister.getInstructionIndex());
			if (interpreter.branchPredictor == 2) {
				if(buffer.getPredictionState().equals("1")) {
					interpreter.pc = interpreter.deregister.getInstructionIndex() + 1;
					interpreter.branchMispredictStallsCounter++;
					interpreter.cancelledInsCounter++;
					resetPipelineRegister();
				}
			}else if (interpreter.branchPredictor == 3) {
				if (buffer.getPredictionState().equals("11") || buffer.getPredictionState().equals("10")) {
					interpreter.pc = interpreter.deregister.getInstructionIndex() + 1;
					interpreter.branchMispredictStallsCounter++;
					interpreter.cancelledInsCounter++;
					resetPipelineRegister();
				}
			}
		}
	}
}
