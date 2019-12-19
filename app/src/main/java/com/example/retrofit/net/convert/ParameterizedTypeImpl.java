package com.example.retrofit.net.convert;

import androidx.annotation.Nullable;

import com.example.retrofit.net.NetUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public class ParameterizedTypeImpl implements ParameterizedType {
    private final Type ownerType;
    private final Type rawType;
    private final Type[] typeArguments;

    ParameterizedTypeImpl(@Nullable Type ownerType, Type rawType, Type... typeArguments) {
        // Require an owner type if the raw type needs it.
        if (rawType instanceof Class<?>
                && (ownerType == null) != (((Class<?>) rawType).getEnclosingClass() == null)) {
            throw new IllegalArgumentException();
        }

        for (Type typeArgument : typeArguments) {
            NetUtil.checkNotNull(typeArgument, "typeArgument == null");
            NetUtil.checkNotPrimitive(typeArgument);
        }

        this.ownerType = ownerType;
        this.rawType = rawType;
        this.typeArguments = typeArguments.clone();
    }

    @Override
    public Type[] getActualTypeArguments() {
        return typeArguments.clone();
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ParameterizedType && NetUtil.typeEquals(this, (ParameterizedType) other);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(typeArguments)
                ^ rawType.hashCode()
                ^ (ownerType != null ? ownerType.hashCode() : 0);
    }

    @Override
    public String toString() {
        if (typeArguments.length == 0) return NetUtil.typeToString(rawType);
        StringBuilder result = new StringBuilder(30 * (typeArguments.length + 1));
        result.append(NetUtil.typeToString(rawType));
        result.append("<").append(NetUtil.typeToString(typeArguments[0]));
        for (int i = 1; i < typeArguments.length; i++) {
            result.append(", ").append(NetUtil.typeToString(typeArguments[i]));
        }
        return result.append(">").toString();
    }
}
