package org.renjin.gcc.gimple;

import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.renjin.gcc.gimple.type.GimpleRecordTypeDef;
import org.renjin.gcc.gimple.type.GimpleType;

public class GimpleCompilationUnit {

  private final List<GimpleFunction> functions = Lists.newArrayList();
  private final List<GimpleRecordTypeDef> recordTypes = Lists.newArrayList();
  private final List<GimpleVarDecl> globalVariables = Lists.newArrayList();

  public List<GimpleFunction> getFunctions() {
    return functions;
  }

  public List<GimpleRecordTypeDef> getRecordTypes() {
    return recordTypes;
  }

  public List<GimpleVarDecl> getGlobalVariables() {
    return globalVariables;
  }

  @Override
  public String toString() {
    return Joiner.on("\n").join(functions);
  }

}
