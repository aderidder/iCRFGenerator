package icrfgenerator.gui.wizard.selectcodebooks;

import icrfgenerator.gui.i18n.I18N;
import icrfgenerator.resourcemanagement.ResourceManager;
import icrfgenerator.settings.GlobalSettings;
import icrfgenerator.settings.runsettings.RunSettings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import org.controlsfx.control.SearchableComboBox;

import java.util.*;

public class CodebooksSearchPage extends Dialog<ButtonType> {
    private static final int prefWidth = 600;
    private static final int prefHeight = 650;

    private static final double hGap = 10;
    private static final double vGap = 8;

    private static final double buttonWidth = 28;

    private final ListSelectionView codebookSelectionView = new ListSelectionView();
    private final List<CheckBox> sourceCheckBoxList = new ArrayList<>();
    private final ListView<String> selectedTagsListView = new ListView<>();
    private final ToggleGroup toggleGroup = new ToggleGroup();

    /**
     * generate a general button with a title
     * @param title button's text
     * @return the button
     */
    private static Button generateSelectionButton(String title){
        Button button = new Button(title);
        button.setPrefWidth(buttonWidth);
        button.setMinWidth(buttonWidth);
        return button;
    }

    /**
     * generate a general gridpane
     * @return the gridpane
     */
    private static GridPane buildGridPane(){
        GridPane gridPane = new GridPane();
        gridPane.setVgap(vGap);
        gridPane.setHgap(hGap);
        gridPane.getStyleClass().add("bordered-gridpane");
        return gridPane;
    }

    /**
     * constructor
     */
    CodebooksSearchPage(){
        super();
        buildDialog();
    }

    /**
     * returns the selected codebooks names
     * @return the selected codebooks names
     */
    List<String> getSelectedCodebookNames(){
        return codebookSelectionView.selectedListView.getItems();
    }

    /**
     * Builds the dialog
     */
    private void buildDialog(){
        initModality(Modality.APPLICATION_MODAL);
        setTitle(I18N.getLanguageText("pageCodebookSearchTitle"));
        getDialogPane().setPrefSize(prefWidth, prefHeight);
        initStyle(StageStyle.UTILITY);
        getDialogPane().getStylesheets().add(ResourceManager.getResourceStyleSheet("style.css"));
        getDialogPane().getStyleClass().add("fillBackground");
        // add the content and add two buttons
        getDialogPane().setContent(buildSearchPane());
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    }

    /**
     * Create the complete Search Pane. It adds
     * - The Source Selection Pane
     * - The Search by Tag Pane
     * - The Search Button
     * - The Codebook Selection Pane
     * @return the gridpane
     */
    private Node buildSearchPane(){
        GridPane gridPane = new GridPane();
        gridPane.setHgap(hGap);
        gridPane.setVgap(vGap);

        gridPane.add(buildSourceSelectionPane(), 0, 0, 1, 1);
        gridPane.add(buildSearchByTagPane(), 1, 0, 3, 1);
        gridPane.add(buildDoSearchPane(), 0, 3, 4, 1);
        gridPane.add(codebookSelectionView, 0, 4, 4, 1);

        return gridPane;
    }

    /**
     * create the search button
     * @return a hbox with the search button
     */
    private Node buildDoSearchPane(){
        Button searchButton = new Button(I18N.getLanguageText("pageCodebookUpdateSearchResults"));
        searchButton.setOnAction(e->doSearch());
        HBox hBox = new HBox(searchButton);
        HBox.setMargin(searchButton, new Insets(0, 0, 10, 0));
        hBox.setAlignment(Pos.TOP_CENTER);
        return hBox;
    }

