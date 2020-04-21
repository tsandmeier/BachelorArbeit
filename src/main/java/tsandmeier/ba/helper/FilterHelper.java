package tsandmeier.ba.helper;

import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.SlotType;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tsandmeier.ba.evaluator.NerlaObjectiveFunctionPartialOverlap;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by tobias on 18.03.20.
 */
public class FilterHelper {

    Logger log = LogManager.getFormatterLogger("de.hterhors.semanticmr.projects.examples.corpus.nerl.NerlCorpusCreationExample");
    Map<Instance, State> entities;

    NerlaObjectiveFunctionPartialOverlap objectiveFunction = new NerlaObjectiveFunctionPartialOverlap(EEvaluationDetail.DOCUMENT_LINKED);


    public FilterHelper(Map<Instance, State> results){
        this.entities = results;
    }

    public IEvaluatable.Score filterForSlot(String slotname){

        IEvaluatable.Score score = new IEvaluatable.Score();

        for (Map.Entry<Instance, State> result : entities.entrySet()) {

            List<AbstractAnnotation> goldAnnosFiltered = result.getValue().getGoldAnnotations().getAnnotations().stream().filter(a -> SlotType.get(slotname).getSlotFillerEntityTypes().contains(a.getEntityType())).collect(Collectors.toList());

            List<AbstractAnnotation> predictedAnnos = result.getValue().getCurrentPredictions().getAnnotations().stream().filter(a -> SlotType.get(slotname).getSlotFillerEntityTypes().contains(a.getEntityType())).collect(Collectors.toList());

            score.add(objectiveFunction.getEvaluator().scoreMultiValues(goldAnnosFiltered,predictedAnnos, IEvaluatable.Score.EScoreType.MICRO));
        }
        return score;
    }

    public IEvaluatable.Score filterTreatment(){
        IEvaluatable.Score score = new IEvaluatable.Score();

        int counter = 0;

        for (Map.Entry<Instance, State> result : entities.entrySet()) {

            counter++;
            log.info("**************************State Nummer "+counter+"***************************************");


            List<AbstractAnnotation> goldAnnos = result.getValue().getGoldAnnotations().getAnnotations().stream().filter(a -> a.getEntityType().getTransitiveClosureSuperEntityTypes().contains(EntityType.get("Treatment")) ||
                    a.getEntityType().getTransitiveClosureSuperEntityTypes().contains(EntityType.get("Compound"))).collect(Collectors.toList());

            log.info("GOLD["+goldAnnos.size()+"]:");
            for(AbstractAnnotation goldAnno : goldAnnos){
                log.info(goldAnno.toPrettyString());
            }

            log.info(System.lineSeparator());

            List<AbstractAnnotation> predictedAnnos = result.getValue().getCurrentPredictions().getAnnotations().stream().filter(a -> a.getEntityType().getTransitiveClosureSuperEntityTypes().contains(EntityType.get("Treatment")) ||
                    a.getEntityType().getTransitiveClosureSuperEntityTypes().contains(EntityType.get("Compound"))).collect(Collectors.toList());

            log.info("PREDICTED["+predictedAnnos.size()+"]:");
            for(AbstractAnnotation predictedAnno : predictedAnnos){
                log.info(predictedAnno.toPrettyString());
            }

            score.add(new NerlaObjectiveFunctionPartialOverlap(EEvaluationDetail.DOCUMENT_LINKED).getEvaluator().scoreMultiValues(goldAnnos, predictedAnnos, IEvaluatable.Score.EScoreType.MICRO));

        }
        log.info(System.lineSeparator());
        log.info("SCORE: " + score);
        return score;
    }

