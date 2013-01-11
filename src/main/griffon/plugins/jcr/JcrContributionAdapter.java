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

import java.util.Map;

/**
 * @author Andres Almiray
 */
public class JcrContributionAdapter implements JcrContributionHandler {
    private static final String DEFAULT = "default";

    private JcrProvider provider = DefaultJcrProvider.getInstance();

    public void setJcrProvider(JcrProvider provider) {
        this.provider = provider != null ? provider : DefaultJcrProvider.getInstance();
    }

    public JcrProvider getJcrProvider() {
        return provider;
    }

    public <R> R withJcr(Closure<R> closure) {
        return withJcr(DEFAULT, closure);
    }

    public <R> R withJcr(String repositoryName, Closure<R> closure) {
        return provider.withJcr(repositoryName, closure);
    }

    public <R> R withJcr(CallableWithArgs<R> callable) {
        return withJcr(DEFAULT, callable);
    }

    public <R> R withJcr(String repositoryName, CallableWithArgs<R> callable) {
        return provider.withJcr(repositoryName, callable);
    }
}