    /**
     * create the Search by Tag Pane
     * @return the Pane
     */
    private Node buildSearchByTagPane(){
        GridPane gridPane = buildGridPane();

        int row=0;
        gridPane.add(new Label(I18N.getLanguageText("pageCodebookSearchSearchByTag")), 0, row);

        // add the searchable combobox component
        SearchableComboBox<String> searchableComboBox = new SearchableComboBox<>(FXCollections.observableArrayList(GlobalSettings.getTags()));
        gridPane.add(searchableComboBox, 0, ++row);

        // add the selected tags component
        selectedTagsListView.setPrefHeight(175);
        gridPane.add(selectedTagsListView, 2, row, 1, 3);

        // create a VBox with buttons to allow a users to (de)select tags
        Button addSearchTagButton = generateSelectionButton(">");
        addSearchTagButton.setOnAction(e-> addSearchTag(searchableComboBox, selectedTagsListView));
        Button removeSearchTagButton = generateSelectionButton("<");
        removeSearchTagButton.setOnAction(e->removeSearchTag(selectedTagsListView));
        Button removeAllSearchTagsButton = generateSelectionButton("<<");
        removeAllSearchTagsButton.setOnAction(e->removeAllSearchTags(selectedTagsListView));

        VBox vBox = new VBox(addSearchTagButton, removeSearchTagButton, removeAllSearchTagsButton);
        vBox.setSpacing(vGap);
        vBox.setAlignment(Pos.CENTER);
        gridPane.add(vBox, 1, row, 1, 3);

        // create an HBox with radio buttons to allow a user to choose Any or All
        RadioButton radioButtonAny = new RadioButton(I18N.getLanguageText("pageCodebookSearchAnyTags"));
        radioButtonAny.setSelected(true);
        radioButtonAny.setToggleGroup(toggleGroup);
        RadioButton radioButtonAll = new RadioButton(I18N.getLanguageText("pageCodebookSearchAllTags"));
        radioButtonAll.setToggleGroup(toggleGroup);

        HBox hBox = new HBox(radioButtonAny, radioButtonAll);
        hBox.setSpacing(hGap);
        hBox.setAlignment(Pos.BOTTOM_LEFT);
        gridPane.add(hBox, 0, ++row);

        return gridPane;
    }

    /**
     * add the tag from the searchableComboBox to the selectedTagsListView
     * @param searchableComboBox   searchableComboBox
     * @param selectedTagsListView selectedTagsListView
     */
    private void addSearchTag(SearchableComboBox<String> searchableComboBox, ListView<String> selectedTagsListView){
        String selectedItem = searchableComboBox.getSelectionModel().getSelectedItem();
        if (!selectedTagsListView.getItems().contains(selectedItem)){
            selectedTagsListView.getItems().add(selectedItem);
        }
    }

    /**
     * remove a tag from the selectedTagsView
     * @param selectedTagsListView selectedTagsView
     */
    private void removeSearchTag(ListView<String> selectedTagsListView){
        selectedTagsListView.getItems().removeAll(selectedTagsListView.getSelectionModel().getSelectedItems());
    }

    /**
     * remove all tags from the selectedTagsView
     * @param selectedTagsListView selectedTagsListView
     */
    private void removeAllSearchTags(ListView<String> selectedTagsListView){
        selectedTagsListView.getItems().clear();
    }

    /**
     * builds the source selection pane, which allows the user to filter ART-DECOR and/or OpenEHR codebooks
     * @return the pane
     */
    private Node buildSourceSelectionPane(){
        GridPane gridPane = buildGridPane();
        int row=0;
        gridPane.add(new Label(I18N.getLanguageText("pageCodebookSearchSelectSource")), 0, row);

        // sources are currently ARTDECOR and OpenEHR
        List<String> sourceChoices = Arrays.asList("ARTDECOR","OPENEHR");
        // create a checkbox for each source type
        for (String sourceChoice:sourceChoices){
            CheckBox checkBox = new CheckBox(sourceChoice);
            checkBox.setId(sourceChoice);
            checkBox.setSelected(true);
            sourceCheckBoxList.add(checkBox);
            gridPane.add(checkBox, 0, ++row);
        }

        // add buttons to select all and none to an hbox
        Button selectAllButton = new Button(I18N.getLanguageText("pageCodebookSearchAllSources"));
        selectAllButton.setOnAction(e-> sourceCheckBoxList.forEach(f->f.setSelected(true)));
        Button selectNoneButton = new Button(I18N.getLanguageText("pageCodebookSearchClearSources"));
        selectNoneButton.setOnAction(e-> sourceCheckBoxList.forEach(f->f.setSelected(false)));
        HBox hBox = new HBox(selectAllButton, selectNoneButton);
        hBox.setSpacing(hGap);
        hBox.setPadding(new Insets(10, 0, 0, 0));
        gridPane.add(hBox, 0, ++row);

        return gridPane;
    }

    /**
     * filter the codebooks
     */
    private void doSearch(){
        // filter by source type
        List<String> searchResults = filterBySource();

        // filter by tags
        List<String> selectedTagsList = selectedTagsListView.getItems();
        if(selectedTagsList.size()>0){
            searchResults = filterByTags(searchResults, selectedTagsList);
        }

        // update the codebookSelectionView with the search results
        codebookSelectionView.updateSelectableCodebooks(searchResults);
    }

    /**
     * filter by source
     * @return filtered list
     */
    private List<String> filterBySource(){
        List<String> selectedSourceList = sourceCheckBoxList.stream().filter(t -> t.isSelected()).map(t->t.getId()).toList();
        return GlobalSettings.getCodebookNamesFilteredBySource(selectedSourceList);
    }

