package icrfgenerator.edc.edc.castorformtypes;


import icrfgenerator.edc.edc.CastorEDC;

public class CastorEDCStep extends CastorEDC {
    public CastorEDCStep(){

    }

    @Override
    protected void buildXMLForFormPart1(StringBuilder stringBuilder, String stepId, String phaseId) {
        stringBuilder.
                append("<study name=\"Study\" guid=\"study\">").
                append("<phases>").
                append("<ph_" + phaseId + " name=\"GeneratedPhase\" guid=\"" + phaseId + "\" duration=\"0\" order=\"1\" rejected=\"0\" stepCount=\"1\">").
                append("<steps>").
                append("<st_" + stepId + " step_id=\"" + stepId + "\" study_phase_id=\"" + phaseId + "\" step_number=\"1\" step_name=\"GeneratedStep\" step_desc=\"GeneratedStep\" step_status=\"active\" name=\"GeneratedStep\" guid=\"" + stepId + "\" rejected=\"0\">");
    }

    @Override
    protected void buildXMLForFormPart2(StringBuilder stringBuilder, String stepId, String phaseId) {
        stringBuilder.
                append("</st_").append(stepId).append(">").
                append("</steps>").
                append("</ph_").append(phaseId).append(">").
                append("</phases>").
                append("</study>").
                append("<export_type>step</export_type>");
    }

    @Override
    protected void buildXMLForFormPart3(StringBuilder stringBuilder, String fieldLabel) {
        // nothing special here
        stringBuilder.append("<encryption_enabled>false</encryption_enabled>").
        append("<field_label_parsed>").append(fieldLabel).append("</field_label_parsed>");
    }

}
