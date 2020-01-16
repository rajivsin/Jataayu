package `in`.org.projecteka.jataayu.provider.viewmodel

import `in`.org.projecteka.jataayu.core.model.Hip
import `in`.org.projecteka.jataayu.core.model.ProviderInfo
import `in`.org.projecteka.jataayu.provider.model.LinkAccountsResponse
import `in`.org.projecteka.jataayu.provider.model.PatientDiscoveryResponse
import `in`.org.projecteka.jataayu.provider.repository.ProviderRepository
import `in`.org.projecteka.jataayu.util.extension.EMPTY
import `in`.org.projecteka.jataayu.util.extension.liveDataOf
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class ProviderSearchViewModel(val providerRepository: ProviderRepository) : ViewModel() {
    val mobile = "9876543210"
    val providers = liveDataOf<List<ProviderInfo>>()
    var providersList = emptyList<ProviderInfo>()
    val patientDiscoveryResponse = liveDataOf<PatientDiscoveryResponse>()
    val linkAccountsResponse = liveDataOf<LinkAccountsResponse>()

    internal var selectedProviderName = String.EMPTY

    fun getProviders(query: String) {
        if (providersList.isEmpty()){
            providerRepository.getProviders(query).enqueue(object : Callback<List<ProviderInfo>?> {
                override fun onFailure(call: Call<List<ProviderInfo>?>, t: Throwable) {
                    Timber.e(t, "Failed to get providers list")
                }
                override fun onResponse(call: Call<List<ProviderInfo>?>, response: Response<List<ProviderInfo>?>) {
                    response.body()?.let {
                        providers.value = it
                        providersList = it.toList()
                    }
                 }
            })
        } else{
            providers.postValue(providersList.filter { it.hip.name.contains(query, true) })
        }
    }

    fun getPatientAccounts(hip: Hip) {
        providerRepository.getPatientAccounts(hip)
            .enqueue(object : Callback<PatientDiscoveryResponse> {
                override fun onFailure(call: Call<PatientDiscoveryResponse>, t: Throwable) {
                    Timber.e(t, "Failed to get patients accounts")
                }

                override fun onResponse(call: Call<PatientDiscoveryResponse>, response: Response<PatientDiscoveryResponse>) {
                    response.body()?.let { patientDiscoveryResponse.value = it }
                }

            })
    }

    fun linkPatientAccounts(patientDiscoveryResponse: PatientDiscoveryResponse) {
        providerRepository.linkPatientAccounts(patientDiscoveryResponse)
            .enqueue(object: Callback<LinkAccountsResponse>{
                override fun onFailure(call: Call<LinkAccountsResponse>, t: Throwable) {
                    Timber.e(t, "Failed to link patients accounts")
                }

                override fun onResponse(call: Call<LinkAccountsResponse>, response: Response<LinkAccountsResponse>) {
                    response.body()?.let { linkAccountsResponse.value = it }
                }
            })
    }

    fun canLinkAccounts() : Boolean{
        for (careContext in patientDiscoveryResponse?.value?.patient?.careContexts!!) {
            if(careContext.contextChecked) {
                return true
            }
        }
        return false
    }

    fun clearList() {
        providersList = emptyList()
    }
}

