package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;
import tsandmeier.ba.normalizer.interpreter.WeightInterpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 /**
 * checks the range of an age, if there is an age and an animal mentioned in the same document
 */

public class NormalizedWeightTemplate extends AbstractFeatureTemplate<NormalizedWeightTemplate.NormalizedWeightFactor> {


    private List<EntityType> animalTypeList = new ArrayList<EntityType>(Arrays.asList(EntityType.get("LewisRat"), EntityType.get("WistarRat"),
            EntityType.get("SpragueDawleyRat"), EntityType.get("LewisRat"), EntityType.get("C57_BL6_Mouse"), EntityType.get("ListerHoodedRat"),
            EntityType.get("LongEvansRat"), EntityType.get("FischerRat"), EntityType.get("RatSpecies"), EntityType.get("CatSpecies"),
            EntityType.get("DogSpecies")));

    static class NormalizedWeightFactor
            extends AbstractFactorScope {

        List<DocumentToken> tokens;
        EntityType type;


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            NormalizedWeightFactor that = (NormalizedWeightFactor) o;
            return Objects.equals(tokens, that.tokens) &&
                    Objects.equals(type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), tokens, type);
        }

        public NormalizedWeightFactor(AbstractFeatureTemplate template, List<DocumentToken> tokens, EntityType type) {
            super(template);
            this.tokens = tokens;
            this.type = type;
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

    }

    @Override
    public List<NormalizedWeightFactor> generateFactorScopes(State state) {
        List<NormalizedWeightFactor> factors = new ArrayList<>();

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
            for (DocumentLinkedAnnotation annotation2 : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
                if (annotation.getEntityType() == EntityType.get("Weight") && isAnimal(annotation2.getEntityType())) {
                    factors.add(new NormalizedWeightFactor(this, annotation.relatedTokens, annotation2.getEntityType()));
                }
            }


        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<NormalizedWeightFactor> factor) {
        WeightInterpreter sw = new WeightInterpreter(makeString(factor.getFactorScope().tokens)).normalize();
        if (sw.getMeanValue() > 10 && sw.getMeanValue() < 50) {
            factor.getFeatureVector().set("Weight in range 10-50 grams: <" + factor.getFactorScope().type.entityName + ">", true);
        }
        if (sw.getMeanValue() > 150 && sw.getMeanValue() < 450) {
            factor.getFeatureVector().set("Weight in range 150-450 grams: <" + factor.getFactorScope().type.entityName + ">", true);
        }
        if (sw.getMeanValue() > 10000 && sw.getMeanValue() < 30000) {
            factor.getFeatureVector().set("Weight in range 10000-30000 grams: <" + factor.getFactorScope().type.entityName + ">", true);
        }
        if (sw.getMeanValue() > 5000 && sw.getMeanValue() < 15000) {
            factor.getFeatureVector().set("Weight in range 5000-15000 grams: <" + factor.getFactorScope().type.entityName + ">", true);
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
