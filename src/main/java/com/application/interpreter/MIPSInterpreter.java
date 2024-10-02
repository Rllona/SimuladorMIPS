package com.application.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.application.userinterface.*;

public class MIPSInterpreter {
	
	private MainController controller;
	private InstructionSetManager IM;
	private String[] instructions;
	protected List<Register> registers;
	protected List<FloatRegister> fregisters;
	protected String[] memory;
	private HashMap<String, Integer> memoryVariables;
	protected int pc;
	protected int totalInsCounter;
	protected int cycles;
	protected int cancelledInsCounter;
	protected int dataStallsCounter;
	protected int RAWStallsCounter;
	protected int WAWStallsCounter;
	protected int structuralStallsCounter;
	protected int branchResolveStallsCounter;
	protected int branchMispredictStallsCounter;
	protected FetchDecodeRegister fdregister;
	protected DecodeExecutionRegister deregister;
	protected ExecutionMemoryRegister emregister;
	protected MemoryWriteBackRegister mwregister;
	private int lastInstructionCompleted;
	protected DecodeExecutionRegister de2register;
	protected DecodeMulDivRegister dfaddregister;
	protected DecodeMulDivRegister dmulregister;
	protected DecodeMulDivRegister ddivregister;
	protected ExecutionMemoryRegister em2register;
	protected MulDivMemoryRegister faddmregister;
	protected MulDivMemoryRegister mulmregister;
	protected MulDivMemoryRegister divmregister;
	protected List<MulDivRegister> faddregisters;
	protected List<MulDivRegister> mulregisters;
	protected List<MulDivRegister> divregisters;
	
	private boolean dataRAWStall;
	private boolean dataWAWStall;
	private boolean structuralMemStall;
	private boolean structuralFaddStall;
	private boolean structuralMulStall;
	private boolean structuralDivStall;
	private boolean toExecution;
	private boolean toFOperation;
	private boolean toMultiplication;
	private boolean toDivision;
	private boolean structuralFaddMemStall;
	private boolean structuralMulMemStall;
	
	private boolean segmentationEnabled;
	private int fadderLatency;
	private int multiplierLatency;
	private int dividerLatency;
	private int faddCounter;
	private int mulCounter;
	private int divCounter;
	
	private HashMap<String, Integer> labels;
	protected int branchPredictor; // 0 -> Desactivado / 1 -> Predicción de salto no tomado / 2 -> Predicción de salto de 1 bit / 3 -> Predicción de salto de 2 bit
	private boolean branchStall;
	protected HashMap<Integer, BranchTargetBuffer> branchBuffer;
	private boolean lastBranchTaken;
	
	private boolean forwardingEnabled;
	private int forwardingArrowsDraw;
	private boolean memToMemFlag;
	
	public MIPSInterpreter(MainController controller) {
		this.controller = controller;
		IM = new InstructionSetManager(this);
		instructions = new String[0];
		registers = new ArrayList<Register>();
		fregisters = new ArrayList<FloatRegister>();
		memory = new String[256];
		memoryVariables = new HashMap<String, Integer>();
        pc = 0;
        totalInsCounter = 0;
        cycles = 0;
        cancelledInsCounter = 0;
        dataStallsCounter = 0;
        RAWStallsCounter = 0;
        WAWStallsCounter = 0;
        structuralStallsCounter = 0;
        branchResolveStallsCounter = 0;
        branchMispredictStallsCounter = 0;
        fdregister = new FetchDecodeRegister();
        deregister = new DecodeExecutionRegister();
        emregister = new ExecutionMemoryRegister();
        mwregister = new MemoryWriteBackRegister();
        lastInstructionCompleted = -1;
        de2register = new DecodeExecutionRegister();
        dfaddregister = new DecodeMulDivRegister();
        dmulregister = new DecodeMulDivRegister();
        ddivregister = new DecodeMulDivRegister();
        em2register = new ExecutionMemoryRegister();
        faddmregister = new MulDivMemoryRegister();
        mulmregister = new MulDivMemoryRegister();
        divmregister = new MulDivMemoryRegister();
        faddregisters = new ArrayList<MulDivRegister>();
        mulregisters = new ArrayList<MulDivRegister>();
        divregisters = new ArrayList<MulDivRegister>();
        dataRAWStall = false;
        dataWAWStall = false;
        structuralMemStall = false;
        structuralFaddStall = false;
        structuralMulStall = false;
        structuralDivStall = false;
        toExecution = false;
        toFOperation = false;
        toMultiplication = false;
        toDivision = false;
        structuralFaddMemStall = false;
        structuralMulMemStall = false;
        labels = new HashMap<String, Integer>();
        branchPredictor = controller.getBranchPredictionConfig();
        branchStall = false;
        branchBuffer = new HashMap<Integer, BranchTargetBuffer>();
        lastBranchTaken = false;
        forwardingEnabled = controller.getForwardingConfig();
        segmentationEnabled = controller.getSegmentationConfig();
        forwardingArrowsDraw = 0;
        memToMemFlag = false;
        fadderLatency = controller.getFaddValue() - 1;
        multiplierLatency = controller.getMulValue() - 1;
        dividerLatency = controller.getDivValue() - 1;
        faddCounter = 0;
        mulCounter = 0;
        divCounter = 0;
        initializeRegisters();
        initializeMemory();
        initializeFaddRegisters();
        initializeMulRegisters();
        initializeDivRegisters();
    }
	
	public int getTotalInsCounter() {
		return totalInsCounter;
	}

	public int getCycles() {
		return cycles;
	}
	
	public boolean getSegmentationEnabled() {
		return segmentationEnabled;
	}

	public boolean getDataRAWStall(){
		return dataRAWStall;
	}
	
	public boolean getDataWAWStall(){
		return dataWAWStall;
	}
	
	public boolean getStructuralFaddStall(){
		return structuralFaddStall;
	}
	
	public boolean getStructuralMulStall(){
		return structuralMulStall;
	}
	
	public boolean getStructuralDivStall(){
		return structuralDivStall;
	}
	
	public boolean getStructuralMemStall(){
		return structuralMemStall;
	}
	
	public boolean getStructuralFaddMemStall(){
		return structuralFaddMemStall;
	}
	
	public boolean getStructuralMulMemStall(){
		return structuralMulMemStall;
	}
	
	public int getCancelledInsCounter() {
		return cancelledInsCounter;
	}

	public int getDataStallsCounter() {
		return dataStallsCounter;
	}
	
	public int getRAWStallsCounter() {
		return RAWStallsCounter;
	}
	
	public int getWAWStallsCounter() {
		return WAWStallsCounter;
	}
	
	public int getStructuralStallsCounter() {
		return structuralStallsCounter;
	}

	public int getBranchResolveStallsCounter() {
		return branchResolveStallsCounter;
	}

	public int getBranchMispredictStallsCounter() {
		return branchMispredictStallsCounter;
	}
	
	public List<Register> getRegisters(){
		return registers;
	}
	
	public List<FloatRegister> getFregisters(){
		return fregisters;
	}
	
	public HashMap<String, Integer> getLabels(){
		return labels;
	}
	
	public String[] getMemory(){
		return memory;
	}
	
	public HashMap<String, Integer> getMemoryVariables(){
		return memoryVariables;
	}
	
	public HashMap<Integer, BranchTargetBuffer> getBranchBuffer(){
		return branchBuffer;
	}
	
	public String[] getInstructions() {
		return instructions;
	}
	
	private void initializeRegisters() {
		for (int i = 0; i < 32; i++) {
			Register r = new Register("R" + i, 0);
            registers.add(r);
        }
		for (int i = 0; i < 32; i++) {
			FloatRegister f = new FloatRegister("F" + i, 0);
            fregisters.add(f);
        }
	}
	
