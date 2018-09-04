/*
 * Copyright (c) 2018 (https://github.com/vindell).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.shiro.biz.web.filter.authc;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.biz.authc.exception.IncorrectCaptchaException;
import org.apache.shiro.biz.authc.token.CaptchaAuthenticationToken;
import org.apache.shiro.biz.authc.token.DefaultAuthenticationToken;
import org.apache.shiro.biz.utils.WebUtils;
import org.apache.shiro.biz.web.filter.authc.captcha.CaptchaResolver;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrustableRestAuthenticationFilter extends FormAuthenticationFilter {

	private static final Logger LOG = LoggerFactory.getLogger(TrustableRestAuthenticationFilter.class);
	public static final String DEFAULT_CAPTCHA_PARAM = "captcha";
	public static final String DEFAULT_RETRY_TIMES_KEY_ATTRIBUTE_NAME = "shiroLoginFailureRetries";
	
	private boolean captchaEnabled = false;
	private String captchaParam = DEFAULT_CAPTCHA_PARAM;
    private String retryTimesKeyAttribute = DEFAULT_RETRY_TIMES_KEY_ATTRIBUTE_NAME;
    /** Maximum number of retry to login . */
	private int retryTimesWhenAccessDenied = 3;
	private CaptchaResolver captchaResolver;
	
	public TrustableRestAuthenticationFilter() {
		setLoginUrl(DEFAULT_LOGIN_URL);
	}
	
	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		if (isLoginRequest(request, response)) {
			if (isLoginSubmission(request, response)) {
				if (LOG.isTraceEnabled()) {
					LOG.trace("Login submission detected.  Attempting to execute login.");
				}
				return executeLogin(request, response);
			} else {
				String mString = "Authentication url [" + getLoginUrl() + "] Not Http Post request.";
				if (LOG.isTraceEnabled()) {
					LOG.trace(mString);
				}
				WebUtils.writeJSONString(response, HttpServletResponse.SC_BAD_REQUEST, mString);
				return false;
			}
		} else {
			String mString = "Attempting to access a path which requires authentication.  Forwarding to the "
					+ "Authentication url [" + getLoginUrl() + "]";
			if (LOG.isTraceEnabled()) {
				LOG.trace(mString);
			}
			WebUtils.writeJSONString(response, HttpServletResponse.SC_UNAUTHORIZED, mString);
			return false;
		}
	}
	
    /**
     * This default implementation merely returns <code>true</code> if the request is an HTTP <code>POST</code>,
     * <code>false</code> otherwise. Can be overridden by subclasses for custom login submission detection behavior.
     * 重写是否登录请求判断逻辑，增加Ajax请求判断
     * @param request  the incoming ServletRequest
     * @param response the outgoing ServletResponse.
     * @return <code>true</code> if the request is an HTTP <code>POST</code>, <code>false</code> otherwise.
     */
    @Override
    protected boolean isLoginSubmission(ServletRequest request, ServletResponse response) {
        return (request instanceof HttpServletRequest) && WebUtils.toHttp(request).getMethod().equalsIgnoreCase(POST_METHOD) && WebUtils.isAjaxRequest(request);
    }
	
	@Override
	protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
		Subject subject = getSubject(request, response);
		AuthenticationToken token = createToken(request, response);
		if (subject.isAuthenticated()) {
			LOG.info("User has already been Authenticated!");
			return onLoginSuccess(token, subject, request, response);
		}
		try {
			if (token == null) {
				String msg = "createToken method implementation returned null. A valid non-null AuthenticationToken "
						+ "must be created in order to execute a login attempt.";
				throw new AuthenticationException(msg);
			}
			
			if (token instanceof CaptchaAuthenticationToken && isOverRetryTimes(request, response)) {
				boolean validation = captchaResolver.validCaptcha(request, (CaptchaAuthenticationToken) token);
				if (!validation) {
					throw new IncorrectCaptchaException("Captcha validation failed!");
				}
			}
			subject.login(token);
			return onLoginSuccess(token, subject, request, response);
		} catch (AuthenticationException e) {
			return onLoginFailure(token, e, request, response);
		}
	}
	
	
	@Override
	protected AuthenticationToken createToken(String username, String password, ServletRequest request,
			ServletResponse response) {

		boolean rememberMe = isRememberMe(request);
		String host = getHost(request);
		// 判断是否需要进行验证码检查
		if (isCaptchaEnabled()) {

			DefaultAuthenticationToken token = new DefaultAuthenticationToken(username, password);

			token.setHost(host);
			token.setRememberMe(rememberMe);
			token.setCaptcha(getCaptcha(request));

			return token;
		}
		return super.createToken(username, password, rememberMe, host);

	}

	

    /**
     * 重写成功登录后的响应逻辑：实现JSON信息回写
     */
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject,
                                     ServletRequest request, ServletResponse response) throws Exception {
        // 响应成功状态信息
        WebUtils.writeJSONString(response, HttpServletResponse.SC_OK, "Authentication Success.");
        
        //we handled the success , prevent the chain from continuing:
        return false;
    }
    
    /**
     * 重写成功失败后的响应逻辑：增加失败次数记录和实现JSON信息回写
     */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e,
                                     ServletRequest request, ServletResponse response) {
        if (LOG.isDebugEnabled()) {
        	LOG.debug( "Authentication exception", e );
        }
        setFailureAttribute(request, e);
        setFailureCountAttribute(request, response, e);
        
        // 已经超出了重试限制，需要进行提醒
        if(isOverRetryTimes(request, response)) {
        	// 响应异常状态信息
        	Map<String, Object> data = new HashMap<String, Object>();
        	data.put("status", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			data.put("message", "Over Maximum number of retry to login.");
			data.put("captcha", "1");
            WebUtils.writeJSONString(response, data);
        } else {
        	// 响应异常状态信息
            WebUtils.writeJSONString(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication Exception.");
        }
        return false;
    }
    
    protected void setFailureCountAttribute(ServletRequest request, ServletResponse response,
			AuthenticationException ae) {

		Session session = getSubject(request, response).getSession(true);
		Object count = session.getAttribute(getRetryTimesKeyAttribute());
		if (null == count) {
			session.setAttribute(getRetryTimesKeyAttribute(), 1);
		} else {
			session.setAttribute(getRetryTimesKeyAttribute(), Long.parseLong(String.valueOf(count)) + 1);
		}
		
	}
	
    protected String getCaptcha(ServletRequest request) {
		return WebUtils.getCleanParam(request, getCaptchaParam());
	}
	
	@Override
	protected String getHost(ServletRequest request) {
		return WebUtils.getRemoteAddr(request);
	}
	
	protected boolean isOverRetryTimes(ServletRequest request, ServletResponse response) {
		Session session = getSubject(request, response).getSession(true);
		Object count = session.getAttribute(getRetryTimesKeyAttribute());
		if (null != count && Long.parseLong(String.valueOf(count)) > getRetryTimesWhenAccessDenied()) {
			return false;
		}
		return true;
	}

	public boolean isCaptchaEnabled() {
		return captchaEnabled && captchaResolver != null;
	}

	public void setCaptchaEnabled(boolean captchaEnabled) {
		this.captchaEnabled = captchaEnabled;
	}
	
	public String getCaptchaParam() {
		return captchaParam;
	}

	public void setCaptchaParam(String captchaParam) {
		this.captchaParam = captchaParam;
	}

	public String getRetryTimesKeyAttribute() {
		return retryTimesKeyAttribute;
	}

	public void setRetryTimesKeyAttribute(String retryTimesKeyAttribute) {
		this.retryTimesKeyAttribute = retryTimesKeyAttribute;
	}

	public int getRetryTimesWhenAccessDenied() {
		return retryTimesWhenAccessDenied;
	}

	public void setRetryTimesWhenAccessDenied(int retryTimesWhenAccessDenied) {
		this.retryTimesWhenAccessDenied = retryTimesWhenAccessDenied;
	}
	
	public CaptchaResolver getCaptchaResolver() {
		return captchaResolver;
	}

	public void setCaptchaResolver(CaptchaResolver captchaResolver) {
		this.captchaResolver = captchaResolver;
	}
	
}
