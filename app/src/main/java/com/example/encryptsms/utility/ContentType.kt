package com.example.encryptsms.utility

import android.webkit.MimeTypeMap




data class ContentType(

    var MMS_MESSAGE: String = "application/vnd.wap.mms-message",
    var THREE_GPP_EXTENSION: String = "3gp",
    var VIDEO_MP4_EXTENSION: String = "mp4",

    // Default extension used when we don't know one.
    var DEFAULT_EXTENSION: String = "dat",

    val TYPE_IMAGE: Int = 0,
    val TYPE_VIDEO: Int = 1,
    val TYPE_AUDIO: Int = 2,
    val TYPE_VCARD: Int = 3,
    val TYPE_OTHER: Int = 4,

    val ANY_TYPE: String = "*/*",
//    val MMS_MESSAGE = "application/vnd.wap.mms-message"

    // The phony content type for generic PDUs (e.g. ReadOrig.ind,
    // Notification.ind, Delivery.ind).
    val MMS_GENERIC: String = "application/vnd.wap.mms-generic",
    val MMS_MULTIPART_MIXED: String = "application/vnd.wap.multipart.mixed",
    val MMS_MULTIPART_RELATED: String = "application/vnd.wap.multipart.related",
    val MMS_MULTIPART_ALTERNATIVE: String = "application/vnd.wap.multipart.alternative",

    val TEXT_PLAIN: String = "text/plain",
    val TEXT_HTML: String = "text/html",
    val TEXT_VCALENDAR: String = "text/x-vCalendar",
    val TEXT_VCARD: String = "text/x-vCard",

    val IMAGE_PREFIX: String = "image/",
    val IMAGE_UNSPECIFIED: String = "image/*",
    val IMAGE_JPEG: String = "image/jpeg",
    val IMAGE_JPG: String = "image/jpg",
    val IMAGE_GIF: String = "image/gif",
    val IMAGE_WBMP: String = "image/vnd.wap.wbmp",
    val IMAGE_PNG: String = "image/png",
    val IMAGE_X_MS_BMP: String = "image/x-ms-bmp",

    val AUDIO_UNSPECIFIED: String = "audio/*",
    val AUDIO_AAC: String = "audio/aac",
    val AUDIO_AMR: String = "audio/amr",
    val AUDIO_IMELODY: String = "audio/imelody",
    val AUDIO_MID: String = "audio/mid",
    val AUDIO_MIDI: String = "audio/midi",
    val AUDIO_MP3: String = "audio/mp3",
    val AUDIO_MPEG3: String = "audio/mpeg3",
    val AUDIO_MPEG: String = "audio/mpeg",
    val AUDIO_MPG: String = "audio/mpg",
    val AUDIO_MP4: String = "audio/mp4",
    val AUDIO_MP4_LATM: String = "audio/mp4-latm",
    val AUDIO_X_MID: String = "audio/x-mid",
    val AUDIO_X_MIDI: String = "audio/x-midi",
    val AUDIO_X_MP3: String = "audio/x-mp3",
    val AUDIO_X_MPEG3: String = "audio/x-mpeg3",
    val AUDIO_X_MPEG: String = "audio/x-mpeg",
    val AUDIO_X_MPG: String = "audio/x-mpg",
    val AUDIO_3GPP: String = "audio/3gpp",
    val AUDIO_X_WAV: String = "audio/x-wav",
    val AUDIO_OGG: String = "application/ogg",

    val MULTIPART_MIXED: String = "multipart/mixed",

    val VIDEO_UNSPECIFIED: String = "video/*",
    val VIDEO_3GP: String = "video/3gp",
    val VIDEO_3GPP: String = "video/3gpp",
    val VIDEO_3G2: String = "video/3gpp2",
    val VIDEO_H263: String = "video/h263",
    val VIDEO_M4V: String = "video/m4v",
    val VIDEO_MP4: String = "video/mp4",
    val VIDEO_MPEG: String = "video/mpeg",
    val VIDEO_MPEG4: String = "video/mpeg4",
    val VIDEO_WEBM: String = "video/webm",

    val APP_SMIL: String = "application/smil",
    val APP_WAP_XHTML: String = "application/vnd.wap.xhtml+xml",
    val APP_XHTML: String = "application/xhtml+xml",

    val APP_DRM_CONTENT: String = "application/vnd.oma.drm.content",
    val APP_DRM_MESSAGE: String = "application/vnd.oma.drm.message"

    ){
    // This class should never be instantiated.
    private fun ContentType()
    {
    }

    fun isTextType(contentType: String): Boolean
    {
        return TEXT_PLAIN == contentType || TEXT_HTML == contentType || APP_WAP_XHTML == contentType
    }

    fun isMediaType(contentType: String?): Boolean
    {
        return (isImageType(contentType)
                || isVideoType(contentType)
                || isAudioType(contentType)
                || isVCardType(contentType))
    }

    private fun isImageType(contentType: String?): Boolean
    {
        return null != contentType && contentType.startsWith(IMAGE_PREFIX)
    }

    private fun isAudioType(contentType: String?): Boolean
    {
        return null != contentType &&
                (contentType.startsWith("audio/") || contentType.equals(
                    AUDIO_OGG,
                    ignoreCase = true))
    }

    private fun isVideoType(contentType: String?): Boolean
    {
        return null != contentType && contentType.startsWith("video/")
    }

    private fun isVCardType(contentType: String?): Boolean
    {
        return null != contentType && contentType.equals(TEXT_VCARD, ignoreCase = true)
    }

    fun isDrmType(contentType: String?): Boolean
    {
        return (null != contentType
                && (contentType == APP_DRM_CONTENT || contentType == APP_DRM_MESSAGE))
    }

    fun isUnspecified(contentType: String?): Boolean
    {
        return null != contentType && contentType.endsWith("*")
    }

    /**
     * If the content type is a type which can be displayed in the conversation list as a preview.
     */
//    fun isConversationListPreviewableType(contentType: String?): Boolean
//    {
//        return ContentType.isAudioType(contentType) || ContentType.isVideoType(contentType) ||
//                ContentType.isImageType(contentType) || ContentType.isVCardType(contentType)
//    }

    /**
     * Given a filename, look at the extension and try and determine the mime type.
     *
     * @param fileName a filename to determine the type from, such as img1231.jpg
     * @param contentTypeDefault type to use when the content type can't be determined from the file
     * extension. It can be null or a type such as ContentType.IMAGE_UNSPECIFIED
     * @return Content type of the extension.
     */
    fun getContentTypeFromExtension(
        fileName: String?,
        contentTypeDefault: String?): String?
    {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        val extension = MimeTypeMap.getFileExtensionFromUrl(fileName)
        var contentType = mimeTypeMap.getMimeTypeFromExtension(extension)
        if (contentType == null)
        {
            contentType = contentTypeDefault
        }
        return contentType
    }

    fun getExtensionFromMimeType(mimeType: String?): String?
    {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(mimeType)
    }

    /**
     * Get the common file extension for a given content type
     * @param contentType The content type
     * @return The extension without the .
     */
    fun getExtension(contentType: String): String?
    {
        return when
        {
            VIDEO_MP4 == contentType  ->
            {
                VIDEO_MP4_EXTENSION
            }
            VIDEO_3GPP == contentType ->
            {
                THREE_GPP_EXTENSION
            }
            else                      ->
            {
                DEFAULT_EXTENSION
            }
        }
    }

}