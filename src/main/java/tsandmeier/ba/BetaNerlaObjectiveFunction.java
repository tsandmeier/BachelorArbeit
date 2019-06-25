package tsandmeier.ba;

import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.AbstractEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.eval.NerlaEvaluator;

import java.util.List;

public class BetaNerlaObjectiveFunction implements IObjectiveFunction {

	final private NerlaEvaluator evaluator;

	public BetaNerlaObjectiveFunction(EEvaluationDetail evaluationDetail) {
		this.evaluator = new NerlaEvaluator(evaluationDetail);
	}

	public BetaNerlaObjectiveFunction() {
		this(EEvaluationDetail.DOCUMENT_LINKED);
	}

	@Override
	public void score(State state) {
		state.setObjectiveScore(state.score(evaluator).getFbeta(4));
	}

	@Override
	public void score(List<State> states) {
		states.parallelStream().forEach(state -> score(state));
	}

	@Override
	public AbstractEvaluator getEvaluator() {
		return evaluator;
	}

}
