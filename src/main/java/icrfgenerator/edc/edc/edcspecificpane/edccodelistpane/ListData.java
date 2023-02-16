package icrfgenerator.edc.edc.edcspecificpane.edccodelistpane;

import icrfgenerator.codebook.CodebookItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * Structure used by the CustomListView with the codelist data
 */
class ListData {
    private final String code;
    private final String codeSystem;
    private final String displayName;
    private final String description;

    /**
     * transforms codeList, valueList and codeSystemList entry into rows in an observableList for the ListView
     * @return observable list of the data
     */
    static ObservableList<ListData> transformDataToListData(CodebookItem codebookItem) {
        ObservableList<ListData> observableList = FXCollections.observableArrayList();
        List<String> codesList = codebookItem.getCodelistCodes();
        List<String> valuesList = codebookItem.getCodelistValues();
        List<String> codeSystemList = codebookItem.getCodelistCodeSystems();
        List<String> displayNameList = codebookItem.getCodelistDescriptions();

        for (int i = 0; i < codesList.size(); i++) {
            // create a ListData object with the code, codesystem and value
            observableList.add(new ListData(codesList.get(i), codeSystemList.get(i), displayNameList.get(i), valuesList.get(i)));
        }
        return observableList;
    }

    /**
     * Constructor
     * @param code        codelist item code
     * @param codeSystem  codelist item codesystem
     * @param description codelist item description
     * @param displayName codelist item displayname
     */
    ListData(String code, String codeSystem, String description, String displayName) {
        this.code = code;
        this.codeSystem = codeSystem;
        this.description = description;
        this.displayName = displayName;
    }

    /**
     * returns code
     * @return code
     */
    String getCode() {
        return code;
    }

    /**
     * returns codesystem
     * @return codesystem
     */
    String getCodeSystem() {
        return codeSystem;
    }

    /**
     * returns description
     * @return description
     */
    String getDescription() {
        return description;
    }

    /**
     * returns displayname
     * @return displayname
     */
    String getDisplayName() {
        return displayName;
    }

}
