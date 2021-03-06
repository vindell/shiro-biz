package org.apache.shiro.biz.authc;

import org.apache.shiro.biz.web.servlet.http.HttpStatus;

/**
 * Enumeration of response code.
 */
public enum AuthcResponseCode {
	/**
	 * Authentication Success
	 */
	SC_AUTHC_SUCCESS("200", "shiro.authc.success"),
	SC_AUTHC_ERROR(HttpStatus.SC_INTERNAL_SERVER_ERROR + "", "shiro.authc.error"),
	SC_AUTHC_LOGOUT(HttpStatus.SC_SESSION_LOGOUT + "", "shiro.authc.logout"),
	SC_AUTHC_FAIL("10001", "shiro.authc.fail"),
	SC_AUTHC_METHOD_NOT_ALLOWED("10002", "shiro.authc.method-not-supported"),
	SC_AUTHC_EXCESSIVE_ATTEMPTS("10003", "shiro.authc.excessive.attempts"),
	SC_AUTHC_CAPTCHA_SEND_FAIL("10004", "shiro.authc.captcha.send-fail"),     
	SC_AUTHC_CAPTCHA_REQUIRED("10005", "shiro.authc.captcha.required"),
	SC_AUTHC_CAPTCHA_EXPIRED("10006", "shiro.authc.captcha.expired"),
	SC_AUTHC_CAPTCHA_INVALID("10007", "shiro.authc.captcha.invalid"),
	SC_AUTHC_CAPTCHA_INCORRECT("10008", "shiro.authc.captcha.incorrect"),
	SC_AUTHC_CREDENTIALS_EXPIRED("10009", "shiro.authc.credentials.expired"),
	SC_AUTHC_CREDENTIALS_INCORRECT("10010", "shiro.authc.credentials.incorrect"),
	SC_AUTHC_USER_UNREGISTERED("10011", "shiro.authc.principal.unregistered"),
	SC_AUTHC_USER_REGISTERED("10012", "shiro.authc.principal.registered"),
	SC_AUTHC_USER_NOT_FOUND("10013", "shiro.authc.principal.not-found"),
	SC_AUTHC_USER_DISABLED("10014", "shiro.authc.principal.disabled"),
	SC_AUTHC_USER_EXPIRED("10015", "shiro.authc.principal.expired"),
	SC_AUTHC_USER_LOCKED("10016", "shiro.authc.principal.locked"),
	SC_AUTHC_USER_NO_ROLE("10017", "shiro.authc.principal.no-role"),
	
	SC_AUTHC_SESSION_KICKEDOUT("10018", "shiro.authc.session.kickedout"),
	SC_AUTHC_SESSION_RESTRICTED("10019", "shiro.authc.session.restricted"),
	SC_AUTHC_SESSION_TERMINAL_RESTRICTED("10020", "shiro.authc.session.terminal.restricted"),
	
	SC_AUTHZ_SUCCESS("200", "shiro.authz.success"),
	SC_AUTHZ_FAIL("10021", "shiro.authz.fail"),
	SC_AUTHZ_CODE_REQUIRED("10022", "shiro.authz.code.required"),
	SC_AUTHZ_CODE_EXPIRED("10023", "shiro.authz.code.expired"),
	SC_AUTHZ_CODE_INVALID("10024", "shiro.authz.code.invalid"),
	SC_AUTHZ_CODE_INCORRECT("10025", "shiro.authz.code.incorrect"),
	SC_AUTHZ_DINGTALK_REQUIRED("10026", "shiro.authz.dingtalk.required"),
	SC_AUTHZ_DINGTALK_EXPIRED("10027", "shiro.authz.dingtalk.expired"),
	SC_AUTHZ_DINGTALK_INVALID("10028", "shiro.authz.dingtalk.invalid"),
	SC_AUTHZ_DINGTALK_INCORRECT("10029", "shiro.authz.dingtalk.incorrect"),
	SC_AUTHZ_TICKET_ISSUED("10030", "shiro.authz.ticket.issued"),
	SC_AUTHZ_TICKET_REQUIRED("10031", "shiro.authz.ticket.required"),
	SC_AUTHZ_TICKET_EXPIRED("10032", "shiro.authz.ticket.expired"),
	SC_AUTHZ_TICKET_INVALID("10033", "shiro.authz.ticket.invalid"),
	SC_AUTHZ_TICKET_INCORRECT("10034", "shiro.authz.ticket.incorrect"),
	SC_AUTHZ_TOKEN_ISSUED("10035", "shiro.authz.token.issued"),
	SC_AUTHZ_TOKEN_REQUIRED("10036", "shiro.authz.token.required"),
	SC_AUTHZ_TOKEN_EXPIRED("10037", "shiro.authz.token.expired"),
	SC_AUTHZ_TOKEN_INVALID("10038", "shiro.authz.token.invalid"),
	SC_AUTHZ_TOKEN_INCORRECT("10039", "shiro.authz.token.incorrect"),
	SC_AUTHZ_THIRD_PARTY_SERVICE("10040", "shiro.authz.server.error");
	
	private final String code;
	private final String msgKey;
	
    private AuthcResponseCode(String code, String msgKey) {
        this.code = code;
        this.msgKey = msgKey;
    }

    public String getCode() {
        return code;
    }
    
    public String getMsgKey() {
        return msgKey;
    }
}
