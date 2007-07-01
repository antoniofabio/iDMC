/*
 * iDMC the interactive Dynamical Model Calculator simulates and performs
 * graphical and numerical analysis of systems of differential and
 * difference equations.
 *
 * Copyright (C) 2004-2007 Marji Lines and Alfredo Medio.
 *
 * Written by Daniele Pizzoni <auouo@tin.it>.
 * Extended by Alexei Grigoriev <alexei_grigoriev@libero.it>.
 *
 *
 * The software program was developed within a research project financed
 * by the Italian Ministry of Universities, the Universities of Udine and
 * Ca'Foscari of Vence, the Friuli-Venezia Giulia Region.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 */
package org.tsho.dmc2;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.tsho.dmc2.ui.MainFrame;

/**
 *
 * @author Daniele Pizzoni <auouo@tin.it>
 */
public class DmcDue {

    public static class Defaults {
        public static final String name = "iDmc";
        public static String version;
        public static String nativeLibVersion;
        public static String nativeLibName;
        public static final String modelsDir=System.getProperty("user.dir") + "/models";
        public static boolean debug=false;

        public static void dump() {
            System.out.println("name: " + name);
            System.out.println("version: " + version);
            System.out.println("nativeLibName: " + nativeLibName);
            System.out.println("modelsDir: " + modelsDir);
            System.out.println("debug: " + debug);
        }
    }
 
    
    public static String getNativeLibName() {
    	return Defaults.nativeLibName;
    }

    public static void main(String[] args) {
        if(true) { /*version infos initialization*/
            Defaults.version = Version.getVersionString();
            if (System.getProperty("os.name").startsWith("Windows")) {
                Defaults.nativeLibName = System.getProperty("user.dir") + "\\jidmclib.dll";
            }
            else if (System.getProperty("os.name").startsWith("Linux")) {
                Defaults.nativeLibName = System.getProperty("user.dir") + "/jidmclib.so";
            }
            else {
                Defaults.nativeLibName = null;
            }
        }


        /* java version check */
        String javaVersion = System.getProperty("java.version");
        if (javaVersion.compareTo("1.4") < 0)
        {
            System.err.println("You are running Java version "
                + javaVersion + ".");
            System.err.println("iDmc requires Java 1.4 or later.");
            System.exit(1);
        }

        /* arguments parsing, inspired by jEdit */
        File file = null;
        String libPath = null;
        Level logLevel = Level.OFF;
        for(int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if(arg == null)
                continue;
            else if(arg.length() == 0)
                args[i] = null;
            else if(arg.startsWith("-"))
            {
                if(arg.equals("-help"))
                {
                    System.out.print(Defaults.name);
                    System.out.print(" ");
                    System.out.println(Defaults.version);
                    System.exit(0);
                }
                else if(arg.equals("-version"))
                {
                    System.out.println(Defaults.version);
                    System.exit(0);
                }
                else if(arg.equals("-debug"))
                {
                   Defaults.debug = true;
                }
                else if(arg.startsWith("-loglevel="))
                {
                    String subArg = arg.substring(10);
                    
                    if (subArg.equals("all"))
                        logLevel = Level.ALL;
                    else if (subArg.equals("none"))
                        logLevel = Level.OFF;
                    else if (subArg.equals("warning"))
                        logLevel = Level.WARNING;
                    else
                        System.err.println("Malformed option: " + arg);
                }
                else if(arg.startsWith("-library=")) {
                    Defaults.nativeLibName = arg.substring(9);
                }
                else if(arg.startsWith("-model=")) {
                    String fileName = arg.substring(7);
                    file = new File(fileName);
                }
                else
                {
                    System.err.println("Unknown option: " + arg);
                    System.exit(1);
                }
            }
        }

        if (Defaults.debug == true) {
            debugProperties();
        }

        System.load(getNativeLibName());
        Defaults.nativeLibVersion = Version.getNativeLibVersionString();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        new MainFrame(file);        
    }

    private static void debugProperties() {
        System.out.println("System properties.");
        System.out.println("OS name: " + System.getProperty("os.name"));
        System.out.println("OS version: " + System.getProperty("os.version"));

        System.out.println("Java vendor: " + System.getProperty("java.vendor"));
        System.out.println("Java version: " + System.getProperty("java.version"));
        System.out.println("JRE specification version: " + 
                           System.getProperty("java.specification.version"));

        System.out.println();
        System.out.println("User properties.");
        System.out.println("user.name: " + System.getProperty("user.name"));
        System.out.println("user.dir: " + System.getProperty("user.dir"));
        System.out.println("user.home: " + System.getProperty("user.home"));

        System.out.println();
        System.out.println("dmcDue defaults.");
        Defaults.dump();
        System.out.println();
    }
}
