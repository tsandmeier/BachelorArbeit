package tsandmeier.ba.evaluator;

import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.AbstractEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.eval.NerlaEvaluator;
import java.util.List;

public class NerlaObjectiveFunctionPartialOverlap implements IObjectiveFunction {

    final private NerlaEvaluatorPartialOverlap evaluator;

    public NerlaObjectiveFunctionPartialOverlap(EEvaluationDetail evaluationDetail) {
        this.evaluator = new NerlaEvaluatorPartialOverlap(evaluationDetail);
    }

    public NerlaObjectiveFunctionPartialOverlap() {
        this(EEvaluationDetail.DOCUMENT_LINKED);
    }

    @Override
    public void score(State state) {
        state.setObjectiveScore(state.getMicroScore(evaluator).getF1());
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