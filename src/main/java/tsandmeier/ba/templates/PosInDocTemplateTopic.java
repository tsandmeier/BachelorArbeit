package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.State;
import tsandmeier.ba.tools.AutomatedSectionification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class PosInDocTemplateTopic extends AbstractFeatureTemplate<PosInDocTemplateTopic.PosInDocScope> {

    public PosInDocTemplateTopic() {
        super();
    }

    public PosInDocTemplateTopic(boolean cache) {
        super(cache);
    }


    static class PosInDocScope
            extends AbstractFactorScope {

        EntityType entityType;
        AutomatedSectionification.ESection section;

        public PosInDocScope(
                AbstractFeatureTemplate<PosInDocScope> template, EntityType entityType, AutomatedSectionification.ESection section) {
            super(template);
            this.entityType = entityType;
            this.section = section;
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
            return Objects.equals(entityType, that.entityType) &&
                    section == that.section;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), entityType, section);
        }
    }

    @Override
    public List<PosInDocScope> generateFactorScopes(State state) {
        List<PosInDocScope> factors = new ArrayList<>();

        Document document = state.getInstance().getDocument();

        AutomatedSectionification sectionification = new AutomatedSectionification(state.getInstance());

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {

            AutomatedSectionification.ESection section = sectionification.getSection(annotation);

            factors.add(new PosInDocScope(this, annotation.getEntityType(), section));
        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<PosInDocScope> factor) {

        factor.getFeatureVector().set("In Section: " + factor.getFactorScope().section.toString() + "<" +
                factor.getFactorScope().entityType.name + ">", true);
    }
}