    public IEvaluatable.Score filterInjury() {
        IEvaluatable.Score score = new IEvaluatable.Score();

        int counter = 0;

        for (Map.Entry<Instance, State> result : entities.entrySet()) {
            counter++;
            log.info("**************************State Nummer "+counter+"***************************************");


            List<AbstractAnnotation> goldAnnos = result.getValue().getGoldAnnotations().getAnnotations().stream().filter(a -> a.getEntityType().getTransitiveClosureSuperEntityTypes().contains(EntityType.get("Injury"))).collect(Collectors.toList());

            log.info("GOLD["+goldAnnos.size()+"]:");
            for(AbstractAnnotation goldAnno : goldAnnos){
                log.info(goldAnno.toPrettyString());
            }

            log.info(System.lineSeparator());

            List<AbstractAnnotation> predictedAnnos = result.getValue().getCurrentPredictions().getAnnotations().stream().filter(a -> a.getEntityType().getTransitiveClosureSuperEntityTypes().contains(EntityType.get("Injury"))).collect(Collectors.toList());


            log.info("PREDICTED["+predictedAnnos.size()+"]:");

            for(AbstractAnnotation predictedAnno : predictedAnnos){
                log.info(predictedAnno.toPrettyString());
            }


            score.add(new NerlaObjectiveFunctionPartialOverlap(EEvaluationDetail.DOCUMENT_LINKED).getEvaluator().scoreMultiValues(goldAnnos, predictedAnnos, IEvaluatable.Score.EScoreType.MICRO));

        }

        log.info("SCORE: " + score);
        return score;
    }

    public IEvaluatable.Score filterOrganismModel() {
        IEvaluatable.Score score = new IEvaluatable.Score();

        int counter = 0;

        for (Map.Entry<Instance, State> result : entities.entrySet()) {
            counter++;
            log.info("**************************State Nummer "+counter+"***************************************");


            List<AbstractAnnotation> goldAnnos = result.getValue().getGoldAnnotations().getAnnotations().stream().filter(a -> a.getEntityType().getTransitiveClosureSuperEntityTypes().contains(EntityType.get("OrganismSpecies"))).collect(Collectors.toList());

            log.info("GOLD["+goldAnnos.size()+"]:");
            for(AbstractAnnotation goldAnno : goldAnnos){
                log.info(goldAnno.toPrettyString());
            }

            log.info(System.lineSeparator());

            List<AbstractAnnotation> predictedAnnos = result.getValue().getCurrentPredictions().getAnnotations().stream().filter(a -> a.getEntityType().getTransitiveClosureSuperEntityTypes().contains(EntityType.get("OrganismSpecies"))).collect(Collectors.toList());


            log.info("PREDICTED["+predictedAnnos.size()+"]:");

            for(AbstractAnnotation predictedAnno : predictedAnnos){
                log.info(predictedAnno.toPrettyString());
            }


            score.add(new NerlaObjectiveFunctionPartialOverlap(EEvaluationDetail.DOCUMENT_LINKED).getEvaluator().scoreMultiValues(goldAnnos, predictedAnnos, IEvaluatable.Score.EScoreType.MICRO));

        }

        log.info("SCORE: " + score);
        return score;
    }

    public IEvaluatable.Score filterWithoutAgeAndWeight() {
        IEvaluatable.Score score = new IEvaluatable.Score();

        int counter = 0;

        for (Map.Entry<Instance, State> result : entities.entrySet()) {
            counter++;
            log.info("**************************State Nummer "+counter+"***************************************");


            List<AbstractAnnotation> goldAnnos = result.getValue().getGoldAnnotations().getAnnotations().stream().filter(a -> !a.getEntityType().equals(EntityType.get("Age")) && !a.getEntityType().equals(EntityType.get("Weight"))).collect(Collectors.toList());

            log.info("GOLD["+goldAnnos.size()+"]:");
            for(AbstractAnnotation goldAnno : goldAnnos){
                log.info(goldAnno.toPrettyString());
            }

            log.info(System.lineSeparator());

            List<AbstractAnnotation> predictedAnnos = result.getValue().getCurrentPredictions().getAnnotations().stream().filter(a -> !a.getEntityType().equals(EntityType.get("Age")) && !a.getEntityType().equals(EntityType.get("Weight"))).collect(Collectors.toList());


            log.info("PREDICTED["+predictedAnnos.size()+"]:");

            for(AbstractAnnotation predictedAnno : predictedAnnos){
                log.info(predictedAnno.toPrettyString());
            }

            IEvaluatable.Score stateScore = new NerlaObjectiveFunctionPartialOverlap(EEvaluationDetail.DOCUMENT_LINKED).getEvaluator().scoreMultiValues(goldAnnos, predictedAnnos, IEvaluatable.Score.EScoreType.MICRO);

            log.info(stateScore.toString());
            log.info(System.lineSeparator());

            score.add(stateScore);

        }

        log.info("SCORE: " + score);
        return score;
    }

}
