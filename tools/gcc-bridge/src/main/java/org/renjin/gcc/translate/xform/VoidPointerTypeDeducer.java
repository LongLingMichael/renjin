package org.renjin.gcc.translate.xform;

import com.google.common.collect.Sets;
import org.renjin.gcc.gimple.*;
import org.renjin.gcc.gimple.expr.GimpleExpr;
import org.renjin.gcc.gimple.expr.GimpleLValue;
import org.renjin.gcc.gimple.expr.GimpleVariableRef;
import org.renjin.gcc.gimple.ins.GimpleAssign;
import org.renjin.gcc.gimple.type.GimplePointerType;
import org.renjin.gcc.gimple.type.GimpleType;
import org.renjin.gcc.gimple.type.GimpleVoidType;

import java.util.Set;

/**
 * Attempts to determine the types of void* pointers so that we can correctly
 * translate malloc calls.
 */
public class VoidPointerTypeDeducer implements FunctionBodyTransformer {

  public static final VoidPointerTypeDeducer INSTANCE = new VoidPointerTypeDeducer();

  private VoidPointerTypeDeducer() {
  }

  @Override
  public boolean transform(GimpleCompilationUnit unit, GimpleFunction fn) {

    boolean updated = false;

    for(GimpleVarDecl decl : fn.getVariableDeclarations()) {
      if(isVoidPtr(decl)) { 
        System.out.println("Deducing type of " + decl + "...");
        if(tryToDeduceType(unit, fn, decl)) {
          updated = true;
        }
      }
    }
    return updated;
  }

  private boolean isVoidPtr(GimpleVarDecl decl) {
    return decl.getType() instanceof GimplePointerType &&
        decl.getType().getBaseType() instanceof GimpleVoidType;
  }


  /**
   * Tries to deduce the type of a given void pointer declaration
   */
  private boolean tryToDeduceType(GimpleCompilationUnit unit, GimpleFunction fn, GimpleVarDecl decl) {
    AssignmentFinder finder = new AssignmentFinder(unit, fn, decl);
    fn.visitIns(finder);
    
    if(finder.possibleTypes.size() == 1) {
      decl.setType(finder.possibleTypes.iterator().next());
      return true;
    } else {
      return false;
    }
  }

  /**
   * Looks for any cases in which the void pointer is assigned to a typed pointer
   */
  private class AssignmentFinder extends GimpleVisitor {
    private final GimpleCompilationUnit unit;
    private final GimpleFunction fn;
    private final GimpleVarDecl decl;
    private final Set<GimpleType> possibleTypes = Sets.newHashSet();

    public AssignmentFinder(GimpleCompilationUnit unit,
                            GimpleFunction fn, GimpleVarDecl decl) {
      this.unit = unit;
      this.fn = fn;
      this.decl = decl;
    }

    @Override
    public void visitAssignment(GimpleAssign assignment) {
      
      switch (assignment.getOperator()) {
      case VAR_DECL:
      case NOP_EXPR:
        if(isReference(assignment.getOperands().get(0))) {
          GimpleType type = inferTypeFromLHS(assignment.getLHS());
          if(type != null) {
            possibleTypes.add(type);
          }
        }
      }
    }

    /**
     * @return true if the given {@code expr} references our void pointer variable
     */
    private boolean isReference(GimpleExpr expr) {
      return expr instanceof GimpleVariableRef &&
          ((GimpleVariableRef) expr).getId() == decl.getId();
    }

    /**
     * Infers the type of the void pointer from the lhs to which it is assigned.
     */
    private GimpleType inferTypeFromLHS(GimpleLValue lhs) {
      if(lhs instanceof GimpleVariableRef) {
        // search local variables
        for(GimpleVarDecl decl : fn.getVariableDeclarations()) {
          if(decl.getId() == ((GimpleVariableRef) lhs).getId()) {
            return decl.getType();
          }
        }
        // search global variables
        for(GimpleVarDecl decl : unit.getGlobalVariables()) {
          if(decl.getName().equals(decl.getName())) {
            return decl.getType();
          }
        }
      }
      return null;
    }
  }
}
