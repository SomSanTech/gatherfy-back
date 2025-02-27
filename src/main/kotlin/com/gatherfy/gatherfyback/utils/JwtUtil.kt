package com.gatherfy.gatherfyback.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.gatherfy.gatherfyback.properties.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.interfaces.RSAPublicKey
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtUtil(
    private val jwtProperties: JwtProperties
) {
//    private val secretKey = "your_very_secure_and_long_secret_key_32_characters_or_more"
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtProperties.key.toByteArray())

    private val googlePublicKeyUrl = "https://www.googleapis.com/oauth2/v3/certs"

    fun getJwtHeader(token: String): Map<String, Any>? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return null  // JWT ต้องมี 3 ส่วน (Header.Payload.Signature)

            val decodedHeader = String(Base64.getUrlDecoder().decode(parts[0]))  // Decode Header
            jacksonObjectMapper().readValue(decodedHeader, Map::class.java) as Map<String, Any>
        } catch (e: Exception) {
            println("Error decoding JWT header: ${e.message}")
            null
        }
    }

    fun verifyToken(token: String): String? {

//        val header = Jwts.parser().build().parseClaimsJws(token).header
//        val header = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).header
        val header = getJwtHeader(token) ?: return null
        val alg = header["alg"] as? String ?: return null

        return when (alg) {
            "HS256" -> "HS256"
            "RS256" -> "RS256"
            else -> null // ไม่รองรับ algorithm นี้
        }
    }

//    private fun verifyHs256Token(token: String): Claims?{
//        return try {
//            val keyBytes = Base64.getDecoder().decode(secretKey) // Ensure it's Base64-decoded
//            val hmacKey: SecretKey = Keys.hmacShaKeyFor(keyBytes) // Convert to a proper SecretKey
//
//            Jwts.parserBuilder()
//                .setSigningKey(hmacKey)
//                .build()
//                .parseClaimsJws(token)
//                .body
//        } catch (e: Exception) {
//            null
//        }
//        println("This is HS256")
//    }
//
//    private fun verifyRs256Token(token: String): Claims? {
//        val googlePublicKey = fetchGooglePublicKey()
//        return try {
//            Jwts.parserBuilder()
//                .setSigningKey(googlePublicKey)
//                .build()
//                .parseClaimsJws(token)
//                .body
//        } catch (e: Exception) {
//            null
//        }
//        println("This is RS256")
//    }
//
//    private fun fetchGooglePublicKey(): RSAPublicKey {
//        // ดึง Public Key จาก Google (ต้องทำ cache key ไว้เพื่อไม่ให้โหลดบ่อย)
//        val key = GooglePublicKeyFetcher.getGooglePublicKey(googlePublicKeyUrl)
//        return key
//    }
}