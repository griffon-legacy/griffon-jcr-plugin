/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package griffon.plugins.jcr

import javax.jcr.*
import org.apache.jackrabbit.core.TransientRepository

import griffon.core.GriffonApplication
import griffon.util.Environment
import griffon.util.Metadata
import griffon.util.CallableWithArgs
import griffon.util.ConfigUtils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
@Singleton
final class JcrConnector implements JcrProvider {
    private static final Logger LOG = LoggerFactory.getLogger(JcrConnector)

    Object withJcr(String repositoryName = 'default', Closure closure) {
        return RepositoryHolder.instance.withJcr(repositoryName, closure)
    }

    public <T> T withJcr(String repositoryName = 'default', CallableWithArgs<T> callable) {
        return RepositoryHolder.instance.withJcr(repositoryName, callable)
    }

    // ======================================================

    ConfigObject createConfig(GriffonApplication app) {
        ConfigUtils.loadConfigWithI18n('JcrConfig')
    }

    private ConfigObject narrowConfig(ConfigObject config, String repositoryName) {
        return repositoryName == 'default' ? config.repository : config.repositories[repositoryName]
    }

    Map<String, Object> connect(GriffonApplication app, ConfigObject config, String repositoryName = 'default') {
        if (RepositoryHolder.instance.isRepositoryConnected(repositoryName)) {
            return RepositoryHolder.instance.getRepository(repositoryName)
        }

        config = narrowConfig(config, repositoryName)
        app.event('JcrConnectStart', [config, repositoryName])
        Map<String, Object> r = createRepository(config)
        RepositoryHolder.instance.setRepository(repositoryName, r)
        app.event('JcrConnectEnd', [repositoryName, r.repository])
        r
    }

    void disconnect(GriffonApplication app, ConfigObject config, String repositoryName = 'default') {
        if (RepositoryHolder.instance.isRepositoryConnected(repositoryName)) {
            config = narrowConfig(config, repositoryName)
            Map<String, Object> r = RepositoryHolder.instance.getRepository(repositoryName)
            app.event('JcrDisconnectStart', [config, repositoryName, r.repository])
            destroyRepository(config, r.repository)
            app.event('JcrDisconnectEnd', [config, repositoryName])
            RepositoryHolder.instance.disconnectRepository(repositoryName)
        }
    }

    Map<String, Object> createRepository(ConfigObject config, String repositoryName = 'default') {
        [
            repository: new TransientRepository(config.configuration, config.home),
            workspace: config.workspace ?: null,
            credentials: makeCredentials(config.credentials)
        ]
    }

    private Credentials makeCredentials(ConfigObject config) {
        if (config.username && config.password) {
            return new SimpleCredentials(config.username, config.password.toCharArray())
        }
        null
    }

    void destroyRepository(ConfigObject config, Repository repository) {
        repository.shutdown()
    }
}
