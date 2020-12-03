/*
 * Copyright 2020, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.model.util;

import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.asset.AssetModelProvider;
import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.syslog.SyslogCategory;
import org.openremote.model.value.*;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.persistence.Entity;
import javax.validation.ConstraintViolation;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.reflect.Modifier.isPublic;
import static java.lang.reflect.Modifier.isStatic;
import static org.openremote.model.syslog.SyslogCategory.MODEL_AND_VALUES;

/**
 * Utility class for retrieving asset model descriptors
 * <p>
 * Custom descriptors can be added by simply adding new {@link Asset}/{@link Agent} sub types and following the discovery
 * rules described in {@link StandardModelProvider}; alternatively a custom {@link AssetModelProvider} implementation
 * can be created and discovered with the {@link ServiceLoader} or manually added to this class via
 * {@link #getModelProviders()} collection.
 */
public class AssetModelUtil {

    /**
     * Built in model provider that dynamically extracts descriptors at runtime using reflection and any registered
     * {@link AssetModelProvider}s as follows:
     * <h2>{@link AssetDescriptor}s/{@link AgentDescriptor}s</h2>
     * All concrete subtypes of the {@link Asset} class and classes that extend it are searched for public static fields of type {@link
     * AssetDescriptor} or {@link AgentDescriptor} for {@link Agent}s.
     * <p>
     * For a given {@link Asset}/{@link Agent} class only one {@link AssetDescriptor}/{@link AgentDescriptor} field
     * should be present otherwise an {@link IllegalStateException} will be thrown. An {@link Agent} class must declare
     * an {@link AgentDescriptor} rather than an {@link AssetDescriptor} and vice versa otherwise an
     * {@link IllegalStateException} will be thrown.
     * <p>
     * Each {@link AssetDescriptor} will discover its' own {@link AttributeDescriptor}s (see
     * {@link AssetDescriptor#getAttributeDescriptors})
     * <h2>{@link MetaItemDescriptor}s from {@link MetaItemType}</h2>
     * Extracts public static fields of type {@link MetaItemDescriptor} from {@link MetaItemType} and {@link Asset} classes;
     * {@link MetaItemDescriptor}s defined in {@link Asset} classes must be annotated with {@link ModelDescriptor}.
     * <h2>{@link ValueDescriptor}s from {@link ValueType}</h2>
     * Extracts public static fields of type {@link ValueDescriptor} from {@link ValueType} and {@link Asset} classes;
     * {@link ValueDescriptor}s defined in {@link Asset} classes must be annotated with {@link ModelDescriptor}.
     */
    public static class StandardModelProvider implements AssetModelProvider {

        protected static Logger LOG = SyslogCategory.getLogger(MODEL_AND_VALUES, StandardModelProvider.class);
        protected Set<Class<? extends Asset<?>>> assetClasses;

        @SuppressWarnings("unchecked")
        protected Set<Class<? extends Asset<?>>> getAssetClasses() {
            if (assetClasses == null) {

                // Search for concrete asset classes in the org.openremote package if this is slow then can look to
                // require a fixed package name - assets in different packages will need to be registered using an
                // AssetModelProvider
                Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage("org.openremote"))
                    .setScanners(
                        new SubTypesScanner(true)
                    ));

                LOG.info("Scanning classpath for concrete Asset classes");

                assetClasses = reflections.getSubTypesOf(Asset.class).stream()
                    .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                    .map(assetClass -> (Class<? extends Asset<?>>)assetClass)
                    .collect(Collectors.toSet());

