/*
 * Copyright 2012-2013 the original author or authors.
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
import griffon.util.ConfigUtils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Andres Almiray
 */
@Singleton
final class JcrConnector {
    private static final String DEFAULT = 'default'
    private static final Logger LOG = LoggerFactory.getLogger(JcrConnector)

    ConfigObject createConfig(GriffonApplication app) {
        if (!app.config.pluginConfig.jcr) {
            app.config.pluginConfig.jcr = ConfigUtils.loadConfigWithI18n('JcrConfig')
        }
        app.config.pluginConfig.jcr
    }

    private ConfigObject narrowConfig(ConfigObject config, String repositoryName) {
        if (config.containsKey('repository') && repositoryName == DEFAULT) {
            return config.repository
        } else if (config.containsKey('repositories')) {
            return config.repositories[repositoryName]
        }
        return config
    }

    Map<String, Object> connect(GriffonApplication app, ConfigObject config, String repositoryName = DEFAULT) {
        if (RepositoryHolder.instance.isRepositoryConnected(repositoryName)) {
            return RepositoryHolder.instance.getRepositoryConfiguration(repositoryName)
        }

        config = narrowConfig(config, repositoryName)
        app.event('JcrConnectStart', [config, repositoryName])
        Map<String, Object> repositoryConfig = createRepository(config)
        RepositoryHolder.instance.setRepository(repositoryName, repositoryConfig)
        app.event('JcrConnectEnd', [repositoryName, repositoryConfig.repository])
        repositoryConfig
    }

    void disconnect(GriffonApplication app, ConfigObject config, String repositoryName = DEFAULT) {
        if (RepositoryHolder.instance.isRepositoryConnected(repositoryName)) {
            config = narrowConfig(config, repositoryName)
            Map<String, Object> repositoryConfig = RepositoryHolder.instance.getRepositoryConfiguration(repositoryName)
            app.event('JcrDisconnectStart', [config, repositoryName, repositoryConfig.repository])
            destroyRepository(config, repositoryConfig.repository)
            app.event('JcrDisconnectEnd', [config, repositoryName])
            RepositoryHolder.instance.disconnectRepository(repositoryName)
        }
    }

    Map<String, Object> createRepository(ConfigObject config) {
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
