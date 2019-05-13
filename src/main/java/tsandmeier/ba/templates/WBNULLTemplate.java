package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.factor.AbstractFactorScope;
import de.hterhors.semanticmr.crf.factor.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.State;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author hterhors
 *
 * @date Nov 15, 2017
 */
public class WBNULLTemplate extends AbstractFeatureTemplate<WBNULLTemplate.WBNULLScope> {

	static class WBNULLScope
			extends AbstractFactorScope<WBNULLScope> {

		public String surfaceFormOne;
		public String surfaceFormTwo;
		public EntityType typeOne;
		public EntityType typeTwo;

		public int beginWordTwo;
		public int endWordOne;


		public WBNULLScope(
                AbstractFeatureTemplate<WBNULLScope> template, int beginWordTwo, int endWordOne,
                EntityType typeOne, EntityType typeTwo) {
			super(template);
			this.endWordOne = endWordOne;
			this.beginWordTwo = beginWordTwo;
			this.typeOne = typeOne;
			this.typeTwo = typeTwo;
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

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;
			WBNULLScope that = (WBNULLScope) o;
			return beginWordTwo == that.beginWordTwo &&
					endWordOne == that.endWordOne &&
					Objects.equals(surfaceFormOne, that.surfaceFormOne) &&
					Objects.equals(surfaceFormTwo, that.surfaceFormTwo) &&
					Objects.equals(typeOne, that.typeOne) &&
					Objects.equals(typeTwo, that.typeTwo);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), surfaceFormOne, surfaceFormTwo, typeOne, typeTwo, beginWordTwo, endWordOne);
		}
	}

	@Override
	public List<WBNULLScope> generateFactorScopes(State state) {
//		writeUsingFileWriter(state.getInstance().getDocument().documentContent);
//		riteUsingFileWriter("Hallo");

		List<WBNULLScope> factors = new ArrayList<>();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
			for (DocumentLinkedAnnotation annotation2 :super.<DocumentLinkedAnnotation>getPredictedAnnotations(state))
				if(!annotation.equals(annotation2)){
					int beginWordTwo = annotation2.getStartDocCharOffset();
					int endWordOne = annotation.getEndDocCharOffset();
					EntityType typeOne = annotation.entityType;
					EntityType typeTwo = annotation2.entityType;
							factors.add(new WBNULLScope(this, beginWordTwo, endWordOne, typeOne, typeTwo));
				}

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<WBNULLScope> factor) {
//		if(!isWordBetween(factor.getFactorScope().beginWordTwo, factor.getFactorScope().endWordOne)) {
			factor.getFeatureVector().set("IS_WORD_BETWEEN_" + factor.getFactorScope().typeOne.entityName+" "+factor.getFactorScope().typeTwo.entityName+" "+isWordBetween(factor.getFactorScope().beginWordTwo, factor.getFactorScope().endWordOne), true);
//			factor.getFeatureVector().set("NO_WORD_BETWEEN_FOR_TYPE_" + factor.getFactorScope().typeTwo.entityName, true);
//			factor.getFeatureVector().set("NO_WORD_BETWEEN ", true);
//		}
	}

	private boolean isWordBetween(int beginWordTwo, int endWordOne){
		return (beginWordTwo - endWordOne >= 2) || (beginWordTwo - endWordOne <= -2);
	}

	private static void writeUsingFileWriter(String data) {
		File file = new File("/home/tobias/Projekte/SemanticMachineReading/bla/inhalt.txt");
		FileWriter fr = null;
		try {
			fr = new FileWriter(file);
			fr.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			//close resources
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}