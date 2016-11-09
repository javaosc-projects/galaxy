package org.javaosc.ratel.context;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
/**
 * 
 * @description
 * @author Dylan Tao
 * @date 2014-09-09
 * Copyright 2014 Javaosc Team. All Rights Reserved.
 */
public class CacheMark {
	
	private static HashMap<String, Boolean> tranCache;
	
	private static List<String> annoServiceCache;
	
	static{
		tranCache = new HashMap<String, Boolean>();
		annoServiceCache = new ArrayList<String>();
	}

	public static boolean getTran(Method method) {
		Boolean flag = tranCache.get(method.toString());
		if(flag!=null){
			return flag;
		}else{
			if(!annoServiceCache.contains(method.getClass().getPackage())){
				return false;
			}
			flag = true;
			if(ConfigHandler.getMethodKeyword()!=null){
				for(String keyword:ConfigHandler.getMethodKeyword()){
					if(method.getName().startsWith(keyword)){
						flag = false;
						break;
					}
				}
			}
			tranCache.put(method.toString(), flag);
			return flag;
		}
	}
	
	public static void putServiceAnno(Class<?> cls) {
		annoServiceCache.add(cls.getPackage().toString());
	}

}
