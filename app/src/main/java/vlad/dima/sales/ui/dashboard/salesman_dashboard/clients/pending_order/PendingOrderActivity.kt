package vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.pending_order

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import vlad.dima.sales.repository.OrderRepository
import vlad.dima.sales.room.SalesDatabase
import vlad.dima.sales.ui.dashboard.salesman_dashboard.clients.Client
import vlad.dima.sales.ui.theme.SalesTheme

class PendingOrderActivity : ComponentActivity() {

    private lateinit var viewModel: PendingOrderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val client = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("client", Client::class.java)
        } else {
            intent.getSerializableExtra("client") as Client
        }!!

        val repository = SalesDatabase.getDatabase(this).run {
            OrderRepository(orderDao(), orderProductDao())
        }

        viewModel = ViewModelProvider(
            this,
            PendingOrderViewModel.Factory(
                client,
                repository
            )
        )[PendingOrderViewModel::class.java]

        setContent {
            SalesTheme {
            }
        }
    }

    private fun orderFinished() {
        setResult(
            RESULT_OK, Intent().putExtra(
                "client",
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getSerializableExtra("client", Client::class.java)
                } else {
                    intent.getSerializableExtra("client")
                }
            )
        )
        finish()
    }
}