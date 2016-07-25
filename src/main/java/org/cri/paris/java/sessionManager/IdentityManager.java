package org.cri.paris.java.sessionManager;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

/**
 *
 * @author TTAK arthur.besnard@cri-paris.org
 */
public class IdentityManager {

    private final String CLIENT_ID;

    public IdentityManager(String clientId) {
        this.CLIENT_ID = clientId;
    }

    String validateToken(String token) throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(httpTransport, JacksonFactory.getDefaultInstance())
                .setAudience(Arrays.asList(CLIENT_ID))
                // If you retrieved the token on Android using the Play Services 8.3 API or newer, set
                // the issuer to "https://accounts.google.com". Otherwise, set the issuer to
                // "accounts.google.com". If you need to verify tokens from multiple sources, build
                // a GoogleIdTokenVerifier for each issuer and try them both.
                .setIssuer("accounts.google.com")
                .build();
        // (Receive idTokenString by HTTPS POST)
        GoogleIdToken idToken = verifier.verify(token);
        if (idToken != null) {
            
            Payload payload = idToken.getPayload();
            return payload.getSubject();
        } else {
            return null;
        }
    }

}
