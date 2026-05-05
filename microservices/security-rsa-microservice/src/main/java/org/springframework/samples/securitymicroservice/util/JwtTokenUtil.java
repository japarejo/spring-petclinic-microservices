package org.springframework.samples.securitymicroservice.util;

import java.io.Serializable;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtTokenUtil implements Serializable {

	private static final long serialVersionUID = -2550185165626007488L;
	public static final long JWT_TOKEN_VALIDITY = 5 * 60 * 60;

	@Value("${jwt.private-key}")
	private String privateKey;

	@Value("${jwt.public-key}")
	private String publicKey;

//retrieve username from jwt token
	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

//retrieve expiration date from jwt token

	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	// for retrieveing any information from token we will need the secret key

	private Claims getAllClaimsFromToken(String token) {
		return Jwts.parser().setSigningKey(getPublicKey()).parseClaimsJws(token).getBody();
	}

//check if the token has expired
	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

//generate token for user
	public String generateToken(String username) {
		Map<String, Object> claims = new HashMap<>();
		return doGenerateToken(claims, username);
	}
	public String generateToken(UserDetails userDetails) {
			Map<String, Object> claims = new HashMap<>();
			claims.put("authorities", userDetails.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toList()));
			return doGenerateToken(claims,userDetails.getUsername());
	}

//while creating the token -

//1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
//2. Sign the JWT using the RS256 algorithm and private key.
//3. According to JWS Compact Serialization(https://tools.ietf.org/html/draft-ietf-jose-json-web-signature-41#section-3.1)
//   compaction of the JWT to a URL-safe string 

	private String doGenerateToken(Map<String, Object> claims, String subject) {
		return Jwts.builder()
				.setClaims(claims)
				.setSubject(subject)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
				.signWith(SignatureAlgorithm.RS256, getPrivateKey()).compact();
	}

//validate token
	public Boolean validateToken(String token, UserDetails userDetails) {
		final String username = getUsernameFromToken(token);
		return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	public boolean validateTokenSignatureAndExpiration(String authToken) {
	    try {
	        Jwts.parser().setSigningKey(getPublicKey()).parseClaimsJws(authToken);
	        return true;
	    } catch (SignatureException ex) {
	        log.error("Invalid JWT signature");
	    } catch (MalformedJwtException ex) {
	        log.error("Invalid JWT token");
	    } catch (ExpiredJwtException ex) {
	        log.error("Expired JWT token");
	    } catch (UnsupportedJwtException ex) {
	        log.error("Unsupported JWT token");
	    } catch (IllegalArgumentException ex) {
	        log.error("JWT claims string is empty.");
	    }
	    return false;
	}

	public String generateToken(Authentication authentication) {
		Map<String,Object> claims=new HashMap<>();
		claims.put("authorities",
					authentication.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toList()));
		return doGenerateToken(
					claims
					,authentication.getName());
	}

	private PrivateKey getPrivateKey() {
		try {
			byte[] keyBytes = Base64.getDecoder().decode(privateKey);
			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
			return KeyFactory.getInstance("RSA").generatePrivate(spec);
		} catch (Exception e) {
			throw new IllegalStateException("Invalid RSA private key", e);
		}
	}

	private PublicKey getPublicKey() {
		try {
			byte[] keyBytes = Base64.getDecoder().decode(publicKey);
			X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
			return KeyFactory.getInstance("RSA").generatePublic(spec);
		} catch (Exception e) {
			throw new IllegalStateException("Invalid RSA public key", e);
		}
	}
	
}
