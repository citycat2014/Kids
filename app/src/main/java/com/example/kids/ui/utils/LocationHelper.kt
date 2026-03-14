package com.example.kids.ui.utils

import android.content.Context
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 位置信息辅助类
 * 用于获取当前设备的地理位置坐标
 */
object LocationHelper {

    /**
     * 位置结果回调
     * @param latitude 纬度，null 表示获取失败
     * @param longitude 经度，null 表示获取失败
     */
    data class LocationResult(
        val latitude: Double?,
        val longitude: Double?
    ) {
        val isValid: Boolean
            get() = latitude != null && longitude != null
    }

    // 缓存的有效时间（毫秒）- GPS 位置可以缓存更长时间
    private const val MIN_TIME_THRESHOLD = 30000L

    // 最小更新距离（米）
    private const val MIN_DISTANCE_THRESHOLD = 10f

    /**
     * 获取当前位置
     * 使用 GPS 和网络定位获取最后已知位置
     *
     * @param context Android Context
     * @return LocationResult 包含经纬度信息
     */
    suspend fun getCurrentLocation(context: Context): LocationResult = withContext(Dispatchers.IO) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!gpsEnabled && !networkEnabled) {
                return@withContext LocationResult(null, null)
            }

            val bestLocation = getBestLastKnownLocation(locationManager, gpsEnabled, networkEnabled)

            if (bestLocation != null) {
                LocationResult(bestLocation.latitude, bestLocation.longitude)
            } else {
                LocationResult(null, null)
            }
        } catch (e: SecurityException) {
            LocationResult(null, null)
        } catch (e: Exception) {
            LocationResult(null, null)
        }
    }

    /**
     * 获取最佳的最后已知位置
     * 优先使用 GPS，其次网络定位
     * 只接受符合时间阈值的位置（在有效期内）
     */
    private fun getBestLastKnownLocation(
        locationManager: LocationManager,
        gpsEnabled: Boolean,
        networkEnabled: Boolean
    ): android.location.Location? {
        val currentTime = System.currentTimeMillis()
        val validTimeThreshold = currentTime - MIN_TIME_THRESHOLD

        // 优先使用 GPS 位置（更准确）
        if (gpsEnabled) {
            val gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (gpsLocation != null && gpsLocation.time > validTimeThreshold) {
                return gpsLocation
            }
        }

        // 其次使用网络位置
        if (networkEnabled) {
            val networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (networkLocation != null && networkLocation.time > validTimeThreshold) {
                return networkLocation
            }
        }

        // 如果没有找到符合时间要求的位置，返回 null
        // 让调用者决定是否请求新位置更新
        return null
    }

    /**
     * 检查位置服务是否可用
     */
    fun isLocationServiceEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}