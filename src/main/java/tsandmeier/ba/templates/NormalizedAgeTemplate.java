package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;
import de.uni.bielefeld.sc.hterhors.psink.scio.semanticmr.literal_normalization.interpreter.AgeInterpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Checks the range of an age, if there is an age and an animal mentioned in the same document.
 */

public class NormalizedAgeTemplate extends AbstractFeatureTemplate<NormalizedAgeTemplate.NormalizedAgeFactor> {


    private List<EntityType> animalTypeList = new ArrayList<EntityType>(Arrays.asList(EntityType.get("LewisRat"), EntityType.get("WistarRat"),
            EntityType.get("SpragueDawleyRat"), EntityType.get("LewisRat"), EntityType.get("C57_BL6_Mouse"), EntityType.get("ListerHoodedRat"),
            EntityType.get("LongEvansRat"), EntityType.get("FischerRat"), EntityType.get("RatSpecies"), EntityType.get("CatSpecies"),
            EntityType.get("DogSpecies")));

    static class NormalizedAgeFactor
            extends AbstractFactorScope {

        List<DocumentToken> tokens;
        EntityType type;


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            NormalizedAgeFactor that = (NormalizedAgeFactor) o;
            return Objects.equals(tokens, that.tokens) &&
                    Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), tokens, type);
        }

        public NormalizedAgeFactor(AbstractFeatureTemplate template, List<DocumentToken> tokens, EntityType type) {
            super(template);
            this.tokens = tokens;
            this.type = type;
        }

        @Override
        public int implementHashCode() {
            return 0;
        }

        @Override
        public boolean implementEquals(Object obj) {
            return false;
        }

    }

    @Override
    public List<NormalizedAgeFactor> generateFactorScopes(State state) {
        List<NormalizedAgeFactor> factors = new ArrayList<>();

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
            for (DocumentLinkedAnnotation annotation2 : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
                if (annotation.getEntityType() == EntityType.get("Age") && isAnimal(annotation2.getEntityType())) {
                    factors.add(new NormalizedAgeFactor(this, annotation.relatedTokens, annotation2.getEntityType()));
                }
            }


        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<NormalizedAgeFactor> factor) {
        AgeInterpreter ai = new AgeInterpreter(makeString(factor.getFactorScope().tokens)).normalize();
        if (ai.getMeanValue() < 800) {
            factor.getFeatureVector().set("Age under 2 years: <" + factor.getFactorScope().type.name + ">", true);
        }
        if (ai.getMeanValue() < 5500) {
            factor.getFeatureVector().set("Age under 15 years: <" + factor.getFactorScope().type.name + ">", true);
        }
        if (ai.getMeanValue() < 3650) {
            factor.getFeatureVector().set("Age under 10 years: <" + factor.getFactorScope().type.name + ">", true);
        }

    }

    private String makeString(List<DocumentToken> list) {
        String text = "";
        for (int i = 0; i < list.size(); i++) {
            text = text + list.get(i).getText();
        }
        return text;
    }

    public boolean isAnimal(EntityType type) {
        return animalTypeList.contains(type);
    }

}
