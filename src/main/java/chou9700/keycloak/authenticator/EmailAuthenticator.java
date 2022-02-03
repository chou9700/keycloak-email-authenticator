package chou9700.keycloak.authenticator;

import java.util.Random;
import javax.ws.rs.core.Response;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.email.DefaultEmailSenderProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.sessions.AuthenticationSessionModel;

public class EmailAuthenticator implements Authenticator {

    private static final String LOGIN_FTL = "login-email.ftl";
    private static Logger LOG = Logger.getLogger(EmailAuthenticator.class);

    private static int RandomCode() {
        int len = 6;
        int max = (int) (Math.pow(10, len) - 1);
        Random rnd = new Random();
        return rnd.nextInt(max);
    }

    @Override
    public void close() {
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        UserModel user = context.getUser();

        String username = user.getFirstAttribute("username");
        int code = RandomCode();
        int ttl = 300; // timeout in seconds
        String msg = String.format("Your Verification Code is %06d", code);

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        authSession.setAuthNote("code", String.format("%06d", code));
        authSession.setAuthNote("ttl", Long.toString(System.currentTimeMillis() + (ttl * 1000L)));

        try {
            DefaultEmailSenderProvider senderProvider = new DefaultEmailSenderProvider(context.getSession());
            LOG.infof("Send verification code, username: %s, code: %06d", username, code);
            senderProvider.send(
                    context.getRealm().getSmtpConfig(),
                    user,
                    "Verification Code",
                    msg,
                    msg);
            context.challenge(context.form().setAttribute("realm", context.getRealm()).createForm(LOGIN_FTL));
        } catch (Exception e) {
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                    context.form().setError("emailAuthCodeNotSent", e.getMessage())
                            .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
        }
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        String enteredCode = context.getHttpRequest().getDecodedFormParameters().getFirst("code");

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        String code = authSession.getAuthNote("code");
        String ttl = authSession.getAuthNote("ttl");

        if (code == null || ttl == null) {
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR,
                    context.form().createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
            return;
        }

        boolean isValid = enteredCode.equals(code);
        if (isValid) {
            if (Long.parseLong(ttl) < System.currentTimeMillis()) {
                // expired
                context.failureChallenge(AuthenticationFlowError.EXPIRED_CODE,
                        context.form().setError("emailAuthCodeExpired").createErrorPage(Response.Status.BAD_REQUEST));
            } else {
                // valid
                context.success();
            }
        } else {
            // invalid
            Response challenge = context.form()
                    .addError(new FormMessage("email", "emailAuthCodeInvalid"))
                    .createForm(LOGIN_FTL);
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
        }
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // Valid for all user
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

}
