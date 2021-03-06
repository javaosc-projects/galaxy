package org.javaosc.galaxy.web;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.javaosc.galaxy.assist.MethodParamHandler;
import org.javaosc.galaxy.assist.RequestParamHandler;
import org.javaosc.galaxy.constant.Constant;
import org.javaosc.galaxy.constant.Constant.PatternValue;
import org.javaosc.galaxy.constant.RouteNode;
import org.javaosc.galaxy.util.GalaxyUtil;
/**
 * 
 * @description
 * @author Dylan Tao
 * @date 2014-09-09
 * Copyright 2014 Javaosc Team. All Rights Reserved.
 */

public abstract class RouteNodeRegistry {
	
	private static RouteNode root = new RouteNode();
	
	private static final Pattern paramPattern = Pattern.compile("\\{([a-zA-Z_]+[0-9]*)\\}");
	
	private static final String URI_PARAM = "_$URI_PARAM_";
	
	protected static final String ACTION = "_$ACTION_";
	
	protected static final String METHOD = "_$METHOD_";
	
	protected static final String METHOD_PRM = "_$METHOD_PRM_";
	
	protected static final String ERROR_CODE = "_ERROR_CODE_";
	
	static boolean isJdk8 = false;
	
	static{
		String jdkVersion = System.getProperty("java.version");
		isJdk8 = GalaxyUtil.isEmpty(jdkVersion)?false:jdkVersion.contains("1.8.")?true:false;
	}
	
	public static void registerRouteNode(String uriPattern, Object action, Method method){
		if(!GalaxyUtil.isEmpty(uriPattern)){
			uriPattern = GalaxyUtil.clearSpace(uriPattern, PatternValue.ALL);
			String[] routePath = uriPattern.split(Constant.URL_LINE);
			RouteNode current = root;
			RouteNode child = null;
			int uriLength = routePath.length;
			
			for (int i = 0;i<uriLength;i++){
				String urlSplitStr = routePath[i];
				if (Constant.EMPTY.equals(urlSplitStr)) continue;
				child = current.getChild(urlSplitStr);
				if (child == null){
					if(urlSplitStr.startsWith("{")){
						Matcher matcher = paramPattern.matcher(urlSplitStr);
						if (matcher.matches()){
							if((child = current.getChild(URI_PARAM)) == null){
								child = new RouteNode(matcher.group(1));
								if(uriLength-i == 1){ 
									child.setAction(action);
									child.setMethod(method);
									String[] methodPrm = isJdk8 ? MethodParamHandler.getParamNameByJdk8(method) : MethodParamHandler.getParamName(method);
									child.setParam(methodPrm);	
								}
								current.addChild(URI_PARAM, child);
							}
						}
					}else{
						child = new RouteNode();
						if(uriLength-i == 1){ 
							child.setAction(action);
							child.setMethod(method);
							String[] methodPrm = isJdk8 ? MethodParamHandler.getParamNameByJdk8(method) : MethodParamHandler.getParamName(method);
							child.setParam(methodPrm);	
						}
						current.addChild(urlSplitStr, child);
					}
				}
				current = child;
				child = null;
			}
		}
	}
	
	protected static Map<String, Object> getRouteNode(String uri){
		Map<String, Object> params = new HashMap<String, Object>();
		String[] routePath = uri.split(Constant.URL_LINE);
		RouteNode current = root;
		RouteNode child = null;
		int uriLength = routePath.length;
		
		for (int i = 0;i<uriLength;i++){
			String urlSplitStr = routePath[i];
			if (Constant.EMPTY.equals(urlSplitStr)) continue;
			child = current.getChild(urlSplitStr);
			if (child == null){
				child = current.getChild(URI_PARAM);
				if (child != null){
					RequestParamHandler.put(child.getParamName(), urlSplitStr);
				}else{
					params.put(ERROR_CODE, 0);
					break;
				}
			}
			
			if(uriLength-i == 1){ 
				params.put(ACTION, child.getAction());
				params.put(METHOD, child.getMethod());
				params.put(METHOD_PRM, child.getParam());
			}
			current = child;
			child = null;
		}
		return params;
	}
	
	public static void clear(){
		root = null;
	}
	
}