	private void initializeMemory() {
		for (int i = 0; i < 256; i++) {
			memory[i] = "00000000";
		}
	}
	
	private void initializeFaddRegisters() {
		for (int i = 0; i < fadderLatency; i++) {
			MulDivRegister f = new MulDivRegister();
			faddregisters.add(f);
		}
	}
	
	private void initializeMulRegisters() {
		for (int i = 0; i < multiplierLatency; i++) {
			MulDivRegister m = new MulDivRegister();
			mulregisters.add(m);
		}
	}
	
	private void initializeDivRegisters() {
		for (int i = 0; i < dividerLatency; i++) {
			MulDivRegister d = new MulDivRegister();
			divregisters.add(d);
		}
	}
	
	public void runCompleteCode() {
		for (int i = 0; i < instructions.length; i++) {
			System.out.println(instructions[i]);
		}

		while (codeNotEnded(instructions.length)) {
			executeCycle(instructions);
		}
        System.out.println("Número de ciclos ejecutados: " + cycles);
	}
	
	public boolean runCycle() {
		if (codeNotEnded(instructions.length)) {
			executeCycle(instructions);
		}
		
		return !codeNotEnded(instructions.length);
	}
	
	private void executeCycle(String[] instructions) {
		//Etapa WriteBack
    	lastInstructionCompleted = mwregister.getTotalInsIndex();
        if(mwregister.getInstructionIndex() != -1) {
        	writeBack();
        }
        
        //Etapa Memory
        mwregister.setInstructionIndex(emregister.getInstructionIndex());
        mwregister.setTotalInsIndex(emregister.getTotalInsIndex());
        if(emregister.getInstructionIndex() != -1) {
            memory();
        }
        
        //Check de StructuralStalls
        executeBranching();
        
        //Etapa Execute
        if(!structuralMemStall) {
	    	em2register.setInstructionIndex(de2register.getInstructionIndex());
	        em2register.setTotalInsIndex(de2register.getTotalInsIndex());
	        if(de2register.getInstructionIndex() != -1) {
	            execute();
	        }
        }
        
        if(!structuralFaddMemStall) {
	        fadderLoop();
	        if(faddCounter == 0) {
	        	faddregisters.get(0).setInstructionIndex(dfaddregister.getInstructionIndex());
		    	faddregisters.get(0).setTotalInsIndex(dfaddregister.getTotalInsIndex());
		        if(dfaddregister.getInstructionIndex() != -1) {
		            fAddSub();
		        }
	        }
        }else {
        	structuralFaddMemStall = false;
        }
        
        if(!structuralMulMemStall) {
	        multiplierLoop();
	        if(mulCounter == 0) {
	        	mulregisters.get(0).setInstructionIndex(dmulregister.getInstructionIndex());
		    	mulregisters.get(0).setTotalInsIndex(dmulregister.getTotalInsIndex());
		        if(dmulregister.getInstructionIndex() != -1) {
		            multiply();
		        }
	        }
        }else {
        	structuralMulMemStall = false;
        }
        
        dividerLoop();
        if(divCounter == 0) {
	    	divregisters.get(0).setInstructionIndex(ddivregister.getInstructionIndex());
	    	divregisters.get(0).setTotalInsIndex(ddivregister.getTotalInsIndex());
	        if(ddivregister.getInstructionIndex() != -1) {
	            divide();
	        }
        }
        
        structuralMemStall = executeMerge();

        //Check de MemToMemFlags
        if(memToMemFlag) {
			emregister.setValueRegToMem(mwregister.getDestValue());
			emregister.setFvalueRegToMem(mwregister.getDestValue());
			controller.addForwardingArrow(cycles, mwregister.getTotalInsIndex(), cycles+1, emregister.getTotalInsIndex(), false);
			forwardingArrowsDraw++;
			memToMemFlag = false;
		}
        
        //Etapa Decode
        deregister.setInstructionIndex(fdregister.getInstructionIndex());
        deregister.setTotalInsIndex(fdregister.getTotalInsIndex());
        if(fdregister.getInstructionIndex() != -1 && !structuralMemStall) {
        	decode();
        	dataRAWStall = dataHazard();
        }
        
        if(!dataRAWStall && !dataWAWStall && !structuralMemStall && !structuralFaddStall && !structuralMulStall && !structuralDivStall) {	
            branchStall = resolveBranch();
        }else {
        	deregister.setInstructionIndex(-1);
            deregister.setTotalInsIndex(-1);
            if(forwardingArrowsDraw > 0) {
            	controller.removeLastForwardingArrows(forwardingArrowsDraw);
            }
            if(dataRAWStall || dataWAWStall) {
            	dataStallsCounter++;
            	if(dataRAWStall) {
            		RAWStallsCounter++;
            	}else if(dataWAWStall) {
            		WAWStallsCounter++;
            	}
            }
            if(structuralMemStall || structuralMulMemStall || structuralFaddStall || structuralMulStall || structuralDivStall) {
            	structuralStallsCounter++;
            }
        }       
        
        //Etapa Fetch
        if(!dataRAWStall && !dataWAWStall && !structuralMemStall && !structuralFaddStall && !structuralMulStall && !structuralDivStall && !branchStall) {
            if(pc < instructions.length) {
            	fdregister.setInstructionIndex(pc);
            	fdregister.setTotalInsIndex(totalInsCounter);
                String instruction = instructions[pc];
                fetchInstruction(instruction);
                if(!lastBranchTaken) {
                	pc++;
                }
                lastBranchTaken = false;
                totalInsCounter++;
                controller.addDiagramRow(totalInsCounter, formatInstruction(instruction));
            }else {
            	fdregister.setInstructionIndex(-1);
            	fdregister.setTotalInsIndex(-1);
            }
        }
        cycles++;
        forwardingArrowsDraw = 0;
        int[] faddRegsIndexes = faddIndexesToArray();
        int[] mulRegsIndexes = mulIndexesToArray();
        int[] divRegsIndexes = divIndexesToArray();
        controller.addDiagramColumn(cycles, fdregister.getTotalInsIndex(), deregister.getTotalInsIndex(), em2register.getTotalInsIndex(), mwregister.getTotalInsIndex(), lastInstructionCompleted, faddRegsIndexes, mulRegsIndexes, divRegsIndexes);
	}
	
	private boolean codeNotEnded(int nInstructions) {
		return (pc < nInstructions || mwregister.getInstructionIndex() != -1 || emregister.getInstructionIndex() != -1 || deregister.getInstructionIndex() != -1 || fdregister.getInstructionIndex() != -1 || checkMulDiv());
	}
	
	private boolean checkMulDiv() {
		if(dfaddregister.getInstructionIndex() != -1 || faddmregister.getInstructionIndex() != -1) {
			return true;
		}
		if(dmulregister.getInstructionIndex() != -1 || mulmregister.getInstructionIndex() != -1) {
			return true;
		}
		if(ddivregister.getInstructionIndex() != -1 || divmregister.getInstructionIndex() != -1) {
			return true;
		}
		for (int i = 0; i <fadderLatency; i++) {
			if(faddregisters.get(i).getInstructionIndex() != -1) {
				return true;
			}
		}
		for (int i = 0; i < multiplierLatency; i++) {
			if(mulregisters.get(i).getInstructionIndex() != -1) {
				return true;
			}
		}
		for (int i = 0; i < dividerLatency; i++) {
			if(divregisters.get(i).getInstructionIndex() != -1) {
				return true;
			}
		}
		return false;
	}
	
