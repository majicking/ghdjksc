package com;

import java.util.regex.*;
public class test {

        public static void main(String[] args){
            String s="abcd\nefg:3123123";
            Pattern p=Pattern.compile("\\:(.*)");

            Matcher m=p.matcher(s);

            while(m.find()){

                System.out.println(m.group(1));

            }
        }

}
