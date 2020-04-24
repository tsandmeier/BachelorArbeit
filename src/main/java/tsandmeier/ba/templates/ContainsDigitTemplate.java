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
 * Checks if a mention starts with a capital, if it's not in the beginning of a sentence
 */
public class ContainsDigitTemplate extends AbstractFeatureTemplate<ContainsDigitTemplate.StartsWithCapitalScope> {


    static class StartsWithCapitalScope
            extends AbstractFactorScope {


        String surfaceForm;
        final public EntityType type;


        public StartsWithCapitalScope(AbstractFeatureTemplate<?> template, String surfaceForm, EntityType type) {
            super(template);
            this.type = type;
            this.surfaceForm = surfaceForm;
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
            StartsWithCapitalScope that = (StartsWithCapitalScope) o;
            return Objects.equals(surfaceForm, that.surfaceForm) &&
                    Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), surfaceForm, type);
        }
    }

    @Override
    public List<StartsWithCapitalScope> generateFactorScopes(State state) {
        List<StartsWithCapitalScope> factors = new ArrayList<>();

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {

            DocumentToken token = annotation.relatedTokens.get(0);
            factors.add(new StartsWithCapitalScope(this, annotation.getSurfaceForm(), annotation.getEntityType()));

        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<StartsWithCapitalScope> factor) {

        String surfaceForm = factor.getFactorScope().surfaceForm;
        EntityType type = factor.getFactorScope().type;
        boolean hasDigit = false;

        for(char letter : surfaceForm.toCharArray()){
            if(Character.isDigit(letter)){
                hasDigit = true;
                break;
            }
        }

        if(hasDigit){
            factor.getFeatureVector().set("Has Digit: <"+ factor.getFactorScope().type.name+">", true);
        }
    }

}
