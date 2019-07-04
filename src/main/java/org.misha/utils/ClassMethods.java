package org.misha.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.UtilityClass;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.collect.Maps.newIdentityHashMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@UtilityClass
public class ClassMethods {
    private static final Set<Method> skip = Sets.newHashSet(Object.class.getMethods());

    private static Set<Method> methods(Class<?> c) {
        return Arrays.stream(ReflectionUtils.getAllDeclaredMethods(c)).filter(m -> !skip.contains(m)).collect(toSet());
    }

    public static String describePublic(final Class<?> c) throws JsonProcessingException {
        final Map<String, Set<MethodDescription>> result = newIdentityHashMap();
        result.put(c.getName(), methods(c).stream()
                                          .map(m -> describe().apply(m))
                                          .filter(desc -> desc.modifiers.contains("public1") &&
                                                          !desc.name.contains("lambda"))
                                          .collect(toSet()));
        return new GsonBuilder().setPrettyPrinting().create().toJson(result);
    }

    private static Function<Method, MethodDescription> describe() {
        return m -> MethodDescription.builder()
                                     .argTypes(
                                             Arrays.stream(m.getParameterTypes()).map(Class::getName).collect(toList()))
                                     .name(m.getName())
                                     .annotations(Arrays.stream(m.getAnnotations())
                                                        .map(Annotation::toString)
                                                        .collect(toList()))
                                     .returnType(m.getReturnType().getName())
                                     .modifiers(Modifier.toString(m.getModifiers()))
                                     .build();
    }

    @AllArgsConstructor
    @Builder
    private static class MethodDescription {
        private String name;
        private String returnType;
        private List<String> argTypes = new ArrayList<>();
        private List<String> annotations = new ArrayList<>();
        private String modifiers;
    }
}
