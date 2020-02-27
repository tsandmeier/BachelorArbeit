package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * looks for one to a set number of words after a mention
 */

public class AMFLTemplate_SLOW extends AbstractFeatureTemplate<AMFLTemplate_SLOW.AMFLScope> {

    public AMFLTemplate_SLOW() {
        super();
    }

    public AMFLTemplate_SLOW(boolean cache) {
        super(cache);
    }

    private static final int NUMBER_OF_WORDS = 5;

    static class AMFLScope
            extends AbstractFactorScope {

        String entityTypeName;
        String wordsAfter;


        public AMFLScope(
                AbstractFeatureTemplate<AMFLScope> template, String entityTypeName, String wordsAfter) {
            super(template);
            this.entityTypeName = entityTypeName;
            this.wordsAfter = wordsAfter;
        }

        @Override
        public int implementHashCode() {
            return 0;
        }

        @Override
        public boolean implementEquals(Object obj) {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            AMFLScope amflScope = (AMFLScope) o;
            return Objects.equals(entityTypeName, amflScope.entityTypeName) &&
                    Objects.equals(wordsAfter, amflScope.wordsAfter);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), entityTypeName, wordsAfter);
        }
    }

    @Override
    public List<AMFLScope> generateFactorScopes(State state) {
        List<AMFLScope> factors = new ArrayList<>();

        Document document = state.getInstance().getDocument();

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
            DocumentToken token = annotation.relatedTokens.get(annotation.relatedTokens.size() - 1);

            for (int i = 0; i <= NUMBER_OF_WORDS; i++) {
                if (annotation.relatedTokens.get(annotation.relatedTokens.size() - 1).getDocTokenIndex() + i <= document.tokenList.size() - 1) {
                    factors.add(new AMFLScope(this, annotation.getEntityType().name, document.getContent(annotation.relatedTokens.get(annotation.relatedTokens.size() - 1),
                            document.tokenList.get(annotation.relatedTokens.get(annotation.relatedTokens.size() - 1).getDocTokenIndex() + i))));
                }
            }
        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<AMFLScope> factor) {

        factor.getFeatureVector().set("Words after <" + factor.getFactorScope().entityTypeName + "> : " +
                factor.getFactorScope().wordsAfter, true);
    }
}
