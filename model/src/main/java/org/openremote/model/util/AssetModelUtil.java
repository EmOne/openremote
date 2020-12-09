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
import org.openremote.model.asset.agent.AgentLink;
import org.openremote.model.rules.AssetState;
import org.openremote.model.syslog.SyslogCategory;
import org.openremote.model.value.*;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.persistence.Entity;
import javax.validation.ConstraintViolation;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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
    protected static Map<String, Class<? extends AgentLink<?>>> agentLinkMap;
    protected static List<MetaItemDescriptor<?>> metaItemDescriptors;
    protected static List<ValueDescriptor<?>> valueDescriptors;

    static {
        // Find all service loader registered asset model providers
        ServiceLoader.load(AssetModelProvider.class).forEach(assetModelProviders::add);
    }

    public static class AssetInfo {
        protected AssetDescriptor<?> assetDescriptor;
        protected AttributeDescriptor<?>[] attributeDescriptors;
        protected MetaItemDescriptor<?>[] metaItemDescriptors;
        protected ValueDescriptor<?>[] valueDescriptors;

        public AssetInfo(AssetDescriptor<?> assetDescriptor, AttributeDescriptor<?>[] attributeDescriptors, MetaItemDescriptor<?>[] metaItemDescriptors, ValueDescriptor<?>[] valueDescriptors) {
            this.assetDescriptor = assetDescriptor;
            this.attributeDescriptors = attributeDescriptors;
            this.metaItemDescriptors = metaItemDescriptors;
            this.valueDescriptors = valueDescriptors;
        }

        public AssetDescriptor<?> getAssetDescriptor() {
            return assetDescriptor;
        }

        public AttributeDescriptor<?>[] getAttributeDescriptors() {
            return attributeDescriptors;
        }

        public MetaItemDescriptor<?>[] getMetaItemDescriptors() {
            return metaItemDescriptors;
        }

        public ValueDescriptor<?>[] getValueDescriptors() {
            return valueDescriptors;
        }

        public boolean isAgent() {
            return assetDescriptor instanceof AgentDescriptor;
        }
    }

    private AssetModelUtil() {
    }

    public static AssetInfo[] getAssetInfos(String parentType) {
        if (assetTypeMap == null) {
            initialise();
        }
        return assetInfoMap.values().toArray(new AssetInfo[0]);
    }

    public static Optional<AssetInfo> getAssetInfo(Class<? extends Asset<?>> assetType) {
        if (assetTypeMap == null) {
            initialise();
        }
        return Optional.ofNullable(assetInfoMap.get(assetType));
    }

    public static Optional<AssetInfo> getAssetInfo(String assetType) {
        if (assetTypeMap == null) {
            initialise();
        }
        Class<? extends Asset<?>> assetClass = assetTypeMap.get(assetType);
        return Optional.ofNullable(assetClass != null ? assetInfoMap.get(assetClass) : null);
    }

    // TODO: Implement ability to restrict which asset types are allowed to be added to a given parent type
    public static AssetDescriptor<?>[] getAssetDescriptors(String parentType) {
        if (assetTypeMap == null) {
            initialise();
        }
        return Arrays.stream(getAssetInfos(parentType)).map(AssetInfo::getAssetDescriptor).toArray(AssetDescriptor[]::new);
    }

    public static <T extends Asset<?>> Optional<AssetDescriptor<T>> getAssetDescriptor(Class<T> assetType) {
        if (assetTypeMap == null) {
            initialise();
        }

        return getAssetInfo(assetType).map(assetInfo -> (AssetDescriptor<T>)assetInfo.getAssetDescriptor());
    }

    public static Optional<AssetDescriptor<?>> getAssetDescriptor(String assetType) {
        if (assetTypeMap == null) {
            initialise();
        }

        return getAssetInfo(assetType).map(AssetInfo::getAssetDescriptor);
    }

    public static <T extends Agent<T, ?, ?>> Optional<AgentDescriptor<T, ?, ?>> getAgentDescriptor(Class<T> agentType) {
        return getAssetDescriptor(agentType)
            .map(assetDescriptor -> assetDescriptor instanceof AgentDescriptor ? (AgentDescriptor<T, ?, ?>)assetDescriptor : null);
    }

    public static Optional<AgentDescriptor<?, ?, ?>> getAgentDescriptor(String agentType) {
        return getAssetDescriptor(agentType)
            .map(assetDescriptor -> assetDescriptor instanceof AgentDescriptor ? (AgentDescriptor<?, ?, ?>)assetDescriptor : null);
    }

    /**
     * Get {@link AgentLink} class by its' simple class name
     */
    public static Optional<Class <? extends AgentLink<?>>> getAgentLinkClass(String agentLinkType) {
        if (assetTypeMap == null) {
            initialise();
        }

        return Optional.ofNullable(agentLinkMap.get(agentLinkType));
    }

    public static MetaItemDescriptor<?>[] getMetaItemDescriptors() {
        if (assetTypeMap == null) {
            initialise();
        }
        return metaItemDescriptors.toArray(new MetaItemDescriptor[0]);
    }

    public static Optional<MetaItemDescriptor<?>[]> getMetaItemDescriptors(Class<? extends Asset<?>> assetType) {
        return getAssetInfo(assetType).map(AssetInfo::getMetaItemDescriptors);
    }

    public static Optional<MetaItemDescriptor<?>[]> getMetaItemDescriptors(String assetType) {
        return getAssetInfo(assetType).map(AssetInfo::getMetaItemDescriptors);
    }

    public static Optional<MetaItemDescriptor<?>> getMetaItemDescriptor(String name) {
        if (TextUtil.isNullOrEmpty(name)) return Optional.empty();
        if (assetTypeMap == null) {
            initialise();
        }
        return metaItemDescriptors.stream().filter(mid -> mid.getName().equals(name)).findFirst();
    }

    public static ValueDescriptor<?>[] getValueDescriptors() {
        if (assetTypeMap == null) {
            initialise();
        }
        return valueDescriptors.toArray(new ValueDescriptor<?>[0]);
    }

    public static Optional<ValueDescriptor<?>[]> getValueDescriptors(Class<? extends Asset<?>> assetType) {
        return getAssetInfo(assetType).map(AssetInfo::getValueDescriptors);
    }

    public static Optional<ValueDescriptor<?>[]> getValueDescriptors(String assetType) {
        return getAssetInfo(assetType).map(AssetInfo::getValueDescriptors);
    }

    public static Optional<ValueDescriptor<?>> getValueDescriptor(String name) {
        if (TextUtil.isNullOrEmpty(name)) return Optional.empty();
        if (assetTypeMap == null) {
            initialise();
        }
        return valueDescriptors.stream().filter(mid -> mid.getName().equals(name)).findFirst();
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
        agentLinkMap = null;
        metaItemDescriptors = null;
        valueDescriptors = null;
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
        agentLinkMap = new HashMap<>();
        metaItemDescriptors = new ArrayList<>();
        valueDescriptors = new ArrayList<>();

        LOG.info("Initialising asset model...");
        Map<Class<? extends Asset<?>>, List<NameHolder>> assetDescriptorProviders = new TreeMap<>(new ClassHierarchyComparator());
        assetDescriptorProviders.put((Class<? extends Asset<?>>)(Class)Asset.class, new ArrayList<>(getDescriptorFields(Asset.class)));

        getModelProviders().forEach(assetModelProvider -> {
            LOG.fine("Processing asset model provider: " + assetModelProvider.getClass().getSimpleName());
            LOG.fine("Auto scan = " + assetModelProvider.useAutoScan());

            if (assetModelProvider.useAutoScan()) {

                Set<Class<? extends Asset<?>>> assetClasses = getAssetClasses(assetModelProvider);
                LOG.fine("Found " + assetClasses.size() + " asset class(es)");

                assetClasses.forEach(assetClass ->
                    assetDescriptorProviders.computeIfAbsent(assetClass, aClass ->
                        new ArrayList<>(getDescriptorFields(aClass))));

                ModelDescriptors modelDescriptors = assetModelProvider.getClass().getAnnotation(ModelDescriptors.class);
                if (modelDescriptors != null) {
                    for (ModelDescriptor modelDescriptor : modelDescriptors.value()) {
                        Class<? extends Asset<?>> assetClass = (Class<? extends Asset<?>>)modelDescriptor.assetType();

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
        Map<Class<? extends Asset<?>>, List<NameHolder>> copy = new HashMap<>(assetDescriptorProviders);
        assetDescriptorProviders.forEach((assetClass, descriptors) -> {

            // Skip abstract classes as a start point - they should be in the class hierarchy of concrete class
            if (!Modifier.isAbstract(assetClass.getModifiers())) {

                AssetInfo assetInfo = buildAssetInfo(assetClass, copy);
                assetInfoMap.put(assetClass, assetInfo);
                assetTypeMap.put(assetInfo.assetDescriptor.getName(), assetClass);

                if (assetInfo.getAssetDescriptor() instanceof AgentDescriptor) {
                    AgentDescriptor<?,?,?> agentDescriptor = (AgentDescriptor<?,?,?>)assetInfo.getAssetDescriptor();
                    agentLinkMap.put(agentDescriptor.getAgentLinkClass().getSimpleName(), agentDescriptor.getAgentLinkClass());
                }
            }
        });
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

    /**
     * Extract public static field values that are of type {@link AssetDescriptor}, {@link AttributeDescriptor}, {@link MetaItemDescriptor} or {@link ValueDescriptor}.
     */
    protected static List<NameHolder> getDescriptorFields(Class<?> type) {
        return Arrays.stream(type.getDeclaredFields())
            .filter(field ->
                isStatic(field.getModifiers())
                && isPublic(field.getModifiers())
                && (AssetDescriptor.class.isAssignableFrom(field.getType())
                    || AttributeDescriptor.class.isAssignableFrom(field.getType())
                    || MetaItemDescriptor.class.isAssignableFrom(field.getType())
                    || ValueDescriptor.class.isAssignableFrom(field.getType())))
            .map(field -> {
                try {
                    return (NameHolder)field.get(null);
                } catch (IllegalAccessException e) {
                    String msg = "Failed to extract descriptor fields from class: " + type.getName();
                    LOG.log(Level.SEVERE, msg, e);
                    throw new IllegalStateException(msg);
                }
            })
            .collect(Collectors.toList());
    }

    protected static AssetInfo buildAssetInfo(Class<? extends Asset<?>> assetClass, Map<Class<? extends Asset<?>>, List<NameHolder>> classDescriptorMap) throws IllegalStateException {

        Class<?> currentClass = assetClass;
        List<Class<?>> classTree = new ArrayList<>();

        while (Asset.class.isAssignableFrom(currentClass)) {
            classTree.add(currentClass);
            currentClass = currentClass.getSuperclass();
        }

        // Check asset class has JPA entity annotation for JPA polymorphism
        if (assetClass.getAnnotation(Entity.class) == null) {
            throw new IllegalStateException("Asset class must have @Entity JPA annotation for polymorphic JPA support: " + assetClass);
        }

        // Order from Asset class downwards
        Collections.reverse(classTree);

        AtomicReference<AssetDescriptor<?>> assetDescriptor = new AtomicReference<>();
        List<AttributeDescriptor<?>> attributeDescriptors = new ArrayList<>(10);
        List<MetaItemDescriptor<?>> metaItemDescriptors = new ArrayList<>(50);
        List<ValueDescriptor<?>> valueDescriptors = new ArrayList<>(50);

        classTree.forEach(aClass -> {
            List<NameHolder> descriptors = classDescriptorMap.get(aClass);
            if (descriptors != null) {
                descriptors.forEach(descriptor -> {
                    if (aClass == assetClass && descriptor instanceof AssetDescriptor) {
                        if (assetDescriptor.get() != null) {
                            throw new IllegalStateException("Duplicate asset descriptor found: asset type=" + assetClass +", descriptor=" + assetDescriptor.get() + ", duplicate descriptor=" + descriptor);
                        }
                        assetDescriptor.set((AssetDescriptor<?>) descriptor);
                    } else if (descriptor instanceof AttributeDescriptor) {
                        int index = attributeDescriptors.indexOf(descriptor);
                        if (index >= 0) {
                            throw new IllegalStateException("Duplicate attribute descriptor found: asset type=" + assetClass +", descriptor=" + attributeDescriptors.get(index) + ", duplicate descriptor=" + descriptor);
                        }
                        attributeDescriptors.add((AttributeDescriptor<?>) descriptor);
                    } else if (descriptor instanceof MetaItemDescriptor) {
                        int index = metaItemDescriptors.indexOf(descriptor);
                        if (index >= 0) {
                            throw new IllegalStateException("Duplicate meta item descriptor found: asset type=" + assetClass +", descriptor=" + metaItemDescriptors.get(index) + ", duplicate descriptor=" + descriptor);
                        }
                        metaItemDescriptors.add((MetaItemDescriptor<?>) descriptor);
                        if (!AssetModelUtil.metaItemDescriptors.contains(descriptor)) {
                            AssetModelUtil.metaItemDescriptors.add((MetaItemDescriptor<?>) descriptor);
                        }
                    } else if (descriptor instanceof ValueDescriptor) {
                        int index = valueDescriptors.indexOf(descriptor);
                        if (index >= 0) {
                            throw new IllegalStateException("Duplicate value descriptor found: asset type=" + assetClass +", descriptor=" + valueDescriptors.get(index) + ", duplicate descriptor=" + descriptor);
                        }
                        valueDescriptors.add((ValueDescriptor<?>) descriptor);
                        if (!AssetModelUtil.valueDescriptors.contains(descriptor)) {
                            AssetModelUtil.valueDescriptors.add((ValueDescriptor<?>) descriptor);
                        }
                    }
                });
            }
        });

        if (assetDescriptor.get() == null || assetDescriptor.get().getType() != assetClass) {
            throw new IllegalStateException("Asset descriptor not found or is not for this asset type: asset type=" + assetClass +", descriptor=" + assetDescriptor.get());
        }

        return new AssetInfo(
            assetDescriptor.get(),
            attributeDescriptors.toArray(new AttributeDescriptor<?>[0]),
            metaItemDescriptors.toArray(new MetaItemDescriptor<?>[0]),
            valueDescriptors.toArray(new ValueDescriptor<?>[0]));
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
