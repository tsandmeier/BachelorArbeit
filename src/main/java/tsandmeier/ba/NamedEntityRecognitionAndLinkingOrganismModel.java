package tsandmeier.ba;

import de.hterhors.semanticmr.candprov.nerla.NerlaCandidateProviderCollection;
import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.SemanticParsingCRF;
import de.hterhors.semanticmr.crf.exploration.EntityRecLinkExplorer;
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
import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.Annotations;
import de.hterhors.semanticmr.crf.variables.IStateInitializer;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.crf.variables.State;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.init.specifications.SystemScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tsandmeier.ba.candprov.GetDictionaryClass;
import tsandmeier.ba.normalizer.AgeNormalization;
import tsandmeier.ba.normalizer.WeightNormalization;
import tsandmeier.ba.specs.NERLASpecsOrganismModel;
import tsandmeier.ba.templates.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Example of how to perform named entity recognition and linking.
 */
public class NamedEntityRecognitionAndLinkingOrganismModel extends AbstractSemReadProject {
    private static Logger log = LogManager.getFormatterLogger("NamedEntityRecognitionAndLinkingOrganismModel");
    private double alpha = 0.001;
    private final boolean overrideModel = false;
    SemanticParsingCRF crf;
    List<AbstractFeatureTemplate> featureTemplates;
    IEvaluatable.Score mean;

    /**1: all
     * 2: singleContextTemplates
     * 3: doubleContextTemplates
     * 4: singleMentionTemplates
     * 5: NumberTemplates
     * 6: POS-Tagging
     * 7:
     */

    /**
     * Start the named entity recognition and linking procedure.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new NamedEntityRecognitionAndLinkingOrganismModel().startProcedure();
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

    private final File dictionaryFile = new File("src/main/resources/examples/nerla/dicts/organismModel.dict");

    /**
     * The directory of the corpus instances. In this example each instance is
     * stored in its own json-file.
     */
    private final File instanceDirectory = new File("src/main/resources/examples/nerla/organismModel/corpus/instances/");

