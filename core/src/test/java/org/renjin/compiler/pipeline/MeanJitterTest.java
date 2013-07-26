package org.renjin.compiler.pipeline;

import org.junit.Test;
import org.renjin.primitives.R$primitive$$times$deferred_dd;
import org.renjin.primitives.matrix.DeferredRowMeans;
import org.renjin.primitives.matrix.TransposingMatrix;
import org.renjin.primitives.summary.DeferredMean;
import org.renjin.sexp.AttributeMap;
import org.renjin.sexp.DoubleArrayVector;
import org.renjin.sexp.Null;
import org.renjin.sexp.Vector;

public class MeanJitterTest {

  @Test
  public void testSimpleArray() {

    DoubleArrayVector vector = new DoubleArrayVector(1,2,3);
    DeferredMean mean = new DeferredMean(vector, AttributeMap.EMPTY);
    DeferredGraph graph = new DeferredGraph(mean);

    DeferredJitter jitter = new DeferredJitter();
    JittedComputation computation = jitter.compile(graph.getRoot());

    double [] result = computation.compute(
            graph.getRoot().flattenVectors());

    System.out.println(result[0]);
  }



  private Vector compute(DeferredGraph graph) {
    return Null.INSTANCE;
  }

  @Test
  public void transposed() {

    DoubleArrayVector vector = new DoubleArrayVector(new double[] { 1,2,3,4,5,6,7,8,9,10,11,12 } ,
            AttributeMap.dim(4,3));
    TransposingMatrix t = new TransposingMatrix(vector, AttributeMap.dim(3,4));
    DeferredMean mean = new DeferredMean(t, AttributeMap.EMPTY);
    DeferredGraph graph = new DeferredGraph(mean);

    compute(graph);
  } 

  @Test
  public void nestedDistanceMatrixAndBinaryOp() {

    DoubleArrayVector a = new DoubleArrayVector(1,2,3,4);
    DoubleArrayVector b = new DoubleArrayVector(10,20,30,40,50,60,70,80);

    Vector times = new R$primitive$$times$deferred_dd(a, b, AttributeMap.EMPTY);
    DeferredMean mean = new DeferredMean(times, AttributeMap.EMPTY);
    DeferredGraph graph = new DeferredGraph(mean);

    Vector x = compute(graph);
    System.out.println(x);
  }

  @Test
  public void rowMeans() {

    DoubleArrayVector a = new DoubleArrayVector(1,2,3,4,5,6,7,8,9,10,11,12 );

    DeferredRowMeans rowMeans = new DeferredRowMeans(a, 4, AttributeMap.EMPTY);
    DeferredGraph graph = new DeferredGraph(rowMeans);

    Vector x = compute(graph);
    System.out.println(x);
  }
}
