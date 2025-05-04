package com.mulosbron.goldbazaar.util.ext

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mulosbron.goldbazaar.R
import com.mulosbron.goldbazaar.view.activity.MainActivity

fun Fragment.getMainActivity(): MainActivity? {
    return activity as? MainActivity
}

fun Fragment.navigateToRegisterFromLogin() {
    findNavController().navigate(R.id.action_login_to_register)
}

fun Fragment.navigateToForgotPasswordFromLogin() {
    findNavController().navigate(R.id.action_login_to_forgot_password)
}

fun Fragment.navigateToLoginFromRegister() {
    findNavController().navigate(R.id.action_register_to_login)
}

fun Fragment.navigateToLoginFromForgotPassword() {
    findNavController().navigate(R.id.action_forgot_password_to_login)
}

fun Fragment.saveAuthData(token: String, username: String) {
    getMainActivity()?.apply {
        saveAuthToken(token)
        saveUsername(username)
    }
}