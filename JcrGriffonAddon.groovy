/*
 * Copyright 2012-2013 the original author or authors.
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

import griffon.core.GriffonClass
import griffon.core.GriffonApplication
import griffon.plugins.jcr.JcrConnector
import griffon.plugins.jcr.JcrEnhancer
import griffon.plugins.jcr.JcrContributionHandler

import static griffon.util.ConfigUtils.getConfigValueAsBoolean

/**
 * @author Andres Almiray
 */
class JcrGriffonAddon {
    void addonPostInit(GriffonApplication app) {
        ConfigObject config = JcrConnector.instance.createConfig(app)
        if (getConfigValueAsBoolean(app.config, 'griffon.jcr.connect.onstartup', true)) {
            JcrConnector.instance.connect(app, config)
        }
        def types = app.config.griffon?.jcr?.injectInto ?: ['controller']
        for(String type : types) {
            for(GriffonClass gc : app.artifactManager.getClassesOfType(type)) {
                if (JcrContributionHandler.isAssignableFrom(gc.clazz)) continue
                JcrEnhancer.enhance(gc.metaClass)
            }
        }
    }

    Map events = [
        ShutdownStart: { app ->
            ConfigObject config = JcrConnector.instance.createConfig(app)
            JcrConnector.instance.disconnect(app, config)
        }
    ]
}