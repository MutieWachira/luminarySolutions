package com.example.luminarysolutions.data.repository

import com.example.luminarysolutions.data.models.MpesaResponse
import com.example.luminarysolutions.data.models.Payment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val functions: FirebaseFunctions,
    private val firestore: FirebaseFirestore
) {
    /**
     * Initiates an M-Pesa STK Push via Cloud Functions.
     */
    suspend fun initiatePayment(amount: Double, phoneNumber: String, reference: String? = null): Result<MpesaResponse> {
        return try {
            // Ensure the user is authenticated to avoid UNAUTHENTICATED errors from Cloud Functions
            if (auth.currentUser == null) {
                android.util.Log.d("PaymentRepository", "No user signed in, attempting anonymous sign-in")
                auth.signInAnonymously().await()
            }

            println("PaymentRepository: Initiating STK Push for $phoneNumber, Amount: $amount")
            val userId = auth.currentUser?.uid ?: "anonymous"
            val data = hashMapOf(
                "amount" to amount,
                "phoneNumber" to phoneNumber,
                "userId" to userId,
                "reference" to (reference ?: "General Donation")
            )

            val result = functions
                .getHttpsCallable("initiateStkPush")
                .call(data)
                .await()
            
            val responseData = result.data as Map<*, *>
            println("PaymentRepository: received response: $responseData")
            Result.success(
                MpesaResponse(
                    success = responseData["success"] as Boolean,
                    checkoutRequestId = responseData["checkoutRequestId"] as? String,
                    customerMessage = responseData["customerMessage"] as? String
                )
            )
        } catch (e: Exception) {
            handleException("initiatePayment", e)
        }
    }

    /**
     * Processes a card payment via Cloud Functions using a Stripe PaymentMethod ID.
     */
    suspend fun processCardPayment(
        amount: Double,
        paymentMethodId: String,
        reference: String? = null
    ): Result<Boolean> {
        return try {
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }
            val userId = auth.currentUser?.uid ?: "anonymous"
            val data = hashMapOf(
                "amount" to amount,
                "paymentMethodId" to paymentMethodId,
                "userId" to userId,
                "reference" to (reference ?: "Card Donation")
            )

            val result = functions
                .getHttpsCallable("processCardPayment")
                .call(data)
                .await()
            
            val responseData = result.data as Map<*, *>
            Result.success(responseData["success"] as? Boolean ?: false)
        } catch (e: Exception) {
            handleException("processCardPayment", e)
        }
    }

    /**
     * Queries the status of an STK Push transaction.
     */
    suspend fun queryPaymentStatus(checkoutRequestId: String): Result<Map<String, Any>> {
        return try {
            if (auth.currentUser == null) {
                auth.signInAnonymously().await()
            }
            val result = functions
                .getHttpsCallable("queryStkStatus")
                .call(mapOf("checkoutRequestId" to checkoutRequestId))
                .await()
            
            @Suppress("UNCHECKED_CAST")
            Result.success(result.data as Map<String, Any>)
        } catch (e: Exception) {
            handleException("queryPaymentStatus", e)
        }
    }

    private fun <T> handleException(action: String, e: Exception): Result<T> {
        val message = when (e) {
            is FirebaseFunctionsException -> {
                val code = e.code
                val details = e.details
                "Firebase Function error [$code]: ${e.message} (Details: $details)"
            }
            else -> e.localizedMessage ?: "Unknown error occurred"
        }
        android.util.Log.e("PaymentRepository", "M-Pesa/Payment action $action failed: $message", e)
        return Result.failure(e)
    }

    /**
     * Listens to payment status updates in Firestore.
     */
    fun observePayment(checkoutRequestId: String, onUpdate: (Payment?) -> Unit) {
        firestore.collection("payments").document(checkoutRequestId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onUpdate(null)
                    return@addSnapshotListener
                }
                val payment = snapshot?.toObject(Payment::class.java)
                onUpdate(payment)
            }
    }
}
