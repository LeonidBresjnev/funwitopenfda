package com.openfda.funwitopenfda

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class OpenFDAEntry(
    val meta: Meta,
    val results: List<OpenFDAResultEntry>
)

@Serializable
data class Meta(
    val disclaimer: String,
    val terms: String,
    val license: String,
    val last_updated: String,
    val results: MetaResults
)

@Serializable
data class MetaResults(
    val total: Int,
    val limit: Int,
    val skip: Int
)

@Serializable
data class OpenFDAResultEntry(
    val abuse: List<String> = emptyList(),
    val controlled_substance: List<String> = emptyList(),
    val dependence: List<String> = emptyList(),
    val drug_abuse_and_dependence: List<String> = emptyList(),
    val overdosage: List<String> = emptyList(),
    val adverse_reactions: List<String> = emptyList(),
    val drug_and_or_laboratory_test_interactions: List<String> = emptyList(),
    val drug_interactions: List<String> = emptyList(),
    val clinical_pharmacology: List<String> = emptyList(),
    val mechanism_of_action: List<String> = emptyList(),
    val pharmacodynamics: List<String> = emptyList(),
    val pharmacokinetics: List<String> = emptyList(),
    val effective_time: String = "",
    val id: String="",
    val set_id: String = "",
    val version: String = "",
    val active_ingredient: List<String> = emptyList(),
    val contraindications: List<String> = emptyList(),
    val description: List<String> = emptyList(),
    val dosage_and_administration: List<String> = emptyList(),
    val dosage_forms_and_strengths: List<String> = emptyList(),
    val inactive_ingredient: List<String> = emptyList(),
    val indications_and_usage: List<String> = emptyList(),
    val purpose: List<String> = emptyList(),
    val spl_product_data_elements: List<String> = emptyList(),
    val animal_pharmacology_and_or_toxicology: List<String> = emptyList(),
    val carcinogenesis_and_mutagenesis_and_impairment_of_fertility: List<String> = emptyList(),
    val nonclinical_toxicology: List<String> = emptyList(),
    val laboratory_tests: List<String> = emptyList(),
    val microbiology: List<String> = emptyList(),
    val package_label_principal_display_panel: List<String> = emptyList(),
    val recent_major_changes: List<String> = emptyList(),
    val spl_unclassified_section: List<String> = emptyList(),
    val ask_doctor: List<String> = emptyList(),
    val ask_doctor_or_pharmacist: List<String> = emptyList(),
    val do_not_use: List<String> = emptyList(),
    val information_for_owners_or_caregivers: List<String> = emptyList(),
    val information_for_patients: List<String> = emptyList(),
    val instructions_for_use: List<String> = emptyList(),
    val keep_out_of_reach_of_children: List<String> = emptyList(),
    val other_safety_information: List<String> = emptyList(),
    val patient_medication_information: List<String> = emptyList(),
    val questions: List<String> = emptyList(),
    val spl_medguide: List<String> = emptyList(),
    val spl_patient_package_insert: List<String> = emptyList(),
    val stop_use: List<String> = emptyList(),
    val when_using: List<String> = emptyList(),
    val clinical_studies: List<String> = emptyList(),
    val references: List<String> = emptyList(),
    val geriatric_use: List<String> = emptyList(),
    val labor_and_delivery: List<String> = emptyList(),
    val nursing_mothers: List<String> = emptyList(),
    val pediatric_use: List<String> = emptyList(),
    val pregnancy: List<String> = emptyList(),
    val pregnancy_or_breast_feeding: List<String> = emptyList(),
    val teratogenic_effects: List<String> = emptyList(),
    val use_in_specific_populations: List<String> = emptyList(),
    val how_supplied: List<String> = emptyList(),
    val safe_handling_warning: List<String> = emptyList(),
    val storage_and_handling: List<String> = emptyList(),
    val boxed_warning: List<String> = emptyList(),
    val general_precautions: List<String> = emptyList(),
    val precautions: List<String> = emptyList(),
    val user_safety_warnings: List<String> = emptyList(),
    val warnings: List<String> = emptyList(),
    val warnings_and_cautions: List<String> = emptyList(),


/*
    val description: List<String> = emptyList(),
    val microbiology: List<String> = emptyList(),
    val indications_and_usage: List<String> = emptyList(),
    val dosage_and_administration: List<String> = emptyList(),
    val dosage_forms_and_strengths: List<String> = emptyList(),
    val contraindications: List<String> = emptyList(),
    val warnings_and_cautions: List<String> = emptyList(),
    val adverse_reactions: List<String> = emptyList(),
    val drug_interactions: List<String> = emptyList(),
    val use_in_specific_populations: List<String> = emptyList(),
    val pregnancy: List<String> = emptyList(),
    val pediatric_use: List<String> = emptyList(),
    val geriatric_use: List<String> = emptyList(),
    val overdosage: List<String> = emptyList(),
    val clinical_pharmacology: List<String> = emptyList(),
    val mechanism_of_action: List<String> = emptyList(),
    val pharmacodynamics: List<String> = emptyList(),
    val pharmacokinetics: List<String> = emptyList(),
    val nonclinical_toxicology: List<String> = emptyList(),
    val carcinogenesis_and_mutagenesis_and_impairment_of_fertility: List<String> = emptyList(),
    val animal_pharmacology_and_or_toxicology: List<String> = emptyList(),
    val clinical_studies: List<String> = emptyList(),
    val how_supplied: List<String> = emptyList(),
    val storage_and_handling: List<String> = emptyList(),
    val information_for_patients: List<String> = emptyList(),
    val spl_medguide: List<String> = emptyList(),
    val package_label_principal_display_panel: List<String> = emptyList(),
    val spl_patient_package_insert: List<String> = emptyList(),
    val spl_unclassified_section: List<String> = emptyList(),
    val recent_major_changes: List<String> = emptyList(),*/


    val abuse_table : List<String> = emptyList(),
    val controlled_substance_table: List<String> = emptyList(),
    val dependence_table: List<String> = emptyList(),
    val drug_abuse_and_dependence_table: List<String> = emptyList(),
    val overdosage_table: List<String> = emptyList(),
    val adverse_reactions_table: List<String> = emptyList(),
    val drug_and_or_laboratory_test_interactions_table: List<String> = emptyList(),
    val drug_interactions_table: List<String> = emptyList(),
    val clinical_pharmacology_table: List<String> = emptyList(),
    val mechanism_of_action_table: List<String> = emptyList(),
    val pharmacodynamics_table: List<String> = emptyList(),
    val pharmacokinetics_table: List<String> = emptyList(),
    val active_ingredient_table: List<String> = emptyList(),
    val contraindications_table: List<String> = emptyList(),
    val description_table: List<String> = emptyList(),
    val dosage_and_administration_table: List<String> = emptyList(),
    val dosage_forms_and_strengths_table: List<String> = emptyList(),
    val inactive_ingredient_table: List<String> = emptyList(),
    val indications_and_usage_table: List<String> = emptyList(),
    val spl_product_data_elements_table: List<String> = emptyList(),
    val animal_pharmacology_and_or_toxicology_table: List<String> = emptyList(),
    val carcinogenesis_and_mutagenesis_and_impairment_of_fertility_table: List<String> = emptyList(),
    val nonclinical_toxicology_table: List<String> = emptyList(),
    val laboratory_tests_table: List<String> = emptyList(),
    val microbiology_table: List<String> = emptyList(),
    val package_label_principal_display_panel_table: List<String> = emptyList(),
    val recent_major_changes_table: List<String> = emptyList(),
    val spl_unclassified_section_table: List<String> = emptyList(),
    val ask_doctor_or_pharmacist_table: List<String> = emptyList(),
    val information_for_owners_or_caregivers_table: List<String> = emptyList(),
    val information_for_patients_table: List<String> = emptyList(),
    val keep_out_of_reach_of_children_table: List<String> = emptyList(),
    val other_safety_information_table: List<String> = emptyList(),
    val patient_medication_information_table: List<String> = emptyList(),
    val questions_table: List<String> = emptyList(),
    val spl_medguide_table: List<String> = emptyList(),
    val spl_patient_package_insert_table: List<String> = emptyList(),
    val stop_use_table: List<String> = emptyList(),
    val when_using_table: List<String> = emptyList(),
    val clinical_studies_table: List<String> = emptyList(),
    val references_table: List<String> = emptyList(),
    val geriatric_use_table: List<String> = emptyList(),
    val labor_and_delivery_table: List<String> = emptyList(),
    val nursing_mothers_table: List<String> = emptyList(),
    val pediatric_use_table: List<String> = emptyList(),
    val pregnancy_or_breast_feeding_table: List<String> = emptyList(),
    val pregnancy_table: List<String> = emptyList(),
    val teratogenic_effects_table: List<String> = emptyList(),
    val use_in_specific_populations_table: List<String> = emptyList(),
    val safe_handling_warning_table: List<String> = emptyList(),
    val storage_and_handling_table: List<String> = emptyList(),
    val boxed_warning_table: List<String> = emptyList(),
    val general_precautions_table: List<String> = emptyList(),
    val precautions_table: List<String> = emptyList(),
    val user_safety_warnings_table: List<String> = emptyList(),
    val warnings_table: List<String> = emptyList(),


    val warnings_and_cautions_table: List<String> = emptyList(),
    val how_supplied_table: List<String> = emptyList(),
/*
    val clinical_pharmacology_table: List<String> = emptyList(),
    val microbiology_table: List<String> = emptyList(),
    val how_supplied_table: List<String> = emptyList(),
    val dosage_and_administration_table: List<String> = emptyList(),
    val dosage_forms_and_strengths_table: List<String> = emptyList(),
    val adverse_reactions_table: List<String> = emptyList(),
    val clinical_studies_table: List<String> = emptyList(),
    val spl_patient_package_insert_table: List<String> = emptyList(),
    val warnings_and_cautions_table: List<String> = emptyList(),
    val drug_interactions_table: List<String> = emptyList(),
    val spl_medguide_table: List<String> = emptyList(),
    val spl_unclassified_section_table: List<String> = emptyList(),
    val recent_major_changes_table: List<String> = emptyList(),*/
    val openfda: OpenFDA
) {
    @OptIn(ExperimentalUuidApi::class)
    @Transient val key= Uuid.random()
}

@Serializable
data class OpenFDA(
    val brand_name: List<String> = emptyList(),
    val generic_name: List<String> = emptyList(),
    val substance_name: List<String> = emptyList(),
    val manufacturer_name: List<String> = emptyList(),
    val route: List<String> = emptyList(),
    val product_type: List<String> = emptyList(),
    val product_ndc: List<String> = emptyList(),
    val pharm_class_cs: List<String> = emptyList(),
    val pharm_class_epc: List<String> = emptyList(),
    val pharm_class_pe: List<String> = emptyList(),
    val pharm_class_moa: List<String> = emptyList(),
)

@Serializable
data class OpenFdaNdc(
    val meta: Meta,
    val results: List<OpenFdaNdcResults> =emptyList()
)

@Serializable
data class OpenFdaNdcResults(
    val product_ndc: String,
    val generic_name: String,
    val brand_name: String,
    val finished: Boolean,
    val dosage_form: String,
    val active_ingredients: List<Active_ingredients> = emptyList(),
    val packaging: List<NdcPackage> = emptyList()

)

@Serializable
data class NdcPackage(
    val package_ndc: String,
    val description: String,
    val sample: Boolean=false
)

@Serializable
data class Active_ingredients(
    val name: String,
    val strength: String
)