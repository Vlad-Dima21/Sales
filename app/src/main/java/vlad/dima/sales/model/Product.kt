package vlad.dima.sales.model

import android.net.Uri
import com.google.firebase.firestore.Exclude

data class Product(
    @Exclude var productId: String = "",
    var productCode: String = "",
    var productName: String = "",
    @Exclude var productImageUri: Uri = Uri.EMPTY,
    var productDescription: String = "",
    var quantitySold: Int = 1,
    @Exclude var quantityAdded: Int = 0,
    var price: Float = 0f,
    var stock: Int = 0
)
