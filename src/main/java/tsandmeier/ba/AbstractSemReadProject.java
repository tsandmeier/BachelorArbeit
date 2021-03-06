package tsandmeier.ba;

import de.hterhors.semanticmr.crf.helper.log.LogUtils;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.CartesianEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.init.specifications.SystemScope;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Map.Entry;

public class AbstractSemReadProject {
	CartesianEvaluator c = new CartesianEvaluator(EEvaluationDetail.LITERAL);

	public AbstractSemReadProject(SystemScope build) {
	}

	public Score evaluate(Logger log, Map<Instance, State> testResults) {
		Score mean = new Score();
		int countInstances = testResults.entrySet().size();
		int counter = 0;
		for (Entry<Instance, State> res : testResults.entrySet()) {
			counter++;
//			if (res.getKey().getDocument().documentID.equals("N008 Jin 2016 26852702")) {
//				System.out.println(c.scoreMultiValues(res.getKey().getGoldAnnotations().getAnnotations(),
//						res.getValue().getCurrentPredictions().getAnnotations()));
//			}




			Score score = res.getValue().getMicroScore();

//			if(score.isMacro()) {
//				score.setMacroToMicro();
//			}

			mean.add(score);


			LogUtils.logState(log, "["+counter+"/"+countInstances+"]"+"======Final Evaluation======", res.getKey(), res.getValue());



		}
		log.info("Mean Score: " + mean);
		return mean;
	}
}
