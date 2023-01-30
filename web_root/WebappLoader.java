package org.apache.catalina.loader;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.security.Permission;
import java.util.jar.JarFile;
import javax.servlet.ServletContext;
import javax.naming.NamingException;
import javax.naming.Binding;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;

import org.apache.naming.resources.Resource;
import org.apache.naming.resources.DirContextURLStreamHandler;
import org.apache.naming.resources.DirContextURLStreamHandlerFactory;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.DefaultContext;
import org.apache.catalina.Globals;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Logger;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;

public class WebappLoader
        implements Lifecycle, Loader, PropertyChangeListener, Runnable {
    public WebappLoader() {
        this(null);
    }

    public WebappLoader(ClassLoader parent) {
        super();
        this.parentClassLoader = parent;
    }

    private int checkInterval = 15;
    private WebappClassLoader classLoader = null;
    private Container container = null;
    private int debug = 0;
    protected DefaultContext defaultContext = null;
    private boolean delegate = false;
    private static final String info = "org.apache.catalina.loader.WebappLoader/1.0";
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    private String loaderClass = "org.apache.catalina.loader.WebappClassLoader";
    private ClassLoader parentClassLoader = null;
    private boolean reloadable = false;
    private String repositories[] = new String[0];
    protected static final StringManager sm = StringManager.getManager(Constants.Package);
    private boolean started = false;
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);
    private Thread thread = null;
    private boolean threadDone = false;
    private String threadName = "WebappLoader";

    public int getCheckInterval() {
        return (this.checkInterval);
    }

    public void setCheckInterval(int checkInterval) {
        int oldCheckInterval = this.checkInterval;
        this.checkInterval = checkInterval;
        support.firePropertyChange("checkInterval", new Integer(oldCheckInterval), new Integer(this.checkInterval));
    }

    /**
     * 没有set方法，它是通过loaderClass这个私有变量来切换类加载器的 {@link org.apache.catalina.loader.WebappLoader#setLoaderClass}
     */
    public ClassLoader getClassLoader() {
        return ((ClassLoader) classLoader);
    }

    public Container getContainer() {
        return (container);
    }

    public void setContainer(Container container) {
        if ((this.container != null) && (this.container instanceof Context))
            ((Context) this.container).removePropertyChangeListener(this);
        Container oldContainer = this.container;
        this.container = container;
        support.firePropertyChange("container", oldContainer, this.container);
        if ((this.container != null) && (this.container instanceof Context)) {
            setReloadable(((Context) this.container).getReloadable());
            ((Context) this.container).addPropertyChangeListener(this);
        }
    }

    public DefaultContext getDefaultContext() {
        return (this.defaultContext);
    }

    public void setDefaultContext(DefaultContext defaultContext) {
        DefaultContext oldDefaultContext = this.defaultContext;
        this.defaultContext = defaultContext;
        support.firePropertyChange("defaultContext", oldDefaultContext, this.defaultContext);
    }

    public int getDebug() {
        return (this.debug);
    }

    public void setDebug(int debug) {
        int oldDebug = this.debug;
        this.debug = debug;
        support.firePropertyChange("debug", new Integer(oldDebug), new Integer(this.debug));
    }

    public boolean getDelegate() {
        return (this.delegate);
    }

    public void setDelegate(boolean delegate) {
        boolean oldDelegate = this.delegate;
        this.delegate = delegate;
        support.firePropertyChange("delegate", new Boolean(oldDelegate), new Boolean(this.delegate));
    }

    public String getInfo() {
        return (info);
    }

    public String getLoaderClass() {
        return (this.loaderClass);
    }

    public void setLoaderClass(String loaderClass) {
        this.loaderClass = loaderClass;
    }

    public boolean getReloadable() {
        return (this.reloadable);
    }

    public void setReloadable(boolean reloadable) {
        boolean oldReloadable = this.reloadable;
        this.reloadable = reloadable;
        support.firePropertyChange("reloadable", new Boolean(oldReloadable), new Boolean(this.reloadable));
        if (!started)
            return;
        if (!oldReloadable && this.reloadable)
            threadStart();
        else if (oldReloadable && !this.reloadable)
            threadStop();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void addRepository(String repository) {
        if (debug >= 1)
            log(sm.getString("webappLoader.addRepository", repository));
        for (int i = 0; i < repositories.length; i++) {
            if (repository.equals(repositories[i]))
                return;
        }
        String results[] = new String[repositories.length + 1];
        for (int i = 0; i < repositories.length; i++)
            results[i] = repositories[i];
        results[repositories.length] = repository;
        repositories = results;
        if (started && (classLoader != null)) {
            classLoader.addRepository(repository);
            setClassPath();
        }
    }

    public String[] findRepositories() {
        return (repositories);
    }

    public boolean modified() {
        return (classLoader.modified());
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("WebappLoader[");
        if (container != null)
            sb.append(container.getName());
        sb.append("]");
        return (sb.toString());
    }

    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }

    public LifecycleListener[] findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }

    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }

    public void start() throws LifecycleException {
        if (started)
            throw new LifecycleException
                    (sm.getString("webappLoader.alreadyStarted"));
        if (debug >= 1)
            log(sm.getString("webappLoader.starting"));
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;
        if (container.getResources() == null)
            return;
        URLStreamHandlerFactory streamHandlerFactory =
                new DirContextURLStreamHandlerFactory();
        try {
            URL.setURLStreamHandlerFactory(streamHandlerFactory);
        } catch (Throwable t) {
        }
        try {
            classLoader = createClassLoader();
            classLoader.setResources(container.getResources());
            classLoader.setDebug(this.debug);
            classLoader.setDelegate(this.delegate);
            for (int i = 0; i < repositories.length; i++) {
                classLoader.addRepository(repositories[i]);
            }
            setRepositories();
            setClassPath();
            setPermissions();
            if (classLoader instanceof Lifecycle)
                ((Lifecycle) classLoader).start();
            DirContextURLStreamHandler.bind
                    ((ClassLoader) classLoader, this.container.getResources());
        } catch (Throwable t) {
            throw new LifecycleException("start: ", t);
        }
        validatePackages();
        if (reloadable) {
            log(sm.getString("webappLoader.reloading"));
            try {
                threadStart();
            } catch (IllegalStateException e) {
                throw new LifecycleException(e);
            }
        }
    }

    public void stop() throws LifecycleException {
        if (!started)
            throw new LifecycleException
                    (sm.getString("webappLoader.notStarted"));
        if (debug >= 1)
            log(sm.getString("webappLoader.stopping"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;
        if (reloadable)
            threadStop();
        if (container instanceof Context) {
            ServletContext servletContext =
                    ((Context) container).getServletContext();
            servletContext.removeAttribute(Globals.CLASS_PATH_ATTR);
        }
        if (classLoader instanceof Lifecycle)
            ((Lifecycle) classLoader).stop();
        DirContextURLStreamHandler.unbind((ClassLoader) classLoader);
        classLoader = null;
    }

    public void propertyChange(PropertyChangeEvent event) {
        if (!(event.getSource() instanceof Context))
            return;
        Context context = (Context) event.getSource();
        if (event.getPropertyName().equals("reloadable")) {
            try {
                setReloadable
                        (((Boolean) event.getNewValue()).booleanValue());
            } catch (NumberFormatException e) {
                log(sm.getString("webappLoader.reloadable",
                        event.getNewValue().toString()));
            }
        }
    }

    /**
     * 通过loaderClass全类名字符串创建类加载器
     * @return
     * @throws Exception
     */
    private WebappClassLoader createClassLoader() throws Exception {
        Class clazz = Class.forName(loaderClass);
        WebappClassLoader classLoader = null;
        if (parentClassLoader == null) {
            classLoader = (WebappClassLoader) clazz.newInstance();
        } else {
            Class[] argTypes = {ClassLoader.class};
            Object[] args = {parentClassLoader};
            Constructor constr = clazz.getConstructor(argTypes);
            classLoader = (WebappClassLoader) constr.newInstance(args);
        }
        return classLoader;
    }

    private void log(String message) {
        Logger logger = null;
        if (container != null)
            logger = container.getLogger();
        if (logger != null)
            logger.log("WebappLoader[" + container.getName() + "]: "
                    + message);
        else {
            String containerName = null;
            if (container != null)
                containerName = container.getName();
            System.out.println("WebappLoader[" + containerName
                    + "]: " + message);
        }
    }

    private void log(String message, Throwable throwable) {
        Logger logger = null;
        if (container != null)
            logger = container.getLogger();
        if (logger != null) {
            logger.log("WebappLoader[" + container.getName() + "] "
                    + message, throwable);
        } else {
            String containerName = null;
            if (container != null)
                containerName = container.getName();
            System.out.println("WebappLoader[" + containerName
                    + "]: " + message);
            System.out.println("" + throwable);
            throwable.printStackTrace(System.out);
        }
    }

    private void notifyContext() {
        WebappContextNotifier notifier = new WebappContextNotifier();
        (new Thread(notifier)).start();
    }

    private void setPermissions() {
        if (System.getSecurityManager() == null)
            return;
        if (!(container instanceof Context))
            return;
        ServletContext servletContext =
                ((Context) container).getServletContext();
        File workDir =
                (File) servletContext.getAttribute(Globals.WORK_DIR_ATTR);
        if (workDir != null) {
            try {
                String workDirPath = workDir.getCanonicalPath();
                classLoader.addPermission
                        (new FilePermission(workDirPath, "read,write"));
                classLoader.addPermission
                        (new FilePermission(workDirPath + File.separator + "-",
                                "read,write,delete"));
            } catch (IOException e) {
            }
        }
        try {
            URL rootURL = servletContext.getResource("/");
            classLoader.addPermission(rootURL);
            String contextRoot = servletContext.getRealPath("/");
            if (contextRoot != null) {
                try {
                    contextRoot =
                            (new File(contextRoot)).getCanonicalPath()
                                    + File.separator;
                    classLoader.addPermission(contextRoot);
                } catch (IOException e) {
                }
            }
            URL classesURL =
                    servletContext.getResource("/WEB-INF/classes/");
            if (classesURL != null)
                classLoader.addPermission(classesURL);
            URL libURL = servletContext.getResource("/WEB-INF/lib/");
            if (libURL != null) {
                classLoader.addPermission(libURL);
            }
            if (contextRoot != null) {
                if (libURL != null) {
                    File rootDir = new File(contextRoot);
                    File libDir = new File(rootDir, "WEB-INF/lib/");
                    String path = null;
                    try {
                        path = libDir.getCanonicalPath() + File.separator;
                    } catch (IOException e) {
                    }
                    if (path != null)
                        classLoader.addPermission(path);
                }
            } else {
                if (workDir != null) {
                    if (libURL != null) {
                        File libDir = new File(workDir, "WEB-INF/lib/");
                        String path = null;
                        try {
                            path = libDir.getCanonicalPath() + File.separator;
                        } catch (IOException e) {
                        }
                        classLoader.addPermission(path);
                    }
                    if (classesURL != null) {
                        File classesDir =
                                new File(workDir, "WEB-INF/classes/");
                        String path = null;
                        try {
                            path = classesDir.getCanonicalPath()
                                    + File.separator;
                        } catch (IOException e) {
                        }
                        classLoader.addPermission(path);
                    }
                }
            }
        } catch (MalformedURLException e) {
        }
    }

    private void setRepositories() {
        if (!(container instanceof Context))
            return;
        ServletContext servletContext =
                ((Context) container).getServletContext();
        if (servletContext == null)
            return;
        File workDir =
                (File) servletContext.getAttribute(Globals.WORK_DIR_ATTR);
        if (workDir == null)
            return;
        log(sm.getString("webappLoader.deploy", workDir.getAbsolutePath()));
        DirContext resources = container.getResources();
        String classesPath = "/WEB-INF/classes";
        DirContext classes = null;
        try {
            Object object = resources.lookup(classesPath);
            if (object instanceof DirContext) {
                classes = (DirContext) object;
            }
        } catch (NamingException e) {}

        if (classes != null) {
            File classRepository = null;
            String absoluteClassesPath = servletContext.getRealPath(classesPath);

            if (absoluteClassesPath != null) {
                classRepository = new File(absoluteClassesPath);
            } else {
                classRepository = new File(workDir, classesPath);
                classRepository.mkdirs();
                copyDir(classes, classRepository);
            }
            log(sm.getString("webappLoader.classDeploy", classesPath, classRepository.getAbsolutePath()));
            classLoader.addRepository(classesPath + "/", classRepository);
        }
        String libPath = "/WEB-INF/lib";
        classLoader.setJarPath(libPath);
        DirContext libDir = null;
        try {
            Object object = resources.lookup(libPath);
            if (object instanceof DirContext)
                libDir = (DirContext) object;
        } catch (NamingException e) {
        }
        if (libDir != null) {
            boolean copyJars = false;
            String absoluteLibPath = servletContext.getRealPath(libPath);
            File destDir = null;
            if (absoluteLibPath != null) {
                destDir = new File(absoluteLibPath);
            } else {
                copyJars = true;
                destDir = new File(workDir, libPath);
                destDir.mkdirs();
            }
            try {
                NamingEnumeration enum =resources.listBindings(libPath);
                while ( enum.hasMoreElements()){
                    Binding binding = (Binding) enum.nextElement();
                    String filename = libPath + "/" + binding.getName();
                    if (!filename.endsWith(".jar"))
                        continue;
                    File destFile = new File(destDir, binding.getName());
                    log(sm.getString("webappLoader.jarDeploy", filename,
                            destFile.getAbsolutePath()));
                    Resource jarResource = (Resource) binding.getObject();
                    if (copyJars) {
                        if (!copy(jarResource.streamContent(),
                                new FileOutputStream(destFile)))
                            continue;
                    }
                    JarFile jarFile = new JarFile(destFile);
                    classLoader.addJar(filename, jarFile, destFile);
                }
            } catch (NamingException e) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setClassPath() {
        if (!(container instanceof Context))
            return;
        ServletContext servletContext =
                ((Context) container).getServletContext();
        if (servletContext == null)
            return;
        StringBuffer classpath = new StringBuffer();
        ClassLoader loader = getClassLoader();
        int layers = 0;
        int n = 0;
        while ((layers < 3) && (loader != null)) {
            if (!(loader instanceof URLClassLoader))
                break;
            URL repositories[] =
                    ((URLClassLoader) loader).getURLs();
            for (int i = 0; i < repositories.length; i++) {
                String repository = repositories[i].toString();
                if (repository.startsWith("file://"))
                    repository = repository.substring(7);
                else if (repository.startsWith("file:"))
                    repository = repository.substring(5);
                else if (repository.startsWith("jndi:"))
                    repository =
                            servletContext.getRealPath(repository.substring(5));
                else
                    continue;
                if (repository == null)
                    continue;
                if (n > 0)
                    classpath.append(File.pathSeparator);
                classpath.append(repository);
                n++;
            }
            loader = loader.getParent();
            layers++;
        }
        servletContext.setAttribute(Globals.CLASS_PATH_ATTR,
                classpath.toString());
    }

    private boolean copyDir(DirContext srcDir, File destDir) {
        try {
            NamingEnumeration enum =srcDir.list("");
            while ( enum.hasMoreElements()){
                NameClassPair ncPair =
                        (NameClassPair) enum.nextElement();
                String name = ncPair.getName();
                Object object = srcDir.lookup(name);
                File currentFile = new File(destDir, name);
                if (object instanceof Resource) {
                    InputStream is = ((Resource) object).streamContent();
                    OutputStream os = new FileOutputStream(currentFile);
                    if (!copy(is, os))
                        return false;
                } else if (object instanceof InputStream) {
                    OutputStream os = new FileOutputStream(currentFile);
                    if (!copy((InputStream) object, os))
                        return false;
                } else if (object instanceof DirContext) {
                    currentFile.mkdir();
                    copyDir((DirContext) object, currentFile);
                }
            }
        } catch (NamingException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private boolean copy(InputStream is, OutputStream os) {
        try {
            byte[] buf = new byte[4096];
            while (true) {
                int len = is.read(buf);
                if (len < 0)
                    break;
                os.write(buf, 0, len);
            }
            is.close();
            os.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private void threadSleep() {
        try {
            Thread.sleep(checkInterval * 1000L);
        } catch (InterruptedException e) {
            ;
        }
    }

    private void threadStart() {
        if (thread != null)
            return;
        if (!reloadable)
            throw new IllegalStateException
                    (sm.getString("webappLoader.notReloadable"));
        if (!(container instanceof Context))
            throw new IllegalStateException
                    (sm.getString("webappLoader.notContext"));
        if (debug >= 1)
            log(" Starting background thread");
        threadDone = false;
        threadName = "WebappLoader[" + container.getName() + "]";
        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.start();
    }

    private void threadStop() {
        if (thread == null)
            return;
        if (debug >= 1)
            log(" Stopping background thread");
        threadDone = true;
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            ;
        }
        thread = null;
    }

    private void validatePackages() throws LifecycleException {
        ClassLoader classLoader = getClassLoader();
        if (classLoader instanceof WebappClassLoader) {
            Extension available[] =
                    ((WebappClassLoader) classLoader).findAvailable();
            Extension required[] =
                    ((WebappClassLoader) classLoader).findRequired();
            if (debug >= 1)
                log("Optional Packages:  available=" +
                        available.length + ", required=" +
                        required.length);
            for (int i = 0; i < required.length; i++) {
                if (debug >= 1)
                    log("Checking for required package " + required[i]);
                boolean found = false;
                for (int j = 0; j < available.length; j++) {
                    if (available[j].isCompatibleWith(required[i])) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    throw new LifecycleException
                            ("Missing optional package " + required[i]);
            }
        }
    }

    public void run() {
        if (debug >= 1)
            log("BACKGROUND THREAD Starting");
        while (!threadDone) {
            threadSleep();
            if (!started)
                break;
            try {
                if (!classLoader.modified())
                    continue;
            } catch (Exception e) {
                log(sm.getString("webappLoader.failModifiedCheck"), e);
                continue;
            }
            notifyContext();
            break;
        }
        if (debug >= 1)
            log("BACKGROUND THREAD Stopping");
    }

    protected class WebappContextNotifier implements Runnable {
        public void run() {
            ((Context) container).reload();
        }
    }
}
