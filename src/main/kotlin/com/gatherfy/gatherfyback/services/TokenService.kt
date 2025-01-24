package com.gatherfy.gatherfyback.services

import com.gatherfy.gatherfyback.properties.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Encoders
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.SecretKey


@Service
class TokenService(
    private val jwtProperties: JwtProperties
) {
    private val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtProperties.key.toByteArray())

    fun generateToken(
        userDetails: UserDetails,
        expirationDate: Date,
        additionalClaims: Map<String, Any> = emptyMap()
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
        expirationDate: Date,
        additionalClaims: Map<String, Long?> = emptyMap()
    ): String = Jwts.builder()
        .claims()
        .issuedAt(Date(System.currentTimeMillis()))
        .expiration(expirationDate)
        .add(additionalClaims)
        .and()
        .signWith(secretKey,SignatureAlgorithm.HS256)
        .compact()

    fun getAllClaimsFromToken(token: String): Claims{
        val parser = Jwts.parser().verifyWith(secretKey).build()
        return parser.parseSignedClaims(token).payload
    }

    fun isTokenExpired(token: String): Boolean{
        return getAllClaimsFromToken(token).expiration.before(Date(System.currentTimeMillis()))
    }

    fun getUsernameFromToken(token: String): String{
        return getAllClaimsFromToken(token).subject
    }

    fun isValidToken(token: String, userDetails: UserDetails): Boolean{
        val username = getUsernameFromToken(token)
        return username == userDetails.username && !isTokenExpired(token)
    }
}