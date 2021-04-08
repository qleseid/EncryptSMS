package com.lolson.encryptsms.data.model

import java.io.Serializable

object Phone: Serializable
{
    /**
     * A representation of a Phone contact
     */
    data class pho(
        var mContactName: String? = null,
        var mNumber: String? = null,
        var mCleanNumber: String? = mNumber?.let { pho().cleanPhoneNumber(it) },
        var mLabel: String? = mContactName,
        var mType: Int = 0,
        var mIsCellPhoneNumber: Boolean = false,
        var mIsDefaultNumber: Boolean = false
    ): Serializable
    {
//        private val cellPhonePattern = Regex("\\+*\\d+")
        private val cellPhonePattern = Regex("^[0-9]{3}[0-9]{3}[0-9]{4,6}\$")

        override fun toString(): String
        {
            return "$mNumber:: $mCleanNumber"
        }

        fun phoneMatch(pho: String): Boolean? {
            var phone = pho
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

        private fun cleanPhoneNumber(number: String): String {
            return number.replace("(", "")
                .replace(")", "")
                .replace("-", "")
                .replace(".", "")
                .replace(" ", "")
                .replace("+", "")
        }
    }
}