	private int[] faddIndexesToArray() {
		int[] array = new int[fadderLatency+1];
		for(int i = 0; i < fadderLatency; i++) {
			array[i] = faddregisters.get(i).getTotalInsIndex();
		}
		array[fadderLatency] = faddmregister.getTotalInsIndex();
		if(!segmentationEnabled) {
			faddmregister.setInstructionIndex(-1);
			faddmregister.setTotalInsIndex(-1);
		}
		return array;
	}
	
	private int[] mulIndexesToArray() {
		int[] array = new int[multiplierLatency+1];
		for(int i = 0; i < multiplierLatency; i++) {
			array[i] = mulregisters.get(i).getTotalInsIndex();
		}
		array[multiplierLatency] = mulmregister.getTotalInsIndex();
		if(!segmentationEnabled) {
			mulmregister.setInstructionIndex(-1);
			mulmregister.setTotalInsIndex(-1);
		}
		return array;
	}
	
	private int[] divIndexesToArray() {
		int[] array = new int[dividerLatency+1];
		for(int i = 0; i < dividerLatency; i++) {
			array[i] = divregisters.get(i).getTotalInsIndex();
		}
		array[dividerLatency] = divmregister.getTotalInsIndex();
		if(!segmentationEnabled) {
			divmregister.setInstructionIndex(-1);
			divmregister.setTotalInsIndex(-1);
		}
		return array;
	}
	
	private String formatInstruction(String instruction) {
		String[] parts = instruction.split("\\s+");
		String format = pc + ". " + parts[0] + " ";
		for (int i = 1; i < parts.length - 1; i++) {
			format = format.concat(parts[i] + ", ");
		}
		format = format.concat(parts[parts.length - 1]);
		return format;
	}
	
	private void fetchInstruction(String instruction) {
		String[] parts = instruction.split("\\s+");
		String opcode = parts[0].toLowerCase();
		
		fdregister.setOpcode(opcode);
		fdregister.setParts(parts);
		
		//Brach prediction
		if (branchPredictor == 2 || branchPredictor == 3) {
			if (branchBuffer.containsKey(pc)) {
				BranchTargetBuffer buffer = branchBuffer.get(pc);
				
				if (branchPredictor == 2) {
					if(buffer.getPredictionState().equals("1")) {
						pc = buffer.getTargetAddress();
						lastBranchTaken = true;
					}
				}else if (branchPredictor == 3) {
					if (buffer.getPredictionState().equals("11") || buffer.getPredictionState().equals("10")) {
						pc = buffer.getTargetAddress();
						lastBranchTaken = true;
					}
				}
			}
		}
    }
	
	private void decode() {
		String opcode = fdregister.getOpcode();
		deregister.setOpcode(opcode);
		String[] parts = fdregister.getParts();
		toExecution = true;
		structuralFaddStall = false;
		structuralMulStall = false;
		structuralDivStall = false;
		
		deregister.setDestRegister(null);
		deregister.setDestFRegister(null);
		
		InstructionType instructionType = InstructionType.unknown;
		
		if(IM.isTypeR(opcode)) {
			instructionType = InstructionType.typeR;
			if("add".equals(opcode) || "sub".equals(opcode) || "and".equals(opcode) || "or".equals(opcode) || "xor".equals(opcode) || "nor".equals(opcode)) {
				deregister.setDestRegister(IM.getRegister(parts[1]));
				deregister.setValue1(IM.getRegisterValue(parts[2]));
				deregister.setValue2(IM.getRegisterValue(parts[3]));
				
			}else if("addi".equals(opcode)) {
				deregister.setDestRegister(IM.getRegister(parts[1]));
				deregister.setValue1(IM.getRegisterValue(parts[2]));
				deregister.setValue2(Integer.parseInt(parts[3]));
				
			}else if("add.d".equals(opcode) || "sub.d".equals(opcode)) {
				toFOperation = true;
				toMultiplication = false;
				toDivision = false;
				toExecution = false;
				deregister.setDestFRegister(IM.getFregister(parts[1]));
				deregister.setFvalue1(IM.getFregisterValue(parts[2]));
				deregister.setFvalue2(IM.getFregisterValue(parts[3]));
				if(!segmentationEnabled && faddregisters.get(0).getInstructionIndex() != -1) {
					structuralFaddStall = true;
				}
				
			}else if("mul.d".equals(opcode)) {
				toMultiplication = true;
				toFOperation = false;
				toDivision = false;
				toExecution = false;
				deregister.setDestFRegister(IM.getFregister(parts[1]));
				deregister.setFvalue1(IM.getFregisterValue(parts[2]));
				deregister.setFvalue2(IM.getFregisterValue(parts[3]));
				if(!segmentationEnabled && mulregisters.get(0).getInstructionIndex() != -1) {
					structuralMulStall = true;
				}
				
			}else if("div.d".equals(opcode)) {
				toDivision = true;
				toFOperation = false;
				toMultiplication = false;
				toExecution = false;
				deregister.setDestFRegister(IM.getFregister(parts[1]));
				deregister.setFvalue1(IM.getFregisterValue(parts[2]));
				deregister.setFvalue2(IM.getFregisterValue(parts[3]));
				if(!segmentationEnabled && divregisters.get(0).getInstructionIndex() != -1) {
					structuralDivStall = true;
				}
				
			}else if("li".equals(opcode)) {
				deregister.setDestRegister(IM.getRegister(parts[1]));
				deregister.setValue1(Integer.parseInt(parts[2]));
				deregister.setValue2(0);
				
			}
		}else if(IM.isTypeMem(opcode)) {
			instructionType = InstructionType.typeMem;
			if("sw".equals(opcode) | "sb".equals(opcode) | "sh".equals(opcode) | "sd".equals(opcode)) {
				deregister.setDestRegister(null);
				deregister.setDestFRegister(null);
				deregister.setValueRegToMem(IM.getRegisterValue(parts[1]));
			}else if("lw".equals(opcode) | "lb".equals(opcode) | "lh".equals(opcode) | "ld".equals(opcode)) {
				deregister.setDestRegister(IM.getRegister(parts[1]));
				deregister.setDestFRegister(null);
			}else if("s.d".equals(opcode)) {
				deregister.setDestRegister(null);
				deregister.setDestFRegister(null);
				deregister.setFvalueRegToMem(IM.getFregisterValue(parts[1]));
			}else if("l.d".equals(opcode)) {
				deregister.setDestFRegister(IM.getFregister(parts[1]));
				deregister.setDestRegister(null);
			}
			
			String offset = parts[2].split("\\(")[0];
			if (memoryVariables.containsKey(offset)){
				deregister.setValue1(memoryVariables.get(offset));
			}else {
				deregister.setValue1(Integer.parseInt(offset));
			}
			deregister.setValue2(IM.getRegisterValue(parts[2].split("\\(")[1].replace(")", "")));
			
		}else if(IM.isTypeBranch(opcode)) {
			instructionType = InstructionType.typeBranch;
			deregister.setValue1(IM.getRegisterValue(parts[1]));
			deregister.setValue2(IM.getRegisterValue(parts[2]));
			deregister.setDestJump(parts[3]);
			deregister.setDestRegister(null);
			
		}else if(IM.isTypeJump(opcode)) {
			instructionType = InstructionType.typeJump;
			deregister.setDestJump(parts[1]);
		}
		
		deregister.setInstructionType(instructionType);
	}
	
