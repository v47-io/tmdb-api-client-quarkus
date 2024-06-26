/*
 * The Clear BSD License
 *
 * Copyright (c) 2022, the tmdb-api-client authors
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted (subject to the limitations in the disclaimer
 * below) provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *
 *      * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 *      * Neither the name of the copyright holder nor the names of its
 *      contributors may be used to endorse or promote products derived from this
 *      software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY
 * THIS LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package io.v47.tmdb.http.quarkus;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.IgnoreSplitPackageBuildItem;
import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.ExtensionSslNativeSupportBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.jackson.spi.ClassPathJacksonModuleBuildItem;
import io.v47.tmdb.http.QuarkusHttpClientConfiguration;
import io.v47.tmdb.http.api.RawErrorResponse;
import io.v47.tmdb.http.impl.ConfigApiKeyProvider;
import io.v47.tmdb.jackson.TmdbApiModule;
import io.v47.tmdb.model.TmdbType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

class QuarkusTmdbProcessor {
    private static final String FEATURE = "tmdb-api-client";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public void enableSslNativeSupport(BuildProducer<ExtensionSslNativeSupportBuildItem> sslNativeSupport) {
        sslNativeSupport.produce(new ExtensionSslNativeSupportBuildItem(FEATURE));
    }

    @BuildStep
    public void registerJacksonModules(BuildProducer<ClassPathJacksonModuleBuildItem> jacksonModules) {
        jacksonModules.produce(new ClassPathJacksonModuleBuildItem(TmdbApiModule.class.getName()));
    }

    @BuildStep
    public void addDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        indexDependency.produce(new IndexDependencyBuildItem("io.v47.tmdb-api-client", "api"));
        indexDependency.produce(new IndexDependencyBuildItem("io.v47.tmdb-api-client", "core"));
    }

    @BuildStep
    public void registerTmdbTypes(CombinedIndexBuildItem combinedIndex,
                                  BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(RawErrorResponse.class).methods(true).build());
        reflectiveClass.produce(ReflectiveClassBuildItem.builder(TmdbType.class).methods(true).build());

        combinedIndex.getIndex().getAllKnownSubclasses(TmdbType.class).forEach(classInfo -> {
            Type jandexType = Type.create(classInfo.name(), Type.Kind.CLASS);
            reflectiveClass.produce(ReflectiveClassBuildItem.builder(jandexType.name().toString())
                                                            .methods(true)
                                                            .build());
        });

        combinedIndex.getIndex()
                     .getKnownClasses()
                     .stream()
                     .filter(cI -> cI.name().packagePrefix().equals("io.v47.tmdb.jackson.mixins"))
                     .forEach(cI -> reflectiveClass.produce(ReflectiveClassBuildItem.builder(cI.name().toString())
                                                                                    .methods(true)
                                                                                    .build()));
    }

    @BuildStep
    public void registerBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(new AdditionalBeanBuildItem(ConfigApiKeyProvider.class));
        additionalBeans.produce(new AdditionalBeanBuildItem(QuarkusHttpClientConfiguration.class));
    }

    @BuildStep
    public void ignoreSplitTmdbPackages(BuildProducer<IgnoreSplitPackageBuildItem> ignoreSplitPackage) {
        List<String> ignoredPackages = Arrays.asList("io.v47.tmdb.http",
                                                     "io.v47.tmdb.utils",
                                                     "io.v47.tmdb.jackson",
                                                     "io.v47.tmdb.http.impl",
                                                     "io.v47.tmdb.jackson.mixins",
                                                     "io.v47.tmdb");

        ignoreSplitPackage.produce(new IgnoreSplitPackageBuildItem(ignoredPackages));
    }

    @BuildStep(onlyIf = IsTckActive.class)
    public void addTestDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
        indexDependency.produce(new IndexDependencyBuildItem("io.v47.tmdb-api-client", "http-client-tck"));
    }

    @BuildStep(onlyIf = IsTckActive.class)
    public void registerTckClasses(BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {
        List<String> tckClasses = Arrays.asList("io.v47.tmdb.http.tck.tests.AbstractTckTest",
                                                "io.v47.tmdb.http.tck.tests.ValidComplexResponseTest",
                                                "io.v47.tmdb.http.tck.tests.ValidSimpleResponseTest");

        List<String> tckSerializableClasses = Arrays.asList("io.v47.tmdb.http.tck.TckResult",
                                                            "io.v47.tmdb.http.tck.TckResult$Success",
                                                            "io.v47.tmdb.http.tck.TckResult$Failure",
                                                            "io.v47.tmdb.http.tck.tests.ValidComplexResponseTest$CompanyAlternativeNames",
                                                            "io.v47.tmdb.http.tck.tests.ValidComplexResponseTest$CompanyAlternativeNames$AlternativeName",
                                                            "io.v47.tmdb.http.tck.tests.ValidSimpleResponseTest$Company");

        tckClasses.forEach(name -> reflectiveClass.produce(ReflectiveClassBuildItem.builder(name)
                                                                                   .constructors(true)
                                                                                   .fields(true)
                                                                                   .build()));

        tckSerializableClasses.forEach(name -> {
            Type jandexType = Type.create(DotName.createSimple(name), Type.Kind.CLASS);

            reflectiveClass.produce(ReflectiveClassBuildItem.builder(jandexType.name().toString())
                                                            .methods(true)
                                                            .build());
        });
    }

    @BuildStep(onlyIf = IsTckActive.class)
    public void ignoreSplitTckPackages(BuildProducer<IgnoreSplitPackageBuildItem> ignoreSplitPackage) {
        ignoreSplitPackage.produce(new IgnoreSplitPackageBuildItem(Collections.singleton("io.v47.tmdb.http.tck")));
    }

    static class IsTckActive implements BooleanSupplier {
        @Override
        public boolean getAsBoolean() {
            return QuarkusClassLoader.isClassPresentAtRuntime("io.v47.tmdb.http.tck.HttpClientTck");
        }
    }
}
