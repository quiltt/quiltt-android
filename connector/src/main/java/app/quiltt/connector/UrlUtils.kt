package app.quiltt.connector

import android.net.Uri

object UrlUtils {
    /**
     * Checks if a string appears to be already URL encoded
     */
    fun isEncoded(str: String): Boolean {
        // Check for typical URL encoding patterns like %20, %3A, etc.
        val hasEncodedChars = "%[0-9A-F]{2}".toRegex(RegexOption.IGNORE_CASE).containsMatchIn(str)
        
        // Check if double encoding has occurred (e.g., %253A instead of %3A)
        // val hasDoubleEncoding = "%25[0-9A-F]{2}".toRegex(RegexOption.IGNORE_CASE).containsMatchIn(str)
        
        // If we have encoded chars but no double encoding, it's likely properly encoded
        return hasEncodedChars 
        
        // TODO: Decide what to do with double encoding
        // && !hasDoubleEncoding
    }
    
    /**
     * Smart URL encoder that ensures a string is encoded exactly once
     */
    fun smartEncodeURIComponent(str: String): String {
        if (str.isEmpty()) return str
        
        // If it's already encoded, return as is
        if (isEncoded(str)) {
            println("URL already encoded, skipping encoding: $str")
            return str
        }
        
        // Otherwise, encode it
        val encoded = Uri.encode(str)
        println("URL encoded from: $str to: $encoded")
        return encoded
    }
    
    /**
     * Normalizes a URL string by decoding it once if it appears to be double-encoded
     */
    fun normalizeUrlEncoding(urlStr: String): String {
        if (isDoubleEncoded(urlStr)) {
            println("Detected double-encoded URL: $urlStr")
            val normalized = Uri.decode(urlStr)
            println("Normalized to: $normalized")
            return normalized
        }
        return urlStr
    }
    
    /**
     * Checks if a string appears to be double-encoded
     */
    private fun isDoubleEncoded(str: String): Boolean {
        if (str.isEmpty()) return false
        return "%25[0-9A-F]{2}".toRegex(RegexOption.IGNORE_CASE).containsMatchIn(str)
    }
}