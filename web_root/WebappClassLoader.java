

package org.apache.catalina.loader;

import java.io.File;
import java.io.FilePermission;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandlerFactory;
import java.net.URLStreamHandler;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;

import javax.naming.directory.DirContext;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.naming.NameClassPair;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;

import org.apache.naming.JndiPermission;
import org.apache.naming.resources.ResourceAttributes;
import org.apache.naming.resources.Resource;

public class WebappClassLoader extends URLClassLoader implements Reloader, Lifecycle {

    protected class PrivilegedFindResource
        implements PrivilegedAction {

        private String name;
        private String path;

        PrivilegedFindResource(String name, String path) {
            this.name = name;
            this.path = path;
        }

        public Object run() {
            return findResourceInternal(name, path);
        }

    }




    private static final String[] triggers = {
        "javax.servlet.Servlet"                     // Servlet API
    };


    private static final String[] packageTriggers = {
        "javax",                                     // Java extensions
        "org.xml.sax",                               // SAX 1 & 2
        "org.w3c.dom",                               // DOM 1 & 2
        "org.apache.xerces",                         // Xerces 1 & 2
        "org.apache.xalan"                           // Xalan
    };




    public WebappClassLoader() {

        super(new URL[0]);
        this.parent = getParent();
        system = getSystemClassLoader();
        securityManager = System.getSecurityManager();

        if (securityManager != null) {
            refreshPolicy();
        }

    }


    public WebappClassLoader(ClassLoader parent) {

        super(new URL[0], parent);
        this.parent = getParent();
        system = getSystemClassLoader();
        securityManager = System.getSecurityManager();

        if (securityManager != null) {
            refreshPolicy();
        }

    }




    protected DirContext resources = null;


    protected ArrayList available = new ArrayList();


    protected HashMap resourceEntries = new HashMap();


    protected HashMap notFoundResources = new HashMap();


    protected int debug = 0;


    protected boolean delegate = false;


    protected String[] repositories = new String[0];


    protected File[] files = new File[0];


    protected JarFile[] jarFiles = new JarFile[0];


    protected File[] jarRealFiles = new File[0];


    protected String jarPath = null;


    protected String[] jarNames = new String[0];


    protected long[] lastModifiedDates = new long[0];


    protected String[] paths = new String[0];


    protected ArrayList required = new ArrayList();


    private ArrayList permissionList = new ArrayList();


    private HashMap loaderPC = new HashMap();


    private SecurityManager securityManager = null;


    private ClassLoader parent = null;


    private ClassLoader system = null;


    protected boolean started = false;


    protected boolean hasExternalRepositories = false;


    private Permission allPermission = new java.security.AllPermission();




    public DirContext getResources() {

        return this.resources;

    }


    public void setResources(DirContext resources) {

        this.resources = resources;

    }


    public int getDebug() {

        return (this.debug);

    }


    public void setDebug(int debug) {

        this.debug = debug;

    }


    public boolean getDelegate() {

        return (this.delegate);

    }


    public void setDelegate(boolean delegate) {

        this.delegate = delegate;

    }


    public void addPermission(String path) {
        if (securityManager != null) {
            Permission permission = null;
            if( path.startsWith("jndi:") || path.startsWith("jar:jndi:") ) {
            } else {
                permission = new FilePermission(path + "-","read");
            }
            addPermission(permission);
        }
    }


    public void addPermission(URL url) {
        addPermission(url.toString());
    }


    public void addPermission(Permission permission) {
        if ((securityManager != null) && (permission != null)) {
            permissionList.add(permission);
        }
    }


    public String getJarPath() {

        return this.jarPath;

    }


    public void setJarPath(String jarPath) {

        this.jarPath = jarPath;

    }




