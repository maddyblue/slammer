// JarLoader.java

import java.io.*;
import java.util.*;
import java.util.jar.*;

/**
 * A JarLoader object is able to load all the jar files in a given
 * directory containing classes of a given type. It can also be set
 * to continuously track that directory for new jar files that are
 * added later.
 * Listeners can be added to JarLoader objects which will be notified
 * when the list of valid jar files in that directory changes.
 *<br>
 * Also contains a static method for dynamically loading a given jar file.
 *
 * @author Melinda Green - Superliminal Software http://www.superliminal.com
 */
public class JarLoader  {

	private Class target;
	final private String jarClassKey;
	private boolean watchForChanges;
	private HashMap name2class = new HashMap();
	private int last_jar_file_count = -1; //tracks when listeners need to be notified.
	private File directory;
	public File getDirectory() { return directory; }


	/**
	 * Used to discover the names of all classes currently
	 * loaded by this JarLoader instance.
	 * @return an array of the names of all the
	 * classes currently loaded by this JarLoader.
	 * Note, this list is suitable for display in a JComboBox.
	 */
	public String[] getLoadedClassNames() {
		Object keys[] = null;
		keys = name2class.keySet().toArray(new String[0]);
		return (String[])keys;
	}

	/**
	 * Retrives the Class object for a previously loaded class.
	 * @param name is the name of a previously loaded class
	 * presumably reported in a previous call to getLoadedClassNames.
	 * @return the class object corrisponding to the loaded
	 * class with the given name, or null if not found.
	 */
	public Class getClass(String name) {
		return (Class)name2class.get(name);
	}

	/**
	 * Creates a JarLoader to which loads from and possibly tracks jar
	 * files of a given type in a directory.
	 * The loaded classes are available via <code>getClass(String name)</code>
	 * where "name" was presumably gotten from <code>getLoadedClassNames()</code>.
	 * @param jarClassKey is the key to look for in each jar's manifest file.
	 * JarLoader will load one class from each jar file who's manifest file
	 * contains a key/value attribute pair of the following form:
	 * <jarClassKey>: <Class name>
	 * <br>Example: <code>new JarLoader("My-Component", MyComponent.class, "plugins", false)</code><br>
	 * will load all classes of type MyComponent identified by the
	 * "My-Component" key in each jar file's manifest file for each jar file
	 * found in the "plugins" directory.
	 * @param containing is a base class or interface which loaded classes
	 * must inherit from or implement in order to be loaded.
	 * @param dir is the directory in which to look for jar files.
	 * @param watchDir is a flag stating whether this JarLoader should
	 * continuously monitor the given directory for changes in the list of
	 * loadable jar files found there.
	 */
	public JarLoader(String jarClassKey, Class containing, String dir) {
		directory = new File(dir);
		if(!directory.isDirectory())
			throw new java.security.InvalidParameterException(directory + " not a directory");
		this.jarClassKey = jarClassKey;
		target = containing;
		load(); //always loads jar files in given directory at least once.
	}

	/**
	 * The directory version of <code>load(File...)</code> which loads all conforming
	 * Classes found in the jar files of the directory with which this JarLoader
	 * instance was created.
	 */
	private void load() {
		File jar_files[] = directory.listFiles(new FileFilter()  {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".jar");
			}
		});
		if(jar_files.length == last_jar_file_count)
			return; //nothing to do so go back to sleep
		name2class.clear();
		//System.out.println("found " + jar_files.length + " jar files");
		last_jar_file_count = jar_files.length; // remember for next time
		for(int i=0; i<jar_files.length; i++)  {
			Class loaded_class = load(jar_files[i], jarClassKey, name2class);
			if(loaded_class!=null && target.isAssignableFrom(loaded_class)) {
				name2class.put(loaded_class.getName(), loaded_class);
			}
		}
	}


	/**
	 * The workhorse of JarLoader which performs the actual dynamic loading
	 * of a single jar file.
	 * @param jarFile is a java File object representing the jar file to load.
	 * @param jarClassKey is the key to look for in each jar's manifest file.
	 * @param name2class is an optional HashMap which, if supplied, will return
	 * null if the class in the given jar file has the same name as a String
	 * key in the HashMap. In all cases the given HashMap is left unchanged.
	 * @return The Class loaded from the jar file or null on error.
	 */
	 public static Class load(File jarFile, String jarClassKey, HashMap name2class) {
		JarFile the_jar = null;
		try {
			final JarFile ajar = new JarFile(jarFile);
			the_jar = ajar; // so it can be closed regardless of exceptions
			Manifest manifest = ajar.getManifest();
			Map map = manifest.getEntries();
			Attributes att = manifest.getMainAttributes();
			final String loaded_class_name = att.getValue(jarClassKey);
			if(loaded_class_name == null) {
				System.out.println("can't find class to load in manifest at the key: " +
					jarClassKey);
				return null;
			}
			if(name2class!=null && name2class.get(loaded_class_name) != null)
				return (Class)name2class.get(loaded_class_name); // already have this one
			//System.out.println("loading " + loaded_class_name);
			Class loaded_class = new ClassLoader()  {
				public Class findClass(String name) {
					JarEntry loaded_class_entry = ajar.getJarEntry(name);
					if(loaded_class_entry == null)
						return(null);
					try {
						InputStream is = ajar.getInputStream(loaded_class_entry);
						int available = is.available();
						byte data[] = new byte[available];
						is.read(data);
						return defineClass(name, data, 0, data.length);
					}
					catch(IOException ioe)  {
						System.out.println("Exception: " + ioe);
						return(null);
					}
				}
			}.loadClass(loaded_class_name);
			return loaded_class;
		}
		catch(Exception e) {
			System.out.println("Exception: " + e);
			return null;
		}
		finally { //insures jar file is always closed regardless of exceptions
			if(the_jar != null) {
				try { the_jar.close(); }
				catch(IOException ioe) {}
			}
		}
	} // end load(File...)
}

