package icrfgenerator.edc.edc.castorformtypes;

import icrfgenerator.edc.edc.CastorEDC;

public class CastorEDCReport extends CastorEDC {
    public CastorEDCReport(){

    }

    @Override
    //  <reports name="Reports" guid="reports">
    //    <report>
    //      <r_20FD9472-2D34-FFFD-F2C5-BA41807F1031 name="Adverse event" guid="20FD9472-2D34-FFFD-F2C5-BA41807F1031" desc="Adverse events" type="event" stepCount="1">
    //        <report_steps>
    //          <rs_4BF3A700-55D2-4C6B-F710-D171DA9F320A name="Event details" guid="4BF3A700-55D2-4C6B-F710-D171DA9F320A" desc="This report allows you to repetitively collect adverse event data." report_step_number="1">
    protected void buildXMLForFormPart1(StringBuilder stringBuilder, String stepId, String phaseId) {
        // type of report... should you be able to specify this?
        // options: Adverse Event; Event; Medication; Other; Repeated measure; Unscheduled phase
        stringBuilder.
                append("<reports name=\"Reports\" guid=\"reports\">").
                append("<report>").
                append("<r_" + phaseId + " name=\"GeneratedReport\" guid=\"" + phaseId + "\" desc=\"Generated Report\" type=\"other\" stepCount=\"1\">").
                append("<report_steps>").
                append("<rs_" + stepId + " name=\"GeneratedStep\" guid=\"" + stepId + "\" desc=\"Generated Step\" report_step_number=\"1\">");
    }

    @Override
    //            </rs_4BF3A700-55D2-4C6B-F710-D171DA9F320A>
    //        </report_steps>
    //      </r_20FD9472-2D34-FFFD-F2C5-BA41807F1031>
    //    </report>
    //  </reports>
    //  <export_type>report</export_type>
    protected void buildXMLForFormPart2(StringBuilder stringBuilder, String stepId, String phaseId) {
        stringBuilder.
                append("</rs_").append(stepId).append(">").
                append("</report_steps>").
                append("</r_").append(phaseId).append(">").
                append("</report>").
                append("</reports>").
                append("<export_type>report</export_type>");
    }

    @Override
    protected void buildXMLForFormPart3(StringBuilder stringBuilder, String fieldLabel) {
        stringBuilder.
                append("<encryption_enabled>false</encryption_enabled>").
                append("<searchability_enabled>false</searchability_enabled>").
                append("<field_label_parsed>").append(fieldLabel).append("</field_label_parsed>");
    }

}
