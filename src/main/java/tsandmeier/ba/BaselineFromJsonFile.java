package tsandmeier.ba;

import de.hterhors.semanticmr.corpus.InstanceProvider;
import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import de.hterhors.semanticmr.crf.structure.annotations.AbstractAnnotation;
import de.hterhors.semanticmr.crf.structure.annotations.DocumentLinkedAnnotation;
import de.hterhors.semanticmr.crf.variables.Instance;
import de.hterhors.semanticmr.eval.EEvaluationDetail;
import de.hterhors.semanticmr.init.reader.csv.CSVDataStructureReader;
import de.hterhors.semanticmr.init.specifications.SystemScope;
import de.hterhors.semanticmr.json.JSONNerlaReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tsandmeier.ba.evaluator.NerlaEvaluatorPartialOverlap;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaselineFromJsonFile extends AbstractSemReadProject {

    private static Logger log = LogManager.getFormatterLogger("de.hterhors.semanticmr.projects.examples.corpus.nerl.NerlCorpusCreationExample");


    private static EEvaluationDetail EVALUATION_DETAIL = EEvaluationDetail.LITERAL;

    private static String TYPE_OF_TOPIC = "vertebral_area";

    private static String SPECIFICATION_DIRECTORY = "ner/" + TYPE_OF_TOPIC + "/data_structure/";
    private static String INSTANCE_DIRECTORY = "ner/" + TYPE_OF_TOPIC + "/instances/";

    public static void main(String[] args) {
        new BaselineFromJsonFile();
    }


    public BaselineFromJsonFile() {
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

        NerlaEvaluatorPartialOverlap nerl = new NerlaEvaluatorPartialOverlap(EVALUATION_DETAIL);

        InstanceProvider instanceProvider = new InstanceProvider(new File(INSTANCE_DIRECTORY));


        //TODO: Problem ist, dass der Reader einen Ordner mit allen Files braucht, die Files sind jedoch nochmal
        //aufgeteilt nach Dokumenten
        JSONNerlaReader reader = new JSONNerlaReader(new File("src/main/resources/regex_baseline/" + TYPE_OF_TOPIC+"/regex_nerla"));

        Map<Instance, IEvaluatable.Score> testResults = new HashMap<>();

        int counter = 0;

        for (Instance instance : instanceProvider.getInstances()) {
            counter++;
            List<DocumentLinkedAnnotation> predictedAnnotations = reader.getForInstance(instance);

            IEvaluatable.Score score = nerl.prf1(instance.getGoldAnnotations().getAnnotations(), predictedAnnotations);

            testResults.putIfAbsent(instance, score);

            log.info("Nummer " + counter + ": " + instance.getName());
            log.info("********************************************GOLD************************************************");
            for (AbstractAnnotation annotation : instance.getGoldAnnotations().getAnnotations()) {
                log.info(annotation.asInstanceOfDocumentLinkedAnnotation().toPrettyString());
            }
            log.info("********************************************PREDICTED************************************************");
            for (AbstractAnnotation annotation : predictedAnnotations) {
                log.info(annotation.asInstanceOfDocumentLinkedAnnotation().toPrettyString());
            }
            log.info(System.lineSeparator());


//            System.out.println("F1: "+score.getF1()+" Recall: "+score.getRecall()+" Precision: "+score.getPrecision());
            log.info(score.toString());


        }


        IEvaluatable.Score mean = new IEvaluatable.Score();
        for (Map.Entry<Instance, IEvaluatable.Score> res : testResults.entrySet()) {

            mean.add(res.getValue());

        }
        log.info("\n");
        log.info("Mean Score: " + mean);


    }


}
