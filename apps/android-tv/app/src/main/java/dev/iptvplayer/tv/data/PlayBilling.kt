package dev.iptvplayer.tv.data

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** The yearly plan as Play offers it to this viewer, its [price] as Play writes it in the viewer's currency. */
data class PlanOffer(val price: String)

/**
 * What Play says of the membership (D121): the subscription's [status], whether it [renews] (false once cancelled while
 * the paid year runs), and the yearly plan to buy as [offer], null while Play has not offered it. [reachable] is false
 * when the last question to Play failed; the screens then offer to ask again.
 */
data class BillingState(val status: PlayStatus = PlayStatus.UNKNOWN, val renews: Boolean = true, val offer: PlanOffer? = null,
    val reachable: Boolean = true)

/** Google Play's side of the membership; tests put a stand-in in its place (TvApplication.billingFactory). */
interface Billing {
    val state: StateFlow<BillingState>
    /** Asks Play again for the subscription and the plan; [state] changes when it answers. */
    fun refresh()
    /** Opens Play's own purchase window for the yearly plan over [activity]; false when Play offered no plan to buy. */
    fun buy(activity: Activity): Boolean
}

/**
 * The yearly subscription through the Play Billing Library. Play's purchase is acknowledged here, on this TV, since no
 * server of ours sees it (D121, SUBSCRIPTION "Risk 1"); one not acknowledged in three days Play refunds, so each refresh
 * acknowledges what an earlier one could not.
 */
class PlayBilling(context: Context) : Billing, PurchasesUpdatedListener {
    private val mutable = MutableStateFlow(BillingState())
    override val state: StateFlow<BillingState> = mutable.asStateFlow()
    private var product: ProductDetails? = null
    private var offerToken: String? = null
    private val client = BillingClient.newBuilder(context.applicationContext).setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection().build()

    override fun refresh() {
        if (client.isReady) { ask(); return }
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingResponseCode.OK) ask() else mutable.update { it.copy(reachable = false) }
            }
            override fun onBillingServiceDisconnected() = Unit
        })
    }

    private fun ask() {
        client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(ProductType.SUBS)
            .includeSuspendedSubscriptions(true).build()) { result, purchases ->
            if (result.responseCode == BillingResponseCode.OK) take(purchases) else mutable.update { it.copy(reachable = false) }
        }
        val wanted = QueryProductDetailsParams.Product.newBuilder().setProductId(PRODUCT).setProductType(ProductType.SUBS).build()
        client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(listOf(wanted)).build()) { result, found ->
            val details = found.productDetailsList.firstOrNull { it.productId == PRODUCT }
            // The base plan itself: D121 has no Play trial or other offer.
            val plan = details?.subscriptionOfferDetails?.firstOrNull { it.basePlanId == BASE_PLAN && it.offerId == null }
            product = details; offerToken = plan?.offerToken
            val price = plan?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice
            mutable.update { it.copy(offer = price?.let(::PlanOffer), reachable = result.responseCode == BillingResponseCode.OK) }
        }
    }

    /** Play's [purchases]: the status they make, and every bought one not yet acknowledged is acknowledged. */
    private fun take(purchases: List<Purchase>) {
        val reduced = purchases.flatMap { purchase -> purchase.products.map { id ->
            PlayPurchase(id, purchase.purchaseState == Purchase.PurchaseState.PURCHASED,
                purchase.purchaseState == Purchase.PurchaseState.PENDING, purchase.isSuspended, purchase.isAutoRenewing)
        } }
        mutable.update { it.copy(status = playStatus(reduced, PRODUCT), reachable = true,
            renews = reduced.any { purchase -> purchase.product == PRODUCT && purchase.purchased && purchase.autoRenewing }) }
        purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }.forEach { purchase ->
            client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()) { }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingResponseCode.OK -> purchases?.let(::take)
            BillingResponseCode.USER_CANCELED -> Unit
            // Bought on another device or before a reinstall: Play's own list says so.
            else -> refresh()
        }
    }

    override fun buy(activity: Activity): Boolean {
        val details = product ?: return false
        val token = offerToken ?: return false
        val item = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(details).setOfferToken(token).build()
        return client.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(item)).build())
            .responseCode == BillingResponseCode.OK
    }

    companion object {
        /** Play Console's subscription and its auto-renewing yearly base plan (SUBSCRIPTION "24 Ekim güncellemesi"). */
        const val PRODUCT = "antenio_premium"
        const val BASE_PLAN = "yearly"
    }
}
