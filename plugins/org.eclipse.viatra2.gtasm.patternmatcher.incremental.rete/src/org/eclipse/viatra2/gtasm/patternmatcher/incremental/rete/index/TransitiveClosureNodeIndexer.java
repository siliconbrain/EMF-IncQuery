package org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.index;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.viatra2.emf.incquery.base.itc.alg.incscc.IncSCCAlg;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.network.Direction;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.network.Receiver;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.single.TransitiveClosureNode;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.tuple.FlatTuple;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.tuple.MaskedTuple;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.tuple.Tuple;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.tuple.TupleMask;

public class TransitiveClosureNodeIndexer extends StandardIndexer implements IterableIndexer {
	private TransitiveClosureNode tcNode;
	private IncSCCAlg<Object> tcAlg;
	private Collection<Tuple> emptySet;
	
	public TransitiveClosureNodeIndexer(TupleMask mask, IncSCCAlg<Object> tcAlg, TransitiveClosureNode tcNode) {
		super(tcNode.getContainer(), mask);
		this.tcAlg = tcAlg;
		this.tcNode = tcNode;
		this.emptySet = Collections.emptySet();
	}
	
	@Override
	public Collection<Tuple> get(Tuple signature) {
		if (signature.getSize() == mask.sourceWidth) {
			if (mask.indices.length == 0) {
				//mask ()/2
				return getSignatures();
			}
			else if (mask.indices.length == 1) {
				Set<Tuple> retSet = new HashSet<Tuple>();
				
				//mask (0)/2
				if (mask.indices[0] == 0) {
					Object source = signature.get(0);
					for (Object target : tcAlg.getAllReachableTargets(source)) {
						retSet.add(new FlatTuple(source, target));
					}
					return retSet;
				}
				//mask (1)/2
				if (mask.indices[0] == 1) {
					Object target = signature.get(1);
					for (Object source : tcAlg.getAllReachableSources(target)) {
						retSet.add(new FlatTuple(source, target));
					}
					return retSet;
				}
			}
			else {
				//mask (0,1)/2
				if (mask.indices[0] == 0 && mask.indices[1] == 1) {
					Object source = signature.get(0);
					Object target = signature.get(1);
					Tuple singleton = new FlatTuple(new FlatTuple(source, target));
					return (tcAlg.isReachable(source, target) ? Collections.singleton(singleton) : emptySet);	
				}
				//mask (1,0)/2
				if (mask.indices[0] == 1 && mask.indices[1] == 0) {
					Object source = signature.get(1);
					Object target = signature.get(0);
					Tuple singleton = new FlatTuple(new FlatTuple(source, target));
					return (tcAlg.isReachable(source, target) ? Collections.singleton(singleton) : emptySet);
				}
			}
		}
		return null;
	}
	
	public Collection<Tuple> getSignatures() {
		return asTupleCollection(tcAlg.getTcRelation());
	}

	public Iterator<Tuple> iterator() {
		return asTupleCollection(tcAlg.getTcRelation()).iterator();	
	}
	
	private Collection<Tuple> asTupleCollection(Collection<org.eclipse.viatra2.emf.incquery.base.itc.alg.misc.Tuple<Object>> tuples) {
		Set<Tuple> retSet = new HashSet<Tuple>();
		for (org.eclipse.viatra2.emf.incquery.base.itc.alg.misc.Tuple<Object> tuple : tuples) {
			retSet.add(new FlatTuple(tuple.getSource(), tuple.getTarget()));
		}
		return retSet;
	}
	
	public void propagate(Direction direction, Tuple updateElement, boolean change) {
		propagate(direction, updateElement, new MaskedTuple(updateElement, mask), change);
	}

	@Override
	public Receiver getActiveNode() {
		return tcNode;
	}
}
