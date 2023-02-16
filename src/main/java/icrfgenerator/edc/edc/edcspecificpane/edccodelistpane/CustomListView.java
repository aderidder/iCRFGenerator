package icrfgenerator.edc.edc.edcspecificpane.edccodelistpane;

import icrfgenerator.codebook.CodebookItem;
import icrfgenerator.resourcemanagement.ResourceManager;
import icrfgenerator.settings.runsettings.RunSettings;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.List;

public class CustomListView extends ListView<ListData> {
    private final String key;
    private final String itemId;
    private MultipleSelectionModel<ListData> selectionModel;


    CustomListView(String key, CodebookItem codebookItem){
        super(ListData.transformDataToListData(codebookItem));
        this.key = key;
        this.itemId = codebookItem.getId();
        setup();
    }

    /**
     * create the listview
     */
    private void setup(){
        RunSettings runSettings = RunSettings.getInstance();
        setPadding(new Insets(5,0,0,5));
        getStylesheets().add(ResourceManager.getResourceStyleSheet("style3.css"));

        // set the selectionmodel to allow for the selection of multiple items
        selectionModel = getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);

        setCellFactory(e -> {
            // use a custom list cell to allow it to display our ListData
            ListCell<ListData> cell = new CustomListCell();

            // normally multiple selection requires the user to press ctrl. To improve the user
            // experience, we add some functionality which allows multiple selection simply based on clicking items
            // without the need to press ctrl.
            cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                if (!cell.isEmpty()) {
                    int index = cell.getIndex();
                    // check whether the clicked cell is currently selected
                    if (selectionModel.getSelectedIndices().contains(index)) {
                        // if it is, clicking it means deselecting it. Remove it from the selection model
                        // and from the runsettings
                        selectionModel.clearSelection(index);
                        runSettings.removeSelectedItemTerminology(key, itemId, cell.getItem().getCode());
                    } else {
                        // if it is not, clicking it means selecting it. Add it to the selection model
                        // and to the runsettings
                        selectionModel.select(index);
                        runSettings.addSelectedItemTerminology(key, itemId, cell.getItem().getCode());
                    }
                }
                // prevent the event from triggering other stuff
                event.consume();
            });
            return cell;
        });
    }

    /**
     * If a view is dirty because it has been updated in the background, we need to update the view
     * The following cases can occur:
     * 1. a group item has been selected
     *      a. if an underlying leafitem with codelist was not yet selected, all its codelist items are now selected
     *      b. if an underlying leafitem with codelist was already selected, nothing changes for this item
     * 2. a group item has been deselected
     *      a. underlying leafitems are now deselected
     * For 2a deselecting also implies the item itself is deselected, which should already reset the codelist
     * For 1b nothing happens
     * For 1a this implies a selectAll
     */
    void updateDirtyView(){
        RunSettings runSettings = RunSettings.getInstance();
        List<String> selectedCodes = runSettings.getSelectedItemSelectedTerminologyCodes(key, itemId);
        if(selectedCodes.size()>0){
            selectionModel.selectAll();
        }
        // Probably not necessary, but I'll add it just in case
        else{
            selectionModel.clearSelection();
        }
    }

    /**
     * enabled the listview fields
     */
    public void enableFields() {
        setDisable(false);
    }

    /**
     * disable the listview fields
     */
    public void disableFields() {
        setDisable(true);
        selectionModel.clearSelection();
    }

    /**
     * deselect all codes
     */
    public void deselectAllCheckBoxes() {
        RunSettings runSettings = RunSettings.getInstance();
        runSettings.removeSelectedItemTerminologies(key, itemId);
        selectionModel.clearSelection();
    }

    /**
     * select all codes
     */
    public void selectAllCheckBoxes() {
        RunSettings runSettings = RunSettings.getInstance();
        runSettings.addSelectedItemTerminologies(key, itemId);
        selectionModel.selectAll();
    }

    /**
     * custom cell for the listview to use
     */
    private static class CustomListCell extends ListCell<ListData> {
        private final HBox content;
        private final Text code;
        private final Text codeSystem;
        private final Text label;
        private final Text description;

        CustomListCell() {
            super();
            code = new Text();
            code.setWrappingWidth(100);

            codeSystem = new Text();
            codeSystem.setWrappingWidth(100);

            label = new Text();
            label.setWrappingWidth(150);

            description = new Text();
            description.setWrappingWidth(400);

            content = new HBox(code, codeSystem, label, description);
            content.setSpacing(10);
        }

        @Override
        protected void updateItem(ListData item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null && !empty) {
                code.setText(item.getCode());
                codeSystem.setText(item.getCodeSystem());
                label.setText(item.getDisplayName());
                description.setText(item.getDescription());

                setGraphic(content);
            } else {
                setGraphic(null);
            }
        }
    }
}
