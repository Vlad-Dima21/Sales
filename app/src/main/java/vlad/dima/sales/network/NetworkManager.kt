package vlad.dima.sales.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkManager(
    private val context: Context
) {
    private val _connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _currentConnection = MutableStateFlow(NetworkStatus.Unavailable)
    val currentConnection = _currentConnection.asStateFlow()

    private val _networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    private val _networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            _currentConnection.value = NetworkStatus.Available
        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)
            _currentConnection.value = NetworkStatus.Losing
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            _currentConnection.value = NetworkStatus.Lost
        }

        override fun onUnavailable() {
            super.onUnavailable()
            _currentConnection.value = NetworkStatus.Unavailable
        }
    }

    init {
        _connectivityManager.requestNetwork(_networkRequest, _networkCallback)
    }

    enum class NetworkStatus {
        Available,
        Losing,
        Lost,
        Unavailable
    }
}