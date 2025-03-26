package com.gatherfy.gatherfyback.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.gatherfy.gatherfyback.properties.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.net.URL
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.util.*
import javax.crypto.SecretKey
import org.json.JSONObject
import java.math.BigInteger
import java.security.spec.RSAPublicKeySpec


@Service
class TokenService(
    private val jwtProperties: JwtProperties
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtProperties.key.toByteArray())
    private val googlePublicKeyUrl = "https://www.googleapis.com/oauth2/v3/certs"

    fun generateToken(
        userDetails: UserDetails,
        expirationDate: Date,
        additionalClaims: Map<String, Any?> = emptyMap()
    ): String = Jwts.builder()
        .claims()
        .subject(userDetails.username)
        .issuedAt(Date(System.currentTimeMillis()))
        .expiration(expirationDate)
        .add(additionalClaims)
        .and()
        .signWith(secretKey,SignatureAlgorithm.HS256)
        .compact()

    fun generateRefreshToken(
        userDetails: UserDetails,
        expirationDate: Date,
        additionalClaims: Map<String, Any?> = emptyMap()
    ): String = Jwts.builder()
        .claims()
        .subject(userDetails.username)
        .issuedAt(Date(System.currentTimeMillis()))
        .expiration(expirationDate)
        .add(additionalClaims)
        .and()
        .signWith(secretKey,SignatureAlgorithm.HS256)
        .compact()

    fun generateCheckInToken(
        username: String,
        expirationDate: Date,
        additionalClaims: Map<String, Any?> = emptyMap()
    ): String = Jwts.builder()
        .claims()
        .subject(username)
        .issuedAt(Date(System.currentTimeMillis()))
        .expiration(expirationDate)
        .add(additionalClaims)
        .and()
        .signWith(secretKey,SignatureAlgorithm.HS256)
        .compact()

    fun getAllClaimsFromToken(token: String): Claims? {
        // Get the header to check the algorithm
        val header = getJwtHeader(token)
        val alg = header!!["alg"] as? String

        return when (alg) {
            "HS256" -> { // Use the secret key for HS256
                println("This is HS256")
                val parser = Jwts.parser().verifyWith(secretKey).build()
                return parser.parseSignedClaims(token).payload
            }
            "RS256" -> { // Use the public key for RS256
                println("This is RS256")
                val jwtKid = header["kid"] ?: throw IllegalArgumentException("Missing 'kid' in JWT header")
                val publicKey = fetchGooglePublicKey(jwtKid.toString())  // Fetch your RS256 public key (e.g., from Google)
                val parser = Jwts.parser().verifyWith(publicKey).build()
                println(parser.parseSignedClaims(token).payload)
                return parser.parseSignedClaims(token).payload
            }
            else -> return null // Unsupported algorithm
        }
    }
    fun isTokenExpired(token: String): Boolean{
        return getAllClaimsFromToken(token)!!.expiration.before(Date(System.currentTimeMillis()))
    }

    fun getUsernameFromToken(token: String): String{
        return getAllClaimsFromToken(token)!!.subject
    }

    fun getUserIdFromToken(token: String): Int{
        return getAllClaimsFromToken(token)!!["userId"] as Int
    }

    fun isValidToken(token: String, userDetails: UserDetails): Boolean{
        val username = getUsernameFromToken(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    fun getAdditionalClaims(token: String, claimKey: String): Any? {
        return getAllClaimsFromToken(token)!![claimKey]
    }

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
    private fun fetchGooglePublicKey(jwtKid: String): RSAPublicKey {
        // ดึง Public Key จาก Google (ต้องทำ cache key ไว้เพื่อไม่ให้โหลดบ่อย)
        val key = getGooglePublicKey(googlePublicKeyUrl, jwtKid)
        return key
    }
    fun getGooglePublicKey(url: String, jwtKid: String): RSAPublicKey {
        val response = URL(url).readText()
        val json = JSONObject(response)
        val keys = json.getJSONArray("keys")

        // Iterate over keys to find matching 'kid'
        for (i in 0 until keys.length()) {
            val key = keys.getJSONObject(i)
            val kid = key.getString("kid")  // Get 'kid' from the key

            if (kid == jwtKid) {  // Match the 'kid' with the one in the JWT header
                val n = key.getString("n")  // Modulus
                val e = key.getString("e")  // Exponent

                val modulus = Base64.getUrlDecoder().decode(n)
                val exponent = Base64.getUrlDecoder().decode(e)

                val keySpec = RSAPublicKeySpec(
                    BigInteger(1, modulus),  // Convert modulus to BigInteger
                    BigInteger(1, exponent)  // Convert exponent to BigInteger
                )

                val keyFactory = KeyFactory.getInstance("RSA")
                return keyFactory.generatePublic(keySpec) as RSAPublicKey
            }
        }
        throw IllegalArgumentException("No matching key found for kid: $jwtKid")
    }
}