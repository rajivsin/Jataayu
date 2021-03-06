package `in`.projecteka.resetpassword.viewmodel

import `in`.projecteka.jataayu.core.model.GenerateOTPResponse
import `in`.projecteka.jataayu.core.model.VerifyOTPRequest
import `in`.projecteka.jataayu.network.utils.PayloadLiveData
import `in`.projecteka.jataayu.network.utils.fetch
import `in`.projecteka.jataayu.presentation.ui.viewmodel.BaseViewModel
import `in`.projecteka.jataayu.util.livedata.SingleLiveEvent
import `in`.projecteka.resetpassword.model.GenerateOTPRequest
import `in`.projecteka.resetpassword.model.VerifyOTPResponse
import `in`.projecteka.resetpassword.repository.ResetPasswordRepository
import android.text.Editable
import android.text.TextWatcher
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField

class ResetPasswordOtpVerificationViewModel(val resetPasswordRepository: ResetPasswordRepository) : BaseViewModel(), TextWatcher {

    companion object {
        const val OTP_LENGTH = 6
        private const val ERROR_EMPTY = ""
    }

    val errorLbl = ObservableField<String>()
    val otpText = ObservableField<String>()
    val submitEnabled = ObservableBoolean()
    val showAccountLockedError = ObservableBoolean(false)

    val onClickValidateEvent = SingleLiveEvent<Void>()
    val verifyOtpResponse = PayloadLiveData<VerifyOTPResponse>()
    val generateOtpResponse = PayloadLiveData<GenerateOTPResponse>()
    val onClickResendEvent = SingleLiveEvent<Void>()

    private var tempToken: String? = null

    override fun afterTextChanged(s: Editable?) {
        if (otpText.get()?.isNotEmpty() == true) {
            errorLbl.set(ERROR_EMPTY)
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        submitEnabled.set(s?.length == OTP_LENGTH)
    }

    fun onClickValidate() {
        onClickValidateEvent.call()
    }

    fun onClickResend(){
        errorLbl.set(ERROR_EMPTY)
         onClickResendEvent.call()
    }

    fun generateOtp(consentManagerId: String) {
        generateOtpResponse.fetch(resetPasswordRepository.generateOtp(GenerateOTPRequest(consentManagerId)))
    }

    fun verifyOtp(sessionId: String, otp: String) {
        verifyOtpResponse.fetch(resetPasswordRepository.verifyOtp(
            VerifyOTPRequest(
                sessionId,
                otp
            )
        ))
    }

    fun init(tempToken: String) {
        this.tempToken = tempToken
    }

}