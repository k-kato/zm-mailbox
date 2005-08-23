/*
 * ***** BEGIN LICENSE BLOCK *****
 * Version: ZPL 1.1
 * 
 * The contents of this file are subject to the Zimbra Public License
 * Version 1.1 ("License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.zimbra.com/license
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 * 
 * The Original Code is: Zimbra Collaboration Suite.
 * 
 * The Initial Developer of the Original Code is Zimbra, Inc.
 * Portions created by Zimbra are Copyright (C) 2005 Zimbra, Inc.
 * All Rights Reserved.
 * 
 * Contributor(s): 
 * 
 * ***** END LICENSE BLOCK *****
 */

/*
 * Created on Jul 7, 2004
 */
package com.zimbra.cs.db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.zimbra.cs.util.Config;



/**
 * @author tim
 */
public class Versions {
	
    private static Log mLog = LogFactory.getLog(Versions.class);
	
	/**
	 * The DB_VERSION is stored into the config table of the DB when the DB is created.  
	 * If the DB_VERSION does not match our server's version, we will not run.
	 * 
	 * UPDATE THESE TO REQUIRE RESET-WORLD TO BE RUN
	 *  
	 */
	public static final String DB_VERSION = "16";

	/**
	 * The INDEX_VERSION is stored into the config table of the DB when the DB is created.  
	 * If the INDEX_VERSION does not match our server's version, we will not run.
	 *
	 * UPDATE THESE TO REQUIRE RESET-WORLD TO BE RUN
	 *  
	 */
	public static final String INDEX_VERSION = "2";

	

    /////////////////////////////////////////////////////////////
	// Called at boot time
    /////////////////////////////////////////////////////////////
	public static boolean checkVersions()
	{
		return (checkDBVersion() && checkIndexVersion());
	}
	
	public static boolean checkDBVersion()
	{
		String val = Config.getString("db.version", "0");
		if (val.equals(DB_VERSION)) {
			return true;
		} else {
			mLog.error("DB Version Mismatch: ours=\""+DB_VERSION+"\" from DB=\""+val+"\"");
			return false;
		}
	}
	
	public static boolean checkIndexVersion()
	{
		String val = Config.getString("index.version", "0");
		if (val.equals(INDEX_VERSION)) {
			return true;
		} else {
			mLog.error("Index Version Mismatch: ours=\""+INDEX_VERSION+"\" from DB=\""+val+"\"");
			return false;
		}
	}
	
	
    /////////////////////////////////////////////////////////////
	// main and command-line parsing
    /////////////////////////////////////////////////////////////

    public static void usage(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(Versions.class.getName(), options); 
        System.exit(1);
    }

    private static CommandLine parseCmdlineArgs(String args[], Options options) {
        CommandLineParser parser = new GnuParser();

        // Loose convention for naming options:
        //
        // Options applicable for normal, production usage have lowercase
        // letter.  Options for debugging, testing, or diagnostic
        // uses have uppercase letter.

        options.addOption("h", "help", false, "print usage");
        options.addOption("o", "outputdir", true, "output directory for version.sql");

        CommandLine cl = null;
        boolean err = false;
        try {
            cl = parser.parse(options, args);
        } catch (ParseException pe) {
            System.err.println("error: " + pe.getMessage());
            err = true;
        }
        
        if (err || cl.hasOption("h"))
            usage(options);

        return cl;
    }

    public static void main(String args[]) 
    {
        // command line argument parsing
        Options options = new Options();
        CommandLine cl = parseCmdlineArgs(args, options);

        String outputDir = cl.getOptionValue("o");
        File outFile = new File(outputDir, "versions-init.sql");
        
        outFile.delete();
        
        Writer output = null;
        
        try {
        	output = new BufferedWriter( new FileWriter(outFile) );
        	
        	String outStr =
        		"# AUTO-GENEARATED .SQL FILE - Generated by the Versions tool\n" +
				"use zimbra;\n" +
        		"INSERT INTO zimbra.config(name, value, description) VALUES\n" +
				"\t('db.version', '" + Versions.DB_VERSION + "', 'db schema version'),\n" + 
				"\t('index.version', '" + Versions.INDEX_VERSION + "', 'index version')\n" +
				";\ncommit;\n";
        	
        	output.write(outStr);
        	
        	if (output != null) {
        		output.close();
        	}
        	
        } catch(IOException e){
        	System.out.println("ERROR - caught exception at\n");
        	e.printStackTrace();
        	System.exit(-1);
        }
    }
        	
}
