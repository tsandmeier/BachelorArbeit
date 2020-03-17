//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package tsandmeier.ba.crf;

import de.hterhors.semanticmr.crf.SemanticParsingCRF;
import de.hterhors.semanticmr.crf.exploration.IExplorationStrategy;
import de.hterhors.semanticmr.crf.helper.log.LogUtils;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.model.Factor;
import de.hterhors.semanticmr.crf.model.FactorGraph;
import de.hterhors.semanticmr.crf.model.Model;
import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.impl.AcceptStrategies;
import de.hterhors.semanticmr.crf.sampling.impl.SamplerCollection;
import de.hterhors.semanticmr.crf.sampling.stopcrit.ISamplingStoppingCriterion;
import de.hterhors.semanticmr.crf.sampling.stopcrit.ITrainingStoppingCriterion;
import de.hterhors.semanticmr.crf.sampling.stopcrit.impl.ConverganceCrit;
import de.hterhors.semanticmr.crf.structure.IEvaluatable.Score;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SemanticParsingCRFCustom {
    public static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.00000");
    private static Logger log = LogManager.getFormatterLogger(SemanticParsingCRF.class);
    public static final int MAX_SAMPLING = 100;
    private static final String COVERAGE_CONTEXT = "===========COVERAGE============\n";
    private static final String TRAIN_CONTEXT = "===========TRAIN============\n";
    private static final String TEST_CONTEXT = "===========TEST============\n";
    final List<IExplorationStrategy> explorerList;
    final Model model;
    IObjectiveFunction objectiveFunction;
    final AbstractSampler sampler;
    private final IStateInitializer initializer;
    private SemanticParsingCRFCustom.CRFStatistics trainingStatistics;
    private SemanticParsingCRFCustom.CRFStatistics testStatistics;

    public SemanticParsingCRFCustom(Model model, IExplorationStrategy explorer, AbstractSampler sampler, IStateInitializer initializer, IObjectiveFunction objectiveFunction) {
        this(model, Arrays.asList(explorer), sampler, initializer, objectiveFunction);
    }

    public SemanticParsingCRFCustom(Model model, List<IExplorationStrategy> explorer, AbstractSampler sampler, IStateInitializer initializer, IObjectiveFunction objectiveFunction) {
        this.model = model;
        this.explorerList = explorer;
        this.objectiveFunction = objectiveFunction;
        this.sampler = sampler;
        this.initializer = initializer;
    }

    public Map<Instance, State> train(AdvancedLearner learner, List<Instance> trainingInstances, int numberOfEpochs, ISamplingStoppingCriterion[] samplingStoppingCrits) {
        return this.train(learner, trainingInstances, numberOfEpochs, new ITrainingStoppingCriterion[0], samplingStoppingCrits);
    }

    public Map<Instance, State> trainAndTestEveryEpoch(AdvancedLearner learner, List<Instance> trainingInstances, int numberOfEpochs, ISamplingStoppingCriterion[] samplingStoppingCrits, List<Instance> testInstances, ISamplingStoppingCriterion stoppingCriterion, ISamplingStoppingCriterion noModelChangeCrit) {
        return this.trainAndTestEveryEpoch(learner, trainingInstances, numberOfEpochs, new ITrainingStoppingCriterion[0], samplingStoppingCrits, testInstances, stoppingCriterion, noModelChangeCrit);
    }

    public Map<Instance, State> train(AdvancedLearner learner, List<Instance> trainingInstances, int numberOfEpochs, ITrainingStoppingCriterion[] trainingStoppingCrits) {
        return this.train(learner, trainingInstances, numberOfEpochs, trainingStoppingCrits, new ISamplingStoppingCriterion[0]);
    }

    public Map<Instance, State> train(AdvancedLearner learner, List<Instance> trainingInstances, int numberOfEpochs, ITrainingStoppingCriterion[] trainingStoppingCrits, ISamplingStoppingCriterion[] samplingStoppingCrits) {
        this.trainingStatistics = new SemanticParsingCRFCustom.CRFStatistics("Train");
        log.info("Start training procedure...");
        this.trainingStatistics.startTrainingTime = System.currentTimeMillis();
        Map<Instance, State> finalStates = new LinkedHashMap();

        for(int epoch = 0; epoch < numberOfEpochs; ++epoch) {
            log.info("############");
            log.info("# Epoch: " + (epoch + 1) + " #");
            log.info("############");
            boolean sampleBasedOnObjectiveFunction = this.sampler.sampleBasedOnObjectiveScore(epoch);
            int instanceIndex = 0;
            Iterator var10 = trainingInstances.iterator();

            while(var10.hasNext()) {
                Instance instance = (Instance)var10.next();
                List<State> producedStateChain = new ArrayList();
                State currentState = this.initializer.getInitState(instance);
                this.objectiveFunction.score(currentState);
                finalStates.put(instance, currentState);
                producedStateChain.add(currentState);

                int samplingStep;
                for(samplingStep = 0; samplingStep < 100; ++samplingStep) {
                    Iterator var15 = this.explorerList.iterator();

                    while(var15.hasNext()) {
                        IExplorationStrategy explorer = (IExplorationStrategy)var15.next();
                        List<State> proposalStates = explorer.explore(currentState);
                        if (proposalStates.isEmpty()) {
                            proposalStates.add(currentState);
                        }

                        if (sampleBasedOnObjectiveFunction) {
                            this.objectiveFunction.score(proposalStates);
                        } else {
                            this.model.score(proposalStates);
                        }

                        State candidateState = this.sampler.sampleCandidate(proposalStates);
                        this.scoreSelectedStates(sampleBasedOnObjectiveFunction, currentState, candidateState);
                        boolean isAccepted = this.sampler.getAcceptanceStrategy(epoch).isAccepted(candidateState, currentState);
                        if (isAccepted) {
                            this.model.updateWeights(learner, currentState, candidateState);
                            currentState = candidateState;
                        }

                        producedStateChain.add(currentState);
                        finalStates.put(instance, currentState);
                    }

                    if (this.meetsSamplingStoppingCriterion(samplingStoppingCrits, producedStateChain)) {
                        break;
                    }
                }

                this.trainingStatistics.endTrainingTime = System.currentTimeMillis();
                Logger var10000 = log;
                StringBuilder var10001 = (new StringBuilder()).append("===========TRAIN============\n [").append(epoch + 1).append("/").append(numberOfEpochs).append("][");
                ++instanceIndex;
                LogUtils.logState(var10000, var10001.append(instanceIndex).append("/").append(trainingInstances.size()).append("][").append(samplingStep + 1).append("]").toString(), instance, currentState);
                log.info("Time: " + this.trainingStatistics.getTotalDuration());
            }

            if (this.meetsTrainingStoppingCriterion(trainingStoppingCrits, finalStates)) {
                break;
            }
        }

        this.trainingStatistics.endTrainingTime = System.currentTimeMillis();
        return finalStates;
    }

    public Map<Instance, State> trainAndTestEveryEpoch(AdvancedLearner learner, List<Instance> trainingInstances, int numberOfEpochs, ITrainingStoppingCriterion[] trainingStoppingCrits, ISamplingStoppingCriterion[] samplingStoppingCrits, List<Instance> testInstances, ISamplingStoppingCriterion maxStepCrit, ISamplingStoppingCriterion noModelChangeCrit) {
        this.trainingStatistics = new SemanticParsingCRFCustom.CRFStatistics("Train");
        log.info("Start training procedure...");
        this.trainingStatistics.startTrainingTime = System.currentTimeMillis();
        Map<Instance, State> finalStates = new LinkedHashMap();

        for(int epoch = 0; epoch < numberOfEpochs; ++epoch) {
            log.info("############");
            log.info("# Epoch: " + (epoch + 1) + " #");
            log.info("############");
            boolean sampleBasedOnObjectiveFunction = this.sampler.sampleBasedOnObjectiveScore(epoch);
            int instanceIndex = 0;
            Iterator var10 = trainingInstances.iterator();

            while(var10.hasNext()) {
                Instance instance = (Instance)var10.next();
                List<State> producedStateChain = new ArrayList();
                State currentState = this.initializer.getInitState(instance);
                this.objectiveFunction.score(currentState);
                finalStates.put(instance, currentState);
                producedStateChain.add(currentState);

                int samplingStep;
                for(samplingStep = 0; samplingStep < 100; ++samplingStep) {
                    Iterator var15 = this.explorerList.iterator();

                    while(var15.hasNext()) {
                        IExplorationStrategy explorer = (IExplorationStrategy)var15.next();
                        List<State> proposalStates = explorer.explore(currentState);
                        if (proposalStates.isEmpty()) {
                            proposalStates.add(currentState);
                        }

                        if (sampleBasedOnObjectiveFunction) {
                            this.objectiveFunction.score(proposalStates);
                        } else {
                            this.model.score(proposalStates);
                        }

                        State candidateState = this.sampler.sampleCandidate(proposalStates);
                        this.scoreSelectedStates(sampleBasedOnObjectiveFunction, currentState, candidateState);
                        boolean isAccepted = this.sampler.getAcceptanceStrategy(epoch).isAccepted(candidateState, currentState);
                        if (isAccepted) {
                            this.model.updateWeights(learner, currentState, candidateState);
                            currentState = candidateState;
                        }

                        producedStateChain.add(currentState);
                        finalStates.put(instance, currentState);
                    }

                    if (this.meetsSamplingStoppingCriterion(samplingStoppingCrits, producedStateChain)) {
                        break;
                    }
                }

                this.trainingStatistics.endTrainingTime = System.currentTimeMillis();
                Logger var10000 = log;
                StringBuilder var10001 = (new StringBuilder()).append("===========TRAIN============\n [").append(epoch + 1).append("/").append(numberOfEpochs).append("][");
                ++instanceIndex;
                //LogUtils.logState(var10000, var10001.append(instanceIndex).append("/").append(trainingInstances.size()).append("][").append(samplingStep + 1).append("]").toString(), instance, currentState);
//                log.info("Time: " + this.trainingStatistics.getTotalDuration());
            }

            if (this.meetsTrainingStoppingCriterion(trainingStoppingCrits, finalStates)) {
                break;
            }
            Map<Instance, State> results = predict(testInstances, maxStepCrit,
                    noModelChangeCrit);

            evaluate(log, results);
        }

        this.trainingStatistics.endTrainingTime = System.currentTimeMillis();
        return finalStates;
    }

    private boolean meetsSamplingStoppingCriterion(ISamplingStoppingCriterion[] stoppingCriterion, List<State> producedStateChain) {
        ISamplingStoppingCriterion[] var3 = stoppingCriterion;
        int var4 = stoppingCriterion.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            ISamplingStoppingCriterion sc = var3[var5];
            if (sc.meetsCondition(producedStateChain)) {
                return true;
            }
        }

        return false;
    }

    private boolean meetsTrainingStoppingCriterion(ITrainingStoppingCriterion[] stoppingCriterion, Map<Instance, State> producedStateChain) {
        ITrainingStoppingCriterion[] var3 = stoppingCriterion;
        int var4 = stoppingCriterion.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            ITrainingStoppingCriterion sc = var3[var5];
            if (sc.meetsCondition(producedStateChain.values())) {
                return true;
            }
        }

        return false;
    }

    private void scoreSelectedStates(boolean sampleBasedOnObjectiveFunction, State currentState, State candidateState) {
        if (sampleBasedOnObjectiveFunction) {
            this.model.score(candidateState);
        } else {
            this.objectiveFunction.score(candidateState);
            this.objectiveFunction.score(currentState);
        }

    }

    public SemanticParsingCRFCustom.CRFStatistics getTrainingStatistics() {
        return this.trainingStatistics;
    }

    public SemanticParsingCRFCustom.CRFStatistics getTestStatistics() {
        return this.testStatistics;
    }

    public Map<Instance, State> predict(List<Instance> instancesToPredict, ISamplingStoppingCriterion... stoppingCriterion) {
        return (Map)this.predictP(this.model, instancesToPredict, 1, stoppingCriterion).entrySet().stream().collect(Collectors.toMap((m) -> {
            return (Instance)m.getKey();
        }, (m) -> {
            return (State)((List)m.getValue()).get(0);
        }));
    }

    public Map<Instance, State> predictHighRecall(List<Instance> instancesToPredict, int n, ISamplingStoppingCriterion... stoppingCriterion) {
        return (Map)this.predictP(this.model, instancesToPredict, n, stoppingCriterion).entrySet().stream().collect(Collectors.toMap((m) -> {
            return (Instance)m.getKey();
        }, (m) -> {
            return this.merge(m, n);
        }));
    }

    public Map<Instance, State> predictHighRecall(Model model, List<Instance> instancesToPredict, int n, ISamplingStoppingCriterion... stoppingCriterion) {
        return (Map)this.predictP(model, instancesToPredict, n, stoppingCriterion).entrySet().stream().collect(Collectors.toMap((m) -> {
            return (Instance)m.getKey();
        }, (m) -> {
            return this.merge(m, n);
        }));
    }

    public Map<Instance, State> predict(Model model, List<Instance> instancesToPredict, ISamplingStoppingCriterion... stoppingCriterion) {
        return (Map)this.predictP(model, instancesToPredict, 1, stoppingCriterion).entrySet().stream().collect(Collectors.toMap((m) -> {
            return (Instance)m.getKey();
        }, (m) -> {
            return (State)((List)m.getValue()).get(0);
        }));
    }

    private Map<Instance, List<State>> predictP(Model model, List<Instance> instancesToPredict, int n, ISamplingStoppingCriterion... stoppingCriterion) {
        this.testStatistics = new SemanticParsingCRFCustom.CRFStatistics("Test");
        this.testStatistics.startTrainingTime = System.currentTimeMillis();
        Map<Instance, List<State>> finalStates = new LinkedHashMap();
        int instanceIndex = 0;
        Iterator var7 = instancesToPredict.iterator();

        while(var7.hasNext()) {
            Instance instance = (Instance)var7.next();
            List<State> producedStateChain = new ArrayList();
            List<State> currentStates = new ArrayList();
            State currentState = this.initializer.getInitState(instance);
            finalStates.put(instance, Arrays.asList(currentState));
            this.objectiveFunction.score(currentState);
            producedStateChain.add(currentState);

            int samplingStep;
            for(samplingStep = 0; samplingStep < 100; ++samplingStep) {
                Iterator var13 = this.explorerList.iterator();

                while(var13.hasNext()) {
                    IExplorationStrategy explorer = (IExplorationStrategy)var13.next();
                    List<State> proposalStates = explorer.explore(currentState);
                    if (proposalStates.isEmpty()) {
                        proposalStates.add(currentState);
                    }

                    model.score(proposalStates);
                    Collections.sort(proposalStates, (s1, s2) -> {
                        return -Double.compare(s1.getModelScore(), s2.getModelScore());
                    });
                    State candidateState = (State)proposalStates.get(0);
                    boolean accepted = AcceptStrategies.strictModelAccept().isAccepted(candidateState, currentState);
                    if (accepted) {
                        currentState = candidateState;
                        this.objectiveFunction.score(candidateState);
                        currentStates = new ArrayList(proposalStates.subList(0, Math.min(proposalStates.size(), n)));
                        this.objectiveFunction.score(currentStates);
                    }

                    producedStateChain.add(currentState);
                    if (n == 1) {
                        finalStates.put(instance, Arrays.asList(currentState));
                    } else {
                        finalStates.put(instance, currentStates);
                    }
                }

                if (this.meetsSamplingStoppingCriterion(stoppingCriterion, producedStateChain)) {
                    break;
                }
            }

            this.testStatistics.endTrainingTime = System.currentTimeMillis();
            Logger var10000 = log;
            StringBuilder var10001 = (new StringBuilder()).append("===========TEST============\n[");
            ++instanceIndex;
            //LogUtils.logState(var10000, var10001.append(instanceIndex).append("/").append(instancesToPredict.size()).append("] [").append(samplingStep).append("]").toString(), instance, currentState);
            //log.info("Time: " + this.testStatistics.getTotalDuration());
        }

        this.testStatistics.endTrainingTime = System.currentTimeMillis();
        return finalStates;
    }

    private State merge(Entry<Instance, List<State>> m, int n) {
        List<AbstractAnnotation> mergedAnnotations = new ArrayList();

        label22:
        for(int i = 0; i < ((List)m.getValue()).size(); ++i) {
            Iterator var5 = ((State)((List)m.getValue()).get(i)).getCurrentPredictions().getAnnotations().iterator();

            while(var5.hasNext()) {
                AbstractAnnotation abstractAnnotation = (AbstractAnnotation)var5.next();
                if (mergedAnnotations.size() == n) {
                    break label22;
                }

                mergedAnnotations.add(abstractAnnotation);
            }
        }

        State s = new State((Instance)m.getKey(), new Annotations(mergedAnnotations));
        this.objectiveFunction.score(s);
        return s;
    }

    private void compare(State currentState, State candidateState) {
        Map<String, Double> differences = this.getDifferences(this.collectFeatures(currentState), this.collectFeatures(candidateState));
        if (!differences.isEmpty()) {
            List<Entry<String, Double>> sortedWeightsPrevState = new ArrayList(this.collectFeatures(currentState).entrySet());
            List<Entry<String, Double>> sortedWeightsCandState = new ArrayList(this.collectFeatures(candidateState).entrySet());
            Collections.sort(sortedWeightsPrevState, (o1, o2) -> {
                return -Double.compare((Double)o1.getValue(), (Double)o2.getValue());
            });
            Collections.sort(sortedWeightsCandState, (o1, o2) -> {
                return -Double.compare((Double)o1.getValue(), (Double)o2.getValue());
            });
            log.info(currentState.getInstance().getName());
            log.info("_____________GoldAnnotations:_____________");
            log.info(currentState.getGoldAnnotations());
            log.info("_____________PrevState:_____________");
            Stream var10000 = sortedWeightsPrevState.stream().filter((k) -> {
                return differences.containsKey(k.getKey());
            });
            Logger var10001 = log;
            var10000.forEach(var10001::info);
            log.info("ModelScore: " + currentState.getModelScore() + ": " + currentState.getCurrentPredictions());
            log.info("_____________CandState:_____________");
            var10000 = sortedWeightsCandState.stream().filter((k) -> {
                return differences.containsKey(k.getKey());
            });
            var10001 = log;
            var10000.forEach(var10001::info);
            log.info("ModelScore: " + candidateState.getModelScore() + ": " + candidateState.getCurrentPredictions());
            log.info("------------------");
        }
    }

    public Map<String, Double> getDifferences(Map<String, Double> currentFeatures, Map<String, Double> candidateFeatures) {
        Map<String, Double> differences = new HashMap();
        Set<String> keys = new HashSet();
        keys.addAll(candidateFeatures.keySet());
        keys.addAll(currentFeatures.keySet());
        Iterator var5 = keys.iterator();

        while(true) {
            while(var5.hasNext()) {
                String key = (String)var5.next();
                if (candidateFeatures.containsKey(key) && currentFeatures.containsKey(key)) {
                    double diff = 0.0D;
                    if ((diff = Math.abs((Double)currentFeatures.get(key) - (Double)candidateFeatures.get(key))) != 0.0D) {
                        differences.put(key, diff);
                    }
                } else if (currentFeatures.containsKey(key)) {
                    differences.put(key, currentFeatures.get(key));
                } else {
                    differences.put(key, candidateFeatures.get(key));
                }
            }

            return differences;
        }
    }

    public Map<String, Double> collectFeatures(State currentState) {
        Map<String, Double> features = new HashMap();
        Iterator var3 = currentState.getFactorGraphs().iterator();

        while(var3.hasNext()) {
            FactorGraph fg = (FactorGraph)var3.next();
            Iterator var5 = fg.getFactors().iterator();

            while(var5.hasNext()) {
                Factor f = (Factor)var5.next();
                Iterator var7 = f.getFeatureVector().getFeatures().entrySet().iterator();

                while(var7.hasNext()) {
                    Entry<Integer, Double> feature = (Entry)var7.next();
                    if (f.getFactorScope().getTemplate().getWeights().getFeatures().containsKey(feature.getKey())) {
                        features.put(f.getFactorScope().getTemplate().getClass().getSimpleName() + ":" + Model.getFeatureForIndex((Integer)feature.getKey()), (Double)feature.getValue() * (Double)f.getFactorScope().getTemplate().getWeights().getFeatures().get(feature.getKey()));
                    }
                }
            }
        }

        return features;
    }

    public Score computeCoverage(boolean printDetailedLog, IObjectiveFunction predictionOF, List<Instance> instances) {
        log.info("Compute coverage...");
        ISamplingStoppingCriterion[] noObjectiveChangeCrit = new ISamplingStoppingCriterion[]{new ConverganceCrit(this.explorerList.size(), (s) -> {
            return s.getObjectiveScore();
        })};
        Map<Instance, State> finalStates = new LinkedHashMap();
        int instanceIndex = 0;
        Iterator var7 = instances.iterator();

        while(var7.hasNext()) {
            Instance instance = (Instance)var7.next();
            List<State> producedStateChain = new ArrayList();
            State currentState = this.initializer.getInitState(instance);
            predictionOF.score(currentState);
            finalStates.put(instance, currentState);
            producedStateChain.add(currentState);

            int samplingStep;
            for(samplingStep = 0; samplingStep < 100; ++samplingStep) {
                Iterator var12 = this.explorerList.iterator();

                while(var12.hasNext()) {
                    IExplorationStrategy explorer = (IExplorationStrategy)var12.next();
                    List<State> proposalStates = explorer.explore(currentState);
                    if (proposalStates.isEmpty()) {
                        proposalStates.add(currentState);
                    }

                    predictionOF.score(proposalStates);
                    State candidateState = SamplerCollection.greedyObjectiveStrategy().sampleCandidate(proposalStates);
                    boolean isAccepted = SamplerCollection.greedyObjectiveStrategy().getAcceptanceStrategy(0).isAccepted(candidateState, currentState);
                    if (isAccepted) {
                        currentState = candidateState;
                    }

                    producedStateChain.add(currentState);
                    finalStates.put(instance, currentState);
                }

                if (this.meetsSamplingStoppingCriterion(noObjectiveChangeCrit, producedStateChain)) {
                    break;
                }
            }

            if (printDetailedLog) {
                Logger var10000 = log;
                StringBuilder var10001 = (new StringBuilder()).append("===========COVERAGE============\n [1/1][");
                ++instanceIndex;
                LogUtils.logState(var10000, var10001.append(instanceIndex).append("/").append(instances.size()).append("][").append(samplingStep + 1).append("]").toString(), instance, currentState);
            }
        }

        Score meanTrainOFScore = new Score();

        Entry finalState;
        for(Iterator var18 = finalStates.entrySet().iterator(); var18.hasNext(); meanTrainOFScore.add(((State)finalState.getValue()).getMicroScore())) {
            finalState = (Entry)var18.next();
            predictionOF.score((State)finalState.getValue());
            if (printDetailedLog) {
                log.info(((Instance)finalState.getKey()).getName().substring(0, Math.min(((Instance)finalState.getKey()).getName().length(), 10)) + "... \t" + SCORE_FORMAT.format(((State)finalState.getValue()).getObjectiveScore()));
            }
        }

        return meanTrainOFScore;
    }


    private static class CRFStatistics {
        private final String context;
        private long startTrainingTime;
        private long endTrainingTime;

        public CRFStatistics(String context) {
            this.context = context;
        }

        private long getTotalDuration() {
            return this.endTrainingTime - this.startTrainingTime;
        }

        public String toString() {
            return "CRFStatistics [context=" + this.context + ", getTotalDuration()=" + this.getTotalDuration() + "]";
        }
    }

    public Score evaluate(Logger log, Map<Instance, State> testResults) {
        Score mean = new Score();
        for (Entry<Instance, State> res : testResults.entrySet()) {

//			if (res.getKey().getDocument().documentID.equals("N008 Jin 2016 26852702")) {
//				System.out.println(c.scoreMultiValues(res.getKey().getGoldAnnotations().getAnnotations(),
//						res.getValue().getCurrentPredictions().getAnnotations()));
//			}

            mean.add(res.getValue().getMicroScore());
//            LogUtils.logState(log, "======Final Evaluation======", res.getKey(), res.getValue());
        }
        log.info("Mean Score: " + mean);
//		System.out.println("Mean Score: " + mean);
        return mean;
    }

    public void changeObjectiveFunction (IObjectiveFunction objectiveFunction){
        this.objectiveFunction = objectiveFunction;
    }

}
