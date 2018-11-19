package org.apache.shiro.biz.web.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.biz.utils.StringUtils;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * <p>Request Cros Filter, 对跨域提供支持</p>
 * @author <a href="https://github.com/vindell">vindell</a>
 * <p>https://blog.csdn.net/guodengh/article/details/73187908 </p>
 * <p>https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Access_control_CORS </p>
 * <p>https://developer.mozilla.org/zh-CN/docs/Web/HTTP/Headers/Access-Control-Allow-Headers </p>
 */
public class HttpServletRequestCrosFilter extends AccessControlFilter {

	public static final String DEFAULT_ACCESS_CONTROL_ALLOW_METHODS = "PUT,POST,GET,DELETE,OPTIONS";
	public static final String DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS = "Origin, X-Requested-With, Content-Type, Accept";

	private boolean accessControlAllowCredentials = false;
	private String accessControlAllowOrigin = "*";
	private String accessControlAllowMethods = DEFAULT_ACCESS_CONTROL_ALLOW_METHODS;
	private String accessControlAllowHeaders = DEFAULT_ACCESS_CONTROL_ALLOW_HEADERS;
	
	/** 对跨域提供支持 */
	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
			throws Exception {
		
		HttpServletRequest httpRequest = WebUtils.toHttp(request);
		HttpServletResponse httpResponse = WebUtils.toHttp(response);
		
		String allowOrigin = StringUtils.hasText(getAccessControlAllowOrigin()) ?  getAccessControlAllowOrigin() :  httpRequest.getHeader("Origin");
		String allowMethods =  StringUtils.hasText(getAccessControlAllowMethods()) ? getAccessControlAllowMethods() : DEFAULT_ACCESS_CONTROL_ALLOW_METHODS;
		String allowHeaders = StringUtils.hasText(getAccessControlAllowHeaders()) ?  getAccessControlAllowHeaders() :  httpRequest.getHeader("Access-Control-Request-Headers");
		
		// 服务器端 Access-Control-Allow-Credentials = true时，参数Access-Control-Allow-Origin 的值不能为 '*' 
		httpResponse.setHeader("Access-Control-Allow-Credentials", Boolean.toString(isAccessControlAllowCredentials()));
		httpResponse.setHeader("Access-Control-Allow-Origin", allowOrigin);
		httpResponse.setHeader("Access-Control-Allow-Methods", allowMethods);
		httpResponse.setHeader("Access-Control-Allow-Headers", allowHeaders);
		
		// 跨域时会首先发送一个option请求，这里我们给option请求直接返回正常状态
		if (httpRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
			httpResponse.setStatus(HttpServletResponse.SC_OK);
			return false;
		}
		return true;
	}


	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		return true;
	}
	
	public boolean isAccessControlAllowCredentials() {
		return accessControlAllowCredentials;
	}

	public void setAccessControlAllowCredentials(boolean accessControlAllowCredentials) {
		this.accessControlAllowCredentials = accessControlAllowCredentials;
	}

	public String getAccessControlAllowOrigin() {
		return accessControlAllowOrigin;
	}

	public void setAccessControlAllowOrigin(String accessControlAllowOrigin) {
		this.accessControlAllowOrigin = accessControlAllowOrigin;
	}

	public String getAccessControlAllowMethods() {
		return accessControlAllowMethods;
	}

	public void setAccessControlAllowMethods(String accessControlAllowMethods) {
		this.accessControlAllowMethods = accessControlAllowMethods;
	}

	public String getAccessControlAllowHeaders() {
		return accessControlAllowHeaders;
	}

	public void setAccessControlAllowHeaders(String accessControlAllowHeaders) {
		this.accessControlAllowHeaders = accessControlAllowHeaders;
	}
	
}