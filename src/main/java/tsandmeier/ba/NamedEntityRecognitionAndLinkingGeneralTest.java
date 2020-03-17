package tsandmeier.ba;

import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.learner.optimizer.SGD;
import de.hterhors.semanticmr.crf.learner.regularizer.L2;
import de.hterhors.semanticmr.crf.model.Model;
import de.hterhors.semanticmr.crf.of.IObjectiveFunction;
import de.hterhors.semanticmr.crf.of.NerlaObjectiveFunction;
import de.hterhors.semanticmr.crf.sampling.AbstractSampler;
import de.hterhors.semanticmr.crf.sampling.impl.EpochSwitchSampler;
import de.hterhors.semanticmr.crf.sampling.stopcrit.ISamplingStoppingCriterion;
import de.hterhors.semanticmr.crf.sampling.stopcrit.impl.ConverganceCrit;
import de.hterhors.semanticmr.crf.sampling.stopcrit.impl.MaxChainLengthCrit;
import de.hterhors.semanticmr.crf.structure.EntityType;
import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.init.reader.csv.CSVDataStructureReader;
import de.hterhors.semanticmr.init.specifications.SystemScope;
import de.hterhors.semanticmr.json.nerla.JsonNerlaIO;
import de.hterhors.semanticmr.json.nerla.wrapper.JsonEntityAnnotationWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tsandmeier.ba.candprov.CreateDictionaryClass;
import tsandmeier.ba.crf.SemanticParsingCRFCustomTwo;
import tsandmeier.ba.evaluator.NerlaObjectiveFunctionPartialOverlap;
import tsandmeier.ba.explorer.EntityRecLinkExplorerCustom;
import tsandmeier.ba.groupnameTemplates.GroupNamesInSameSentenceTemplate_FAST;
import tsandmeier.ba.groupnameTemplates.WBFGroupNamesTemplate_FAST;
import tsandmeier.ba.groupnameTemplates.WBLGroupNamesTemplate_FAST;
import tsandmeier.ba.groupnameTemplates.WordsInBetweenGroupNamesTemplate_FAST;
import tsandmeier.ba.templates.*;

import javax.sound.midi.SysexMessage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Example of how to perform named entity recognition and linking.
 */
public class NamedEntityRecognitionAndLinkingGeneralTest extends AbstractSemReadProject {
    private static Logger log = LogManager.getFormatterLogger("de.hterhors.semanticmr.projects.examples.corpus.nerl.NerlCorpusCreationExample");
    private final boolean overrideModel = false;
    SemanticParsingCRFCustomTwo crf;
    private IEvaluatable.Score mean;
    private int mode;
    List<AbstractFeatureTemplate<?>> featureTemplates;

    private final EEvaluationDetail evaluationDetail = EEvaluationDetail.LITERAL;

    private double alpha;

    private static String TYPE_OF_TOPIC = "group_name";

    private static String SPECIFICATION_DIRECTORY = "ner/" + TYPE_OF_TOPIC + "/data_structure/";
    private static String INSTANCE_DIRECTORY = "ner/" + TYPE_OF_TOPIC + "/instances/";


    private static String ENTITIES = "ner/group_name/data_structure/entities.csv";
    private static String HIERARCHIES = "ner/group_name/data_structure/hierarchies.csv";
    private static String SLOTS = "ner/group_name/data_structure/slots.csv";
    private static String STRUCTURES = "ner/group_name/data_structure/structures.csv";

    int recallFactor;

    private String typeOfTopic;
    File instanceDirectory;

    String modelName;


    /**
     * Start the named entity recognition and linking procedure.
     *
     * @param args
     */
    public static void main(String[] args) throws IOException {

        int mode;
        double alpha;

        if (args.length == 4) {
            mode = Integer.valueOf(args[0]);
            alpha = Double.valueOf(args[1]);
            new NamedEntityRecognitionAndLinkingGeneralTest(args[2], args[3], 1).startProcedure(mode, alpha);
        } else if (args.length == 3) {
            mode = Integer.valueOf(args[0]);
            alpha = Double.valueOf(args[1]);
            new NamedEntityRecognitionAndLinkingGeneralTest(args[2], "NERLA1234" + new Random().nextInt(10000), 1).startProcedure(mode, alpha);
        } else if (args.length == 5) {
            mode = Integer.valueOf(args[0]);
            alpha = Double.valueOf(args[1]);
            new NamedEntityRecognitionAndLinkingGeneralTest(args[2], args[3], Integer.valueOf(args[4])).startProcedure(mode, alpha);
        } else {
            log.info("Falsche Anzahl an Parametern!");
        }
    }

