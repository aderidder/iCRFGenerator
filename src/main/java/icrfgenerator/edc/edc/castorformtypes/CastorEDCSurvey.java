package icrfgenerator.edc.edc.castorformtypes;


import icrfgenerator.edc.edc.CastorEDC;

public class CastorEDCSurvey extends CastorEDC {
    public CastorEDCSurvey(){

    }

    @Override
    //   <surveys name="Surveys" guid="surveys">
    //    <survey>
    //      <su_A653F85E-AC78-2F4A-B73F-904AA145570D survey_id="A653F85E-AC78-2F4A-B73F-904AA145570D" survey_name="Short Form Health Survey (SF-36)" survey_description="Medical Outcomes Study (MOS) 36-Item Short Form Health Survey" survey_invitation="" survey_invitation_subject="" survey_send_after="0" survey_auto_send="0" survey_reminder="0" survey_required="0" survey_intro_text="This is the second of the two surveys, The Short Form (36) Health Survey is a 36-item, patient-reported survey of patient health." survey_outro_text="You have now filled in both surveys, thank you" show_step_navigator="0" allow_step_navigation="0" force_step_completion="0" survey_finish_url="" survey_sender_name="Martijn Kersloot [Castor EDC]" survey_sender_address="martijn@ciwit.nl" auto_lock_on_finish="0" send_pattern="" name="Short Form Health Survey (SF-36)" guid="A653F85E-AC78-2F4A-B73F-904AA145570D" stepCount="9">
    //        <survey_steps>
    //          <ss_4E9F6689-B70D-5493-E930-420B60664F2F survey_id="A653F85E-AC78-2F4A-B73F-904AA145570D" survey_step_id="4E9F6689-B70D-5493-E930-420B60664F2F" survey_step_name="Energy and emotions" survey_step_description="" survey_step_number="7" name="Energy and emotions" rejected="0" guid="4E9F6689-B70D-5493-E930-420B60664F2F">
    protected void buildXMLForFormPart1(StringBuilder stringBuilder, String stepId, String phaseId) {
        stringBuilder.
                append("<surveys name=\"Surveys\" guid=\"surveys\">").
                append("<survey>").
                append("<su_" + phaseId + " survey_id=\"" + phaseId + "\" survey_name=\"GeneratedSurvey\" " +
                       "survey_description=\"Generated Survey\" survey_invitation=\"\" survey_invitation_subject=\"\" " +
                       "survey_send_after=\"0\" survey_auto_send=\"0\" survey_reminder=\"0\" survey_required=\"0\" " +
                       "survey_intro_text=\"This is a generated survey\" survey_outro_text=\"Thank you\" " +
                       "show_step_navigator=\"0\" allow_step_navigation=\"0\" force_step_completion=\"0\" " +
                       "survey_finish_url=\"\" survey_sender_name=\"Please specify sender name\" " +
                       "survey_sender_address=\"Please specify sender address\" auto_lock_on_finish=\"0\" " +
                       "send_pattern=\"\" name=\"Generated Survey\" " +
                       "guid=\""+phaseId+"\" stepCount=\"1\">").
                append("<survey_steps>").
                append("<ss_" + stepId + " survey_id=\"" + phaseId + "\" survey_step_id=\"" + stepId + "\" " +
                       "survey_step_name=\"GeneratedStep\" survey_step_description=\"\" " +
                       "survey_step_number=\"1\" step_status=\"active\" name=\"GeneratedStep\" " +
                       "guid=\"" + stepId + "\" rejected=\"0\">");
    }

    @Override
    //           </ss_A6920B12-A762-FC62-2791-14A3010873A4>
    //        </survey_steps>
    //      </su_A653F85E-AC78-2F4A-B73F-904AA145570D>
    //    </survey>
    //  </surveys>
    //  <export_type>survey</export_type>
    protected void buildXMLForFormPart2(StringBuilder stringBuilder, String stepId, String phaseId) {
        stringBuilder.
                append("</ss_").append(stepId).append(">").
                append("</survey_steps>").
                append("</su_").append(phaseId).append(">").
                append("</survey>").
                append("</surveys>").
                append("<export_type>survey</export_type>");
    }

    @Override
    protected void buildXMLForFormPart3(StringBuilder stringBuilder, String fieldLabel) {
        // nothing special here
    }

}
