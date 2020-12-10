/*
 * Copyright 2018, OpenRemote Inc.
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
package org.openremote.manager.rules;

import org.openremote.container.timer.TimerService;
import org.openremote.manager.asset.AssetStorageService;
import org.openremote.model.asset.Asset;
import org.openremote.model.asset.impl.ThingAsset;
import org.openremote.model.attribute.MetaList;
import org.openremote.model.query.AssetQuery;
import org.openremote.model.query.LogicGroup;
import org.openremote.model.query.filter.AttributePredicate;
import org.openremote.model.query.filter.ParentPredicate;
import org.openremote.model.query.filter.PathPredicate;
import org.openremote.model.query.filter.TenantPredicate;
import org.openremote.model.rules.AssetState;
import org.openremote.model.util.AssetModelUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Test an {@link AssetState} with a {@link AssetQuery}.
 */
public class AssetQueryPredicate implements Predicate<AssetState> {

    final protected AssetQuery query;
    final protected TimerService timerService;
    final protected AssetStorageService assetStorageService;

    public AssetQueryPredicate(TimerService timerService, AssetStorageService assetStorageService, AssetQuery query) {
        this.timerService = timerService;
        this.assetStorageService = assetStorageService;
        this.query = query;
    }

    @Override
    public boolean test(AssetState assetState) {

        if (query.ids != null && query.ids.length > 0) {
            if (Arrays.stream(query.ids).noneMatch(id -> assetState.getId().equals(id))) {
                return false;
            }
        }

        if (query.names != null && query.names.length > 0) {
            if (Arrays.stream(query.names)
                    .map(stringPredicate -> stringPredicate.asPredicate(timerService::getCurrentTimeMillis))
                    .noneMatch(np -> np.test(assetState.getName()))) {
                return false;
            }
        }

        if (query.parents != null && query.parents.length > 0) {
            if (Arrays.stream(query.parents)
                    .map(AssetQueryPredicate::asPredicate)
                    .noneMatch(np -> np.test(assetState))) {
                return false;
            }
        }

        if (query.types != null && query.types.length > 0) {
            if (Arrays.stream(query.types).noneMatch(type ->
                        type.isAssignableFrom(
                            AssetModelUtil.getAssetDescriptor(assetState.getType())
                                .orElse(ThingAsset.DESCRIPTOR).getType()))
                    ) {
                return false;
            }
        }

        if (query.paths != null && query.paths.length > 0) {
            if (Arrays.stream(query.paths)
                    .map(AssetQueryPredicate::asPredicate)
                    .noneMatch(np -> np.test(assetState.getPath()))) {
                return false;
            }
        }

        if (query.tenant != null) {
            if (!AssetQueryPredicate.asPredicate(query.tenant).test(assetState)) {
                return false;
            }
        }

        if (query.attributes != null) {
            // TODO: LogicGroup AND doesn't make much sense when applying to a single asset state
            if (!asPredicate(timerService::getCurrentTimeMillis, query.attributes).test(assetState)) {
                return false;
            }
        }

        // Apply user ID predicate last as it is the most expensive
        if (query.userIds != null && query.userIds.length > 0) {
            return assetStorageService.isUserAsset(Arrays.asList(query.userIds), assetState.getId());
        }

        return true;
    }

    public static Predicate<AssetState> asPredicate(ParentPredicate predicate) {
        return assetState ->
            (predicate.id == null || predicate.id.equals(assetState.getParentId()))
                && (predicate.type == null || predicate.type.equals(assetState.getParentType()))
                && (predicate.name == null || predicate.name.equals(assetState.getParentName()))
                && (!predicate.noParent || assetState.getParentId() == null);
    }

    public static Predicate<String[]> asPredicate(PathPredicate predicate) {
        return givenPath -> Arrays.equals(predicate.path, givenPath);
    }

    public static Predicate<AssetState> asPredicate(TenantPredicate predicate) {
        return assetState ->
            predicate == null || (predicate.realm != null && predicate.realm.equals(assetState.getRealm()));
    }

