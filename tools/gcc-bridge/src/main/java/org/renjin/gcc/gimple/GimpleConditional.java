package org.renjin.gcc.gimple;

import com.google.common.base.Joiner;
import org.renjin.gcc.gimple.expr.GimpleExpr;

import java.util.List;

public class GimpleConditional extends GimpleIns {
//  gimple_cond <ne_expr, i_4, j_6, NULL, NULL>
//    goto <bb 46>;
//  else
//    goto <bb 47>;
	
	private GimpleOp operator;
	private List<GimpleExpr> operands;
	private int trueLabel;
	private int falseLabel;
	
	GimpleConditional() {
		
	}
	
	void setOperator(GimpleOp op) {
		this.operator = op;
	}
	
	void setOperands(List<GimpleExpr> operands) {
		this.operands = operands;
	}
	
	public GimpleOp getOperator() {
		return operator;
	}

	public List<GimpleExpr> getOperands() {
		return operands;
	}

	public int getTrueLabel() {
		return trueLabel;
	}

	public void setTrueLabel(int trueLabel) {
		this.trueLabel = trueLabel;
	}

	public int getFalseLabel() {
		return falseLabel;
	}

	public void setFalseLabel(int falseLabel) {
		this.falseLabel = falseLabel;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("gimple_cond<")
		  .append(operator)
		  .append(",");
		
		Joiner.on(", ").appendTo(sb, operands);
		sb.append("> goto <")
		  .append("BB").append(trueLabel)
		  .append("> else goto <")
		  .append("BB").append(falseLabel)
		  .append(">");
		return sb.toString();
	}


  @Override
  public void visit(GimpleVisitor visitor) {
    visitor.visitConditional(this);
  }
}
