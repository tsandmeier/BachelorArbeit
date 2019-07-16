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
 * looks for the words after a mention
 * joins AM1F and AM1L and even more
 */
public class AMFLTemplate extends AbstractFeatureTemplate<AMFLTemplate.AMFLScope> {

    public AMFLTemplate (boolean cache) {
        super(cache);
    }

    private static final int NUMBER_OF_WORDS = 3;

    static class AMFLScope
            extends AbstractFactorScope {

        DocumentToken token;
        EntityType type;
        Document document;

        public AMFLScope(
                AbstractFeatureTemplate<AMFLScope> template, Document document, DocumentToken token, EntityType type) {
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
        public boolean implementEquals(Object obj) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            AMFLScope amflScope = (AMFLScope) o;
            return Objects.equals(token, amflScope.token) &&
                    Objects.equals(type, amflScope.type) &&
                    Objects.equals(document, amflScope.document);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), token, type, document);
        }
    }

    @Override
    public List<AMFLScope> generateFactorScopes(State state) {
        List<AMFLScope> factors = new ArrayList<>();

        Document document = state.getInstance().getDocument();

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
            DocumentToken token = annotation.relatedTokens.get(annotation.relatedTokens.size() - 1);
            factors.add(new AMFLScope(this, document, token, annotation.getEntityType()));
        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<AMFLScope> factor) {
        DocumentToken lastToken;
        String subtext;
        for (int i = 1; i <= NUMBER_OF_WORDS; i++) {
            int tokenIndex = factor.getFactorScope().token.getDocTokenIndex();
            if (tokenIndex + i <= factor.getFactorScope().document.tokenList.size() - 1) {
                lastToken = factor.getFactorScope().document.tokenList.get(tokenIndex + i);
                subtext = factor.getFactorScope().document.getContent(
                        factor.getFactorScope().document.tokenList.get(tokenIndex+1), lastToken);

                factor.getFeatureVector().set("Words after <"+factor.getFactorScope().type.entityName + ">: " + subtext, true);
            }
        }
    }
}
