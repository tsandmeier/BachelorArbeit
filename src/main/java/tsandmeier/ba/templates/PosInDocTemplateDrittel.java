package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class PosInDocTemplateDrittel extends AbstractFeatureTemplate<PosInDocTemplateDrittel.PosInDocScope> {

    public PosInDocTemplateDrittel() {
        super();
    }

    public PosInDocTemplateDrittel(boolean cache) {
        super(cache);
    }


    static class PosInDocScope
            extends AbstractFactorScope {

        EntityType entityType;
        double relativePosInDoc;

        public PosInDocScope(
                AbstractFeatureTemplate<PosInDocScope> template, EntityType entityType, double relativePosInDoc) {
            super(template);
            this.entityType = entityType;
            this.relativePosInDoc = relativePosInDoc;
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
            PosInDocScope that = (PosInDocScope) o;
            return Double.compare(that.relativePosInDoc, relativePosInDoc) == 0 &&
                    Objects.equals(entityType, that.entityType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), entityType, relativePosInDoc);
        }
    }

    @Override
    public List<PosInDocScope> generateFactorScopes(State state) {
        List<PosInDocScope> factors = new ArrayList<>();

        Document document = state.getInstance().getDocument();

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
            factors.add(new PosInDocScope(this, annotation.getEntityType(), ((double) annotation.documentPosition.docCharOffset/ (double) document.documentContent.length())));
        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<PosInDocScope> factor) {

        double relativePosInDoc = factor.getFactorScope().relativePosInDoc;

        if (relativePosInDoc < 0.33) {
            factor.getFeatureVector().set("Im ersten Drittel: " + factor.getFactorScope().entityType.name, true);
        } else if (relativePosInDoc > 0.66) {
            factor.getFeatureVector().set("Im zweiten Drittel: " + factor.getFactorScope().entityType.name, true);
        } else {
            factor.getFeatureVector().set("im letzten Drittel:" + factor.getFactorScope().entityType.name, true);
        }
    }
}
