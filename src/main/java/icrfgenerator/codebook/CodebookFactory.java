package icrfgenerator.codebook;


import icrfgenerator.codebook.artdecor.CodebookArtDecor;
import icrfgenerator.codebook.openehr.CodebookOpenEHR;
import icrfgenerator.settings.GlobalSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CodebookFactory {
    private static final Logger logger = LogManager.getLogger(CodebookFactory.class.getName());

    public static Codebook generateCodebook(String datasetId, String extendedLanguage, String codebookName){
        String source = GlobalSettings.getSource(codebookName);
        switch (source) {
            case "ARTDECOR" -> {
                logger.info("ART-DECOR Codebook");
                return new CodebookArtDecor(datasetId, extendedLanguage, codebookName);
            }
            case "OPENEHR" -> {
                logger.info("OPENEHR Codebook");
                return new CodebookOpenEHR(datasetId, extendedLanguage, codebookName);
            }
            default -> System.err.println("Unknown codebook type: " + source);
        }
        return null;
    }
}
