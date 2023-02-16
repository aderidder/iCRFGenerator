package icrfgenerator.codebook;

import icrfgenerator.settings.LanguageHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

abstract public class CodebookMetaDataDefault implements CodebookMetaData {
    protected Map<String, DatasetMetaData> idToDatasetMetaDataMap = new HashMap<>();
    protected String codebookName;
    private final SimpleDateFormat dateFormatter1;

    public CodebookMetaDataDefault(String codebookName, String dateFormat){
        this.dateFormatter1 = new SimpleDateFormat(dateFormat, Locale.ENGLISH);
        this.codebookName = codebookName;
    }

    /**
     * get a (sorted) list of the dataset identifiers present for this codebook
     * @return a sorted list with dataset identifiers
     */
    @Override
    public List<String> getCodebookDatasetIdentifiers(){
        List<DatasetMetaData> datasetMetaDataList = new ArrayList<>(idToDatasetMetaDataMap.values());
        Collections.sort(datasetMetaDataList);
        return datasetMetaDataList.stream().map(t->t.getId()).collect(Collectors.toList());
    }

    /**
     * returns the name of the dataset
     * @param datasetIdentifier the id of the dataset
     * @return the name of the dataset
     */
    @Override
    public String getDatasetName(String datasetIdentifier){
        return idToDatasetMetaDataMap.get(datasetIdentifier).getName();
    }

    /**
     * returns the version of the dataset
     * @param datasetIdentifier the id of the dataset
     * @return the version of the dataset
     */
    @Override
    public String getDatasetVersion(String datasetIdentifier){
        return idToDatasetMetaDataMap.get(datasetIdentifier).getVersion();
    }

    /**
     * returns the effective date of the dataset
     * @param datasetIdentifier the id of the dataset
     * @return the effective date of the dataset
     */
    @Override
    public String getDatasetEffectiveDate(String datasetIdentifier){
        return idToDatasetMetaDataMap.get(datasetIdentifier).getEffectiveDate();
    }

    /**
     * returns a list with all available languages for a codebook version
     * @return a list with all available languages for a codebook version
     */
    @Override
    public List<String> getDatasetSimpleLanguages(String datasetIdentifier){
        return idToDatasetMetaDataMap.get(datasetIdentifier).getSimpleLanguages();
    }

    @Override
    public List<String> getDatasetExtendedLanguagesForSimpleLanguage(String datasetIdentifier, String simpleLanguage){
        return idToDatasetMetaDataMap.get(datasetIdentifier).getExtendedLanguagesForSimpleLanguage(simpleLanguage);
    }

    /**
     * class for keeping track of metadata
     * there should be exactly 1 for each id with status non-deprecated (final / draft)
     */
    public class DatasetMetaData implements Comparable<DatasetMetaData> {
        private final SimpleDateFormat dateFormatter2 = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);

        private final String version;
        private final String id;
        private final String name;
        private final List<String> extendedLanguages;
        private final Map<String, List<String>> simpleToExtendedLanguageMap = new HashMap<>();

        private Date effectiveDate;

        public DatasetMetaData(String id, String version, String name, String effectiveDate, List<String> extendedLanguages) {
            this.id = id;
            this.version = version;
            this.name = name;

            this.extendedLanguages = extendedLanguages;
            handleLanguages();

            // try to parse the effective date according to the expected format
            try {
                this.effectiveDate = dateFormatter1.parse(effectiveDate);
            } catch (ParseException e) {
                // if that fails, set it to a default date
                try {
                    this.effectiveDate = dateFormatter2.parse("01-Jan-1900");
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void handleLanguages(){
            for(String language: extendedLanguages){
                String simpleLanguage = LanguageHelper.getSimpleName(language);
                if(!simpleToExtendedLanguageMap.containsKey(simpleLanguage)){
                    simpleToExtendedLanguageMap.put(simpleLanguage, new ArrayList<>());
                }
                simpleToExtendedLanguageMap.get(simpleLanguage).add(language);
            }
        }

        List<String> getExtendedLanguagesForSimpleLanguage(String simpleLanguage){
            return simpleToExtendedLanguageMap.get(simpleLanguage);
        }

        /**
         * returns the dataset version
         * @return the dataset version
         */
        String getVersion() {
            return version;
        }

        /**
         * returns the dataset id
         * @return the dataset id
         */
        String getId() {
            return id;
        }

        /**
         * returns the dataset name
         * @return the dataset name
         */
        String getName() {
            return name;
        }

        /**
         * returns the dataset available languages
         * @return the dataset available languages
         */
        List<String> getExtendedLanguages() {
            return extendedLanguages;
        }

        List<String> getSimpleLanguages(){
            return new ArrayList<>(simpleToExtendedLanguageMap.keySet());
        }

        /**
         * returns the dataset effective date
         * @return the dataset effective date
         */
        String getEffectiveDate() {
            return dateFormatter2.format(effectiveDate);
        }

        @Override
        public int compareTo(DatasetMetaData datasetMetaData) {
            if (datasetMetaData.effectiveDate.compareTo(this.effectiveDate) == 0) {
                return this.name.compareTo(datasetMetaData.name);
            } else {
                return datasetMetaData.effectiveDate.compareTo(this.effectiveDate);
            }
        }
    }
}