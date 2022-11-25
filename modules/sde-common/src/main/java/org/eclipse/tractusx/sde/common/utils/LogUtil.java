package org.eclipse.tractusx.sde.common.utils;

import org.apache.commons.text.StringEscapeUtils;

public class LogUtil {
	
	private LogUtil() {
	}
	
	public static String encode(String message) {
		return StringEscapeUtils.unescapeHtml4(StringEscapeUtils.escapeJava(message));
	}

}
