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

import org.apache.commons.lang3.StringUtils;
import org.openremote.model.ModelDescriptor;
import org.openremote.model.ModelDescriptors;
import org.openremote.model.StandardModelProvider;
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.AssetDescriptor;
import org.openremote.model.AssetModelProvider;
import org.openremote.model.asset.agent.Agent;
import org.openremote.model.asset.agent.AgentDescriptor;
import org.openremote.model.syslog.SyslogCategory;
import org.openremote.model.value.*;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.validation.ConstraintViolation;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
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
@SuppressWarnings("unchecked")
public final class AssetModelUtil {

    /**
     * Copied from: https://puredanger.github.io/tech.puredanger.com/2006/11/29/writing-a-class-hierarchy-comparator/
     */
    protected static class ClassHierarchyComparator implements Comparator<Class<?>> {

        public int compare(Class<?> c1, Class<?> c2) {
            if(c1 == null) {
                if(c2 == null) {
                    return 0;
                } else {
                    // Sort nulls first
                    return 1;
                }
            } else if(c2 == null) {
                // Sort nulls first
                return -1;
            }

            // At this point, we know that c1 and c2 are not null
            if(c1.equals(c2)) {
                return 0;
            }

            // At this point, c1 and c2 are not null and not equal, here we
            // compare them to see which is "higher" in the class hierarchy
            boolean c1Lower = c2.isAssignableFrom(c1);
            boolean c2Lower = c1.isAssignableFrom(c2);

            if(c1Lower && !c2Lower) {
                return 1;
            } else if(c2Lower && !c1Lower) {
                return -1;
            }

            // Doesn't matter, sort consistently on classname
            return c1.getName().compareTo(c2.getName());
        }
    }

    public static Logger LOG = SyslogCategory.getLogger(MODEL_AND_VALUES, AssetModelUtil.class);
    // Preload the Standard model provider so it takes priority over others
    protected static final List<AssetModelProvider> assetModelProviders = new ArrayList<>(Collections.singletonList(new StandardModelProvider()));
    protected static Map<Class<? extends Asset<?>>, AssetInfo> assetInfoMap;
    protected static Map<String, Class<? extends Asset<?>>> assetTypeMap;
    protected static AssetDescriptor<?>[] assetDescriptors;
    protected static MetaItemDescriptor<?>[] metaItemDescriptors;
    protected static ValueDescriptor<?>[] valueDescriptors;

    static {
        // Find all service loader registered asset model providers
        ServiceLoader.load(AssetModelProvider.class).forEach(assetModelProviders::add);
    }

    public static class AssetInfo {
        protected AssetDescriptor<?> assetDescriptor;


        public AssetDescriptor<?> getAssetDescriptor() {
            return assetDescriptor;
        }

        public boolean isAgent() {
            return assetDescriptor instanceof AgentDescriptor;
        }
    }

    private AssetModelUtil() {
    }

    public static AssetInfo[] getAssetInfos(String parentType) {

    }

    public static <T extends Asset<?>> Optional<AssetDescriptor<T>> getAssetInfo(Class<T> assetType) {

    }

    public static Optional<AssetDescriptor<?>> getAssetInfo(String assetType) {

    }

    // TODO: Implement ability to restrict which asset types are allowed to be added to a given parent type
    public static AssetDescriptor<?>[] getAssetDescriptors(String parentType) {
        if (assetTypeMap == null) {
            initialise();
        }
        return getAssetDescriptors();
    }

    public static <T extends Asset<?>> Optional<AssetDescriptor<T>> getAssetDescriptor(Class<T> assetType) {
        if (assetTypeMap == null) {
            initialise();
        }

        return Optional.ofNullable(assetInfoMap.get(assetType)).map(assetInfo -> (AssetDescriptor<T>)assetInfo.getAssetDescriptor());
    }

    public static Optional<AssetDescriptor<?>> getAssetDescriptor(String assetType) {
        return Arrays.stream(getAssetDescriptors())
            .filter(assetDescriptor -> assetDescriptor.getName().equals(assetType))
            .findFirst();
    }

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

    // TODO: Implement value descriptor lookup
    public static Optional<MetaItemDescriptor<?>> getMetaItemDescriptor(String name) {
        if (TextUtil.isNullOrEmpty(name)) return Optional.empty();
        return Optional.empty();
    }

    public static ValueDescriptor<?>[] getValueDescriptors() {
        if (!initialised) {
            initialise();
        }

        return valueDescriptors;
    }

    // TODO: Implement value descriptor lookup
    public static Optional<ValueDescriptor<?>> getValueDescriptor(String name) {
        if (TextUtil.isNullOrEmpty(name)) return Optional.empty();
        boolean isArray = name.endsWith("[]");
        String val = isArray ? name.substring(0, name.length() - 2) : name;
        return Optional.empty();
    }

