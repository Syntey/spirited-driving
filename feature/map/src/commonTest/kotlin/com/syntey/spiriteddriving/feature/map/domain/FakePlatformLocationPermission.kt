package com.syntey.spiriteddriving.feature.map.domain

import com.syntey.spiriteddriving.core.platform.binding.LocationAccess
import com.syntey.spiriteddriving.core.platform.binding.LocationPermissionListener
import com.syntey.spiriteddriving.core.platform.binding.PlatformLocationPermission

/**
 * [PlatformLocationPermission] that answers with [access], which a test can change: the answer to a prompt is what the user
 * allows at that point.
 */
internal class FakePlatformLocationPermission(var access: LocationAccess = LocationAccess.Denied) : PlatformLocationPermission {

    override fun request(listener: LocationPermissionListener) = listener.onResult(access)

    override fun currentAccess(listener: LocationPermissionListener) = listener.onResult(access)

    override fun requestPrecise(listener: LocationPermissionListener) = listener.onResult(access)

    override fun openSettings() = Unit
}