    /**
     * A dictionary file that is used for the in-memory dictionary based candidate
     * retrieval component. It is basically a list of terms and synonyms for
     * specific entities.
     * <p>
     * In a real world scenario dictionary lookups for candidate retrieval is mostly
     * not sufficient! Consider implementing your own candidate retrieval e.g. fuzzy
     * lookup, Lucene-based etc...
     */

//    private final File dictionaryFile = new File("src/main/resources/examples/nerla/dicts/injury.dict");

    /**
     * The directory of the corpus instances. In this example each instance is
     * stored in its own json-file.
     */

    public NamedEntityRecognitionAndLinkingGeneralTest(String typeOfTopic, String modelName, int recallFactor) {

        /**
         * 1. STEP initialize the system.
         *
         * The scope represents the specifications of the 4 (in this case only the
         * entities-file is important) defined specification files. The scope mainly
         * affects the exploration.
         */
        super(SystemScope.Builder.getScopeHandler()
                /**
                 * We add a scope reader that reads and interprets the 4 specification files.
                 */
                .addScopeSpecification(new CSVDataStructureReader(new File("ner/" + typeOfTopic + "/data_structure/entities.csv"), new File("ner/" + typeOfTopic + "/data_structure/hierarchies.csv"), new File("ner/" + typeOfTopic + "/data_structure/slots.csv"), new File("ner/" + typeOfTopic + "/data_structure/structures.csv")))
                /**
                 * We apply the scope(s).
                 */
                .apply()
                /**
                 * Now normalization functions can be added. A normalization function is
                 * especially used for literal-based annotations. In case a normalization
                 * function is provided for a specific entity type, the normalized value is
                 * compared during evaluation instead of the actual surface form. A
                 * normalization function normalizes different surface forms so that e.g. the
                 * weights "500 g", "0.5kg", "500g" are all equal. Each normalization function
                 * is bound to exactly one entity type.
                 */
//                .registerNormalizationFunction(new WeightNormalization())
//                .registerNormalizationFunction(new AgeNormalization())
                /**
                 * Finally, we build the systems scope.
                 */
                .build());

        this.typeOfTopic = typeOfTopic;
        this.instanceDirectory = new File("ner/" + typeOfTopic + "/instances/");
        this.modelName = modelName;
        this.recallFactor = recallFactor;
    }