	private void executeBranching() {
		de2register.setInstructionIndex(-1);
		de2register.setTotalInsIndex(-1);
		dfaddregister.setInstructionIndex(-1);
		dfaddregister.setTotalInsIndex(-1);
		dmulregister.setInstructionIndex(-1);
		dmulregister.setTotalInsIndex(-1);
		ddivregister.setInstructionIndex(-1);
		ddivregister.setTotalInsIndex(-1);
		
		if(toExecution) {
			de2register.deepCopy(deregister);		
		}else if(toFOperation) {
			dfaddregister.deepCopy(deregister);
		}else if(toMultiplication) {
			dmulregister.deepCopy(deregister);
		}else if(toDivision) {
			ddivregister.deepCopy(deregister);
		}
	}
	
	private void execute() {
		InstructionType instructionType = de2register.getInstructionType();
		em2register.setInstructionType(instructionType);
		String opcode = de2register.getOpcode();
		em2register.setOpcode(opcode);
		em2register.setDestRegister(de2register.getDestRegister());
		em2register.setDestFRegister(de2register.getDestFRegister());
		
		int instructionIndex = de2register.getInstructionIndex();
		boolean branchTaken;
		
		switch (instructionType) {
		case typeR:
			if ("add".equals(opcode) || "addi".equals(opcode) || "li".equals(opcode)) {
				em2register.setDestValue(IM.add(de2register.getValue1(), de2register.getValue2()));
				
			}else if("sub".equals(opcode)) {
				em2register.setDestValue(IM.sub(de2register.getValue1(), de2register.getValue2()));
				
			}else if("and".equals(opcode)) {
				em2register.setDestValue(IM.and(de2register.getValue1(), de2register.getValue2()));
				
			}else if("or".equals(opcode)) {
				em2register.setDestValue(IM.or(de2register.getValue1(), de2register.getValue2()));
				
			}else if("xor".equals(opcode)) {
				em2register.setDestValue(IM.xor(de2register.getValue1(), de2register.getValue2()));
				
			}else if("nor".equals(opcode)) {
				em2register.setDestValue(IM.nor(de2register.getValue1(), de2register.getValue2()));
				
			}
			break;
			
		case typeMem:
			em2register.setDestValue(IM.add(de2register.getValue1(), de2register.getValue2()));
			em2register.setValueRegToMem(de2register.getValueRegToMem());
			em2register.setFvalueRegToMem(de2register.getFvalueRegToMem());
			break;
		
		case typeBranch:
			if(branchPredictor != 0) {
				branchTaken = false;
				if ("beq".equals(opcode)){
					branchTaken = IM.beq(de2register.getValue1(), de2register.getValue2(), de2register.getDestJump());
					System.out.println(branchTaken + " " + de2register.getValue1() + "  " + de2register.getValue2());
					
				}else if ("bne".equals(opcode)){
					branchTaken = IM.bne(de2register.getValue1(), de2register.getValue2(), de2register.getDestJump());
				}
				
				branchPrediction(branchTaken, instructionIndex);
			}
			break;
			
		case typeJump:
			if(branchPredictor != 0) {
				branchTaken = true;
				IM.jump(de2register.getDestJump());
				
				branchPrediction(branchTaken, instructionIndex);
			}
			break;
			
		case unknown:
			// Manejar instrucciones no soportadas o desconocidas
            System.out.println("Instrucción no soportada: " + opcode);
			break;
		}
	}
	
	private boolean executeMerge() {
		boolean check = false;
		if(divmregister.getInstructionIndex() != -1) {
			emregister.setInstructionIndex(divmregister.getInstructionIndex());
			emregister.setTotalInsIndex(divmregister.getTotalInsIndex());
			emregister.setOpcode(divmregister.getOpcode());
			emregister.setInstructionType(InstructionType.typeR);
			emregister.setDestFRegister(divmregister.getDestRegister());
			emregister.setDestFvalue(divmregister.getDestValue());
			if(mulmregister.getInstructionIndex() != -1) {
				structuralMulMemStall = true;
			}
			if(faddmregister.getInstructionIndex() != -1) {
				structuralFaddMemStall = true;
			}
			if(em2register.getInstructionIndex() != -1) {
				check = true;
			}
		}else if(mulmregister.getInstructionIndex() != -1) {
			emregister.setInstructionIndex(mulmregister.getInstructionIndex());
			emregister.setTotalInsIndex(mulmregister.getTotalInsIndex());
			emregister.setOpcode(mulmregister.getOpcode());
			emregister.setInstructionType(InstructionType.typeR);
			emregister.setDestFRegister(mulmregister.getDestRegister());
			emregister.setDestFvalue(mulmregister.getDestValue());
			if(faddmregister.getInstructionIndex() != -1) {
				structuralFaddMemStall = true;
			}
			if(em2register.getInstructionIndex() != -1) {
				check = true;
			}
		}else if(faddmregister.getInstructionIndex() != -1) {
			emregister.setInstructionIndex(faddmregister.getInstructionIndex());
			emregister.setTotalInsIndex(faddmregister.getTotalInsIndex());
			emregister.setOpcode(faddmregister.getOpcode());
			emregister.setInstructionType(InstructionType.typeR);
			emregister.setDestFRegister(faddmregister.getDestRegister());
			emregister.setDestFvalue(faddmregister.getDestValue());
			if(em2register.getInstructionIndex() != -1) {
				check = true;
			}
		}else if(em2register.getInstructionIndex() != -1) {
			emregister.deepCopy(em2register);
		}else {
			emregister.setInstructionIndex(-1);
			emregister.setTotalInsIndex(-1);
		}
		return check;
	}
	
	private void fadderLoop() {
		if(segmentationEnabled) {
			faddmregister.setInstructionIndex(faddregisters.get(fadderLatency-1).getInstructionIndex());
			faddmregister.setTotalInsIndex(faddregisters.get(fadderLatency-1).getTotalInsIndex());
			faddmregister.setOpcode(faddregisters.get(fadderLatency-1).getOpcode());
			faddmregister.setDestRegister(faddregisters.get(fadderLatency-1).getDestRegister());
			faddmregister.setDestValue(faddregisters.get(fadderLatency-1).getDestValue());
			
			for(int i = fadderLatency-1; i > 0; i--) {
				faddregisters.get(i).setInstructionIndex(faddregisters.get(i-1).getInstructionIndex());
				faddregisters.get(i).setTotalInsIndex(faddregisters.get(i-1).getTotalInsIndex());
				faddregisters.get(i).setOpcode(faddregisters.get(i-1).getOpcode());
				faddregisters.get(i).setDestRegister(faddregisters.get(i-1).getDestRegister());
				faddregisters.get(i).setDestValue(faddregisters.get(i-1).getDestValue());
			}
		}else {
			if(faddregisters.get(0).getInstructionIndex() != -1) {
				faddCounter++;
			}
			if(faddCounter >= fadderLatency) {
				faddmregister.setInstructionIndex(faddregisters.get(0).getInstructionIndex());
				faddmregister.setTotalInsIndex(faddregisters.get(0).getTotalInsIndex());
				faddmregister.setOpcode(faddregisters.get(0).getOpcode());
				faddmregister.setDestRegister(faddregisters.get(0).getDestRegister());
				faddmregister.setDestValue(faddregisters.get(0).getDestValue());
				faddregisters.get(0).setInstructionIndex(-1);
				faddregisters.get(0).setTotalInsIndex(-1);
				faddCounter = 0;
			}
		}
	}
	
	private void fAddSub() {
		faddregisters.get(0).setOpcode(dfaddregister.getOpcode());
		faddregisters.get(0).setDestRegister(dfaddregister.getDestRegister());
		if(dfaddregister.getOpcode().equals("add.d")) {
			faddregisters.get(0).setDestValue(IM.fadd(dfaddregister.getValue1(), dfaddregister.getValue2()));
		}else if(dfaddregister.getOpcode().equals("sub.d")) {
			faddregisters.get(0).setDestValue(IM.fsub(dfaddregister.getValue1(), dfaddregister.getValue2()));
		}
		
	}
	
