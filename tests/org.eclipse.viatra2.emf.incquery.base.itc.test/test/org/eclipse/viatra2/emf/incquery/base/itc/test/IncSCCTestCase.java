package org.eclipse.viatra2.emf.incquery.base.itc.test;

import junit.framework.TestCase;

import org.eclipse.viatra2.emf.incquery.base.itc.alg.fw.FloydWarshallAlg;
import org.eclipse.viatra2.emf.incquery.base.itc.alg.incscc.IncSCCAlg;
import org.eclipse.viatra2.emf.incquery.base.itc.alg.misc.dfs.DFSAlg;
import org.eclipse.viatra2.emf.incquery.base.itc.graphimpl.Graph;
import org.eclipse.viatra2.emf.incquery.base.itc.test.graphs.Graph1;
import org.eclipse.viatra2.emf.incquery.base.itc.test.graphs.Graph2;
import org.eclipse.viatra2.emf.incquery.base.itc.test.graphs.Graph3;
import org.junit.Test;

public class IncSCCTestCase extends TestCase {

	public IncSCCTestCase() {
	}

	@Test
	public void testResult() {
		
		Graph1 g1 = new Graph1();
    	FloydWarshallAlg<Integer> fwa = new FloydWarshallAlg<Integer>(g1);
    	IncSCCAlg<Integer> isa = new IncSCCAlg<Integer>(g1);
		g1.modify();	
		assertEquals(true, isa.checkTcRelation(fwa.getTcRelation()));
        
        Graph2 g2 = new Graph2();
    	fwa = new FloydWarshallAlg<Integer>(g2);
    	isa = new IncSCCAlg<Integer>(g2);
		g2.modify();	
		assertEquals(true, isa.checkTcRelation(fwa.getTcRelation()));
        
        Graph3 g3 = new Graph3();
    	fwa = new FloydWarshallAlg<Integer>(g3);
    	isa = new IncSCCAlg<Integer>(g3);
		g3.modify();	
		assertEquals(true, isa.checkTcRelation(fwa.getTcRelation()));
		
		int nodeCount = 10;
		Graph<Integer> g = new Graph<Integer>();
		DFSAlg<Integer> da = new DFSAlg<Integer>(g);
		isa = new IncSCCAlg<Integer>(g);
		
		for (int i = 0; i < nodeCount; i++) {
			g.insertNode(i);
		}

		//System.out.println("insert");
		for (int i = 0; i < nodeCount; i++) {
			for (int j = 0; j < nodeCount; j++) {
				if (i != j) {
					g.insertEdge(i, j);
					assertEquals(true, isa.checkTcRelation(da.getTcRelation()));
				}
			}
		}

		//System.out.println("delete");
		for (int i = 0; i < nodeCount; i++) {
			for (int j = 0; j < nodeCount; j++) {
				if (i != j) {
					g.deleteEdge(i, j);
					assertEquals(true, isa.checkTcRelation(da.getTcRelation()));
				}
			}
		}
	}

	public static void main(String[] args) {
		junit.textui.TestRunner.run(IncSCCTestCase.class);
	}
}