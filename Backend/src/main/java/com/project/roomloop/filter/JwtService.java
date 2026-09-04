    package com.project.roomloop.filter;


    import com.project.roomloop.entity.User;
    import io.jsonwebtoken.Claims;
    import io.jsonwebtoken.Jwts;
    import io.jsonwebtoken.security.Keys;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;

    import javax.crypto.SecretKey;
    import java.nio.charset.StandardCharsets;
    import java.util.Date;

    @Service
    public class JwtService {

        @Value("${jwt.secretKey}")
        private String secretKey;

        private SecretKey getSecreteKey(){
            return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        }

        public String generateAccessToken(User user){
            return Jwts.builder()
                    .setSubject(user.getId().toString())
                    .claim("userId",user.getId().toString())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 1000*60*10))
                    .signWith(getSecreteKey())
                    .compact();
        }

        public Long getUserIdFromToken(String jwtToken){
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSecreteKey())
                    .build()
                    .parseClaimsJws(jwtToken)
                    .getBody();

            return Long.valueOf(claims.getSubject());
        }

        public boolean isJwtValid(String jwtToken){
            try{
                Jwts.parserBuilder().setSigningKey(getSecreteKey()).build().parseClaimsJws(jwtToken);
                return true;
            }catch (Exception e){
                return false;
            }
        }

    }
