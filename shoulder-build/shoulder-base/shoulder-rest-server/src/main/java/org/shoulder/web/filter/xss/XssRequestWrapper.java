package org.shoulder.web.filter.xss;

import org.shoulder.core.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * xss 安全保护
 * @author lym
 */
public class XssRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 脚本正则
     */
    private static final Pattern[] SCRIPT_PATTERNS = new Pattern[]{
        // Avoid anything between script tags
        Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE),
        // Avoid anything in a src='...' type of expression
        Pattern.compile("src[\\s]*=[\\s]*\\\'(.*?)\\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("src[\\s]*=[\\s]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Remove any lonesome </script> tag
        Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
        // Remove any lonesome <script ...> tag
        Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid eval(...) expressions
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid expression(...) expressions
        Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // Avoid javascript:... expressions
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        // Avoid vbscript:... expressions
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        // Avoid onload= expressions
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };

    /**
     * html 转义符
     */
    private static final Map<String, String> HTML_ESCAPE_CHARACTER_MAP = new HashMap<>();
    static {
        HTML_ESCAPE_CHARACTER_MAP.put("<", "&lt;");
        HTML_ESCAPE_CHARACTER_MAP.put(">", "&gt;");
        HTML_ESCAPE_CHARACTER_MAP.put("\\(", "&#x28;");
        HTML_ESCAPE_CHARACTER_MAP.put("\\)", "&#x29;");
        HTML_ESCAPE_CHARACTER_MAP.put("'", "&#x27;");
        HTML_ESCAPE_CHARACTER_MAP.put("\"", "&quot;");
        HTML_ESCAPE_CHARACTER_MAP.put("/", "&#x2f;");
    }

    private static String charset;

    public XssRequestWrapper(HttpServletRequest servletRequest) {
        super(servletRequest);
        charset = super.getCharacterEncoding();
    }

    /**
     * 对数组参数进行特殊字符过滤
     */
    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null) {
            return null;
        }
        int count = values.length;
        String[] encodedValues = new String[count];
        for (int i = 0; i < count; i++) {
            encodedValues[i] = securityContext(values[i]);
        }
        return encodedValues;
    }

    /**
     * 对参数中特殊字符进行过滤
     */
    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (StringUtils.isEmpty(value)) {
            return value;
        }
        return securityContext(value);
    }

    /**
     * 获取attribute,特殊字符过滤
     */
    @Override
    public Object getAttribute(String name) {
        Object value = super.getAttribute(name);
        if (value instanceof String) {
            securityContext((String) value);
        }
        return value;
    }


    /**
     * 去除请求头中的脚本
     */
    @Override
    public String getHeader(String name) {
        String value = super.getHeader(name);
        return securityContext(value);
    }

    @Override
    public String getQueryString() {
        return securityContext(super.getQueryString());
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> map = super.getParameterMap();
        Map<String, String[]> newMap = new HashMap<>();
        for (String key : map.keySet()) {
            String[] values = map.get(key);
            if (values != null && values.length > 0) {
                String[] newValues = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    newValues[i] = securityContext(values[i]);
                }
                newMap.put(key, newValues);
            } else {
                newMap.put(key, values);
            }
        }
        return newMap;
    }

    /**
     * 过滤脚本攻击，将html字符转义
     * todo 开启是否转义等
     */
    private String securityContext(String context) {
        return filterHtmlEscape(stripXSS(context));
    }

    /**
     * 去掉 script 脚本关键字
     */
    private String stripXSS(String context) {
        if(StringUtils.isEmpty(context)){
            return context;
        }
        try {
            if (charset == null) {
                // todo 支持修改
                charset = StandardCharsets.UTF_8.name();
            }
            context = URLDecoder.decode(context, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            return context;
        }
        // 开始替换
        context = context.replaceAll("\0", "");

        // 去除脚本正则
        for (Pattern scriptPattern : SCRIPT_PATTERNS) {
            context = scriptPattern.matcher(context).replaceAll("");
        }
        return filterHtmlEscape(context);
    }

    /**
     * 对 html 进行转换
     */
    private String filterHtmlEscape(String context) {
        for(Map.Entry<String, String> entry : HTML_ESCAPE_CHARACTER_MAP.entrySet()){
            String escapeChar = entry.getKey();
            String convertChar = entry.getValue();
            context = context.replace(escapeChar, convertChar);
        }
        return context;
    }
}