    public static ValueDescriptor<?> getValueDescriptorForValue(Object value) {
        if (value == null) {
            return ValueType.OBJECT;
        }

        Class<?> valueClass = value.getClass();
        boolean isArray = valueClass.isArray();
        valueClass = isArray ? valueClass.getComponentType() : valueClass;
        ValueDescriptor<?> valueDescriptor = ValueType.OBJECT;

        if (valueClass == Boolean.class) valueDescriptor = ValueType.BOOLEAN;
        else if (valueClass == String.class) valueDescriptor = ValueType.STRING;
        else if (valueClass == Integer.class) valueDescriptor = ValueType.INTEGER;
        else if (valueClass == Long.class) valueDescriptor = ValueType.LONG;
        else if (valueClass == Double.class || valueClass == Float.class) valueDescriptor = ValueType.NUMBER;
        else if (valueClass == BigInteger.class) valueDescriptor = ValueType.BIG_INTEGER;
        else if (valueClass == BigDecimal.class) valueDescriptor = ValueType.BIG_NUMBER;
        else if (valueClass == Byte.class) valueDescriptor = ValueType.BYTE;
        else if (Map.class.isAssignableFrom(valueClass)) {
            Object firstElem = Values.findFirstNonNullEntry((Map<?,?>)value);

            if (firstElem == null) valueDescriptor = ValueType.OBJECT_MAP;
            else {
                boolean elemIsArray = firstElem.getClass().isArray();
                Class<?> elemClass = elemIsArray ? firstElem.getClass() : firstElem.getClass().getComponentType();
                if (elemIsArray) {
                    valueDescriptor = elemClass == String.class ? ValueType.MULTIVALUED_STRING_MAP : ValueType.OBJECT_MAP;
                } else {
                    if (elemClass == String.class)
                        valueDescriptor = ValueType.STRING_MAP;
                    else if (elemClass == Double.class || elemClass == Float.class)
                        valueDescriptor = ValueType.NUMBER_MAP;
                    else if (elemClass == Integer.class)
                        valueDescriptor = ValueType.STRING_MAP;
                    else if (elemClass == Boolean.class)
                        valueDescriptor = ValueType.BOOLEAN_MAP;
                }
            }
        }

        return isArray ? valueDescriptor.asArray() : valueDescriptor;
    }

