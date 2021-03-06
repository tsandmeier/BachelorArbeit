package tsandmeier.ba.evaluator;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.*;
import de.hterhors.semanticmr.crf.variables.DocumentToken;
import de.hterhors.semanticmr.eval.AbstractEvaluator;
import de.hterhors.semanticmr.eval.EEvaluationDetail;

import java.util.*;

import java.util.Arrays;
import java.util.Collection;

import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.EntityTypeAnnotation;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NerlaEvaluatorPartialOverlap extends AbstractEvaluator {

    private static Logger log = LogManager.getFormatterLogger("de.hterhors.semanticmr.projects.examples.corpus.nerl.NerlCorpusCreationExample");
    int counter = 0;
    HashMap<String, Integer> hashMap = new HashMap<String, Integer>();


    public NerlaEvaluatorPartialOverlap(EEvaluationDetail evaluationDetail) {
        super(evaluationDetail);
    }

    public Score prf1(Collection<? extends AbstractAnnotation> annotations, Collection<? extends AbstractAnnotation> otherAnnotations) {
        return prf1(evaluationDetail, annotations, otherAnnotations);
    }

    public Score prf1(EEvaluationDetail evaluationDetail, Collection<? extends AbstractAnnotation> annotations,
                      Collection<? extends AbstractAnnotation> otherAnnotations) {

        int tp = 0;
        int fp = 0;
        int fn = 0;

        //EntityType.get("Injury").getTransitiveClosureSubEntityTypes().contains()


        if(evaluationDetail.equals(EEvaluationDetail.LITERAL)) {

            //checks if the predicted annotation's text overlaps with any gold annotation's text

            //TODO: Auch prüfen ob Entity_Name übereinstimmt! Klappt halt meistens auch ohne

            outer:
            for (AbstractAnnotation oa : otherAnnotations) {
                for (AbstractAnnotation a : annotations) {

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
//
//            fp += otherAnnotations.size() - tp;

            fp = Math.max(otherAnnotations.size() - tp, 0);


//            fn += annotations.size() - tp;


//
//            outer:
//            for (AbstractAnnotation a : annotations) {
//
//                boolean hasTruePositive = false;
//
//                for (AbstractAnnotation oa : otherAnnotations) {
//
//                    DocumentLinkedAnnotation da = (DocumentLinkedAnnotation) a;
//                    List<DocumentToken> goldTokens = da.relatedTokens;
//
//                    DocumentLinkedAnnotation doa = (DocumentLinkedAnnotation) oa;
//                    List<DocumentToken> predictedTokens = doa.relatedTokens;
//
//                    for (DocumentToken goldToken : goldTokens) {
//                        if (predictedTokens.stream().anyMatch(p -> p.getText().toLowerCase().equals(goldToken.getText().toLowerCase()))) {
//                            hasTruePositive = true;
//                        }
//                    }
//                }
//                if(!hasTruePositive)
//                    fn++;
//            }


//            outer:
//            for (AbstractAnnotation oa : otherAnnotations) {
//                for (AbstractAnnotation a : annotations) {
//                    DocumentLinkedAnnotation da = (DocumentLinkedAnnotation) a;
//                    List<DocumentToken> predictedTokens = da.relatedTokens;
//
//                    DocumentLinkedAnnotation doa = (DocumentLinkedAnnotation) oa;
//                    List<DocumentToken> goldTokens = doa.relatedTokens;
//                    for (DocumentToken goldToken : goldTokens) {
//                        if (predictedTokens.stream().anyMatch(p -> p.getText().equals(goldToken.getText()))) {
//                            continue outer;
//                        }
//                    }
//
//                }
//                fp++;
//            }

        }

        //will check the predicted annotation is an actual part of the gold annotation, e.g. if it has an overlapping text and
        // the same position in the document

        else if(evaluationDetail.equals(EEvaluationDetail.DOCUMENT_LINKED)){
            outer:
            for (AbstractAnnotation a : annotations) {
                for (AbstractAnnotation oa : otherAnnotations) {

                    DocumentLinkedAnnotation da = (DocumentLinkedAnnotation) a;
                    List<DocumentToken> goldTokens = da.relatedTokens;

                    DocumentLinkedAnnotation doa = (DocumentLinkedAnnotation) oa;
                    List<DocumentToken> predictedTokens = doa.relatedTokens;

                    for (DocumentToken goldToken : goldTokens) {
                        if (predictedTokens.stream()
                                .anyMatch(p -> p.getText().equals(goldToken.getText()) && p.getDocTokenIndex() == goldToken.getDocTokenIndex())) {
                            tp++;

                            continue outer;
                        }
                    }
                }
                fn++;
            }

            fp += otherAnnotations.size() - tp;
        }

        // && p.getDocTokenIndex() == goldToken.getDocTokenIndex()

        else if(evaluationDetail.equals(EEvaluationDetail.ENTITY_TYPE)) {
            outer:
            for (AbstractAnnotation oa : otherAnnotations) {
                for (AbstractAnnotation a : annotations) {

                    if(a.getEntityType().equals(oa.getEntityType())){
                        tp++;
                        continue outer;
                    }
                }
            }

            fp += otherAnnotations.size() - tp;

            outer:
            for (AbstractAnnotation a : annotations) {

                boolean hasTruePositive = false;

                for (AbstractAnnotation oa : otherAnnotations) {

                    if(a.getEntityType().equals(oa.getEntityType())){
                        hasTruePositive = true;
                    }
                }
                if(!hasTruePositive)
                    fn++;
            }

        }


        return new Score(tp, fp, fn);

    }

    private void removePredictionsWithSameText(Collection<? extends AbstractAnnotation> otherAnnotations) {
        HashSet<String> seen = new HashSet<>();

        HashSet<AbstractAnnotation> toDeleteAnnotations = new HashSet<>();

        for(AbstractAnnotation oa: otherAnnotations){
//            log.info(oa.toPrettyString());
            DocumentLinkedAnnotation doa = (DocumentLinkedAnnotation) oa;

            StringBuilder sb = new StringBuilder();
            doa.relatedTokens.forEach(p -> sb.append(p.getText() + " "));

            if(!seen.add(oa.toPrettyString())){
                toDeleteAnnotations.add(oa);
            }

        }

        toDeleteAnnotations.forEach(p -> {
            otherAnnotations.remove(p);
            log.info("REMOVED: "+p.toPrettyString());
        });
    }

    public Score prf1(EntityTypeAnnotation gold, EntityTypeAnnotation predictions) {
        return prf1(Arrays.asList(gold), Arrays.asList(predictions));
    }

    @Override
    protected Score scoreMax(Collection<? extends AbstractAnnotation> annotations,
                             Collection<? extends AbstractAnnotation> otherAnnotations, Score.EScoreType scoretype) {
        Score score = prf1(annotations, otherAnnotations);

        if (scoretype == Score.EScoreType.MACRO)
            score.toMacro();
        return score;
    }

    @Override
    protected boolean evalEqualsMax(Collection<? extends AbstractAnnotation> annotations,
                                    Collection<? extends AbstractAnnotation> otherAnnotations) {

        int tp = 0;

        outer:
        for (AbstractAnnotation oa : otherAnnotations) {
            for (AbstractAnnotation a : annotations) {

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
            return false;
        }

        return Math.max(otherAnnotations.size() - tp, 0) == 0;

    }

    @Override
    public List<Integer> getBestAssignment(Collection<? extends AbstractAnnotation> collection, Collection<? extends AbstractAnnotation> collection1, Score.EScoreType eScoreType) {
        throw new NotImplementedException("Not impl.");
    }

}
