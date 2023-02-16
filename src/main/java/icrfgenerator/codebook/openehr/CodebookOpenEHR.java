package icrfgenerator.codebook.openehr;


import com.nedap.archie.aom.*;
import com.nedap.archie.aom.primitives.*;
import com.nedap.archie.aom.terminology.ValueSet;
import com.nedap.archie.base.Interval;
import com.nedap.archie.rules.Assertion;
import icrfgenerator.codebook.CodebookDefault;
import icrfgenerator.codebook.CodebookItem;
import icrfgenerator.codebook.CodebookItemDefault;
import icrfgenerator.codebook.shared.CodebookStructureNode;
import icrfgenerator.types.NodeType;
import icrfgenerator.types.OperatorType;
import icrfgenerator.utils.KeyUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.time.Duration;
import java.time.Period;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CodebookOpenEHR extends CodebookDefault {
    private static final Logger logger = LogManager.getLogger(CodebookOpenEHR.class.getName());
    private final List<String> doubleDetectList = new ArrayList<>();

    private int id_cnt=0;

    /**
     * constructor for new openEHR codebook
     * @param datasetId        id of the dataset
     * @param extendedLanguage full language (e.g. en-US, although for openEHR this will still be en)
     * @param codebookName     codebook name
     */
    public CodebookOpenEHR(String datasetId, String extendedLanguage, String codebookName){
        super(datasetId, extendedLanguage, codebookName);
        createBook(codebookName, root);
    }

    /**
     * Add an underscore and an ever-increasing number within the codebook to all the identifiers to ensure they are unique
     * This ensures there won't be issues with multiple codebooks or repeats of codebooks in codebooks
     * @param id the identifier
     * @return a unique version of the identifier
     */
    String getUniqueId(String curCodebookName, String id){
        return KeyUtils.getKey(curCodebookName, id, Integer.toString(id_cnt++));
    }


    /**
     * Entrypoint for creating/processing a codebook
     * @param curCodebookName       name of the codebook
     * @param codebookStructureNode structure node
     */
    private void createBook(String curCodebookName, CodebookStructureNode codebookStructureNode){
        Archetype archetype = OpenEHRCodebookRepository.getArchetype(curCodebookName);
        List<CAttribute> attributeList = archetype.getDefinition().getAttributes();
        descendTree(curCodebookName, attributeList, codebookStructureNode);
    }

    /**
     * Create a group item in the tree
     * @param curCodebookName the current codebook's name
     * @param cObject         the object with the details
     * @param parentTreeNode  the parent tree node to which a new tree node will be added
     * @return a new codebookStructureNode created for the group
     */
    private CodebookStructureNode createGroupItem(String curCodebookName, CObject cObject, CodebookStructureNode parentTreeNode){
        String id = getUniqueId(curCodebookName, OpenEHRCodebookRepository.getItemID(cObject));
        String name = OpenEHRCodebookRepository.getItemText(curCodebookName, cObject, extendedLanguage);
        String description = OpenEHRCodebookRepository.getItemDescription(curCodebookName, cObject, extendedLanguage);
        return addItem(new CodebookItemOpenEHR(id, name, description, NodeType.GROUPITEM), parentTreeNode);
    }

    /**
     * Create one or more leaf items. Normally one, but more than one can happen
     * @param curCodebookName the current codebook's name
     * @param cObject         the object with the details
     * @param parentTreeNode  the parent tree node to which a new tree node will be added
     */
    private void createLeafItem(String curCodebookName, CObject cObject, CodebookStructureNode parentTreeNode){
        try {
            List<CodebookItem> childList = new ArrayList<>();
            List<CodebookItem> nullValueList = new ArrayList<>();
            List<CAttribute> cAttributeList = cObject.getAttributes();

            // cAttributesList.size() is normally 1, but can also be (at least?) 2, when there's also a "null_flavour" child.
            // Create separate items for both these types and then merge the null_flavours into the normal child lists
            for (CAttribute cAttribute : cAttributeList) {
                String childType = cAttribute.getRmAttributeName();
                if (childType.equalsIgnoreCase("value")) {
                    addChildren(cObject, cAttribute.getChildren(), curCodebookName, childList);
                }
                else if (childType.equalsIgnoreCase("null_flavour")) {
                    List<CObject> cObjectChildren = cAttribute.getChildren();
                    // not sure whether there is ever more than one child, since that would mean
                    // 2 separate null_flavour lists in one null_flavour item. But let's play it safe here
                    for (CObject cObjectChild : cObjectChildren) {
                        nullValueList.add(createChild(getUniqueId(curCodebookName, ""), "", "", cObjectChild.getRmTypeName(), cObjectChild, curCodebookName));
                    }
                }
                else {
                    // if we find a childType which isn't a value, nor a null_flavour... what is it? De we need to do something with it?
                    String outString = "createLeafItem found an unexpected type: " + cAttribute.getRmAttributeName();
                    System.err.println("\t" + outString);
                    logger.log(Level.ERROR, outString);
                }
            }

            // if there's a nullValueList, merge its contents with the children in the childList
            if (nullValueList.size() > 0) {
                childList.forEach(childItem -> nullValueList.forEach(nullItem -> childItem.mergeCodebookItemCodeLists((CodebookItemDefault) nullItem)));
            }

            // add the items to the tree
            childList.forEach(e -> addItem(e, parentTreeNode));
        } catch (Exception e){
            // For debugging purposes
            String outString = "An error occurred for nodeID "+cObject.getNodeId()+" in codebook "+curCodebookName;
            logger.log(Level.ERROR, outString);
            System.err.println(outString);
        }
    }

    /**
     * An extra layer for adding leaf items as sometimes a single item can consist of multiple items
     * An example is in "blood pressure" (<a href="https://ckm.openehr.org/ckm/archetypes/1013.1.3574">...</a>), where the
     * item "Location of measurement" has "Coded Text" and "Text".
     * If this happens, we'll create more items to which we'll add "pt1", "pt2", etc.
     * @param baseObject       the parent
     * @param cObjectChildren  the value children list
     * @param curCodebookName  the current codebook's name
     * @param codebookItemList list to which we're adding newly created CodebookItems
     */
    private void addChildren(CObject baseObject, List<CObject> cObjectChildren, String curCodebookName, List<CodebookItem> codebookItemList){
        // check whether there are multiple children. If so, add a _v<nr> for each one, which allows
        // the user to select the one he/she is interested in
        if(cObjectChildren.size()>1){
            for(int i=0; i<cObjectChildren.size(); i++){
                CObject childDetailsObject = cObjectChildren.get(i);
                String id = getUniqueId(curCodebookName, OpenEHRCodebookRepository.getItemID(baseObject) + "_v" + (i + 1));
                String itemName = OpenEHRCodebookRepository.getItemText(curCodebookName, baseObject, extendedLanguage) + "_v" + (i + 1);
                createChild(id, itemName, baseObject, childDetailsObject, curCodebookName, codebookItemList);
            }
        }
        else{
            // single-item, which happens most of the time
            CObject childDetailsObject = cObjectChildren.get(0);
            String id = getUniqueId(curCodebookName, OpenEHRCodebookRepository.getItemID(baseObject));
            String itemName = OpenEHRCodebookRepository.getItemText(curCodebookName, baseObject, extendedLanguage);
            createChild(id, itemName, baseObject, childDetailsObject, curCodebookName, codebookItemList);
        }
    }

    /**
     * An extra layer of complexity is added by the DV_INTERVAL<DV_SOMETHING> type, e.g. DV_INTERVAL<DV_DATE_TIME>
     * If this is encountered there should probably be 2 children: a "from" and a "to"
     * @param id                 unique id for the item
     * @param itemName           item name
     * @param baseObject         the parent
     * @param childDetailsObject object which has the details which will be used to populate the item
     * @param curCodebookName    name of the current codebook
     * @param codebookItemList   list to which we're adding newly created CodebookItems
     */
    private void createChild(String id, String itemName, CObject baseObject, CObject childDetailsObject, String curCodebookName, List<CodebookItem> codebookItemList) {
        Pattern p = Pattern.compile(".*<(.*)>");
        String childType = childDetailsObject.getRmTypeName();
        String description = OpenEHRCodebookRepository.getItemDescription(curCodebookName, baseObject, extendedLanguage);
        Matcher matcher = p.matcher(childType);
        if(matcher.matches()){
            // grab what type of interval we're dealing with, e.g. DV_DATE
            String intervalOf = matcher.group(1);
            // hopefully there is information about the interval
            // for quantity and count this seems ok; for date and date_time this is a bit dubious, since we don't do anything with min/max-like things there
            // however, so far I haven't encountered an interval with dates that has a min/max. If this does exist we'll have to update the code here.
            if(childDetailsObject.getAttributes().size()==2) {
                codebookItemList.add(createChild(id + "_interval_from", itemName + "_interval_from", description, intervalOf, childDetailsObject.getAttribute("lower").getChildren().get(0), curCodebookName));
                codebookItemList.add(createChild(id + "_interval_to", itemName + "_interval_to", description, intervalOf, childDetailsObject.getAttribute("upper").getChildren().get(0), curCodebookName));
            }
            else if(childDetailsObject.getAttributes().size()==1) {
                // if there's only 1 attribute, this can be either the upper of lower attribute.
                // since there is no information about the other one, we'll add a "_SKIP" to the intervalOf for these items
                String skipType = intervalOf+"_SKIP";
                if(childDetailsObject.getAttribute("upper")!=null){
                    codebookItemList.add(createChild(id + "_interval_from", itemName + "_interval_from", description, skipType, childDetailsObject, curCodebookName));
                    codebookItemList.add(createChild(id + "_interval_to", itemName + "_interval_to", description, intervalOf, childDetailsObject.getAttribute("upper").getChildren().get(0), curCodebookName));
                }
                else if(childDetailsObject.getAttribute("lower")!=null){
                    codebookItemList.add(createChild(id + "_interval_from", itemName + "_interval_from", description, intervalOf, childDetailsObject.getAttribute("lower").getChildren().get(0), curCodebookName));
                    codebookItemList.add(createChild(id + "_interval_to", itemName + "_interval_to", description, skipType, childDetailsObject, curCodebookName));
                }
                else{
                    logger.error("Attribute found that's not 'upper' or 'lower'. That's unexpected. Attribute: "+childDetailsObject.getAttributes());
                }

            }
            else{
                codebookItemList.add(createChild(id + "_interval_from", itemName + "_interval_from", description, intervalOf, childDetailsObject, curCodebookName));
                codebookItemList.add(createChild(id + "_interval_to", itemName + "_interval_to", description, intervalOf, childDetailsObject, curCodebookName));
            }
        }
        else {
            codebookItemList.add(createChild(id, itemName, description, childType, childDetailsObject, curCodebookName));
        }
    }

    /**
     * Create the codebookItem and attempt to properly populate the child details based on the childType
     * @param id                 id of the new item
     * @param itemName           name of the new item
     * @param description        description of the new item
     * @param childType          type of item
     * @param childDetailsObject object which has the details which will be used to populate the item
     * @param curCodebookName    name of the current codebook
     * @return the newly created and populated codebookItem
     */
    private CodebookItem createChild(String id, String itemName, String description, String childType, CObject childDetailsObject, String curCodebookName) {
        CodebookItemOpenEHR codebookItemOpenEHR =  new CodebookItemOpenEHR(id, itemName, description, NodeType.LEAFITEM);

        boolean print = !childDetailsObject.getRmTypeName().equalsIgnoreCase("ELEMENT");

        // I'll add DV_DATE_TIME to TEXT for now. Not sure yet whether it should become something different, since we
        // don't seem to be doing things with it in ART-DECOR either
        // DV_MULITMEDIA is text as well for now; maybe add some for of File field?
        // DV_TIME is text as well for now
        switch (childType) {
            case "DV_DATE" -> buildDVDATE(codebookItemOpenEHR);
            case "DV_ORDINAL" -> buildDVORDINALChild(curCodebookName, childDetailsObject, codebookItemOpenEHR);
            case "DV_CODED_TEXT" -> buildDVCODEDTEXTChild(curCodebookName, childDetailsObject, codebookItemOpenEHR);
            case "DV_TEXT", "DV_URI", "DV_IDENTIFIER", "DV_DATE_TIME", "DV_MULTIMEDIA", "DV_TIME" -> buildDVTEXTChild(codebookItemOpenEHR);
            case "DV_QUANTITY", "DV_INTERVAL<DV_QUANTITY>" -> buildDVQUANTITYChild(childDetailsObject, codebookItemOpenEHR, curCodebookName);
            case "DV_QUANTITY_SKIP" -> buildDVSKIP(codebookItemOpenEHR, "quantity");
            case "DV_DATE_SKIP" -> buildDVSKIP(codebookItemOpenEHR, "date");
            case "DV_COUNT_SKIP" -> buildDVSKIP(codebookItemOpenEHR, "count");
            case "DV_COUNT" -> buildDVCOUNT(childDetailsObject, codebookItemOpenEHR);
            case "DV_BOOLEAN" -> buildDVBOOLEAN(codebookItemOpenEHR);
            case "DV_DURATION" -> buildDVDURATION(childDetailsObject, codebookItemOpenEHR);
            default -> {
                if(print) {
                    System.err.println("Maybe build leaf code for me: " + childDetailsObject.getRmTypeName());
                    codebookItemOpenEHR.setDataType("string");
                }
            }
        }
        return codebookItemOpenEHR;
    }

    /**
     * A helper to skip the logic for an item and just set the data type
     * This is used when e.g. for a DV_QUANTITY interval there is only an upper and no lower set; in that case
     * this skip function is called for the lower.
     * @param codebookItemOpenEHR codebookItem to which to add these details
     * @param itemType            data type to which to set the item
     */
    private void buildDVSKIP(CodebookItemOpenEHR codebookItemOpenEHR, String itemType){
        codebookItemOpenEHR.setDataType(itemType);
    }

    /**
     * DV_DURATION is based on the ISO_8601 specifications: <a href="https://en.wikipedia.org/wiki/ISO_8601">...</a>
     * Also see <a href="https://specifications-test.openehr.org/releases/AM/latest/ADL2.html#_duration_constraints">...</a>
     * Furthermore, there may still be issue with this format:
     * See e.g. Timing - non-daily: Repetition interval
     *  DV_DURATION[id9002] matches {
     *      value matches {PYMWD/|>P0D|}
     *  }
     * The PYMWD/ does not seem to be parsed by archie
     * @param cObject             object with the details
     * @param codebookItemOpenEHR codebookItem to which to add these details
     */
    private void buildDVDURATION(CObject cObject, CodebookItemOpenEHR codebookItemOpenEHR){
        codebookItemOpenEHR.setDataType("quantity");

        if(cObject.getAttributes().size()>0 && cObject.getAttributes().get(0).getChildren().size()>0) {
            CDuration cDuration = (CDuration) cObject.getAttributes().get(0).getChildren().get(0);
            String durationConstraint = cDuration.getConstraint().toString();
            convertDuration(codebookItemOpenEHR, durationConstraint);
        }
    }

    /**
     * The format provided by the codebook must be converted to something which the EDCs can use
     * E.g. [|PT0S..PT12H|] is a time range between 0 seconds and 12 hours
     * At the moment we're converting this range to
     * - units seconds
     * - min 0
     * - max 43200
     * @param codebookItemOpenEHR codebookItem to which to add these details
     * @param durationConstraint  string representation of the constraints
     */
    private static void convertDuration(CodebookItemOpenEHR codebookItemOpenEHR, String durationConstraint){
        List<String> constraints = Arrays.asList(durationConstraint.split("\\.\\."));
        String symbol="=";

        if(constraints.size()>1){
            // in this case it should be a range - e.g. [|PT0S..PT12H|], which then becomes:
            // constraints = [PT0S, PT12H]
            // symbol = "="
            constraints = constraints.stream().map(t->t.replaceAll("\\W", "")).collect(Collectors.toList());
        }
        else if(constraints.size()==1){
            // in this case there could be a >= kind of construction in the string, e.g. [|>=PT0S|], which then becomes:
            // constraints = [PT0S]
            // symbol = ">="
            Pattern pattern = Pattern.compile(".*?([>=<]*)(\\w+).*");
            Matcher matcher = pattern.matcher(constraints.get(0));
            if(matcher.matches()){
                constraints = List.of(matcher.group(2));
                if(matcher.group(1)!=null) {
                    symbol = matcher.group(1);
                }
            }
        }
        else{
            logger.error("DV_DURATION constraint.size() can be 0? That's unexpected. durationConstraint="+durationConstraint);
        }

        convertDuration(codebookItemOpenEHR, constraints, symbol, durationConstraint.toUpperCase().contains("T"));
    }

    /**
     * Next part of the conversion duration, which:
     * - converts the values to the unit's scale
     * - sets the units
     * - sets the min and max
     * @param codebookItemOpenEHR codebookItem to which to add these details
     * @param constraints         the cleaned constraints split into an array
     * @param symbol              symbol used for the constraints
     * @param isTimeConstraint    whether we're dealing with a Time constraint or a Period constraint
     */
    private static void convertDuration(CodebookItemOpenEHR codebookItemOpenEHR, List<String> constraints, String symbol, boolean isTimeConstraint){
        List<Integer> convertedValues;
        String firstConstraint = constraints.get(0);

        // set the units. For e.g. [|PT0S..PT12H|] this becomes S
        String units = firstConstraint.substring(firstConstraint.length()-1).toUpperCase();
        codebookItemOpenEHR.setUnits(units);

        // if durationConstraint contains a T, we're dealing with time, otherwise we're dealing with period
        // The values are converted to the unit, so for e.g. [|PT0S..PT12H|], since the S is the unit picked,
        // the 12H is converted to seconds (43200) as well.
        if(isTimeConstraint) {
            convertedValues = convertDurationTime(constraints, units);
        }
        else{
            convertedValues = convertDurationPeriod(constraints, units);
        }

        // now that we have the converted values, we can set the min and max
        if(convertedValues.size()>1){
            codebookItemOpenEHR.setMin(convertedValues.get(0).toString());
            codebookItemOpenEHR.setMax(convertedValues.get(1).toString());
        }
        else{
            if(symbol.equalsIgnoreCase(">") || symbol.equalsIgnoreCase("=>") || symbol.equalsIgnoreCase(">=")) {
                codebookItemOpenEHR.setMin(convertedValues.get(0).toString());
            }
            else if(symbol.equalsIgnoreCase("=")){
                codebookItemOpenEHR.setMin(convertedValues.get(0).toString());
                codebookItemOpenEHR.setMax(convertedValues.get(0).toString());
            }
            else{
                codebookItemOpenEHR.setMax(convertedValues.get(0).toString());
            }
        }
    }

    /**
     * convert values to a single unit for Time constraints
     * @param constraints list with constraints
     * @param units       the unit to convert to
     * @return converted values list
     */
    private static List<Integer> convertDurationTime(List<String> constraints, String units){
        long value;
        List<Integer> convertedValues = new ArrayList<>();
        for(String constraint : constraints) {
            Duration duration = Duration.parse(constraint);
            switch (units){
                case "D" -> value = duration.toDays();
                case "H" -> value = duration.toHours();
                case "M" -> value = duration.toMinutes();
                case "S" -> value = duration.toSeconds();
                default -> {
                    logger.error("Missing time convertion units: "+units);
                    value = -999;
                }
            }
            convertedValues.add(Long.valueOf(value).intValue());
        }
        return convertedValues;
    }

    /**
     * convert values to a single unit for Period constraints
     * @param constraints list with contraints
     * @param units       the unit to convert to
     * @return converted values list
     */
    private static List<Integer> convertDurationPeriod(List<String> constraints, String units){
        int value;
        List<Integer> convertedValues = new ArrayList<>();
        for(String constraint : constraints) {
            Period period = Period.parse(constraint);
            switch (units){
                case "D" -> value = period.getDays();
                case "M" -> value = period.getMonths();
                case "Y" -> value = period.getYears();
                default -> {
                    logger.error("Missing period convertion units: "+units);
                    value = -999;
                }
            }
            convertedValues.add(Long.valueOf(value).intValue());
        }
        return convertedValues;
    }

    /**
     * handle DV_DATE
     * @param codebookItemOpenEHR codebookItem to which to add these details
     */
    private void buildDVDATE(CodebookItemOpenEHR codebookItemOpenEHR){
        codebookItemOpenEHR.setDataType("date");
    }

    /**
     * handle DV_BOOLEAN
     * @param codebookItemOpenEHR codebookItem to which to add these details
     */
    private void buildDVBOOLEAN(CodebookItemOpenEHR codebookItemOpenEHR){
        codebookItemOpenEHR.setDataType("boolean");
    }

    /**
     * handle DV_COUNT
     * Example: Sympon / Sign - Number of previous episodes
     * DV_COUNT[id9020] matches {
     *  magnitude matches {|>=0|}
     * }
     * @param cObject             object with details
     * @param codebookItemOpenEHR codebookItem to which to add these details
     */
    private void buildDVCOUNT(CObject cObject, CodebookItemOpenEHR codebookItemOpenEHR){
        codebookItemOpenEHR.setDataType("count");
        if(cObject.getAttributes().size()>0) {
            CAttribute cAttribute = cObject.getAttributes().get(0);
            if (!cAttribute.getRmAttributeName().equalsIgnoreCase("magnitude")) {
                System.err.println("DV_COUNT not always 'magnitude'. Check what else can be in there, since we're probably not compatible");
            }
            handleInterval(cAttribute, codebookItemOpenEHR);
        }
    }

    /**
     * Helper for e.g. DV_COUNT, DV_QUANTITY
     * Sets the min and max based on whether the interval is upper/lower bounded
     * @param cAttribute          attribute details
     * @param codebookItemOpenEHR codebookItem to which to add these details
     */
    private void handleInterval(CAttribute cAttribute, CodebookItemOpenEHR codebookItemOpenEHR){
        // ToDo: So, it turns out it's also possible to have multiple items here
        //  see e.g. Timing - non-daily: Frequency
        //  DV_QUANTITY[id9003] matches {
        //      property matches {[at9000]}    -- Frequency
        //          [magnitude, units, precision] matches {
        //          [{|1.0..7.0|}, {"1/wk"}, {0}],
        //          [{|1.0..31.0|}, {"1/mo"}, {0}],
        //          [{|1.0..366.0|}, {"1/a"}, {0}]
        //      }
        //  }
        //  TBD: how can we present this? Maybe add some form of dropdown for pre-selects? But this is pretty situational
        COrdered<?> child = (COrdered<?>)  cAttribute.getChildren().get(0);
        Interval<?> interval = child.getConstraint().get(0);
        if(!interval.isLowerUnbounded()) {
            String value = interval.getLower().toString();
//            codebookItemOpenEHR.setMin(removeDot(value));
            codebookItemOpenEHR.setMin(value);
            if(interval.isLowerIncluded()){
                codebookItemOpenEHR.setMinCheckOperator(OperatorType.GTE);
            }
            else{
                codebookItemOpenEHR.setMinCheckOperator(OperatorType.GT);
            }
        }
        if(!interval.isUpperUnbounded()) {
            String value = interval.getUpper().toString();
//            codebookItemOpenEHR.setMax(removeDot(value));
            codebookItemOpenEHR.setMax(value);
            if(interval.isUpperIncluded()){
                codebookItemOpenEHR.setMaxCheckOperator(OperatorType.LTE);
            }
            else{
                codebookItemOpenEHR.setMaxCheckOperator(OperatorType.LT);
            }
        }
    }

    /**
     * handle DV_ORDINAL
     * Example - Glasgow Coma Scale (GCS): Best eye response (E)
     * DV_ORDINAL[id9004] matches {
     *  [value, symbol] matches {
     *      [{1}, {[at11]}],
     *      [{2}, {[at12]}],
     *      [{3}, {[at13]}],
     *      [{4}, {[at14]}]
     *  }
     * }
     * @param curCodebookName     name of the current codebook being parsed
     * @param cObject             object with details
     * @param codebookItemOpenEHR codebookItem to which to add these details
     */
    private void buildDVORDINALChild(String curCodebookName, CObject cObject, CodebookItemOpenEHR codebookItemOpenEHR){
        codebookItemOpenEHR.setDataType("code");
        List<CAttributeTuple> attributeTupleList = ((CComplexObject) cObject).getAttributeTuples();
        for (CAttributeTuple cAttributeTuple:attributeTupleList){
            // we'll grab the symbols (e.g. at11, at12, etc) and give these to the handleTerminologyCode
            List<?> symbolList = cAttributeTuple.getMember("symbol").getChildren();
            for(Object symbol : symbolList){
                handleTerminologyCodes(curCodebookName, ((CTerminologyCode)symbol).getConstraint().get(0), codebookItemOpenEHR);
            }
        }
    }

    /**
     * Handle DV_CODED_TEXT
     * Example: Blood Pressure - Position
     * DV_CODED_TEXT[id9015] matches {
     *  defining_code matches {[ac9061]}    -- Position (synthesised)
     * }
     * @param curCodebookName     name of the current codebook being parsed
     * @param cObject             object with details
     * @param codebookItemOpenEHR codebookItem to which to add these details
     */
    private void buildDVCODEDTEXTChild(String curCodebookName, CObject cObject, CodebookItemOpenEHR codebookItemOpenEHR){
        codebookItemOpenEHR.setDataType("code");
        List<CAttribute> cAttributeList = cObject.getAttributes();
        for (CAttribute cAttribute : cAttributeList) {
            for(CObject child:cAttribute.getChildren()) {
                String terminologyKey = ((CTerminologyCode)child).getConstraint().get(0);
                handleTerminologyCodes(curCodebookName, terminologyKey, codebookItemOpenEHR);
            }
        }
    }

    /**
     * Convert the key to either a code or a list of codes.
     * In some cases a code refers to a list of codes
     * Example: Blood pressure - Position
     * DV_CODED_TEXT[id9015] matches {
     *  defining_code matches {[ac9061]}    -- Position (synthesised)
     * }
     * value_sets = <
     *  ["ac9061"] = <
     *      id = <"ac9061">
     *      members = <"at1001", "at1002", "at1003", "at1004", "at1015">
     *  >
     * @param curCodebookName     current codebook
     * @param key                 reference to a code or codelist
     * @param codebookItemOpenEHR codebookItem to which to add the details
     */
    private void handleTerminologyCodes(String curCodebookName, String key, CodebookItemOpenEHR codebookItemOpenEHR){
        Map<String, ValueSet> valueSets = OpenEHRCodebookRepository.getTerminologyValueSets(curCodebookName);

        if(valueSets.containsKey(key)){
            Set<String> members = valueSets.get(key).getMembers();
            for (String code : members) {
                String [] vals = handleSingleCode(curCodebookName, code);
                codebookItemOpenEHR.addCodelistItem(vals[0], vals[1], vals[2], vals[3]);
            }
        }
        else {
            String [] vals = handleSingleCode(curCodebookName, key);
            codebookItemOpenEHR.addCodelistItem(vals[0], vals[1], vals[2], vals[3]);
        }
    }

    /**
     * Handle a single code
     * First check whether it's in the bindings - this would allow us to use e.g. a SNOMED CT code
     * If not, use the local information
     * We return the values since we use this for both adding codelist items and adding the property description to
     * the description
     * @param curCodebookName     current codebook
     * @param code                reference to a code or binding
     * @return an array with code, name, codesystem, description
     */
    private String [] handleSingleCode(String curCodebookName, String code){
        String itemText = OpenEHRCodebookRepository.getItemText(curCodebookName, code, extendedLanguage);
        String itemDescription = OpenEHRCodebookRepository.getItemDescription(curCodebookName, code, extendedLanguage);
        String [] bindings = getBinding(curCodebookName, code);

        if(bindings!=null){
            return new String [] {bindings[1], itemText, bindings[0], itemDescription};
        }

        return new String [] {code, itemText, "openehr", itemDescription};
    }

    /**
     * Returns a code and codesystem when a term is bound to a different system via term_bindings
     * Example: Glasgow Coma Scale (GCS) - Best eye response
     * DV_CODED_TEXT[id9005] matches {
     *  defining_code matches {[at9001]}    -- not applicable
     * }
     *
     * term_bindings = <
     *  ["openehr"] = <
     *      ["at9001"] = <http://openehr.org/id/273>
     *  >
     * >
     * In this example, openehr and 273 are returned
     * @param curCodebookName the current codebook
     * @param code            the code which we need to check for bindings
     * @return array with codesystem and the code of this term in the system
     */
    private String [] getBinding(String curCodebookName, String code){
        Map<String, Map<String, URI>> termBindings = OpenEHRCodebookRepository.getTermBindings(curCodebookName);
        for (Map.Entry<String, Map<String, URI>> entry : termBindings.entrySet()) {
            Map<String, URI> systemBindingMap = entry.getValue();

            if (systemBindingMap.containsKey(code)) {
                String codesystem = entry.getKey();
                String uriString = systemBindingMap.get(code).toString();
                String codeInCodeSystem = uriString.substring(uriString.lastIndexOf("/")+1);
                return new String[]{codesystem, codeInCodeSystem};
            }

        }
        return null;
    }


    /**
     * Handle DV_TEXT
     * @param codebookItemOpenEHR item to which to add the details
     */
    private void buildDVTEXTChild(CodebookItemOpenEHR codebookItemOpenEHR){
        codebookItemOpenEHR.setDataType("string");
    }

    /**
     * Handle DV_QUANTITY
     * E.g. Hip arthroplasty component - Size
     *  DV_QUANTITY[id9003] matches {
     *      property matches {[at9001]}    -- Length
     *      magnitude matches {|0.0..20.0|}
     *      units matches {"mm"}
     *      precision matches {1}
     *  }
     * @param cObject             object with the details
     * @param codebookItemOpenEHR item to which to add the details
     */
    private void buildDVQUANTITYChild(CObject cObject, CodebookItemOpenEHR codebookItemOpenEHR, String curCodebookName){
        codebookItemOpenEHR.setDataType("quantity");
        List<CAttribute> cAttributeList = cObject.getAttributes();
        for(CAttribute cAttribute:cAttributeList){
            switch (cAttribute.getRmAttributeName()){
                case "property" ->  handleProperty(cAttribute, codebookItemOpenEHR, curCodebookName);
                case "magnitude" ->  handleInterval(cAttribute, codebookItemOpenEHR);
                case "units" ->  handleUnits(cAttribute, codebookItemOpenEHR);
                case "precision" ->  handlePrecision(cAttribute, codebookItemOpenEHR);
                default -> System.err.println("TODO Quantity attribute: "+cAttribute.getRmAttributeName());
            }
        }
    }

    /**
     * The property provides some extra description, e.g. "Length" or "Pressure". Since it's not something you can
     * change, we'll add it to the item's description.
     * @param cAttribute          object with the details
     * @param codebookItemOpenEHR item to which to add the details
     * @param curCodebookName     the current codebook
     */
    private void handleProperty(CAttribute cAttribute, CodebookItemOpenEHR codebookItemOpenEHR, String curCodebookName){
        CTerminologyCode child = (CTerminologyCode) cAttribute.getChildren().get(0);
        String terminologyKey = child.getConstraint().get(0);
        String [] vals = handleSingleCode(curCodebookName, terminologyKey);
        codebookItemOpenEHR.setDescription(codebookItemOpenEHR.getItemDescription()+" (Property: "+vals[1]+")");
    }

    /**
     * Handle units
     * @param cAttribute          object with the details
     * @param codebookItemOpenEHR item to which to add the details
     */
    private void handleUnits(CAttribute cAttribute, CodebookItemOpenEHR codebookItemOpenEHR){
        CString child = (CString) cAttribute.getChildren().get(0);
        String units = child.getConstraint().get(0);
        codebookItemOpenEHR.setUnits(units);
    }

    /**
     * Handle precision, which defines the decimals (e.g. precision=1 is one decimal)
     * @param cAttribute          object with the details
     * @param codebookItemOpenEHR item to which to add the details
     */
    private void handlePrecision(CAttribute cAttribute, CodebookItemOpenEHR codebookItemOpenEHR){
        COrdered<?> child = (COrdered<?>)  cAttribute.getChildren().get(0);
        Interval<?> interval = child.getConstraint().get(0);
        codebookItemOpenEHR.setPrecision(interval.getLower().toString());
    }

    /**
     * Archetypeslots refer to other archetypes (codebooks)
     * @param curCodebookName       the current codebook being parsed
     * @param archetypeSlot         the archetypeslot object which holds the reference information
     * @param codebookStructureNode the node in the tree to which the items need to be added
     */
    private void createArchetypeslotItem(String curCodebookName, ArchetypeSlot archetypeSlot, CodebookStructureNode codebookStructureNode){
        String id = OpenEHRCodebookRepository.getItemID(archetypeSlot);
        String name = OpenEHRCodebookRepository.getItemText(curCodebookName, archetypeSlot, extendedLanguage);
        String description = OpenEHRCodebookRepository.getItemDescription(curCodebookName, archetypeSlot, extendedLanguage);

        // Sometimes an archetypeslot does not have a reference. In that case we'll just add some information to the leaf
        if(archetypeSlot.getIncludes().size()==0){
            addItem(new CodebookItemOpenEHR(id, name, description, NodeType.LEAFINFOITEM), codebookStructureNode);
            return;
        }

        // Otherwise, create a CodebookIncludes object, which takes care of the messy details
        // of how the archetypeslot parsing.
        //
        // Next, we retrieve the RefsVersionsMap. This maps contains:
        //  - The referenced codebookname
        //  - The versions of this codebook that are referenced
        CodebookIncludes codebookIncludes = new CodebookIncludes(archetypeSlot.getIncludes());
        Map<String, List<Integer>> refVersionsMap = codebookIncludes.getRefsVersionsMaps();
        for (Map.Entry<String, List<Integer>> entry : refVersionsMap.entrySet()) {
            String refCodebookName = entry.getKey();
            List<Integer> versionList = entry.getValue();
            // In this tree branch, prevent that a codebook is referenced more than once
            // E.g.
            //  GroupItem
            //      Item1
            //      Ref1 --> ok
            //          Item2
            //          Ref1 --> prevented
            //  GroupItem2
            //      Ref1 --> ok
            if (!doubleDetectList.contains(refCodebookName)) {
                // special case
                if (refCodebookName.equalsIgnoreCase("ANY")) {
                    String newDescription = "All not explicitly excluded archetypes - these will have to be added manually";
                    addItem(new CodebookItemOpenEHR(id, name, newDescription, NodeType.LEAFINFOITEM), codebookStructureNode);
                }
                else {
                    // - add the referenced codebook to the double-detection list (without the version)
                    // - parse the referenced codebook
                    // - remove the referenced codebook from the double-detection list. This ensures that if the
                    //   codebook is referenced in another branch, it can be added again
                    doubleDetectList.add(refCodebookName);
                    handleRefCodebooks(refCodebookName, versionList, id, name, description, codebookStructureNode);
                    doubleDetectList.remove(refCodebookName);
                }
            }
            // show a message in the leaf that the branch is not expanded to prevent infinite expansion
            else{
                String newDescription = description+"\nBranch not added to prevent infinite expansion\nReference: "+refCodebookName.substring(0, refCodebookName.length()-2);
                addItem(new CodebookItemOpenEHR(id, name, newDescription, NodeType.LEAFINFOITEM), codebookStructureNode);
            }
        }
    }

    /**
     * We enter this function with a referenced codebookName and an ordered List of version that are referenced
     * Combining the codebookName with the version gives the fullRefName
     * This fullRefName can then be used to retrieve its identifier, which is required to be able to retrieve
     * the codebook
     * @param refCodebookName name of the referenced codebook
     * @param versionList     versions referenced of this codebook
     * @return an array with the identifier found and the full reference name
     */
    private String[] getRefCodebookInfo(String refCodebookName, List<Integer> versionList){
        String refCodebookId = "";
        String fullRefName = "";
        for (Integer version : versionList) {
            fullRefName = refCodebookName + version;
            refCodebookId = OpenEHRCodebookRepository.getRefCodebookId(codebookName, fullRefName);
            // As soon as we find a refCodebookId, get out of here
            if(!refCodebookId.equalsIgnoreCase("")){
                break;
            }
        }
        return new String[]{refCodebookId, fullRefName};
    }

    /**
     *
     * @param refCodebookName       codebook being referenced
     * @param versionList           versions of this codebook to that are referenced
     * @param id                    id of the current item
     * @param name                  name of the current item
     * @param description           description of the current item
     * @param codebookStructureNode structure node
     */
    private void handleRefCodebooks(String refCodebookName, List<Integer> versionList, String id, String name, String description, CodebookStructureNode codebookStructureNode){
        // (attempt to) fetch the codebook identifier being referenced and the full name (codebookName + version)
        String [] refInfo = getRefCodebookInfo(refCodebookName, versionList);
        String refCodebookId = refInfo[0];
        String fullRefName = refInfo[1];

        // if there is no refCodebookId, it means the references don't exist. Provide this information as feedback
        if(refCodebookId.equalsIgnoreCase("")){
            String checked = versionList.stream().map(t->refCodebookName+t).collect(Collectors.joining("\n"));
            String newDescription = "The referenced codebook does not seem to exist in ckm. References tried:\n"+checked;
            addItem(new CodebookItemOpenEHR(id, name, newDescription, NodeType.LEAFINFOITEM), codebookStructureNode);
        }
        else{
            // add a group item as the basis for the referenced codebook
            CodebookItem codebookItem = new CodebookItemOpenEHR(id, name, description, NodeType.GROUPITEM);
            CodebookStructureNode codebookStructureNode1 = addItem(codebookItem, codebookStructureNode);
            // fetch and prepare the referenced codebook
            OpenEHRCodebookRepository.addRefCodebook(codebookName, fullRefName, refCodebookId);
            // call createbook to parse the referenced codebook and add its items to the node we just created
            createBook(fullRefName, codebookStructureNode1);
        }
    }

    /**
     * the interval events seem to refer to existing nodes in the tree structure
     * E.g. Blood pressure - 24 hour average
     *  INTERVAL_EVENT[id1043] occurrences matches {0..1} matches {    -- 24 hour average
     *    ...
     *    data matches {
     *      use_node ITEM_TREE[id9060] /data[id2]/events[id7]/data[id4]
     *    }
     *    state matches {
     *      use_node ITEM_TREE[id9061] /data[id2]/events[id7]/state[id8]
     *    }
     *   }
     * There's also some math_function and duration, which we could maybe add to the description, but we'll stick
     * to using the general description for now.
     * @param curCodebookName name of the current codebook
     * @param child           object with the details
     * @param parentTreeNode  parent's structure node
     */
    private void handleIntervalEvent(String curCodebookName, CObject child, CodebookStructureNode parentTreeNode){
        Archetype archetype = OpenEHRCodebookRepository.getArchetype(curCodebookName);
        // add the current node to the tree (e.g. 24 hour average)
        CodebookStructureNode newParent = createGroupItem(curCodebookName, child, parentTreeNode);
        List<CAttribute> attributeList = child.getAttributes();
        for (CAttribute cAttribute:attributeList){
            switch (cAttribute.getRmAttributeName()){
                case "data", "state" -> {
                    // To be safe we'll assume there can be multiple entries
                    List<CObject> childrenList = cAttribute.getChildren();
                    for (CObject child2:childrenList) {
                        // fetch the targetpath (e.g. /data[id2]/events[id7]/data[id4]) and the CObject for it
                        String targetPath = ((CComplexObjectProxy) child2).getTargetPath();
                        CObject child3 = archetype.itemAtPath(targetPath);
                        // add the item for this object
                        CodebookStructureNode newParent2 = createGroupItem(curCodebookName, child3, newParent);
                        // add its children
                        descendTree(curCodebookName, child3.getAttributes(), newParent2);
                    }
                }
                case "math_function", "width" -> {
                    // do nothing with these
                }
                default -> System.err.println("TBD: "+cAttribute.getRmAttributeName());
            }
        }
    }

    /**
     * The codebook is basically a tree structure. Here we descend this tree recursively and use the information to
     * build our internal codebook.
     * The structure of the openEHR Codebooks are something like this:
     * * CObject, which has
     *    - an rmTypeName, e.g. "OBSERVATION", "ELEMENT", "DV_QUANTITY", etc
     *    - a list with attributes
     *      * Each of these CAttributes has
     *          - an rmAttributeName, e.g. "data", "items", "magnitude", "precision", etc
     *          - a list with children, which are CObjects
     *
     * @param curCodebookName name of the current codebook
     * @param attributeList   items in the current node
     * @param parentTreeNode  parent's structure node
     */
    private void descendTree(String curCodebookName, List<CAttribute> attributeList, CodebookStructureNode parentTreeNode){
        for(CAttribute attribute:attributeList){
            List<CObject> children = attribute.getChildren();
            for(CObject child:children){
                // When we find an ELEMENT, we can build a leaf item
                if(child.getRmTypeName().equalsIgnoreCase("ELEMENT")){
                    createLeafItem(curCodebookName, child, parentTreeNode);
                }
                else if(child.getRmTypeName().equalsIgnoreCase("INTERVAL_EVENT")){
                    handleIntervalEvent(curCodebookName, child, parentTreeNode);
                }
                // Other types of CComplexObject will just be a group item
                else if(child instanceof CComplexObject){
                    CodebookStructureNode newParent = createGroupItem(curCodebookName, child, parentTreeNode);
                    descendTree(curCodebookName, child.getAttributes(), newParent);
                }
                // If the child is an archtypeslot, add the referenced codebooks
                else if(child instanceof ArchetypeSlot archetypeSlot){
                    createArchetypeslotItem(curCodebookName, archetypeSlot, parentTreeNode);
                }
                else{
                    System.err.println(child.getRmTypeName()+": that's an unexpected RmTypeName. Debug this...");
                }
            }
        }
    }

    /**
     * Helper class for handling includes of other codebooks
     */
    static class CodebookIncludes{
        private final Map<String, List<Integer>> refsVersionsMaps = new HashMap<>();
        // e.g. archetype_id/value matches {/openEHR-EHR-CLUSTER\.anatomical_location(-[a-zA-Z0-9_]+)*\.v1\..*/}
        private static final Pattern openEHRNameVersionPattern = Pattern.compile(".*(open.*)(\\d+)\\W+.*");

        CodebookIncludes(List<Assertion> includesList){
            parseIncludeList(includesList);
        }

        /**
         * returns the refsVersionsMap
         * @return the refsVersionsMap
         */
        private Map<String, List<Integer>> getRefsVersionsMaps(){
            return refsVersionsMaps;
        }

        /**
         * Grabs the includes from the assertions and cleans them
         * @param includesList list with the assertions
         */
        private void parseIncludeList(List<Assertion> includesList){
            for(Assertion assertion:includesList){
                String expression = assertion.getStringExpression();
                cleanRefs(expression);
            }
            sortVersions();
        }

        /**
         * Ensure the versions are sorted, which will allow us to use the newest version first
         */
        private void sortVersions(){
            for(List<Integer> valuesList : refsVersionsMaps.values()){
                valuesList.sort(Comparator.reverseOrder());
            }
        }

        /**
         * clean a ref string
         * we end up with a map which contains codebooknames and their versions
         */
        private void cleanRefs(String ref){
            if(ref.equalsIgnoreCase("archetype_id/valuematches{/.*/}")){
                refsVersionsMaps.put("ANY", new ArrayList<>());
            }
            else {
                String[] splitRefs = ref.split("\\|");
                for (String splitRef : splitRefs) {
                    String curRef = splitRef;
                    curRef = curRef.replace("(-[a-zA-Z0-9_]+)*", "");
                    Matcher m = openEHRNameVersionPattern.matcher(curRef);
                    if (m.matches()) {
                        String codebookRef = m.group(1).replace("\\", "");
                        String codebookVersion = m.group(2);
                        if (!refsVersionsMaps.containsKey(codebookRef)) {
                            refsVersionsMaps.put(codebookRef, new ArrayList<>());
                        }
                        refsVersionsMaps.get(codebookRef).add(Integer.parseInt(codebookVersion));
                    }
                }
            }
        }
    }
}
