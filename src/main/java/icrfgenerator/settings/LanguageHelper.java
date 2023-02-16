package icrfgenerator.settings;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.HashMap;
import java.util.Map;

/**
 * keeps a mapping of simple language names and complicated language names
 * e.g.
 * Extended Name    Simple Name Descriptive Name
 * en-gb	        en	        English (United Kingdom)
 * en-nz	        en	        English (New Zealand)
 * en-us	        en	        English (United States)
 * This is necessary since we just want to call this "English" to be able to find overlapping languages between
 * codebooks. OpenEHR simply uses English, whereas ART-DECOR sometimes uses en-us.
 * So basically this class helps us to harmonise the languages
 */
public class LanguageHelper {
    private static final Map<String, String> extendedNameToDescriptiveNameMap = new HashMap<>();
    private static final Map<String, String> extendedNameToSimpleNameMap = new HashMap<>();

    /**
     * parse the sheet with the language mappings
     * @param sheet name of the sheet
     */
    public static void readLanguageSheet(Sheet sheet) {
        int nrRows = sheet.getPhysicalNumberOfRows();
        for (int i = 1; i <= nrRows; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                String extendedName = row.getCell(0).getStringCellValue().toLowerCase();
                String simpleName = row.getCell(1).getStringCellValue().toLowerCase();
                String descriptiveName = row.getCell(2).getStringCellValue();
                extendedNameToDescriptiveNameMap.put(extendedName, descriptiveName);
                extendedNameToSimpleNameMap.put(extendedName, simpleName);
            }
        }
    }

    /**
     * returns the Descriptive Name of a language based on the Extended Name
     * @param extendedName extended name of the language, e.g. en-gb
     * @return descriptive name of the language, e.g. English (United Kingdom)
     */
    public static String getDescriptiveName(String extendedName){
        return extendedNameToDescriptiveNameMap.get(extendedName.toLowerCase());
    }

    /**
     * returns the Simple Name of a language based on the Extended Name
     * @param extendedName extended name of the language, e.g. en-gb
     * @return simple name of the language, e.g. en
     */
    public static String getSimpleName(String extendedName){
        return extendedNameToSimpleNameMap.get(extendedName.toLowerCase());
    }


}