    public void addRepository(String repository) {

        if (repository.startsWith("/WEB-INF/lib")
            || repository.startsWith("/WEB-INF/classes"))
            return;

        try {
            URL url = new URL(repository);
            super.addURL(url);
            hasExternalRepositories = true;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e.toString());
        }

    }


    synchronized void addRepository(String repository, File file) {


        if (repository == null)
            return;

        if (debug >= 1)
            log("addRepository(" + repository + ")");

        int i;

        String[] result = new String[repositories.length + 1];
        for (i = 0; i < repositories.length; i++) {
            result[i] = repositories[i];
        }
        result[repositories.length] = repository;
        repositories = result;

        File[] result2 = new File[files.length + 1];
        for (i = 0; i < files.length; i++) {
            result2[i] = files[i];
        }
        result2[files.length] = file;
        files = result2;

    }


    synchronized void addJar(String jar, JarFile jarFile, File file)
        throws IOException {

        if (jar == null)
            return;
        if (jarFile == null)
            return;
        if (file == null)
            return;

        if (debug >= 1)
            log("addJar(" + jar + ")");

        int i;

        if ((jarPath != null) && (jar.startsWith(jarPath))) {

            String jarName = jar.substring(jarPath.length());
            while (jarName.startsWith("/"))
                jarName = jarName.substring(1);

            String[] result = new String[jarNames.length + 1];
            for (i = 0; i < jarNames.length; i++) {
                result[i] = jarNames[i];
            }
            result[jarNames.length] = jarName;
            jarNames = result;

        }

        try {


            long lastModified =
                ((ResourceAttributes) resources.getAttributes(jar))
                .getLastModified();

            String[] result = new String[paths.length + 1];
            for (i = 0; i < paths.length; i++) {
                result[i] = paths[i];
            }
            result[paths.length] = jar;
            paths = result;

            long[] result3 = new long[lastModifiedDates.length + 1];
            for (i = 0; i < lastModifiedDates.length; i++) {
                result3[i] = lastModifiedDates[i];
            }
            result3[lastModifiedDates.length] = lastModified;
            lastModifiedDates = result3;

        } catch (NamingException e) {
        }

        if (!validateJarFile(file))
            return;

        JarFile[] result2 = new JarFile[jarFiles.length + 1];
        for (i = 0; i < jarFiles.length; i++) {
            result2[i] = jarFiles[i];
        }
        result2[jarFiles.length] = jarFile;
        jarFiles = result2;

        File[] result4 = new File[jarRealFiles.length + 1];
        for (i = 0; i < jarRealFiles.length; i++) {
            result4[i] = jarRealFiles[i];
        }
        result4[jarRealFiles.length] = file;
        jarRealFiles = result4;

        Manifest manifest = jarFile.getManifest();
        if (manifest != null) {
            Iterator extensions = Extension.getAvailable(manifest).iterator();
            while (extensions.hasNext()) {
                available.add(extensions.next());
            }
            extensions = Extension.getRequired(manifest).iterator();
            while (extensions.hasNext()) {
                required.add(extensions.next());
            }
        }

    }


    public Extension[] findAvailable() {

        ArrayList results = new ArrayList();
        Iterator available = this.available.iterator();
        while (available.hasNext())
            results.add(available.next());

        ClassLoader loader = this;
        while (true) {
            loader = loader.getParent();
            if (loader == null)
                break;
            if (!(loader instanceof WebappClassLoader))
                continue;
            Extension extensions[] =
                ((WebappClassLoader) loader).findAvailable();
            for (int i = 0; i < extensions.length; i++)
                results.add(extensions[i]);
        }

        Extension extensions[] = new Extension[results.size()];
        return ((Extension[]) results.toArray(extensions));

    }


    public String[] findRepositories() {

        return (repositories);

    }


    public Extension[] findRequired() {

        ArrayList results = new ArrayList();
        Iterator required = this.required.iterator();
        while (required.hasNext())
            results.add(required.next());

        ClassLoader loader = this;
        while (true) {
            loader = loader.getParent();
            if (loader == null)
                break;
            if (!(loader instanceof WebappClassLoader))
                continue;
            Extension extensions[] =
                ((WebappClassLoader) loader).findRequired();
            for (int i = 0; i < extensions.length; i++)
                results.add(extensions[i]);
        }

        Extension extensions[] = new Extension[results.size()];
        return ((Extension[]) results.toArray(extensions));

    }


    public boolean modified() {

        if (debug >= 2)
            log("modified()");

        int length = paths.length;

        int length2 = lastModifiedDates.length;
        if (length > length2)
            length = length2;

        for (int i = 0; i < length; i++) {
            try {
                long lastModified =
                    ((ResourceAttributes) resources.getAttributes(paths[i]))
                    .getLastModified();
                if (lastModified != lastModifiedDates[i]) {
                    log("  Resource '" + paths[i]
                        + "' was modified; Date is now: "
                        + new java.util.Date(lastModified) + " Was: "
                        + new java.util.Date(lastModifiedDates[i]));
                    return (true);
                }
            } catch (NamingException e) {
                log("    Resource '" + paths[i] + "' is missing");
                return (true);
            }
        }

        length = jarNames.length;

        if (getJarPath() != null) {

            try {
                NamingEnumeration enum = resources.listBindings(getJarPath());
                int i = 0;
                while (enum.hasMoreElements() && (i < length)) {
                    NameClassPair ncPair = (NameClassPair) enum.nextElement();
                    String name = ncPair.getName();
                    if (!name.endsWith(".jar"))
                        continue;
                    if (!name.equals(jarNames[i])) {
                        log("    Additional JARs have been added : '" 
                            + name + "'");
                        return (true);
                    }
                    i++;
                }
                if (enum.hasMoreElements()) {
                    while (enum.hasMoreElements()) {
                        NameClassPair ncPair = 
                            (NameClassPair) enum.nextElement();
                        String name = ncPair.getName();
                        if (name.endsWith(".jar")) {
                            log("    Additional JARs have been added");
                            return (true);
                        }
                    }
                } else if (i < jarNames.length) {
                    log("    Additional JARs have been added");
                    return (true);
                }
            } catch (NamingException e) {
                if (debug > 2)
                    log("    Failed tracking modifications of '"
                        + getJarPath() + "'");
            } catch (ClassCastException e) {
                log("    Failed tracking modifications of '"
                    + getJarPath() + "' : " + e.getMessage());
            }

        }

        return (false);

    }


    public String toString() {

        StringBuffer sb = new StringBuffer("WebappClassLoader\r\n");
        sb.append("  available:\r\n");
        Iterator available = this.available.iterator();
        while (available.hasNext()) {
            sb.append("    ");
            sb.append(available.next().toString());
            sb.append("\r\n");
        }
        sb.append("  delegate: ");
        sb.append(delegate);
        sb.append("\r\n");
        sb.append("  repositories:\r\n");
        for (int i = 0; i < repositories.length; i++) {
            sb.append("    ");
            sb.append(repositories[i]);
            sb.append("\r\n");
        }
        sb.append("  required:\r\n");
        Iterator required = this.required.iterator();
        while (required.hasNext()) {
            sb.append("    ");
            sb.append(required.next().toString());
            sb.append("\r\n");
        }
        if (this.parent != null) {
            sb.append("----------> Parent Classloader:\r\n");
            sb.append(this.parent.toString());
            sb.append("\r\n");
        }
        return (sb.toString());

    }




    public Class findClass(String name) throws ClassNotFoundException {

        if (debug >= 3)
            log("    findClass(" + name + ")");

        if (securityManager != null) {
            int i = name.lastIndexOf('.');
            if (i >= 0) {
                try {
                    if (debug >= 4)
                        log("      securityManager.checkPackageDefinition");
                    securityManager.checkPackageDefinition(name.substring(0,i));
                } catch (Exception se) {
                    if (debug >= 4)
                        log("      -->Exception-->ClassNotFoundException", se);
                    throw new ClassNotFoundException(name);
                }
            }
        }

        Class clazz = null;
        try {
            if (debug >= 4)
                log("      findClassInternal(" + name + ")");
            try {
                clazz = findClassInternal(name);
            } catch(ClassNotFoundException cnfe) {
                if (!hasExternalRepositories) {
                    throw cnfe;
                }
            } catch(AccessControlException ace) {
                ace.printStackTrace();
                throw new ClassNotFoundException(name);
            } catch (RuntimeException e) {
                if (debug >= 4)
                    log("      -->RuntimeException Rethrown", e);
                throw e;
            }
            if ((clazz == null) && hasExternalRepositories) {
                try {
                    clazz = super.findClass(name);
                } catch(AccessControlException ace) {
                    throw new ClassNotFoundException(name);
                } catch (RuntimeException e) {
                    if (debug >= 4)
                        log("      -->RuntimeException Rethrown", e);
                    throw e;
                }
            }
            if (clazz == null) {
                if (debug >= 3)
                    log("    --> Returning ClassNotFoundException");
                throw new ClassNotFoundException(name);
            }
        } catch (ClassNotFoundException e) {
            if (debug >= 3)
                log("    --> Passing on ClassNotFoundException", e);
            throw e;
        }

        if (debug >= 4)
            log("      Returning class " + clazz);
        if ((debug >= 4) && (clazz != null))
            log("      Loaded by " + clazz.getClassLoader());
        return (clazz);

    }


    public URL findResource(final String name) {

        if (debug >= 3)
            log("    findResource(" + name + ")");

        URL url = null;

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry == null) {
            if (securityManager != null) {
                PrivilegedAction dp =
                    new PrivilegedFindResource(name, name);
                entry = (ResourceEntry)AccessController.doPrivileged(dp);
            } else {
                entry = findResourceInternal(name, name);
            }
        }
        if (entry != null) {
            url = entry.source;
        }

        if ((url == null) && hasExternalRepositories)
            url = super.findResource(name);

        if (debug >= 3) {
            if (url != null)
                log("    --> Returning '" + url.toString() + "'");
            else
                log("    --> Resource not found, returning null");
        }
        return (url);

    }


    public Enumeration findResources(String name) throws IOException {

        if (debug >= 3)
            log("    findResources(" + name + ")");

        Vector result = new Vector();

        int jarFilesLength = jarFiles.length;
        int repositoriesLength = repositories.length;

        int i;

        for (i = 0; i < repositoriesLength; i++) {
            try {
                String fullPath = repositories[i] + name;
                resources.lookup(fullPath);
                try {
                    result.addElement(getURL(new File(files[i], name)));
                } catch (MalformedURLException e) {
                }
            } catch (NamingException e) {
            }
        }

        for (i = 0; i < jarFilesLength; i++) {
            JarEntry jarEntry = jarFiles[i].getJarEntry(name);
            if (jarEntry != null) {
                try {
                    String jarFakeUrl = getURL(jarRealFiles[i]).toString();
                    jarFakeUrl = "jar:" + jarFakeUrl + "!/" + name;
                    result.addElement(new URL(jarFakeUrl));
                } catch (MalformedURLException e) {
                }
            }
        }

        if (hasExternalRepositories) {

            Enumeration otherResourcePaths = super.findResources(name);

            while (otherResourcePaths.hasMoreElements()) {
                result.addElement(otherResourcePaths.nextElement());
            }

        }

        return result.elements();

    }


    public URL getResource(String name) {

        if (debug >= 2)
            log("getResource(" + name + ")");
        URL url = null;

        if (delegate) {
            if (debug >= 3)
                log("  Delegating to parent classloader");
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            url = loader.getResource(name);
            if (url != null) {
                if (debug >= 2)
                    log("  --> Returning '" + url.toString() + "'");
                return (url);
            }
        }

        if (debug >= 3)
            log("  Searching local repositories");
        url = findResource(name);
        if (url != null) {
            if (debug >= 2)
                log("  --> Returning '" + url.toString() + "'");
            return (url);
        }

        if( !delegate ) {
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            url = loader.getResource(name);
            if (url != null) {
                if (debug >= 2)
                    log("  --> Returning '" + url.toString() + "'");
                return (url);
            }
        }

        if (debug >= 2)
            log("  --> Resource not found, returning null");
        return (null);

    }


    public InputStream getResourceAsStream(String name) {

        if (debug >= 2)
            log("getResourceAsStream(" + name + ")");
        InputStream stream = null;

        stream = findLoadedResource(name);
        if (stream != null) {
            if (debug >= 2)
                log("  --> Returning stream from cache");
            return (stream);
        }

        if (delegate) {
            if (debug >= 3)
                log("  Delegating to parent classloader");
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            stream = loader.getResourceAsStream(name);
            if (stream != null) {
                if (debug >= 2)
                    log("  --> Returning stream from parent");
                return (stream);
            }
        }

        if (debug >= 3)
            log("  Searching local repositories");
        URL url = findResource(name);
        if (url != null) {
            if (debug >= 2)
                log("  --> Returning stream from local");
            stream = findLoadedResource(name);
            try {
                if (hasExternalRepositories && (stream == null))
                    stream = url.openStream();
            } catch (IOException e) {
                ; // Ignore
            }
            if (stream != null)
                return (stream);
        }

        if (!delegate) {
            if (debug >= 3)
                log("  Delegating to parent classloader");
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            stream = loader.getResourceAsStream(name);
            if (stream != null) {
                if (debug >= 2)
                    log("  --> Returning stream from parent");
                return (stream);
            }
        }

        if (debug >= 2)
            log("  --> Resource not found, returning null");
        return (null);

    }


    public Class loadClass(String name) throws ClassNotFoundException {

        return (loadClass(name, false));

    }


    public Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException {

        if (debug >= 2)
            log("loadClass(" + name + ", " + resolve + ")");
        Class clazz = null;

        if (!started) {
            log("Lifecycle error : CL stopped");
            throw new ClassNotFoundException(name);
        }

        clazz = findLoadedClass0(name);
        if (clazz != null) {
            if (debug >= 3)
                log("  Returning class from cache");
            if (resolve)
                resolveClass(clazz);
            return (clazz);
        }

        clazz = findLoadedClass(name);
        if (clazz != null) {
            if (debug >= 3)
                log("  Returning class from cache");
            if (resolve)
                resolveClass(clazz);
            return (clazz);
        }

        try {
            clazz = system.loadClass(name);
            if (clazz != null) {
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
        }

        if (securityManager != null) {
            int i = name.lastIndexOf('.');
            if (i >= 0) {
                try {
                    securityManager.checkPackageAccess(name.substring(0,i));
                } catch (SecurityException se) {
                    String error = "Security Violation, attempt to use " +
                        "Restricted Class: " + name;
                    System.out.println(error);
                    se.printStackTrace();
                    log(error);
                    throw new ClassNotFoundException(error);
                }
            }
        }

        boolean delegateLoad = delegate || filter(name);

        if (delegateLoad) {
            if (debug >= 3)
                log("  Delegating to parent classloader");
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            try {
                clazz = loader.loadClass(name);
                if (clazz != null) {
                    if (debug >= 3)
                        log("  Loading class from parent");
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                ;
            }
        }

        if (debug >= 3)
            log("  Searching local repositories");
        try {
            clazz = findClass(name);
            if (clazz != null) {
                if (debug >= 3)
                    log("  Loading class from local repository");
                if (resolve)
                    resolveClass(clazz);
                return (clazz);
            }
        } catch (ClassNotFoundException e) {
            ;
        }

        if (!delegateLoad) {
            if (debug >= 3)
                log("  Delegating to parent classloader");
            ClassLoader loader = parent;
            if (loader == null)
                loader = system;
            try {
                clazz = loader.loadClass(name);
                if (clazz != null) {
                    if (debug >= 3)
                        log("  Loading class from parent");
                    if (resolve)
                        resolveClass(clazz);
                    return (clazz);
                }
            } catch (ClassNotFoundException e) {
                ;
            }
        }

        throw new ClassNotFoundException(name);

    }


    protected PermissionCollection getPermissions(CodeSource codeSource) {

        String codeUrl = codeSource.getLocation().toString();
        PermissionCollection pc;
        if ((pc = (PermissionCollection)loaderPC.get(codeUrl)) == null) {
            pc = super.getPermissions(codeSource);
            if (pc != null) {
                Iterator perms = permissionList.iterator();
                while (perms.hasNext()) {
                    Permission p = (Permission)perms.next();
                    pc.add(p);
                }
                loaderPC.put(codeUrl,pc);
            }
        }
        return (pc);

    }


    public URL[] getURLs() {

        URL[] external = super.getURLs();

        int filesLength = files.length;
        int jarFilesLength = jarRealFiles.length;
        int length = filesLength + jarFilesLength + external.length;
        int i;

        try {

            URL[] urls = new URL[length];
            for (i = 0; i < length; i++) {
                if (i < filesLength) {
                    urls[i] = getURL(files[i]);
                } else if (i < filesLength + jarFilesLength) {
                    urls[i] = getURL(jarRealFiles[i - filesLength]);
                } else {
                    urls[i] = external[i - filesLength - jarFilesLength];
                }
            }

            return urls;

        } catch (MalformedURLException e) {
            return (new URL[0]);
        }

    }




    public void addLifecycleListener(LifecycleListener listener) {
    }


    public LifecycleListener[] findLifecycleListeners() {
        return new LifecycleListener[0];
    }


    public void removeLifecycleListener(LifecycleListener listener) {
    }


    public void start() throws LifecycleException {

        started = true;

    }


    public void stop() throws LifecycleException {

        started = false;

        int length = jarFiles.length;
        for (int i = 0; i < length; i++) {
            try {
                jarFiles[i].close();
                jarFiles[i] = null;
            } catch (IOException e) {
            }
        }

        notFoundResources.clear();
        resourceEntries.clear();
        repositories = new String[0];
        files = new File[0];
        jarFiles = new JarFile[0];
        jarRealFiles = new File[0];
        jarPath = null;
        jarNames = new String[0];
        lastModifiedDates = new long[0];
        paths = new String[0];
        hasExternalRepositories = false;

        required.clear();
        permissionList.clear();
        loaderPC.clear();

    }




    protected Class findClassInternal(String name)
        throws ClassNotFoundException {

        if (!validate(name))
            throw new ClassNotFoundException(name);

        String tempPath = name.replace('.', '/');
        String classPath = tempPath + ".class";

        ResourceEntry entry = null;

        if (securityManager != null) {
            PrivilegedAction dp =
                new PrivilegedFindResource(name, classPath);
            entry = (ResourceEntry)AccessController.doPrivileged(dp);
        } else {
            entry = findResourceInternal(name, classPath);
        }

        if ((entry == null) || (entry.binaryContent == null))
            throw new ClassNotFoundException(name);

        Class clazz = entry.loadedClass;
        if (clazz != null)
            return clazz;

        String packageName = null;
        int pos = name.lastIndexOf('.');
        if (pos != -1)
            packageName = name.substring(0, pos);

        Package pkg = null;

        if (packageName != null) {

            pkg = getPackage(packageName);

            if (pkg == null) {
                if (entry.manifest == null) {
                    definePackage(packageName, null, null, null, null, null,
                                  null, null);
                } else {
                    definePackage(packageName, entry.manifest, entry.codeBase);
                }
            }

        }

        CodeSource codeSource =
            new CodeSource(entry.codeBase, entry.certificates);

        if (securityManager != null) {

            if (pkg != null) {
                boolean sealCheck = true;
                if (pkg.isSealed()) {
                    sealCheck = pkg.isSealed(entry.codeBase);
                } else {
                    sealCheck = (entry.manifest == null)
                        || !isPackageSealed(packageName, entry.manifest);
                }
                if (!sealCheck)
                    throw new SecurityException
                        ("Sealing violation loading " + name + " : Package "
                         + packageName + " is sealed.");
            }

        }

        if (entry.loadedClass == null) {
            synchronized (this) {
                if (entry.loadedClass == null) {
                    clazz = defineClass(name, entry.binaryContent, 0,
                                        entry.binaryContent.length, 
                                        codeSource);
                    entry.loadedClass = clazz;
                } else {
                    clazz = entry.loadedClass;
                }
            }
        } else {
            clazz = entry.loadedClass;
        }

        return clazz;

    }


    protected ResourceEntry findResourceInternal(String name, String path) {

        if (!started) {
            log("Lifecycle error : CL stopped");
            return null;
        }

        if ((name == null) || (path == null))
            return null;

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry != null)
            return entry;

        int contentLength = -1;
        InputStream binaryStream = null;

        int jarFilesLength = jarFiles.length;
        int repositoriesLength = repositories.length;

        int i;

        Resource resource = null;

        for (i = 0; (entry == null) && (i < repositoriesLength); i++) {
            try {

                String fullPath = repositories[i] + path;

                Object lookupResult = resources.lookup(fullPath);
                if (lookupResult instanceof Resource) {
                    resource = (Resource) lookupResult;
                }


                entry = new ResourceEntry();
                try {
                    entry.source = getURL(new File(files[i], path));
                    entry.codeBase = entry.source;
                } catch (MalformedURLException e) {
                    return null;
                }
                ResourceAttributes attributes =
                    (ResourceAttributes) resources.getAttributes(fullPath);
                contentLength = (int) attributes.getContentLength();
                entry.lastModified = attributes.getLastModified();

                if (resource != null) {

                    try {
                        binaryStream = resource.streamContent();
                    } catch (IOException e) {
                        return null;
                    }

                    synchronized (allPermission) {

                        int j;

                        long[] result2 = 
                            new long[lastModifiedDates.length + 1];
                        for (j = 0; j < lastModifiedDates.length; j++) {
                            result2[j] = lastModifiedDates[j];
                        }
                        result2[lastModifiedDates.length] = entry.lastModified;
                        lastModifiedDates = result2;

                        String[] result = new String[paths.length + 1];
                        for (j = 0; j < paths.length; j++) {
                            result[j] = paths[j];
                        }
                        result[paths.length] = fullPath;
                        paths = result;

                    }

                }

            } catch (NamingException e) {
            }
        }

        if ((entry == null) && (notFoundResources.containsKey(name)))
            return null;

        JarEntry jarEntry = null;

        for (i = 0; (entry == null) && (i < jarFilesLength); i++) {

            jarEntry = jarFiles[i].getJarEntry(path);

            if (jarEntry != null) {

                entry = new ResourceEntry();
                try {
                    entry.codeBase = getURL(jarRealFiles[i]);
                    String jarFakeUrl = entry.codeBase.toString();
                    jarFakeUrl = "jar:" + jarFakeUrl + "!/" + path;
                    entry.source = new URL(jarFakeUrl);
                } catch (MalformedURLException e) {
                    return null;
                }
                contentLength = (int) jarEntry.getSize();
                try {
                    entry.manifest = jarFiles[i].getManifest();
                    binaryStream = jarFiles[i].getInputStream(jarEntry);
                } catch (IOException e) {
                    return null;
                }
            }

        }

        if (entry == null) {
            synchronized (notFoundResources) {
                notFoundResources.put(name, name);
            }
            return null;
        }

        if (binaryStream != null) {

            byte[] binaryContent = new byte[contentLength];

            try {
                int pos = 0;
                while (true) {
                    int n = binaryStream.read(binaryContent, pos,
                                              binaryContent.length - pos);
                    if (n <= 0)
                        break;
                    pos += n;
                }
                binaryStream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            entry.binaryContent = binaryContent;

            if (jarEntry != null) {
                entry.certificates = jarEntry.getCertificates();
            }

        }

        synchronized (resourceEntries) {
            ResourceEntry entry2 = (ResourceEntry) resourceEntries.get(name);
            if (entry2 == null) {
                resourceEntries.put(name, entry);
            } else {
                entry = entry2;
            }
        }

        return entry;

    }


    protected boolean isPackageSealed(String name, Manifest man) {

        String path = name + "/";
        Attributes attr = man.getAttributes(path);
        String sealed = null;
        if (attr != null) {
            sealed = attr.getValue(Name.SEALED);
        }
        if (sealed == null) {
            if ((attr = man.getMainAttributes()) != null) {
                sealed = attr.getValue(Name.SEALED);
            }
        }
        return "true".equalsIgnoreCase(sealed);

    }


    protected InputStream findLoadedResource(String name) {

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry != null) {
            if (entry.binaryContent != null)
                return new ByteArrayInputStream(entry.binaryContent);
        }
        return (null);

    }


    protected Class findLoadedClass0(String name) {

        ResourceEntry entry = (ResourceEntry) resourceEntries.get(name);
        if (entry != null) {
            return entry.loadedClass;
        }
        return (null);  // FIXME - findLoadedResource()

    }


    protected void refreshPolicy() {

        try {
            Policy policy = Policy.getPolicy();
            policy.refresh();
        } catch (AccessControlException e) {
        }

    }


    protected boolean filter(String name) {

        if (name == null)
            return false;

        String packageName = null;
        int pos = name.lastIndexOf('.');
        if (pos != -1)
            packageName = name.substring(0, pos);
        else
            return false;

        for (int i = 0; i < packageTriggers.length; i++) {
            if (packageName.startsWith(packageTriggers[i]))
                return true;
        }

        return false;

    }


    protected boolean validate(String name) {

        if (name == null)
            return false;
        if (name.startsWith("java."))
            return false;

        return true;

    }


    private boolean validateJarFile(File jarfile)
        throws IOException {

        if (triggers == null)
            return (true);
        JarFile jarFile = new JarFile(jarfile);
        for (int i = 0; i < triggers.length; i++) {
            Class clazz = null;
            try {
                if (parent != null) {
                    clazz = parent.loadClass(triggers[i]);
                } else {
                    clazz = Class.forName(triggers[i]);
                }
            } catch (Throwable t) {
                clazz = null;
            }
            if (clazz == null)
                continue;
            String name = triggers[i].replace('.', '/') + ".class";
            if (debug >= 2)
                log(" Checking for " + name);
            JarEntry jarEntry = jarFile.getJarEntry(name);
            if (jarEntry != null) {
                log("validateJarFile(" + jarfile + 
                    ") - jar not loaded. See Servlet Spec 2.3, "
                    + "section 9.7.2. Offending class: " + name);
                jarFile.close();
                return (false);
            }
        }
        jarFile.close();
        return (true);

    }


    protected URL getURL(File file)
        throws MalformedURLException {

        File realFile = file;
        try {
            realFile = realFile.getCanonicalFile();
        } catch (IOException e) {
        }

        return realFile.toURL();

    }


    private void log(String message) {

        System.out.println("WebappClassLoader: " + message);

    }


    private void log(String message, Throwable throwable) {

        System.out.println("WebappClassLoader: " + message);
        throwable.printStackTrace(System.out);

    }


}

