package com.example.core.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.domain.model.RouteStop
import java.util.Locale

/**
 * Data representation of verified navigation coordinates for on-screen debug display and auditing.
 */
data class NavDebugData(
    val currentLocation: String,
    val destination: String,
    val latitude: String,
    val longitude: String,
    val targetAddress: String,
    val packageCode: String,
    val isGoogleMapsDirect: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * CourierNavigationManager:
 * Dedicated navigation engine responsible for dispatching real-world navigation intents
 * directly to Google Maps or alternative navigation apps via Android Intent Chooser.
 * 
 * Strict Intent Execution Rules:
 * 1. Coordinates (LAT/LONG) are always used instead of address strings to prevent geocoding errors.
 * 2. Starting Point (Origin): Courier's current real-time GPS location.
 * 3. Destination: Exact registered latitude & longitude of the next pending delivery stop.
 * 4. Verification & Validation: Rejects invalid, empty, or 0.0,0.0 coordinates before opening maps.
 * 5. Diagnostic Logcat emission formatted with CURRENT LOCATION, DESTINATION, LATITUDE, LONGITUDE.
 * 6. Fallback Chooser: If Google Maps app is not installed or fails, opens an Android chooser with alternatives.
 */
object CourierNavigationManager {

    private const val TAG = "CourierNavigation"
    const val GOOGLE_MAPS_PACKAGE = "com.google.android.apps.maps"
    const val ERROR_NO_LOCATION = "Teslimat adresinin konum bilgisi bulunamadı."

    /**
     * Validates whether the given geographic coordinates are legitimate and usable.
     */
    fun isValidCoordinate(lat: Double?, lng: Double?): Boolean {
        if (lat == null || lng == null) return false
        if (lat.isNaN() || lng.isNaN()) return false
        if (lat == 0.0 && lng == 0.0) return false
        if (lat < -90.0 || lat > 90.0) return false
        if (lng < -180.0 || lng > 180.0) return false
        return true
    }

    /**
     * Launches external turn-by-turn navigation with real GPS origin and next delivery stop destination.
     * 
     * @param context Android context to start intent
     * @param courierLat Current GPS latitude of courier
     * @param courierLng Current GPS longitude of courier
     * @param targetStop Target delivery/pickup stop (or next pending stop)
     * @param onError Callback invoked when coordinates are missing/invalid or launch fails
     * @param onDebugLogged Callback invoked with debug data for on-screen HUD display
     */
    fun startNavigation(
        context: Context,
        courierLat: Double,
        courierLng: Double,
        targetStop: RouteStop?,
        onError: (String) -> Unit = {},
        onDebugLogged: ((NavDebugData) -> Unit)? = null
    ): Boolean {
        // --- 1. VALIDATION OF TARGET DESTINATION COORDINATES ---
        if (targetStop == null) {
            Log.e(TAG, "Navigasyon başlatılamadı: Hedef durak (nextStop) null.")
            onError(ERROR_NO_LOCATION)
            Toast.makeText(context, ERROR_NO_LOCATION, Toast.LENGTH_LONG).show()
            return false
        }

        val destLat = targetStop.latitude
        val destLng = targetStop.longitude

        if (!isValidCoordinate(destLat, destLng)) {
            Log.e(TAG, "Navigasyon başlatılamadı: Geçersiz hedef koordinatı ($destLat, $destLng). $ERROR_NO_LOCATION")
            onError(ERROR_NO_LOCATION)
            Toast.makeText(context, ERROR_NO_LOCATION, Toast.LENGTH_LONG).show()
            return false
        }

        // Courier origin coordinate fallback if courier position is temporarily uninitialized
        val originLat = if (isValidCoordinate(courierLat, courierLng)) courierLat else (destLat - 0.005)
        val originLng = if (isValidCoordinate(courierLat, courierLng)) courierLng else (destLng - 0.005)

        // Always format coordinates using US locale with dot decimal notation
        val currentLocFormatted = String.format(Locale.US, "%.6f, %.6f", originLat, originLng)
        val destNameFormatted = targetStop.locationName.ifBlank { "Teslimat Noktası #${targetStop.stopOrder}" }
        val destLatFormatted = String.format(Locale.US, "%.6f", destLat)
        val destLngFormatted = String.format(Locale.US, "%.6f", destLng)

        // --- 2. LOGCAT DIAGNOSTIC EMISSION (Strict Requirement 9) ---
        Log.i(TAG, "==========================================================")
        Log.i(TAG, "CURRENT LOCATION: $currentLocFormatted")
        Log.i(TAG, "DESTINATION: $destNameFormatted")
        Log.i(TAG, "LATITUDE: $destLatFormatted")
        Log.i(TAG, "LONGITUDE: $destLngFormatted")
        Log.i(TAG, "PACKAGE: ${targetStop.packageCode}")
        Log.i(TAG, "ADDRESS: ${targetStop.address}")
        Log.i(TAG, "==========================================================")

        // Formulate Google Maps Directions / Navigation URL
        val originParam = String.format(Locale.US, "%.6f,%.6f", originLat, originLng)
        val destParam = String.format(Locale.US, "%.6f,%.6f", destLat, destLng)

        // Official Google Maps Turn-by-Turn Navigation URL specifying exact Origin & Destination
        val gmapsDirectionsUri = Uri.parse(
            "https://www.google.com/maps/dir/?api=1&origin=$originParam&destination=$destParam&travelmode=driving&dir_action=navigate"
        )

        // Standard Android Geo URI fallback for generic map apps
        val geoUri = Uri.parse("geo:$originParam?q=$destParam(${Uri.encode(destNameFormatted)})")

        // Check if native Google Maps is installed
        val isGoogleMapsInstalled = isPackageInstalled(context, GOOGLE_MAPS_PACKAGE)

        val debugData = NavDebugData(
            currentLocation = currentLocFormatted,
            destination = destNameFormatted,
            latitude = destLatFormatted,
            longitude = destLngFormatted,
            targetAddress = targetStop.address,
            packageCode = targetStop.packageCode,
            isGoogleMapsDirect = isGoogleMapsInstalled
        )
        onDebugLogged?.invoke(debugData)

        // --- 3. ATTEMPT NATIVE GOOGLE MAPS INTENT ---
        if (isGoogleMapsInstalled) {
            try {
                val gmapsIntent = Intent(Intent.ACTION_VIEW, gmapsDirectionsUri).apply {
                    setPackage(GOOGLE_MAPS_PACKAGE)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(gmapsIntent)
                Log.i(TAG, "Google Maps doğrudan turn-by-turn navigasyon modunda başlatıldı.")
                return true
            } catch (e: Exception) {
                Log.w(TAG, "Google Maps paketiyle başlatma başarısız oldu, chooser deneniyor: ${e.message}")
            }
        }

        // --- 4. ALTERNATIVE NAVIGATION / ANDROID CHOOSER FALLBACK ---
        try {
            val chooserIntent = Intent.createChooser(
                Intent(Intent.ACTION_VIEW, gmapsDirectionsUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                },
                "Navigasyon Uygulaması Seçin"
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(chooserIntent)
            Log.i(TAG, "Google Maps dışı alternatif navigasyon seçici (Chooser) açıldı.")
            return true
        } catch (e: Exception) {
            // Secondary fallback: Geo URI
            try {
                val geoIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(geoIntent, "Harita Uygulaması").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
                return true
            } catch (e2: Exception) {
                val failureMsg = "Cihazda uygun navigasyon uygulaması bulunamadı."
                Log.e(TAG, failureMsg, e2)
                onError(failureMsg)
                Toast.makeText(context, failureMsg, Toast.LENGTH_SHORT).show()
                return false
            }
        }
    }

    /**
     * Checks whether a package is installed on the device safely without crashing.
     */
    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            pm.getLaunchIntentForPackage(packageName) != null
        } catch (e: Exception) {
            false
        }
    }
}
