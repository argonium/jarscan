# Jarscan
Jarscan is a Java command-line application for searching a single JAR file or a directory of JAR files for a class.  Two examples of running it are:

* jarscan . Logger
* jarscan lucene.jar Logger

The first option searches the current directory for any JAR file containing Logger.class.

The second option searches only lucene.jar (in the current directory) for Logger.class.

To build the application, use Ant to run 'ant clean dist'.  This will produce jarscan.jar.  Run it via 'java -jar jarscan.jar \<dir/JAR file\> \<class name\>'.

The source code is released under the MIT license.
