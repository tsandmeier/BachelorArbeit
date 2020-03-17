package tsandmeier.ba;

import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.AbstractEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.eval.NerlaEvaluator;

import java.util.List;

public class BetaNerlaObjectiveFunction implements IObjectiveFunction {

	final private NerlaEvaluator evaluator;

	int beta = 2;

	public BetaNerlaObjectiveFunction(EEvaluationDetail evaluationDetail) {
		this.evaluator = new NerlaEvaluator(evaluationDetail);
	}

	public BetaNerlaObjectiveFunction(EEvaluationDetail evaluationDetail, int beta) {
		this.evaluator = new NerlaEvaluator(evaluationDetail);
		this.beta = beta;
	}

	public BetaNerlaObjectiveFunction() {
		this(EEvaluationDetail.DOCUMENT_LINKED);
	}

	@Override
	public void score(State state) {
		state.setObjectiveScore(state.getMicroScore().getFbeta(beta));
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
