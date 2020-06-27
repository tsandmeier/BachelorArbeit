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

/**
 * Checks the Number of other annotated mentions between two mentions
 */
public class NumberMBTemplate_FAST extends AbstractFeatureTemplate<NumberMBTemplate_FAST.NumberMBScope> {

    public NumberMBTemplate_FAST(boolean cache){super(cache);}

    public NumberMBTemplate_FAST() {
        super();
    }

    static class NumberMBScope
            extends AbstractFactorScope {

        long mentionCounter;

        String entityNameOne;
        String entityNameTwo;


        public NumberMBScope(
                AbstractFeatureTemplate<NumberMBScope> template, String entityOne, String entityTwo, long mentionCounter) {
            super(template);
            this.entityNameOne = entityOne;
            this.entityNameTwo = entityTwo;

            this.mentionCounter = mentionCounter;
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
            NumberMBScope that = (NumberMBScope) o;
            return mentionCounter == that.mentionCounter &&
                    Objects.equals(entityNameOne, that.entityNameOne) &&
                    Objects.equals(entityNameTwo, that.entityNameTwo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), mentionCounter, entityNameOne, entityNameTwo);
        }
    }

    @Override
    public List<NumberMBScope> generateFactorScopes(State state) {
        List<NumberMBScope> factors = new ArrayList<>();

        List<DocumentLinkedAnnotation> annotationList = super.<DocumentLinkedAnnotation>getPredictedAnnotations(state);

        long mentionCounter = 0;

        for(int i = 0; i < annotationList.size(); i++) {

            int indexOne = annotationList.get(i).relatedTokens.get(annotationList.get(i).relatedTokens.size()-1).getDocTokenIndex();

            for(int j = i+1; j < annotationList.size(); j++){

                int indexTwo = annotationList.get(i).relatedTokens.get(0).getDocTokenIndex();

                if(indexTwo > indexOne){
                    mentionCounter =
                    annotationList.stream().filter(e -> e.relatedTokens.get(e.relatedTokens.size()-1).getDocTokenIndex() < indexTwo
                    && e.relatedTokens.get(0).getDocTokenIndex() > indexOne).count();

                    //TODO: Bessere Lösung als stream? Verlangsamt immer noch sehr...


                    factors.add(new NumberMBScope(this, annotationList.get(i).getEntityType().name,
                            annotationList.get(j).entityType.name, mentionCounter));

                } else {
                    mentionCounter =
                    annotationList.stream().filter(e -> e.relatedTokens.get(e.relatedTokens.size()-1).getDocTokenIndex() < indexOne
                            && e.relatedTokens.get(0).getDocTokenIndex() > indexTwo).count();


                    factors.add(new NumberMBScope(this, annotationList.get(j).getEntityType().name,
                            annotationList.get(i).entityType.name, mentionCounter));
                }

            }

        }


        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<NumberMBScope> factor) {

        factor.getFeatureVector().set("Mentions between <" + factor.getFactorScope().entityNameOne + ", " + factor.getFactorScope().entityNameTwo + "> :" +
                factor.getFactorScope().mentionCounter, true);

    }

}
