package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;
import tsandmeier.ba.normalizer.interpreter.WeightInterpreter;
import tsandmeier.ba.normalizer.interpreter.struct.ISingleUnit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class NormalizedWeightTemplate extends AbstractFeatureTemplate<NormalizedWeightTemplate.EmptyScope> {


	static class EmptyScope
			extends AbstractFactorScope {

		List<DocumentToken> tokens;



		public EmptyScope(
                AbstractFeatureTemplate<EmptyScope> template, List<DocumentToken> tokens) {
			super(template);
			this.tokens = tokens;
		}

		@Override
		public int implementHashCode() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean implementEquals(Object obj) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	@Override
	public List<EmptyScope> generateFactorScopes(State state) {
		List<EmptyScope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {

			if(annotation.getEntityType() == EntityType.get("Weight")) {
				factors.add(new EmptyScope(this, annotation.relatedTokens));
			}


		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<EmptyScope> factor) {
		WeightInterpreter sw = new WeightInterpreter(makeString(factor.getFactorScope().tokens));
		boolean isInRange;
		if(sw.getUnit().equals("g") )
		if(sw.getMeanValue() > 150 && sw.getMeanValue() < 450)
				 factor.getFeatureVector().set("Normalized Weight: " + sw.getMeanValue(), true);
	}

	private String makeString (List<DocumentToken> list){
		String text = "";
		for (int i = 0; i < list.size(); i++){
			text = text + list.get(i).getText();
		}
		return text;
	}

}
