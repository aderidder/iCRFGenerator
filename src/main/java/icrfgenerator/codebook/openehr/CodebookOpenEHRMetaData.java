package icrfgenerator.codebook.openehr;

import icrfgenerator.codebook.CodebookMetaDataDefault;
import icrfgenerator.settings.GlobalSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * meta data for a codebook, e.g. name, languages, dataset versions, etc.
 */
public class CodebookOpenEHRMetaData extends CodebookMetaDataDefault {
    private static final Logger logger = LogManager.getLogger(CodebookMetaDataDefault.class.getName());
    private static final String dateFormat = "yyyy-MM-dd";

    public CodebookOpenEHRMetaData(String codebookName){
        super(codebookName, dateFormat);
        readOpenEHR();
        setMetaData();
    }

    /**
     * parse the openEHR codebook
     */
    private void readOpenEHR(){
        OpenEHRCodebookRepository.addCodebook(codebookName);
    }

    /**
     * set the codebook's metadata
     */
    private void setMetaData(){
        String id = GlobalSettings.getCodebookPrefix(codebookName);
        String name = codebookName;
        List<String> languages = OpenEHRCodebookRepository.getLanguages(codebookName);
        String version = OpenEHRCodebookRepository.getVersion(codebookName);
        // there does not seem to be a date
        String date = "";
        DatasetMetaData datasetMetaData = new DatasetMetaData(id, version, name, date, languages);
        idToDatasetMetaDataMap.put(id, datasetMetaData);
    }

}
