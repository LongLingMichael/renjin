package org.renjin.gcc.gimple;

import com.google.common.collect.Lists;

import java.util.List;

public class GimpleBasicBlock {

	private int index;
	private List<GimpleIns> instructions = Lists.newArrayList();

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public String getName() {
		return "" + index;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(index).append(">:\n");
		for(GimpleIns ins : instructions) {
			sb.append("  ").append(ins).append("\n");
		}
		return sb.toString();
	}

	public void addIns(GimpleIns ins) {
		instructions.add(ins);
	}

	public List<GimpleIns> getInstructions() {
		return instructions;
	}

}
