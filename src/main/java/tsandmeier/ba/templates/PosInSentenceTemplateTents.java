package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * checks in which 1/10 of the sentence the annotation is
 */

public class PosInSentenceTemplateTents extends AbstractFeatureTemplate<PosInSentenceTemplateTents.PosInSentenceScope> {

    public PosInSentenceTemplateTents() {
        super();
    }

    public PosInSentenceTemplateTents(boolean cache) {
        super(cache);
    }


    static class PosInSentenceScope
            extends AbstractFactorScope {

        int indexInSentence;
        int numberOfTokensInSentence;
        EntityType entityType;

        public PosInSentenceScope(
                AbstractFeatureTemplate<PosInSentenceScope> template, int indexInSentence, int numberOfTokensInSentence, EntityType entityType) {
            super(template);
            this.indexInSentence = indexInSentence;
            this.numberOfTokensInSentence = numberOfTokensInSentence;
            this.entityType = entityType;
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
            PosInSentenceScope that = (PosInSentenceScope) o;
            return indexInSentence == that.indexInSentence &&
                    numberOfTokensInSentence == that.numberOfTokensInSentence &&
                    Objects.equals(entityType, that.entityType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), indexInSentence, numberOfTokensInSentence, entityType);
        }
    }

    @Override
    public List<PosInSentenceScope> generateFactorScopes(State state) {
        List<PosInSentenceScope> factors = new ArrayList<>();

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
            DocumentToken token = annotation.relatedTokens.get(0);
            factors.add(new PosInSentenceScope(this, annotation.getTokenizedSentenceOfAnnotation().indexOf(token), annotation.getTokenizedSentenceOfAnnotation().size(), annotation.entityType));
        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<PosInSentenceScope> factor) {

        int positionINSentence = factor.getFactorScope().indexInSentence;
        int numberOfTokensInSentence = factor.getFactorScope().numberOfTokensInSentence;

        double x = (double) (positionINSentence + 1) / (double) numberOfTokensInSentence;

        double xTemp = x * 10;

        int y = (int) Math.ceil(xTemp);

        factor.getFeatureVector().set("Im " + y + ". Zehntel des Satzes: " + factor.getFactorScope().entityType.name, true);

//        if (x < 0.33) {
//            factor.getFeatureVector().set("Im ersten Drittel: " + factor.getFactorScope().entityType.name, true);
//        } else if (x > 0.66) {
//            factor.getFeatureVector().set("Im letzten Drittel: " + factor.getFactorScope().entityType.name, true);
//        } else {
//            factor.getFeatureVector().set("Im zweiten Drittel:" + factor.getFactorScope().entityType.name, true);
//        }
    }
}
