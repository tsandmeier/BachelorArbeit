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
 * looks for one to a set number of words after a mention
 */

public class NumberOfAnnotationsTemplate extends AbstractFeatureTemplate<NumberOfAnnotationsTemplate.AMFLScope> {

    public NumberOfAnnotationsTemplate() {
        super();
    }

    public NumberOfAnnotationsTemplate(boolean cache) {
        super(cache);
    }

    private static final int NUMBER_OF_WORDS = 5;

    static class AMFLScope
            extends AbstractFactorScope {

        int numberOfAnnotations;
        EntityType type;

        public AMFLScope(
                AbstractFeatureTemplate<AMFLScope> template, int numberOfAnnotations, EntityType type) {
            super(template);
            this.type = type;
            this.numberOfAnnotations = numberOfAnnotations;
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
            return numberOfAnnotations == amflScope.numberOfAnnotations &&
                    Objects.equals(type, amflScope.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), numberOfAnnotations, type);
        }
    }



    @Override
    public List<AMFLScope> generateFactorScopes(State state) {
        List<AMFLScope> factors = new ArrayList<>();

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
            factors.add(new AMFLScope(this, super.getPredictedAnnotations(state).size(), annotation.getEntityType()));
        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<AMFLScope> factor) {

        int numberOfAnnotations = factor.getFactorScope().numberOfAnnotations;

        if(numberOfAnnotations > 3) {
            factor.getFeatureVector().set("More than 3 entities in Document: <" + factor.getFactorScope().type.name + ">", true);
        } if(numberOfAnnotations > 5) {
            factor.getFeatureVector().set("More than 5 entities in Document: <" + factor.getFactorScope().type.name + ">", true);
        } if(numberOfAnnotations >10) {
            factor.getFeatureVector().set("More than 10 entities in Document: <" + factor.getFactorScope().type.name + ">", true);
        } if(numberOfAnnotations >20){
            factor.getFeatureVector().set("More than 20 entities in Document: <" + factor.getFactorScope().type.name + ">", true);
        }
    }
}
