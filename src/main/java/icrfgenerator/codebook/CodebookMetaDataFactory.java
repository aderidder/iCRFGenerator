package icrfgenerator.codebook;


import icrfgenerator.codebook.artdecor.CodebookArtDecorMetaData;
import icrfgenerator.codebook.openehr.CodebookOpenEHRMetaData;
import icrfgenerator.settings.GlobalSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CodebookMetaDataFactory {
    private static final Logger logger = LogManager.getLogger(CodebookMetaDataFactory.class.getName());

    public static CodebookMetaData getMetaData(String codebookName){
        String source = GlobalSettings.getSource(codebookName);
        switch (source){
            case "ARTDECOR":
                return new CodebookArtDecorMetaData(codebookName);
            case "OPENEHR":
                return new CodebookOpenEHRMetaData(codebookName);
            default: logger.error("Unknown codebook type: "+source);
        }
        return null;
    }
}
