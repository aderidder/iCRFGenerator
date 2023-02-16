package icrfgenerator.codebook.openehr;

import com.nedap.archie.adlparser.ADLParseException;
import com.nedap.archie.adlparser.ADLParser;
import com.nedap.archie.aom.*;
import com.nedap.archie.aom.terminology.ArchetypeTerm;
import com.nedap.archie.aom.terminology.ArchetypeTerminology;
import com.nedap.archie.aom.terminology.ValueSet;
import com.nedap.archie.archetypevalidator.ValidationResult;
import com.nedap.archie.flattener.InMemoryFullArchetypeRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openehr.referencemodels.BuiltinReferenceModels;

import icrfgenerator.settings.GlobalSettings;
import icrfgenerator.utils.RestCalls;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Archetype repository and helper functions to retrieve data from these archetypes
 */
public class OpenEHRCodebookRepository {
    private static final Logger logger = LogManager.getLogger(OpenEHRCodebookRepository.class.getName());
    private static final InMemoryFullArchetypeRepository repository = new InMemoryFullArchetypeRepository();
    private static final HashMap<String, Archetype> nameToFlattenedArchetype = new HashMap<>();

    /**
     * attempts to add a new codebook to the repository if it is not yet available there
     * @param codebookName name of the codebook to add
     */
    static void addCodebook(String codebookName){
        if(!nameToFlattenedArchetype.containsKey(codebookName)){
            try {
                parseFile(codebookName, getFile(codebookName));
            } catch (IOException | ADLParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the codebook file
     * e.g. <a href="https://ckm.openehr.org/ckm/retrieveArchetype?cid-archetype=1013.1.1631&format=ADL2">...</a>
     * @param codebookName name of the codebook we're looking for
     * @return codebook file
     */
    private static File getFile(String codebookName){
//        String uri = GlobalSettings.getServer(codebookName)+"retrieveArchetype?cid-archetype="+GlobalSettings.getCodebookPrefix(codebookName)+"&format=ADL2";
        String uri = getCodebookURI(GlobalSettings.getServer(codebookName), GlobalSettings.getCodebookPrefix(codebookName));
        String dir = GlobalSettings.getCacheDir()+ File.separator;
        String fileName = dir+codebookName+".adls";
        return RestCalls.getFile(uri, fileName);
    }

    /**
     * attempts to add a reference codebook to the repository
     * @param parentCodebook  the top parent, which we need to determine the server we can find the reference codebook
     * @param refCodebookName name of the reference codebook
     * @param refCodebookId   id of the reference codebook
     */
    static void addRefCodebook(String parentCodebook, String refCodebookName, String refCodebookId){
        if(!nameToFlattenedArchetype.containsKey(refCodebookName)){
            try {
                parseFile(refCodebookName, getRefFile(parentCodebook, refCodebookName, refCodebookId));
            } catch (IOException | ADLParseException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the codebook file for a reference codebook
     * @param parentCodebook  the top parent, which can tell us the server to use
     * @param refCodebookName name of the codebook we're looking for
     * @param refCodebookId   id of the codebook we're looking for
     * @return codebook file
     */
    private static File getRefFile(String parentCodebook, String refCodebookName, String refCodebookId){
        String uri = getCodebookURI(GlobalSettings.getServer(parentCodebook), refCodebookId);
        String dir = GlobalSettings.getCacheDir()+ File.separator;
        String fileName = dir+refCodebookName+".adls";
        return RestCalls.getFile(uri, fileName);
    }


    /**
     * returns the full URI which we can use to fetch the codebook
     * @param codebookServer server where the codebook can be found
     * @param codebookId     id of the codebook we're looking for
     * @return uri to download the codebook
     */
    private static String getCodebookURI(String codebookServer, String codebookId){
        return codebookServer+"retrieveArchetype?cid-archetype="+codebookId+"&format=ADL2";
    }

    /**
     * Parse a codebook
     * @param codebookName name of the codebook
     * @param file         the file to parse
     * @throws IOException issue with the file
     * @throws ADLParseException issue with the file
     */
    private static void parseFile(String codebookName, File file) throws IOException, ADLParseException {
        ADLParser parser = new ADLParser();
        try (InputStream stream = new FileInputStream(file)) {
            Archetype archetype = parser.parse(stream);

            repository.addArchetype(archetype);

            // the getmetamodels give 2 token recognition errors, apparently cause by antlr
            // maybe intercept what happens (pipedoutpustream?)
            // or we can temporarily write err to null, but that's kinda dubious.
            PrintStream printStream = System.err;
            System.setErr(System.out);
            repository.compile(BuiltinReferenceModels.getMetaModels());
            System.setErr(printStream);

            for(ValidationResult result:repository.getAllValidationResults()) {
                if(!result.passes()){
                    logger.error("There was an issue with the codebook validation. CodebookName = "+codebookName);
                }
                else{
                    nameToFlattenedArchetype.put(codebookName, repository.getFlattenedArchetype(archetype.getArchetypeId().getFullId()));
                }
            }
        }
    }

    /**
     * attempt to find the id for a codebookname
     * @param parentCodebook  the top parent, which can tell us the server to use
     * @param refCodebookName name of the codebook we're looking for
     * @return the identifier
     */
    static String getRefCodebookId(String parentCodebook, String refCodebookName){
        String refCodebookId;
        String cidUri = GlobalSettings.getServer(parentCodebook)+"rest/v1/archetypes/citeable-identifier/"+refCodebookName;
        try {
            refCodebookId = RestCalls.generalCall(cidUri);
        } catch (IOException e) {
            return "";
        }
        return refCodebookId;
    }

    /**
     * returns an item's description
     * @param codebook codebook
     * @param cObject  the object with the details for which we want the description
     * @param language language for the description
     * @return description in the language
     */
    static String getItemDescription(String codebook, CObject cObject, String language){
        ArchetypeTerm archetypeTerm = getArchetype(codebook).getTerm(cObject, language);
        if (archetypeTerm==null || archetypeTerm.getDescription() ==null){
            return "NO_DESCRIPTION";
        }
        return archetypeTerm.getDescription();
    }

    /**
     * returns an item's description
     * @param codebook codebook
     * @param code     code for which to obtain the description
     * @param language language for the description
     * @return description in the language
     */
    static String getItemDescription(String codebook, String code, String language){
        ArchetypeTerminology archetypeTerminology = getArchetype(codebook).getTerminology();
        ArchetypeTerm archetypeTerm = archetypeTerminology.getTermDefinition(language, code);
        if (archetypeTerm==null || archetypeTerm.getDescription() ==null){
            return "NO_DESCRIPTION";
        }
        return archetypeTerm.getDescription();
    }

    /**
     * returns an item's text
     * @param codebook codebook
     * @param cObject  the object with the details for which we want the text
     * @param language language for the text
     * @return text in the language
     */
    static String getItemText(String codebook, CObject cObject, String language){
        ArchetypeTerm archetypeTerm = getArchetype(codebook).getTerm(cObject, language);
        if (archetypeTerm==null || archetypeTerm.getText() ==null){
            return "NO_TEXT";
        }
        return archetypeTerm.getText();
    }
    /**
     * returns an item's text
     * @param codebook codebook
     * @param code     code for which to obtain the text
     * @param language language for the text
     * @return text in the language
     */
    static String getItemText(String codebook, String code, String language){
        ArchetypeTerminology archetypeTerminology = getArchetype(codebook).getTerminology();
        ArchetypeTerm archetypeTerm = archetypeTerminology.getTermDefinition(language, code);
        if (archetypeTerm==null || archetypeTerm.getText() ==null){
            return "NO_TEXT";
        }
        return archetypeTerm.getText();
    }

    /**
     * returns and item's term bindings, which is e.g.
     * openehr -> at9000 --> openehr.org/id/125
     *            at9056 --> openehr.org/id/146
     * snomed-ct -> etc.
     * @param codebook codebook
     * @return map with maps with the bindings
     */
    static Map<String, Map<String, URI>> getTermBindings(String codebook){
         return getArchetype(codebook).getTerminology().getTermBindings();
    }

    /**
     * returns nodeId
     * @param cObject object for which to return the id
     * @return the object's id
     */
    static String getItemID(CObject cObject){
        return cObject.getNodeId();
    }

    /**
     * retuns the codebook's languages
     * @param codebookName name of the codebook
     * @return the codebook's languages
     */
    static List<String> getLanguages(String codebookName){
        ArchetypeTerminology archetypeTerminology = getArchetype(codebookName).getTerminology();
        return new ArrayList<>(archetypeTerminology.getTermDefinitions().keySet());
    }

    /**
     * returns the codebook's version
     * @param codebookName name of the codebook
     * @return version of the codebook
     */
    static String getVersion(String codebookName){
        return getArchetype(codebookName).getArchetypeId().getReleaseVersion();
    }

    /**
     * returns the codebook's terminology valueset
     * @param codebookName name of the codebook
     * @return the terminology valyeset
     */
    static Map<String, ValueSet> getTerminologyValueSets(String codebookName){
        return getArchetype(codebookName).getTerminology().getValueSets();
    }

    /**
     * returns the codebook's archetype
     * @param codebookName name of the codebook
     * @return the archetype
     */
    static Archetype getArchetype(String codebookName){
        return  nameToFlattenedArchetype.get(codebookName);
    }

}
