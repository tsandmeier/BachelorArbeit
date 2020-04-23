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
 * checks all the single words in a mention
 */

public class SurfaceFormTemplate extends AbstractFeatureTemplate<SurfaceFormTemplate.SurfaceFormScope> {

    public SurfaceFormTemplate(boolean cache) {
        super(cache);
    }

    public SurfaceFormTemplate() {
        super();
    }

    class SurfaceFormScope extends AbstractFactorScope {

        String surfaceForm;
        final public EntityType entityType;

        public SurfaceFormScope(AbstractFeatureTemplate template, String surfaceForm, EntityType entityType) {
            super(template);
            this.surfaceForm = surfaceForm;
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
            SurfaceFormScope that = (SurfaceFormScope) o;
            return Objects.equals(surfaceForm, that.surfaceForm) &&
                    Objects.equals(entityType, that.entityType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), surfaceForm, entityType);
        }
    }

    @Override
    public List<SurfaceFormScope> generateFactorScopes(State state) {
        final List<SurfaceFormScope> factors = new ArrayList<>();
        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
            EntityType entityType = annotation.getEntityType();
            factors.add(new SurfaceFormScope(this, annotation.getSurfaceForm(), entityType));

        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<SurfaceFormScope> factor) {
        factor.getFeatureVector().set("Surfaceform of: <" + factor.getFactorScope().entityType.name + ">: " +
                factor.getFactorScope().surfaceForm, true);
    }
}
