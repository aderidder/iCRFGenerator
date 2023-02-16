package icrfgenerator.codebook.openehr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * not sure whether we'll be using this
 * TBD, but I'll keep it around for now
 */
public class OpenEHRStatics {
    static final Map<String,String> propertyMap;
    private static final Map<String,String> nullMap;
    static final List<String> sectionList = Arrays.asList("concept", "language", "description", "definition", "ontology");

    static{
        // https://specifications.openehr.org/releases/1.0.2/architecture/terminology.pdf
        //
        // TODO: update using https://github.com/openEHR/terminology/blob/master/openEHR_RM/en/openehr_terminology.xml
        nullMap = new HashMap<>();
        nullMap.put("271", "No information");
        nullMap.put("253", "Unknown");
        nullMap.put("272", "Masked");
        nullMap.put("273", "Not applicable");

        propertyMap = new HashMap<>();
        propertyMap.put("339", "Acceleration");
        propertyMap.put("342", "Acceleration, angular");
        propertyMap.put("381", "Amount (Eq)");
        propertyMap.put("384", "Amount (mole)");
        propertyMap.put("497", "Angle, plane");
        propertyMap.put("500", "Angle, solid");
        propertyMap.put("335", "Area");
        propertyMap.put("350", "Density");
        propertyMap.put("362", "Diffusion coefficient");
        propertyMap.put("501", "Electrical capacitance");
        propertyMap.put("498", "Electrical charge");
        propertyMap.put("502", "Electrical conductance");
        propertyMap.put("334", "Electrical current");
        propertyMap.put("377", "Electrical field strength");
        propertyMap.put("121", "Energy");
        propertyMap.put("366", "Energy density");
        propertyMap.put("508", "Energy dose");
        propertyMap.put("365", "Energy per area");
        propertyMap.put("347", "Flow rate, mass");
        propertyMap.put("352", "Flow rate, mass/force");
        propertyMap.put("351", "Flow rate, mass/volume");
        propertyMap.put("126", "Flow rate, volume");
        propertyMap.put("348", "Flux, mass");
        propertyMap.put("355", "Force");
        propertyMap.put("357", "Force, body");
        propertyMap.put("382", "Frequency");
        propertyMap.put("373", "Heat transfer coefficient");
        propertyMap.put("505", "Illuminance");
        propertyMap.put("379", "Inductance");
        propertyMap.put("122", "Length");
        propertyMap.put("499", "Light intensity");
        propertyMap.put("123", "Loudness");
        propertyMap.put("504", "Luminous flux");
        propertyMap.put("378", "Magnetic flux");
        propertyMap.put("503", "Magnetic flux density");
        propertyMap.put("124", "Mass");
        propertyMap.put("385", "Mass (IU)");
        propertyMap.put("349", "Mass per area");
        propertyMap.put("344", "Moment inertia, area");
        propertyMap.put("345", "Moment inertia, mass");
        propertyMap.put("340", "Momentum");
        propertyMap.put("346", "Momentum, flow rate");
        propertyMap.put("343", "Momentum, angular");
        propertyMap.put("369", "Power density");
        propertyMap.put("368", "Power flux");
        propertyMap.put("367", "Power, linear");
        propertyMap.put("125", "Pressure");
        propertyMap.put("507", "Proportion");
        propertyMap.put("380", "Qualified real");
        propertyMap.put("506", "Radioactivity");
        propertyMap.put("375", "Resistance");
        propertyMap.put("370", "Specific energy");
        propertyMap.put("371", "Specific heat, gas content");
        propertyMap.put("337", "Specific surface");
        propertyMap.put("336", "Specific volume");
        propertyMap.put("356", "Surface tension");
        propertyMap.put("127", "Temperature");
        propertyMap.put("128", "Time");
        propertyMap.put("338", "Velocity");
        propertyMap.put("341", "Velocity, angular");
        propertyMap.put("360", "Velocity, dynamic");
        propertyMap.put("361", "Velocity, kinematic");
        propertyMap.put("374", "Voltage, electrical");
        propertyMap.put("129", "Volume");
        propertyMap.put("130", "Work");
    }
}
