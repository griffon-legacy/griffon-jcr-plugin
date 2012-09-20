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

import griffon.core.GriffonApplication
import griffon.util.ApplicationHolder
import griffon.util.CallableWithArgs
import static griffon.util.GriffonNameUtils.isBlank

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.jcr.*

/**
 * @author Andres Almiray
 */
@Singleton
class RepositoryHolder implements JcrProvider {
    private static final Logger LOG = LoggerFactory.getLogger(RepositoryHolder)
    private static final Object[] LOCK = new Object[0]
    private final Map<String, Map<String, Object>> repositories = [:]

    String[] getRepositoryNames() {
        List<String> repositoryNames = new ArrayList().addAll(repositories.keySet())
        repositoryNames.toArray(new String[repositoryNames.size()])
    }

    Map<String, Object> getRepository(String repositoryName = 'default') {
        if(isBlank(repositoryName)) repositoryName = 'default'
        retrieveRepository(repositoryName)
    }

    void setRepository(String repositoryName = 'default', Map<String, Object> repository) {
        if(isBlank(repositoryName)) repositoryName = 'default'
        storeRepository(repositoryName, repository)
    }

    Object withJcr(String repositoryName = 'default', Closure closure) {
        Map<String, Object> r = fetchRepository(repositoryName)
        if(LOG.debugEnabled) LOG.debug("Executing statement on repository '$repositoryName'")
        Session session = openSession(r)
        try {
            return closure(repositoryName, session)
        } finally {
            session.logout()
        }
    }

    public <T> T withJcr(String repositoryName = 'default', CallableWithArgs<T> callable) {
        Map<String, Object> r = fetchRepository(repositoryName)
        if(LOG.debugEnabled) LOG.debug("Executing statement on repository '$repositoryName'")
        Session session = openSession(r)
        try {
            callable.args = [repositoryName, session] as Object[]
            return callable.call()
        } finally {
            session.logout()
        }
    }
    
    boolean isRepositoryConnected(String repositoryName) {
        if(isBlank(repositoryName)) repositoryName = 'default'
        retrieveRepository(repositoryName) != null
    }

    void disconnectRepository(String repositoryName) {
        if(isBlank(repositoryName)) repositoryName = 'default'
        storeRepository(repositoryName, null)
    }

    private Session openSession(Map<String, Object> config) {
        if (config.credentials) { 
            if (config.workspace) {
                return config.repository.login(config.credentials, config.workspace)
            } else {
                return config.repository.login(config.credentials)
            }
        } else if(config.workspace) {
            return config.repository.login(config.workspace)
        }
        config.repository.login()
    }

    private Map<String, Object> fetchRepository(String repositoryName) {
        if(isBlank(repositoryName)) repositoryName = 'default'
        Map<String, Object> r = retrieveRepository(repositoryName)
        if(r == null) {
            GriffonApplication app = ApplicationHolder.application
            ConfigObject config = JcrConnector.instance.createConfig(app)
            r = JcrConnector.instance.connect(app, config, repositoryName)
        }

        if(r == null) {
            throw new IllegalArgumentException("No such Repository configuration for name $repositoryName")
        }
        r
    }

    private Map<String, Object> retrieveRepository(String repositoryName) {
        synchronized(LOCK) {
            repositories[repositoryName]
        }
    }

    private void storeRepository(String repositoryName, Map<String, Object> repository) {
        synchronized(LOCK) {
            repositories[repositoryName] = repository
        }
    }
}
