/*
 * SampleParser.java
 *
 * Created on September 4, 2004, 4:32 PM
 * Written by Alexei Grigoriev
 *
 */

package org.tsho.dmc2.core.util;

/**
 * Wrapper class with methods to
 * find "words" (substrings delimited by spaces)
 * in argument strings.
 * @author  altair
 */
public class SampleParser {
    
    /** Creates a new instance of SampleParser */
    public SampleParser() {
    }
    
    /**starting from position pos, finds the next word delimited by spaces
     * unless pos==0, in which case it returns a word starting at pos=0,
     * if such a word is present, or the next word delimited by spaces.
     * It returns an array of two objects; first is the word found, the second
     * is Integer which is the position of the final character of the word found.
     * If no such word exists, the first returned object is null.
     * Caution: if the string starts with one character word, nextWord(string,0) returns
     * this character and the new position which is 0 in this case. Invoking
     * nextWord(string, new position) will give the same one character word
     * and the same new position 0.
     */
    public static Object[] nextWord(String str,int pos){
        Object [] result=new Object[2];
        if (str==null) return null;
        //go forward till you find a substring delimited by space ' ' characters.
        //return this substring.
        String s="";
        if (pos<0 || pos>=str.length()){
            result[0]=null;
            result[1]=new Integer(-1);
            return result;
        }
        try{
            if (pos!=0){
                while (str.charAt(pos)!=' ')
                    pos++;
            }
            while (str.charAt(pos)==' ')
                pos++;
        }
        catch(Exception e){//not the right format
            result[0]="";
            result[1]=new Integer(-1);
            return result;
        }
        try{
            while (str.charAt(pos)!=' '){
                s=s+str.charAt(pos);
                pos++;
            }
        }
        catch(Exception e){//substring end coincides with string end
            result[0]=s;
            result[1]=new Integer(pos-1);
            return result;
        }
        result[0]=s;
        result[1]=new Integer(pos-1);
        return result;
    }
    
    /** finds i-th word in s*/
    public static String find(String s,int i){
        String r=null;
        int pos=0;
        int pos1;
        for (int j=0;j<i;j++){
            Object [] o;
            o= nextWord(s,pos);
            r=(String) o[0];
            if (pos==0){
                pos=((Integer)o[1]).intValue()+1;
            }
            else{
                pos=((Integer)o[1]).intValue();
            }
        }
        if (pos<0)
            return null;
        else
            return r;
    }
    
    
    /** removes i-th word from s*/
    public static String remove(String s,int num){
        String r;
        int pos=0;
        int pos1;
        for (int i=0;i<num-1;i++){
            Object [] o;
            o= nextWord(s,pos);
            pos=((Integer)o[1]).intValue();
        }
        if (pos>=0){
            Object [] o;
            o= nextWord(s,pos);
            pos1=((Integer)o[1]).intValue();
            if (pos1>=0){
                String t1=s.substring(0,pos+1);
                String t2;
                if (pos1+1<=s.length()-1){
                    t2=s.substring(pos1+1,s.length());
                }
                else
                    t2="";
                r=t1+t2;
                return r;
            }
            else
                return s;
        }
        else{
            return s;
        }
    }
    
    /** inserts s1 into s, so that s1 comes after (num-1)-throws word*/
    public static String insert(String s,String s1,int num){
        String r;
        int pos=0;
        int pos1;
        for (int i=0;i<num-1;i++){
            Object [] o;
            o= nextWord(s,pos);
            pos=((Integer)o[1]).intValue();
        }
        if (pos>=0){
            String t1=s.substring(0,pos+1);
            String t2;
            if (pos+1<=s.length()-1){
                t2=s.substring(pos+1,s.length());
            }
            else
                t2="";
            return t1+s1+t2;
        }
        else{
            return s;
        }
    }
    
    public static void out(String s){
        System.out.println(s);
    }
    
    /** returns -1 if s==null*/
    public static int numberOfWords(String s){
        if (s==null) return -1;
        int c=0;
        int pos=0;
        Object [] o=nextWord(s,pos);
        pos=((Integer) o[1]).intValue();
        while (pos>=0){
            c++;
            o=nextWord(s,pos);
            pos=((Integer) o[1]).intValue();
        }
        return c;
    }
    
    public static boolean isEqualContent(String s1,String s2){
        int l1,l2;
        l1=numberOfWords(s1);
        l2=numberOfWords(s2);
        if (l1!=l2)
            return false;
        else{
            for (int i=0;i<l1;i++){
                if (!find(s1,i+1).equals(find(s2,i+1)))
                    return false;
                
            }
            return true;
        }
    }
    
    public static String toWord(String s){
        if (s==null)
            return null;
        String r="";
        for (int i=0;i<s.length();i++){
            if (s.charAt(i)!=' '){
                r=r+s.charAt(i);
            }
            else{
                r=r+'~';
            }
        }
        return r;
    }
    
    public static String toString(String s){
        String r="";
        for (int i=0;i<s.length();i++){
            if (s.charAt(i)!='~'){
                r=r+s.charAt(i);
            }
            else{
                r=r+' ';
            }
        }
        return r;
    }
    
    public static void main(String [] args){
        System.out.println(find("1 4 4  7",4));
        out(toWord(""));
        int n=numberOfWords(null);
    }
    
}

