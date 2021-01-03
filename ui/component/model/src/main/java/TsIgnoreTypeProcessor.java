/*
 * Copyright 2021, OpenRemote Inc.
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

import cz.habarta.typescript.generator.TsType;
import cz.habarta.typescript.generator.TypeProcessor;
import cz.habarta.typescript.generator.util.Utils;
import org.openremote.model.util.TsIgnore;

import java.lang.reflect.Type;

public class TsIgnoreTypeProcessor implements TypeProcessor {
    @Override
    public Result processType(Type javaType, Context context) {
        final Class<?> rawClass = Utils.getRawClassOrNull(javaType);
        if (rawClass != null && rawClass.getAnnotation(TsIgnore.class) != null) {
            return new Result(TsType.Any);
        }
        return null;
    }
}
