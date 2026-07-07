package com.checkin.watermark.record

import java.security.MessageDigest

object Sha256Hasher {
    fun hash(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }
}
