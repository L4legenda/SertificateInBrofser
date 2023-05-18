package com.example.docsaspex

import android.net.http.SslError
import android.os.Bundle
import android.security.KeyChain
import android.security.KeyChainAliasCallback
import android.security.KeyChainException
import android.webkit.ClientCertRequest
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.UnrecoverableKeyException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext


class MainActivity : AppCompatActivity() {
    private var chosenAlias: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val myWebView: WebView = findViewById(R.id.webview)
        myWebView.settings.javaScriptEnabled = true

        myWebView.webViewClient = object : WebViewClient() {
            override fun onReceivedClientCertRequest(view: WebView, request: ClientCertRequest) {
                KeyChain.choosePrivateKeyAlias(
                    this@MainActivity,
                    { alias ->
                        if (alias != null) {
                            try {
                                val privateKey = KeyChain.getPrivateKey(this@MainActivity, alias)
                                val certificate = KeyChain.getCertificateChain(this@MainActivity, alias)
                                request.proceed(privateKey, certificate)
                            } catch (e: KeyChainException) {
                                e.printStackTrace()
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }
                    },
                    null,
                    null,
                    null,
                    -1,
                    null
                )
            }
        }

        myWebView.loadUrl("https://docs.aspex.ru/")
    }

    private fun chooseCertificate() {
        KeyChain.choosePrivateKeyAlias(
            this,
            { alias -> // Этот код будет вызван, когда пользователь выберет сертификат.
                // Если пользователь не выбрал сертификат, alias будет null.
                alias?.let { useCertificate(it) }
            },
            null,
            null,
            null,
            -1,
            null
        )
    }

    private fun useCertificate(alias: String) {
        try {
            // Получаем приватный ключ и сертификат по выбранному alias.
            val privateKey: PrivateKey? = KeyChain.getPrivateKey(this, alias)
            val certificates: Array<X509Certificate>? = KeyChain.getCertificateChain(this, alias)

            // Создаем SSLContext, который будет использовать выбранный сертификат.
            val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
            keyStore.load(null, null)
            keyStore.setKeyEntry(alias, privateKey, null, certificates)
            val keyManagerFactory: KeyManagerFactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(keyStore, null)
            val sslContext: SSLContext = SSLContext.getInstance("TLS")
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null)

            // Здесь вы можете использовать полученный sslContext для установления защищенного соединения.
        } catch (e: KeyChainException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: KeyStoreException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: UnrecoverableKeyException) {
            e.printStackTrace()
        }
    }

    fun openWebSite() {
        val webview = findViewById<WebView>(R.id.webview)
        webview.settings.javaScriptEnabled = true
        webview.loadUrl("https://example.com")
    }
}