package tsandmeier.ba.evaluator;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score.EScoreType;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.eval.AbstractEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import org.apache.commons.lang3.NotImplementedException;

public class NerlaEvaluatorPartialOverlapTwo extends AbstractEvaluator {

    public NerlaEvaluatorPartialOverlapTwo(EEvaluationDetail evaluationDetail) {
        super(evaluationDetail);
    }

    public Score prf1(Collection<? extends AbstractAnnotation> annotations,
                      Collection<? extends AbstractAnnotation> otherAnnotations) {
        return prf1(evaluationDetail, annotations, otherAnnotations);
    }

    public Score prf1(EEvaluationDetail evaluationDetail, Collection<? extends AbstractAnnotation> annotations,
                      Collection<? extends AbstractAnnotation> otherAnnotations) {

        int tp = 0;
        int fp = 0;
        int fn = 0;

        outer: for (AbstractAnnotation a : annotations) {
            a.asInstanceOfDocumentLinkedAnnotation().relatedTokens.get(0).getSentenceIndex();

            Collection<AbstractAnnotation> otherAnnotationsInSentence = otherAnnotations.stream().filter(oa -> oa.asInstanceOfDocumentLinkedAnnotation().relatedTokens.get(0).getSentenceIndex() == a.asInstanceOfDocumentLinkedAnnotation().relatedTokens.get(0).getSentenceIndex()).collect(Collectors.toList());

            for (AbstractAnnotation oa : otherAnnotationsInSentence) {
                DocumentLinkedAnnotation da = (DocumentLinkedAnnotation) a;
                List<DocumentToken> goldTokens = da.relatedTokens;

                DocumentLinkedAnnotation doa = (DocumentLinkedAnnotation) oa;
                List<DocumentToken> predictedTokens = doa.relatedTokens;

                for (DocumentToken goldToken : goldTokens) {
                    if (predictedTokens.stream().anyMatch(p -> p.getText().toLowerCase().equals(goldToken.getText().toLowerCase()))) {
                        tp++;
                        continue outer;
                    }
                }
            }

            fn++;
        }

        fp = Math.max(otherAnnotations.size() - tp, 0);

        return new Score(tp, fp, fn);

    }

    public Score prf1(EntityTypeAnnotation gold, EntityTypeAnnotation predictions) {
        return prf1(Arrays.asList(gold), Arrays.asList(predictions));
    }

    @Override
    protected Score scoreMax(Collection<? extends AbstractAnnotation> annotations,
                             Collection<? extends AbstractAnnotation> otherAnnotations, EScoreType scoretype) {
        Score score = prf1(annotations, otherAnnotations);

        if (scoretype == EScoreType.MACRO)
            score.toMacro();
        return score;
    }

    @Override
    protected boolean evalEqualsMax(Collection<? extends AbstractAnnotation> annotations,
                                    Collection<? extends AbstractAnnotation> otherAnnotations) {

        int tp = 0;

        outer: for (AbstractAnnotation a : annotations) {
            for (AbstractAnnotation oa : otherAnnotations) {
                if (oa.evaluateEquals(this, a)) {
                    tp++;
                    continue outer;
                }
            }

            return false;
        }

        return Math.max(otherAnnotations.size() - tp, 0) == 0;

    }

    @Override
    public List<Integer> getBestAssignment(Collection<? extends AbstractAnnotation> collection, Collection<? extends AbstractAnnotation> collection1, EScoreType eScoreType) {
        throw new NotImplementedException("Not impl.");    }

}
