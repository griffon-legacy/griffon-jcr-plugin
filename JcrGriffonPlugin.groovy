/*
 * Copyright 2012 the original author or authors.
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
    String version = '0.2'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '1.1.0 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [:]
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
Upon installation the plugin will generate the following artifacts in `$appdir/griffon-app/conf`:

 * JcrConfig.groovy - contains repository definitions.

A new dynamic method named `withJcr` will be injected into all controllers,
giving you access to a `javax.jcr.Session` object, with which you'll be able
to make calls to the repository. Remember to make all repositry calls off the EDT
otherwise your application may appear unresponsive when doing long computations
inside the EDT.

This method is aware of multiple repositories. If no repositoryName is specified when calling
it then the default repository will be selected. Here are two example usages, the first
queries against the default repository while the second queries a repository whose name has
been configured as 'internal'

    package sample
    class SampleController {
        def queryAllRepositories = {
            withJcr { repositoryName, session -> ... }
            withJcr('internal') { repositoryName, session -> ... }
        }
    }

This method is also accessible to any component through the singleton `griffon.plugins.jcr.JcrConnector`.
You can inject these methods to non-artifacts via metaclasses. Simply grab hold of a particular metaclass and call
`JcrEnhancer.enhance(metaClassInstance, jcrProviderInstance)`.

Configuration
-------------
### Dynamic method injection

The `withJcr()` dynamic method will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.jcr.injectInto = ['controller', 'service']

### Events

The following events will be triggered by this addon

 * JcrConnectStart[config, repositoryName] - triggered before connecting to the repository
 * JcrConnectEnd[repositoryName, repository] - triggered after connecting to the repository
 * JcrDisconnectStart[config, repositoryName, repository] - triggered before disconnecting from the repository
 * JcrDisconnectEnd[config, repositoryName] - triggered after disconnecting from the repository

### Multiple Repositories

The config file `JcrConfig.groovy` defines a default repository block. As the name
implies this is the repository used by default, however you can configure named repositories
by adding a new config block. For example connecting to a repository whose name is 'internal'
can be done in this way

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

Until further notice only local repositories (those that require a `repository.xml` file and a home directoy) can be configured
by this plugin.

### Example

Inspired by [Jackrabbit's First Hops][3] here's how you can write a controller that creates, queries and deletes some content

        class SampleController {
            def insert = {
                withJcr { repositoryName, session ->
                    Node root = session.getRootNode()
                    Node hello = root.addNode("hello")
                    Node world = hello.addNode("world")
                    world.setProperty("message", "Hello, World!")
                    session.save()
                }
            }

            def queryAndDelete = {
                withJcr { repositoryName, session ->
                    Node root = session.getRootNode()
                    Node node = root.getNode("hello/world")
                    println(node.getPath())
                    println(node.getProperty("message").getString())
                    root.getNode("hello").remove()
                    session.save()
                }
            }
        }

Testing
-------
The `withJcr()` dynamic method will not be automatically injected during unit testing, because addons are simply not initialized
for this kind of tests. However you can use `JcrEnhancer.enhance(metaClassInstance, jcrProviderInstance)` where 
`jcrProviderInstance` is of type `griffon.plugins.jcr.JcrProvider`. The contract for this interface looks like this

    public interface JcrProvider {
        Object withJcr(Closure closure);
        Object withJcr(String repositoryName, Closure closure);
        <T> T withJcr(CallableWithArgs<T> callable);
        <T> T withJcr(String repositoryName, CallableWithArgs<T> callable);
    }

It's up to you define how these methods need to be implemented for your tests. For example, here's an implementation that never
fails regardless of the arguments it receives

    class MyJcrProvider implements JcrProvider {
        Object withJcr(String repositoryName = 'default', Closure closure) { null }
        public <T> T withJcr(String repositoryName = 'default', CallableWithArgs<T> callable) { null }
    }

This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            JcrEnhancer.enhance(service.metaClass, new MyJcrProvider())
            // exercise service methods
        }
    }

[1]: http://jcp.org/en/jsr/detail?id=283
[2]: http://jackrabbit.apache.org
[3]: http://jackrabbit.apache.org/first-hops.html
'''
}