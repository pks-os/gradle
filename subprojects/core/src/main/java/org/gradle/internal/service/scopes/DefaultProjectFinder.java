/*
 * Copyright 2018 the original author or authors.
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

package org.gradle.internal.service.scopes;

import org.gradle.api.artifacts.component.BuildIdentifier;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.internal.artifacts.dsl.dependencies.ProjectFinder;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.internal.build.BuildStateRegistry;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class DefaultProjectFinder implements ProjectFinder {
    private final BuildStateRegistry buildStateRegistry;
    private final Supplier<ProjectInternal> baseProjectSupplier;

    public DefaultProjectFinder(BuildStateRegistry buildStateRegistry, Supplier<ProjectInternal> baseProjectSupplier) {
        this.buildStateRegistry = buildStateRegistry;
        this.baseProjectSupplier = baseProjectSupplier;
    }

    public ProjectInternal getProject(String path) {
        return baseProjectSupplier.get().project(path);
    }

    @Override
    public ProjectInternal findProject(String path) {
        return baseProjectSupplier.get().findProject(path);
    }

    @Nullable
    @Override
    public ProjectInternal findProject(BuildIdentifier build, String path) {
        if (build.isCurrentBuild()) {
            return findProject(path);
        }
        GradleInternal gradle = buildStateRegistry.getIncludedBuild(build).getConfiguredBuild();
        return gradle.getRootProject().findProject(path);
    }
}