                LOG.info("Found asset class count = " + assetClasses.size());
            }
            return assetClasses;
        }

        @Override
        public AssetDescriptor<?>[] getAssetDescriptors() {

            return getAssetClasses().stream().map(assetClass -> {

                AssetDescriptor<?>[] assetDescriptors = getDescriptorFields(assetClass, AssetDescriptor.class, false);

                if (assetDescriptors.length > 1) {
                    String msg = "Multiple asset descriptors found in asset class: " + assetClass.getName();
                    LOG.severe(msg);
                    throw new IllegalStateException(msg);
                }

                if (assetDescriptors.length == 0) {
                    String msg = "No asset/agent descriptor found in asset class: " + assetClass.getName();
                    LOG.severe(msg);
                    throw new IllegalStateException(msg);
                }

                if (Agent.class.isAssignableFrom(assetClass) && !(assetDescriptors[0] instanceof AgentDescriptor)) {
                    String msg = "Asset descriptor found instead of Agent descriptor on agent class: " + assetClass.getName();
                    LOG.severe(msg);
                    throw new IllegalStateException(msg);
                }

                LOG.info("Found asset descriptor in asset class '" + assetClass.getSimpleName() + "': " + assetDescriptors[0].getName());
                return assetDescriptors[0];
            }).toArray(AssetDescriptor[]::new);
        }

        @Override
        public MetaItemDescriptor<?>[] getMetaItemDescriptors() {
            return getDescriptors(MetaItemDescriptor.class, MetaItemType.class);
        }

        @Override
        public ValueDescriptor<?>[] getValueDescriptors() {
            return getDescriptors(ValueDescriptor.class, ValueType.class);
        }

        protected <T> T[] getDescriptors(Class<T> descriptorType, Class<?>...additionalClassesToSearch) {
            String descriptorTypeName = descriptorType.getSimpleName();

            LOG.info("Getting " + descriptorTypeName + " descriptors...");

            List<T> metaItemDescriptors = new ArrayList<>();
            BiConsumer<Class<?>, T> metaItemDescriptorConsumer = (type, metaItemDescriptor) -> {
                LOG.info("Found " + descriptorTypeName + " descriptor in class '" + type.getSimpleName() + "': " + metaItemDescriptor);
                metaItemDescriptors.add(metaItemDescriptor);
            };

            List<Class<?>> searchClasses = new ArrayList<>();
            if (additionalClassesToSearch != null) {
                searchClasses.addAll(Arrays.asList(additionalClassesToSearch));
            }
            searchClasses.addAll(getAssetClasses());

            searchClasses.forEach(assetClass ->
                Arrays.stream(getDescriptorFields(assetClass, descriptorType, true))
                    .forEach(descriptor -> metaItemDescriptorConsumer.accept(assetClass, descriptor)));

            T[] descriptorArray = Values.createArray(0, descriptorType);
            return metaItemDescriptors.toArray(descriptorArray);
        }

        /**
         * Extract public static field values of type fieldType from the specified class and optionally require the field
         * to be annotated with the {@link ModelDescriptor} annotation.
         */
        public static <T> T[] getDescriptorFields(Class<?> type, Class<T> fieldType, boolean requireModelDescriptor) {
            return Arrays.stream(type.getDeclaredFields())
                .filter(field ->
                    fieldType.isAssignableFrom(field.getType())
                        && isStatic(field.getModifiers())
                        && isPublic(field.getModifiers())
                        && (!requireModelDescriptor || field.getDeclaredAnnotation(ModelDescriptor.class) != null))
                .map(field -> {
                    try {
                        //noinspection unchecked
                        return (T)field.get(null);
                    } catch (IllegalAccessException e) {
                        LOG.log(Level.SEVERE, "Failed to extract descriptor field of type '" + fieldType.getName() + "' in class: " + type.getName(), e);
                        throw new IllegalStateException("Failed to extract descriptor field of type '" + fieldType.getName() + "' in class: " + type.getName());
                    }
                })
                .toArray(size -> Values.createArray(size, fieldType));
        }
    }

    public static Logger LOG = SyslogCategory.getLogger(MODEL_AND_VALUES, AssetModelUtil.class);
    protected static final List<AssetModelProvider> assetModelProviders = new ArrayList<>(Collections.singletonList(new StandardModelProvider()));
    protected static AssetDescriptor<?>[] assetDescriptors;
    protected static MetaItemDescriptor<?>[] metaItemDescriptors;
    protected static ValueDescriptor<?>[] valueDescriptors;
    protected static boolean initialised;

    static {
        // Find all service loader registered asset model providers
        ServiceLoader.load(AssetModelProvider.class).forEach(assetModelProviders::add);
    }

    protected AssetModelUtil() {
    }

    protected static AssetDescriptor<?>[] getAssetDescriptors() {
        if (!initialised) {
            initialise();
        }
        return assetDescriptors;
    }

    // TODO: Implement ability to restrict which asset types are allowed to be added to a given parent type
    public static AssetDescriptor<?>[] getAssetDescriptors(String parentId) {
        return getAssetDescriptors();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Asset<?>> Optional<AssetDescriptor<T>> getAssetDescriptor(Class<T> assetType) {
        return Arrays.stream(getAssetDescriptors())
            .filter(assetDescriptor -> assetDescriptor.getType() == assetType)
            .map(assetDescriptor -> (AssetDescriptor<T>)assetDescriptor)
            .findFirst();
    }

    public static Optional<AssetDescriptor<?>> getAssetDescriptor(String assetType) {
        return Arrays.stream(getAssetDescriptors())
            .filter(assetDescriptor -> assetDescriptor.getName().equals(assetType))
            .findFirst();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Agent<T, ?, ?>> Optional<AgentDescriptor<T, ?, ?>> getAgentDescriptor(Class<T> agentType) {
        return getAssetDescriptor(agentType)
            .map(assetDescriptor -> assetDescriptor instanceof AgentDescriptor ? (AgentDescriptor<T, ?, ?>)assetDescriptor : null);
    }

    public static Optional<AgentDescriptor<?, ?, ?>> getAgentDescriptor(String agentType) {
        return getAssetDescriptor(agentType)
            .map(assetDescriptor -> assetDescriptor instanceof AgentDescriptor ? (AgentDescriptor<?, ?, ?>)assetDescriptor : null);
    }

    public static MetaItemDescriptor<?>[] getMetaItemDescriptors() {
        if (!initialised) {
            initialise();
        }

        return metaItemDescriptors;
    }

    public static ValueDescriptor<?>[] getValueDescriptors() {
        if (!initialised) {
            initialise();
        }

        return valueDescriptors;
    }

    public static void refresh() {
        initialised = false;
    }

    public static List<AssetModelProvider> getModelProviders() {
        return assetModelProviders;
    }

    protected static void initialise() {
        try {
            initialiseOrThrow();
        } catch (IllegalStateException e) {
            LOG.log(Level.SEVERE, "Failed to initialise the asset model", e);
            throw e;
        }
    }

    /**
     * Initialise the asset model and throw an {@link IllegalStateException} exception if a problem is detected; this
     * can be called by applications at startup to fail hard and fast if the {@link AssetModelUtil} is un-usable
     */
    public static void initialiseOrThrow() throws IllegalStateException {
        List<AssetDescriptor<?>> assetDescriptors = new ArrayList<>();
        List<MetaItemDescriptor<?>> metaItemDescriptors = new ArrayList<>();
        List<ValueDescriptor<?>> valueDescriptors = new ArrayList<>();

        LOG.info("Initialising asset model...");

        getModelProviders().forEach(assetModelProvider -> {
            LOG.info("Getting descriptors from asset model provider: " + assetModelProvider.getClass().getSimpleName());
            LOG.info("Getting asset descriptors...");

            AssetDescriptor<?>[] providerAssetDescriptors = assetModelProvider.getAssetDescriptors();
            LOG.info("Found asset descriptor count = " + providerAssetDescriptors.length);
            assetDescriptors.addAll(Arrays.asList(providerAssetDescriptors));

            MetaItemDescriptor<?>[] providerMetaItemDescriptors = assetModelProvider.getMetaItemDescriptors();
            LOG.info("Found meta item descriptor count = " + providerMetaItemDescriptors.length);
            metaItemDescriptors.addAll(Arrays.asList(providerMetaItemDescriptors));

            ValueDescriptor<?>[] providerValueDescriptors = assetModelProvider.getValueDescriptors();
            LOG.info("Found value descriptor count = " + providerValueDescriptors.length);
            valueDescriptors.addAll(Arrays.asList(providerValueDescriptors));
        });

        LOG.info("Checking for duplicate descriptors...");
        Function<Stream<? extends NameHolder>, List<String>> duplicateExtractor = stream ->
            stream
                .collect(Collectors.groupingBy(NameHolder::getName, Collectors.toList()))
                .entrySet().stream()
                .filter(es -> es.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<String> duplicateAssetDescriptors = duplicateExtractor.apply(assetDescriptors.stream());
        List<String> duplicateMetaItemDescriptors = duplicateExtractor.apply(metaItemDescriptors.stream());
        List<String> duplicateValueDescriptors = duplicateExtractor.apply(valueDescriptors.stream());
        boolean duplicatesFound = !duplicateAssetDescriptors.isEmpty()
            || !duplicateMetaItemDescriptors.isEmpty() || !duplicateValueDescriptors.isEmpty();

        if (duplicatesFound) {
            duplicateAssetDescriptors.forEach(duplicate -> LOG.severe("Duplicate asset descriptor found: " + duplicate));
            duplicateMetaItemDescriptors.forEach(duplicate -> LOG.severe("Duplicate meta item descriptor found: " + duplicate));
            duplicateValueDescriptors.forEach(duplicate -> LOG.severe("Duplicate value descriptor found: " + duplicate));
            throw new IllegalStateException("One or more duplicate descriptors detected");
        }

        AssetModelUtil.assetDescriptors = assetDescriptors.toArray(new AssetDescriptor<?>[0]);
        AssetModelUtil.metaItemDescriptors = metaItemDescriptors.toArray(new MetaItemDescriptor<?>[0]);
        AssetModelUtil.valueDescriptors = valueDescriptors.toArray(new ValueDescriptor<?>[0]);
    }

    /**
     * Validates the supplied object using standard JSR-380 bean validation; therefore any passed in here must follow
     * the JSR-380 annotation requirements.
     */
    // TODO: Implement validation using javax bean validation JSR-380
    public static ConstraintViolation<?>[] validate(@NotNull Object asset) {


//        AssetDescriptor<?> descriptor = getAssetDescriptor(asset.getType())
//            .orElseThrow(() -> new IllegalStateException("Cannot find asset descriptor for asset type: " + asset.getType()));
//
//        Arrays.stream(descriptor.getAttributeDescriptors()).forEach(
//            attributeDescriptor -> {
//
//            }
//        );

//        asset .getAttributes().stream().forEach(assetAttribute -> {
//            AssetModelUtil.getAttributeDescriptor(assetAttribute.name).ifPresent(wellKnownAttribute -> {
//                //Check if the type matches
//                if (!wellKnownAttribute.getValueDescriptor().equals(assetAttribute.getTypeOrThrow())) {
//                    throw new IllegalStateException(
//                        String.format("Well known attribute isn't of the correct type. Attribute name: %s. Expected type: %s",
//                            assetAttribute.name, wellKnownAttribute.getValueDescriptor().getName()));
//                }
//
//                //Check if the value is valid
//                wellKnownAttribute.getValueDescriptor()
//                    .getValidator().flatMap(v -> v.apply(assetAttribute.getValue().orElseThrow(() -> new IllegalStateException("Value is empty for " + assetAttribute.name))))
//                    .ifPresent(validationFailure -> {
//                        throw new IllegalStateException(
//                            String.format("Validation failed for %s with reason %s", assetAttribute.name, validationFailure.getReason().name())
//                        );
//                    });
//            });
//        });

        return new ConstraintViolation[0];
    }
}
