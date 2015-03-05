package io.miti.jarscan.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author mike
 */
public final class JarSearch
{
  /** The root file or directory. */
  private File root = null;
  
  /**
   * Default constructor.
   */
  public JarSearch()
  {
    super();
  }
  
  
  /**
   * Constructor taking the root directory or file.
   * 
   * @param home the root directory or file
   */
  public JarSearch(final String home)
  {
    // Check the input - it should either be a directory name or a file name
    if ((home == null) || (home.trim().length() == 0))
    {
      return;
    }
    
    File file = new File(home);
    if (!file.exists())
    {
      return;
    }
    
    root = file;
  }
  
  
  /**
   * Check if this is a valid class name.
   * 
   * @param className the class name to check
   * @return whether the name is valid
   */
  private boolean isValidClassName(final String className)
  {
    if ((className == null) || (className.trim().length() < 1))
    {
      return false;
    }
    
    return true;
  }
  
  
  /**
   * Search for the specified class name.
   * 
   * @param className the class name to search for
   */
  public void search(final String className)
  {
    if (root == null)
    {
      return;
    }
    else if (!isValidClassName(className))
    {
      return;
    }
    else if (root.isFile())
    {
      searchFile(root, className);
    }
    else
    {
      searchDir(root, className);
    }
  }
  
  
  /**
   * Return the canonical name of the input file.
   * 
   * @param file the input file
   * @return the canonical name
   */
  private String getFName(final File file)
  {
    String fname = null;
    try
    {
      fname = file.getCanonicalPath();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    
    return fname;
  }
  
  
  /**
   * Search the specified directory for the specified class name.
   * 
   * @param dir the input directory
   * @param className the class name to search for
   */
  private void searchDir(final File dir, final String className)
  {
    // Check for an empty list
    final String[] list = dir.list();
    if ((list == null) || (list.length == 0))
    {
      return;
    }
    
    // Iterate over the files and subdirectories
    final int size = list.length;
    for (int i = 0; i < size; ++i)
    {
      // System.out.println("Processing " + list[i]);
      final File child = new File(dir, list[i]);
      if (child.isFile())
      {
        searchFile(child, className);
      }
      else
      {
        searchDir(child, className);
      }
    }
  }
  
  
  /**
   * Search the specified jar file for the specified class name.
   * 
   * @param file the input file
   * @param className the class name
   */
  private void searchFile(final File file, final String className)
  {
    if (!getFName(file).toLowerCase().endsWith(".jar"))
    {
      return;
    }
    
    FileInputStream inStream = null;
    try
    {
      // Iterate over each entry in the input file
      inStream = new FileInputStream(file);
      ZipInputStream zis = new ZipInputStream(inStream);
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null)
      {
        // Check if this is a class file
        String ename = entry.getName().trim();
        if (!ename.toLowerCase().endsWith(".class"))
        {
          continue;
        }
        
        // Convert the jar file entry to a package name (slashes
        // become periods, and remove the '.class' from the end)
        ename = ename.replace('/', '.').replace('\\', '.')
                     .substring(0, ename.length() - 6);
        
        // If the class is in the default package, save the class
        // name as the modified entry name; else the class name
        // is just the text after the last period
        final int lastPeriod = ename.lastIndexOf('.');
        String cname = null;
        if (lastPeriod < 0)
        {
          cname = ename;
        }
        else
        {
          cname = ename.substring(lastPeriod + 1);
        }
        
        // System.out.println("Entry: " + ename);
        // System.out.println("  Name: " + cname);
        
        // Save the name of the input file
        final String fname = getFName(file);
        
        // Check if the current class name matches the target
        if (className.equalsIgnoreCase(cname))
        {
          // We have a match
          System.out.println(fname + ": " + ename);
        }
      }
      
      // Close the input streams
      zis.close();
      inStream.close();
      inStream = null;
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }
  
  
  /**
   * Write out info on using the tool.
   */
  private static void writeHelp()
  {
    System.out.println("JarScan - Search a file or directory for a Java class");
    System.out.println("Usage: java -jar jarscan.jar <dir/jar file> <class name>");
    System.out.println("Ex: jarscan . Logger");
    System.out.println("    jarscan ojdbc6.jar Logger");
  }
  
  
  /**
   * Validate the input arguments.
   * 
   * @param args the input arguments
   * @return whether the input is valid
   */
  private static boolean isHelpValid(final String[] args)
  {
    // Input format: JarSearch <dir/file name> <class name>
    if (args.length != 2)
    {
      writeHelp();
      return false;
    }
    
    // Save the input variables
    final String base = args[0];
    // final String cname = args[1];
    
    // Check the input file/directory
    File file = new File(base);
    if (!file.exists())
    {
      // The input file/dir doesn't exist.
      System.err.println("Error: The input directory/file " + base + " was not found");
      writeHelp();
      return false;
    }
    
    // The input must be valid, so return true
    return true;
  }
  
  
  /**
   * Entry point for the application.
   * 
   * @param args the input arguments
   */
  public static void main(final String[] args)
  {
    // Write out the system help and show the options
    if (!isHelpValid(args))
    {
      return;
    }
    
    // The options passed, so set the initial dir/file (args[0])
    // and then search for the class (args[1])
    JarSearch search = new JarSearch(args[0]);
    search.search(args[1]);
  }
}