	private void multiplierLoop() {
		if(segmentationEnabled) {
			mulmregister.setInstructionIndex(mulregisters.get(multiplierLatency-1).getInstructionIndex());
			mulmregister.setTotalInsIndex(mulregisters.get(multiplierLatency-1).getTotalInsIndex());
			mulmregister.setOpcode(mulregisters.get(multiplierLatency-1).getOpcode());
			mulmregister.setDestRegister(mulregisters.get(multiplierLatency-1).getDestRegister());
			mulmregister.setDestValue(mulregisters.get(multiplierLatency-1).getDestValue());
			
			for(int i = multiplierLatency-1; i > 0; i--) {
				mulregisters.get(i).setInstructionIndex(mulregisters.get(i-1).getInstructionIndex());
				mulregisters.get(i).setTotalInsIndex(mulregisters.get(i-1).getTotalInsIndex());
				mulregisters.get(i).setOpcode(mulregisters.get(i-1).getOpcode());
				mulregisters.get(i).setDestRegister(mulregisters.get(i-1).getDestRegister());
				mulregisters.get(i).setDestValue(mulregisters.get(i-1).getDestValue());
			}
		}else {
			if(mulregisters.get(0).getInstructionIndex() != -1) {
				mulCounter++;
			}
			if(mulCounter >= multiplierLatency) {
				mulmregister.setInstructionIndex(mulregisters.get(0).getInstructionIndex());
				mulmregister.setTotalInsIndex(mulregisters.get(0).getTotalInsIndex());
				mulmregister.setOpcode(mulregisters.get(0).getOpcode());
				mulmregister.setDestRegister(mulregisters.get(0).getDestRegister());
				mulmregister.setDestValue(mulregisters.get(0).getDestValue());
				mulregisters.get(0).setInstructionIndex(-1);
				mulregisters.get(0).setTotalInsIndex(-1);
				mulCounter = 0;
			}
		}
	}
	
	private void multiply() {
		mulregisters.get(0).setOpcode(dmulregister.getOpcode());
		mulregisters.get(0).setDestRegister(dmulregister.getDestRegister());
		mulregisters.get(0).setDestValue(IM.mul(dmulregister.getValue1(), dmulregister.getValue2()));
	}
	
	private void dividerLoop() {
		if(segmentationEnabled) {
			divmregister.setInstructionIndex(divregisters.get(dividerLatency-1).getInstructionIndex());
			divmregister.setTotalInsIndex(divregisters.get(dividerLatency-1).getTotalInsIndex());
			divmregister.setOpcode(divregisters.get(dividerLatency-1).getOpcode());
			divmregister.setDestRegister(divregisters.get(dividerLatency-1).getDestRegister());
			divmregister.setDestValue(divregisters.get(dividerLatency-1).getDestValue());
			
			for(int i = dividerLatency-1; i > 0; i--) {
				divregisters.get(i).setInstructionIndex(divregisters.get(i-1).getInstructionIndex());
				divregisters.get(i).setTotalInsIndex(divregisters.get(i-1).getTotalInsIndex());
				divregisters.get(i).setOpcode(divregisters.get(i-1).getOpcode());
				divregisters.get(i).setDestRegister(divregisters.get(i-1).getDestRegister());
				divregisters.get(i).setDestValue(divregisters.get(i-1).getDestValue());
			}
		}else {
			if(divregisters.get(0).getInstructionIndex() != -1) {
				divCounter++;
			}
			if(divCounter >= dividerLatency) {
				divmregister.setInstructionIndex(divregisters.get(0).getInstructionIndex());
				divmregister.setTotalInsIndex(divregisters.get(0).getTotalInsIndex());
				divmregister.setOpcode(divregisters.get(0).getOpcode());
				divmregister.setDestRegister(divregisters.get(0).getDestRegister());
				divmregister.setDestValue(divregisters.get(0).getDestValue());
				divregisters.get(0).setInstructionIndex(-1);
				divregisters.get(0).setTotalInsIndex(-1);
				divCounter = 0;
			}
		}
	}
	
	private void divide() {
		divregisters.get(0).setOpcode(ddivregister.getOpcode());
		divregisters.get(0).setDestRegister(ddivregister.getDestRegister());
		divregisters.get(0).setDestValue(IM.div(ddivregister.getValue1(), ddivregister.getValue2()));
	}
	
	private void memory() {
		InstructionType instructionType = emregister.getInstructionType();
		String opcode = emregister.getOpcode();
		mwregister.setOpcode(opcode);
		Register destRegister = emregister.getDestRegister();
		FloatRegister destFRegister = emregister.getDestFRegister();
		int destValue = emregister.getDestValue();
		float destFvalue = emregister.getDestFvalue();
		
		if(instructionType == InstructionType.typeMem) {
			if ("lw".equals(opcode)){
				destValue = IM.lw(destValue);
				
			}else if("sw".equals(opcode)) {
				IM.sw(destValue, emregister.getValueRegToMem());
				
			}else if ("l.d".equals(opcode)){
				destFvalue = IM.fl(destValue);
				
			}else if("s.d".equals(opcode)) {
				IM.fs(destValue, emregister.getFvalueRegToMem());
				
			}else if ("lb".equals(opcode)){
				destValue = IM.lb(destValue);
				
			}else if("sb".equals(opcode)) {
				IM.sb(destValue, emregister.getValueRegToMem());
				
			}else if ("lh".equals(opcode)){
				destValue = IM.lh(destValue);
				
			}else if("sh".equals(opcode)) {
				IM.sh(destValue, emregister.getValueRegToMem());
				
			}else if ("ld".equals(opcode)){
				destValue = IM.ld(destValue);
				
			}else if("sd".equals(opcode)) {
				IM.sd(destValue, emregister.getValueRegToMem());
			}
		}
		
		mwregister.setDestRegister(destRegister);
		mwregister.setDestValue(destValue);
		mwregister.setDestFRegister(destFRegister);
		mwregister.setDestFvalue(destFvalue);
	}
	
	private void writeBack() {
		Register destRegister = mwregister.getDestRegister();
		FloatRegister destFRegister = mwregister.getDestFRegister();
		if(destRegister != null) {
			int destValue = mwregister.getDestValue();		
			IM.setRegister(destRegister, destValue);
		}
		if (destFRegister != null) {
			float destFvalue = mwregister.getDestFvalue();		
			IM.setFregister(destFRegister, destFvalue);
		}
	}
	
	public void instructionsParser(String code) {
		String[] lines = code.split("\n");
		int numIns = 0;
		boolean inDataSection = false;
		int indexDataAddress = 0;
		boolean inTextSection = false;
		int indexTextSectionStart = -1;
		
		for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i].trim();
			
			//Eliminar Comentarios
			if(lines[i].contains(";")) {
				int semiColonIndex = lines[i].indexOf(";");
				lines[i] = lines[i].substring(0, semiColonIndex);
			}
			
