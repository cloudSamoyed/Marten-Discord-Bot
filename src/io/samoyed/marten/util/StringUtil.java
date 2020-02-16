package io.samoyed.marten.util;

public class StringUtil {

	public static String decoratePronouns(String[] pronouns, int size, boolean includeDittos) {
		String str = "";
		int length = 0;
		
		if(!includeDittos) {
			for(String s: pronouns) {
				
				if(length >= size)
					break;
				
				boolean ditto = false;
				if(str.contains("/")) {
					for(String c : str.split("/")) {
						if (c.equals(s)) {
							ditto = true;
							break;
						}
					}
				}
				
				if(ditto)
					continue;
				
				str+=s+"/";
				length+=1;
				
			}
			
			return str.substring(0, str.length()-1);
		}
		
		for(int i = 0; i < size; i++) {
			str+=pronouns[i] + "/";
		}
		return str.substring(0, str.length()-1);
	}
	
	public static String appendStringArray(String[] array, String seperator, int start, int cutoff) {
		String str = array[start];
		for(int i = start+1; i < array.length-cutoff; i++)
			str+= seperator + array[i];
		return str;
	}
	
	public static String[] cropStringArray(String[] strArray, int start, int cutoff) {
		String[] newArray = new String[strArray.length - start - cutoff];
		int a = 0;
		for(int i = start; i < strArray.length-cutoff; i++) {
			newArray[a] = strArray[i];
			a++;
		}
		return newArray;
	}
}
