/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.internal.resources

import org.gradle.api.Action
import spock.lang.Specification


class AbstractResourceLockRegistryTest extends Specification {
    def coordinationService = Stub(ResourceLockCoordinationService)
    def registry = new TestRegistry(coordinationService)

    def "can get a lock associated with the current thread"() {
        when:
        def lock = registry.getResourceLock("test")

        then:
        lock instanceof TestTrackedResourceLock

        when:
        lock.tryLock()

        then:
        registry.getResourceLocks() == [lock]
    }

    def "does not get locks associated with other threads"() {
        when:
        def lock = registry.getResourceLock("test")
        inNewThread { registry.getResourceLock("another").tryLock() }

        and:
        lock.tryLock()

        then:
        registry.getResourceLocks() == [lock]
    }

    def "identifies open locks in the registry"() {
        when:
        def lock = registry.getResourceLock("test")

        then:
        !registry.hasOpenLocks()

        when:
        lock.lockedState = true

        then:
        registry.hasOpenLocks()
    }

    def inNewThread(Closure closure) {
        def thread = new Thread(closure)
        thread.start()
        thread.join()
    }

    static class TestRegistry extends AbstractResourceLockRegistry {
        TestRegistry(ResourceLockCoordinationService coordinationService) {
            super(coordinationService)
        }

        def getResourceLock(String displayName) {
            return getOrRegisterResourceLock(displayName, new AbstractResourceLockRegistry.ResourceLockProducer() {
                @Override
                ResourceLock create(String name, ResourceLockCoordinationService coordinationService, Action<ResourceLock> lockAction, Action<ResourceLock> unlockAction) {
                    return new TestTrackedResourceLock(name, coordinationService, lockAction, unlockAction)
                }
            })
        }
    }
}