    public NamedEntityRecognitionAndLinkingOrganismModel() {

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
                .addScopeSpecification(NERLASpecsOrganismModel.csvSpecsReader)
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
                .registerNormalizationFunction(new WeightNormalization())
                .registerNormalizationFunction(new AgeNormalization())
                /**
                 * Finally, we build the systems scope.
                 */
                .build());


    }

    public void startProcedure() throws IOException {

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
                .setCorpusSizeFraction(1F).setTrainingProportion(80).setTestProportion(20).build();

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
//        NerlaCandidateProviderCollection candidateRetrieval = new NerlaCandidateProviderCollection(
//                new GetDictionaryClass(dictionaryFile));
        NerlaCandidateProviderCollection candidateRetrieval = new NerlaCandidateProviderCollection(
                new GetDictionaryClass(dictionaryFile));

        /**
         * For the entity recognition and linking problem, the EntityRecLinkExplorer is
         * added to perform changes during the exploration. This explorer is especially
         * designed for NERLA and is parameterized with a candidate retrieval.
         */
        EntityRecLinkExplorer explorer = new EntityRecLinkExplorer(candidateRetrieval);

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
        IObjectiveFunction objectiveFunction = new NerlaObjectiveFunction(EEvaluationDetail.DOCUMENT_LINKED);

        /**
         * The learner defines the update strategy of learned weights. parameters are
         * the alpha value that is specified in the SGD (first parameter) and the
         * L2-regularization value.
         *
         */
        AdvancedLearner learner = new AdvancedLearner(new SGD(alpha, 0), new L2(0.0001)); //alpha von 0.001 scheint besser als 0.01, 0.0001 macht jedoch wieder schlechter

        /**
         * Next, we need to specify the actual feature templates. In this example we
         * provide 3 templates that implements standard features like morphological-,
         * context-, and surface form-features.
         *
         */

        featureTemplates = new ArrayList<>();

        addNumberTemplates(featureTemplates);
        addsingleContextTemplates(featureTemplates);
        addDoubleContextTemplates(featureTemplates);
        addSingleMentionTemplates(featureTemplates);
        featureTemplates.add(new ML12Template());
        addNormalizationtemplates(featureTemplates);
        addDoubleComparisonTemplates(featureTemplates);


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
         */
        int numberOfEpochs = 10;  //10 scheint doppelt so gut wie 9, danach wohl keine Besserung

        /**
         * To increase the systems speed performance, we add two stopping criterion for
         * sampling. The first one is a maximum chain length of produced states. In this
         * example we set the maximum chain length to 10. That means, only 10 changes
         * (annotations) can be added to each document.
         */
        ISamplingStoppingCriterion maxStepCrit = new MaxChainLengthCrit(10);

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
        final File modelBaseDir = new File("models/nerla/organismModel/");
        final String modelName = "NERLA1234" + new Random().nextInt(10000);
//        final String modelName = "testModel";

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
        crf = new SemanticParsingCRF(model, explorer, sampler, stateInitializer, objectiveFunction);

//        IEvaluatable.Score coverage = crf.computeCoverage(true,objectiveFunction, instanceProvider.getRedistributedTrainingInstances());
//
//        System.out.println("COVERAGE: "+coverage);
//
//        System.exit(1);

        /**
         * If the model was loaded from the file system, we do not need to train it.
         */
        if (!model.isTrained()) {
            /**
             * Train the CRF.
             */
            crf.train(learner, instanceProvider.getRedistributedTrainingInstances(), numberOfEpochs, sampleStoppingCrits);

            /**
             * Save the model as binary. Do not override, in case a file already exists for
             * that name.
             */
            model.save(true);

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
//
        Map<Instance, State> results = crf.predict(instanceProvider.getRedistributedTestInstances(), maxStepCrit,
                noModelChangeCrit);


        /**
         * Finally, we evaluate the produced states and print some statistics.
         */
        mean = evaluate(log, results);

		log.info(crf.getTrainingStatistics());
		log.info(crf.getTestStatistics());


            //for saving the results into a table
        //StatSaver.addToSpreadsheet("statistics/organismModel_stats_no_POS_Norm_new.ods", featureTemplates, mean.getF1(), crf.getTrainingStatistics().getTotalDuration() + crf.getTestStatistics().getTotalDuration(),
        //        crf.getTrainingStatistics().getTotalDuration(), crf.getTestStatistics().getTotalDuration(), alpha);


//        Map<Instance, State> results = crf.predictHighRecall(instanceProvider.getInstances(),5, maxStepCrit,
//                noModelChangeCrit);



        //To get the Json Files

        //      Map<Instance, Set<DocumentLinkedAnnotation>> annotations = new HashMap<>();

        //     for (Map.Entry<Instance, State> result : results.entrySet()) {
        //       for (AbstractAnnotation aa : result.getValue().getCurrentPredictions().getAnnotations()) {

        //         annotations.putIfAbsent(result.getKey(), new HashSet<>());
        //       annotations.get(result.getKey()).add(aa.asInstanceOfDocumentLinkedAnnotation());
        //   }
        //  }

//        JsonNerlaIO io = new JsonNerlaIO(true);

//       for (Instance instance : results.keySet()) {


        //          List<JsonEntityAnnotationWrapper> wrappedAnnotation = annotations.get(instance).stream()
        //                .map(d -> new JsonEntityAnnotationWrapper(d))
        //               .collect(Collectors.toList());
        //        io.writeNerlas(new File("jsonFiles/OrganismModel/HighRecall30/"+instance.getName() + ".nerla.json"), wrappedAnnotation);

        //  }

//
//        IEvaluatable.Score mean = evaluate(log, results);
//        System.out.println(crf.getTrainingStatistics());
//        System.out.println(crf.getTestStatistics());

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


    public SemanticParsingCRF getCRF() {
        return crf;
    }

    public IEvaluatable.Score getMean() {
        return mean;
    }

    public List<AbstractFeatureTemplate> getFeatureTemplates() {
        return featureTemplates;
    }

}
