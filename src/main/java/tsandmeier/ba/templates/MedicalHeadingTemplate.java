package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;
import tsandmeier.ba.mesh.MeshConcept;
import tsandmeier.ba.mesh.MeshDescriptor;
import tsandmeier.ba.mesh.MeshTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * first word of a mention
 */
public class MedicalHeadingTemplate extends AbstractFeatureTemplate<MedicalHeadingTemplate.MedicalHeadingScope> {

	List<MeshDescriptor> descriptorList;

	public MedicalHeadingTemplate(List<MeshDescriptor> descriptorList, boolean cache){
		super(cache);
		this.descriptorList = descriptorList;
	}

	public MedicalHeadingTemplate(List<MeshDescriptor> descriptorList) {
		super();
		this.descriptorList = descriptorList;
	}

	static class MedicalHeadingScope
			extends AbstractFactorScope {

		EntityType type;
		String medicalHeading;


		public MedicalHeadingScope(
				AbstractFeatureTemplate<MedicalHeadingScope> template, EntityType type, String medicalHeading) {
			super(template);
			this.type = type;
			this.medicalHeading = medicalHeading;
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
			MedicalHeadingScope that = (MedicalHeadingScope) o;
			return Objects.equals(type, that.type) &&
					Objects.equals(medicalHeading, that.medicalHeading);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), type, medicalHeading);
		}

		@Override
		public boolean implementEquals(Object obj) {
			return false;
		}
	}

	@Override
	public List<MedicalHeadingScope> generateFactorScopes(State state) {
		List<MedicalHeadingScope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
			for(MeshDescriptor descriptor : descriptorList){
				for(MeshConcept concept : descriptor.getConcepts()){
					for(MeshTerm term : concept.getTerms()){
						if(term.getName().equals(annotation.getSurfaceForm())){
							factors.add(new MedicalHeadingScope(this, annotation.entityType, descriptor.getName()));
						}
					}
				}
			}


		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<MedicalHeadingScope> factor) {
		factor.getFeatureVector().set("Descriptor of <"+factor.getFactorScope().type.name + ">: " + factor.getFactorScope().medicalHeading, true);
	}

}
