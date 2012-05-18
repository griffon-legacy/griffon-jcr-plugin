griffon.project.dependency.resolution = {
    inherits "global"
    log "warn"
    repositories {
        griffonHome()
        mavenCentral()
        mavenRepo 'http://repository.sonatype.org/content/groups/public'
        mavenRepo 'https://repository.jboss.org/nexus/content/groups/public-jboss'
    }
    dependencies {
        def jcrVersion = '2.4.1'
        compile "org.apache.jackrabbit:jackrabbit-core:$jcrVersion"
        compile 'javax.jcr:jcr:2.0'
        compile('commons-pool:commons-pool:1.5.6') {
            transitive = false
        }
    }
}

griffon {
    doc {
        logo = '<a href="http://griffon.codehaus.org" target="_blank"><img alt="The Griffon Framework" src="../img/griffon.png" border="0"/></a>'
        sponsorLogo = "<br/>"
        footer = "<br/><br/>Made with Griffon (@griffon.version@)"
    }
}

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c - %m%n')
    }

    error 'org.codehaus.griffon',
          'org.springframework',
          'org.apache.karaf',
          'groovyx.net'
    warn  'griffon'
}