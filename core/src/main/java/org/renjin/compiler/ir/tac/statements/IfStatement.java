package org.renjin.compiler.ir.tac.statements;

import org.objectweb.asm.MethodVisitor;
import org.renjin.compiler.emit.EmitContext;
import org.renjin.compiler.ir.tac.IRLabel;
import org.renjin.compiler.ir.tac.expressions.Expression;
import org.renjin.compiler.ir.tac.expressions.SimpleExpression;
import org.renjin.eval.EvalException;
import org.renjin.sexp.*;

import java.util.Arrays;


public class IfStatement implements Statement, BasicBlockEndingStatement {
  
  private Expression condition;
  private IRLabel trueTarget;
  private IRLabel falseTarget;
  private IRLabel naTarget;
  
  public IfStatement(Expression condition, IRLabel trueTarget, IRLabel falseTarget, IRLabel naTarget) {
    this.condition = condition;
    this.trueTarget = trueTarget;
    this.falseTarget = falseTarget;
    this.naTarget = naTarget;
  }
  
  public IfStatement(Expression condition, IRLabel trueTarget, IRLabel falseTarget) {
    this.condition = condition;
    this.trueTarget = trueTarget;
    this.falseTarget = falseTarget;
    this.naTarget = null;
  }
  

  public Expression getCondition() {
    return condition;
  }
  
  @Override
  public Expression getRHS() {
    return condition;
  }
  
  public IRLabel getTrueTarget() {
    return trueTarget;
  }

  public IRLabel getFalseTarget() {
    return falseTarget;
  }
  
  public IfStatement setTrueTarget(IRLabel label) {
    return new IfStatement(condition, label, falseTarget, naTarget);
  }
  
  public IfStatement setFalseTarget(IRLabel label) {
    return new IfStatement(condition, trueTarget, label, naTarget);
  }

  @Override
  public Iterable<IRLabel> possibleTargets() {
    if(naTarget == null) {
      return Arrays.asList(trueTarget, falseTarget); 
    } else {
      return Arrays.asList(trueTarget, falseTarget, naTarget);
    }
  }

  @Override
  public void setRHS(Expression newRHS) {
    this.condition = newRHS;
  }
 
  private Logical toLogical(Object obj) {
    if(obj instanceof Boolean) {
      return ((Boolean)obj) ? Logical.TRUE : Logical.FALSE;
    } else if(obj instanceof SEXP) {
      SEXP s = (SEXP)obj;
      if (s.length() == 0) {
        throw new EvalException("argument is of length zero");
      }
      if( (s instanceof DoubleVector) ||
          (s instanceof ComplexVector) ||
          (s instanceof IntVector) ||
          (s instanceof LogicalVector) ) {
        return ((SEXP)obj).asLogical();        
      }
    } 
    throw new EvalException("invalid type where logical expected");
    
  }

  @Override
  public String toString() {
    return "if " + condition + " => TRUE:" + trueTarget + ", FALSE:" +  falseTarget +
          ", NA:" + (naTarget == null ? "ERROR" : naTarget);
  }

  @Override
  public void setChild(int childIndex, Expression child) {
    if(childIndex == 0) {
      condition = child;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public int getChildCount() {
    return 1;
  }

  @Override
  public Expression childAt(int index) {
    if(index == 0) {
      return condition;
    } else {
      throw new IllegalArgumentException();
    }
  }

  @Override
  public void accept(StatementVisitor visitor) {
    visitor.visitIf(this);
  }

  @Override
  public void emit(EmitContext emitContext, MethodVisitor mv) {
    throw new UnsupportedOperationException();
  }
}
