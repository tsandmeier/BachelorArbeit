package tsandmeier.ba.explorer;

import de.hterhors.semanticmr.crf.variables.State;

import java.util.Iterator;
import java.util.List;

public interface IExplorationStrategyCustom extends Iterator<State> {

	public List<State> explore(State currentState);

	public void set(State state);

	public void setSentenceIndex(int sentenceIndex);

}
