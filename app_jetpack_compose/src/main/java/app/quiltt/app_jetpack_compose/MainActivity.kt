package app.quiltt.app_jetpack_compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// Splash screen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModelProvider
import app.quiltt.connector.PingResponse
import app.quiltt.connector.QuilttAuthApi
import app.quiltt.connector.QuilttConnector
import app.quiltt.connector.QuilttConnectorConnectConfiguration
import kotlinx.coroutines.Dispatchers

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val token = SharedPreferencesHelper(context = this).getData("token")
        val viewModel: MainViewModel by viewModels { MainViewModelFactory(token) }

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.loading.value
            }
        }

        setContent {
            val isValidToken by viewModel.isValidToken.collectAsState()
            if (isValidToken) {
                val intent = Intent(this@MainActivity, QuilttHubActivity::class.java)
                startActivity(intent)
            } else {
                IngressConnector()
            }
        }
    }
}

class MainViewModelFactory(private val token: String?) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(token) as T
}

class MainViewModel(private val token:String?) : ViewModel() {
    private val _loading = MutableStateFlow(true)
    private val _isValidToken = MutableStateFlow(false)
    val loading = _loading.asStateFlow()
    val isValidToken = _isValidToken.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {// run background task here
            if (token != null) {
                val result = QuilttAuthApi(clientId = null).ping(token = token)
                if (result is PingResponse.SessionResponse) {
                    _isValidToken.value = true
                    _loading.value = false
                } else {
                    _isValidToken.value = false
                    _loading.value = false
                }
            } else {
                _isValidToken.value = false
                _loading.value = false
            }
        }
    }
}

@Composable
fun IngressConnector() {
    val context = LocalContext.current
    val quilttConnector = QuilttConnector(context)
    val config = QuilttConnectorConnectConfiguration(connectorId = AppConfig.ingressConnectorId)
    val connectorWebView = quilttConnector.connect(config = config, onExitSuccess = { metadata ->
        val token: String? = metadata.token
        if (token != null) {
            SharedPreferencesHelper(context).saveData("token", token)
            val intent = Intent(context, QuilttHubActivity::class.java)
            context.startActivity(intent)
            (context as MainActivity).finish()
        }
    })
    AndroidView(factory = { connectorWebView } )
}

@Preview(showBackground = true)
@Composable
fun QuilttHubPreview() {
    IngressConnector()
}