    public void startProcedure(int mode, double alpha) throws IOException {

        log.info("Trainiert mit Objective Function, getestet mit ObjectiveFunctionPartialOverlap");
        log.info("Evaluation Detail: " + evaluationDetail.toString());
        log.info("MaxStepCrit: 50");

        this.alpha = alpha;
        this.mode = mode;


        InstanceProvider.maxNumberOfAnnotations = 1000;

        /**
         * 2. STEP read and distribute the corpus.
         *
         * We define a corpus distribution strategy. In this case we want to
         * redistribute the corpus based on a random shuffle. We set the corpus size to
         * 1F which is 100%. This value can be reduced ion order to read less data
         * during development e.g. We set the training proportion to 80 (%) and test
         * proportion to 20 (%). Finally we can define a seed to ensure the same random
         * instance assignment during development.
         *
         */
        AbstractCorpusDistributor shuffleCorpusDistributor = new ShuffleCorpusDistributor.Builder()
                .setCorpusSizeFraction(1F).setTrainingProportion(80).setTestProportion(20).setSeed(100L).build();


        /**
         * The instance provider reads all json files in the given directory. We can set
         * the distributor in the constructor. If not all instances should be read from
         * the file system, we can add an additional parameter that specifies how many
         * instances should be read. NOTE: in contrast to the corpusSizeFraction in the
         * ShuffleCorpusDistributor, we initially set a limit to the number of files
         * that should be read.
         */
        InstanceProvider instanceProvider = new InstanceProvider(instanceDirectory, shuffleCorpusDistributor);


        /**
         * 3. STEP
         *
         * For the named entity recognition and linking, we need to define a candidate
         * retrieval strategy in order to speed up the exploration. For this simple
         * case, we chose an in-memory dictionary lookup candidate retrieval.
         *
         * WARN: If no candidate retrieval is provided, an exhaustive retrieval is
         * performed! @see ExhaustiveCandidateRetrieval
         *
         */

        CreateDictionaryClass dictionaryClass = new CreateDictionaryClass(instanceProvider.getInstances());

        for (Instance instance : instanceProvider.getInstances()) {
            instance.addCandidates(dictionaryClass.getDictionary());
        }


        /**
         * For the entity recognition and linking problem, the EntityRecLinkExplorer is
         * added to perform changes during the exploration. This explorer is especially
         * designed for NERLA and is parameterized with a candidate retrieval.
         */
        EntityRecLinkExplorerCustom explorer = new EntityRecLinkExplorerCustom();

        /**
         * 4. STEP
         *
         * Define further and create CRF parameter.
         */

        /**
         * As objective function we select the NERLA-specific objective function which
         * basically checks overlapping annotation in gold and predicted set.
         *
         * Each objective function is parameterized with an EvaluationDetail-parameter.
         * The most strict is the DOCUMENT_LINKED-evaluation. Here, the position in the
         * text, the annotation text and the assigned entity is compared and needs to be
         * correct to count as true positive.
         *
         * Switch to EntityType to ignore position and surface form during evaluation,
         * if not necessary.
         *
         */
//		IObjectiveFunction objectiveFunction = new BetaNerlaObjectiveFunction(EEvaluationDetail.LITERAL);
        IObjectiveFunction objectiveFunction = new NerlaObjectiveFunction(evaluationDetail);

        /**
         * The learner defines the update strategy of learned weights. parameters are
         * the alpha value that is specified in the SGD (first parameter) and the
         * L2-regularization value.
         *
         * TODO: find best alpha value in combination with L2-regularization.
         */
        AdvancedLearner learner = new AdvancedLearner(new SGD(alpha, 0), new L2(0.0001)); //alpha von 0.001 scheint besser als 0.01, 0.0001 macht jedoch wieder schlechter

        /**
         * Next, we need to specify the actual feature templates. In this example we
         * provide 3 templates that implements standard features like morphological-,
         * context-, and surface form-features.
         *
         * TODO: Implement further templates / features to solve your problem.
         *
         */


        featureTemplates = new ArrayList<>();

        switch (mode) {
            case 1:
                featureTemplates.add(new AMFLTemplate());
                featureTemplates.add(new BMFLTemplate());
                featureTemplates.add(new MentionsInSentenceTemplate_FAST());
//                featureTemplates.add(new WBOTemplate());
                featureTemplates.add(new WBTemplate_FAST());
//                featureTemplates.add(new WMTemplate());
                featureTemplates.add(new WordsInBetweenTemplate_FAST());
//                featureTemplates.add(new BigramTemplate());
//                featureTemplates.add(new HMTemplate());
                featureTemplates.add(new NumberMBTemplate_FAST());
                featureTemplates.add(new NumberWBTemplate_FAST());
//                featureTemplates.add(new OverlappingTemplate());
                featureTemplates.add(new PosInDocTemplate());
                featureTemplates.add(new PosInSentenceTemplate());
                break;
            case 2:
                featureTemplates.add(new AMFLTemplate());
                featureTemplates.add(new BMFLTemplate());
                featureTemplates.add(new GroupNamesInSameSentenceTemplate_FAST());
//                featureTemplates.add(new WBOTemplate());
                featureTemplates.add(new WBFGroupNamesTemplate_FAST());
                featureTemplates.add(new WBLGroupNamesTemplate_FAST());
//                featureTemplates.add(new WMTemplate());
                featureTemplates.add(new WordsInBetweenGroupNamesTemplate_FAST());
//                featureTemplates.add(new BigramTemplate());
//                featureTemplates.add(new HMTemplate());
                featureTemplates.add(new NumberMBTemplate_FAST());
                featureTemplates.add(new NumberWBTemplate_FAST());
//                featureTemplates.add(new OverlappingTemplate());
                featureTemplates.add(new PosInDocTemplate());
                featureTemplates.add(new PosInSentenceTemplate());
                break;
            case 3:
                featureTemplates.add(new AMFLTemplate());
                featureTemplates.add(new BMFLTemplate());
            case 4:
                featureTemplates.add(new AMFLTemplate());
                featureTemplates.add(new BMFLTemplate());
                featureTemplates.add(new OverlappingTemplate());
                break;
            case 5:
                featureTemplates.add(new AMFLTemplate());
                featureTemplates.add(new BMFLTemplate());
                featureTemplates.add(new SimilarWordsTemplate());
            case 6:
                featureTemplates.add(new BigramTemplate());
                featureTemplates.add(new SimilarWordsTemplate());
                break;
            case 7:
                featureTemplates.add(new BigramTemplate());
                featureTemplates.add(new OverlappingTemplate());
                break;
            case 8:
                featureTemplates.add(new BigramTemplate());
                featureTemplates.add(new OverlappingTemplate());
                featureTemplates.add(new SimilarWordsTemplate());
                break;
            case 9:
                featureTemplates.add(new BigramTemplate());
                featureTemplates.add(new OverlappingTemplate());
                featureTemplates.add(new SimilarWordsTemplate());
                featureTemplates.add(new AMFLTemplate());
                featureTemplates.add(new BMFLTemplate());
                break;
        }


//        addNumberTemplates(featureTemplates);

//
//        featureTemplates.add(new AMFLTemplate());
//        featureTemplates.add(new BMFLTemplate());


//		featureTemplates.add(new WMTemplate()); //scheint nichts beizutragen, obwohl einzeln nicht schlecht


//		featureTemplates.add(new WBOTemplate());


        //		featureTemplates.add(new WeightBetweenTemplate());


//		featureTemplates.add(new MorphologicalNerlaTemplate());
//		featureTemplates.add(new TokenContextTemplate());
//		featureTemplates.add(new IntraTokenTemplate());
//		featureTemplates.add(new LevenshteinTemplate());

//		featureTemplates.add(new AvgNumberTemplate());

        /**
         * During exploration we initialize each state with no annotations. In NERLA
         * this makes sense, as random annotations are likely to be wrong. However, in
         * more complex tasks, such as slot filling, a state might be initialized with
         * an empty entity-template. @see SlotFillingExample for more details.
         */
        IStateInitializer stateInitializer = ((instance) -> new State(instance, new Annotations()));

        /**
         * Number of epochs, the system should train.
         *
         * TODO: Find perfect number of epochs.
         */
        int numberOfEpochs = 10;

        /**
         * To increase the systems speed performance, we add two stopping criterion for
         * sampling. The first one is a maximum chain length of produced states. In this
         * example we set the maximum chain length to 10. That means, only 10 changes
         * (annotations) can be added to each document.
         */
        ISamplingStoppingCriterion maxStepCrit = new MaxChainLengthCrit(100);

        /**
         * The next stopping criterion checks for no or only little (based on a
         * threshold) changes in the model score of the produced chain. In this case, if
         * the last three states were scored equally, we assume the system to be
         * converged.
         */
        ISamplingStoppingCriterion noModelChangeCrit = new ConverganceCrit(3, s -> s.getModelScore());
        ISamplingStoppingCriterion[] sampleStoppingCrits = new ISamplingStoppingCriterion[]{maxStepCrit,
                noModelChangeCrit};

        /**
         * Sampling strategy that defines how the system should be trained. We
         * distinguish between two sampling and strategies and two sampling modes.
         *
         * The two modes are:
         *
         * M1: Sample based on objective function.
         *
         * M2: Sample based on model score.
         *
         * The two strategies are:
         *
         * S1: Select the best state greedily
         *
         * S2: Select the next state based on the distribution of model or objective
         * score, respectively.
         *
         *
         * For now, we chose a simple epoch switch strategy that switches between greedy
         * objective score and greedy models score every epoch.
         *
         * TODO: Although many problems seem to work well with this strategy there are
         * certainly better strategies.
         */
//		AbstractSampler sampler = SamplerCollection.greedyModelStrategy();
//		AbstractSampler sampler = SamplerCollection.greedyObjectiveStrategy();
        AbstractSampler sampler = new EpochSwitchSampler(epoch -> epoch % 2 == 0);
//		AbstractSampler sampler = new EpochSwitchSampler(new RandomSwitchSamplingStrategy());
//		AbstractSampler sampler = new EpochSwitchSampler(e -> new Random(e).nextBoolean());

        /**
         * Finally, we chose a model base directory and a name for the model.
         *
         * NOTE: Make sure that the base model directory exists!
         */
        final File modelBaseDir = new File("models/nerla/");
//        final String modelName = "NERLA1234" + new Random().nextInt(10000);
//        final String modelName = "NERLA12341332";

        Model model;


        if (Model.exists(modelBaseDir, modelName) && !overrideModel) {
            /**
             * If the model exists load from the file system.
             */
            model = Model.load(modelBaseDir, modelName);
        } else {
            /**
             * If the model does not exists, create a new model.
             */
            model = new Model(featureTemplates, modelBaseDir, modelName);
        }

        /**
         * Create a new semantic parsing CRF and initialize with needed parameter.
         */
        crf = new SemanticParsingCRFCustomTwo(model, explorer, sampler, stateInitializer, objectiveFunction);

//        IEvaluatable.Score coverage = crf.computeCoverage(true,objectiveFunction, instanceProvider.getRedistributedTrainingInstances());
//
//        System.out.println(coverage);
//
//
//         System.exit(1);


        /**
         * If the model was loaded from the file system, we do not need to train it.
         */
        if (!model.isTrained()) {
            /**
             * Train the CRF.
             */
//            crf.train(learner, instanceProvider.getRedistributedTrainingInstances(), numberOfEpochs, sampleStoppingCrits);
            crf.train(learner, instanceProvider.getRedistributedTrainingInstances(), numberOfEpochs, sampleStoppingCrits);

            /**
             * Save the model as binary. Do not override, in case a file already exists for
             * that name.
             */
            model.save(false);

            /**
             * Print the model in a readable format.
             */
            model.printReadable();
        }

        /**
         * At this position the model was either successfully loaded or trained. Now we
         * want to apply the model to unseen data. We select the redistributed test data
         * in this case. This method returns for each instances a final state (best
         * state based on the trained model) that contains annotations.
         */


        log.info("******************TRAINIERT MIT LITERAL - PREDICTED UND EVALUIERT MIT " + evaluationDetail + "*****************************");
        log.info("PREDICTHIGHRECALL: " + recallFactor);

        crf.changeObjectiveFunction(new NerlaObjectiveFunctionPartialOverlap(evaluationDetail));


        Map<Instance, State> results = crf.predictHighRecall(instanceProvider.getRedistributedTestInstances(), recallFactor, maxStepCrit,
                noModelChangeCrit);

        /*
          Finally, we evaluate the produced states and print some statistics.
         */

        writeToJson(results, "jsonFiles/treatment_literal_for_filtering/");

        filterForSuperEntitiesTreatment(results);

//        mean = evaluate(log, results);

        log.info(crf.getTrainingStatistics());
        log.info(crf.getTestStatistics());

        log.info("genutztes Modell: " + modelBaseDir.toString() + "/" + modelName);

//        log.info("******************TRAINIERT MIT LITERAL - GETESTET MIT DOCLINKED*****************************");
//
//        crf.changeObjectiveFunction(new NerlaObjectiveFunctionPartialOverlap(EEvaluationDetail.DOCUMENT_LINKED));
//
//        results = crf.predictHighRecall(instanceProvider.getRedistributedTestInstances(), recallFactor , maxStepCrit,
//                noModelChangeCrit);
//        mean = evaluate(log, results);
//
//        log.info(crf.getTrainingStatistics());
//        log.info(crf.getTestStatistics());
//
//        log.info("genutztes Modell: " + modelBaseDir.toString() + "/" + modelName);
    }

