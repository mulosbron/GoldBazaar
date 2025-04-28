package com.mulosbron.goldbazaar.service

import android.widget.Toast
import androidx.fragment.app.Fragment
import com.mulosbron.goldbazaar.model.AuthRequest
import com.mulosbron.goldbazaar.model.AuthResponse
import com.mulosbron.goldbazaar.model.DailyGoldPrice
import com.mulosbron.goldbazaar.model.DailyGoldPercentage
import com.mulosbron.goldbazaar.model.ForgotPasswordRequest
import com.mulosbron.goldbazaar.model.ForgotPasswordResponse
import com.mulosbron.goldbazaar.model.ResetPasswordRequest
import com.mulosbron.goldbazaar.model.ResetPasswordResponse
import com.mulosbron.goldbazaar.view.MainActivity
import com.mulosbron.goldbazaar.view.fragment.ResetPasswordFragment
import com.mulosbron.goldbazaar.view.fragment.WalletFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ApiService(private val context: Fragment) {
    private val baseURL = "http://10.0.2.2:5000/"

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    fun fetchDailyPrices(
        compositeDisposable: CompositeDisposable,
        callback: (Map<String, DailyGoldPrice>) -> Unit
    ) {
        val goldPricesAPI = getRetrofit().create(DailyPricesAPI::class.java)
        compositeDisposable.add(
            goldPricesAPI.getLatestGoldPrices()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    callback(response)
                }, { error ->
                    Toast.makeText(
                        context.requireContext(),
                        "Error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                })
        )
    }

    fun fetchDailyPercentages(
        compositeDisposable: CompositeDisposable,
        callback: (Map<String, DailyGoldPercentage>) -> Unit
    ) {
        val dailyPercentagesAPI = getRetrofit().create(DailyPercentagesAPI::class.java)
        compositeDisposable.add(
            dailyPercentagesAPI
                .getLatestDailyPercentages()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    callback(response)
                }, { error ->
                    Toast.makeText(
                        context.requireContext(),
                        "Error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                })
        )
    }

    fun registerUser(email: String, password: String) {
        val userAPI = getRetrofit().create(UserAPI::class.java)
        val registerRequest = AuthRequest(email, password)
        userAPI.registerUser(registerRequest).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(
                call: Call<AuthResponse>,
                response: Response<AuthResponse>
            ) {
                if (response.isSuccessful) {
                    val registerResponse = response.body()!!
                    Toast.makeText(
                        context.requireContext(),
                        "Registration successful: " + registerResponse.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    context.requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Toast.makeText(
                        context.requireContext(),
                        "Registration unsuccessful: " + response.errorBody()?.string(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(
                call: Call<AuthResponse>,
                t: Throwable
            ) {
                Toast.makeText(
                    context.requireContext(),
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun loginUser(email: String, password: String) {
        val userAPI = getRetrofit().create(UserAPI::class.java)
        userAPI.loginUser(AuthRequest(email, password)).enqueue(object : Callback<AuthResponse> {
            override fun onResponse(call: Call<AuthResponse>, response: Response<AuthResponse>) {
                if (response.isSuccessful && response.body()?.token != null) {
                    (context.activity as? MainActivity)?.saveAuthToken(response.body()!!.token!!)
                    (context.activity as? MainActivity)?.saveUsername(response.body()!!.message)
                    Toast.makeText(
                        context.requireContext(),
                        "Login successful: ${response.body()!!.message.split(" ").last()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    (context.activity as? MainActivity)?.replaceFragment(WalletFragment())
                } else {
                    Toast.makeText(
                        context.requireContext(),
                        "Login unsuccessful: ${response.errorBody()?.string()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                Toast.makeText(
                    context.requireContext(),
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun resetPassword(token: String, newPassword: String) {
        val userAPI = getRetrofit().create(UserAPI::class.java)
        userAPI.resetPassword(ResetPasswordRequest(token, newPassword))
            .enqueue(object : Callback<ResetPasswordResponse> {
                override fun onResponse(
                    call: Call<ResetPasswordResponse>,
                    response: Response<ResetPasswordResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            context.requireContext(),
                            "Password successfully reset",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context.requireContext(),
                            "Password reset unsuccessful",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResetPasswordResponse>, t: Throwable) {
                    Toast.makeText(
                        context.requireContext(),
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    fun forgotPassword(email: String) {
        val userAPI = getRetrofit().create(UserAPI::class.java)
        userAPI.forgotPassword(ForgotPasswordRequest(email))
            .enqueue(object : Callback<ForgotPasswordResponse> {
                override fun onResponse(
                    call: Call<ForgotPasswordResponse>,
                    response: Response<ForgotPasswordResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            context.requireContext(),
                            "Instructions sent to your email.",
                            Toast.LENGTH_SHORT
                        ).show()
                        (context.activity as? MainActivity)?.replaceFragment(ResetPasswordFragment())
                    } else {
                        Toast.makeText(
                            context.requireContext(),
                            "Failed to send instructions. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ForgotPasswordResponse>, t: Throwable) {
                    Toast.makeText(
                        context.requireContext(),
                        "Network error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

}

