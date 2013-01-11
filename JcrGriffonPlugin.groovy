/*
 * Copyright 2011-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Andres Almiray
 */
 class JcrGriffonPlugin {
    // the plugin version
    String version = '1.0.0'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '1.2.0 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [lombok: '0.4']
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, gtk
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/griffon/griffon-jcr-plugin'

    List authors = [
        [
            name: 'Andres Almiray',
            email: 'aalmiray@yahoo.com'
        ]
    ]
    String title = 'JCR support'
    String description = '''
The JCR plugin enables the usage of the [JCR 2.0][1] specification via [Apache Jackrabbit][2].

Usage
-----
Upon installation the plugin will generate the following artifacts in
`$appdir/griffon-app/conf`:

 * JcrConfig.groovy - contains repository definitions.

A new dynamic method named `withJcr` will be injected into all controllers,
giving you access to a `javax.jcr.Session` object, with which you'll be able to
make calls to the repository. Remember to make all repository calls off the UI
thread otherwise your application may appear unresponsive when doing long
computations inside the UI thread.

This method is aware of multiple repositories. If no repositoryName is specified
when calling it then the default repository will be selected. Here are two
example usages, the first queries against the default repository while the
second queries a repository whose name has been configured as 'internal'

    package sample
    class SampleController {
        def queryAllRepositories = {
            withJcr { repositoryName, session -> ... }
            withJcr('internal') { repositoryName, session -> ... }
        }
    }

The following list enumerates all the variants of the injected method

 * `<R> R withJcr(Closure<R> stmts)`
 * `<R> R withJcr(CallableWithArgs<R> stmts)`
 * `<R> R withJcr(String sesionName, Closure<R> stmts)`
 * `<R> R withJcr(String sessionName, CallableWithArgs<R> stmts)`

These methods are also accessible to any component through the singleton
`griffon.plugins.jcr.JcrEnhancer`. You can inject these methods to
non-artifacts via metaclasses. Simply grab hold of a particular metaclass and
call `JcrEnhancer.enhance(metaClassInstance)`.

Configuration
-------------

### JcrAware AST Transformation

The preferred way to mark a class for method injection is by annotating it with
`@griffon.plugins.jcr.JcrAware`. This transformation injects the
`griffon.plugins.jcr.JcrContributionHandler` interface and default behavior
that fulfills the contract.

### Dynamic Method Injection

Dynamic methods will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.jcr.injectInto = ['controller', 'service']

Dynamic method injection wil skipped for classes implementing
`griffon.plugins.jcr.JcrContributionHandler`.

### Events

The following events will be triggered by this addon

 * JcrConnectStart[config, repositoryName] - triggered before connecting to the repository
 * JcrConnectEnd[repositoryName, session] - triggered after connecting to the repository
 * JcrDisconnectStart[config, repositoryName, session] - triggered before disconnecting from the repository
 * JcrDisconnectEnd[config, repositoryName] - triggered after disconnecting from the repository

### Multiple Stores

The config file `JcrConfig.groovy` defines a default repository block. As the
name implies this is the repository used by default, however you can configure
named repositories by adding a new config block. For example connecting to a
repository whose name is 'internal' can be done in this way

    repositories {
        internal {
            configuration = '/var/jackrabbit/internal/repository.xml'
            home = '/var/jackrabbit/internal'
            credentials {
                username = 'wallace'
                password = 'gromit'
            }
        }
    }

This block can be used inside the `environments()` block in the same way as the
default repository block is used.

### Repository Types

Until further notice only local repositories (those that require a `repository.xml`
file and a home directoy) can be configured by this plugin.

### Example

Inspired by [Jackrabbit's First Hops][3] here's how you can write a controller
that creates, queries and deletes some content

        package sample
        import javax.jcr.*
        @griffon.plugins.jcr.JcrAware
        class SampleController {
            def insert = {
                withJcr { repositoryName, session ->
                    Node root = session.rootNode
                    Node hello = root.addNode("hello")
                    Node world = hello.addNode("world")
                    world.setProperty("message", "Hello, World!")
                    session.save()
                }
            }

            def queryAndDelete = {
                withJcr { repositoryName, session ->
                    Node root = session.rootNode
                    Node node = root.getNode("hello/world")
                    println(node.path)
                    println(node.getProperty("message").getString())
                    root.getNode("hello").remove()
                    session.save()
                }
            }
        }

The plugin exposes a Java friendly API to make the exact same calls from Java,
or any other JVM language for that matter. Here's for example the previous code
rewritten in Java. Note the usage of @JcrWare on a Java class

        package sample;
        import griffon.core.GriffonApplication;
        import griffon.util.CallableWithArgs;
        import java.awt.event.ActionEvent;
        import javax.jcr.*;
        import org.codehaus.griffon.runtime.core.AbstractGriffonController;
        @griffon.plugins.jcr.JcrAware
        public class SampleController extends AbstractGriffonController {
            private SampleModel model;
        
            public void setModel(SampleModel model) {
                this.model = model;
            }
        
            public void insert(ActionEvent event) {
                withJcr(new CallableWithArgs<Void>() {
                    public Void call(Object[] args) {
                        Session session = (Session) args[1];
                        Node root = session.getRootNode();
                        Node hello = root.addNode("hello");
                        Node world = hello.addNode("world");
                        world.setProperty("message", "Hello, World!");
                        session.save();
                        return null;
                    }
                });
            }
            
            public void queryAndDelete(ActionEvent event) {
                withJcr(new CallableWithArgs<Void>() {
                    public Void call(Object[] args) {
                        Session session = (Session) args[1];
                        Node root = session.getRootNode();
                        Node node = root.getNode("hello/world");
                        System.out.println(node.getPath());
                        System.out.println(node.getProperty("message").getString());
                        root.getNode("hello").remove();
                        session.save();
                        return null;
                    }
                });
            }
        }

Testing
-------

Dynamic methods will not be automatically injected during unit testing, because
addons are simply not initialized for this kind of tests. However you can use
`JcrEnhancer.enhance(metaClassInstance, jcrProviderInstance)` where
`jcrProviderInstance` is of type `griffon.plugins.jcr.JcrProvider`.
The contract for this interface looks like this

    public interface JcrProvider {
        <R> R withJcr(Closure<R> closure);
        <R> R withJcr(CallableWithArgs<R> callable);
        <R> R withJcr(String repositoryName, Closure<R> closure);
        <R> R withJcr(String repositoryName, CallableWithArgs<R> callable);
    }

It's up to you define how these methods need to be implemented for your tests.
For example, here's an implementation that never fails regardless of the
arguments it receives

    class MyJcrProvider implements JcrProvider {
        public <R> R withJcr(Closure<R> closure) { null }
        public <R> R withJcr(CallableWithArgs<R> callable) { null }
        public <R> R withJcr(String repositoryName, Closure<R> closure) { null }
        public <R> R withJcr(String repositoryName, CallableWithArgs<R> callable) { null }
    }

This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            JcrEnhancer.enhance(service.metaClass, new MyJcrProvider())
            // exercise service methods
        }
    }

On the other hand, if the service is annotated with `@JcrAware` then usage
of `JcrEnhancer` should be avoided at all costs. Simply set
`jcrProviderInstance` on the service instance directly, like so, first the
service definition

    @griffon.plugins.jcr.JcrAware
    class MyService {
        def serviceMethod() { ... }
    }

Next is the test

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            service.jcrProvider = new MyJcrProvider()
            // exercise service methods
        }
    }

Tool Support
------------

### DSL Descriptors

This plugin provides DSL descriptors for Intellij IDEA and Eclipse (provided
you have the Groovy Eclipse plugin installed). These descriptors are found
inside the `griffon-jcr-compile-x.y.z.jar`, with locations

 * dsdl/jcr.dsld
 * gdsl/jcr.gdsl

### Lombok Support

Rewriting Java AST in a similar fashion to Groovy AST transformations is
posisble thanks to the [lombok][4] plugin.

#### JavaC

Support for this compiler is provided out-of-the-box by the command line tools.
There's no additional configuration required.

#### Eclipse

Follow the steps found in the [Lombok][4] plugin for setting up Eclipse up to
number 5.

 6. Go to the path where the `lombok.jar` was copied. This path is either found
    inside the Eclipse installation directory or in your local settings. Copy
    the following file from the project's working directory

         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/jcr-<version>/dist/griffon-jcr-compile-<version>.jar .

 6. Edit the launch script for Eclipse and tweak the boothclasspath entry so
    that includes the file you just copied

        -Xbootclasspath/a:lombok.jar:lombok-pg-<version>.jar:\
        griffon-lombok-compile-<version>.jar:griffon-jcr-compile-<version>.jar

 7. Launch Eclipse once more. Eclipse should be able to provide content assist
    for Java classes annotated with `@JcrAware`.

#### NetBeans

Follow the instructions found in [Annotation Processors Support in the NetBeans
IDE, Part I: Using Project Lombok][5]. You may need to specify
`lombok.core.AnnotationProcessor` in the list of Annotation Processors.

NetBeans should be able to provide code suggestions on Java classes annotated
with `@JcrAware`.

#### Intellij IDEA

Follow the steps found in the [Lombok][4] plugin for setting up Intellij IDEA
up to number 5.

 6. Copy `griffon-jcr-compile-<version>.jar` to the `lib` directory

         $ pwd
           $USER_HOME/Library/Application Support/IntelliJIdea11/lombok-plugin
         $ cp $USER_HOME/.griffon/<version>/projects/<project>/plugins/jcr-<version>/dist/griffon-jcr-compile-<version>.jar lib

 7. Launch IntelliJ IDEA once more. Code completion should work now for Java
    classes annotated with `@JcrAware`.

[1]: http://jcp.org/en/jsr/detail?id=283
[2]: http://jackrabbit.apache.org
[3]: http://jackrabbit.apache.org/first-hops.html
[4]: /plugin/lombok
[5]: http://netbeans.org/kb/docs/java/annotations-lombok.html
'''
}
