package tsandmeier.ba;

import com.github.jferard.fastods.*;
import de.hterhors.semanticmr.crf.SemanticParsingCRF;
import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import tsandmeier.ba.NutzloseTemplates.WordsInBetweenTemplateSingle;
import tsandmeier.ba.templates.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class StatisticProvider {
    private static List<Double> alphaList = Arrays.asList(0.001, 0.01, 0.1);

    public static void main(String[] args) throws IOException, FastOdsException, ClassNotFoundException {

//        List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
//        classLoadersList.add(ClasspathHelper.contextClassLoader());
//        classLoadersList.add(ClasspathHelper.staticClassLoader());
//
//        Reflections reflections = new Reflections(new ConfigurationBuilder()
//                .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
//                .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
//                .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("tsandmeier.ba.templates"))));
//
//
//
//        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);

//        List<File> files = getClasses("ba.templates");
//
//        File[] tempFiles= files.get(0).listFiles(file -> {
//            if(file.getName().contains("Template.class")) {
//                return true;
//            }
//            return false;
//        });

//        String name = tempFiles[0].getAbsoluteFile().getName().split("\\.")[0];

//        ImmutableSet<ClassPath> set = ClassLoader.getTopLevelClasses();

        final List<String> featureTemplates = Arrays.asList(AMFLTemplate.class.getSimpleName(), BigramTemplate.class.getSimpleName(),
                BMFLTemplate.class.getSimpleName(), BracketsTemplate.class.getSimpleName(), HMTemplate.class.getSimpleName(), IdentityTemplate.class.getSimpleName(),
                MentionsInSentenceTemplate.class.getSimpleName(), ML12Template.class.getSimpleName(), NormalizedAgeTemplate.class.getSimpleName(),
                NormalizedWeightTemplate.class.getSimpleName(),
                NumberMBTemplate.class.getSimpleName(), NumberWBTemplate.class.getSimpleName(), OverlappingTemplate.class.getSimpleName(), RootTypeTemplate.class.getSimpleName(),
                SimilarWordsTemplate.class.getSimpleName(),
                StartsWithCapitalTemplate.class.getSimpleName(), WBOTemplate.class.getSimpleName(), WBTemplate.class.getSimpleName(),
                WMTemplate.class.getSimpleName(), WordCountTemplate.class.getSimpleName(), WordsInBetweenTemplateSingle.class.getSimpleName());


//        final List<?> featureTemplates = Arrays.asList(AMFLTemplate.class, AvgNumberTemplate.class,
//                BigramTemplate.class, BMFLTemplate.class, BracketsTemplate.class, HMTemplate.class,
//                MentionsInSentenceTemplate.class, ML12Template.class, NumberMBTemplate.class,
//                NumberWBTemplate.class, StartsWithCapitalTemplate.class, WBFTemplate.class, WBLTemplate.class,
//                WBNULLTemplate.class, WBOTemplate.class, WeightBetweenTemplate.class, WMTemplate.class,
//                WMTemplate.class, WordsInBetweenTemplateSingle.class);

//        final List<AbstractFeatureTemplate> featureTemplates = Arrays.asList(new AMFLTemplate(), new AvgNumberTemplate(), new BigramTemplate(false),
//                new BMFLTemplate(), new BracketsTemplate(), new HMTemplate(false), new MentionsInSentenceTemplate(), new ML12Template(), new NormalizedWeightTemplate(),
//                new NumberMBTemplate(), new NumberWBTemplate(), new StartsWithCapitalTemplate(), new WBFTemplate(), new WBLTemplate(), new WBNULLTemplate(),
//                new WBOTemplate(), new WeightBetweenTemplate(), new WMTemplate(), new WordsInBetweenTemplateSingle());


//        usedTemplates.add(featureTemplates.get(8));
//        usedTemplates.add(featureTemplates.get(6));


        final OdsFactory odsFactory = OdsFactory.create(Logger.getLogger("example"), Locale.US);
        final AnonymousOdsFileWriter writer = odsFactory.createWriter();
        final OdsDocument document = writer.document();
        final Table table = document.addTable("test");

//        NamedEntityRecognitionAndLinkingExample nerla = new NamedEntityRecognitionAndLinkingExample();
        NamedEntityRecognitionAndLinkingInjury nerla = new NamedEntityRecognitionAndLinkingInjury();

        for (int y = 0; y <= 7; y++) {


            final TableRow row = table.nextRow();
            final TableCellWalker cell = row.getWalker();

            if (y == 0) {
                for (int x = 0; x < featureTemplates.size(); x++) {
                    cell.setStringValue(featureTemplates.get(x));
//                    cell.setStringValue(tempFiles[x].getAbsoluteFile().getName().split("\\.")[0]);
                    cell.next();
                }
                cell.setStringValue("F1-Score");
                cell.next();
                cell.setStringValue("Total Time");
                cell.next();
                cell.setStringValue("Training Time");
                cell.next();
                cell.setStringValue("Testing Time");
                cell.next();
                cell.setStringValue("Alpha");
            }

            if (y >= 1) {
                System.out.println("*****************DURCHGANG NUMMER " + 1 + " *************************");

                for (double alpha : alphaList) {

                    System.out.println("***********NEUER ALPHAWERT: " + alpha + " ****************************");

                    nerla.startProcedure(y, alpha);
                    IEvaluatable.Score mean = nerla.getMean();
                    SemanticParsingCRF crf = nerla.getCRF();
                    SemanticParsingCRF.CRFStatistics crfStats = crf.getTrainingStatistics();
                    long trainDuration = crfStats.getTotalDuration();
                    long testDuration = nerla.getCRF().getTestStatistics().getTotalDuration();
                    long totalDuration = trainDuration + testDuration;

                    double f1 = mean.getF1();

                    List<AbstractFeatureTemplate> usedTemplates = nerla.getFeatureTemplates();


                    for (int x = 0; x < featureTemplates.size(); x++) {
                        boolean isUsed = false;
                        for (AbstractFeatureTemplate usedtemplate : usedTemplates) {
                            if (usedtemplate.getClass().getSimpleName().equals(featureTemplates.get(x))) {
                                isUsed = true;
                            }
                        }
                        if (isUsed) {
                            cell.setStringValue("X");
                        } else {
                            cell.setStringValue("");
                        }

                        cell.next();
                    }

                    cell.setStringValue(Double.toString(f1));
                    cell.next();
                    cell.setStringValue(Long.toString(totalDuration));
                    cell.next();
                    cell.setStringValue(Long.toString(trainDuration));
                    cell.next();
                    cell.setStringValue(Long.toString(testDuration));
                    cell.next();
                    cell.setStringValue(Double.toString(alpha));
                }
            }
//        }


//        final TableCellStyle style = TableCellStyle.builder("green cell style").backgroundColor().build();
//        for (int y = 0; y < 50; y++) {
//            final TableRow row = table.nextRow();
//            final TableCellWalker cell = row.getWalker();
//            for (int x = 0; x < 5; x++) {
//                cell.setFloatValue(x*y);
////                cell.setStyle(style);
//                cell.next();
//            }
        }

        writer.saveAs(new File("statistics", "InjuryStatistics.ods"));

//    private static List findClasses(String packageName) throws ClassNotFoundException {
//
//        List classes = new ArrayList();
//
//        File directory =
//
//        for (File file : files) {
//            if(file.getName().endsWith(".class")) {
//                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
//            }
//        }
//        return classes;
//    }
    }

    private static List<File> getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getFile().contains("Template"))
                dirs.add(new File(resource.getFile()));
        }
        return dirs;
    }
}
