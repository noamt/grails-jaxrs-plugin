/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugins.jaxrs

import spock.lang.Specification

/**
 * @author Martin Krasser
 */
class JaxrsClassesSpec extends Specification {

    def 'test Is JaxrsResource'() {
        expect:
        JaxrsClasses.isJaxrsResource(TestA)
        JaxrsClasses.isJaxrsResource(TestB)
        JaxrsClasses.isJaxrsResource(TestC)
        !JaxrsClasses.isJaxrsResource(TestD)
        !JaxrsClasses.isJaxrsResource(TestE)
    }

    def 'test Is JaxrsResourceInherit'() {
        expect:
        JaxrsClasses.isJaxrsResource(TestH1B)
        !JaxrsClasses.isJaxrsResource(TestH2B)
        JaxrsClasses.isJaxrsResource(TestH3B)
    }
}
