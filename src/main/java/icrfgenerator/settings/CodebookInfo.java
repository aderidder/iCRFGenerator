package icrfgenerator.settings;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.*;
import java.util.stream.Collectors;

/**
 * information about the codebooks, taken from the codebooks.xlsx file
 */
public class CodebookInfo {
    private static final Map<String, Source> codebookNameToSourceMap = new TreeMap<>();
    private static final Map<String, List<String>> tagToCodebookNameMap = new HashMap<>();
    private static final List<String> lowQualityCodebookLanguageCombinations = new ArrayList<>();

    /**
     * read the codebooks sheet. Required:
     * - type
     * - name
     * - prefix
     * - server
     * - whether a group itself should be considered an item
     * Optional:
     * - language to ignore (e.g. en-US). In case of multiple languages, these are comma-separated
     */
    static void readCodebooks(Sheet sheet) {
        int nrRows = sheet.getPhysicalNumberOfRows();
        for (int i = 1; i <= nrRows; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                String type = row.getCell(0).getStringCellValue();
                String name = row.getCell(1).getStringCellValue();
                String prefix = row.getCell(2).getStringCellValue();
                String server = row.getCell(3).getStringCellValue();
                boolean group_is_item = row.getCell(4).getBooleanCellValue();
                parseTags(name, row.getCell(5).getStringCellValue());
                codebookNameToSourceMap.put(name, new Source(name, type, prefix, server, group_is_item));
                if(row.getPhysicalNumberOfCells()>6) {
                    String skip_language = row.getCell(6).getStringCellValue();
                    if (!skip_language.equalsIgnoreCase("")) {
                        String[] skipLanguages = skip_language.split(",");
                        for (String skipLanguage : skipLanguages) {
                            lowQualityCodebookLanguageCombinations.add(name + "_" + skipLanguage.trim());
                        }
                    }
                }
            }
        }
    }

    /**
     * splits the tags found in the codebook sheet and stores them
     * @param name codebook name
     * @param tags comma sepratated string with tags
     */
    private static void parseTags(String name, String tags){
        String[] tagArray = tags.split("\\s*,\\s*");
        for (String tag:tagArray){
            if(!tagToCodebookNameMap.containsKey(tag)){
                tagToCodebookNameMap.put(tag, new ArrayList<>());
            }
            tagToCodebookNameMap.get(tag).add(name);
        }

    }

    /**
     * returns codebooks from the specified sources
     * @param sources list with source names
     * @return list with codebooks from the specified sources
     */
    static List<String> getCodebookNamesFilteredBySource(List<String> sources){
        return codebookNameToSourceMap.values().parallelStream().filter(t->sources.contains(t.getSource())).map(Source::getName).toList();
    }

    /**
     * returns codebooks that have all the tags specified (AND)
     * @param tags list with tags
     * @return list with codebooks
     */
    static List<String> getCodebookNamesFilteredByTagAll(List<String> tags){
        Set<String> list1 = new HashSet<>(tagToCodebookNameMap.get(tags.get(0)));
        if(tags.size()>1){
            for(int i=1; i<tags.size(); i++){
                List<String> list2 = tagToCodebookNameMap.get(tags.get(i));
                list1 = intersectList(list1, list2);
                if(list1.size()==0) break;
            }
        }
        return new ArrayList<>(list1);
    }

    /**
     * determine the intersection between the collections
     * @param list1 collection1
     * @param list2 collection2
     * @return intersection collection
     */
    static Set<String> intersectList(Set<String> list1, List<String> list2){
        return list1.stream().filter(list2::contains).collect(Collectors.toSet());
    }

    /**
     * returns codebooks that meet any of the tags (OR)
     * @param tags list with tags
     * @return list with codebooks
     */
    static List<String> getCodebookNamesFilteredByTagAny(List<String> tags){
        Set<String> codebookNames = new TreeSet<>();
        for(String tag : tags){
            codebookNames.addAll(tagToCodebookNameMap.get(tag));
        }
        return new ArrayList<>(codebookNames);
    }

    /**
     * returns all the available tags
     * @return list with all tags
     */
    static List<String> getTags(){
        return new ArrayList<>(tagToCodebookNameMap.keySet());
    }

    /**
     * returns a list of the known codebook names
     * @return a list of the known codebook names
     */
    static List<String> getCodebookNames() {
        return new ArrayList<>(codebookNameToSourceMap.keySet());
    }

    /**
     * returns a codebook's source
     * @param codebookName name of the codebook
     * @return the codebook's source
     */
    static String getSource(String codebookName){
        return codebookNameToSourceMap.get(codebookName).getSource();
    }

    /**
     * returns the server on which the codebook is stored
     * @param codebookName name of the codebook
     * @return server on which the codebook is stored
     */
    static String getServer(String codebookName){
        return codebookNameToSourceMap.get(codebookName).getServer();
    }

    /**
     * returns whether a codebook + language is marked as being of bad quality
     * @param codebookName name of the codebook
     * @param language language of the codebook
     * @return true/false
     */
    static boolean isLowQualityCodebookLanguage(String codebookName, String language){
        return lowQualityCodebookLanguageCombinations.contains(codebookName+"_"+language);
    }

    /**
     * get a prefix of a codebook, which is necessary for the art-decor calls
     * @param codebookName    name of the codebook
     * @return  prefix of the codebook
     */
    static String getCodebookPrefix(String codebookName){
        return codebookNameToSourceMap.get(codebookName).getPrefix();
    }

    /**
     * some codebooks in ART-DECOR have a group node which itself is also an item
     * this functions returns whether, for this codebook, groups should also be considered items.
     * @param codebookName name of the codebook
     * @return whether group should be considered an item
     */
    static boolean groupIsAnItem(String codebookName){
        return codebookNameToSourceMap.get(codebookName).isGroupIsAnItem();
    }

    /**
     * Definition of a codebook; stores its prefix and whether a non-leaf node should be considered an item or not
     */
    static class Source{
        private final String name;
        private final String source;
        private final String server;
        private final String prefix;
        private final boolean groupIsAnItem;

        Source(String name, String source, String prefix, String server, boolean groupIsAnItem){
            this.name = name;
            this.source = source;
            this.prefix = prefix;
            this.groupIsAnItem = groupIsAnItem;
            this.server = server;
        }
        String getName(){
            return name;
        }

        String getServer(){
            return server;
        }

        String getPrefix() {
            return prefix;
        }

        boolean isGroupIsAnItem() {
            return groupIsAnItem;
        }

        String getSource(){
            return source;
        }
    }

}
