package tsandmeier.ba.templates.NutzloseTemplates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class AvgNumberTemplate extends AbstractFeatureTemplate<AvgNumberTemplate.AvgNumberScope> {

	static class AvgNumberScope
			extends AbstractFactorScope {

		final public String surfaceForm;
		final public EntityType entityType;

		public AvgNumberScope(
                AbstractFeatureTemplate<AvgNumberScope> template, String surfaceForm, EntityType entityType) {
			super(template);
			this.surfaceForm = surfaceForm;
			this.entityType = entityType;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((surfaceForm == null) ? 0 : surfaceForm.hashCode());
			result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			AvgNumberScope other = (AvgNumberScope) obj;
			if (surfaceForm == null) {
				if (other.surfaceForm != null)
					return false;
			} else if (!surfaceForm.equals(other.surfaceForm))
				return false;
			if (entityType == null) {
				if (other.entityType != null)
					return false;
			} else if (!entityType.equals(other.entityType))
				return false;
			return true;
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
	public List<AvgNumberScope> generateFactorScopes(State state) {
		List<AvgNumberScope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
			String surfaceForm = annotation.getSurfaceForm();
			EntityType entityType = annotation.getEntityType();
			factors.add(new AvgNumberScope(this,surfaceForm, entityType));

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<AvgNumberScope> factor) {
		int numericCounter = 0;
		int tokensum = 0;
		List<String> numberTokens = new ArrayList<>();
		for (String token : factor.getFactorScope().surfaceForm.split("\\W")) {
//			featureVector.set(classLabel + token, true);
			if (isNumeric(token)){
				numberTokens.add(numericCounter,token);
				numericCounter++;
			}
		}
		if (numericCounter>0){
			for(String token : numberTokens) {
				tokensum += Integer.parseInt(token);
			}
			factor.getFeatureVector().set(factor.getFactorScope().entityType.entityName + " " + (tokensum/numericCounter),true);
		} else {
//			factor.getFeatureVector().set(factor.getFactorScope().entityType.entityName + " keine Zahl", true);
			//TODO: Fall vernünftig behandeln
		}
	}
	private boolean isNumeric(String text){
		return text.matches("\\d+");
	}

}
