package tsandmeier.ba;

import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.learner.AdvancedLearner;
import de.hterhors.semanticmr.crf.learner.optimizer.SGD;
import de.hterhors.semanticmr.crf.learner.regularizer.L2;
import de.hterhors.semanticmr.crf.model.FactorPoolCache;
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
import de.hterhors.semanticmr.crf.variables.*;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.init.reader.csv.CSVDataStructureReader;
import de.hterhors.semanticmr.init.specifications.SystemScope;
import de.hterhors.semanticmr.json.nerla.JsonNerlaIO;
import de.hterhors.semanticmr.json.nerla.wrapper.JsonEntityAnnotationWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;
import tsandmeier.ba.candprov.CreateDictionaryClass;
import tsandmeier.ba.crf.SemanticParsingCRFCustom;
import tsandmeier.ba.explorer.EntityRecLinkExplorerCustom;
import tsandmeier.ba.explorer.EntityRecLinkExplorerInjury;
import tsandmeier.ba.groupnameTemplates.GroupNamesInSameSentenceTemplate_FAST;
import tsandmeier.ba.groupnameTemplates.WBFGroupNamesTemplate_FAST;
import tsandmeier.ba.groupnameTemplates.WBLGroupNamesTemplate_FAST;
import tsandmeier.ba.groupnameTemplates.WordsInBetweenGroupNamesTemplate_FAST;
import tsandmeier.ba.templates.*;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Example of how to perform named entity recognition and linking.
 */
public class NamedEntityRecognitionAndLinkingInjury extends AbstractSemReadProject {
    private static final Logger log = LogManager.getFormatterLogger("de.hterhors.semanticmr.projects.examples.corpus.nerl.NerlCorpusCreationExample");
    private final boolean overrideModel = false;
    SemanticParsingCRFCustom crf;
    private IEvaluatable.Score mean;
    List<AbstractFeatureTemplate<?>> featureTemplates;
    private final EEvaluationDetail evaluationDetail = EEvaluationDetail.DOCUMENT_LINKED;

    int recallFactor;
    File instanceDirectory;
    String modelName;


