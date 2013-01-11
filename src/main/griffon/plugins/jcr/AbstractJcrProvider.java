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

package griffon.plugins.jcr;

import griffon.util.CallableWithArgs;
import groovy.lang.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;

import static griffon.util.GriffonNameUtils.isBlank;

/**
 * @author Andres Almiray
 */
public abstract class AbstractJcrProvider implements JcrProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractJcrProvider.class);
    private static final String DEFAULT = "default";

    public <R> R withJcr(Closure<R> closure) {
        return withJcr(DEFAULT, closure);
    }

    public <R> R withJcr(String repositoryName, Closure<R> closure) {
        if (isBlank(repositoryName)) repositoryName = DEFAULT;
        if (closure != null) {
            Session session = getSession(repositoryName);
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Executing statement on session '" + repositoryName + "'");
                }
                return closure.call(repositoryName, session);
            } finally {
                session.logout();
            }
        }
        return null;
    }

    public <R> R withJcr(CallableWithArgs<R> callable) {
        return withJcr(DEFAULT, callable);
    }

    public <R> R withJcr(String repositoryName, CallableWithArgs<R> callable) {
        if (isBlank(repositoryName)) repositoryName = DEFAULT;
        if (callable != null) {
            Session session = getSession(repositoryName);
            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Executing statement on session '" + repositoryName + "'");
                }
                callable.setArgs(new Object[]{repositoryName, session});
                return callable.call();
            } finally {
                session.logout();
            }
        }
        return null;
    }

    protected abstract Session getSession(String repositoryName);
}