package tsandmeier.ba.templates;

import de.hterhors.semanticmr.crf.model.AbstractFactorScope;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.crf.variables.State;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author hterhors
 * @date Nov 15, 2017
 */
public class WBNULLTemplate extends AbstractFeatureTemplate<WBNULLTemplate.WBNULLScope> {

    static class WBNULLScope
            extends AbstractFactorScope {

        EntityType typeOne;
        EntityType typeTwo;

        DocumentToken tokenOne;
        DocumentToken tokenTwo;


        public WBNULLScope(AbstractFeatureTemplate template, EntityType typeOne, EntityType typeTwo, DocumentToken tokenOne, DocumentToken tokenTwo) {
            super(template);
            this.typeOne = typeOne;
            this.typeTwo = typeTwo;
            this.tokenOne = tokenOne;
            this.tokenTwo = tokenTwo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            WBNULLScope that = (WBNULLScope) o;
            return Objects.equals(typeOne, that.typeOne) &&
                    Objects.equals(typeTwo, that.typeTwo) &&
                    Objects.equals(tokenOne, that.tokenOne) &&
                    Objects.equals(tokenTwo, that.tokenTwo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), typeOne, typeTwo, tokenOne, tokenTwo);
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
    public List<WBNULLScope> generateFactorScopes(State state) {
//		writeUsingFileWriter(state.getInstance().getDocument().documentContent);
//		riteUsingFileWriter("Hallo");

        List<WBNULLScope> factors = new ArrayList<>();

        for (DocumentLinkedAnnotation annotation : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state)) {
            for (DocumentLinkedAnnotation annotation2 : super.<DocumentLinkedAnnotation>getPredictedAnnotations(state))
                if (!annotation.equals(annotation2)) {
                    factors.add(new WBNULLScope(this, annotation.entityType, annotation2.entityType, annotation.relatedTokens.get(annotation.relatedTokens.size() - 1),
                            annotation2.relatedTokens.get(0)));
                }

        }
        return factors;
    }

    @Override
    public void generateFeatureVector(Factor<WBNULLScope> factor) {

        factor.getFeatureVector().set("SEPARATED <" + factor.getFactorScope().typeOne.entityName + ", " +
                factor.getFactorScope().typeTwo.entityName + "> " +
                isWordBetween(factor.getFactorScope().tokenOne, factor.getFactorScope().tokenTwo), true);

    }

    private boolean isWordBetween(DocumentToken tokenOne, DocumentToken tokenTwo) {
        return (tokenTwo.getDocTokenIndex() - tokenOne.getDocTokenIndex() == 1);
    }

    private static void writeUsingFileWriter(String data) {
        File file = new File("/home/tobias/Projekte/SemanticMachineReading/bla/inhalt.txt");
        FileWriter fr = null;
        try {
            fr = new FileWriter(file);
            fr.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //close resources
            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}