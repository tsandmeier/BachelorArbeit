package tsandmeier.ba;

import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.corpus.distributor.AbstractCorpusDistributor;
import de.hterhors.semanticmr.corpus.distributor.ShuffleCorpusDistributor;
import de.hterhors.semanticmr.crf.helper.log.LogUtils;
import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.AnnotationBuilder;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import de.hterhors.semanticmr.crf.variables.*;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.init.reader.csv.CSVDataStructureReader;
import de.hterhors.semanticmr.init.specifications.SystemScope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tsandmeier.ba.tools.AutomatedSectionification;
import tsandmeier.ba.baseLine.CollectExpGroupNames;
import tsandmeier.ba.baseLine.SCIOEntityTypes;
import tsandmeier.ba.crf.SemanticParsingCRFCustomTwo;
import tsandmeier.ba.evaluator.NerlaEvaluatorPartialOverlap;
import tsandmeier.ba.specs.NERLASpecsGroupName;
import tsandmeier.ba.templates.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Example of how to perform named entity recognition and linking.
 */
public class NamedEntityRecognitionAndLinkingGroupBaseLine extends AbstractSemReadProject {
    private static Logger log = LogManager.getFormatterLogger("de.hterhors.semanticmr.projects.examples.corpus.nerl.NerlCorpusCreationExample");
    private final boolean overrideModel = false;
    SemanticParsingCRFCustomTwo crf;
    private IEvaluatable.Score mean;
    private int mode;
    List<AbstractFeatureTemplate<?>> featureTemplates;

    private final EEvaluationDetail evaluationDetail = EEvaluationDetail.LITERAL;

    private static String TYPE_OF_TOPIC = "treatment";

    private static String SPECIFICATION_DIRECTORY = "ner/" + TYPE_OF_TOPIC + "/data_structure/";
    private static String INSTANCE_DIRECTORY = "ner/" + TYPE_OF_TOPIC + "/instances/";


    private double alpha;