			//Parsear sección .data
			if(inDataSection && lines[i] != null && lines[i] != "" && lines[i] != " " && lines[i] != "\n") {
				if(lines[i].contains(":")) {
					int colonIndex = lines[i].indexOf(":");
					memoryVariables.put(lines[i].substring(0, colonIndex).trim(), indexDataAddress);
					lines[i] = lines[i].substring(colonIndex + 1).trim();
				}
				if(lines[i].contains(".word") || lines[i].contains(".word32") || lines[i].contains(".word16") || lines[i].contains(".byte") || lines[i].contains(".double") || lines[i].contains(".float")) {
					String[] dataParts = lines[i].split(",");
					dataParts[0] = dataParts[0].split("\\s+")[1];
					if(lines[i].contains(".word ") || lines[i].contains(".word32")){
						for(int j = 0; j < dataParts.length; j++) {
							String hexValue = "";
							dataParts[j] = dataParts[j].trim();
							if(dataParts[j].startsWith("0x") || dataParts[j].startsWith("0X")) {
								hexValue = dataParts[j].substring(2);
							}else {
								int dataValue = Integer.parseInt(dataParts[j]);
								hexValue = Integer.toHexString(dataValue);
							}
							memory[indexDataAddress/4] = hexStringDigits(hexValue, 8);
							indexDataAddress += 4;
						}
					}else if(lines[i].contains(".float")){
						for(int j = 0; j < dataParts.length; j++) {
							String hexValue = "";
							dataParts[j] = dataParts[j].trim();
							if(dataParts[j].startsWith("0x") || dataParts[j].startsWith("0X")) {
								hexValue = dataParts[j].substring(2);
							}else {
								float dataValue = Float.parseFloat(dataParts[j]);
								int intBits = Float.floatToIntBits(dataValue);
								hexValue = String.format("%08x", intBits);
							}
							memory[indexDataAddress/4] = hexStringDigits(hexValue, 8);
							indexDataAddress += 4;
						}
					}else if(lines[i].contains(".word16")){
						for(int j = 0; j < dataParts.length; j++) {
							String hexValue = "";
							dataParts[j] = dataParts[j].trim();
							if(dataParts[j].startsWith("0x") || dataParts[j].startsWith("0X")) {
								hexValue = dataParts[j].substring(2);
							}else {
								int dataValue = Integer.parseInt(dataParts[j]);
								hexValue = Integer.toHexString(dataValue);
							}
							if(memory[indexDataAddress/4].equals("00000000")) {
								memory[indexDataAddress/4] = "0000" + hexStringDigits(hexValue, 4);
							}else {
								memory[indexDataAddress/4] = hexStringDigits(hexValue, 4) + memory[indexDataAddress/4].substring(4);
							}
							indexDataAddress += 2;
						}
					}else if(lines[i].contains(".byte")){
						for(int j = 0; j < dataParts.length; j++) {
							String hexValue = "";
							dataParts[j] = dataParts[j].trim();
							if(dataParts[j].startsWith("0x") || dataParts[j].startsWith("0X")) {
								hexValue = dataParts[j].substring(2);
							}else {
								int dataValue = Integer.parseInt(dataParts[j]);
								hexValue = Integer.toHexString(dataValue);
							}
							if(memory[indexDataAddress/4].equals("00000000")) {
								memory[indexDataAddress/4] = "000000" + hexStringDigits(hexValue, 2);
							}else {
								if(indexDataAddress%4 == 1) {
									memory[indexDataAddress/4] = "0000" + hexStringDigits(hexValue, 2) + memory[indexDataAddress/4].substring(6);
								}else if(indexDataAddress%4 == 2) {
									memory[indexDataAddress/4] = "00" + hexStringDigits(hexValue, 2) + memory[indexDataAddress/4].substring(4);
								}else if(indexDataAddress%4 == 3) {
									memory[indexDataAddress/4] = hexStringDigits(hexValue, 2) + memory[indexDataAddress/4].substring(2);
								}
							}
							indexDataAddress += 1;
						}
					}else if(lines[i].contains(".double")){
						for(int j = 0; j < dataParts.length; j++) {
							String hexValue = "";
							String hexValue2 = "";
							dataParts[j] = dataParts[j].trim();
							if(dataParts[j].startsWith("0x") || dataParts[j].startsWith("0X")) {
								hexValue = dataParts[j].substring(10);
								hexValue2 = dataParts[j].substring(2, 10);
							}else {
								int dataValue = Integer.parseInt(dataParts[j]);
								hexValue = Integer.toHexString(dataValue);
								if(hexValue.length() <= 8) {
									hexValue2 = "00000000";
								}else {
									hexValue2 = hexValue.substring(0, hexValue.length()-8);
								}
							}
							memory[indexDataAddress/4] = hexStringDigits(hexValue, 8);
							indexDataAddress += 4;
							memory[indexDataAddress/4] = hexStringDigits(hexValue2, 8);
							indexDataAddress += 4;
						}
					}
					if(indexDataAddress % 4 != 0) {
						indexDataAddress += 4 - (indexDataAddress % 4);
					}
				}
			}
			//Parsear sección .text
			else if(inTextSection && lines[i] != null && lines[i] != "" && lines[i] != " " && lines[i] != "\n") {
				numIns++;
			}
			
