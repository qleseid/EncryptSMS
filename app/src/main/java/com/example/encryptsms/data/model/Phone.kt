package com.example.encryptsms.data.model

import android.provider.ContactsContract.CommonDataKinds

class Phone
{
    private val cellPhonePattern = Regex("\\+*\\d+")

    private var mContactName: String? = null
    private var mNumber: String? = null
    private var mCleanNumber: String? = null
    private var mLabel: String? = null
    private var mType = 0
    private var mIsCellPhoneNumber = false
    private var mIsDefaultNumber = false

    /**
     *
     * @param contactName
     * @param number
     */
    constructor(contactName: String?, number: String) {
        mContactName = contactName
        mLabel = contactName
        mNumber = number
        mCleanNumber = cleanPhoneNumber(number)
        mIsCellPhoneNumber = true
        mType = CommonDataKinds.Phone.TYPE_MOBILE
    }

    /**
     *
     * @param number
     * @param label
     * @param type
     * @param super_primary
     */
    constructor(number: String, label: String?, type: Int, super_primary: Int) {
        mNumber = number
        mCleanNumber = cleanPhoneNumber(number)
        mLabel = label
        mType = type
        mIsDefaultNumber = super_primary > 0
    }

    fun phoneMatch(phone: String): Boolean? {
        var phone = phone
        phone = cleanPhoneNumber(phone)
        if (mCleanNumber == phone) {
            return true
        } else if (mCleanNumber!!.length != phone.length) {
            if (mCleanNumber!!.length > phone.length && mCleanNumber!!.startsWith("+")) {
                return mCleanNumber!!.replaceFirst("\\+\\d\\d".toRegex(), "0") == phone
            } else if (phone.length > mCleanNumber!!.length && phone.startsWith("+")) {
                return phone.replaceFirst("\\+\\d\\d".toRegex(), "0") == mCleanNumber
            }
        }
        return false
    }

    fun isCellPhoneNumber(number: String?): Boolean? {
        return number?.let { cleanPhoneNumber(it).matches(cellPhonePattern) }
    }

    fun cleanPhoneNumber(number: String): String {
        return number.replace("(", "")
            .replace(")", "")
            .replace("-", "")
            .replace(".", "")
            .replace(" ", "")
            .replace("+", "")
    }

    fun getContactName(): String? {
        return mContactName
    }

    fun getNumber(): String? {
        return mNumber
    }

    fun getCleanNumber(): String? {
        return mCleanNumber
    }

    fun getLabel(): String? {
        return mLabel
    }

    fun getType(): Int {
        return mType
    }

    fun isCellPhoneNumber(): Boolean {
        return mIsCellPhoneNumber
    }

    fun isDefaultNumber(): Boolean {
        return mIsDefaultNumber
    }

    fun setContactName(name: String?) {
        mContactName = name
    }
}