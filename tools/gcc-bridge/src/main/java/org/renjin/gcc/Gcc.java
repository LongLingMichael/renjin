package org.renjin.gcc;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.renjin.gcc.gimple.GimpleCompilationUnit;
import org.renjin.gcc.gimple.GimpleParser;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Gcc {

  private File workingDirectory;
  private File pluginLibrary;

  private List<File> includeDirectories = Lists.newArrayList();

  private static final Logger LOGGER = Logger.getLogger(Gcc.class.getName());

  private boolean debug;
  
  public Gcc() {
    workingDirectory = Files.createTempDir();

  }

  public void setPluginLibrary(File pluginLibrary) {
    this.pluginLibrary = pluginLibrary;
  }

  public void setDebug(boolean debug) {
    this.debug = debug;
  }

  public GimpleCompilationUnit compileToGimple(File source) throws IOException {
    
    checkEnvironment();
    
    List<String> arguments = Lists.newArrayList();
    
    // cross compile to i386 so that our pointers are 32-bits 
    // rather than 64-bit. We use arrays to back pointers,
    // and java arrays can only be indexed by 32-bit integers
    if(is64bit()) {
      arguments.add("-m32");
    }

    arguments.add("-c"); // compile only, do not link
    arguments.add("-S"); // stop at assembly generation
    // command.add("-O9"); // highest optimization

    // Enable our plugin which dumps the Gimple as JSON
    // to standard out

    arguments.add("-fplugin=" + pluginLibrary.getAbsolutePath());
    arguments.add("-fplugin-arg-bridge-json-output-file=gimple.json");

    for (File includeDir : includeDirectories) {
      arguments.add("-I");
      arguments.add(includeDir.getAbsolutePath());
    }

    arguments.add(source.getAbsolutePath());

    LOGGER.info("Executing " + Joiner.on(" ").join(arguments));

    callGcc(arguments);

    String json = Files.toString(new File(workingDirectory, "gimple.json"), Charsets.UTF_8);
    if(debug) {
      System.out.println(json);
    }
    
    GimpleParser parser = new GimpleParser();
    return parser.parse(new StringReader(json));
  }

  /**
   * Executes GCC and returns the standard output and error
   * @param arguments
   * @return
   * @throws IOException
   * @throws GccException if the GCC process does not exit successfully
   */
  private String callGcc(List<String> arguments) throws IOException {
    List<String> command = Lists.newArrayList();
    command.add("gcc");
    command.addAll(arguments);

    Process gcc = new ProcessBuilder().command(command).directory(workingDirectory).redirectErrorStream(true).start();
    
    try {
      gcc.waitFor();
    } catch (InterruptedException e) {
      throw new GccException("Compiler interrupted");
    }

    String output = new String(ByteStreams.toByteArray(gcc.getInputStream()));

    if (gcc.exitValue() != 0) {

      if(output.contains("error trying to exec 'f951': execvp: No such file or directory")) {
        throw new GccException("Compilation failed: Fortran compiler is missing:\n" + output);
      }

      throw new GccException("Compilation failed:\n" + output);
    }
    return output;
  }

  private void checkEnvironment() {
    if(PlatformUtils.OS == PlatformUtils.OSType.WINDOWS) {
      throw new GccException("Sorry, gcc-bridge does not work on Windows/Cygwin because of problems building \n" +
              "and linking the required gcc plugin. You can still compile on a *NIX platform and use the " +
              "resulting pure-Java class files on any platform.");
    }
  }

  private boolean is64bit() {
    return Strings.nullToEmpty(System.getProperty("os.arch")).contains("64");
  }

  public void addIncludeDirectory(File path) {
    includeDirectories.add(path);
  }
  
  public void extractPlugin() throws IOException {
    String libraryName = PlatformUtils.getPortableLibraryName("gcc-bridge");
    
    
    URL pluginResource;
    try {
      pluginResource = Resources.getResource("org/renjin/gcc/" + libraryName);
    } catch(IllegalArgumentException e) {
      throw new GccException("Could not find a bundled version of the gcc plugin for your platform.\n" +
              "(Was expecting: /org/renjin/gcc/" +  libraryName + " on the classpath.)\n" +
              "You will need to build it yourself and specify the path to the binary. ");
    }
    
    pluginLibrary = new File(workingDirectory, "bridge" + PlatformUtils.getExtension());
    
    Files.copy(Resources.newInputStreamSupplier(pluginResource), pluginLibrary);

    pluginLibrary.deleteOnExit();
  }
  
  public void checkVersion() {
    try {
      String versionOutput = callGcc(Arrays.asList("--version"));
      if (!versionOutput.contains("4.6.3")) {
        System.err.println("WARNING: gcc-bridge has been tested against 4.6.3, other versions may not work correctly.");
      }
    } catch (IOException e) {
      throw new GccException("Failed to start GCC: " + e.getMessage() + ".\n" +
              "Make sure gcc 4.6.3 is installed." );
    }
  }

}
