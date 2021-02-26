package com.example.encryptsms.data.manager

import android.content.SharedPreferences




class SettingsManager
{
    private val xmppConnectionSettings = arrayOf(
        "serverHost", "serviceName", "serverPort",
        "login", "password", "useDifferentAccount",
        "xmppSecurityMode", "manuallySpecifyServerSettings",
        "useCompression"
    )

    val XMPPSecurityDisabled = 1
    val XMPPSecurityRequired = 2
    val XMPPSecurityOptional = 3

    // XMPP connection
    var serverHost: String? = null
    var serviceName: String? = null
    var serverPort = 0
    var pingIntervalInSec = 0
/*
    private var _login: String? = null
    fun getLogin(): String? {
        return _login
    }

    fun setLogin(value: String?) {
        _login = saveSetting("login", value)
    }

    private var _password: String? = null
    fun getPassword(): String? {
        return _password
    }

    fun setPassword(value: String?) {
        _password = saveSetting("password", value)
    }

    private val _blockedResourcePrefixes: ArrayStringSetting =
        ArrayStringSetting("blockedResourcePrefixes", "\n", this)

    fun getBlockedResourcePrefixes(): ArrayStringSetting? {
        return _blockedResourcePrefixes
    }

    private val _notifiedAddresses: ArrayStringSetting = ArrayStringSetting("notifiedAddress", this)
    fun getNotifiedAddresses(): ArrayStringSetting? {
        return _notifiedAddresses
    }

    private var _connectOnMainScreenStartup = false

    fun getConnectOnMainScreenStartup(): Boolean {
        return _connectOnMainScreenStartup
    }

    fun setConnectOnMainScreenStartup(value: Boolean) {
        _connectOnMainScreenStartup = saveSetting("connectOnMainscreenShow", value)
    }
*/
    var roomPassword: String? = null
    var mucServer: String? = null
    var forceMucServer = false
    var useCompression = false
    private val xmppSecurityMode: String? = null
    var xmppSecurityModeInt = 0
    var manuallySpecifyServerSettings = false

    var connectionSettingsObsolete = false

    // notifications
    var notifyApplicationConnection = false
    var formatResponses = false
    var showStatusIcon = false
    var displayContactNumber = false
    var notificationIgnoreDelay = 0

    /*
    private val _notifHiddenApps: ArrayStringSetting =
        ArrayStringSetting("hiddenNotifications", "#sep#", this)

    fun getNotifHiddenApps(): ArrayStringSetting? {
        return _notifHiddenApps
    }

    private val _notifHiddenMsg: ArrayStringSetting =
        ArrayStringSetting("hiddenMsgNotifications", "#sep#", this)

    fun getNotifHiddenMsgs(): ArrayStringSetting? {
        return _notifHiddenMsg
    }
     */

    // geo location
    var useGoogleMapUrl = false
    var useOpenStreetMapUrl = false

    // ring
    var ringtone: String? = null

    // battery
    var notifyBatteryInStatus = false
    var notifyBattery = false
    var batteryNotificationIntervalInt = 0
    private val batteryNotificationInterval: String? = null

    // sms
    var smsNumber = 5
    var notifySmsSent = false
    var notifySmsDelivered = false
    var notifySmsSentDelivered = false
    var notifyIncomingCalls = false
    var notifySmsInChatRooms = false
    var notifySmsInSameConversation = false
    var notifyInMuc = false
    var smsReplySeparate = false
    var markSmsReadOnReply = false
    var smsMagicWord: String? = null

    // locale
    //var locale: Locale? = null

    // app settings
    var debugLog = false
    var displayIconIndex: String? = null

    // auto start and stop settings
    private val startOnBoot = false
    var startOnPowerConnected = false
    private val startOnWifiConnected = false
    var stopOnPowerDisconnected = false
    private val stopOnWifiDisconnected = false
    var stopOnPowerDelay = 0

    // public intents settings
    var publicIntentsEnabled = false
    var publicIntentTokenRequired = false
    var publicIntentToken: String? = null

    // recipient command settings
    var dontDisplayRecipient = false

    // Camera settings
    var cameraMaxDurationInSec = 0
    var cameraRotationInDegree = 0
    var cameraMaxFileSizeInMegaBytes: Long = 0
    var cameraProfile: String? = null


    private val sSettingsManager: SettingsManager? = null

    private val mProtectedSettings = ArrayList<String>()
    private val mHiddenSettings = ArrayList<String>()
    private val mSharedPreferences: SharedPreferences? = null
    //private val mContext: Context? = null
}