    public static void refresh() {
        assetInfoMap = null;
        assetTypeMap = null;
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

        assetInfoMap = new HashMap<>();
        assetTypeMap = new HashMap<>();

        LOG.info("Initialising asset model...");
        Map<Class<? extends Asset<?>>, List<NameHolder>> assetDescriptorProviders = new TreeMap<>(new ClassHierarchyComparator());

        getModelProviders().forEach(assetModelProvider -> {
            LOG.fine("Processing asset model provider: " + assetModelProvider.getClass().getSimpleName());
            LOG.fine("Auto scan = " + assetModelProvider.useAutoScan());

            if (assetModelProvider.useAutoScan()) {

                Set<Class<? extends Asset<?>>> assetClasses = getAssetClasses(assetModelProvider);
                LOG.fine("Found " + assetClasses.size() + " asset class(es)");

                assetClasses.forEach(assetClass ->
                    assetDescriptorProviders.compute(assetClass, (aClass, list) -> {
                        if (list == null) {
                            list = new ArrayList<>();
                        }

                        list.addAll(getDescriptorFields(aClass));
                        return list;
                    }));

                ModelDescriptors modelDescriptors = assetModelProvider.getClass().getAnnotation(ModelDescriptors.class);
                if (modelDescriptors != null) {
                    for (ModelDescriptor modelDescriptor : modelDescriptors.value()) {
                        Class<? extends Asset<?>> assetClass = modelDescriptor.assetType();

                        assetDescriptorProviders.compute(assetClass, (aClass, list) -> {
                            if (list == null) {
                                list = new ArrayList<>();
                            }

                            list.addAll(getDescriptorFields(modelDescriptor.provider()));
                            return list;
                        });
                    }
                }
            }

            if (assetModelProvider.getAssetDescriptors() != null) {
                for (AssetDescriptor<?> assetDescriptor : assetModelProvider.getAssetDescriptors()) {
                    Class<? extends Asset<?>> assetClass = assetDescriptor.getType();

                    assetDescriptorProviders.compute(assetClass, (aClass, list) -> {
                        if (list == null) {
                            list = new ArrayList<>();
                        }

                        list.add(assetDescriptor);
                        return list;
                    });
                }
            }

            if (assetModelProvider.getAttributeDescriptors() != null) {
                assetModelProvider.getAttributeDescriptors().forEach((assetClass, attributeDescriptors) ->
                    assetDescriptorProviders.compute(assetClass, (aClass, list) -> {
                        if (list == null) {
                            list = new ArrayList<>();
                        }

                        list.addAll(attributeDescriptors);
                        return list;
                    }));
            }

            if (assetModelProvider.getMetaItemDescriptors() != null) {
                assetModelProvider.getMetaItemDescriptors().forEach((assetClass, metaDescriptors) ->
                    assetDescriptorProviders.compute(assetClass, (aClass, list) -> {
                        if (list == null) {
                            list = new ArrayList<>();
                        }

                        list.addAll(metaDescriptors);
                        return list;
                    }));
            }

            if (assetModelProvider.getValueDescriptors() != null) {
                assetModelProvider.getValueDescriptors().forEach((assetClass, valueDescriptors) ->
                    assetDescriptorProviders.compute(assetClass, (aClass, list) -> {
                        if (list == null) {
                            list = new ArrayList<>();
                        }

                        list.addAll(valueDescriptors);
                        return list;
                    }));
            }
        });

        // Build each asset info checking that no conflicts occur
        assetDescriptorProviders.forEach((assetClass, descriptors) -> {

            Class<?> currentClass = assetClass;

            while(Asset.class.isAssignableFrom(currentClass)) {
                if (Modifier.isAbstract(currentClass.getModifiers())) {
                    
                }

                currentClass = currentClass.getSuperclass();
            }
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

    protected static <T> AttributeDescriptor<?>[] extractAttributeDescriptors(Class<T> type, AttributeDescriptor<?>[] additionalAttributeDescriptors) throws IllegalArgumentException, IllegalStateException {
        Map<String, AttributeDescriptor<?>> descriptors = new HashMap<>();
        Class<?> currentType = type;

        Consumer<AttributeDescriptor<?>> descriptorConsumer = attributeDescriptor -> {
            if (descriptors.containsKey(attributeDescriptor.getName())) {
                throw new IllegalArgumentException("Duplicate attribute descriptor name '" + attributeDescriptor.getName() + "' for asset type hierarchy: " + type.getName());
            }
            descriptors.put(attributeDescriptor.getName(), attributeDescriptor);
        };

        while (currentType != Object.class) {
            Arrays.stream(type.getDeclaredFields())
                .filter(field ->
                    field.getType() == AttributeDescriptor.class && isStatic(field.getModifiers()) && isPublic(field.getModifiers()) && field.getDeclaredAnnotation(ModelDescriptor.class) != null)
                .map(descriptorField -> {
                    try {
                        AttributeDescriptor<?> descriptor = (AttributeDescriptor<?>)descriptorField.get(null);
                        // Check for corresponding getter
                        String pascalCaseName = StringUtils.capitalize(descriptor.getName());
                        Map<String, Method> getterMap = Arrays.stream(type.getDeclaredMethods()).filter(AssetDescriptor::isGetter).collect(Collectors.toMap(Method::getName, Function.identity()));
                        Method method = getterMap.containsKey("get" + pascalCaseName) ? getterMap.get("get" + pascalCaseName) : getterMap.get("is" + pascalCaseName);
                        if (method == null || method.getReturnType() != descriptor.getValueType().getType()) {
                            throw new IllegalArgumentException("Attribute descriptor '" + descriptor.getName() + "' doesn't have a corresponding getter in asset class: " + type.getName());
                        }
                        return descriptor;
                    } catch (IllegalAccessException e) {
                        throw new IllegalArgumentException("Failed to extract attribute descriptors from asset class: " + type.getName(), e);
                    }
                }).forEach(descriptorConsumer);
            currentType = currentType.getSuperclass();
        }

        if (additionalAttributeDescriptors != null) {
            Arrays.stream(additionalAttributeDescriptors).forEach(descriptorConsumer);
        }

        return descriptors.values().toArray(new AttributeDescriptor[0]);
    }

    protected static boolean isGetter(Method method) {
        if (Modifier.isPublic(method.getModifiers()) &&
            method.getParameterTypes().length == 0) {
            if (method.getName().matches("^get[A-Z].*") &&
                !method.getReturnType().equals(void.class))
                return true;
            if (method.getName().matches("^is[A-Z].*") &&
                method.getReturnType().equals(boolean.class))
                return true;
        }
        return false;
    }

    protected static Set<Class<? extends Asset<?>>> getAssetClasses(AssetModelProvider assetModelProvider) {

        Set<Class<? extends Asset<?>>> assetClasses;

        // Search for concrete asset classes in the same JAR as the provided AssetModelProvider
        Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forClass(assetModelProvider.getClass()))
            .setScanners(
                new SubTypesScanner(true)
            ));

        LOG.fine("Scanning for Asset classes");

        assetClasses = reflections.getSubTypesOf(Asset.class).stream()
            .map(assetClass -> (Class<? extends Asset<?>>)assetClass)
            .collect(Collectors.toSet());

        LOG.fine("Found asset class count = " + assetClasses.size());

        return assetClasses;
    }

    public static AssetDescriptor<?>[] getAssetDescriptors() {

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
     * Extract public static field values that are of type {@link AssetDescriptor}, {@link AttributeDescriptor}, {@link MetaItemDescriptor} or {@link ValueDescriptor}.
     */
    protected static List<NameHolder> getDescriptorFields(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
            .filter(field ->
                fieldType.isAssignableFrom(field.getType())
                    && isStatic(field.getModifiers())
                    && isPublic(field.getModifiers()))
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
