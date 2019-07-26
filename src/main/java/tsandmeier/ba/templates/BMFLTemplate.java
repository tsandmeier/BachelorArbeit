package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * looks for the words before a mention
 * joins BM1F and BM1L and even more
 * looks for second/third word before mention
 */
public class BMFLTemplate extends AbstractFeatureTemplate<BMFLTemplate.BMFLScope> {

	public BMFLTemplate (boolean cache){
		super(cache);
	}

	public BMFLTemplate() {
		super();
	}

	private static final int NUMBER_OF_WORDS = 5;

	static class BMFLScope
			extends AbstractFactorScope {

		DocumentToken token;
		EntityType type;
		Document document;

		public BMFLScope(
				AbstractFeatureTemplate<BMFLScope> template, Document document, DocumentToken token, EntityType type) {
			super(template);
			this.token = token;
			this.type = type;
			this.document = document;
		}

		@Override
		public int implementHashCode() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			if (!super.equals(o)) return false;
			BMFLScope bmflScope = (BMFLScope) o;
			return Objects.equals(token, bmflScope.token) &&
					Objects.equals(type, bmflScope.type) &&
					Objects.equals(document, bmflScope.document);
		}

		@Override
		public int hashCode() {
			return Objects.hash(super.hashCode(), token, type, document);
		}

		@Override
		public boolean implementEquals(Object obj) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	@Override
	public List<BMFLScope> generateFactorScopes(State state) {
		List<BMFLScope> factors = new ArrayList<>();

		Document document = state.getInstance().getDocument();

		for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
			DocumentToken token = annotation.relatedTokens.get(0);
			factors.add(new BMFLScope(this, document, token, annotation.getEntityType()));

		}
		return factors;
	}

	@Override
	public void generateFeatureVector(Factor<BMFLScope> factor) {
		DocumentToken firstToken;
		String subtext;
		for (int i = 2; i <= NUMBER_OF_WORDS; i++) {
			if(factor.getFactorScope().token.getDocTokenIndex() -i >= 0) {
				firstToken = factor.getFactorScope().document.tokenList.get(factor.getFactorScope().token.getDocTokenIndex() - i);
				subtext = factor.getFactorScope().document.getContent(firstToken, factor.getFactorScope().document.tokenList.get(factor.getFactorScope().token.getDocTokenIndex() - 1));

				factor.getFeatureVector().set("Words before <"+factor.getFactorScope().type.entityName + ">: " + subtext, true);
				factor.getFeatureVector().set(i+"  Words before <"+factor.getFactorScope().type.entityName + ">: " + factor.getFactorScope().document.tokenList.get(factor.getFactorScope().token.getDocTokenIndex() - i).getText(), true);
			}
		}
	}
}
