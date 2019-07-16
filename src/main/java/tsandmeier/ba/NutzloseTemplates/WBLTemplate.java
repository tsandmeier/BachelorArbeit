package tsandmeier.ba.NutzloseTemplates;

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
 * last word inbetween if more than two words in betweeen
 */
public class WBLTemplate extends AbstractFeatureTemplate<WBLTemplate.WBLScope> {

    static class WBLScope
            extends AbstractFactorScope {

        DocumentToken tokenOne;
        DocumentToken tokenTwo;

        EntityType typeOne;
        EntityType typeTwo;

        Document document;

        public WBLScope(AbstractFeatureTemplate template, DocumentToken tokenOne, DocumentToken tokenTwo, EntityType typeOne, EntityType typeTwo, Document document) {
            super(template);
            this.tokenOne = tokenOne;
            this.tokenTwo = tokenTwo;
            this.typeOne = typeOne;
            this.typeTwo = typeTwo;
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
            WBLScope that = (WBLScope) o;
            return Objects.equals(tokenOne, that.tokenOne) &&
                    Objects.equals(tokenTwo, that.tokenTwo) &&
                    Objects.equals(typeOne, that.typeOne) &&
                    Objects.equals(typeTwo, that.typeTwo) &&
                    Objects.equals(document, that.document);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), tokenOne, tokenTwo, typeOne, typeTwo, document);
        }
    }

    @Override
    public List<WBLScope> generateFactorScopes(State state) {
        List<WBLScope> factors = new ArrayList<>();
        Document document = state.getInstance().getDocument();

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
            for (DocumentLinkedAnnotation annotation2 : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
                if (!annotation.equals(annotation2)) {

                    factors.add(new WBLScope(this,
                            annotation.relatedTokens.get(annotation.relatedTokens.size() - 1),
                            annotation2.relatedTokens.get(0),
                            annotation.entityType, annotation2.entityType,
                            document));
                }
            }
        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<WBLScope> factor) {

        if (factor.getFactorScope().tokenOne.getDocTokenIndex() < factor.getFactorScope().tokenTwo.getDocTokenIndex() &&
                factor.getFactorScope().tokenTwo.getDocTokenIndex() - factor.getFactorScope().tokenOne.getDocTokenIndex() > 3) {

                String lastWordBetween = factor.getFactorScope().document.tokenList.get(factor.getFactorScope().tokenTwo.getDocTokenIndex()-1).getText();
                factor.getFeatureVector().set("WBL: <" + factor.getFactorScope().typeOne.entityName + " "
                        + factor.getFactorScope().typeTwo.entityName + "> " + lastWordBetween, true);



        }
    }

    private String[] tokenizeString(String text) {
        return text.split("\\s+");
    }
}
