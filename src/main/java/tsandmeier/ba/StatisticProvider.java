package tsandmeier.ba;

import com.github.jferard.fastods.*;
import com.github.jferard.fastods.style.TableCellStyle;
import de.hterhors.semanticmr.crf.SemanticParsingCRF;
import de.hterhors.semanticmr.crf.structure.IEvaluatable;
import tsandmeier.ba.templates.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

public class StatisticProvider {
    public static void main(String[] args) throws IOException, FastOdsException {
        NamedEntityRecognitionAndLinkingExample nerla = new NamedEntityRecognitionAndLinkingExample(2);
        SemanticParsingCRF crf = nerla.getCRF();
        IEvaluatable.Score mean = nerla.getMean();
        double f1 = mean.getF1();

        final List<String> templateNames = Arrays.asList(AMFLTemplate.class.toString(), AvgNumberTemplate.class.toString(),
                BigramTemplate.class.toString(), BMFLTemplate.class.toString(), BracketsTemplate.class.toString(), HMTemplate.class.toString(),
                MentionsInSentenceTemplate.class.toString(),ML12Template.class.toString(),NumberMBTemplate.class.toString(),
                NumberWBTemplate.class.toString(),StartsWithCapitalTemplate.class.toString(),WBFTemplate.class.toString(), WBLTemplate.class.toString(),
                WBNULLTemplate.class.toString(), WBOTemplate.class.toString(), WeightBetweenTemplate.class.toString(), WMTemplate.class.toString(),
                WMTemplate.class.toString(), WordsInBetweenTemplate.class.toString());


        final OdsFactory odsFactory = OdsFactory.create(Logger.getLogger("example"), Locale.US);
        final AnonymousOdsFileWriter writer = odsFactory.createWriter();
        final OdsDocument document = writer.document();
        final Table table = document.addTable("test");




        final TableRow row = table.getRow(0);
        final TableCellWalker cell = row.getWalker();
        for(int x = 0; x < templateNames.size(); x++){
            cell.setStringValue(templateNames.get(x));
            cell.next();
        }
        cell.setStringValue("F1-Score");


//        final TableCellStyle style = TableCellStyle.builder("green cell style").backgroundColor().build();
//        for (int y = 0; y < 50; y++) {
//            final TableRow row = table.nextRow();
//            final TableCellWalker cell = row.getWalker();
//            for (int x = 0; x < 5; x++) {
//                cell.setFloatValue(x*y);
////                cell.setStyle(style);
//                cell.next();
//            }
//        }

        writer.saveAs(new File("generated_files", "readme_example.ods"));

    }
}
