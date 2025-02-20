package com.openclassrooms.controllers;

import java.security.Principal;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController  // Annotation indiquant que cette classe est un contrôleur Spring qui renvoie directement des réponses JSON
public class LoginController {
	
	private OAuth2AuthorizedClientService authorizedClientService;

	// Constructeur injectant le service OAuth2AuthorizedClientService
	public LoginController(OAuth2AuthorizedClientService authorizedClientService) {
		   this.authorizedClientService = authorizedClientService;
	}

    @GetMapping("/user") // Endpoint accessible aux utilisateurs authentifiés avec le rôle USER
    public String getUser() {
        return "Welcome, User";
    }
    
    @GetMapping("/admin") // Endpoint accessible aux administrateurs
    public String getAdmin() {
        return "Welcome, Admin";
    }
    
    @GetMapping("/*") // Endpoint générique qui retourne des informations sur l'utilisateur connecté
    public String getUserInfo(Principal user, @AuthenticationPrincipal OidcUser oidcUser) {
        StringBuffer userInfo = new StringBuffer();
        if (user instanceof UsernamePasswordAuthenticationToken) {
            userInfo.append(getUsernamePasswordLoginInfo(user));
        } else if (user instanceof OAuth2AuthenticationToken) {
            userInfo.append(getOauth2LoginInfo(user, oidcUser));
        }
        return userInfo.toString();
    }
    
    // Récupère les informations de l'utilisateur pour une authentification classique (login/mot de passe)
    private StringBuffer getUsernamePasswordLoginInfo(Principal user)
    {
       StringBuffer usernameInfo = new StringBuffer();
       
       UsernamePasswordAuthenticationToken token = ((UsernamePasswordAuthenticationToken) user);
       
       // Vérifie si l'utilisateur est bien authentifié
       if(token.isAuthenticated()){
          User u = (User) token.getPrincipal();
          usernameInfo.append("Welcome, " + u.getUsername()); // Retourne le nom d'utilisateur
       }
       else{
          usernameInfo.append("NA"); // Si l'utilisateur n'est pas authentifié, retourne "NA"
       }
       return usernameInfo;
    }
    
    // Récupère les informations de l'utilisateur pour une authentification OAuth2 (ex: via GitHub)
    private StringBuffer getOauth2LoginInfo(Principal user, OidcUser oidcUser) {
		StringBuffer protectedInfo = new StringBuffer();

		OAuth2AuthenticationToken authToken = ((OAuth2AuthenticationToken) user);
		OAuth2AuthorizedClient authClient = this.authorizedClientService
				.loadAuthorizedClient(authToken.getAuthorizedClientRegistrationId(), authToken.getName());
		if (authToken.isAuthenticated()) {

			Map<String, Object> userAttributes = ((DefaultOAuth2User) authToken.getPrincipal()).getAttributes();

			String userToken = authClient.getAccessToken().getTokenValue();
			protectedInfo.append("Welcome, " + userAttributes.get("name") + "<br><br>");
			protectedInfo.append("e-mail: " + userAttributes.get("email") + "<br><br>");
			protectedInfo.append("Access Token: " + userToken + "<br><br>");

			if (oidcUser != null) {
				OidcIdToken idToken = oidcUser.getIdToken();
				if (idToken != null) {
					protectedInfo.append("idToken value: " + idToken.getTokenValue() + "<br><br>");
					protectedInfo.append("Token mapped values <br><br>");
					Map<String, Object> claims = idToken.getClaims();
					for (String key : claims.keySet()) {
						protectedInfo.append("  " + key + ": " + claims.get(key) + "<br>");
					}
				}
			}
		} else {
			protectedInfo.append("NA");
		}
		return protectedInfo;
	}
}
