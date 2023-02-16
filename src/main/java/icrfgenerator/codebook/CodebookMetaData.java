package icrfgenerator.codebook;

import java.util.List;

public interface CodebookMetaData {

    /**
     * returns the list of simple languages (e.g. en, nl) for a dataset identifier
     * @param datasetIdentifier dataset identifier
     * @return list of simple languages
     */
    List<String> getDatasetSimpleLanguages(String datasetIdentifier);

    /**
     * returns the list of extended languages (e.g. en-UK, en-US) for a dataset identifier and simpleLanguage (en)
     * @param datasetIdentifier dataset identifier
     * @param simpleLanguage    simple language
     * @return list of extended languages
     */
    List<String> getDatasetExtendedLanguagesForSimpleLanguage(String datasetIdentifier, String simpleLanguage);

    /**
     * returns the effective date
     * @param datasetIdentifier dataset identifier
     * @return effective date string
     */
    String getDatasetEffectiveDate(String datasetIdentifier);

    /**
     * returns the dataset's name
     * @param datasetIdentifier dataset identifier
     * @return the dataset's name
     */
    String getDatasetName(String datasetIdentifier);

    /**
     * returns the dataset's version
     * @param datasetIdentifier dataset identifier
     * @return the dataset's version
     */
    String getDatasetVersion(String datasetIdentifier);

    /**
     * returns a list with all the dataset identifiers
     * @return a list with all the dataset identifiers
     */
    List<String> getCodebookDatasetIdentifiers();
}
