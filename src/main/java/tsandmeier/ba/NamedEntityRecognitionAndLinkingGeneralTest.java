package tsandmeier.ba;

import com.mashape.unirest.http.exceptions.UnirestException;
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
import tsandmeier.ba.evaluator.NerlaObjectiveFunctionPartialOverlap;
import tsandmeier.ba.explorer.EntityRecLinkExplorerCustom;
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
public class NamedEntityRecognitionAndLinkingGeneralTest extends AbstractSemReadProject {
    private static final Logger log = LogManager.getFormatterLogger("de.hterhors.semanticmr.projects.examples.corpus.nerl.NerlCorpusCreationExample");
    private final boolean overrideModel = false;
    SemanticParsingCRFCustom crf;
    private IEvaluatable.Score mean;
    List<AbstractFeatureTemplate<?>> featureTemplates;
    private final EEvaluationDetail evaluationDetail = EEvaluationDetail.LITERAL;

    int recallFactor;
    File instanceDirectory;
    String modelName;


    /**
     * Start the named entity recognition and linking procedure.
     *
     * @param args
     */
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {

        int mode;
        double alpha;

        if (args.length == 4) {
            mode = Integer.parseInt(args[0]);
            alpha = Double.parseDouble(args[1]);
            new NamedEntityRecognitionAndLinkingGeneralTest(mode, alpha, args[2], args[3], 1);
        } else if (args.length == 3) {
            mode = Integer.parseInt(args[0]);
            alpha = Double.parseDouble(args[1]);
            new NamedEntityRecognitionAndLinkingGeneralTest(mode, alpha, args[2], "NERLA1234" + new Random().nextInt(10000), 1);
        } else if (args.length == 5) {
            mode = Integer.parseInt(args[0]);
            alpha = Double.parseDouble(args[1]);
            new NamedEntityRecognitionAndLinkingGeneralTest(mode, alpha, args[2], args[3], Integer.parseInt(args[4]));
        } else {
            log.info("Falsche Anzahl an Parametern!");
        }
    }

    public NamedEntityRecognitionAndLinkingGeneralTest(int mode, double alpha, String typeOfTopic, String modelName, int recallFactor) throws IOException, ParserConfigurationException, SAXException {
        super(SystemScope.Builder.getScopeHandler()
                .addScopeSpecification(new CSVDataStructureReader(new File("ner/" + typeOfTopic + "/data_structure/entities.csv"), new File("ner/" + typeOfTopic + "/data_structure/hierarchies.csv"), new File("ner/" + typeOfTopic + "/data_structure/slots.csv"), new File("ner/" + typeOfTopic + "/data_structure/structures.csv")))
                .apply()
                .build());

        this.instanceDirectory = new File("ner/" + typeOfTopic + "/instances/");
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
                .setCorpusSizeFraction(0.1F).setTrainingProportion(80).setTestProportion(20).setSeed(100L).build();


        InstanceProvider instanceProvider = new InstanceProvider(instanceDirectory, shuffleCorpusDistributor);


        CreateDictionaryClass dictionaryClass = new CreateDictionaryClass(instanceProvider.getInstances());

        for (Instance instance : instanceProvider.getInstances()) {
//            instance.addCandidates(dictionaryClass.getDictionary());
            instance.addCandidates(EntityType.get("OrganismModel").getRelatedEntityTypes());
        }

        EntityRecLinkExplorerCustom explorer = new EntityRecLinkExplorerCustom();

//        EntityRecLinkExplorer explorer = new EntityRecLinkExplorer();

//		IObjectiveFunction objectiveFunction = new BetaNerlaObjectiveFunction(EEvaluationDetail.LITERAL);
        IObjectiveFunction objectiveFunction = new NerlaObjectiveFunction(evaluationDetail);

        AdvancedLearner learner = new AdvancedLearner(new SGD(alpha, 0), new L2(0.0001)); //alpha von 0.001 scheint besser als 0.01, 0.0001 macht jedoch wieder schlechter


        featureTemplates = new ArrayList<>();

        switch (mode) {
            case 1:
//                featureTemplates.add(new AMFLTemplate(true));
//                featureTemplates.add(new BMFLTemplate(true));
//                featureTemplates.add(new MentionsInSentenceTemplate_FAST(true));
//                featureTemplates.add(new WBTemplate_FAST(true));
//                featureTemplates.add(new WordsInBetweenTemplate_FAST(true));
//                featureTemplates.add(new BigramTemplate());
//                featureTemplates.add(new BagOfWordsTemplate(true));
//                featureTemplates.add(new NumberMBTemplate_FAST(true));
//                featureTemplates.add(new NumberWBTemplate_FAST(true));
//                featureTemplates.add(new PosInDocTemplateZehntel(true));
//                featureTemplates.add(new PosInSentenceTemplateZehntel(true));
//
//                featureTemplates.add(new StartsWithCapitalTemplate(true));
//                featureTemplates.add(new OnlyUppercaseTemplate(true));
//                featureTemplates.add(new ContainsDigitTemplate(true));
//
                featureTemplates.add(new MedicalHeadingTemplate());

                break;
            case 2:
                featureTemplates.add(new AMFLTemplate());
                featureTemplates.add(new BMFLTemplate());
                featureTemplates.add(new GroupNamesInSameSentenceTemplate_FAST());
                featureTemplates.add(new WBFGroupNamesTemplate_FAST());
                featureTemplates.add(new WBLGroupNamesTemplate_FAST());
                featureTemplates.add(new WordsInBetweenGroupNamesTemplate_FAST());
                featureTemplates.add(new NumberMBTemplate_FAST(true));
                featureTemplates.add(new NumberWBTemplate_FAST(true));
                featureTemplates.add(new PosInDocTemplateDrittel());
                featureTemplates.add(new PosInSentenceTemplateDrittel());
                break;
        }

        IStateInitializer stateInitializer = ((instance) -> new State(instance, new Annotations()));

        int numberOfEpochs = 2;

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
//        System.exit(1);


        if (!model.isTrained()) {
            crf.train(learner, instanceProvider.getRedistributedTrainingInstances(), numberOfEpochs, sampleStoppingCrits);

            model.save(true);

            model.printReadable();
        }

//        crf.changeObjectiveFunction(new NerlaObjectiveFunctionPartialOverlap(evaluationDetail));


        Map<Instance, State> results = crf.predictHighRecall(instanceProvider.getRedistributedTestInstances(), recallFactor, maxStepCrit,
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
