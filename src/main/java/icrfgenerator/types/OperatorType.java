package icrfgenerator.types;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Types of operators
 * Used to check if a value is e.g. <= 100 and attempts to provide warnings in the appropriate language
 */
public enum OperatorType {
    LT("<", "LT"),
    LTE("<=", "LTE"),
    GT(">", "GT"),
    GTE(">=", "GTE"),
    UNKNOWN("UNK", "UNK"),
    NONE("", "");

    private static final Logger logger = LogManager.getLogger(OperatorType.class.getName());

    private static final Map<String, OperatorType> dropdownLabelToOperatorTypeMap = new HashMap<>();
    private static final Map<String, OperatorType> textLabelToOperatorTypeMap = new HashMap<>();
    private static final Map<OperatorType, OperatorType> flipMap = new HashMap<>();
    private static final List<OperatorType> ltTypes = new ArrayList<>();
    private static final List<OperatorType> gtTypes = new ArrayList<>();
    private static final String defaultLanguage = "en";

    private final String dropdownLabel;
    private final String textLabel;
    private final Map<String, String> languageMessagesMap = new HashMap<>();

    // initialise the static maps and lists
    static {
        for (OperatorType operatorType : OperatorType.values()) {
            dropdownLabelToOperatorTypeMap.put(operatorType.dropdownLabel, operatorType);
            textLabelToOperatorTypeMap.put(operatorType.textLabel, operatorType);
        }
        flipMap.put(GT, LTE); flipMap.put(GTE, LT);
        flipMap.put(LT, GTE); flipMap.put(LTE, GT);
        gtTypes.add(NONE); gtTypes.add(GT); gtTypes.add(GTE);
        ltTypes.add(NONE); ltTypes.add(LT); ltTypes.add(LTE);
    }

    /**
     * returns a list with the "greater than" operator types
     * @return a list with the "greater than" operator types
     */
    public static List<OperatorType> getGTTypes() {
        return gtTypes;
    }

    /**
     * returns a list with the "less than" operator types
     * @return a list with the "less than" operator types
     */
    public static List<OperatorType> getLTTypes() {
        return ltTypes;
    }

    /**
     * Flips the operator, e.g. lte becomes gte.
     * This is used by Castor, since their logic is the other way around
     * @param operatorType operatorType to flip
     * @return flipped operator
     */
    public static OperatorType flipOperator(OperatorType operatorType){
        return flipMap.get(operatorType);
    }

    /**
     * returns the OperatorType for a dropdownLabel
     * @param dropdownLabel dropdownLabel, e.g. <=
     * @return the OperatorType for this label
     */
    public static OperatorType getOperatorTypeByDropdownLabel(String dropdownLabel) {
        return dropdownLabelToOperatorTypeMap.get(dropdownLabel);
    }

    /**
     * returns the OperatorType for a textLabel
     * @param textLabel textLabel, e.g. LTE
     * @return the OperatorType for this label
     */
    public static OperatorType getOperatorTypeByTextLabel(String textLabel) {
        return textLabelToOperatorTypeMap.get(textLabel);
    }

    /**
     * reads the sheet with the languages specific validation messages from the iCRFSettings file's ValidationMessages sheet
     * @param sheet sheet with the language-specific validation messages
     */
    public static void readValidationMessages(Sheet sheet) {
        int nrRows = sheet.getPhysicalNumberOfRows();
        for (int i = 1; i <= nrRows; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                // Rows look like this:
                // Language Check   ErrorMessage
                // en       v<max	Value must be less than
                String language = row.getCell(0).getStringCellValue();
                String check = parseCheck(row.getCell(1).getStringCellValue());
                if (check.equalsIgnoreCase("")) {
                    throw new RuntimeException("There is an issue with a check in the ComparatorLanguagesMessages. Language " + language + " check: " + check);
                }
                String message = row.getCell(2).getStringCellValue();
                OperatorType operatorType = getOperatorTypeByTextLabel(check);
                operatorType.languageMessagesMap.put(language, message);
            }
        }
    }

    /**
     * takes the "check", e.g. v<max, and returns the corresponding textLabel, e.g. LT
     * @param check check entry in the row
     * @return corresponding textLabel
     */
    private static String parseCheck(String check) {
        Pattern p = Pattern.compile("v(.*)m.*");
        Matcher matcher = p.matcher(check);
        if (matcher.matches()) {
            return switch (matcher.group(1)) {
                case "<" -> "LT";
                case "<=" -> "LTE";
                case ">" -> "GT";
                case ">=" -> "GTE";
                default -> "";
            };
        }
        return "";
    }

    /**
     * Enum Constructor for OperatorTypes
     * @param dropdownLabel label as will be used in the dropdowns
     * @param textLabel     textual representation of this label
     */
    OperatorType(String dropdownLabel, String textLabel) {
        this.dropdownLabel = dropdownLabel;
        this.textLabel = textLabel;
    }

    /**
     * returns the dropdownLabel
     * @return the dropdownLabel
     */
    public String getDropdownLabel(){
        return dropdownLabel;
    }

    /**
     * returns the textLabel
     * @return the textLabel
     */
    public String getTextLabel(){
        return textLabel;
    }

    /**
     * Returns an appropriate error message for a CRF in a language for a value
     * This is based on the entries in the ValidationMessages sheet from the iCRFSettings
     * E.g.
     * English LT --> Value must be less than "value"
     * Dutch LT   --> Waarde moet kleiner zijn dan "value"
     * @param language simpleLanguage, e.g. en
     * @param value    value against which is checked in the CRF, e.g. 100
     * @return a string with the error messages
     */
    public String getErrorMessage(String language, String value) {
        if(languageMessagesMap.containsKey(language)) {
            return languageMessagesMap.get(language) + " " + value;
        }
        logger.warn("No message(s) defined for {} in language {}. Default language {} will be used for the message. Please add the appropriate message for the language to your cache\\iCRFSettings.xlsx", dropdownLabel, language, defaultLanguage);
        return languageMessagesMap.get(defaultLanguage) + " " + value;
    }

    @Override
    public String toString(){
        return dropdownLabel;
    }

}

