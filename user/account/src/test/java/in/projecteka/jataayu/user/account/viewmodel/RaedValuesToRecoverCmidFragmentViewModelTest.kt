package `in`.projecteka.jataayu.user.account.viewmodel

import `in`.projecteka.jataayu.core.model.GenerateOTPResponse
import `in`.projecteka.jataayu.core.repository.UserAccountsRepository
import `in`.projecteka.jataayu.util.repository.PreferenceRepository
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.gson.Gson
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@RunWith(MockitoJUnitRunner::class)
class RaedValuesToRecoverCmidFragmentViewModelTest {

    private lateinit var viewModel: ReadValuesFragmentViewModel

    @Mock
    private lateinit var repository: UserAccountsRepository

    @Mock
    private lateinit var preferenceRepository: PreferenceRepository

    @get:Rule
    val taskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var generateOTPResponseCall: Call<GenerateOTPResponse>


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        viewModel = ReadValuesFragmentViewModel(repository, preferenceRepository)
    }

    @After
    fun tearDown() {
        Mockito.verifyNoMoreInteractions(repository, generateOTPResponseCall)
        Mockito.validateMockitoUsage()
    }

    @Test
    fun `should set error flag when name is empty`() {
        viewModel.inputFullName.set("")
        viewModel.validateName()
        assertEquals(true, viewModel.showErrorName.get())
    }

    @Test
    fun `should not set error flag when name is not empty`() {
        viewModel.inputFullName.set("Anu")
        viewModel.validateName()
        assertEquals(false, viewModel.showErrorName.get())
    }

    @Test
    fun `should set error flag when mobile is empty`() {
        viewModel.inputMobileNumber.set("")
        viewModel.validateMobileNumber()
        assertEquals(true, viewModel.showErrorMobile.get())
    }

    @Test
    fun `should not set error flag when mobile is not empty`() {
        viewModel.inputMobileNumber.set("1234567890")
        viewModel.validateMobileNumber()
        assertEquals(false, viewModel.showErrorMobile.get())
    }

    @Test
    fun `should set error flag when gender is not checked`() {
        viewModel.onCheckedChanged(null, -1)
        assertEquals(true, viewModel.showErrorGender.get())
    }

    @Test
    fun `should enable submit button when all the required fields entered`() {
        viewModel.inputFullName.set("Anu")
        viewModel.inputMobileNumber.set("1234567890")
        viewModel.onCheckedChanged(null, 2)
        assertEquals(true,  viewModel.validateFields())
        assertEquals(true, viewModel.submitEnabled.get())
    }


    @Test
    fun `should disable submit button when gender not selected`() {
        viewModel.inputFullName.set("Anu")
        viewModel.inputMobileNumber.set("1234567890")
        assertEquals(false,  viewModel.validateFields())
        assertEquals(false, viewModel.submitEnabled.get())
    }

    @Test
    fun `should disable submit button when name not entered`() {
        viewModel.inputMobileNumber.set("1234567890")
        viewModel.onCheckedChanged(null, 2)
        assertEquals(false,  viewModel.validateFields())
        assertEquals(false, viewModel.submitEnabled.get())
    }

    @Test
    fun `should disable submit button when mobile number not entered`() {
        viewModel.inputFullName.set("")
        viewModel.onCheckedChanged(null, 2)
        assertEquals(false,  viewModel.validateFields())
        assertEquals(false, viewModel.submitEnabled.get())
    }

    @Test
    fun `should create correct recover cmid payload for entered inputs`() {
        val fullName = "Anu"
        val mobile = "9876543210"
        val ayushmanId = "PAyush123"
        viewModel.inputFullName.set(fullName)
        viewModel.inputMobileNumber.set(mobile)
        viewModel.inputAyushmanIdLbl.set(ayushmanId)
        viewModel.selectedYoB(1977)
        viewModel.onCheckedChanged(null, 2)

        val recoverCmidRequest = viewModel.getRecoverCmidPayload()
        assertEquals(fullName, recoverCmidRequest.name)
        assertEquals(viewModel.countryCode.get() + mobile, recoverCmidRequest.verifiedIdentifiers?.get(0)?.value)
        assertEquals(ayushmanId.toUpperCase(), recoverCmidRequest.unverifiedIdentifiers?.get(0)?.value)
        assertEquals("O", recoverCmidRequest.gender)
        assertEquals(1977, recoverCmidRequest.yearOfBirth)
    }

    @Test
    fun `should return the session and trigger the otp when cm id found`() {
        val fullName = "Anu"
        val mobile = "9876543210"
        val ayushmanId = "PAyush123"
        viewModel.inputFullName.set(fullName)
        viewModel.inputMobileNumber.set(mobile)
        viewModel.inputAyushmanIdLbl.set(ayushmanId)
        viewModel.selectedYoB(1977)
        viewModel.onCheckedChanged(null, 2)
        val recoverCmidRequest = viewModel.getRecoverCmidPayload()

        val generateOtpResponseJson =
            """{"sessionId": "3fa85f64-5717-4562-b3fc-2c963f66afa6","otpMedium": "MOBILE","otpMediumValue": 9999999999,"expiryInMinutes": 5}"""
        val generateOtpResponse = Gson().fromJson(generateOtpResponseJson, GenerateOTPResponse::class.java)
        Mockito.`when`(repository.generateOTPForRecoverCMID(recoverCmidRequest)).thenReturn(generateOTPResponseCall)
        Mockito.`when`(generateOTPResponseCall.enqueue(Mockito.any())).then {
            val callback = it.arguments[0] as Callback<GenerateOTPResponse>
            callback.onResponse(generateOTPResponseCall, Response.success(generateOtpResponse))
        }

        viewModel.recoverCmid()
        Mockito.verify(repository).generateOTPForRecoverCMID(recoverCmidRequest)
        Mockito.verify(generateOTPResponseCall).enqueue(any())

    }
}