    private void writeToJson(Map<Instance, State> results, String path) throws IOException {
        Map<Instance, Set<DocumentLinkedAnnotation>> annotations = new HashMap<>();

        for (Map.Entry<Instance, State> result : results.entrySet()) {
            for (AbstractAnnotation aa : result.getValue().getCurrentPredictions().getAnnotations()) {

                annotations.putIfAbsent(result.getKey(), new HashSet<>());
                annotations.get(result.getKey()).add(aa.asInstanceOfDocumentLinkedAnnotation());
            }
        }

        JsonNerlaIO io = new JsonNerlaIO(true);

        for (Instance instance : results.keySet()) {
            if (annotations.get(instance) != null) {
                List<JsonEntityAnnotationWrapper> wrappedAnnotation = annotations.get(instance).stream()
                        .map(d -> new JsonEntityAnnotationWrapper(d))
                        .collect(Collectors.toList());
                io.writeNerlas(new File(path + instance.getName() + ".json"), wrappedAnnotation);
            }
        }
    }

    private IEvaluatable.Score filterForSuperEntitiesInjury(Map<Instance, State> results) {
        IEvaluatable.Score score = new IEvaluatable.Score();

        int counter = 0;

        for (Map.Entry<Instance, State> result : results.entrySet()) {
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

    private IEvaluatable.Score filterForSuperEntitiesTreatment(Map<Instance, State> results) {
        IEvaluatable.Score score = new IEvaluatable.Score();

        int counter = 0;

        for (Map.Entry<Instance, State> result : results.entrySet()) {

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

    private Collection<Instance.GoldModificationRule> getGoldModifications() {
        List<Instance.GoldModificationRule> goldMods = new ArrayList<>();

        goldMods.add(a -> {
            return a.getEntityType().getTransitiveClosureSuperEntityTypes().contains(EntityType.get("Treatment")) ? a : null;
        });

        return goldMods;
    }

    private void addNumberTemplates(List<AbstractFeatureTemplate> featureTemplates) {
        featureTemplates.add(new NumberMBTemplate());
        featureTemplates.add(new NumberWBTemplate());
    }

    private void addsingleContextTemplates(List<AbstractFeatureTemplate> featureTemplates) {
        featureTemplates.add(new BMFLTemplate(false));
        featureTemplates.add(new AMFLTemplate(false));
        featureTemplates.add(new BracketsTemplate());
    }

    private void addDoubleContextTemplates(List<AbstractFeatureTemplate> featureTemplates) {
        featureTemplates.add(new MentionsInSentenceTemplate()); //sehr nützlich
        featureTemplates.add(new WordsInBetweenTemplate());
        featureTemplates.add(new WBTemplate());
        featureTemplates.add(new RootTypeTemplate());
        featureTemplates.add(new WBOTemplate());
    }

    private void addSingleMentionTemplates(List<AbstractFeatureTemplate> featureTemplates) {
        featureTemplates.add(new BigramTemplate(false)); //nützlich
        featureTemplates.add(new HMTemplate(false));
        featureTemplates.add(new StartsWithCapitalTemplate());
        featureTemplates.add(new IdentityTemplate());
        featureTemplates.add(new WMTemplate());
        featureTemplates.add(new WordCountTemplate());
    }

    private void addDoubleComparisonTemplates(List<AbstractFeatureTemplate> featureTemplates) {
        featureTemplates.add(new OverlappingTemplate(false));
        featureTemplates.add(new SimilarWordsTemplate());
    }

    private void addNormalizationtemplates(List<AbstractFeatureTemplate> featureTemplates) {
        featureTemplates.add(new NormalizedWeightTemplate());
        featureTemplates.add(new NormalizedAgeTemplate());
    }


    public SemanticParsingCRFCustomTwo getCRF() {
        return crf;
    }

    public IEvaluatable.Score getMean() {
        return mean;
    }

    public List<AbstractFeatureTemplate<?>> getFeatureTemplates() {
        return featureTemplates;
    }

}
