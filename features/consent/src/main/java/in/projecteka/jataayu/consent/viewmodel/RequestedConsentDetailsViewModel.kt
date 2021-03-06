package `in`.projecteka.jataayu.consent.viewmodel

import `in`.projecteka.jataayu.consent.repository.ConsentRepository
import `in`.projecteka.jataayu.core.model.*
import `in`.projecteka.jataayu.core.model.approveconsent.CareReference
import `in`.projecteka.jataayu.core.model.approveconsent.ConsentArtifact
import `in`.projecteka.jataayu.core.model.approveconsent.ConsentArtifactRequest
import `in`.projecteka.jataayu.core.model.approveconsent.ConsentArtifactResponse
import `in`.projecteka.jataayu.network.utils.PayloadLiveData
import `in`.projecteka.jataayu.network.utils.fetch
import `in`.projecteka.jataayu.presentation.ui.viewmodel.BaseViewModel
import `in`.projecteka.jataayu.util.extension.EMPTY
import `in`.projecteka.jataayu.util.repository.CredentialsRepository
import `in`.projecteka.jataayu.util.repository.PreferenceRepository
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MediatorLiveData

class RequestedConsentDetailsViewModel(private val repository: ConsentRepository,
                                       val preferenceRepository: PreferenceRepository,
                                       private val credentialsRepository: CredentialsRepository) : BaseViewModel() {

    val linkedAccountsResponse = PayloadLiveData<LinkedAccountsResponse>()
    val consentArtifactResponse = PayloadLiveData<ConsentArtifactResponse>()
    val consentDenyResponse = PayloadLiveData<Void>()
    var grantConsentAfterGettingLinkedAccountsEvent = ObservableBoolean(false)

    internal var selectedProviderName = String.EMPTY

    fun getLinkedAccounts() =
        linkedAccountsResponse.fetch(repository.getLinkedAccounts())

    fun grantConsent(
        requestId: String,
        consentArtifacts: List<ConsentArtifact>
    ) =
        consentArtifactResponse.fetch(repository.grantConsent(requestId, getConsentArtifactRequest(consentArtifacts), credentialsRepository.consentTemporaryToken))

    fun getConsentArtifactRequest(consentArtifacts: List<ConsentArtifact>) = ConsentArtifactRequest(consentArtifacts)

    fun getConsentArtifact(
        links: List<Links>,
        hiTypeObjects: ArrayList<HiType>,
        permission: Permission
    ): List<ConsentArtifact> {

        val consentArtifactList = ArrayList<ConsentArtifact>()
        val hiTypes: List<String> = hiTypeObjects.mapNotNull { if(it.isChecked) it.type else null }


        links.forEach { link ->
            val careReferences = ArrayList<CareReference>()
            link.careContexts.forEach { careContext ->
                if (careContext.contextChecked == true) careReferences.add(newCareReference(link, careContext))
            }

            if (careReferences.isNotEmpty()) {
                consentArtifactList.add(
                    ConsentArtifact(hiTypes, link.hip, careReferences, permission)
                )
            }
        }
        return consentArtifactList
    }

    fun denyConsent(requestId: String){
        consentDenyResponse.fetch(repository.denyConsent(requestId))
    }

    fun fetchHipHiuNamesOf(idList: List<HipHiuIdentifiable>): MediatorLiveData<HipHiuNameResponse> {
        return repository.getProviderBy(idList)
    }

    fun canShowBottomButtons(status: RequestStatus): Boolean {
        return when(status) {
            RequestStatus.EXPIRED, RequestStatus.DENIED -> false
            else -> true
        }
    }

    private fun newCareReference(link: Links, it: CareContext) = CareReference(link.referenceNumber, it.referenceNumber)
}

