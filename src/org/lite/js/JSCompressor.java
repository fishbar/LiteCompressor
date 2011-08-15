/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.lite.js;
import jargs.gnu.CmdLineParser;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import java.io.*;
import java.nio.charset.Charset;
import com.yahoo.platform.yui.compressor.*;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.regex.*;

/**
 *
 * @author fish
 */
public class JSCompressor {
	private boolean munge;
	private boolean preserveAllSemiColons;
	private boolean disableOptimizations;
	private String in_ext = "src.js";
	private String out_ext = ".js";
	private String revision=null;
	private JavaScriptCompressor compressor;
	public static void main(String args[]) {

		CmdLineParser parser = new CmdLineParser();
		//CmdLineParser.Option typeOpt = parser.addStringOption("t");
		CmdLineParser.Option confOpt = parser.addStringOption('c',"config_file_path");
		CmdLineParser.Option revisionOpt = parser.addStringOption('r',"revision");
		CmdLineParser.Option outputFilenameOpt = parser.addStringOption('o', "output");
		CmdLineParser.Option inputFilenameOpt = parser.addStringOption('i', "input");
		CmdLineParser.Option charsetOpt = parser.addStringOption("charset");
		CmdLineParser.Option helpOpt = parser.addBooleanOption('h', "help");
		CmdLineParser.Option iextOpt = parser.addStringOption("iext");
		CmdLineParser.Option oextOpt = parser.addStringOption("oext");
		CmdLineParser.Option nomungeOpt = parser.addBooleanOption("nomunge");
		CmdLineParser.Option preserveSemiOpt = parser.addBooleanOption("preserve-semi");
		CmdLineParser.Option disableOptimizationsOpt = parser.addBooleanOption("disable-optimizations");

		// parse cmd params
		try{
			parser.parse(args);
		}catch(Exception e){
			System.out.println("[ERROR_CONFIG]access denial, caused by wrong params");
			help();
			return;
		}
		/**
		 * ***************** help option *****************
		 */
		Boolean help = (Boolean)parser.getOptionValue(helpOpt);
		if(help != null && help.booleanValue()){
			help();
			return;
		}

	    /**
		    * ***************** charset option **************
		 */
		String charset = (String)parser.getOptionValue(charsetOpt);
		 if (charset == null || !Charset.isSupported(charset)) {
			charset = System.getProperty("file.encoding");
			if (charset == null) {
			    charset = "utf-8";
			}
		}

		String revision = (String) parser.getOptionValue(revisionOpt);
		/**
		 * ***************** Javascript optimize option *****
		 */
				/** get js compress config **/
		boolean munge = parser.getOptionValue(nomungeOpt) == null;
		boolean preserveAllSemiColons = parser.getOptionValue(preserveSemiOpt) != null;
		boolean disableOptimizations = parser.getOptionValue(disableOptimizationsOpt) != null;
		/**
		 * ***************** output ********************
		 */
		String output = (String)parser.getOptionValue(outputFilenameOpt);
		if(output == null){
			System.out.print("[ ERROR_CONFIG]need output path(or dir)");
			return ;
		}
		/**
			* ***************** input option ***************
		 */
		String input = (String)parser.getOptionValue(inputFilenameOpt);
		String cfg = (String)parser.getOptionValue(confOpt);

		JSCompressor compressor = new JSCompressor(revision,munge,preserveAllSemiColons,disableOptimizations);

		String iext = (String) parser.getOptionValue(iextOpt);
		String oext = (String) parser.getOptionValue(oextOpt);
		if(iext != null){
			compressor.setInputExt(iext);
		}
		if(oext != null){
			compressor.setOutputExt(oext);
		}
		//System.out.println(input);
		if(input != null){
			File input_f = new File(input);
			if(input_f.isDirectory()){			// dir file
				compressor.compressDir(input, output, charset);
			}else{							// single file
				compressor.comoressFile(input,output,charset);
			}
		}else if(cfg != null){

		}


		//compressor.compress(input,output,charset,true,true,true);
	}
	public JSCompressor(String _revision,boolean _munge, boolean _preserveAllSemiColons, boolean _disableOptimizations){
		this.revision = _revision;
		this.munge = _munge;
		this.preserveAllSemiColons = _preserveAllSemiColons;
		this.disableOptimizations = _disableOptimizations;
	}
	public void setInputExt(String _ext){
		this.in_ext = _ext;
	}
	public void setOutputExt(String _ext){
		this.out_ext = _ext;
	}
	public void comoressFile(String input,String output,String charset){
		System.out.println("1\t[Compress JS]" + input);
		compress(input,output,charset,revision,munge,preserveAllSemiColons,disableOptimizations);
	}
	public void compressDir(String input,String output,String charset){
		input = input.replaceAll("/$", "");
		output = output.replaceAll("/$", "");
		ArrayList files = getFileListByDir(input);
		// check output dir
		File out_dir = new File(output);
		if(!out_dir.exists()){
			if(!out_dir.mkdirs()){
				System.out.println("make output folder fail, please check pomission!");
				return;
			}
		}
		int i = 0;
		String regEx="^(\\w+)\\.([\\w\\-]+)$";
		Pattern p=Pattern.compile(regEx);
		for(Object f : files){
			i ++;
			String fname_with_encode = (String)f;
			String fname = fname_with_encode;
			Matcher m=p.matcher(fname_with_encode);
			if(m.find()){
				fname = m.group(1);
				charset = m.group(2);
			}
			
			System.out.println( i+"\t[Compress JS]" + fname);
			String _input = input +'/' + fname_with_encode + '.' + in_ext;
			String _output = output + '/' + fname + '.' + out_ext ;
			System.err.println("-->"+fname+"<--");
			compress(_input,_output,charset,revision,munge,preserveAllSemiColons,disableOptimizations);
			System.err.println("--EOF--");
		}
	}
	public void compressCfg(){

	}
	private  boolean compress(
			String input , // input file path
			String output , // output file path
			String charset , // charset
			String revison,	// revision mark
			boolean munge , // Minify only, do not obfuscate
			boolean preserveAllSemiColons,
			boolean disableOptimizations
	){
		Reader in = null;
		Writer out = null;
		try{
			 in = new InputStreamReader(new FileInputStream(input), charset);
		}catch(Exception e){
			e.printStackTrace();
		}
		//in =  new InputStreamReader(new FileInputStream(input), charset);
		try {
				compressor = new JavaScriptCompressor(in, new CEReporter());
				// Close the input stream first, and then .open the output stream,
				// in case the output file should override the input file.
				in.close();
				in = null;
				out = new OutputStreamWriter(new FileOutputStream(output), charset);

// revision write into file
//				if(revision != null){
//					out.write("/**\n * @author:ued.asc.alibaba.com\n * @revision:"+revision+"\n */\n");
//				}
				compressor.compress(out, -1, munge, false,preserveAllSemiColons, disableOptimizations);
		} catch (EvaluatorException e) {
			System.err.println("--EOF--");
			//e.printStackTrace();
			// Return a special error code used specifically by the web front-end.
			System.exit(1);
		}catch(IOException e){
			System.err.println("--EOF--");
			//e.printStackTrace();
			System.exit(1);
		}finally {
			if(in != null) {
				try{
					in.close();
					in = null;
				}catch(Exception e){}
			}
			if(out != null){
				try{
					out.close();
					out = null;
				}catch(Exception e){}
			}
		}
		return true;
	}
	private  ArrayList getFileListByDir(String tar){
		ArrayList files = new ArrayList();
		File dir = new File(tar);
		FileFilter ft = new FileFilter(in_ext);
		String[] ffs = dir.list(ft);
		Arrays.sort(ffs);
		for(String _f:ffs){
			_f = _f.replace('.'+in_ext,"");
			files.add(_f);
		}
		return files;
	}
	private static void help(){
		  System.out.println(
                "\nUsage: java -jar JSCompressor.jar [options] [input file]\n\n"

                        + "Global Options\n"
                        + "  -h, --help                Displays this information\n"
                        + "  --type <js|css>           Specifies the type of the input file\n"
                        + "  --charset <charset>       Read the input file using <charset>\n"
                        + "  --line-break <column>     Insert a line break after the specified column number\n"
                        + "  -v, --verbose             Display informational messages and warnings\n"
                        + "  -o <file>                 Place the output into <file>. Defaults to stdout.\n\n"

                        + "JavaScript Options\n"
                        + "  --nomunge                 Minify only, do not obfuscate\n"
                        + "  --preserve-semi           Preserve all semicolons\n"
                        + "  --disable-optimizations   Disable all micro optimizations\n\n"
		);
	}
}
class FileFilter implements  FilenameFilter {
	private String[] exts;
	public FileFilter(String ... _exts){
		exts = _exts;
	}
        @Override
        public boolean accept(File dir, String name) {
            File file = new File(dir, name);
            boolean accept = false;
            for (String ext : exts) {
                if (file.isDirectory()) break;
                if (name.toLowerCase().endsWith("." + ext.toLowerCase())) {
                    accept = true;
                    break;
                }
            }
            return accept;
        }
}

class  CEReporter implements ErrorReporter{
	public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
		if (line < 0) {
			System.err.println("[WARNING]" + message);
		} else {
			System.err.println("[WARNING]" + line + ":" + lineOffset + "%_%" + message + "%_%" + lineSource);
		}
	}

	public void error(String message, String sourceName,	int line, String lineSource, int lineOffset) {
		if (line < 0) {
			System.err.println("[ERROR_PACKER]" + message );
		} else {
			System.err.println("[ERROR_PACKER]" + line + ":" + lineOffset + "%_%" + message+"%_%" + lineSource);
		}
	}

	public EvaluatorException runtimeError(String message, String sourceName,int line, String lineSource, int lineOffset) {
		error(message, sourceName, line, lineSource, lineOffset);
		return new EvaluatorException(message);
	}
}