    /**
     * Start the named entity recognition and linking procedure.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) {
        new NamedEntityRecognitionAndLinkingGroupBaseLine().startProcedure();
    }

    public NamedEntityRecognitionAndLinkingGroupBaseLine() {

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
                .addScopeSpecification(new CSVDataStructureReader(new File(SPECIFICATION_DIRECTORY + "entities.csv"), new File(SPECIFICATION_DIRECTORY + "hierarchies.csv"), new File(SPECIFICATION_DIRECTORY + "slots.csv"), new File(SPECIFICATION_DIRECTORY + "structures.csv")))
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


    }

    public void startProcedure() {

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
        InstanceProvider instanceProvider = new InstanceProvider(new File(INSTANCE_DIRECTORY), shuffleCorpusDistributor);

//        for(Instance instance: instanceProvider.getInstances()){
//            List<Integer> indexes = new ArrayList<Integer>();
//            String textString = instance.getDocument().documentContent;
//            String word = "OM";
//
//            int index = 0;
//            while(index != -1){
//                index = textString.indexOf(word, index);
//                if (index != -1) {
//                    indexes.add(index);
//                    index++;
//                    System.out.println("INDEX GEFUNDEN: " + index);
//                }
//            }
//        }

        NerlaEvaluatorPartialOverlap nerl = new NerlaEvaluatorPartialOverlap(evaluationDetail);
        Map<Instance, IEvaluatable.Score> testResults = new HashMap<>();
        int counter = 0;
        for(Instance instance: instanceProvider.getInstances()){
            counter++;
            List<DocumentLinkedAnnotation> patternAnnotations = extractGroupNamesWithPattern(instance);

            IEvaluatable.Score score = nerl.prf1(instance.getGoldAnnotations().getAnnotations(), patternAnnotations);

            testResults.putIfAbsent(instance, score);

            log.info("Nummer "+counter+": "+instance.getName());
            log.info("********************************************GOLD************************************************");
            for(AbstractAnnotation annotation: instance.getGoldAnnotations().getAnnotations()){
                log.info(annotation.asInstanceOfDocumentLinkedAnnotation().toPrettyString());
            }
            log.info("********************************************PREDICTED************************************************");
            for(AbstractAnnotation annotation: patternAnnotations){
                log.info(annotation.asInstanceOfDocumentLinkedAnnotation().toPrettyString());
            }
            log.info(System.lineSeparator());



//            System.out.println("F1: "+score.getF1()+" Recall: "+score.getRecall()+" Precision: "+score.getPrecision());
            log.info(score.toString());
        }

        IEvaluatable.Score mean = new IEvaluatable.Score();
        for (Map.Entry<Instance, IEvaluatable.Score> res : testResults.entrySet()) {

            mean.add(res.getValue());
//            LogUtils.logState(log, "======Final Evaluation======", res.getKey(), res.getValue());
        }

        log.info("\n");
        log.info("Mean Score: " + mean);





//




//        public static final PatternIndexPair[] pattern = new PatternIndexPair[] { new PatternIndexPair(0,
//                Pattern.compile("(\\W)[\\w-\\+ '[^\\x20-\\x7E]]{3,20} (treated|grafted|transplanted|(un)?trained)(?=\\W)",
//                        Pattern.CASE_INSENSITIVE)),
//                new PatternIndexPair(1, Pattern.compile(
//                        " ([\\w']+?( with | and | plus | ?(\\+|-|/) ?))*[\\w']+?(-|[^\\x20-\\x7E])(animals|mice|rats|cats|dogs|transplantation)",
//                        Pattern.CASE_INSENSITIVE)),
//                new PatternIndexPair(2, new HashSet<>(Arrays.asList(0, 1)),
//                        Pattern.compile("([^ ]+? (with|and|plus| ?(\\+|-|/) ?) [^ ]+?) ?\\((n)\\W?=\\W?\\d{1,2}\\)",
//                                Pattern.CASE_INSENSITIVE),
//                        3),
//                new PatternIndexPair(3, new HashSet<>(Arrays.asList(0, 1)),
//                        Pattern.compile("received both ([^ ]+ (with|/|and|plus| ?(\\+|-) ?) [^ ]+)",
//                                Pattern.CASE_INSENSITIVE)),
//                new PatternIndexPair(4, new HashSet<>(Arrays.asList(0, 3)),
//                        Pattern.compile("((only|or) )?([a-z][^ ]+?) ?\\((n)\\W?=\\W?\\d{1,2}\\)", Pattern.CASE_INSENSITIVE),
//                        5),
//                new PatternIndexPair(5, new HashSet<>(Arrays.asList(0, 2)),
//                        Pattern.compile(
//                                "(a|the|in) ([\\w-\\+ ']{3,20} (group|animals|mice|rats|cats|dogs|transplantation))",
//                                Pattern.CASE_INSENSITIVE)),
//                new PatternIndexPair(6, new HashSet<>(Arrays.asList(0, 3)),
//                        Pattern.compile(
//                                "(,( ?and ?)?|;)([\\w-\\+ ']{3,20} ?(group|animals|mice|rats|cats|dogs|transplantation))",
//                                Pattern.CASE_INSENSITIVE),
//                        2),
//                new PatternIndexPair(7, new HashSet<>(Arrays.asList(0, 2)), Pattern.compile(
//                        "(\\)|;|:) ?((\\(\\w\\) ?)?([\\w-\\+ ',\\.]|[^\\x20-\\x7E]){5,100})(\\( ?)?n\\W?=\\W?\\d{1,2}( ?\\))?(?=(,|\\.|;))",
//                        Pattern.CASE_INSENSITIVE), 5),
//                new PatternIndexPair(8, new HashSet<>(Arrays.asList(0, 3)), Pattern.compile(
//                        "in(jured)? (animals|mice|rats|cats|dogs).{1,10}(receiv.{3,20}(,|;|\\.| injections?| treatments?))",
//                        Pattern.CASE_INSENSITIVE)),
//                new PatternIndexPair(9, new HashSet<>(Arrays.asList(0, 2)), Pattern.compile(
//                        "(the|a|\\)|in) ([\\w-\\+ ']{3,20} (treated|grafted|transplanted|(un)?trained) ((control |sham )?((injury )?(only )?))? (group|animals|mice|rats|cats|dogs|transplantation))",
//                        Pattern.CASE_INSENSITIVE)),
//                new PatternIndexPair(10, Pattern.compile(
//                        "([\\w']+?( and | plus | ?(\\+|-|/|[^\\x20-\\x7E]) ?))*[\\w']+?(-|[^\\x20-\\x7E]| ){1,2}(treated\\W|grafted\\W|transplanted\\W|(un)?trained\\W)((control |sham )?((injury )?(only )?))?(group|transplantation|animals|mice|rats|cats|dogs)",
//                        Pattern.CASE_INSENSITIVE)),
//                new PatternIndexPair(11, Pattern.compile(
//                        "((control |sham )?((injury )?(only )?))?(group|animals|mice|rats|cats|dogs) that were (treated|grafted|transplanted|(un)?trained) with.+? ",
//                        Pattern.CASE_INSENSITIVE)),
//                new PatternIndexPair(12,
//                        Pattern.compile("([\\w']+?( with | and | plus | ?(\\+|-|/) ?))*[\\w']+? ?treatment")),
//                new PatternIndexPair(13, Pattern.compile(
//                        "((control|sham) ((injury )?(only )?))(treatment|grafting|transplantation|training|operation)")),
//
//        };

    }

    // Diese Funktion zum Patternvergleichen benutzen. Erstellt Liste mit Annotationen,
    // die dann mit Liste von Goldannotationen verglichen werden sollen

    public static List<DocumentLinkedAnnotation> extractGroupNamesWithPattern(Instance instance) {
        List<DocumentLinkedAnnotation> anns = new ArrayList<>();
        Set<String> distinct = new HashSet<>();
        for (CollectExpGroupNames.PatternIndexPair p : CollectExpGroupNames.pattern) {
            Matcher m = p.pattern.matcher(instance.getDocument().documentContent);
            while (m.find()) {
                for (Integer group : p.groups) {
                    DocumentLinkedAnnotation annotation;
                    try {
                        String groupName = m.group(group);
                        if (groupName.length() > 80)
                            continue;

                        if (CollectExpGroupNames.STOP_TERM_LIST.contains(groupName))
                            continue;
                        annotation = AnnotationBuilder.toAnnotation(instance.getDocument(), SCIOEntityTypes.groupName,
                                groupName.substring(groupName.lastIndexOf(" ")), m.start(group));
                    } catch (Exception e) {
                        annotation = null;
                    }
                    if (annotation != null)
                        anns.add(annotation);
                }
            }
        }
        return filter(instance, anns);
    }

    public static List<DocumentLinkedAnnotation> extractOrganismModelWithPattern(Instance instance) {
        List<DocumentLinkedAnnotation> anns = new ArrayList<>();
        Set<String> distinct = new HashSet<>();
        for (CollectExpGroupNames.PatternIndexPair p : CollectExpGroupNames.pattern) {
            Matcher m = p.pattern.matcher(instance.getDocument().documentContent);
            while (m.find()) {
                for (Integer group : p.groups) {
                    DocumentLinkedAnnotation annotation;
                    try {
                        String groupName = m.group(group);
                        if (groupName.length() > 80)
                            continue;

                        if (CollectExpGroupNames.STOP_TERM_LIST.contains(groupName))
                            continue;
                        annotation = AnnotationBuilder.toAnnotation(instance.getDocument(), SCIOEntityTypes.groupName,
                                groupName.substring(groupName.lastIndexOf(" ")), m.start(group));
                    } catch (Exception e) {
                        annotation = null;
                    }
                    if (annotation != null)
                        anns.add(annotation);
                }
            }
        }
        return filter(instance, anns);
    }

    public static List<DocumentLinkedAnnotation> filter(Instance instance, List<DocumentLinkedAnnotation> groupNames) {
        AutomatedSectionification sectionification = AutomatedSectionification.getInstance(instance);

        groupNames.removeIf(groupName -> sectionification.getSection(groupName) != AutomatedSectionification.ESection.RESULTS
                && sectionification.getSection(groupName) != AutomatedSectionification.ESection.ABSTRACT
                && sectionification.getSection(groupName) != AutomatedSectionification.ESection.METHODS);

        return groupNames;
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