			if(lines[i].equals(".data")) {
				inDataSection = true;
				inTextSection = false;
			}
			else if(lines[i].equals(".text") || lines[i].equals(".code")) {
				inDataSection = false;
				inTextSection = true;
				indexTextSectionStart = i + 1;
			}
		}
		
		if(numIns <= 0) return;
		
		String[] instructionsAux = new String[numIns];
		
		int index = 0;
		for (int i = indexTextSectionStart; i < lines.length; i++) {
			if(lines[i] != null && lines[i] != "" && lines[i] != " " && lines[i] != "\n") {
				if(lines[i].contains(":")) {
					int colonIndex = lines[i].indexOf(":");
					labels.put(lines[i].substring(0, colonIndex).trim(), index);
					lines[i] = lines[i].substring(colonIndex + 1);
				}
				lines[i] = lines[i].replace(',', ' ');
				if(checkOpcode(lines[i])) {
					instructionsAux[index] = lines[i].trim();
					index++;
				}
			}
		}
		
		if(numIns != index) {
			instructions = new String[index];
			for(int i = 0; i < index; i++) {
				instructions[i] = instructionsAux[i];
			}
		}else {
			instructions = instructionsAux.clone();
		}
	}
	
	private boolean checkOpcode(String line) {
		String opCode = line.trim().split("\\s+")[0];
		return IM.isTypeR(opCode) || IM.isTypeMem(opCode) || IM.isTypeJump(opCode) || IM.isTypeBranch(opCode);
	}

	private boolean dataHazard() {
		//RAW hazards
		boolean check = false;
		if(!forwardingEnabled) {
			if(deregister.getInstructionType() == InstructionType.typeR && !deregister.getOpcode().equals("li")) {
				if(loopCompareRegs(2,3)) {
					check = true;
				}
			}else if(deregister.getInstructionType() == InstructionType.typeBranch) {
				if(loopCompareRegs(1,2)) {
					check = true;
				}
			}else if(isSaveOp(deregister.getOpcode())) {
				if(loopCompareRegs(1,2)) {
					check = true;
				}
			}else if(isLoadOp(deregister.getOpcode())) {
				if(loopCompareRegs(2)) {
					check = true;
				}
			}
		}else {		
			if(deregister.getInstructionType() == InstructionType.typeR && !deregister.getOpcode().equals("li")) {
				if(checkNonForwardingsRegs(2,3)) return true;
				if(compareRegId(2, mwregister)) {
					if(checkForwardings(mwregister, 1)) return true;
				}
				if(compareRegId(3, mwregister)) {
					if(checkForwardings(mwregister, 2)) return true;	
				}
				if(compareRegId(2, emregister)) {
					if(checkForwardings(emregister, 1)) return true;
				}
				if(compareRegId(3, emregister)) {
					if(checkForwardings(emregister, 2)) return true;
				}
			}else if(deregister.getInstructionType() == InstructionType.typeBranch) {
				if(checkNonForwardingsRegs(1,2)) return true;
				if(compareRegId(1, mwregister)) {
					if(checkForwardings(mwregister, 1)) return true;	
				}
				if(compareRegId(2, mwregister)) {
					if(checkForwardings(mwregister, 2)) return true;	
				}
				if(compareRegId(1, emregister)) {
					if(checkForwardings(emregister, 1)) return true;
				}
				if(compareRegId(2, emregister)) {
					if(checkForwardings(emregister, 2)) return true;
				}
			}else if(isSaveOp(deregister.getOpcode())) {
				if(checkNonForwardingsRegs(1,2)) return true;
				if(compareRegId(1, mwregister)) {
					deregister.setValueRegToMem(mwregister.getDestValue());
					deregister.setFvalueRegToMem(mwregister.getDestValue());

					controller.addForwardingArrow(cycles, mwregister.getTotalInsIndex(), cycles+1, deregister.getTotalInsIndex(), true);
					forwardingArrowsDraw++;
				}
				if(compareRegId(2, mwregister)) {
					if(checkForwardings(mwregister, 2)) return true;
				}
				if(compareRegId(1, emregister)) {
					memToMemFlag = true;
				}
				if(compareRegId(2, emregister)) {
					if(checkForwardings(emregister, 2)) return true;
				}
			}else if(isLoadOp(deregister.getOpcode())) {
				if(checkNonForwardingsRegs(2)) return true;
				if(compareRegId(2, mwregister)) {
					if(checkForwardings(mwregister, 2)) return true;				
				}
				if(compareRegId(2, emregister)) {
					if(checkForwardings(emregister, 2)) return true;
				}
			}
		}
		
		//WAW hazards
		if(isFloatOp(deregister.getOpcode())) {
			dataWAWStall = checkWAWRegs(deregister.getOpcode());
		}
		
		return check;
	}
	
	private boolean compareRegId(int partIndex, ExecutionMemoryRegister reg) {
		boolean equalRegIds = false;
		if(reg.getInstructionIndex() != -1 && fdregister.getParts()[partIndex].length() > 1) {
			String fdRegId;
			if (!fdregister.getParts()[partIndex].contains("(")) {
				fdRegId = fdregister.getParts()[partIndex];
			}else {
				fdRegId = fdregister.getParts()[partIndex].split("\\(")[1].replace(")", "");
			}
			fdRegId = fdRegId.toUpperCase();
			if(reg.getDestRegister() != null) {
				equalRegIds = fdRegId.equals(reg.getDestRegister().getId());
				if(equalRegIds) return true;
			}
			if(reg.getDestFRegister() != null) {
				equalRegIds = fdRegId.equals(reg.getDestFRegister().getId());
			}
		}
		return equalRegIds;
	}
	
	private boolean compareRegId(int partIndex, MemoryWriteBackRegister reg) {
		boolean equalRegIds = false;
		if(reg.getInstructionIndex() != -1 && fdregister.getParts()[partIndex].length() > 1) {
			String fdRegId;
			if (!fdregister.getParts()[partIndex].contains("(")) {
				fdRegId = fdregister.getParts()[partIndex];
			}else {
				fdRegId = fdregister.getParts()[partIndex].split("\\(")[1].replace(")", "");
			}
			fdRegId = fdRegId.toUpperCase();
			if(reg.getDestRegister() != null) {
				equalRegIds = fdRegId.equals(reg.getDestRegister().getId());
				if(equalRegIds) return true;
			}
			if(reg.getDestFRegister() != null) {
				equalRegIds = fdRegId.equals(reg.getDestFRegister().getId());
			}
		}
		return equalRegIds;
	}
	
	private boolean compareRegId(int partIndex, MulDivRegister reg) {
		boolean equalRegIds = false;
		if(reg.getInstructionIndex() != -1 && fdregister.getParts()[partIndex].length() > 1) {
			String fdRegId = fdregister.getParts()[partIndex];
			fdRegId = fdRegId.toUpperCase();
			if(reg.getDestRegister() != null) {
				equalRegIds = fdRegId.equals(reg.getDestRegister().getId());
			}
		}
		return equalRegIds;
	}
	
	private boolean compareRegId(int partIndex, MulDivMemoryRegister reg) {
		boolean equalRegIds = false;
		if(reg.getInstructionIndex() != -1 && fdregister.getParts()[partIndex].length() > 1) {
			String fdRegId = fdregister.getParts()[partIndex];
			fdRegId = fdRegId.toUpperCase();
			if(reg.getDestRegister() != null) {
				equalRegIds = fdRegId.equals(reg.getDestRegister().getId());
			}
		}
		return equalRegIds;
	}
	
	private boolean loopCompareRegs(int index1, int index2) {
		boolean comparation = compareRegId(index1, emregister) ||
				compareRegId(index1, mwregister) ||
				compareRegId(index1, faddmregister) ||
				compareRegId(index1, mulmregister) ||
				compareRegId(index1, divmregister) ||
				compareRegId(index2, emregister) ||
				compareRegId(index2, mwregister) ||
				compareRegId(index2, faddmregister) ||
				compareRegId(index2, mulmregister) ||
				compareRegId(index2, divmregister);
		
		if(comparation) return true;
		
		for (int i = 0; i < faddregisters.size(); i++) {
			comparation = compareRegId(index1, faddregisters.get(i)) || compareRegId(index2, faddregisters.get(i));
			if(comparation) return true;	
		}
		
		for (int i = 0; i < mulregisters.size(); i++) {
			comparation = compareRegId(index1, mulregisters.get(i)) || compareRegId(index2, mulregisters.get(i));
			if(comparation) return true;	
		}
		
		for (int i = 0; i < divregisters.size(); i++) {
			comparation = compareRegId(index1, divregisters.get(i)) || compareRegId(index2, divregisters.get(i));
			if(comparation) return true;	
		}
		return false;
	}
	
	private boolean loopCompareRegs(int index1) {
		boolean comparation = compareRegId(index1, emregister) ||
				compareRegId(index1, mwregister) ||
				compareRegId(index1, faddmregister) ||
				compareRegId(index1, mulmregister) ||
				compareRegId(index1, divmregister);
		
		if(comparation) return true;
		
		for (int i = 0; i < faddregisters.size(); i++) {
			comparation = compareRegId(index1, faddregisters.get(i));
			if(comparation) return true;	
		}		
		
		for (int i = 0; i < mulregisters.size(); i++) {
			comparation = compareRegId(index1, mulregisters.get(i));
			if(comparation) return true;	
		}
		
		for (int i = 0; i < divregisters.size(); i++) {
			comparation = compareRegId(index1, divregisters.get(i));
			if(comparation) return true;	
		}
		return false;
	}
	
	private boolean checkNonForwardingsRegs(int index1, int index2) {
		boolean comparation = false;
		
		for (int i = 0; i < faddregisters.size(); i++) {
			comparation = compareRegId(index1, faddregisters.get(i)) || compareRegId(index2, faddregisters.get(i));
			if(comparation) return true;	
		}
		
		for (int i = 0; i < mulregisters.size(); i++) {
			comparation = compareRegId(index1, mulregisters.get(i)) || compareRegId(index2, mulregisters.get(i));
			if(comparation) return true;	
		}
		
		for (int i = 0; i < divregisters.size(); i++) {
			comparation = compareRegId(index1, divregisters.get(i)) || compareRegId(index2, divregisters.get(i));
			if(comparation) return true;	
		}
		return false;
	}
	
	private boolean checkNonForwardingsRegs(int index1) {
		boolean comparation = false;
		
		for (int i = 0; i < faddregisters.size(); i++) {
			comparation = compareRegId(index1, faddregisters.get(i));
			if(comparation) return true;	
		}
		
		for (int i = 0; i < mulregisters.size(); i++) {
			comparation = compareRegId(index1, mulregisters.get(i));
			if(comparation) return true;	
		}
		
		for (int i = 0; i < divregisters.size(); i++) {
			comparation = compareRegId(index1, divregisters.get(i));
			if(comparation) return true;	
		}
		return false;
	}
	
	private boolean checkForwardings(ExecutionMemoryRegister reg, int value) {
		if(value == 1) {
			deregister.setValue1(reg.getDestValue());
			deregister.setFvalue1(reg.getDestFvalue());
		}else if(value == 2) {
			deregister.setValue2(reg.getDestValue());
			deregister.setFvalue2(reg.getDestFvalue());
		}
		
		if(isLoadOp(reg.getOpcode())) {
			return true;
		}else {
			controller.addForwardingArrow(cycles, reg.getTotalInsIndex(), cycles+1, deregister.getTotalInsIndex(), false);
			forwardingArrowsDraw++;
		}
		return false;
	}
	
	private boolean checkForwardings(MemoryWriteBackRegister reg, int value) {
		if(value == 1) {
			deregister.setValue1(reg.getDestValue());
			deregister.setFvalue1(reg.getDestFvalue());
		}else if(value == 2) {
			deregister.setValue2(reg.getDestValue());
			deregister.setFvalue2(reg.getDestFvalue());
		}

		controller.addForwardingArrow(cycles, reg.getTotalInsIndex(), cycles+1, deregister.getTotalInsIndex(), true);
		forwardingArrowsDraw++;
		
		return false;
	}
	
	private boolean checkWAWRegs(String opcode) {
		boolean comparation = false;
		
		for (int i = 0; i < faddregisters.size(); i++) {
			comparation = compareRegId(1, faddregisters.get(i));
			if(comparation) {
				if(opcode.equals("l.d")) return true;
			}
		}
		
		for (int i = 0; i < mulregisters.size(); i++) {
			comparation = compareRegId(1, mulregisters.get(i));
			if(comparation) {	
				if(opcode.equals("l.d")) return true;
				if(opcode.equals("add.d") || opcode.equals("sub.d")) {
					if(!segmentationEnabled) {
						if(mulCounter + fadderLatency < multiplierLatency) return true;
					}else {
						if(i + fadderLatency < multiplierLatency) return true;
					}
				}
			}
		}
		
		for (int i = 0; i < divregisters.size(); i++) {
			comparation = compareRegId(1, divregisters.get(i));
			if(comparation) {	
				if(opcode.equals("l.d")) return true;
				if(opcode.equals("add.d") || opcode.equals("sub.d")) {
					if(!segmentationEnabled) {
						if(divCounter + fadderLatency < dividerLatency) return true;
					}else {
						if(i + fadderLatency < dividerLatency) return true;
					}
				}
				if(opcode.equals("mul.d")) {
					if(!segmentationEnabled) {
						if(divCounter + multiplierLatency < dividerLatency) return true;
					}else {
						if(i + multiplierLatency < dividerLatency) return true;
					}
				}
			}
		}
		return false;
	}
	
	/*private boolean compareRegId(int partIndex, ExecutionMemoryRegister reg) {
		boolean equalRegIds = false;
		if(reg.getInstructionIndex() != -1 && fdregister.getParts()[partIndex].length() > 1 && reg.getDestRegister() != null) {
			int fdRegIndex;
			if (!fdregister.getParts()[partIndex].contains("(")) {
				fdRegIndex = IM.getIndexOfRegister(fdregister.getParts()[partIndex]);
			}else {
				fdRegIndex = IM.getIndexOfRegister(fdregister.getParts()[partIndex].split("\\(")[1].replace(")", ""));
			}
			
			equalRegIds = fdRegIndex == IM.getIndexOfRegister(reg.getDestRegister().getId());
		}
		return equalRegIds;
	}
	
	private boolean compareRegId(int partIndex, MemoryWriteBackRegister reg) {
		boolean equalRegIds = false;
		if(reg.getInstructionIndex() != -1 && fdregister.getParts()[partIndex].length() > 1 && reg.getDestRegister() != null) {
			int fdRegIndex;
			if (!fdregister.getParts()[partIndex].contains("(")) {
				fdRegIndex = IM.getIndexOfRegister(fdregister.getParts()[partIndex]);
			}else {
				fdRegIndex = IM.getIndexOfRegister(fdregister.getParts()[partIndex].split("\\(")[1].replace(")", ""));
			}
			
			equalRegIds = fdRegIndex == IM.getIndexOfRegister(reg.getDestRegister().getId());
		}
		return equalRegIds;
	}*/
	
	private boolean isSaveOp(String opcode) {
		return opcode.equals("sw") | opcode.equals("sh") | opcode.equals("sb") | opcode.equals("sd") | opcode.equals("s.d");
	}
	
	private boolean isLoadOp(String opcode) {
		return opcode.equals("lw") | opcode.equals("lh") | opcode.equals("lb") | opcode.equals("ld") | opcode.equals("l.d");
	}
	
	private boolean isFloatOp(String opcode) {
		return opcode.equals("add.d") | opcode.equals("sub.d") | opcode.equals("mul.d") | opcode.equals("l.d");
	}
	
	private boolean resolveBranch() {
		boolean check = false;
		if(branchPredictor == 0 && !branchStall) {
			if ("j".equals(deregister.getOpcode())){
				pc = labels.get(deregister.getDestJump());
				IM.resetPipelineRegister();
				check = true;
				branchResolveStallsCounter++;
			}else if ("beq".equals(deregister.getOpcode())){
				check = true;
				IM.resetPipelineRegister();
				branchResolveStallsCounter++;
				if(deregister.getValue1() == deregister.getValue2()) {
					pc = labels.get(deregister.getDestJump());
				}
			}else if ("bne".equals(deregister.getOpcode())){
				check = true;
				IM.resetPipelineRegister();
				branchResolveStallsCounter++;
				if(deregister.getValue1() != deregister.getValue2()) {
					pc = labels.get(deregister.getDestJump());
				}
			}
		}
		return check;
	}
	
	private void branchPrediction(boolean branchTaken, int instructionIndex) {
		if (branchPredictor == 2 || branchPredictor == 3) {
			String state = "";
			BranchTargetBuffer buffer;
			if (branchPredictor == 2) {
				state = (branchTaken)? "1" : "0";
				
			}else if (branchPredictor == 3) {
				if (branchBuffer.containsKey(instructionIndex)) {
					String previousState = branchBuffer.get(instructionIndex).getPredictionState();
					switch (previousState) {
					case "00":
						state = (branchTaken)? "01" : "00";
						break;
					case "01":
						state = (branchTaken)? "11" : "00";
						break;
					case "10":
						state = (branchTaken)? "11" : "00";
						break;
					case "11":
						state = (branchTaken)? "11" : "10";
						break;
					}			
				}else {
					state = (branchTaken)? "10" : "01";
				}
			}
			
			if (branchBuffer.containsKey(instructionIndex)) {
				buffer = branchBuffer.get(instructionIndex);
				buffer.setPredictionState(state);
				branchBuffer.replace(instructionIndex, buffer);
			}else {
				buffer = new BranchTargetBuffer(state, labels.get(deregister.getDestJump()));
				branchBuffer.put(instructionIndex, buffer);
			}
		}
	}
	
	//Métodos auxiliares
	
	public String hexStringDigits(String hexValue, int digits) {
		if(hexValue.length() < digits) {
			String zeros = "";
			for (int i = 0; i < digits; i++) {
				zeros = zeros + "0";
		    }
			hexValue = zeros.substring(hexValue.length()) + hexValue;
		}else if(hexValue.length() > digits) {
			//Error
		}
		return hexValue;
	}
}