    public static Predicate<AssetState> asPredicate(Supplier<Long> currentMillisSupplier, AttributePredicate predicate) {

        Predicate<Object> namePredicate = predicate.name != null
            ? predicate.name.asPredicate(currentMillisSupplier) : str -> true;

        Predicate<Object> valuePredicate = value -> {
            if (predicate.value == null) {
                return true;
            }
            return predicate.value.asPredicate(currentMillisSupplier).test(value);
        };

        Predicate<MetaList> metaPredicate = meta -> {
            if (predicate.meta == null || predicate.meta.length == 0) {
                return true;
            }

            return Arrays.stream(predicate.meta)
                .allMatch(metaItemPredicate -> {

                    boolean matched = true;

                    if (metaItemPredicate.mustNotExist && metaItemPredicate.name != null) {
                        matched = meta.stream()
                            .noneMatch(metaItem ->
                                metaItemPredicate.name.asPredicate(currentMillisSupplier).test(metaItem.getName())
                            );
                    } else if (metaItemPredicate.name != null) {
                        matched = meta.stream()
                            .anyMatch(metaItem ->
                                metaItemPredicate.name.asPredicate(currentMillisSupplier).test(metaItem.getName())
                            );
                    }

                    if (!matched) {
                        return false;
                    }

                    if (metaItemPredicate.value != null) {
                        // Get meta that match the name predicate and see if any of them match the value predicate
                        matched = meta.stream()
                            .filter(metaItem ->
                                metaItemPredicate.name == null
                                    || metaItemPredicate.name
                                        .asPredicate(currentMillisSupplier)
                                        .test(metaItem.getName()))
                            .anyMatch(metaItem ->
                                metaItemPredicate.value
                                    .asPredicate(currentMillisSupplier)
                                    .test(metaItem.getValue().orElse(null)));
                    }

                    return matched;
                });
        };

        Predicate<Object> oldValuePredicate = value -> {
            if (predicate.previousValue == null) {
                return true;
            }
            return predicate.previousValue.asPredicate(currentMillisSupplier).test(value);
        };

        return assetState -> namePredicate.test(assetState.getAttributeName())
            && valuePredicate.test(assetState.getValue().orElse(null))
            && oldValuePredicate.test(assetState.getOldValue().orElse(null))
            && metaPredicate.test(assetState.getMeta());
    }

    public static Predicate<AssetState> asPredicate(Supplier<Long> currentMillisProducer, LogicGroup<AttributePredicate> condition) {
        if (groupIsEmpty(condition)) {
            return as -> true;
        }

        LogicGroup.Operator operator = condition.operator == null ? LogicGroup.Operator.AND : condition.operator;

        List<Predicate<AssetState>> assetStatePredicates = new ArrayList<>();

        if (condition.getItems().size() > 0) {
            assetStatePredicates.addAll(
                condition.getItems().stream()
                            .map(p -> asPredicate(currentMillisProducer, p))
                    .collect(Collectors.toList())
            );
        }

        if (condition.groups != null && condition.groups.size() > 0) {
            assetStatePredicates.addAll(
                condition.groups.stream()
                            .map(c -> asPredicate(currentMillisProducer, c)).collect(Collectors.toList())
            );
        }

        return asPredicate(assetStatePredicates, operator);
    }

    protected static boolean groupIsEmpty(LogicGroup<?> condition) {
        return condition.getItems().size() == 0
            && (condition.groups == null || condition.groups.isEmpty());
    }

    protected static <T> Predicate<T> asPredicate(Collection<Predicate<T>> predicates, LogicGroup.Operator operator) {
        return in -> {
            boolean matched = false;

            for (Predicate<T> p : predicates) {

                if (p.test(in)) {
                    matched = true;

                    if (operator == LogicGroup.Operator.OR) {
                        break;
                    }
                } else {
                    matched = false;

                    if (operator == LogicGroup.Operator.AND) {
                        break;
                    }
                }
            }

            return matched;
        };
    }

}