    /**
     * filter by tags
     * @param searchResults1   current results
     * @param selectedTagsList selected tags
     * @return filtered list
     */
    private List<String> filterByTags(List<String> searchResults1, List<String> selectedTagsList){
        List<String> searchResults2;
        // Determine whether filtering should be Any of All and do the filtering
        String selectedRadio = ((RadioButton)toggleGroup.getSelectedToggle()).getText();
        if(selectedRadio.equalsIgnoreCase(I18N.getLanguageText("pageCodebookSearchAnyTags"))){
            searchResults2 = GlobalSettings.getCodebookNamesFilteredByTagAny(selectedTagsList);
        }
        else{
            searchResults2 = GlobalSettings.getCodebookNamesFilteredByTagAll(selectedTagsList);
        }
        return searchResults1.stream().filter(searchResults2::contains).toList();
    }

    /**
     * custom list selection view
     * has a "selected" and a "selectable" listview as well as some buttons to allow the user to move items from
     * one list to the other
     */
    private static class ListSelectionView extends GridPane {
        private static final double listViewPrefHeight = 300;

        private final ListView<String> selectableListView = new ListView<>();
        private final ListView<String> selectedListView = new ListView<>();
        private List<String> searchResultList;

        /**
         * sort items in a listView's list, natural order
         * @param listView listView
         */
        private static void sortListViewItems(ListView<String> listView){
            listView.getItems().sort(Comparator.naturalOrder());
        }

        /**
         * returns the list1 without the items from list2
         * @param list1 list1
         * @param list2 list2
         * @return filtered list
         */
        private static List<String> filterList(Collection<String> list1, Collection<String> list2){
            return list1.stream().filter(t->!list2.contains(t)).toList();
        }

        /**
         * construct the ListSelectionView
         */
        private ListSelectionView(){
            super();
            setVgap(vGap);
            setHgap(hGap);
            getStyleClass().add("bordered-gridpane");
            setup();
        }

        /**
         * build the contents
         */
        private void setup(){
            int row = 0 ;
            add(new Label(I18N.getLanguageText("pageCodebookSearchSelectCodebooks")), 0, row);

            selectableListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            selectableListView.setPrefHeight(listViewPrefHeight);
            add(selectableListView, 0, ++row, 1, 4);

            Button addButton = generateSelectionButton(">");
            addButton.setOnAction(e->addItems(selectableListView.getSelectionModel().getSelectedItems()));

            Button addAllButton = generateSelectionButton(">>");
            addAllButton.setOnAction(e->addItems(selectableListView.getItems()));

            Button removeButton = generateSelectionButton("<");
            removeButton.setOnAction(e->removeItems(selectedListView.getSelectionModel().getSelectedItems()));

            Button removeAllButton = generateSelectionButton("<<");
            removeAllButton.setOnAction(e->removeItems(selectedListView.getItems()));

            VBox vBox = new VBox(addButton, addAllButton, removeButton, removeAllButton);
            vBox.setSpacing(vGap);
            vBox.setAlignment(Pos.CENTER);
            add(vBox, 1, row, 1, 2);

            selectedListView.getItems().setAll(FXCollections.observableArrayList(RunSettings.getInstance().getSelectedCodebooks()));
            selectedListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            selectedListView.setPrefHeight(listViewPrefHeight);
            add(selectedListView, 2, row, 1, 4);

            // update the selectable codebooks using all codebook names, since initially no filtering takes place
            updateSelectableCodebooks(GlobalSettings.getCodebookNames());
        }

        /**
         * the selectable list is the searchresults minus items that were already selected
         * so basically, if the result set contains PALGA Colon Biopsy and you search for
         * all PALGA codebooks, the selection set will contain all PALGA codebooks minus the already selected
         * PALGA Colon Biopsy codebook
         * @param searchResultList results from the search query
         */
        private void updateSelectableCodebooks(List<String> searchResultList){
            this.searchResultList = searchResultList;
            selectableListView.getItems().setAll(FXCollections.observableArrayList(filterList(searchResultList, selectedListView.getItems())));
            sortListViewItems(selectableListView);
        }

        /**
         * add items to the selectedListView and remove these items from the selectableListView
         * @param selectedItems list of items
         */
        private void addItems(ObservableList<String> selectedItems){
            selectedListView.getItems().addAll(selectedItems);
            selectableListView.getItems().removeAll(selectedItems);
            selectableListView.getSelectionModel().clearSelection();
            sortListViewItems(selectedListView);
        }

        /**
         * remove items from the selectedListView and add these items to the selectableListView
         * @param selectedItems list of items
         */
        private void removeItems(ObservableList<String> selectedItems){
            for(String item:selectedItems){
                if(searchResultList.contains(item)){
                    selectableListView.getItems().add(item);
                }
            }
            selectedListView.getItems().removeAll(selectedItems);
            selectedListView.getSelectionModel().clearSelection();
            sortListViewItems(selectableListView);
        }
    }
}




