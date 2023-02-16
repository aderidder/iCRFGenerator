package icrfgenerator.settings.runsettings;

import icrfgenerator.codebook.CodebookManager;
import icrfgenerator.settings.LanguageHelper;
import icrfgenerator.utils.KeyUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * keep track of selected codebooks, datasets and languages
 */
public class SelectedCodebooksTracker {
    private final CodebookManager codebookManager = CodebookManager.getInstance();
    private final List<String> selectedCodebooks = new ArrayList<>();
    private final Map<String, List<String>> selectedDatasetIds = new HashMap<>();

    private String mainSimpleLanguage = "";
    private final List<String> otherSimpleLanguages = new ArrayList<>();

    // used to return all identifiers for a language, allowing us to create CRFs per language
    private Map<String, List<String>> simpleLanguageBasedIdentifiers;
    private List<String> allSimpleLanguages;

    SelectedCodebooksTracker() {
    }

    /**
     * returns whether any codebooks is selected
     * @return true/false
     */
    boolean anyCodebookSelected() {
        return selectedCodebooks.size() > 0;
    }

    /**
     * returns whether any dataset is selected
     * @return true/false
     */
    boolean anyDatasetSelected() {
        for (List<String> datasetIds : selectedDatasetIds.values()) {
            if (datasetIds.size() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * create a list with all simple languages
     */
    private void determineAllSimpleLanguages() {
        allSimpleLanguages = new ArrayList<>(otherSimpleLanguages);
        if (!allSimpleLanguages.contains(mainSimpleLanguage)) {
            allSimpleLanguages.add(mainSimpleLanguage);
        }
    }

    /**
     * creates a map with
     * codebookId, datasetId and mainSimpleLanguage --> {codebookId, datasetId, allLanguages}
     * so e.g.
     * "Basisgegevenssset Zorg 2017_2.16.840.1.113883.2.4.3.11.60.42.1.1_en"
     * -->
     * {
     *  "Basisgegevenssset Zorg 2017_2.16.840.1.113883.2.4.3.11.60.42.1.1_en",
     *  "Basisgegevenssset Zorg 2017_2.16.840.1.113883.2.4.3.11.60.42.1.1_nl"
     *  }
     * These can be used to quickly select items for all the languages
     */
    void determineLanguageBasedIdentifiers() {
        determineAllSimpleLanguages();
        simpleLanguageBasedIdentifiers = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : selectedDatasetIds.entrySet()) {
            for (String datasetId : entry.getValue()) {
                String codebookId = entry.getKey();
                // create a key based on codebookId, datasetId and mainSimpleLanguage
                // this becomes something like "Basisgegevenssset Zorg 2017_2.16.840.1.113883.2.4.3.11.60.42.1.1_en"
                String mainId = KeyUtils.getSimpleLanguageKey(codebookId, datasetId, mainSimpleLanguage);
                List<String> idList = new ArrayList<>();
                simpleLanguageBasedIdentifiers.put(mainId, idList);
                for (String language : allSimpleLanguages) {
                    String languageId = KeyUtils.getSimpleLanguageKey(codebookId, datasetId, language);
                    idList.add(languageId);
                }
            }
        }
    }

    /**
     * the key is something like "Basisgegevenssset Zorg 2017_2.16.840.1.113883.2.4.3.11.60.42.1.1_en"
     * the map maps this to a list with "..._en" , "..._nl", etc
     * these identifiers can be used to ensure the item selection in the codebooks in all the selected languages
     * @param key key
     * @return list with keys for all selected languages for the key
     */
    List<String> getAllSimpleLanguageKeysForMainKey(String key) {
        return simpleLanguageBasedIdentifiers.get(key);
    }

    /**
     * returns all the codebook_datasetId_language keys for a language
     * this way we can create a CRF for a single language, based on all the codebooks/datasets
     * @param simpleLanguage the language
     * @return key list
     */
    List<String> getAllKeysForSimpleLanguage(String simpleLanguage) {
        List<String> keys = new ArrayList<>();
        for (String codebook : selectedCodebooks) {
            List<String> datasetIdList = selectedDatasetIds.get(codebook);
            for (String datasetId : datasetIdList) {
                keys.add(KeyUtils.getSimpleLanguageKey(codebook, datasetId, simpleLanguage));
            }
        }
        return keys;
    }

    /**
     * list with all selected languages (main language + other languages)
     * @return list with all selected languages
     */
    List<String> getAllSimpleLanguages() {
        return allSimpleLanguages;
    }

    // page 1
    /**
     * select a codebook
     * @param codebook the codebook
     */
    void addCodebook(String codebook) {
        selectedCodebooks.add(codebook);
        selectedDatasetIds.put(codebook, new ArrayList<>());
    }

    /**
     * deselect a codebook
     * @param codebook the codebook
     */
    void removeCodebook(String codebook) {
        selectedCodebooks.remove(codebook);
        selectedDatasetIds.remove(codebook);
    }

    /**
     * get selected codebooks
     * @return list with selected codebooks
     */
    List<String> getSelectedCodebooks() {
        return selectedCodebooks;
    }

    // page 2
    /**
     * select a codebook's dataset
     * @param codebook          the codebook
     * @param codebookDatasetId the dataset identifier
     */
    void addCodebookDatasetId(String codebook, String codebookDatasetId) {
        List<String> datasetIds = selectedDatasetIds.get(codebook);
        if (!datasetIds.contains(codebookDatasetId)) {
            datasetIds.add(codebookDatasetId);
        }
    }

    /**
     * deselect a codebook's dataset
     *
     * @param codebook          the codebook
     * @param codebookDatasetId the dataset identifier
     */
    void removeCodebookDatasetId(String codebook, String codebookDatasetId) {
        selectedDatasetIds.get(codebook).remove(codebookDatasetId);
    }

    // page 3
    /**
     * set the main language
     * @param mainSimpleLanguage the main language
     */
    void setMainSimpleLanguage(String mainSimpleLanguage) {
        this.mainSimpleLanguage = mainSimpleLanguage;
    }

    /**
     * set other languages
     * @param otherLanguages list with other languages
     */
    void setOtherSimpleLanguage(List<String> otherLanguages) {
        otherSimpleLanguages.clear();
        otherSimpleLanguages.addAll(otherLanguages);
    }

    /**
     * add an "other" language
     * @param otherLanguage the other language
     */
    void addOtherSimpleLanguage(String otherLanguage) {
        if (!otherSimpleLanguages.contains(otherLanguage)) {
            otherSimpleLanguages.add(otherLanguage);
        }
    }

    /**
     * remote an "other" language
     * @param otherLanguage the other language
     */
    void removeOtherSimpleLanguage(String otherLanguage) {
        otherSimpleLanguages.remove(otherLanguage);
    }

    /**
     * get the main language
     * @return the main language
     */
    String getMainSimpleLanguage() {
        return mainSimpleLanguage;
    }

    /**
     * get a list with the other languages
     * @return the other languages
     */
    List<String> getOtherSimpleLanguages() {
        return otherSimpleLanguages;
    }

    /**
     * returns a list with overlapping languages for the selected codebooks/datasets
     * @return list with the overlapping languages
     */
    List<String> getOverlappingSimpleLanguages() {
        List<String> overlappingLanguagesList = null;
        if (anyDatasetSelected()) {
            for (Map.Entry<String, List<String>> entry : selectedDatasetIds.entrySet()) {
                for (String datasetId : entry.getValue()) {
                    List<String> datasetLanguages = codebookManager.getDatasetSimpleLanguages(entry.getKey(), datasetId);
                    if (overlappingLanguagesList == null) {
                        overlappingLanguagesList = datasetLanguages;
                    } else {
                        overlappingLanguagesList = overlappingLanguagesList.stream().filter(datasetLanguages::contains).collect(Collectors.toList());
                    }
                }
            }
        } else {
            overlappingLanguagesList = new ArrayList<>();
        }
        return overlappingLanguagesList;
    }

    /**
     * fetches a (tree)map with the overlapping languages. Key descriptive name, Value simple name
     * @return map descriptive name --> simple name
     */
    Map<String, String> getOverlappingDescriptiveToSimpleLanguageMap(){
        Map<String, String> treeMap = new TreeMap<>();
        List<String> overlappingLanguages = getOverlappingSimpleLanguages();
        overlappingLanguages.forEach(e->treeMap.put(LanguageHelper.getDescriptiveName(e), e));
        return treeMap;
    }

    /**
     * returns the selected dataset identifiers for a codebook
     * used e.g. when determining which datasets were previously selected, as well as when building some of the identifiers
     * @param codebook the codebooks
     * @return list of the codebook's selected datasetIds
     */
    List<String> getCodebookDatasetIds(String codebook) {
        if (selectedDatasetIds.containsKey(codebook)) {
            return selectedDatasetIds.get(codebook);
        }
        return new ArrayList<>();
    }

    /**
     * returns a list with all the identifiers for the main language
     * Format: codebookName_datasetId_mainLanguage
     * @return a list with all the identifiers for the main language
     */
    List<String> getMainSimpleLanguageIds() {
        List<String> idList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : selectedDatasetIds.entrySet()) {
            for (String datasetId : entry.getValue()) {
                idList.add(KeyUtils.getSimpleLanguageKey(entry.getKey(), datasetId, mainSimpleLanguage));
            }
        }
        return idList;
    }

    // Only used in a print statement, so we will probably remove this.
    List<String> getOtherSimpleLanguageIds() {
        List<String> idList = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : selectedDatasetIds.entrySet()) {
            for (String datasetId : entry.getValue()) {
                for (String language : otherSimpleLanguages) {
                    idList.add(KeyUtils.getSimpleLanguageKey(entry.getKey(), datasetId, language));
                }
            }
        }
        return idList;
    }
}
