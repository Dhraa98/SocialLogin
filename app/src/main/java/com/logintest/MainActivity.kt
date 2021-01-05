package com.logintest

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.facebook.*
import com.facebook.AccessToken
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.logintest.databinding.ActivityFbBinding
import org.json.JSONException
import org.json.JSONObject
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var callbackManager: CallbackManager
    private lateinit var bindind: ActivityFbBinding
    private lateinit var accessTokenTracker: AccessTokenTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindind = DataBindingUtil.setContentView(this, R.layout.activity_fb)
        printHashKey(this)
        var accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired

        if (isLoggedIn) {
            getUserProfile(AccessToken.getCurrentAccessToken())
        }


        bindind.loginButton.setReadPermissions(
            Arrays.asList(
                "email",
                "public_profile",
                "user_friends"
            )
        )
        callbackManager = CallbackManager.Factory.create()
        accessTokenTracker = object : AccessTokenTracker() {
            override fun onCurrentAccessTokenChanged(
                oldAccessToken: AccessToken?,
                currentAccessToken: AccessToken?
            ) {
                if (currentAccessToken == null) {
                    LoginManager.getInstance().logOut()
                    finish()
                }
            }

        }
        bindind.loginButton.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {

                    loginResult?.accessToken?.let { getUserProfile(it) }
                }

                override fun onCancel() {
                    // App code
                    Log.e("TAG", "onCancel: ")
                }

                override fun onError(exception: FacebookException) {
                    // App code
                    Log.e("TAG", "onError: " + exception.message)
                }
            })


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getUserProfile(currentAccessToken: AccessToken) {
        val request: GraphRequest = GraphRequest.newMeRequest(
            currentAccessToken, object : GraphRequest.GraphJSONObjectCallback {
                override fun onCompleted(`object`: JSONObject, response: GraphResponse?) {
                    Log.d("TAG", `object`.toString())
                    try {
                        val first_name = `object`.getString("first_name")
                        val last_name = `object`.getString("last_name")
                        val email = `object`.getString("email")
                        val id = `object`.getString("id")
                        val image_url =
                            "https://graph.facebook.com/$id/picture?type=normal"
                        bindind.personName.setText("First Name: $first_name\nLast Name: $last_name")
                        bindind.personEmail.setText(email)
                        // Picasso.with(this@MainActivity).load(image_url).into(imageView)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            })
        val parameters = Bundle()
        parameters.putString("fields", "first_name,last_name,email,id")
        request.setParameters(parameters)
        request.executeAsync()
    }

    fun printHashKey(pContext: Context) {
        try {
            val info: PackageInfo = pContext.getPackageManager().getPackageInfo(
                pContext.getPackageName(),
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey: String = String(Base64.encode(md.digest(), 0))
                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("TAG", "printHashKey()", e)
        } catch (e: Exception) {
            Log.e("TAG", "printHashKey()", e)
        }
    }
}