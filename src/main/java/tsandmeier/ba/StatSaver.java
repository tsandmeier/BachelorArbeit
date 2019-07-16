package tsandmeier.ba;

import com.github.jferard.fastods.*;
import com.github.jferard.fastods.util.FileOpenResult;
import de.hterhors.semanticmr.crf.templates.AbstractFeatureTemplate;
import org.jopendocument.dom.OOUtils;
import tsandmeier.ba.templates.*;

import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;

public class StatSaver {
    private static List<Double> alphaList = Arrays.asList(0.001, 0.01, 0.1);

    final static List<String> featureTemplates = Arrays.asList(AMFLTemplate.class.getSimpleName(), BigramTemplate.class.getSimpleName(),
            BMFLTemplate.class.getSimpleName(), BracketsTemplate.class.getSimpleName(), HMTemplate.class.getSimpleName(), IdentityTemplate.class.getSimpleName(),
            MentionsInSentenceTemplate.class.getSimpleName(), ML12Template.class.getSimpleName(), NormalizedAgeTemplate.class.getSimpleName(),
            NormalizedWeightTemplate.class.getSimpleName(),
            NumberMBTemplate.class.getSimpleName(), NumberWBTemplate.class.getSimpleName(), OverlappingTemplate.class.getSimpleName(), RootTypeTemplate.class.getSimpleName(),
            SimilarWordsTemplate.class.getSimpleName(),
            StartsWithCapitalTemplate.class.getSimpleName(), WBOTemplate.class.getSimpleName(), WBTemplate.class.getSimpleName(),
            WMTemplate.class.getSimpleName(), WordCountTemplate.class.getSimpleName(), WordsInBetweenTemplate.class.getSimpleName());

    static String[] temps;

//    public static void main(String[] args) throws IOException, FastOdsException, ClassNotFoundException {
////        readODS(new File("generated_files/readme_example.ods"));
//        List<AbstractFeatureTemplate> bla = new ArrayList<>();
//        bla.add(new BigramTemplate(false));
//        bla.add(new WordsInBetweenTemplate());
//        bla.add(new AMFLTemplate(false));
//        bla.add(new WBOTemplate());
//        bla.add(new SimilarWordsTemplate());
//        bla.add(new NumberWBTemplate());
//        addToSpreadsheet("statistics/testest.ods",bla, 0.58, 678326, 4387493, 84934, 0.001);
//        List<AbstractFeatureTemplate> bla2 = new ArrayList<>();
//        bla2.add(new StartsWithCapitalTemplate());
//        bla2.add(new WBTemplate());
//        bla2.add(new ML12Template());
//        bla2.add(new WordCountTemplate());
//        bla2.add(new OverlappingTemplate(false));
//        addToSpreadsheet("statistics/testest.ods",bla2,0.55,213,43154,545, 0.01);
//
//    }


    public static File createSpreadsheet(String filePath) throws IOException {
        // Create the data to save.
        temps = featureTemplates.toArray(new String[featureTemplates.size()]);

        String[] values = {"F1", "TotalTime", "TrainTime", "TestTime", "Alpha"};


        String[] both = Arrays.copyOf(temps, temps.length + values.length);
        System.arraycopy(values, 0, both, temps.length, values.length);

        TableModel model = new DefaultTableModel();

        // Save the data to an ODS file and open it.
        final File file = new File(filePath);
        SpreadSheet spreadSheet = SpreadSheet.createEmpty(model);

        Sheet sheet = spreadSheet.getSheet(0);
//        sheet.setRowCount(sheet.getRowCount()+1);
        sheet.setColumnCount(both.length);
        for(int x=0; x<sheet.getColumnCount();x++){
            sheet.setValueAt(both[x], x, 0);
        }
        return sheet.getSpreadSheet().saveAs(file);
    }

    public static void addToSpreadsheet(String filePath, List<AbstractFeatureTemplate> usedTemplates, double f1, long totalDuration, long trainDuration,
                                        long testDuration, double alpha) throws IOException {

        File file = new File(filePath);

        if(!file.exists()){
            file = createSpreadsheet(filePath);
            System.out.println("FILE ERSTELLT");
        }

        SpreadSheet createFromFile = SpreadSheet.createFromFile(file);
        Sheet sheet = createFromFile.getSheet(0);
        int newRowIndex = sheet.getRowCount() + 1;
        sheet.setRowCount(newRowIndex);
        int x = 0;

        while (x < featureTemplates.size()) {
            boolean isUsed = false;
            for (AbstractFeatureTemplate usedtemplate : usedTemplates) {
                if (usedtemplate.getClass().getSimpleName().equals(featureTemplates.get(x))) {
                    isUsed = true;
                }
            }
            if (isUsed) {
                sheet.setValueAt("X", x, newRowIndex - 1);
            }

            x++;
        }


        sheet.setValueAt(Double.toString(f1), x, newRowIndex - 1);
        x++;
        sheet.setValueAt(Long.toString(totalDuration), x, newRowIndex - 1);
        x++;
        sheet.setValueAt(Long.toString(trainDuration), x, newRowIndex - 1);
        x++;
        sheet.setValueAt(Long.toString(testDuration), x, newRowIndex - 1);
        x++;
        sheet.setValueAt(Double.toString(alpha), x, newRowIndex - 1);

        File outputFile = new File(filePath);
        sheet.getSpreadSheet().saveAs(outputFile);
    }
}