    /**
     * Start the named entity recognition and linking procedure.
     *
     * @param args
     */
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        new NamedEntityRecognitionAndLinkingInjury(0.001, 1);
    }

    public NamedEntityRecognitionAndLinkingInjury(double alpha, int recallFactor) throws IOException, ParserConfigurationException, SAXException {
        super(SystemScope.Builder.getScopeHandler()
                .addScopeSpecification(new CSVDataStructureReader(new File("ner/" + "injury" + "/data_structure/entities.csv"), new File("ner/" + "injury" + "/data_structure/hierarchies.csv"), new File("ner/" + "injury" + "/data_structure/slots.csv"), new File("ner/" + "injury" + "/data_structure/structures.csv")))
                .apply()
                .build());

        this.instanceDirectory = new File("ner/" + "injury" + "/instances/");
        this.modelName = modelName;
        this.recallFactor = recallFactor;

//        log.info("Trainiert mit Objective Function, getestet mit ObjectiveFunctionPartialOverlap");
        log.info("Evaluation Detail: " + evaluationDetail.toString());
        log.info("MaxStepCrit: 50");
        log.info("PREDICTHIGHRECALL: " + recallFactor);


        InstanceProvider.maxNumberOfAnnotations = 1000;

        Set<String> stopWords = new HashSet<>(Collections.emptySet());
        try {
            stopWords.addAll(Files.readAllLines(Paths.get("src/main/resources/stopWords.txt")));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        Document.setStopWords(stopWords);

        Set<String> punctuationWords = Collections.unmodifiableSet(new HashSet(Arrays.asList("±", ",", ".", "-", "_", ";", ":", "#", "'", "+", "*", "!", "\"", "§", "$", "%", "&", "/", "{", "}", "[", "]", "=", "?", "\\", "´", "`", "^", "°", "<", ">", "|")));

        Document.setPunctuationWords(punctuationWords);


        AbstractCorpusDistributor shuffleCorpusDistributor = new ShuffleCorpusDistributor.Builder()
                .setCorpusSizeFraction(1F).setTrainingProportion(80).setTestProportion(20).setSeed(100L).build();


        InstanceProvider instanceProvider = new InstanceProvider(instanceDirectory, shuffleCorpusDistributor);


        CreateDictionaryClass dictionaryClass = new CreateDictionaryClass(instanceProvider.getInstances());

        for (Instance instance : instanceProvider.getInstances()) {
//            instance.addCandidates(dictionaryClass.getDictionary());

            instance.addCandidates(EntityType.get("Injury").getRelatedEntityTypes());

//            instance.addCandidates(EntityType.get("Injury").getRelatedEntityTypes().stream()
//                    .filter(et -> !et.isLiteral)
//                    .collect(Collectors.toSet())
//            );
        }

        EntityRecLinkExplorerInjury explorer = new EntityRecLinkExplorerInjury();

        explorer.fillWindowSizeEntitiesFromGoldAnnotaions(instanceProvider.getInstances());

//        EntityRecLinkExplorer explorer = new EntityRecLinkExplorer();

//		IObjectiveFunction objectiveFunction = new BetaNerlaObjectiveFunction(EEvaluationDetail.LITERAL);
        IObjectiveFunction objectiveFunction = new NerlaObjectiveFunction(evaluationDetail);

        AdvancedLearner learner = new AdvancedLearner(new SGD(alpha, 0), new L2(0.0001)); //alpha von 0.001 scheint besser als 0.01, 0.0001 macht jedoch wieder schlechter


        featureTemplates = new ArrayList<>();

                featureTemplates.add(new AMFLTemplate());
                featureTemplates.add(new BMFLTemplate());
                featureTemplates.add(new MentionsInSentenceTemplate_FAST());
                featureTemplates.add(new WBTemplate_FAST());
                featureTemplates.add(new WordsInBetweenTemplate_FAST());
                featureTemplates.add(new BigramTemplate(true));
                featureTemplates.add(new BagOfWordsTemplate());
                featureTemplates.add(new NumberMBTemplate_FAST());
                featureTemplates.add(new NumberWBTemplate_FAST());
                featureTemplates.add(new PosInDocTemplateZehntel());
                featureTemplates.add(new PosInSentenceTemplateZehntel());

                featureTemplates.add(new StartsWithCapitalTemplate());
                featureTemplates.add(new OnlyUppercaseTemplate());
                featureTemplates.add(new ContainsDigitTemplate());
//
//        featureTemplates.add(new MedicalHeadingTemplate());


        IStateInitializer stateInitializer = ((instance) -> new State(instance, new Annotations()));

        int numberOfEpochs = 10;

        ISamplingStoppingCriterion maxStepCrit = new MaxChainLengthCrit(100);

        ISamplingStoppingCriterion noModelChangeCrit = new ConverganceCrit(3, s -> s.getModelScore());
        ISamplingStoppingCriterion[] sampleStoppingCrits = new ISamplingStoppingCriterion[]{maxStepCrit,
                noModelChangeCrit};


//		AbstractSampler sampler = SamplerCollection.greedyModelStrategy();
//		AbstractSampler sampler = SamplerCollection.greedyObjectiveStrategy();
        AbstractSampler sampler = new EpochSwitchSampler(epoch -> epoch % 2 == 0);
//		AbstractSampler sampler = new EpochSwitchSampler(new RandomSwitchSamplingStrategy());
//		AbstractSampler sampler = new EpochSwitchSampler(e -> new Random(e).nextBoolean());


        final File modelBaseDir = new File("models/nerla/");
        this.modelName = "NERLA1234" + new Random().nextInt(10000);
//        this.modelName = "NERLA12346839";




        Model model;



        if (Model.exists(modelBaseDir, this.modelName) && !overrideModel) {

            model = Model.load(modelBaseDir, this.modelName);
        } else {
            model = new Model(featureTemplates, modelBaseDir, this.modelName);
        }

        model.setCache(new FactorPoolCache(model, 6400, 3200));

        crf = new SemanticParsingCRFCustom(model, explorer, sampler, stateInitializer, objectiveFunction);


//        long timeBefore = System.currentTimeMillis();
//        IEvaluatable.Score coverage = crf.computeCoverage(true, objectiveFunction, instanceProvider.getInstances());
//        System.out.println(coverage);
//        long timeAfter = System.currentTimeMillis();
//        System.out.println("Dauer: "+((timeAfter-timeBefore)/1000));
//        System.out.println("Average Number of Proposal States: " + explorer.getAverageNumberOfNewProposalStates());
//        System.exit(1);


        if (!model.isTrained()) {
            crf.train(learner, instanceProvider.getRedistributedTrainingInstances(), numberOfEpochs, sampleStoppingCrits);

            model.save(true);

            log.info("Modell gespeichert unter: " + modelBaseDir.toString() + "/" + this.modelName);

            model.printReadable();
        }

//        crf.changeObjectiveFunction(new NerlaObjectiveFunctionPartialOverlap(evaluationDetail));

        log.info("****************PREDICTED MIT ENTITYTYPE**************************");

        crf.changeObjectiveFunction(new NerlaObjectiveFunction(EEvaluationDetail.ENTITY_TYPE));

        Map<Instance, State> results = crf.predictHighRecall(instanceProvider.getRedistributedTestInstances(), recallFactor, maxStepCrit,
                noModelChangeCrit);

        mean = evaluate(log, results);


        log.info("****************PREDICTED MIT LITERAL**************************");

        crf.changeObjectiveFunction(new NerlaObjectiveFunction(EEvaluationDetail.LITERAL));

        results = crf.predictHighRecall(instanceProvider.getRedistributedTestInstances(), recallFactor, maxStepCrit,
                noModelChangeCrit);

        mean = evaluate(log, results);


        log.info("****************PREDICTED MIT DOCLINKED**************************");

        crf.changeObjectiveFunction(new NerlaObjectiveFunction(EEvaluationDetail.DOCUMENT_LINKED));

        results = crf.predictHighRecall(instanceProvider.getRedistributedTestInstances(), recallFactor, maxStepCrit,
                noModelChangeCrit);

        mean = evaluate(log, results);

        log.info(crf.getTrainingStatistics());
        log.info(crf.getTestStatistics());

        log.info("genutztes Modell: " + modelBaseDir.toString() + "/" + this.modelName);


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

    private Collection<Instance.GoldModificationRule> getGoldModifications() {
        List<Instance.GoldModificationRule> goldMods = new ArrayList<>();

        goldMods.add(a -> {
            return a.getEntityType().getTransitiveClosureSuperEntityTypes().contains(EntityType.get("Treatment")) ? a : null;
        });

        return goldMods;
    }

    public IEvaluatable.Score getMean() {
        return mean;
    }

}
