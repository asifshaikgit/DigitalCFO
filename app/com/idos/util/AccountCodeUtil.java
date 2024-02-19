package com.idos.util;

import java.math.BigInteger;

public class AccountCodeUtil {
	public static Long generateAccountCode(Long parentActCode,Long maxActCode){
		Long resultActCode=null;
		Long divisor = null;
		Long remainder=null;
		if(parentActCode!=null && maxActCode==null){
			int numOfBit=String.valueOf(parentActCode).length()-1;
			String one="1";
			for(int i=0;i<numOfBit;i++){
				one+="0";
				divisor=Long.parseLong(one);
				//divisor = new BigInteger(one).longValue();
			}
			remainder=parentActCode%divisor;
			if(remainder==0){
				numOfBit=numOfBit-1;
			}
			while(remainder!=0 && String.valueOf(remainder).length()>1){
				String newone="1";
				for(int j=0;j<numOfBit;j++){
					newone+="0";
					divisor=Long.parseLong(newone);
					//divisor = new BigInteger(one).longValue();
				}
				remainder=parentActCode%divisor;
				numOfBit=numOfBit-1;
			}
			if(remainder==0 || String.valueOf(remainder).length()==1){
				String ones="1";
				for(int i=0;i<numOfBit;i++){
					ones+="0";
					divisor=Long.parseLong(ones);
					//divisor = new BigInteger(one).longValue();
				}
				resultActCode=parentActCode+divisor;
			}
		}else{
			int numOfBit=String.valueOf(maxActCode).length()-1;
			String one="1";
			for(int j=0;j<numOfBit;j++){
				one+="0";
				divisor=Long.parseLong(one);
				//divisor = new BigInteger(one).longValue();
			}
			remainder=maxActCode%divisor;
			if(remainder==0){
				numOfBit=numOfBit-1;
			}
			while(remainder!=0 && String.valueOf(remainder).length()>1){
				String newone="1";
				for(int j=0;j<numOfBit;j++){
					newone+="0";
					divisor=Long.parseLong(newone);
					//divisor = new BigInteger(one).longValue();
				}
				remainder=maxActCode%divisor;
				numOfBit=numOfBit-1;
			}
			if(remainder==0 || String.valueOf(remainder).length()==1){
				resultActCode=maxActCode+divisor;
			}
		}
		return resultActCode;
	}
}
