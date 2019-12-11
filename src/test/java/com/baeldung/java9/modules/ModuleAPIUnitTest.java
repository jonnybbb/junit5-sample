package com.baeldung.java9.modules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleDescriptor.*;
import java.sql.Date;
import java.sql.Driver;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModuleAPIUnitTest {

    public static final String JAVA_BASE_MODULE_NAME = "java.base";

    private Module javaBaseModule;
    private Module javaSqlModule;
    private Module module;

    @BeforeEach
    public void setUp() {
        Class<HashMap> hashMapClass = HashMap.class;
        javaBaseModule = hashMapClass.getModule();

        Class<Date> dateClass = Date.class;
        javaSqlModule = dateClass.getModule();

        Class<Person> personClass = Person.class;
        module = personClass.getModule();
    }

    @Test
    public void whenCheckingIfNamed_thenModuleIsNamed() {
        assertThat(javaBaseModule.isNamed()).isTrue();
        assertThat(javaBaseModule.getName()).isEqualTo(JAVA_BASE_MODULE_NAME);
    }

    @Test
    public void whenCheckingIfNamed_thenModuleIsUnnamed() {
        assertThat(module.isNamed()).isFalse();
        assertThat(module.getName()).isNull();
    }

    @Test
    public void whenExtractingPackagesContainedInAModule_thenModuleContainsOnlyFewOfThem() {
        assertTrue(javaBaseModule.getPackages().contains("java.lang.annotation"));
        assertFalse(javaBaseModule.getPackages().contains("java.sql"));
    }

    @Test
    public void whenRetrievingClassLoader_thenClassLoaderIsReturned() {
        assertThat(
            module.getClassLoader().getClass().getName()).
            isEqualTo("jdk.internal.loader.ClassLoaders$AppClassLoader");
    }

    @Test
    public void whenGettingAnnotationsPresentOnAModule_thenNoAnnotationsArePresent() {
        assertThat(javaBaseModule.getAnnotations().length).isEqualTo(0);
    }

    @Test
    public void whenGettingLayerOfAModule_thenModuleLayerInformationAreAvailable() {
        ModuleLayer javaBaseModuleLayer = javaBaseModule.getLayer();

        assertTrue(javaBaseModuleLayer.configuration().findModule(JAVA_BASE_MODULE_NAME).isPresent());
        assertThat(javaBaseModuleLayer.configuration().modules().size()).isGreaterThanOrEqualTo((50));
        assertTrue(javaBaseModuleLayer.parents().get(0).configuration().parents().isEmpty());
    }

    @Test
    public void whenRetrievingModuleDescriptor_thenTypeOfModuleIsInferred() {
        ModuleDescriptor javaBaseModuleDescriptor = javaBaseModule.getDescriptor();
        ModuleDescriptor javaSqlModuleDescriptor = javaSqlModule.getDescriptor();

        assertFalse(javaBaseModuleDescriptor.isAutomatic());
        assertFalse(javaBaseModuleDescriptor.isOpen());
        assertFalse(javaSqlModuleDescriptor.isAutomatic());
        assertFalse(javaSqlModuleDescriptor.isOpen());
    }

    @Test
    public void givenModuleName_whenBuildingModuleDescriptor_thenBuilt() {
        Builder moduleBuilder = ModuleDescriptor.newModule("baeldung.base");

        ModuleDescriptor moduleDescriptor = moduleBuilder.build();

        assertThat(moduleDescriptor.name()).isEqualTo(("baeldung.base"));
    }

    @Test
    public void givenModules_whenAccessingModuleDescriptorRequires_thenRequiresAreReturned() {
        Set<Requires> javaBaseRequires = javaBaseModule.getDescriptor().requires();
        Set<Requires> javaSqlRequires = javaSqlModule.getDescriptor().requires();

        Set<String> javaSqlRequiresNames = javaSqlRequires.stream()
            .map(Requires::name)
            .collect(Collectors.toSet());

        assertThat(javaBaseRequires).isEmpty();
        assertThat(javaSqlRequires.size()).isEqualTo(4);
        assertThat(javaSqlRequiresNames).containsAnyOf("java.base", "java.xml", "java.logging");
    }



    @Test
    public void whenAddingReadsToAModule_thenModuleCanReadNewModule() {
        Module updatedModule = module.addReads(javaSqlModule);

        assertTrue(updatedModule.canRead(javaSqlModule));
    }

    @Test
    public void whenExportingPackage_thenPackageIsExported() {
        Module updatedModule = module.addExports("com.baeldung.java9.modules", javaSqlModule);

        assertTrue(updatedModule.isExported("com.baeldung.java9.modules"));
    }

    @Test
    public void whenOpeningAModulePackage_thenPackagedIsOpened() {
        Module updatedModule = module.addOpens("com.baeldung.java9.modules", javaSqlModule);

        assertTrue(updatedModule.isOpen("com.baeldung.java9.modules", javaSqlModule));
    }

    @Test
    public void whenAddingUsesToModule_thenUsesIsAdded() {
        Module updatedModule = module.addUses(Driver.class);

        assertTrue(updatedModule.canUse(Driver.class));
    }



    private class Person {
        private String name;

        public Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
