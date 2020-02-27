package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.Document;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;
import tsandmeier.ba.tools.AutomatedSectionification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * looks for one to a set number of words after a mention
 */

public class SectionTemplate extends AbstractFeatureTemplate<SectionTemplate.AMFLScope> {

    public SectionTemplate() {
        super();
    }

    public SectionTemplate(boolean cache) {
        super(cache);
    }

    private static final int NUMBER_OF_WORDS = 5;

    static class AMFLScope
            extends AbstractFactorScope {

        String entityName;
        String section;

        public AMFLScope(
                AbstractFeatureTemplate<AMFLScope> template, String entityName, String section) {
            super(template);

            this.entityName = entityName;
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

    }

    @Override
    public List<AMFLScope> generateFactorScopes(State state) {
        List<AMFLScope> factors = new ArrayList<>();

        Document document = state.getInstance().getDocument();

        AutomatedSectionification sectionification = new AutomatedSectionification(state.getInstance());

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
            DocumentToken token = annotation.relatedTokens.get(annotation.relatedTokens.size() - 1);
//            factors.add(new AMFLScope(this, document, token, annotation.getEntityType()));
        }
        return factors;
    }




    @Override
    public void generateFeatureVector(Factor<AMFLScope> factor) {




    }
}
