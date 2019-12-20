package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * identity of a mention
 */
public class IdentityTemplate extends AbstractFeatureTemplate<IdentityTemplate.IdentityScope> {



	static class IdentityScope
			extends AbstractFactorScope {

		EntityType type;
		String surfaceForm;


		public IdentityScope(AbstractFeatureTemplate template, EntityType type, String surfaceForm) {
			super(template);
			this.type = type;
			this.surfaceForm = surfaceForm;
		}

		@Override
		public int implementHashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;
			IdentityScope that = (IdentityScope) o;
			return Objects.equals(type, that.type) &&
					Objects.equals(surfaceForm, that.surfaceForm);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), type, surfaceForm);
		}

		@Override
		public boolean implementEquals(Object obj) {
			return false;
		}

	}

	@Override
	public List<IdentityScope> generateFactorScopes(State state) {
		List<IdentityScope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {

			factors.add(new IdentityScope(this, annotation.getEntityType(), annotation.getSurfaceForm()));

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<IdentityScope> factor) {

		if(factor.getFactorScope().type.entityName.equals(factor.getFactorScope().surfaceForm)) {
			factor.getFeatureVector().set("Exact Match: <"+ factor.getFactorScope().type.entityName+"> "+ factor.getFactorScope().surfaceForm, true);

			Set<EntityType> parentEntity = factor.getFactorScope().type.getDirectSuperEntityTypes();
			for(EntityType parent : parentEntity) {

				factor.getFeatureVector().set("Exact Match for ChildEntity: <" + parent.entityName + "> " + factor.getFactorScope().surfaceForm, true);
			}
		}

	}

}
